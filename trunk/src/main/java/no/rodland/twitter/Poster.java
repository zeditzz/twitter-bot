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
public class Poster {
    static Logger log = Logger.getLogger(Poster.class);
    static Twitter twitter;

    private static String twitterUser;
    private static String twitterPassword;
    private static String status;

    public static void main(String[] args) throws TwitterException {
        init(args);
        if (status == null) {
            status = "Please send me feedback if you have any on the amount and quality of news from @schibstednews. I know it's not perfect, but I'm trying.";
        }
        post(status);
    }

    private static void post(String status) throws TwitterException {
        if (status.length() > Config.TWITTER_MSG_LENGTH) {
            log.error("Status must not exceed 140 characters. Will exit because it's " + status.length());
        } else {
            Twitter tw = getTwitter();
            log.info("posting status: " + status);
            log.info(tw.update(status));
        }
    }

    private static Twitter getTwitter() {
        if (twitter == null) {
            twitter = new Twitter(twitterUser, twitterPassword);
            twitter.setSource("web");
        }
        return twitter;
    }

    public static void init(String[] args) {
        if (args.length == 2) {
            twitterUser = args[0];
            twitterPassword = args[1];
        } else if (args.length == 3) {
            twitterUser = args[0];
            twitterPassword = args[1];
            status = args[3];
        } else {
            usage();
            System.exit(2);
        }
    }

    public static void usage() {
        System.out.println("Twitter poster");
        System.out.println("usage 1: java no.rodland.twitter.Poster <twitteruser> <twitterpassword>");
        System.out.println("usage 2: java no.rodland.twitter.Poster <twitteruser> <twitterpassword> \"<MSG>\"");
    }

}
