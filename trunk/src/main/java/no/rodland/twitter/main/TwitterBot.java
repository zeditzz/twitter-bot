package no.rodland.twitter.main;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import no.rodland.twitter.Config;
import no.rodland.twitter.FollowerRetriever;
import no.rodland.twitter.Posting;
import no.rodland.twitter.RSSRetriever;
import no.rodland.twitter.TwitterAPI;
import no.rodland.twitter.TwitterRetriever;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class TwitterBot {

    static final Logger log = Logger.getLogger(TwitterBot.class);
    private static String cfgFile;
    private static Config cfg;
    private static String twitterUser;

    public static void main(String[] args) {
        log.info("STARTING BOT");
        init(args);
        User user = null;
        // XXX: should use lastUpdated from cfg-file to search SINCE in all searches.
        try {
            cfg = new Config(cfgFile);
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
        }
        catch (TwitterException e) {
            handleFatalException("TwitterException caught", e);
        }
        catch (Exception e) {
            handleFatalException("Exception caught, User: " + user, e);
        }
        log.info("ENDING BOT");
    }

    private static void handleFatalException(String msg, Exception e) {
        log.fatal(msg, e);
        System.err.println(msg);
        e.printStackTrace(System.err);
    }

    private static void logRateInfo(Twitter twitter) throws TwitterException {
        RateLimitStatus rls = twitter.getRateLimitStatus();
        log.info("reset-time in sec = " + rls.getResetTimeInSeconds());
        log.info("reset-time        = " + rls.getResetTime());
        log.info("sec to reset      = " + rls.getSecondsUntilReset());
        log.info("limit             = " + rls.getHourlyLimit());
        log.info("remaining calls   = " + rls.getRemainingHits());
    }

    private static Date callTwitter(Twitter twitter,
                                    Date lastUpdate,
                                    final boolean actuallyPost) throws TwitterException {
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

    private static Date postNewEntries(List<Posting> entries,
                                       Twitter twitter,
                                       Date lastUpdate,
                                       final boolean actuallyPost) {
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
                    String status = entry.getStatus();
                    log.info("New entry published at " + entry.getUpdated());
                    log.info("  status: " + status);
                    log.info("  src: " + entry.getSrc());
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
                    if (cfg.sendEmail(bad)) {
                        System.err.println("filtered out content - will not post - bad word: " + bad);
                        System.err.println("entry.getTitle()   = " + entry.getTitle());
                        System.err.println("entry.getSrc()     = " + entry.getSrc());
                        System.err.println("entry.getStatus()  = " + entry.getStatus());
                        System.err.println("entry.getUpdated() = " + published);
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

        log.info("Got " + entries.size() + " entries");
        log.info("Posted " + posted);
        log.info("Dropped " + droppedOld + " entries because they were too old");
        log.info("Dropped " + droppedBad + " entries because they had bad content");
        log.info("Dropped " + droppedMaxReached + " entries because max limit pr run was reached");

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
}
