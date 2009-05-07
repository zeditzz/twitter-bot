package no.rodland.twitter;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Test;

public class TwitterAPITest {
    private static final String TWITTER_USER = "HEISAN";

    @Test
    public void testGetSearchString() {
        List<String> queries = new ArrayList<String>();
        queries.add("schibsted");
        queries.add("%22kjell+aamot%22");
        Assert.assertEquals("-" + TWITTER_USER + " schibsted OR %22kjell+aamot%22", TwitterAPI.getSearchStringExcludingUser(queries, TWITTER_USER));
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
}
