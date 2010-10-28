package no.rodland.twitter.main;

import no.rodland.twitter.TwitterAPI;
import no.rodland.twitter.util.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author fmr
 * @since Oct 28, 2010 2:24:10 PM
 */
abstract class AuthMain {

    private OAuth oAuth;
    private String payLoad;
    private boolean isAnon;
    private String[] args;
    private final Twitter twitter;

    protected AuthMain(boolean anon, String[] args) {
        isAnon = anon;
        this.args = args;
        initArgs();
        TwitterAPI.setNullConfig();
        twitter = initTwitter();  // will set up twitter
    }

    void run() {
        try {
            doWork();
        }
        catch (TwitterException e) {
            System.err.println("Got Exception from Twitter:");
            e.printStackTrace();
        }
    }

    abstract String getShortName();

    abstract String getLoadDescription();

    abstract void doWork() throws TwitterException;

    void initArgs() {
        if (isAnon) {
            initArgsAnon();
        }
        else {
            initArgsAuth();
        }
    }

    private void initArgsAuth() {
        if (args.length >= 5) {
            oAuth = new OAuth(args[0], args[1], args[2], args[3]);
            payLoad = args[4];
        }
        else {
            usage();
        }
    }

    private void initArgsAnon() {
        if (args.length >= 3) {
            oAuth = new OAuth(args[0], args[1]);
            payLoad = args[2];
        }
        else {
            usage();
        }
    }

    public OAuth getoAuth() {
        return oAuth;
    }

    public String getPayLoad() {
        return payLoad;
    }

    public String[] getArgs() {
        return args;
    }

    public Twitter getTwitter() {
        return twitter;
    }

    void usage() {
        String clazz = this.getClass().getName();
        System.out.println("Twitter " + getShortName());
        System.out.println("usage: java " + clazz + getUsageKeys() + getLoadDescription());
        System.exit(2);
    }

    private String getUsageKeys() {
        return isAnon ? " <consumerKey> <consumerSecretKey> " : " <consumerKey> <consumerSecretKey> <accessKey> <accessSecretKey> ";
    }

    private Twitter initTwitter() {
        return isAnon ? TwitterAPI.getAnonTwitter(getoAuth()) : TwitterAPI.getAuthTwitter(getoAuth());
    }
}
