package no.rodland.twitter;

import com.sun.syndication.feed.synd.SyndEntry;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import twitter4j.Status;
import twitter4j.Tweet;

/**
 * <p/>
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: Apr 29, 2009
 * Time: 11:32:33 PM
 */
public class Posting implements Comparable<Posting> {
    private static final Logger log = Logger.getLogger(Posting.class);

    private final Date updated;
    private final String title;
    private final Link link;
    private final String src;
    // XXX: replace with values from Config without making it very dependent
    private static final int maxMsgLength = 140;
    private static final int minTitleLength = 15;
    private static final Pattern TITLE_WITH_URL = Pattern.compile("(.*)(https?://[^ ]*) *$");

    public Posting(Date updated, String title, Link link, String src) {
        this.updated = updated == null? new Date(0) : new Date(updated.getTime());
        this.title = title;
        this.link = link;
        this.src = src;
    }

    public Posting(SyndEntry entry, String src) {
        this.updated = entry.getPublishedDate();
        this.title = entry.getTitle().split("\n")[0];
        this.link = new Link(entry.getLink());
        this.src = src;
    }

    public Posting(Date updated, String title, String src) {
        this.updated = updated == null? new Date(0) : new Date(updated.getTime());
        this.title = extractTitle(title);
        this.link = extractLink(title);
        this.src = src;
    }

    public Posting(Status status) {
        this(status.getCreatedAt(), getTitle(status), "Twitter: @" + status.getUser().getScreenName() + " (" + status.getId() + ")");
    }

    public Posting(Tweet tweet) {
        this(tweet.getCreatedAt(), getTitle(tweet), "Twitter: @" + tweet.getFromUser() + " (" + tweet.getId() + ")");
    }

    public Date getUpdated() {
        return new Date(updated.getTime());
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        if (link == null) {
            log.info("no link for posting: " + this);
            return "";
        }
        return link.getLink();
    }

    public String getSrc() {
        return src;
    }

    public String getStatus() {
        String url = link.getLink();
        String status = title + ": " + url;
        if (status.length() > maxMsgLength) {
            if (url.length() < maxMsgLength) {
                int end = title.length() - (status.length() - (maxMsgLength - 3));
                if (end > minTitleLength) {
                    status = title.substring(0, end) + "...";
                    status += "".equals(url) ? "" : ": " + url;
                }
                else {
                    status = title;
                }
            }
            else {
                if (title.length() > maxMsgLength) {
                    status = title.substring(0, maxMsgLength);
                }
                else {
                    status = title;
                }
            }
        }
        // this should not happen - too often that is....
        if (status.length() > 140) {
            log.error("status longer than 140: " + status);
            status = status.substring(0, 139);
        }
        return status;
    }

    public int compareTo(Posting p) {
        int test = getUpdated().compareTo(p.getUpdated());
        if (test != 0) {
            return test;
        }
        test = getTitle().compareTo(p.getTitle());
        if (test != 0) {
            return test;
        }
        test = getUrl().compareTo(p.getUrl());
        if (test != 0) {
            return test;
        }
        return getSrc().compareTo(p.getSrc());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Posting)) {
            return false;
        }

        Posting otherPosting = (Posting) obj;
        return areEqual(this.getUpdated(), otherPosting.getUpdated()) &&
                areEqual(this.getTitle(), otherPosting.getTitle()) &&
                areEqual(this.getUrl(), otherPosting.getUrl()) &&
                areEqual(this.getSrc(), otherPosting.getSrc());
    }

    private static boolean areEqual(Object aThis, Object aThat) {
        return aThis == null ? aThat == null : aThis.equals(aThat);
    }

    private static String getTitle(Tweet tweet) {
        return getReTweetTitle(tweet.getFromUser(), tweet.getText());
    }

    private static String getTitle(Status status) {
        return getReTweetTitle(status.getUser().getScreenName(), status.getText());
    }

    private static String getReTweetTitle(String fromUser, String text) {
        return "RT @" + fromUser + ": " + text;
    }

    public String extractTitle(String origTitle) {
        if (origTitle == null) {
            return null;
        }
        Matcher m = TITLE_WITH_URL.matcher(origTitle);
        if (m.matches()) {
            return m.group(1);
        }
        return origTitle;
    }

    public Link extractLink(String origTitle) {
        if (origTitle == null) {
            return null;
        }
        Matcher m = TITLE_WITH_URL.matcher(origTitle);
        if (m.matches()) {
            return new Link(m.group(2));
        }
        return null;
    }

    @Override
    public String toString() {
        return "Posting{" +
                "updated=" + updated +
                ", title='" + title + '\'' +
                ", link=" + link +
                ", src='" + src + '\'' +
                '}';
    }
}
