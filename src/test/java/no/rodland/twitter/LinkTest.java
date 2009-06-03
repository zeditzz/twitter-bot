package no.rodland.twitter;

import org.junit.Test;
import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: Apr 29, 2009
 * Time: 10:02:37 PM
 */
public class LinkTest {

    @Test
    public void testSimplify() {
        Assert.assertEquals("https://rodland.no", Link.simplify("http://google.com/redirectto?url=https://rodland.no&heisan=hoppsan&ID=dhsjkhdkjshdui&hei=hopp"));
    }

    @Test
    public void testSimplifyLastSlash() {
        Assert.assertEquals("https://rodland.no", Link.simplify("http://google.com/redirectto?url=https://rodland.no/"));
    }

    @Test
    public void testSimplifyYahoo() {
        Assert.assertEquals("http://www.chicoer.com/ci_12122084?source=rss", Link.simplify("http://us.rd.yahoo.com/dailynews/rss/search/google/SIG=11h4ig1ld/*http%3A//www.chicoer.com/ci_12122084?source=rss"));
    }

    @Test
    public void testRemoveParamsSame() {
        assertSameRemoveParams("http://rodland.no");
        assertSameRemoveParams("http://rodland.no/");
        assertSameRemoveParams("http://rodland.no/index.html");
        assertSameRemoveParams("http://rodland.no/?heisan=hoppsan");
        assertSameRemoveParams("http://rodland.no/?heisan=hoppsan");
        assertSameRemoveParams("http://rodland.no/index.html/?heisan=hoppsan");
        assertSameRemoveParams("http://rodland.no/index.html?heisan=hoppsan");
    }

    @Test
    public void testRemoveParams() {
        Assert.assertEquals("https://rodland.no", Link.removeParams("https://rodland.no&heisan=hoppsan"));
    }

    @Test
    public void testRemoveParamsMultiple() {
        Assert.assertEquals("https://rodland.no", Link.removeParams("https://rodland.no&heisan=hoppsan&ID=dhsjkhdkjshdui&hei=hopp"));
    }

    @Test
    public void testRemoveMulitpleUrlsHttps() {
        Assert.assertEquals("https://vg.no", Link.removeMulitpleUrls("https://rodland.nohttps://vg.no"));
    }

    @Test
    public void testRemoveMulitpleUrlsNull() {
        Assert.assertNull(Link.removeMulitpleUrls(null));
    }

    @Test
    public void testRemoveParamsNull() {
        Assert.assertNull(Link.removeParams(null));
    }

    @Test
    public void testRemoveMulitpleUrlsMixHttpHttps() {
        Assert.assertEquals("https://vg.no", Link.removeMulitpleUrls("http://rodland.nohttps://vg.no"));
    }

    @Test
    public void testRemoveMulitpleUrls2Http() {
        Assert.assertEquals("http://vg.no", Link.removeMulitpleUrls("http://rodland.nohttp://vg.no"));
    }

    @Test
    public void testRemoveMulitpleUrls3Http() {
        Assert.assertEquals("http://vg.no", Link.removeMulitpleUrls("http://sesam.no/http://rodland.nohttp://vg.no"));
    }

    @Test
    public void testRemoveMulitpleUrls() {
        assertSameMulitpleUrls("http://rodland.no");
        assertSameMulitpleUrls("http://rodland.no/");
        assertSameMulitpleUrls("http://rodland.no/index.html");
        assertSameMulitpleUrls("http://rodland.no/?heisan=hoppsan");
        assertSameMulitpleUrls("http://rodland.no/?heisan=hoppsan&hei=hopp");
        assertSameMulitpleUrls("http://rodland.no/?heisan=hoppsan");
        assertSameMulitpleUrls("http://rodland.no/index.html/?heisan=hoppsan");
        assertSameMulitpleUrls("http://rodland.no/index.html?heisan=hoppsan");
    }

    private static void assertSameMulitpleUrls(String url) {
        Assert.assertEquals(url, Link.removeMulitpleUrls(url));
    }

    private static void assertSameRemoveParams(String url) {
        Assert.assertEquals(url, Link.removeParams(url));
    }

    @Test
    public void testRemoveLastSlash() {
        Assert.assertEquals("https://rodland.no", Link.removeLastSlash("https://rodland.no/"));
        Assert.assertEquals("http://rodland.no", Link.removeLastSlash("http://rodland.no/"));
        Assert.assertEquals("", Link.removeLastSlash("/"));
        Assert.assertEquals("", Link.removeLastSlash(""));
        Assert.assertEquals(null, Link.removeLastSlash(null));
    }
}
