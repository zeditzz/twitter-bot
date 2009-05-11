package no.rodland.twitter;

import twitter4j.*;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class TwitterAPI {
    private static final Logger log = Logger.getLogger(TwitterAPI.class);

    public static Posting getPosting(Status status) {
        return new Posting(status.getCreatedAt(), getTitle(status), null, "Twitter: @" + status.getUser().getScreenName() + " (" + status.getId() + ")");
    }

    public static Posting getPosting(Tweet tweet) {
        return new Posting(tweet.getCreatedAt(), getTitle(tweet), null, "Twitter: @" + tweet.getFromUser() + " (" + tweet.getId() + ")");
    }

    public static String getTitle(Tweet tweet) {
        return getTitle(tweet.getFromUser(), tweet.getText());
    }

    public static String getTitle(Status status) {
        return getTitle(status.getUser().getScreenName(), status.getText());
    }

    public static String getTitle(String fromUser, String text) {
        return "RT @" + fromUser + ": " + text;
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

    public static Posting getPosting(long tweetId) throws TwitterException {
        Twitter anonTwitter = new Twitter();
        Status status = anonTwitter.show(tweetId);
        return getPosting(status);
    }

    public static List<String> getFriends(Twitter twitter) {
        List<String> returnList = new ArrayList<String>();
        try {
            List<User> users = twitter.getFriends();
            log.info(twitter.verifyCredentials().getScreenName() + " has " + users.size() + " friends.");
            // Must be a better way (but Array.asList does not seem to work for primitives
            for (User user : users) {
                returnList.add(user.getScreenName().toLowerCase());
            }
        } catch (TwitterException e) {
            log.error("Exception when searching twitter", e);
        }
        return returnList;
    }

    public static List<String> getFollowersIDs(Twitter twitter) {
        List<String> returnList = new ArrayList<String>();
        try {
            List<User> users = twitter.getFollowers();
            log.info(twitter.verifyCredentials().getScreenName() + " has " + users.size() + " followers.");
            // Must be a better way (but Array.asList does not seem to work for primitives
            for (User user : users) {
                returnList.add(user.getScreenName().toLowerCase());
            }
        } catch (TwitterException e) {
            log.error("Exception when searching twitter", e);
        }
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
        } catch (TwitterException e) {
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
            postings.add(getPosting(tweet));
        }
        return postings;
    }

    static List<Tweet> filterTweets(List<Tweet> tweets, String twitterUser) {
        List<Tweet> filteredTweets = new ArrayList<Tweet>();
        int droppedOwn = 0;
        for (Tweet tweet : tweets) {
            if (twitterUser.equals(tweet.getFromUser())) {
                droppedOwn++;
            } else {
                filteredTweets.add(tweet);
            }
        }
        if (droppedOwn > 0) {
            log.info("Dropped " + droppedOwn + " tweets from user @" + twitterUser);
        }
        return filteredTweets;
    }

    static void post(Twitter twitter, Posting entry) {
        String title = entry.getTitle();
        String link = entry.getUrl();

        TwitterBot.log.info("New entry " + title + " published at " + entry.getUpdated());
        TwitterBot.log.info("           link: " + link);
        TwitterBot.log.info("           src: " + entry.getSrc());

        String status = Posting.formatStatus(title, link);
        TwitterBot.log.info("Updating Twitter: " + status);
        if (status.length() > 140) {
            TwitterBot.log.error("status longer than 140: " + status);
        }

        try {
            twitter.update(status);
        } catch (TwitterException e) {
            TwitterBot.log.error("Exception when posting update", e);
        }
    }

    public static void reTwitter(long id, Twitter twitter) throws TwitterException {
        post(twitter, getPosting(id));
    }
}


