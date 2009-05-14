package no.rodland.twitter;

import twitter4j.*;

import java.util.*;

import org.apache.log4j.Logger;

class FollowerRetriever {
    private static final Logger log = Logger.getLogger(FollowerRetriever.class);
    private final List<String> queries;
    private final Config cfg;
    private String twitterUser;
    private Twitter twitter;

    private enum FollowType {
        POSTERS, FOLLOWERS;

        public String toString() {
            return name().toLowerCase();
        }
    }

    public FollowerRetriever(Twitter twitter, Config cfg) throws TwitterException {
        this.queries = cfg.getFollowerQueries();
        this.cfg = cfg;
        this.twitterUser = twitter.verifyCredentials().getScreenName();
        this.twitter = twitter;
    }

    public int followNew() {
        Set<String> friends;
        Set<String> followers;
        int numberOfFriends;
        int numberOfFollowers;
        try {
            friends = TwitterAPI.getFriends(twitter);
            numberOfFriends = TwitterAPI.getFriendCount(twitter);
            followers = TwitterAPI.getFollowersIDs(twitter);
            numberOfFollowers = TwitterAPI.getFollowerCount(twitter);
        }
        catch (TwitterException e) {
            log.error("Exception when getting friends or followers from twitter.", e);
            return 0;
        }

        int numerbNew = (int) Math.ceil((cfg.getFollowFactor() * numberOfFollowers) - numberOfFriends);
        if (numerbNew < 1) {
            log.info("No more room for new friends for now.");
            return 0;
        } else {
            log.info("Should be able to follow " + numerbNew + " new friends.");
        }

        int followedPosters = followFolks(friends, FollowType.POSTERS, getPosters(), numberOfFriends, numberOfFollowers);
        numberOfFriends += followedPosters;
        // check (again) that the limit isn't reached
        int followedFollowers = followFolks(friends, FollowType.FOLLOWERS, followers, numberOfFriends, numberOfFollowers);

        log.info("followed " + followedPosters + " posters and " + followedFollowers + " friends");
        return followedFollowers + followedPosters;
    }

    public int unfollowBlackList() throws TwitterException {
        Set<String> friends = TwitterAPI.getFriends(twitter);
        List<String> destroyed = new ArrayList<String>();
        //int destroyed = 0;
        for (String friend : friends) {
            if (cfg.isBlacklisted(friend)) {
                log.info(friend + " is balcklisted since last time, unfollowing");
                twitter.destroyFriendship(friend);
                destroyed.add(friend);
            }
        }
        log.info("un-followed the following friends: " + destroyed);
        return destroyed.size();
    }

    private int followFolks(Set<String> friends,  FollowType type, Collection<String> potentials, int numberOfFriends, int numberOfFollowers) {
        //int numberOfFollowers = followers.size();
        //int numberOfFriends = friends.size();
        if (tooMany(numberOfFollowers, numberOfFriends)) {
            log.info("Following too many already.  not following more " + type);
            return 0;
        }
        Set<String> alreadyFollowed = new HashSet<String>();
        int nmbFollowed = 0;
        for (String potentialId : potentials) {
            if (friends.contains(potentialId)) {
                alreadyFollowed.add(potentialId);
            } else if (cfg.isBlacklisted(potentialId)) {
                log.info(potentialId + " is blacklisted, will not follow.");
            } else if (okToFollow(nmbFollowed, numberOfFollowers, numberOfFriends, type)) {
                try {
                    twitter.createFriendship(potentialId);
                    log.info("followed " + type + ": " + potentialId);
                    nmbFollowed++;
                    numberOfFriends++;
                    friends.add(potentialId);
                }
                catch (TwitterException e) {
                    log.error("Error trying to befriend", e);
                }
            } else {
                log.info("already followed to many people, will not follow more for now.");
                log.info("BTW: already following posters: " + alreadyFollowed);
                return nmbFollowed;
            }
        }

        log.info("already following " + type + ": " + alreadyFollowed);
        return nmbFollowed;
    }

    private boolean okToFollow(int followedType, int numberOfFollowers, int numberOfFriends, FollowType type) {
        if (tooMany(numberOfFollowers, numberOfFriends)) {
            return false;
        }
        int num = type == FollowType.POSTERS ? cfg.getMaxPosters() : cfg.getMaxFollowers();
        return followedType < num;
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
