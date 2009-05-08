package no.rodland.twitter;

import twitter4j.*;

import java.util.*;

import org.apache.log4j.Logger;
import org.apache.commons.configuration.ConfigurationException;

public class TwitterBot {

    static final Logger log = Logger.getLogger(TwitterBot.class);
    private static Date lastUpdate;
    private static String twitterUser;
    private static String twitterPassword;
    private static String cfgFile;
    private static Config cfg;

    public static void main(String[] args) {
        init(args);

        // XXX TODO: should use lastUpdated from cfg-fiel to search SINCE in all searches.
        try {
            cfg = new Config(cfgFile);
            //cfg.update();
            twitterUser = cfg.twitterUser;
            twitterPassword = cfg.twitterPassword;

            Twitter twitter = new Twitter(twitterUser, twitterPassword);
            twitter.setSource("web");
            User user = twitter.getUserDetail(twitterUser);
            lastUpdate = user.getStatusCreatedAt();
            if (lastUpdate == null) {
                lastUpdate = new Date(0L);
            }

            log.info("STARTING BOT");
            log.info("Looking for entries newer than " + lastUpdate + " for " + twitterUser);


            callTwitter(twitter);

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

    }

    private static void callTwitter(Twitter twitter) throws TwitterException {
        retrieveAndPost(twitter);
        FollowerRetriever followerRetriever = new FollowerRetriever(cfg.getFollowerQueries(), twitter);
        followerRetriever.followNew();
    }

    private static void retrieveAndPost(Twitter twitter) {
        List<Posting> postings = new ArrayList<Posting>();
        postings.addAll((new RSSRetriever(cfg.getFeedUrls())).retrieve());
        TwitterRetriever tr = new TwitterRetriever(cfg.getTwitterQueries(), twitterUser);
        postings.addAll(tr.retrieve());
        Collections.sort(postings);
        // PrintUtil.printPostings(postings);
        postNewEntries(postings, twitter);
    }


    /**
     * Will post entries.  MUST BE SORTED on DATE (newest last) to work properly,.
     *
     * @param twitter The authenticated twitter.
     * @param entries The entries to post (given that the date is newer than lastUpdate.
     */
    private static void postNewEntries(List<Posting> entries, Twitter twitter) {
        int droppedOld = 0;
        for (Posting entry : entries) {
            Date published = entry.getUpdated();
            if (lastUpdate.before(published)) {
                lastUpdate = published;
                TwitterAPI.post(twitter, entry);
            }
            else {
                droppedOld++;
            }
        }
        log.info("Dropped " + droppedOld + " posting because they were too old");
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
