package no.rodland.twitter.main;

import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by IntelliJ IDEA. User: fmr Date: May 6, 2009 Time: 10:51:26 AM
 */
public class Follower extends AuthMain {

    private static final Logger log = Logger.getLogger(Follower.class);

    protected Follower(String[] args) {
        super(false, args);
    }

    public static void main(String[] args) throws TwitterException {
        Follower follower = new Follower(args);
        follower.run();
    }

    @Override
    String getShortName() {
        return "follower";
    }

    @Override
    String getLoadDescription() {
        return "<screenname-to-follow>";
    }

    @Override
    void doWork() throws TwitterException {
        Twitter twitter = getTwitter();
        log.info("follow: " + getPayLoad());
        log.info(twitter.createFriendship(getPayLoad()));
    }
}
