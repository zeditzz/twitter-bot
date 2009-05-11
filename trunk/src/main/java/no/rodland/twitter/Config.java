package no.rodland.twitter;

import java.util.*;

import org.apache.log4j.Logger;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 6, 2009
 * Time: 10:55:30 AM
 */
public class Config {
    private static final Logger log = Logger.getLogger(Config.class);
    @SuppressWarnings({"FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})

//    public static final double FOLLOW_FACTOR = 1.2;
//    public static final int DEFAULT_TWITTER_HITS = 30;
//    public static final int TWITTER_FOLLOW_SEARCH_HITS = 50;
//    private static final int NUMBER_NEWS = 20;
//    public static final int MAX_POSTERS = 5;
//    public static final int MAX_FOLLOWERS = 3;
//    public static final int TWITTER_MSG_LENGTH = 140;
//    public static final int MIN_TITLE_LENGTH = 15;

    private static final String CFG_KEY_TWITTERUSER = "twitteruser";
    private static final String CFG_KEY_TWITTERPASSWORD = "twitterpassword";
    private static final String CFG_KEY_TWITTER_QUERY = "twitterquery";
    private static final String CFG_KEY_FOLLOWER_QUERY = "followerquery";
    private static final String CFG_KEY_RSS_QUERY = "rssquery";
    private static final String CFG_KEY_SITES = "sites";
    private static final String CFG_KEY_BLACKLIST = "blacklist";

    public String twitterUser;
    public String twitterPassword;

    private PropertiesConfiguration config;
    private static final String CFG_KEY_LASTUPDATED = "lastupdated";
    private static final String CFG_KEY_URLS = "urls";

    @SuppressWarnings({"UnusedDeclaration"})
    public Config(String fileName) throws ConfigurationException {
        log.info("Loading properties from " + fileName);
        config = new PropertiesConfiguration(fileName);

        twitterUser = config.getString(CFG_KEY_TWITTERUSER);
        twitterPassword = config.getString(CFG_KEY_TWITTERPASSWORD);

    }

    public void update() {
        update(new Date());
    }

    public void update(Date lastUpdated) {
        config.setProperty(CFG_KEY_LASTUPDATED, lastUpdated.getTime());
        log.info("setting last updated in " + config.getFileName() + " to " + config.getLong(CFG_KEY_LASTUPDATED));
        try {
            config.save();
        }
        catch (ConfigurationException e) {
            log.error("ERROR setting last updated in " + config.getFileName() + " to " + config.getLong(CFG_KEY_LASTUPDATED), e);
        }
    }

    public Date getLastUpdated() {
        long time = config.getLong(CFG_KEY_LASTUPDATED, 0);
        return new Date(time);
    }

    @SuppressWarnings({"unchecked"})
    List<String> getTwitterQueries() {
        return config.getList(CFG_KEY_TWITTER_QUERY);
    }

    @SuppressWarnings({"unchecked"})
    List<String> getFollowerQueries() {
        return config.getList(CFG_KEY_FOLLOWER_QUERY);
    }

    public boolean isBlacklisted(String potentialTwitterUser) {
        if (potentialTwitterUser == null) {
            return false;
        }
        List urls = config.getList(CFG_KEY_BLACKLIST);
        return urls.contains(potentialTwitterUser);
    }


    /**
     * Constructs a series of RSS-urls based on two lists: one with queries and ne with urls.  A new URL will be created for each pair of these.
     *
     * @return a list of ready-to-call RSS URLs.
     */
    List<FeedUrl> getFeedUrls() {
        List urls = config.getList(CFG_KEY_URLS);
        List queries = config.getList(CFG_KEY_RSS_QUERY);
        List sites = config.getList(CFG_KEY_SITES);
        List<FeedUrl> myList = new ArrayList<FeedUrl>();
        for (Object siteObj : sites) {
            String site = ((String) siteObj).replaceAll("NUMBER_NEWS", Integer.toString(getNumberNews()));
            for (Object query : queries) {
                myList.add(new FeedUrl(site, (String) query));
            }
        }
        for (Object url : urls) {
            String site = ((String) url).replaceAll("NUMBER_NEWS", Integer.toString(getNumberNews()));
            myList.add(new FeedUrl(site));
        }
        return myList;
    }

    @Override
    public String toString() {
        return "getTwitterQueries() = " + getTwitterQueries() + "\n" +
                "getFeedUrls() = " + getFeedUrls() + "\n" +
                "getFollowerQueries() = " + getFollowerQueries() + "\n" +
                "getLastUpdated() = " + getLastUpdated();
    }

    public double getFollowFactor() {
        return config.getDouble("FOLLOW_FACTOR", 1.0);
    }

    public int getTwitterHits() {
        return config.getInt("DEFAULT_TWITTER_HITS", 20);
    }

    public int getFollowerHits() {
        return config.getInt("TWITTER_FOLLOW_SEARCH_HITS", 20);
    }

    int getNumberNews() {
        return config.getInt("NUMBER_NEWS", 10);
    }

    public int getMaxPosters() {
        return config.getInt("MAX_POSTERS", 5);
    }

    public int getMaxFollowers() {
        return config.getInt("MAX_FOLLOWERS", 2);
    }

    public int getTwitterMsgLength() {
        return config.getInt("TWITTER_MSG_LENGTH", 140);
    }

    public int getMinTitleLength() {
        return config.getInt("MIN_TITLE_LENGTH", 15);
    }


}
