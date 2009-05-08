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
    private static String twitterUser;
    private static String twitterPassword;
    private static String screenNameToFollow;

    public static void main(String[] args) throws TwitterException {
        init(args);
        follow(screenNameToFollow);
    }

    private static void follow(String screenName) {
        Twitter twitter = new Twitter(twitterUser, twitterPassword);
        twitter.setSource("web");
        log.info("follow: " + screenName);
        try {
            log.info(twitter.create(screenName));
        } catch (TwitterException e) {
            log.error("error when trying to follow", e);
        }
    }

    public static void init(String[] args) {
        if (args.length == 3) {
            twitterUser = args[0];
            twitterPassword = args[1];
            screenNameToFollow = args[2];
        } else {
            usage();
            System.exit(2);
        }
    }

    public static void usage() {
        System.out.println("Twitter poster");
        System.out.println("usage 2: java no.rodland.twitter.ReTwitter <twitteruser> <twitterpassword> <scrrenname-to-follow>");
    }
}
