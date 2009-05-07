package no.rodland.twitter.util;

import twitter4j.*;

import java.util.List;
import java.util.HashSet;

import no.rodland.twitter.Config;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 4, 2009
 * Time: 11:53:17 AM
 */
public class TwitterUtil {

    public static void print() throws TwitterException {
        Twitter anonTwitter = new Twitter();
        Twitter twitter = new Twitter(Config.TEST_TWITTER_USER, Config.TEST_TWITTER_PASSWORD);
        twitter.setSource("web");
        Twitter fmrTwitter = new Twitter("fredrikr", "fmr123");
        Query query = new Query("schibsted OR vg.no OR finn.no OR aftenposten OR aftonbladet OR secondamano OR secundamano OR blocket.se");
        query.setRpp(30);
        QueryResult result = anonTwitter.search(query);
        HashSet<String> users = new HashSet<String>();
        for (Tweet tweet : result.getTweets()) {

            System.out.println(tweet.getFromUser() + ":" + tweet.getText());
            users.add(tweet.getFromUser());
        }
        System.out.println("users = " + users);
        System.out.println("num users = " + users.size());
        System.out.println(fmrTwitter);
        String userId = fmrTwitter.getUserId();
        System.out.println("USER: " + userId);
        User user = twitter.getUserDetail("ntesttest");
        System.out.println("USER-print: " + PrintUtil.print(user));
        System.out.println("USER-DET: " + user);
        System.out.println("USER-ID: " + user.getId());
        System.out.println("USER-SName: " + user.getScreenName());
        System.out.println("USER-Name: " + user.getName());
        System.out.println("USER-lastupd: " + user.getStatusCreatedAt());

        List<User> followers = fmrTwitter.getFollowers("arodland");
        System.out.println("f: " + followers);

        System.out.print("boston AND (");
        for (User follower : followers) {
            System.out.print(follower.getScreenName() + " OR ");
        }
        System.out.println("");
        System.out.println("FRIENDS: " + fmrTwitter.getFriends());
        System.out.println("FOLLOW: " + fmrTwitter.getFollowers());

        // XXX to follow
        //System.out.println(twitter.create("fredrikr"));

    }
}
