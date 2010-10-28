package no.rodland.twitter;

import java.util.*;

import no.rodland.twitter.util.OAuth;
import org.apache.log4j.Logger;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAPI {

    private static final Logger log = Logger.getLogger(TwitterAPI.class);
    private static Twitter anonTwitter;
    private static Twitter authTwitter;
    static Config config;

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

    private static Posting getPosting(long tweetId) throws TwitterException {
        Twitter anonTwitter = getAnonTwitter();
        Status status = anonTwitter.showStatus(tweetId);
        return new Posting(status);
    }

    public static int getFriendCount(Twitter twitter) throws TwitterException {
        return getFriendCount(twitter.verifyCredentials());
    }

    public static int getFollowerCount(Twitter twitter) throws TwitterException {
        return getFollowerCount(twitter.verifyCredentials());
    }

    public static int getFriendCount(User user) throws TwitterException {
        return user.getFriendsCount();
    }

    public static int getFollowerCount(User user) throws TwitterException {
        return user.getFollowersCount();
    }

    public static long getLatestStatusIs(User user) throws TwitterException {
        return user.getStatus().getId();
    }

    public static Set<String> getFriends(Twitter twitter) throws TwitterException {
        Set<String> returnList = new HashSet<String>();
        PagableResponseList<User> users = null;
        //Paging paging = new Paging(page);
        boolean first = true;
        long next = -1L;
        while (first || users.hasNext()) {
            first = false;
            users = twitter.getFriendsStatuses(next);
            next = users.getNextCursor();
            for (User user : users) {
                returnList.add(user.getScreenName().toLowerCase());
            }
            log.trace("next friends-counter: " + next);
        }
        User eUser = twitter.verifyCredentials();
        log.info(eUser.getScreenName() + " has " + getFriendCount(eUser) + " friends. (size of users-list: " + returnList.size() + ")");

        return returnList;
    }

    public static Set<String> getFollowersIDs(Twitter twitter) throws TwitterException {
        Set<String> returnList = new HashSet<String>();

        PagableResponseList<User> users = twitter.getFollowersStatuses(-1L);
        while (users.hasNext()) {
            for (User user : users) {
                returnList.add(user.getScreenName().toLowerCase());
            }
            long next = users.getNextCursor();
            users = twitter.getFollowersStatuses(next);
        }
        User eUser = twitter.verifyCredentials();
        log.info(eUser.getScreenName() + " has " + getFollowerCount(eUser) + " followers. (size of users-list: " + returnList.size() + ")");
        // Must be a better way (but Array.asList does not seem to work for primitives
        return returnList;
    }

    public static List<Tweet> search(List<String> queries, String excludedTwitterUser) {
        checkConfig();
        Twitter anonTwitter = getAnonTwitter();
        Query query = new Query(getSearchStringExcludingUser(queries, excludedTwitterUser));

        query.setRpp(config.getTwitterHits());
        QueryResult result = null;
        try {
            result = anonTwitter.search(query);
            log.info("Got " + result.getTweets().size() + " results from twitter (took " + result.getCompletedIn() + "ms)");
            log.info("...for query: " + result.getQuery());
        }
        catch (TwitterException e) {
            log.error("Exception when searching twitter", e);
        }
        if (result != null) {
            return result.getTweets();
        }
        return Collections.emptyList();
    }

    static List<Posting> getPostings(List<Tweet> tweets) {
        List<Posting> postings = new ArrayList<Posting>();
        for (Tweet tweet : tweets) {
            postings.add(new Posting(tweet));
        }
        return postings;
    }

    static List<Tweet> filterTweets(List<Tweet> tweets, String twitterUser) {
        checkConfig();
        List<Tweet> filteredTweets = new ArrayList<Tweet>();
        int droppedOwn = 0;
        int droppedReplies = 0;
        int droppedRT = 0;
        int droppedVia = 0;
        int droppedBlacklisted = 0;

        for (Tweet tweet : tweets) {
            String tweetUC = tweet.getText().toUpperCase();
            if (twitterUser.equals(tweet.getFromUser())) {
                droppedOwn++;
            }
            else if (tweet.getToUser() != null) {
                droppedReplies++;
            }
            else if (config.isBlacklisted(tweet.getFromUser())) {
                droppedBlacklisted++;
            }
            else if (tweetUC.startsWith("RT")) {
                droppedRT++;
            }
            else if (tweetUC.contains("(VIA @")) {
                droppedVia++;
            }
            else {
                filteredTweets.add(tweet);
            }
        }
        log.info("Dropped tweets: " + droppedReplies + " replies, " + droppedBlacklisted + " blacklisted, " + droppedOwn + " own, " + droppedRT + " retweets, " + droppedVia + " VIAs");
        return filteredTweets;
    }

    public static void post(Twitter twitter, Posting entry) {
        String status = entry.getStatus();

        log.info("New entry published at " + entry.getUpdated());
        log.info("  status: " + status);
        log.info("  src: " + entry.getSrc());

        log.info("Updating Twitter: " + status);
        if (status.length() > 140) {
            log.error("status longer than 140: " + status);
        }

        try {
            twitter.updateStatus(status);
        }
        catch (TwitterException e) {
            log.error("Exception when posting update", e);
        }
    }

    public static void reTwitter(long id, Twitter twitter) throws TwitterException {
        log.info("retweeting status " + id);
        twitter.retweetStatus(id);
//         post(twitter, getPosting(id));
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
        OAuth oAuth = new OAuth(config.getConsumerKey(),
                                config.getConsumerKeySecret());
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
            confBuilder.setOAuthConsumerKey(oAuthConsumerKey);
            confBuilder.setOAuthConsumerSecret(oAuthConsumerSecret);
            anonTwitter = getTwitter(confBuilder);
        }
        return anonTwitter;
    }

    private static Twitter getTwitter(ConfigurationBuilder confBuilder) {
        confBuilder.setSource("web");
        Configuration configuration = confBuilder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        return factory.getInstance();
    }

    private static void checkConfig() {
        if (config == null) {
            System.err.println("Config not correctly set up. exiting");
            System.exit(3);
        }
    }
}


