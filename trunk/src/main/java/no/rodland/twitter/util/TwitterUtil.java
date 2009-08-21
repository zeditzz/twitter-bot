package no.rodland.twitter.util;

import twitter4j.*;

import java.util.List;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 4, 2009
 * Time: 11:53:17 AM
 */
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class TwitterUtil {
    private static final String TWITTER_USER = "";
    private static final String TWITTER_PASSWORD = "";

    @SuppressWarnings({})
    public static void print() throws TwitterException {
        Twitter anonTwitter = new Twitter();
        Twitter twitter = new Twitter(TWITTER_USER, TWITTER_PASSWORD);
        twitter.setSource("web");
        Query query = new Query("cnn.com");
        query.setRpp(30);
        QueryResult result = anonTwitter.search(query);
        HashSet<String> users = new HashSet<String>();
        for (Tweet tweet : result.getTweets()) {

            System.out.println(tweet.getFromUser() + ":" + tweet.getText());
            users.add(tweet.getFromUser());
        }
        System.out.println("users = " + users);
        System.out.println("num users = " + users.size());
        System.out.println(twitter);
        String userId = twitter.getUserId();
        System.out.println("USER: " + userId);
        User user = twitter.showUser(TWITTER_USER);
        System.out.println("USER-print: " + PrintUtil.print(user));
        System.out.println("USER-DET: " + user);
        System.out.println("USER-ID: " + user.getId());
        System.out.println("USER-SName: " + user.getScreenName());
        System.out.println("USER-Name: " + user.getName());
        System.out.println("USER-lastupd: " + user.getStatusCreatedAt());

        List<User> followers = twitter.getFollowersStatuses(TWITTER_USER);
        System.out.println("f: " + followers);

        System.out.print("boston AND (");
        for (User follower : followers) {
            System.out.print(follower.getScreenName() + " OR ");
        }
        System.out.println("");
        System.out.println("FRIENDS: " + twitter.getFriendsStatuses());
        System.out.println("FOLLOW: " + twitter.getFollowersStatuses());

        // to follow
        //System.out.println(twitter.create("fredrikr"));

    }
}
