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
public class Posting implements Comparable{
    static Logger log = Logger.getLogger(Posting.class);

    private Date updated;
    private String title;
    private Link link;
    private String src;

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

    public int compareTo(Object o) {
        Posting posting = (Posting)o;
        return getUpdated().compareTo(posting.getUpdated());
    }

    static String formatStatus(String title, String link) {
        String status = title + ": " + link;
        //title = title.replaceAll("b", "");
        if (status.length() > Config.TWITTER_MSG_LENGTH) {
            if (link.length() < Config.TWITTER_MSG_LENGTH) {
                int end = title.length() - (status.length() - (Config.TWITTER_MSG_LENGTH - 3));
                if (end > Config.MIN_TITLE_LENGTH) {
                    status = title.substring(0, end) + "...";
                    status += "".equals(link) ? "" :  ": " + link;
                } else {
                    status = title;
                }
            } else {
                if (title.length() > Config.TWITTER_MSG_LENGTH) {
                    status = title.substring(0, Config.TWITTER_MSG_LENGTH);
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
}
