package no.rodland.twitter;

import twitter4j.*;

import java.util.Set;
import java.util.List;
import java.util.HashSet;

import org.apache.log4j.Logger;

class FollowerRetriever {
    private static final Logger log = Logger.getLogger(FollowerRetriever.class);
    private final List<String> queries;
    private final Config cfg;
    private String twitterUser;
    private Twitter twitter;

    public FollowerRetriever(Twitter twitter, Config cfg) throws TwitterException {
        this.queries = cfg.getFollowerQueries();
        this.cfg = cfg;
        this.twitterUser = twitter.verifyCredentials().getScreenName();
        this.twitter = twitter;
    }

    public void followNew() {
        List<String> friends = TwitterAPI.getFriends(twitter);
        List<String> followers = TwitterAPI.getFollowersIDs(twitter);

        int numerbNew = (int) Math.ceil((cfg.getFollowFactor() * followers.size()) - friends.size());
        if (numerbNew < 1) {
            log.info("No more room for new friends for now.");
            return;
        }
        else if (friends.size() == 0) {
            log.warn("no friends - hhu. don't think so.. exiting ");
            return;
        }
        else {
            log.info("Should be able to follow " + numerbNew + " new friends.");
        }


        int followedPosters = followPosters(friends, followers);

        // check (again) that the limit isn't reached
        int followedFollowers = followFollowers(friends, followers);

        log.info("followed " + followedPosters + " posters and " + followedFollowers + " friends");
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
            else if (cfg.isBlacklisted(posterId)) {
                log.info(posterId + " is blacklisted, will not follow.");
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
            else if (cfg.isBlacklisted(followerId)) {
                log.info(followerId + " is blacklisted, will not follow.");
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
        return !tooMany(numberOfFollowers, numberOfFriends) && (followedFollowers < cfg.getMaxFollowers());
    }

    private boolean okToFollowPoster(int followedPosters, int numberOfFollowers, int numberOfFriends) {
        return !tooMany(numberOfFollowers, numberOfFriends) && (followedPosters < cfg.getMaxPosters());
    }

    private boolean tooMany(int numberOfFollowers, int numberOfFriends) {
        return numberOfFriends > (cfg.getFollowFactor() * numberOfFollowers);
    }

    private Set<String> getPosters() {
        Set<String> users = new HashSet<String>();
        List<Tweet> tweets = TwitterAPI.search(queries, twitterUser, cfg.getFollowerHits());
        tweets = TwitterAPI.filterTweets(tweets, twitterUser);
        for (Tweet tweet : tweets) {
            users.add(tweet.getFromUser().toLowerCase());
        }
        return users;
    }
}
