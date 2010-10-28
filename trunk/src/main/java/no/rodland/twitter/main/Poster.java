package no.rodland.twitter.main;

import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by IntelliJ IDEA. User: fmr Date: May 6, 2009 Time: 10:51:26 AM
 */
public class Poster extends AuthMain {

    private static final Logger log = Logger.getLogger(Poster.class);

    public Poster(String[] args) {
        super(false, args);
    }

    public static void main(String[] args) throws TwitterException {
        Poster poster = new Poster(args);
        poster.run();
    }

    @Override
    String getShortName() {
        return "poster";
    }

    @Override
    String getLoadDescription() {
        return "\"<MSG>\"";
    }

    @Override
    void doWork() throws TwitterException {
        String status = getPayLoad();
        if (status.length() > 140) {
            log.error("Status must not exceed 140 characters. Will exit because it's " + status.length());
        }
        else {
            Twitter twitter = getTwitter();
            log.info("posting status: " + status);
            log.info(twitter.updateStatus(status));
        }
    }
}
