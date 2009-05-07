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
        Set<String> alreadyFollowed = new HashSet<String>();

        // not possible to follow. seems like we're already following them...
        friends.add("jobbainorge");

        int numberOfFollowers = followers.size();
        int numberOfFriends = friends.size();
        if (tooMany(numberOfFollowers, numberOfFriends)) {
            log.info("Following too many allreday.  exiting following..");
            return;
        }

        Set<String> potential = getPotentialFollowers();
        int followedPosters = 0;
        for (String potenitalId : potential) {
            if (friends.contains(potenitalId)) {
                alreadyFollowed.add(potenitalId);
            } else if (okToFollowPoster(followedPosters, numberOfFollowers, numberOfFriends)) {
                try {
                    twitter.create(potenitalId);
                    log.info("followed: " + potenitalId);
                    followedPosters++;
                    numberOfFriends++;
                    friends.add(potenitalId);
                } catch (TwitterException e) {
                    log.error("Error trying to befirend");
                    e.printStackTrace();
                }
            } else {
                log.info("already followed to many posters, will not follow now: " + potenitalId);
            }
        }

        log.info("already following potential posters: " + alreadyFollowed);

        if (tooMany(numberOfFollowers, numberOfFriends)) {
            log.info("Following too many allreday.  not following followers..");
            return;
        }

        alreadyFollowed.clear();
        int followedFollowers = 0;
        for (String potenitalId : followers) {
            if (friends.contains(potenitalId)) {
                alreadyFollowed.add(potenitalId);
            } else if (okToFollowFollower(followedFollowers, numberOfFollowers, numberOfFriends)) {
                followedFollowers++;
                numberOfFriends++;
                friends.add(potenitalId);
                log.info("follow follower: " + potenitalId);
                try {
                    twitter.create(potenitalId);
                } catch (TwitterException e) {
                    log.error("Error trying to befirend", e);
                }
            } else {
                log.info("already followed to many followers, will not follow now: " + potenitalId);
            }
        }
        log.info("already following potential followers: " + alreadyFollowed);
        log.info("followed " + followedPosters + " posters and " + followedFollowers + " followers");
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

    private Set<String> getPotentialFollowers() {
        Set<String> users = new HashSet<String>();
        List<Tweet> tweets = TwitterAPI.search(queries, twitterUser, Config.TWITTER_FOLLOW_SEARCH_HITS);
        tweets = TwitterAPI.filterTweets(tweets, twitterUser);
        for (Tweet tweet : tweets) {
            users.add(tweet.getFromUser());
        }
        return users;
    }
}
