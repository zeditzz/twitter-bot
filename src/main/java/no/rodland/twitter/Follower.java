package no.rodland.twitter;

import twitter4j.TwitterException;
import twitter4j.Twitter;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 6, 2009
 * Time: 10:51:26 AM
 */
public class Follower {
    static Logger log = Logger.getLogger(Follower.class);

    public static void main(String[] args) throws TwitterException {
        Twitter twitter = new Twitter(Config.TWITTER_USER, Config.TWITTER_PASSWORD);
        twitter.setSource("web");
        follow("jobbainorge", twitter);
    }

    private static void follow(String screenName, Twitter twitter) {
        log.info("follow: " + screenName);
        try {
            log.info(twitter.create(screenName));
        } catch (TwitterException e) {
            log.error("error when trying to follow", e);
        }
    }
}
