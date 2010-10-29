package no.rodland.twitter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: fmr Date: May 6, 2009 Time: 10:55:30 AM
 */
public class Config {

    private static final Logger log = Logger.getLogger(Config.class);
    public static final Config NULL_CONFIG = new Config();

    private PropertiesConfiguration config;

    private static final String CFG_KEY_BLACKLIST = "blacklist";
    private static final String CFG_KEY_DEFAULT_TWITTER_HITS = "DEFAULT_TWITTER_HITS";
    private static final String CFG_KEY_FOLLOWER_QUERY = "followerquery";
    private static final String CFG_KEY_FOLLOW_FACTOR = "FOLLOW_FACTOR";
    private static final String CFG_KEY_LASTUPDATED = "lastupdated";
    private static final String CFG_KEY_MAX_FOLLOWERS = "MAX_FOLLOWERS";
    private static final String CFG_KEY_MAX_POSTERS = "MAX_POSTERS";
    private static final String CFG_KEY_MIN_TITLE_LENGTH = "MIN_TITLE_LENGTH";
    private static final String CFG_KEY_NUMBER_NEWS = "NUMBER_NEWS";
    private static final String CFG_KEY_RSS_QUERY = "rssquery";
    private static final String CFG_KEY_SITES = "sites";
    private static final String CFG_KEY_TWITTERPASSWORD = "twitterpassword";
    private static final String CFG_KEY_TWITTERUSER = "twitteruser";
    private static final String CFG_KEY_TWITTER_FOLLOW_SEARCH_HITS = "TWITTER_FOLLOW_SEARCH_HITS";
    private static final String CFG_KEY_TWITTER_MSG_LENGTH = "TWITTER_MSG_LENGTH";
    private static final String CFG_KEY_TWITTER_QUERY = "twitterquery";
    private static final String CFG_KEY_URLS = "urls";
    private static final String NUM_REPLACE_STRING = "NUMBER_NEWS";
    private static final String CFG_KEY_CONTENT_FILTER = "contentfilter";
    private static final String CFG_KEY_MAX_POSTINGS_PR_RUN = "MAX_POSTINGS_PR_RUN";
    private static final String CFG_KEY_NOT_SEND_EMAIL_FOR_THESE_WORDS = "not_email";

    private static final String CFG_KEY_CONSUMER_KEY = "consumer_key";
    private static final String CFG_KEY_CONSUMER_KEY_SECRET = "consumer_key_secret";
    private static final String CFG_KEY_ACCESS_TOKEN = "access_token";
    private static final String CFG_KEY_ACCESS_TOKEN_SECRET = "access_token_secret";
    private static final String CFG_KEY_ACTUALLY_POST = "ACTUALLY_POST";

