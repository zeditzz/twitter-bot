package no.rodland.twitter;

import twitter4j.*;

import java.util.List;

import org.apache.log4j.Logger;

class TwitterRetriever {
    private static final Logger log = Logger.getLogger(TwitterRetriever.class);
    private final List<String> queries;
    private final String twitterUser;

    public TwitterRetriever(List<String> queries, String twitterUser) {
        this.queries = queries;
        this.twitterUser = twitterUser;
    }

    public List<Posting> retrieve() {
        List<Tweet> tweets = TwitterAPI.search(queries, twitterUser);
        tweets = TwitterAPI.filterTweets(tweets, twitterUser);
        List<Posting> postings =  TwitterAPI.getPostings(tweets);
        log.info("got " + postings.size() + " twitter-postings");
        return postings;
    }

}
