package no.rodland.twitter;

import java.util.List;

import org.apache.log4j.Logger;
import twitter4j.Tweet;

public class TwitterRetriever {

    private static final Logger log = Logger.getLogger(TwitterRetriever.class);
    private final List<String> queries;
    private final String twitterUser;
    private final Config cfg;

    public TwitterRetriever(List<String> queries, String twitterUser, Config cfg) {
        this.queries = queries;
        this.twitterUser = twitterUser;
        this.cfg = cfg;
    }

    public List<Posting> retrieve() {
        TwitterAPI.setConfig(cfg);
        List<Tweet> tweets = TwitterAPI.search(queries, twitterUser);
        tweets = TwitterAPI.filterTweets(tweets, twitterUser);
        List<Posting> postings = TwitterAPI.getPostings(tweets);
        log.info("got " + postings.size() + " twitter-postings");
        return postings;
    }
}
