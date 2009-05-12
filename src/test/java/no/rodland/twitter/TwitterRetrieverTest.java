package no.rodland.twitter;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: Apr 30, 2009
 * Time: 6:20:58 PM
 */
public class TwitterRetrieverTest {
    private static final String TWITTER_USER = "twitteruser";

    @Test
    public void testGetSearchStringWithoutUser() {
        List<String> queries = new ArrayList<String>();
        queries.add("whitehouse");
        queries.add("%22bill+clinton%22");
        Assert.assertEquals("-" + TWITTER_USER + " whitehouse OR %22bill+clinton%22", TwitterAPI.getSearchStringExcludingUser(queries, TWITTER_USER));
    }

    @Test
    public void testGetSearchString() {
        List<String> queries = new ArrayList<String>();
        queries.add("whitehouse");
        queries.add("%22bill+clinton%22");
        Assert.assertEquals("whitehouse OR %22bill+clinton%22", TwitterAPI.getSearchString(queries));
    }

    @Test
    public void testGetSearchStringNull() {
        Assert.assertEquals("", TwitterAPI.getSearchString(null));
    }

    @Test
    public void testGetSearchStringEmptyList() {
        List<String> queries = new ArrayList<String>();
        Assert.assertEquals("", TwitterAPI.getSearchString(queries));
    }
}
