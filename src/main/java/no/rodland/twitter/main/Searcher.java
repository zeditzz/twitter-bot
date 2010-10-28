package no.rodland.twitter.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.rodland.twitter.TwitterAPI;
import org.apache.log4j.Logger;
import twitter4j.Tweet;
import twitter4j.TwitterException;

/**
 * Created by IntelliJ IDEA. User: fmr Date: May 6, 2009 Time: 10:51:26 AM
 */
public class Searcher extends AuthMain {

    private static final Logger log = Logger.getLogger(Searcher.class);
    private List<String> searchTerms;

    protected Searcher(String[] args) {
        super(true, args);
    }

    public static void main(String[] args) throws TwitterException {
        Searcher searcher = new Searcher(args);
        searcher.run();
    }

    @Override
    String getShortName() {
        return "searcher";
    }

    @Override
    String getLoadDescription() {
        return "\"<text to search for>\"";
    }

    @Override
    void doWork() throws TwitterException {
        List<Tweet> result = TwitterAPI.search(searchTerms, "testusername");
        for (Tweet tweet : result) {
            System.out.println("tweet = " + tweet);
        }
    }

    @Override
    void initArgs() {
        super.initArgs();
        searchTerms = new ArrayList<String>();
        String[] args = getArgs();
        searchTerms.addAll(Arrays.asList(args).subList(2, args.length));
    }
}
