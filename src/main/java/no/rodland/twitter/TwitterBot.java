package no.rodland.twitter;

import twitter4j.*;

import java.util.*;

import org.apache.log4j.Logger;
import no.rodland.twitter.util.PrintUtil;


public class TwitterBot {

    private static Date lastUpdate;
    static Logger log = Logger.getLogger(TwitterBot.class);
    private static String twitterUser;
    private static String twitterPassword;



    public static void main(String[] args) throws TwitterException {
        init(args);

        Twitter twitter = new Twitter(twitterUser, twitterPassword);
        twitter.setSource("web");
        User user = twitter.getUserDetail(twitterUser);
        lastUpdate = user.getStatusCreatedAt();

        if (lastUpdate == null) {
            lastUpdate = new Date(0L);
        }

        log.info("STARTING BOT");
//        log.info(twitter);
//        log.info(user);
//        log.info(PrintUtil.print(user));
        log.info("Looking for entries newer than " + lastUpdate + " for " + twitterUser);


        retrieveAndPost(twitter);
        FollowerRetriever followerRetriever = new FollowerRetriever(Config.getFollowerQueries(), twitter);
        followerRetriever.followNew();

        log.info("Latest status is now: " + lastUpdate);
    }

    private static void retrieveAndPost(Twitter twitter) {
        List<Posting> postings = new ArrayList<Posting>();
        postings.addAll((new RSSRetriever(Config.getFeedUrls())).retrieve());
        TwitterRetriever tr = new TwitterRetriever(Config.getTwitterQueries(), twitterUser);
        postings.addAll(tr.retrieve());
        Collections.sort(postings);
        // PrintUtil.printPostings(postings);
        postNewEntries(postings, twitter);
    }

    /**
     * Will post entries.  MUST BE SORTED on DATE (newest last) to work properly,.
     */
    public static Date postNewEntries(List<Posting> entries, Twitter twitter) {
        int droppedOld = 0;
        for (Posting entry : entries) {
            Date published = entry.getUpdated();
            if (lastUpdate.before(published)) {
                lastUpdate = published;
                TwitterAPI.post(twitter, entry);
            } else {
                droppedOld++;
            }
        }
        log.info("Dropped " + droppedOld + " posting because they were too old");
        return lastUpdate;
    }

    public static void init(String[] args) {
        if (args.length != 2) {
            usage();
            System.exit(2);
        }
        twitterUser = args[0];
        twitterPassword = args[1];
    }

    public static void usage() {
        System.out.println("Twitter news bot");
        System.out.println("usage: java no.rodland.twitter.TwitterBot <twitteruser> <twitterpassword>");
    }
}
