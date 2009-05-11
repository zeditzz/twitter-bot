package no.rodland.twitter;

import com.sun.syndication.feed.synd.SyndEntry;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: Apr 29, 2009
 * Time: 11:32:33 PM
 */
public class Posting implements Comparable<Posting>{
    private static final Logger log = Logger.getLogger(Posting.class);

    private final Date updated;
    private final String title;
    private final Link link;
    private final String src;
    // XXX: replace with values from Config without making it very dependent
    private static final int maxMsgLength = 140;
    private static final int minTitleLength = 15;

    public Posting(Date updated, String title, Link link, String src) {
        this.updated = updated;
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

    public Date getUpdated() {
        return updated;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        if (link == null){
            log.info("no link for posting: " + this);
            return "";
        }
        return link.getLink();
    }

    public String getSrc() {
        return src;
    }

    static String formatStatus(String title, String link) {
        String status = title + ": " + link;
        if (status.length() > maxMsgLength) {
            if (link.length() < maxMsgLength) {
                int end = title.length() - (status.length() - (maxMsgLength- 3));
                if (end > minTitleLength) {
                    status = title.substring(0, end) + "...";
                    status += "".equals(link) ? "" :  ": " + link;
                } else {
                    status = title;
                }
            } else {
                if (title.length() > maxMsgLength) {
                    status = title.substring(0, maxMsgLength);
                } else {
                    status = title;
                }
            }
        }
        return status;
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

    public int compareTo(Posting p) {
        return getUpdated().compareTo(p.getUpdated());
    }
}
