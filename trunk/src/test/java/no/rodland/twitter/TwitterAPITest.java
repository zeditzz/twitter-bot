package no.rodland.twitter;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Test;

public class TwitterAPITest {

    @Test
    public void testGetSearchString() {
        List<String> queries = new ArrayList<String>();
        queries.add("schibsted");
        queries.add("%22kjell+aamot%22");
        Assert.assertEquals("-" + Config.TWITTER_USER + " schibsted OR %22kjell+aamot%22", TwitterAPI.getSearchStringExcludingUser(queries, Config.TWITTER_USER));
    }

    @Test
    public void testGetSearchStringNull() {
        Assert.assertEquals("", TwitterAPI.getSearchStringExcludingUser(null, Config.TWITTER_USER));
    }

    @Test
    public void testGetSearchStringEmptyList() {
        List<String> queries = new ArrayList<String>();
        Assert.assertEquals("", TwitterAPI.getSearchStringExcludingUser(queries, Config.TWITTER_USER));
    }
}
