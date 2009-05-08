package no.rodland.twitter;

import twitter4j.*;

import java.util.Set;
import java.util.List;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class FollowerRetriever {
    private static Logger log = Logger.getLogger(FollowerRetriever.class);
    private List<String> queries;
    private String twitterUser;
    private Twitter twitter;

    public FollowerRetriever(List<String> queries, Twitter twitter) throws TwitterException {
        this.queries = queries;
        this.twitterUser = twitter.verifyCredentials().getScreenName();
        this.twitter = twitter;
    }

    public void followNew() {
        List<String> friends = TwitterAPI.getFriends(twitter);
        List<String> followers = TwitterAPI.getFollowersIDs(twitter);

        int numerbNew = (int) ((Config.FOLLOW_FACTOR * followers.size()) - friends.size());
        log.info("Should be able to follow " + numerbNew + " new followers.");

        int followedPosters = followPosters(friends, followers);

        // check (again) that the limit isn't reached
        int followedFollowers = followFollowers(friends, followers);

        log.info("followed " + followedPosters + " posters and " + followedFollowers + " followers");
    }

    private int followPosters(List<String> friends, List<String> followers) {
        int numberOfFollowers = followers.size();
        int numberOfFriends = friends.size();
        if (tooMany(numberOfFollowers, numberOfFriends)) {
            log.info("Following too many already.  not following posters.");
            return 0;
        }
        Set<String> alreadyFollowed = new HashSet<String>();
        Set<String> posters = getPosters();
        int followedPosters = 0;
        for (String posterId : posters) {
            if (friends.contains(posterId)) {
                alreadyFollowed.add(posterId);
            }
            else if (okToFollowPoster(followedPosters, numberOfFollowers, numberOfFriends)) {
                try {
                    twitter.create(posterId);
                    log.info("followed poster: " + posterId);
                    followedPosters++;
                    numberOfFriends++;
                    friends.add(posterId);
                }
                catch (TwitterException e) {
                    log.error("Error trying to befriend", e);
                }
            }
            else {
                log.info("already followed to many people, will not follow more for now.");
                log.info("BTW: already following posters: " + alreadyFollowed);
                return followedPosters;
            }
        }

        log.info("already following posters posters: " + alreadyFollowed);
        return followedPosters;
    }

    private int followFollowers(List<String> friends, List<String> followers) {
        int numberOfFollowers = followers.size();
        int numberOfFriends = friends.size();
        if (tooMany(numberOfFollowers, numberOfFriends)) {
            log.info("Following too many already.  not following followers.");
            return 0;
        }
        Set<String> alreadyFollowed = new HashSet<String>();
        alreadyFollowed.clear();
        int followedFollowers = 0;
        for (String followerId : followers) {
            if (friends.contains(followerId)) {
                alreadyFollowed.add(followerId);
            }
            else if (okToFollowFollower(followedFollowers, numberOfFollowers, numberOfFriends)) {
                try {
                    twitter.create(followerId);
                    log.info("followed follower: " + followerId);
                    followedFollowers++;
                    numberOfFriends++;
                    friends.add(followerId);
                }
                catch (TwitterException e) {
                    log.error("Error trying to befriend", e);
                }
            }
            else {
                log.info("already followed to many people, will not follow more for now.");
                log.info("BTW: already following : " + alreadyFollowed);
                return followedFollowers;
            }
        }
        log.info("already following potential followers: " + alreadyFollowed);
        return followedFollowers;
    }

    private boolean okToFollowFollower(int followedFollowers, int numberOfFollowers, int numberOfFriends) {
        return !tooMany(numberOfFollowers, numberOfFriends) && (followedFollowers < Config.MAX_FOLLOWERS);
    }

    private boolean okToFollowPoster(int followedPosters, int numberOfFollowers, int numberOfFriends) {
        return !tooMany(numberOfFollowers, numberOfFriends) && (followedPosters < Config.MAX_POSTERS);
    }

    private boolean tooMany(int numberOfFollowers, int numberOfFriends) {
        return numberOfFriends > (Config.FOLLOW_FACTOR * numberOfFollowers);
    }

    private Set<String> getPosters() {
        Set<String> users = new HashSet<String>();
        List<Tweet> tweets = TwitterAPI.search(queries, twitterUser, Config.TWITTER_FOLLOW_SEARCH_HITS);
        tweets = TwitterAPI.filterTweets(tweets, twitterUser);
        for (Tweet tweet : tweets) {
            users.add(tweet.getFromUser());
        }
        return users;
    }
}
