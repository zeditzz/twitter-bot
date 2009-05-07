package no.rodland.twitter;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: Apr 30, 2009
 * Time: 1:58:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Retriever {
    List<Posting> retrieve();
}
