package no.rodland.twitter;

import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;

public class PostingTest {
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
    public void testFormat(){
        Assert.assertEquals("heisan: hoppsan", Posting.formatStatus("heisan", "hoppsan"));
    }
    @Test
    public void testMath(){
        int numerbNew =(int)Math.ceil((1.2d * 63) - 70);
        System.out.println("numerbNew = " + numerbNew);
    }



}

