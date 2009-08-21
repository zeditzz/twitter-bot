package no.rodland.twitter;

import org.apache.log4j.Logger;
import twitter4j.*;

import java.util.*;

public class TwitterAPI {
    private static final Logger log = Logger.getLogger(TwitterAPI.class);

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
        Twitter anonTwitter = new Twitter();
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
        return user.getStatusId();
    }

    public static Set<String> getFriends(Twitter twitter) throws TwitterException {
        Set<String> returnList = new HashSet<String>();
        int page = 1;
        Paging paging = new Paging(page);
        List<User> users = twitter.getFriendsStatuses(paging);
        while (users.size() > 0) {
            // Must be a better way (but Array.asList does not seem to work for primitives)
            for (User user : users) {
                returnList.add(user.getScreenName().toLowerCase());
            }
            page++;
            paging.setPage(page);
            users = twitter.getFriendsStatuses(paging);
        }
        User eUser = twitter.verifyCredentials();
        log.info(eUser.getScreenName() + " has " + getFriendCount(eUser) + " friends. (size of users-list: " + returnList.size() + ")");

        return returnList;
    }

    public static Set<String> getFollowersIDs(Twitter twitter) throws TwitterException {
        Set<String> returnList = new HashSet<String>();

        int page = 1;
        Paging paging = new Paging(page);
        List<User> users = twitter.getFollowersStatuses(paging);
        while (users.size() > 0) {
            for (User user : users) {
                returnList.add(user.getScreenName().toLowerCase());
            }
            page++;
            paging.setPage(page);
            users = twitter.getFollowersStatuses(paging);
        }
        User eUser = twitter.verifyCredentials();
        log.info(eUser.getScreenName() + " has " + getFollowerCount(eUser) + " followers. (size of users-list: " + returnList.size() + ")");
        // Must be a better way (but Array.asList does not seem to work for primitives
        return returnList;
    }

    static List<Tweet> search(List<String> queries, String twitterUser, Config cfg) {
        return search(queries, twitterUser, cfg.getTwitterHits());
    }

    static List<Tweet> search(List<String> queries, String excludedTwitterUser, int hits) {
        Twitter anonTwitter = new Twitter();
        Query query = new Query(getSearchStringExcludingUser(queries, excludedTwitterUser));
        query.setRpp(hits);
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
        List<Tweet> filteredTweets = new ArrayList<Tweet>();
        int droppedOwn = 0;
        int droppedReplies = 0;
        int droppedRT = 0;
        int droppedVia = 0;
        for (Tweet tweet : tweets) {
            String tweetUC = tweet.getText().toUpperCase();
            if (twitterUser.equals(tweet.getFromUser())) {
                droppedOwn++;
            }
            else if (tweet.getToUser() != null) {
                droppedReplies++;
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
        log.info("Dropped tweets: " + droppedReplies + " replies, " + droppedOwn + " own, " + droppedRT + " retweets, " + droppedVia + " VIAs");
        return filteredTweets;
    }

    static void post(Twitter twitter, Posting entry) {
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
        post(twitter, getPosting(id));
    }
}