    private Config() {
        config = new PropertiesConfiguration();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Config(String fileName) {
        log.info("Loading properties from " + fileName);
        try {
            config = new PropertiesConfiguration(fileName);
        }
        catch (ConfigurationException e) {
            log.error("Error loading config - exiting", e);
            System.exit(1);
        }
    }

    public void update() {
        update(new Date());
    }

    public void update(Date lastUpdated) {
        config.setProperty(CFG_KEY_LASTUPDATED, lastUpdated.getTime());
        log.info("setting last updated in " + config.getFileName() + " to " + config.getLong(CFG_KEY_LASTUPDATED));
        config.clearProperty("norske_bostaver");
        config.addProperty("norske_bostaver", "ae: �");
        config.addProperty("norske_bostaver", "oe: �");
        config.addProperty("norske_bostaver", "aa: �");
        config.addProperty("norske_bostaver", "AE: �");
        config.addProperty("norske_bostaver", "OE: �");
        config.addProperty("norske_bostaver", "AA: �");

        try {
            config.save();
        }
        catch (ConfigurationException e) {
            log.error("ERROR setting last updated in " + config.getFileName() + " to " + config.getLong(
                    CFG_KEY_LASTUPDATED), e);
        }
    }

    public Date getLastUpdated() {
        long time = config.getLong(CFG_KEY_LASTUPDATED, 0);
        return new Date(time);
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getTwitterQueries() {
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
        List users = config.getList(CFG_KEY_BLACKLIST);
        return users.contains(potentialTwitterUser);
    }

    /**
     * Checks <code>content</code> and returns <code>null</code> if content ok, otherwise returns the word that is bad.
     *
     * @param content the content to check
     * @return <code>null</code> if content ok, otherwise returns the word that is bad.
     */
    public String isBadContent(String content) {
        if (content == null) {
            return null;
        }
        @SuppressWarnings({"unchecked"})
        List<String> badWords = config.getList(CFG_KEY_CONTENT_FILTER);
        content = content.toLowerCase();
        for (String badWord : badWords) {
            if (content.contains(badWord)) {
                return badWord;
            }
        }
        return null;
    }

    public boolean sendEmail(String bad) {
        boolean doNotSendEmail = config.getList(CFG_KEY_NOT_SEND_EMAIL_FOR_THESE_WORDS).contains(bad);
        return !doNotSendEmail;
    }

    /**
     * Constructs a series of RSS-urls based on two lists: one with queries and ne with urls.  A new URL will be created
     * for each pair of these.
     *
     * @return a list of ready-to-call RSS URLs.
     */
    public List<FeedUrl> getFeedUrls() {
        List urls = config.getList(CFG_KEY_URLS);
        List queries = config.getList(CFG_KEY_RSS_QUERY);
        List sites = config.getList(CFG_KEY_SITES);
        List<FeedUrl> myList = new ArrayList<FeedUrl>();
        for (Object url : urls) {
            String site = ((String) url).replaceAll(NUM_REPLACE_STRING, Integer.toString(getNumberNews()));
            myList.add(new FeedUrl(site));
        }
        for (Object siteObj : sites) {
            String site = ((String) siteObj).replaceAll(NUM_REPLACE_STRING, Integer.toString(getNumberNews()));
            for (Object query : queries) {
                myList.add(new FeedUrl(site, (String) query));
            }
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
        return config.getDouble(CFG_KEY_FOLLOW_FACTOR, 1.0);
    }

    public int getTwitterHits() {
        return config.getInt(CFG_KEY_DEFAULT_TWITTER_HITS, 20);
    }

    public int getFollowerHits() {
        return config.getInt(CFG_KEY_TWITTER_FOLLOW_SEARCH_HITS, 20);
    }

    int getNumberNews() {
        return config.getInt(CFG_KEY_NUMBER_NEWS, 10);
    }

    public int getMaxPosters() {
        return config.getInt(CFG_KEY_MAX_POSTERS, 5);
    }

    public int getMaxFollowers() {
        return config.getInt(CFG_KEY_MAX_FOLLOWERS, 2);
    }

    public int getTwitterMsgLength() {
        return config.getInt(CFG_KEY_TWITTER_MSG_LENGTH, 140);
    }

    public int getMinTitleLength() {
        return config.getInt(CFG_KEY_MIN_TITLE_LENGTH, 15);
    }

    public int getMaxPostingsPrRun() {
        return config.getInt(CFG_KEY_MAX_POSTINGS_PR_RUN, 4);
    }

    public String getConsumerKey() {
        return config.getString(CFG_KEY_CONSUMER_KEY);
    }

    public String getConsumerKeySecret() {
        return config.getString(CFG_KEY_CONSUMER_KEY_SECRET);
    }

    public String getAccessToken() {
        return config.getString(CFG_KEY_ACCESS_TOKEN);
    }

    public String getAccessTokenSecret() {
        return config.getString(CFG_KEY_ACCESS_TOKEN_SECRET);
    }

    public boolean getActuallyPost() {
        return config.getBoolean(CFG_KEY_ACTUALLY_POST);
    }
}
