package no.rodland.twitter;

import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;
import twitter4j.TwitterException;

public class PostingTest {
    private static final int NORMAL_URL_LENGTH = 30;
    private static final int EXTRA_PLACE_LENGTH = 5;
    // XXX hmm. should match value in cfg
    private static final int TWITTER_MSG_LENGTH = 140;
    private static final int MIN_TITLE_LENGTH = 15;
    private static final String TEST_URL = "http://just.a.test.url/heisan?q=hei";

    @Test
    public void testCompareTo() {
        Posting p1 = new Posting(new Date(1), null, null, null);
        Posting p2 = new Posting(new Date(2), null, null, null);
        Posting p3 = new Posting(new Date(3), null, null, null);
        Posting p4 = new Posting(new Date(4), null, null, null);

        List<Posting> list = new ArrayList<Posting>();
        list.add(p3);
        list.add(p2);
        list.add(p4);
        list.add(p1);

        Assert.assertEquals(3, list.get(0).getUpdated().getTime());
        Assert.assertEquals(2, list.get(1).getUpdated().getTime());
        Assert.assertEquals(4, list.get(2).getUpdated().getTime());
        Assert.assertEquals(1, list.get(3).getUpdated().getTime());

        Collections.sort(list);

        Assert.assertEquals(1, list.get(0).getUpdated().getTime());
        Assert.assertEquals(2, list.get(1).getUpdated().getTime());
        Assert.assertEquals(3, list.get(2).getUpdated().getTime());
        Assert.assertEquals(4, list.get(3).getUpdated().getTime());
    }

    @Test
    public void testFormat() {
        Posting p = new Posting(null, "heisan", new Link("hoppsan"), null);
        Assert.assertEquals("heisan: hoppsan", p.getStatus());
    }

    @Test
    public void testExtractLink() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        Link link = p.extractLink("heisan " + TEST_URL);
        Assert.assertEquals(TEST_URL, link.getLink());
    }

    @Test
    public void testExtractLinkExtraSpace() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        Link link = p.extractLink("heisan " + TEST_URL + "    ");
        Assert.assertEquals(TEST_URL, link.getLink());
    }

    @Test
    public void testExtractLinkNoLink() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        Link link = p.extractLink("heisan ");
        Assert.assertNull(link);
    }

    @Test
    public void testExtractLinkNull() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        Link link = p.extractLink(null);
        Assert.assertNull(link);
    }

    @Test
    public void testExtractLinkNoSpace() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        Link link = p.extractLink("heisan" + TEST_URL);
        Assert.assertEquals(TEST_URL, link.getLink());
    }

    @Test
    public void testExtractText() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        String title = p.extractTitle("heisan " + TEST_URL);
        Assert.assertEquals("heisan ", title);
    }

    @Test
    public void testExtractTextNoLinke() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        String title = p.extractTitle("heisan");
        Assert.assertEquals("heisan", title);
    }

    @Test
    public void testExtractTextNull() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        String title = p.extractTitle(null);
        Assert.assertNull(title);
    }

    @Test
    public void testExtractTextNoSpace() throws TwitterException {
        Posting p = new Posting(null, null, null, null);
        String title = p.extractTitle("heisan" + TEST_URL);
        Assert.assertEquals("heisan", title);
    }

    @Test
    public void testLongReTweet() {
        String orig = "RT @" + "fredrikr" + ": stusser veldig over utviklingen til sesam. vet at sesam har sparka selgerne, men nå sparka de jaggu designen også: ";
        String withLink = orig + "http://sesam.no/";
        String expected = "RT @fredrikr: stusser veldig over utviklingen til sesam. vet at sesam har sparka selgerne, men nå sparka de jaggu desig...: http://sesam.no/";
        Posting p = new Posting(new Date(), withLink, "SRC");
        //Assert.assertEquals("http://sesam.no/", p.getUrl());
        //Assert.assertEquals(orig, p.getTitle());
        Assert.assertEquals(expected, p.getStatus());
    }

    @Test
    public void testFormatStatusPlain() {
        Posting posting = new Posting(null, "this is a title", new Link("http://rodland.no"), null);
        Assert.assertEquals("this is a title: http://rodland.no", posting.getStatus());
    }

    @Test
    public void testFormatStatusLongerUrlThanLimit() {
        Posting posting = new Posting(null, rep('t', 30), new Link(rep('l', 150)), null);
        String expected = rep('t', 30);
        Assert.assertEquals(expected, posting.getStatus());
    }

    @Test
    public void testFormatStatusLongUrlLimit() {
        Posting posting = new Posting(null, rep('t', 30), new Link(rep('l', TWITTER_MSG_LENGTH - 1)), null);
        String expected = rep('t', 30);
        Assert.assertEquals(expected, posting.getStatus());
        posting = new Posting(null, rep('t', 30), new Link(rep('l', TWITTER_MSG_LENGTH - MIN_TITLE_LENGTH)), null);
        expected = rep('t', 30);
        Assert.assertEquals(expected, posting.getStatus());
        posting = new Posting(null, rep('t', 30), new Link(rep('l', TWITTER_MSG_LENGTH - MIN_TITLE_LENGTH - EXTRA_PLACE_LENGTH)), null);
        expected = rep('t', 30);
        Assert.assertEquals(expected, posting.getStatus());
        int urllength = TWITTER_MSG_LENGTH - MIN_TITLE_LENGTH - EXTRA_PLACE_LENGTH - 1;
        posting = new Posting(null, rep('t', 30), new Link(rep('l', urllength)), null);
        expected = rep('t', TWITTER_MSG_LENGTH - EXTRA_PLACE_LENGTH - urllength) + "...: " + rep('l', urllength);
        Assert.assertEquals(expected, posting.getStatus());
    }

    @Test
    public void testFormatStatusLimit() {
        // under limit
        Posting posting = new Posting(null, rep('t', 107), new Link(rep('l', NORMAL_URL_LENGTH)), null);
        String expected = rep('t', 107) + ": " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, posting.getStatus());
        // on limit (140)
        posting = new Posting(null, rep('t', 108),new Link( rep('l', NORMAL_URL_LENGTH)), null);
        expected = rep('t', 108) + ": " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, posting.getStatus());
        // one over limit
        posting = new Posting(null, rep('t', 109), new Link(rep('l', NORMAL_URL_LENGTH)), null);
        expected = rep('t', TWITTER_MSG_LENGTH - NORMAL_URL_LENGTH - EXTRA_PLACE_LENGTH) + "...: " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, posting.getStatus());
    }

    @Test
    public void testFormatStatusLongTitle() {
        Posting posting = new Posting(null, rep('t', 150),new Link( rep('l', NORMAL_URL_LENGTH)), null);
        String expected = rep('t', TWITTER_MSG_LENGTH - NORMAL_URL_LENGTH - EXTRA_PLACE_LENGTH) + "...: " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, posting.getStatus());
        posting = new Posting(null, rep('t', 130),new Link( rep('l', NORMAL_URL_LENGTH)), null);
        expected = rep('t', TWITTER_MSG_LENGTH - NORMAL_URL_LENGTH - EXTRA_PLACE_LENGTH) + "...: " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, posting.getStatus());
    }

    /**
     * Produce a String of a given repeating character.
     *
     * @param c     the character to repeat
     * @param count the number of times to repeat
     * @return String, e.g. rep('*',4) returns "****"
     */
    private static String rep(char c, int count) {
        char[] s = new char[count];
        for (int i = 0; i < count; i++) {
            s[i] = c;
        }
        return new String(s);
    } // end rep
}

