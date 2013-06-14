package no.rodland.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.rodland.twitter.util.OAuth;
import org.apache.log4j.Logger;
import twitter4j.PagableResponseList;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAPI {

    private static final Logger log = Logger.getLogger(TwitterAPI.class);
    private static Twitter anonTwitter;
    private static Twitter authTwitter;
    static Config config;
    private static Set<String> friendList;
    private static Set<String> followersList;
    private static User user;

    public static void setConfig(Config config) {
        TwitterAPI.config = config;
    }

    public static void setNullConfig() {
        setConfig(Config.NULL_CONFIG);
    }

    public static String getSearchStringExcludingUser(List<String> queries, String twitterUser) {
        if (queries == null || queries.size() == 0) {
            return "";
        }
        return "-" + twitterUser + " " + getSearchString(queries);
    }

    public static String getSearchString(List<String> queries) {
        if (queries == null || queries.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String query : queries) {
            sb.append(query).append(" OR ");
        }

        return sb.substring(0, sb.length() - 4);
    }

    public static int getFriendCount(Twitter twitter) throws TwitterException {
        return getFriendCount(getUser(twitter));
    }

    public static int getFollowerCount(Twitter twitter) throws TwitterException {
        return getFollowerCount(getUser(twitter));
    }

    public static int getFriendCount(User user) throws TwitterException {
        return user.getFriendsCount();
    }

    public static int getFollowerCount(User user) throws TwitterException {
        return user.getFollowersCount();
    }

    public static Set<String> getFriends(Twitter twitter) throws TwitterException {
        if (friendList == null) {
            friendList = new HashSet<String>();
            PagableResponseList<User> users = null;
            //Paging paging = new Paging(page);
            boolean first = true;
            long next = -1L;
            while (first || users.hasNext()) {
                first = false;
                users = twitter.getFriendsList(twitter.getScreenName(), next);
                next = users.getNextCursor();
                for (User user : users) {
                    friendList.add(user.getScreenName().toLowerCase());
                }
                log.trace("next friends-counter: " + next);
            }
            User eUser = getUser(twitter);
            log.info(eUser.getScreenName() + " has " + getFriendCount(eUser) + " friends. (size of users-list: " + friendList.size()
                     + ")");
        }
        else {
            User eUser = getUser(twitter);
            log.info("Returning cached version: " + eUser.getScreenName() + " has " + getFriendCount(eUser) + " friends. (size of " +
                     "users-list: " + friendList.size() + ")");
        }
        return friendList;
    }

    public static Set<String> getFollowersIDs(Twitter twitter) throws TwitterException {
        if (followersList == null) {
            followersList = new HashSet<String>();
            PagableResponseList<User> users = twitter.getFollowersList(twitter.getScreenName(), -1L);
            while (users.hasNext()) {
                for (User user : users) {
                    followersList.add(user.getScreenName().toLowerCase());
                }
                long next = users.getNextCursor();
                users = twitter.getFollowersList(twitter.getScreenName(), next);
            }
            User eUser = getUser(twitter);
            log.info(eUser.getScreenName() + " has " + getFollowerCount(eUser) +
                     " followers. (size of users-list: " + followersList.size() + ")");
        }
        else {
            User eUser = getUser(twitter);
            log.info("Returning cached version: " + eUser.getScreenName() + " has " + getFollowerCount(eUser) +
                     " followers. (size of users-list: " + followersList.size() + ")");
        }
        return followersList;
    }

    public static List<Status> search(List<String> queries, String excludedTwitterUser) {
        checkConfig();
        Twitter anonTwitter = getAnonTwitter();
        Query query = new Query(getSearchStringExcludingUser(queries, excludedTwitterUser));

        query.setCount(config.getTwitterHits());
        QueryResult result = null;
        try {
            result = anonTwitter.search(query);
            log.info("Got " + result.getTweets().size() + " results from twitter (took " + result.getCompletedIn() + "ms)");
            log.info("...for query: " + result.getQuery());
        } catch (TwitterException e) {
            log.error("Exception when searching twitter", e);
        }
        if (result != null) {
            return result.getTweets();
        }
        return Collections.emptyList();
    }

    static List<Posting> getPostings(List<Status> tweets) {
        List<Posting> postings = new ArrayList<Posting>();
        for (Status tweet : tweets) {
            postings.add(new Posting(tweet));
        }
        return postings;
    }

    static List<Status> filterTweets(List<Status> tweets, String twitterUser) {
        checkConfig();
        List<Status> filteredTweets = new ArrayList<Status>();
        int droppedOwn = 0;
        int droppedReplies = 0;
        int droppedRT = 0;
        int droppedVia = 0;
        int droppedBlacklisted = 0;

        for (Status tweet : tweets) {
            String tweetUC = tweet.getText().toUpperCase();
            String fromUser = tweet.getUser().getScreenName();
            if (twitterUser.equals(fromUser)) {
                droppedOwn++;
            }
            else if (tweet.getInReplyToScreenName() != null) {
                droppedReplies++;
            }
            else if (config.isBlacklisted(fromUser)) {
                droppedBlacklisted++;
            }
            else if (tweetUC.contains("RT")) {
                droppedRT++;
            }
            else if (tweetUC.contains("VIA @")) {
                droppedVia++;
            }
            else {
                filteredTweets.add(tweet);
            }
        }
        log.info("Dropped tweets: " + droppedReplies + " replies, " + droppedBlacklisted + " blacklisted, " + droppedOwn + " own, " +
                 "" + droppedRT + " retweets, " + droppedVia + " VIAs");
        return filteredTweets;
    }

    public static void post(Twitter twitter, Posting entry) {
        String status = entry.getStatus();

        log.info("Updating Twitter: " + status);
        if (status.length() > 140) {
            log.error("status longer than 140: " + status);
        }

        try {
            twitter.updateStatus(status);
        } catch (TwitterException e) {
            log.error("Exception when posting update", e);
        }
    }

    public static void reTwitter(long id, Twitter twitter) throws TwitterException {
        log.info("retweeting status " + id);
        twitter.retweetStatus(id);
    }

    public static Twitter getAuthTwitter() {
        checkConfig();
        OAuth oAuth = new OAuth(config.getConsumerKey(),
                                config.getConsumerKeySecret(),
                                config.getAccessToken(),
                                config.getAccessTokenSecret());
        return getAuthTwitter(oAuth);
    }

    public static Twitter getAuthTwitter(OAuth oAuth) {
        final String oAuthConsumerKey = oAuth.getConsumerKey();
        final String oAuthConsumerSecret = oAuth.getConsumerKeySecret();
        final String accessToken = oAuth.getAccessKey();
        final String accessTokenSecret = oAuth.getAccessKeySecret();
        return getAuthTwitter(oAuthConsumerKey, oAuthConsumerSecret, accessToken, accessTokenSecret);
    }

    public static Twitter getAuthTwitter(String oAuthConsumerKey,
                                         String oAuthConsumerSecret,
                                         String authAccessToken,
                                         String tokenSecret) {
        if (authTwitter == null) {
            ConfigurationBuilder confBuilder = new ConfigurationBuilder();
            confBuilder.setOAuthConsumerKey(oAuthConsumerKey);
            confBuilder.setOAuthConsumerSecret(oAuthConsumerSecret);
            confBuilder.setOAuthAccessToken(authAccessToken);
            confBuilder.setOAuthAccessTokenSecret(tokenSecret);

            authTwitter = getTwitter(confBuilder);
        }
        return authTwitter;
    }

    public synchronized static Twitter getAnonTwitter() {
        checkConfig();
        OAuth oAuth = new OAuth(config.getConsumerKey(), config.getConsumerKeySecret());
        return getAnonTwitter(oAuth);
    }

    public static Twitter getAnonTwitter(OAuth oAuth) {
        final String oAuthConsumerKey = oAuth.getConsumerKey();
        final String oAuthConsumerSecret = oAuth.getConsumerKeySecret();
        return getAnonTwitter(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public static Twitter getAnonTwitter(String oAuthConsumerKey, String oAuthConsumerSecret) {

        if (anonTwitter == null) {
            ConfigurationBuilder confBuilder = new ConfigurationBuilder();
            confBuilder.setUseSSL(true);
            confBuilder.setApplicationOnlyAuthEnabled(true);
            confBuilder.setOAuthConsumerKey(oAuthConsumerKey);
            confBuilder.setOAuthConsumerSecret(oAuthConsumerSecret);
            anonTwitter = getTwitter(confBuilder);
            try {
                anonTwitter.getOAuth2Token();
            } catch (TwitterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return anonTwitter;
    }

    private static Twitter getTwitter(ConfigurationBuilder confBuilder) {
        Configuration configuration = confBuilder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        return factory.getInstance();
    }

    private static void checkConfig() {
        if (config == null) {
            System.err.println("TwitterAPI.checkConfig: Config not correctly set up. exiting");
            System.exit(3);
        }
    }

    private static User getUser(Twitter twitter) throws TwitterException {
        if (user == null) {
            user = twitter.verifyCredentials();
        }
        return user;
    }
}


