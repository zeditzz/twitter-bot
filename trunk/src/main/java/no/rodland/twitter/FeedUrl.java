package no.rodland.twitter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: fmr
 * Date: May 11, 2009
 * Time: 8:02:51 AM
 */
public class FeedUrl {
    private String baseUrl;
    private String query;
    private static final Pattern MULTIPLE_URLS = Pattern.compile("https?://([^/]*\\.)?([^/]*\\.[^/]*)/.*");

    public FeedUrl(String baseUrl, String query) {
        this.baseUrl = baseUrl;
        this.query = query;
    }

    public FeedUrl(String url) {
        this.baseUrl = url;
    }

    private String getFormattedQuery() {
        if (query == null) {
            return "Query in URL";
        }
        return query;
    }

    private String getQuery() {
        return query;
    }

    public String getUrl() {
        return baseUrl + (query == null ? "" : getQuery());
    }

    public String getSource() {
        if (baseUrl == null) {
            return null;
        }
        if (query == null) {
            return baseUrl;
        }

        Matcher m = MULTIPLE_URLS.matcher(baseUrl);
        if (m.matches()) {
            return m.group(2) + " " + getFormattedQuery();
        }
        return getUrl();
    }

}
