package no.rodland.twitter.util;

import java.util.HashSet;
import java.util.List;

import no.rodland.twitter.TwitterAPI;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by IntelliJ IDEA. User: fmr Date: May 4, 2009 Time: 11:53:17 AM
 */
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public class TwitterUtil {

    private static OAuth oAuth = new OAuth("", "", "", "");

    @SuppressWarnings({})
    public static void print() throws TwitterException {

        // TODO does not work without config.

        Twitter anonTwitter = TwitterAPI.getAnonTwitter();
        Twitter twitter = TwitterAPI.getAuthTwitter(oAuth);
        //Twitter twitter = new TwitterFactory().getInstance(TWITTER_USER, TWITTER_PASSWORD);
        Query query = new Query("cnn.com");
        query.setCount(30);
        QueryResult result = anonTwitter.search(query);
        HashSet<String> users = new HashSet<String>();
        Status tweet = result.getTweets().get(0);
        //for (Tweet tweet : result.getTweets()) {
        System.out.println(tweet.getUser().getScreenName() + ":" + tweet.getText());
        users.add(tweet.getUser().getScreenName());
        //}
        System.out.println("users = " + users);
        System.out.println("num users = " + users.size());
        System.out.println(twitter);
        String userId = twitter.getScreenName();
        System.out.println("USER: " + userId);
        User user = twitter.showUser(twitter.getScreenName());
        System.out.println("USER-print: " + PrintUtil.print(user));
        System.out.println("USER-DET: " + user);
        System.out.println("USER-ID: " + user.getId());
        System.out.println("USER-SName: " + user.getScreenName());
        System.out.println("USER-Name: " + user.getScreenName());
        System.out.println("USER-lastupd: " + user.getStatus().getCreatedAt());

        List<User> followers = twitter.getFollowersList(twitter.getId(), -1);
        System.out.println("f: " + followers);

        //TODO FIX: Set<String> list = TwitterAPI.getFriends(twitter);
        //System.out.println("list.size() = " + list.size());
        //System.out.println("list = " + list);
    }

    public static void main(String[] args) throws TwitterException {
        print();
    }
}
