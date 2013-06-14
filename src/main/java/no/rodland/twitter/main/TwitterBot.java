package no.rodland.twitter.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;

import no.rodland.twitter.Config;
import no.rodland.twitter.FollowerRetriever;
import no.rodland.twitter.Posting;
import no.rodland.twitter.RSSRetriever;
import no.rodland.twitter.TwitterAPI;
import no.rodland.twitter.TwitterRetriever;
import org.apache.log4j.Logger;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class TwitterBot {

    static final Logger log = Logger.getLogger(TwitterBot.class);
    private static String cfgFile;
    private static Config cfg;
    private static String twitterUser;
    private static EmailSender emailSender;

    public static void main(String[] args) {
        log.info("STARTING BOT");
        init(args);
        User user = null;
        // XXX: should use lastUpdated from cfg-file to search SINCE in all searches.
        try {
            cfg = new Config(cfgFile);
            if (cfg.sendEmailContentFilter()) {
                emailSender = new EmailSender(cfg.getSmtpUser(), cfg.getSmtpPassword(), cfg.getSmtpHost(), cfg.getSmtpFrom());
            }

            TwitterAPI.setConfig(cfg);
            Date cfgLastUpdate = cfg.getLastUpdated();
            Twitter twitter = TwitterAPI.getAuthTwitter();
            twitterUser = twitter.getScreenName();
            boolean actuallyPost = cfg.getActuallyPost();
            user = twitter.showUser(twitterUser);
            Date lastUpdate = user.getStatus().getCreatedAt();
            if (lastUpdate == null) {
                lastUpdate = new Date(0L);
            }

            if (lastUpdate.before(cfgLastUpdate)) {
                log.info("lastUpdate = " + lastUpdate + ", cfgLastUpdate = " + cfgLastUpdate);
                lastUpdate = cfgLastUpdate;
            }

            log.info("Looking for entries newer than " + lastUpdate + " for " + twitterUser);

            lastUpdate = callTwitter(twitter, lastUpdate, actuallyPost);

            logRateInfo(twitter);
            cfg.update(lastUpdate);
            log.info("Latest status is now: " + lastUpdate);
        } catch (TwitterException e) {
            handleFatalException("TwitterException caught", e);
        } catch (Exception e) {
            handleFatalException("Exception caught, User: " + user, e);
        }
        log.info("ENDING BOT");
    }

    private static Date callTwitter(Twitter twitter, Date lastUpdate, final boolean actuallyPost) throws TwitterException {
        Date lastPublished = retrieveAndPost(twitter, lastUpdate, actuallyPost);
        FollowerRetriever followerRetriever = new FollowerRetriever(twitter, cfg);
        followerRetriever.followNew();
        followerRetriever.unfollowBlackList();
        return lastPublished;
    }

    private static Date retrieveAndPost(Twitter twitter, Date lastUpdate, final boolean actuallyPost) {
        List<Posting> postings = new ArrayList<Posting>();
        postings.addAll((new RSSRetriever(cfg.getFeedUrls())).retrieve());
        TwitterRetriever tr = new TwitterRetriever(cfg.getTwitterQueries(), twitterUser, cfg);
        postings.addAll(tr.retrieve());
        Collections.sort(postings);

        return postNewEntries(postings, twitter, lastUpdate, actuallyPost);
    }

    private static Date postNewEntries(List<Posting> entries, Twitter twitter, Date lastUpdate, final boolean actuallyPost) {
        int droppedOld = 0;
        int droppedBad = 0;
        int posted = 0;
        int droppedMaxReached = 0;
        Date lastPublished = lastUpdate;
        for (Posting entry : entries) {

            Date published = entry.getUpdated();
            if (posted > cfg.getMaxPostingsPrRun()) {
                droppedMaxReached++;
            }
            else if (lastUpdate.before(published)) {   // post ALL entries newer than lastPublished
                String bad = cfg.isBadContent(entry.getStatus());
                if (bad == null) {  // not bad words
                    logPost(entry);
                    if (actuallyPost) {
                        TwitterAPI.post(twitter, entry);
                    }
                    posted++;
                    if (lastPublished.before(published)) {  // only update lastPublished if it's the newest
                        lastPublished = published;
                    }
                    lastUpdate = published;
                }
                else {
                    droppedBad++;
                    log.warn("filtered out content - will not post - bad word: " + bad);
                    log.warn(entry);
                    if (cfg.sendEmailForBadWords(bad)) {
                        sendMailBadContent(entry, published, bad);
                    }
                    else {
                        log.warn("not sending emails for this because not-email in cfg");
                    }
                }
            }
            else {
                droppedOld++;
            }
        }

        logSummary(entries, posted, droppedOld, droppedBad, droppedMaxReached);

        return lastPublished;
    }

    private static void init(String[] args) {
        if (args.length == 1) {
            cfgFile = args[0];
        }
        else {
            usage();
            System.exit(2);
        }
    }

    private static void usage() {
        System.out.println("Twitter news bot");
        System.out.println("usage: java no.rodland.twitter.main.TwitterBot <file.properties>");
    }

    private static void handleFatalException(String msg, Exception e) {
        log.fatal(msg, e);
        System.err.println(msg);
        e.printStackTrace(System.err);
    }

    private static void logRateInfo(Twitter twitter) throws TwitterException {
        Map<String, RateLimitStatus> rls = twitter.getRateLimitStatus();
        log.info(rls);
    }

    private static void logPost(Posting entry) {
        log.info("New entry published at " + entry.getUpdated());
        log.info("  status: " + entry.getStatus());
        log.info("  src: " + entry.getSrc());
    }

    private static void logSummary(List<Posting> entries, int posted, int droppedOld, int droppedBad, int droppedMaxReached) {
        log.info("Got " + entries.size() + " entries");
        log.info("Posted " + posted);
        log.info("Dropped " + droppedOld + " entries because they were too old");
        log.info("Dropped " + droppedBad + " entries because they had bad content");
        log.info("Dropped " + droppedMaxReached + " entries because max limit pr run was reached");
    }

    private static void sendMailBadContent(Posting entry, Date published, String bad) {
        String subject = "twitterbot: filtered bad word: " + bad;
        String body = subject + "\n";
        body += "entry.getTitle()   = " + entry.getTitle() + "/n";
        body += "entry.getSrc()     = " + entry.getSrc() + "/n";
        body += "entry.getStatus()  = " + entry.getStatus() + "/n";
        body += "entry.getUpdated() = " + published + "/n";

        try {
            if (emailSender != null) {
                emailSender.send(subject, body, cfg.getEmailContentFilter());
            }
            else {
                log.warn("Emailsender not set up");
                // XXX should be deleted when email works.
                System.err.println("filtered out content - will not post - bad word: " + bad);
                System.err.println();
                System.err.println("entry.getUpdated() = " + published);
                System.err.println("entry.getStatus()  = " + entry.getStatus());
                System.err.println("entry.getSrc()     = " + entry.getSrc());
                System.err.println();
            }
        } catch (MessagingException e) {
            log.error("Error sending mail", e);
        }
    }
}
