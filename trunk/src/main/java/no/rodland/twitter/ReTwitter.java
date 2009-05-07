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
public class ReTwitter {
    static Logger log = Logger.getLogger(ReTwitter.class);

    public static void main(String[] args) throws TwitterException {
        Twitter twitter = new Twitter(Config.TWITTER_USER, Config.TWITTER_PASSWORD);
        twitter.setSource("web");
        TwitterAPI.reTwitter(1715064044, twitter);
        TwitterAPI.reTwitter(1715384404, twitter);
        TwitterAPI.reTwitter(1713899058, twitter);
    }
}
