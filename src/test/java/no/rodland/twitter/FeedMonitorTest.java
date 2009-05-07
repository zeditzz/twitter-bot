package no.rodland.twitter;


import junit.framework.Assert;
import org.junit.Test;


@SuppressWarnings({"MagicNumber"})
public class FeedMonitorTest {
    private static final int NORMAL_URL_LENGTH = 30;
    private static final int EXTRA_PLACE_LENGTH = 5;
    private static final int TWITTER_MSG_LENGTH = Config.TWITTER_MSG_LENGTH;
    private static final int MIN_TITLE_LENGTH = Config.MIN_TITLE_LENGTH;


    @Test
    public void testFormatStatusPlain() {
        String status = Posting.formatStatus("this is a title", "http://rodland.no");
        Assert.assertEquals("this is a title: http://rodland.no", status);

    }

    @Test
    public void testFormatStatusLongerUrlThanLimit() {
        String status = Posting.formatStatus(rep('t', 30), rep('l', 150));
        String expected = rep('t', 30);
        Assert.assertEquals(expected, status);
    }

    @Test
    public void testFormatStatusLongUrlLimit() {
        String status = Posting.formatStatus(rep('t', 30), rep('l', TWITTER_MSG_LENGTH - 1));
        String expected = rep('t', 30);
        Assert.assertEquals(expected, status);
        status = Posting.formatStatus(rep('t', 30), rep('l', TWITTER_MSG_LENGTH - MIN_TITLE_LENGTH));
        expected = rep('t', 30);
        Assert.assertEquals(expected, status);
        status = Posting.formatStatus(rep('t', 30), rep('l', TWITTER_MSG_LENGTH - MIN_TITLE_LENGTH - EXTRA_PLACE_LENGTH));
        expected = rep('t', 30);
        Assert.assertEquals(expected, status);
        int urllength =                                          TWITTER_MSG_LENGTH - MIN_TITLE_LENGTH - EXTRA_PLACE_LENGTH - 1;
        status = Posting.formatStatus(rep('t', 30), rep('l', urllength));
        expected = rep('t', TWITTER_MSG_LENGTH - EXTRA_PLACE_LENGTH - urllength) + "...: " + rep('l', urllength);
        Assert.assertEquals(expected, status);
    }


    @Test
    public void testFormatStatusLimit() {
        // under limit
        String status = Posting.formatStatus(rep('t', 107), rep('l', NORMAL_URL_LENGTH));
        String expected = rep('t', 107) + ": " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, status);
        // on limit (140)
        status = Posting.formatStatus(rep('t', 108), rep('l', NORMAL_URL_LENGTH));
        expected = rep('t', 108) + ": " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, status);
        // one over limit
        status = Posting.formatStatus(rep('t', 109), rep('l', NORMAL_URL_LENGTH));
        expected = rep('t', TWITTER_MSG_LENGTH - NORMAL_URL_LENGTH - EXTRA_PLACE_LENGTH) + "...: " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, status);
    }

    @Test
    public void testFormatStatusLongTitle() {
        String status = Posting.formatStatus(rep('t', 150), rep('l', NORMAL_URL_LENGTH));
        String expected = rep('t', TWITTER_MSG_LENGTH - NORMAL_URL_LENGTH - EXTRA_PLACE_LENGTH) + "...: " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, status);
        status = Posting.formatStatus(rep('t', 130), rep('l', NORMAL_URL_LENGTH));
        expected = rep('t', TWITTER_MSG_LENGTH - NORMAL_URL_LENGTH - EXTRA_PLACE_LENGTH) + "...: " + rep('l', NORMAL_URL_LENGTH);
        Assert.assertEquals(expected, status);
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
