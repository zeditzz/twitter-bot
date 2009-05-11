package no.rodland.twitter;

import junit.framework.TestCase;
import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 4, 2009
 * Time: 11:03:35 AM
 */
public class FeedUrlTest extends TestCase {
    public void testGetSource() {
        FeedUrl f = new FeedUrl("http://www.sesam.no/heisan?q=", "heisan");
        Assert.assertEquals("sesam.no heisan", f.getSource());
    }


    public void testGetSourceNoPreDomain() {
        FeedUrl f = new FeedUrl("http://sesam.no/heisan?q=", "heisan");
        Assert.assertEquals("sesam.no heisan", f.getSource());
    }

    public void testGetSourceNull() {
        FeedUrl f = new FeedUrl(null, "heisan");
        Assert.assertEquals(null, f.getSource());
    }
    public void testGetSourceSimple() {
        FeedUrl f = new FeedUrl("sesam.no?q=", "heisan");
        Assert.assertEquals("sesam.no?q=heisan", f.getSource());
    }
}
