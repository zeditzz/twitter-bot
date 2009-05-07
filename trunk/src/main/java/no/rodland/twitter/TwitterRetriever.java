package no.rodland.twitter;

import twitter4j.*;

import java.util.List;

import org.apache.log4j.Logger;

public class TwitterRetriever implements PostingRetriever {
    private static Logger log = Logger.getLogger(TwitterRetriever.class);
    private List<String> queries;
    private String twitterUser;

    public TwitterRetriever(List<String> queries, String twitterUser) {
        this.queries = queries;
        this.twitterUser = twitterUser;
    }

    public List<Posting> retrieve() {
        List<Tweet> tweets = TwitterAPI.search(queries, twitterUser);
        tweets = TwitterAPI.filterTweets(tweets, twitterUser);
        return TwitterAPI.getPostings(tweets);
    }

}
