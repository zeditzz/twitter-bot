package no.rodland.twitter.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by IntelliJ IDEA. User: fmr Date: May 6, 2009 Time: 10:51:26 AM
 */
public class Deleter extends AuthMain {

    private static final Logger log = Logger.getLogger(Deleter.class);

    private List<Long> statusIds;

    protected Deleter(String[] args) {
        super(false, args);
    }

    public static void main(String[] args) throws TwitterException {
        Deleter deleter = new Deleter(args);
        deleter.run();
    }

    @Override
    String getShortName() {
        return "deleter";
    }

    @Override
    String getLoadDescription() {
        return "<status-ids-to-delete> ... <status-ids-to-delete>";
    }

    @Override
    void doWork() throws TwitterException {
        Twitter tw = getTwitter();
        log.info("deleting status: " + statusIds);
        for (Long id : statusIds) {
            log.info(tw.destroyStatus(id));
        }
    }

    @Override
    void initArgs() {
        super.initArgs();
        statusIds = new ArrayList<Long>();
        String[] args = getArgs();
        for (int i = 4; i < args.length; i++) {
            statusIds.add(Long.valueOf(args[i]));
        }
    }
}