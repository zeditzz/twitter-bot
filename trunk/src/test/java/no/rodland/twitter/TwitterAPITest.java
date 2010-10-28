package no.rodland.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterAPITest {

    private static final String TWITTER_USER = "HEISAN";

    @Test
    public void testGetSearchString() {
        List<String> queries = new ArrayList<String>();
        queries.add("whitehouse");
        queries.add("%22bill+clinton%22");
        Assert.assertEquals("-" + TWITTER_USER + " whitehouse OR %22bill+clinton%22",
                            TwitterAPI.getSearchStringExcludingUser(queries, TWITTER_USER));
    }

    @Test
    public void testGetSearchStringNull() {
        Assert.assertEquals("", TwitterAPI.getSearchStringExcludingUser(null, TWITTER_USER));
    }

    @Test
    public void testGetSearchStringEmptyList() {
        List<String> queries = new ArrayList<String>();
        Assert.assertEquals("", TwitterAPI.getSearchStringExcludingUser(queries, TWITTER_USER));
    }

    @Test
    @Ignore
    public void testGetFriends() throws TwitterException {
        List<String> queries = new ArrayList<String>();
        Twitter twitter = TwitterAPI.getAuthTwitter("", "", "", "");
        Set<String> list = TwitterAPI.getFriends(twitter);
        System.out.println("list.size() = " + list.size());
        System.out.println("list = " + list);

        Assert.assertEquals("", TwitterAPI.getSearchStringExcludingUser(queries, TWITTER_USER));
    }

    @Test
    @Ignore
    public void testGetFollowers() throws TwitterException {
        List<String> queries = new ArrayList<String>();
        Twitter twitter = TwitterAPI.getAuthTwitter("", "", "", "");
        Set<String> list = TwitterAPI.getFollowersIDs(twitter);
        System.out.println("list.size() = " + list.size());
        System.out.println("list = " + list);

        Assert.assertEquals("", TwitterAPI.getSearchStringExcludingUser(queries, TWITTER_USER));
    }
}
