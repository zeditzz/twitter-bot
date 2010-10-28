package no.rodland.twitter.main;

import no.rodland.twitter.TwitterAPI;
import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by IntelliJ IDEA. User: fmr Date: May 6, 2009 Time: 10:51:26 AM
 */
public class ReTwitter extends AuthMain {

    private static final Logger log = Logger.getLogger(ReTwitter.class);

    public ReTwitter(String[] args) {
        super(false, args);
    }

    public static void main(String[] args) throws TwitterException {
        ReTwitter reTwitter = new ReTwitter(args);
        reTwitter.run();
    }

    @Override
    void doWork() throws TwitterException {
        Twitter twitter = getTwitter();
        try {
            long id = Long.valueOf(getPayLoad());
            TwitterAPI.reTwitter(id, twitter);
        }
        catch (NumberFormatException nfe) {
            log.error(getPayLoad() + " is not a number, is it?");
            usage();
        }
    }

    @Override
    String getShortName() {
        return "retwitter";
    }

    @Override
    String getLoadDescription() {
        return "<msg-id>";
    }
}
