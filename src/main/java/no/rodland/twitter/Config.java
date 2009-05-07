package no.rodland.twitter;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 6, 2009
 * Time: 10:55:30 AM
 */
public class Config {
    public static final String TWITTER_USER = "schibstednews";
    public static final String TWITTER_PASSWORD = "fmr123";
    public static final String TEST_TWITTER_USER = "ntesttest";
    public static final String TEST_TWITTER_PASSWORD = "ntesttest1";
    public static final double FOLLOW_FACTOR = 1.2;
    public static final int DEFAULT_TWITTER_HITS = 30;
    public static final int TWITTER_FOLLOW_SEARCH_HITS = 50;
    public static final int NUMBER_NEWS = 20;
    public static final int MAX_POSTERS = 5;
    public static final int MAX_FOLLOWERS = 3;
    public static final int TWITTER_MSG_LENGTH = 140;
    public static final int MIN_TITLE_LENGTH = 15;

    static List<String> getTwitterQueries() {
        List<String> queries = new ArrayList<String>();
        queries.add("schibsted");
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
