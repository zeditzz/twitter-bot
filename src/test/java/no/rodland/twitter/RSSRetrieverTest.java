package no.rodland.twitter;

import junit.framework.TestCase;
import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 4, 2009
 * Time: 11:03:35 AM
 */
public class RSSRetrieverTest extends TestCase {
    public void testGetSource() {
        Assert.assertEquals("sesam.no", RSSRetriever.getSource("http://www.sesam.no/heisan"));
    }


    public void testGetSourceNoPreDomain() {
        Assert.assertEquals("sesam.no", RSSRetriever.getSource("http://sesam.no/heisan"));
    }

    public void testGetSourceNull() {
        Assert.assertEquals(null, RSSRetriever.getSource(null));
    }
    public void testGetSourceSimple() {
        Assert.assertEquals("sesam.no", RSSRetriever.getSource("sesam.no"));
    }
}
