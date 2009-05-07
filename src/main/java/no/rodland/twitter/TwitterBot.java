package no.rodland.twitter;

import twitter4j.*;

import java.util.*;

import org.apache.log4j.Logger;


public class TwitterBot {

    private static Date lastUpdate;
    static Logger log = Logger.getLogger(TwitterBot.class);

    public static void main(String[] args) throws TwitterException {
        Twitter twitter = new Twitter(Config.TWITTER_USER, Config.TWITTER_PASSWORD);
        twitter.setSource("web");
        User user = twitter.getUserDetail(Config.TWITTER_USER);
        lastUpdate = user.getStatusCreatedAt();

        if (lastUpdate == null) {
            lastUpdate = new Date(0L);
        }
        log.info("Looking for entries newer than " + lastUpdate);

        retrieveAndPost(twitter);
        FollowerRetriever followerRetriever = new FollowerRetriever(Config.getFollowerQueries(), twitter);
        followerRetriever.followNew();

        log.info("Latest status is now: " + lastUpdate);
    }

    private static void retrieveAndPost(Twitter twitter) {
        List<Posting> postings = new ArrayList<Posting>();
        postings.addAll((new RSSRetriever(Config.getFeedUrls())).retrieve());
        TwitterRetriever tr = new TwitterRetriever(Config.getTwitterQueries(), Config.TWITTER_USER);
        postings.addAll(tr.retrieve());
        Collections.sort(postings);
        // PrintUtil.printPostings(postings);
        postNewEntries(postings, twitter);
    }

    /**
     * Will post entries.  MUST BE SORTED on DATE (newest last) to work properly,.
     *
     * @param entries
     * @param twitter
     * @return
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
}
