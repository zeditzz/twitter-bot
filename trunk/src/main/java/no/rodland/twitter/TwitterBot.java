package no.rodland.twitter;

import twitter4j.*;

import java.util.*;

import org.apache.log4j.Logger;
import org.apache.commons.configuration.ConfigurationException;

public class TwitterBot {

    static final Logger log = Logger.getLogger(TwitterBot.class);
    private static String twitterUser;
    private static String cfgFile;
    private static Config cfg;

    public static void main(String[] args) {
        log.info("STARTING BOT");
        init(args);

        // XXX: should use lastUpdated from cfg-fiel to search SINCE in all searches.
        try {
            cfg = new Config(cfgFile);
            twitterUser = cfg.twitterUser;
            String twitterPassword = cfg.twitterPassword;
            Date cfgLastUpdate = cfg.getLastUpdated();
            Twitter twitter = new Twitter(twitterUser, twitterPassword);
            twitter.setSource("web");
            User user = twitter.getUserDetail(twitterUser);
            Date lastUpdate = user.getStatusCreatedAt();
            if (lastUpdate == null) {
                lastUpdate = new Date(0L);
            }

            if (lastUpdate.before(cfgLastUpdate)) {
                log.info("lastUpdate = " + lastUpdate + ", cfgLastUpdate = " + cfgLastUpdate);
                lastUpdate = cfgLastUpdate;
            }

            log.info("Looking for entries newer than " + lastUpdate + " for " + twitterUser);

            lastUpdate = callTwitter(twitter, lastUpdate);

            logRateInfo(twitter);
            cfg.update(lastUpdate);
            log.info("Latest status is now: " + lastUpdate);
        }
        catch (ConfigurationException e) {
            log.fatal("config not loaded for file: " + cfgFile, e);
            System.exit(3);
        }
        catch (TwitterException e) {
            log.fatal("TwitterException caught: ", e);
            System.exit(4);
        }
        log.info("ENDING BOT");
    }

    private static void logRateInfo(Twitter twitter) throws TwitterException {
        RateLimitStatus rls = twitter.rateLimitStatus();
        log.info("reset-time in sec = " + rls.getResetTimeInSeconds());
        log.info("rate limit reset = " + rls.getRateLimitReset());
        log.info("reset-time = " + rls.getDateTime());
        log.info("limit = " + rls.getHourlyLimit() + ", remaining calls = " + rls.getRemainingHits());
    }

    private static Date callTwitter(Twitter twitter, Date lastUpdate) throws TwitterException {
        Date lastPublished = retrieveAndPost(twitter, lastUpdate);
        FollowerRetriever followerRetriever = new FollowerRetriever(twitter, cfg);
        followerRetriever.followNew();
        followerRetriever.unfollowBlackList();
        return lastPublished;
    }

    private static Date retrieveAndPost(Twitter twitter, Date lastUpdate) {
        List<Posting> postings = new ArrayList<Posting>();
        postings.addAll((new RSSRetriever(cfg.getFeedUrls())).retrieve());
        TwitterRetriever tr = new TwitterRetriever(cfg.getTwitterQueries(), twitterUser, cfg);
        postings.addAll(tr.retrieve());
        Collections.sort(postings);
        return postNewEntries(postings, twitter, lastUpdate);
    }

    private static Date postNewEntries(List<Posting> entries, Twitter twitter, Date lastUpdate) {
        int droppedOld = 0;
        Date lastPublished = lastUpdate;
        for (Posting entry : entries) {

            Date published = entry.getUpdated();
            if (lastUpdate.before(published)) {   // post ALL entries newer than lastPublished
                String bad = cfg.isBadContent(entry.getStatus());
                if (bad == null) {  // not bad words
                    TwitterAPI.post(twitter, entry);
                    if (lastPublished.before(published)) {  // only update lastPublished if it's the newest
                        lastPublished = published;
                    }
                    lastUpdate = published;
                }
                else {
                    log.warn("filtered out content - will not post - bad word: " + bad);
                    log.warn(entry);
                }
            }
            else {
                droppedOld++;
            }
        }
        log.info("Dropped " + droppedOld + " posting because they were too old");
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
        System.out.println("usage: java no.rodland.twitter.TwitterBot <file.properties>");
    }
}
