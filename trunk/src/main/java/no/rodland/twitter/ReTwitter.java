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
    private static final Logger log = Logger.getLogger(ReTwitter.class);

    private static String twitterUser;
    private static String twitterPassword;
    private static String statusId;

    public static void main(String[] args) throws TwitterException {
        init(args);

        Twitter twitter = new Twitter(twitterUser, twitterPassword);
        twitter.setSource("web");
        try {
            long id = Long.valueOf(statusId);
            TwitterAPI.reTwitter(id, twitter);
        }
        catch (NumberFormatException nfe) {
            log.error(statusId + " is not a number, is it?");
            usage();
        }
    }


    private static void init(String[] args) {
        if (args.length == 3) {
            twitterUser = args[0];
            twitterPassword = args[1];
            statusId = args[2];
        } else {
            usage();
            System.exit(2);
        }
    }

    private static void usage() {
        System.out.println("Twitter poster");
        System.out.println("usage 2: java no.rodland.twitter.ReTwitter <twitteruser> <twitterpassword> \"<MSG-ID>\"");
    }

}
