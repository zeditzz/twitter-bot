package no.rodland.twitter;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 6, 2009
 * Time: 10:55:30 AM
 */
public class Config {
    private static final Logger log = Logger.getLogger(Config.class);
    @SuppressWarnings({"FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
    private final Properties prop = new Properties();

    public static final double FOLLOW_FACTOR = 1.2;
    public static final int DEFAULT_TWITTER_HITS = 30;
    public static final int TWITTER_FOLLOW_SEARCH_HITS = 50;
    private static final int NUMBER_NEWS = 20;
    public static final int MAX_POSTERS = 5;
    public static final int MAX_FOLLOWERS = 3;
    public static final int TWITTER_MSG_LENGTH = 140;
    public static final int MIN_TITLE_LENGTH = 15;


    @SuppressWarnings({"UnusedDeclaration"})
    public Config(String fileName) {
        log.info("Loading properties from " + fileName);
        try {
            prop.load(new FileInputStream(fileName));
        } catch (IOException ex) {
            log.error("Configuration file not found:" + ex.getMessage());
            System.exit(-1);
        }
        //this.twitter = new Twitter(prop.getProperty("id"), prop.getProperty("password"));
        //this.feedurl = prop.getProperty("feedurl");
        //this.lastUpdate = new Date(Long.valueOf(prop.getProperty("lastUpdate", "0")));

    }

    static List<String> getTwitterQueries() {
        List<String> queries = new ArrayList<String>();
        queries.add("schibsted");
        queries.add("schibsteds");
        queries.add("finn.no");
        queries.add("nettby");
        queries.add("finn_no");
        queries.add("blocket.se");
        queries.add("%22kjell+aamot%22");
        return queries;
    }

    static List<String> getFollowerQueries() {
        List<String> queries = new ArrayList<String>();
        queries.add("schibsted");
        queries.add("aftenposten");
        queries.add("vg.no");
        queries.add("aftonbladet");
        queries.add("finn.no");
        queries.add("blocket.se");
        queries.add("%22kjell+aamot%22");
        return queries;
    }

    /**
     * Constructs a series of RSS-urls based on two lists: one with queries and ne with urls.  A new URL will be created for each pair of these.
     *
     * @return a list of ready-to-call RSS URLs.
     */
    static List<String> getFeedUrls() {
        List<String> queries = new ArrayList<String>();
        queries.add("schibsted");
        queries.add("%22kjell+aamot%22");
        queries.add("%22finn.no%22");
        queries.add("%22blocket.se%22");
        List<String> sites = new ArrayList<String>();
        sites.add("http://nyheter.abcsok.no/search/rss?rows=" + NUMBER_NEWS + "&q=");
        sites.add("http://sesam.no/search/?c=m&x=0&y=0&layout=rss&q=");
        sites.add("http://news.google.no/news?pz=1&ned=no_no&hl=no&as_qdr=w&as_drrb=q&scoring=n&output=rss&num=" + NUMBER_NEWS + "&q=");
        sites.add("http://news.search.yahoo.com/news/rss?ei=UTF-8&eo=UTF-8&n=" + NUMBER_NEWS + "&p=");

        List<String> myList = new ArrayList<String>();
        for (String site : sites) {
            for (String query : queries) {
                myList.add(site + query);
            }
        }
        return myList;
    }
}
