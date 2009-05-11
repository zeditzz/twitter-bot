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

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getQuery() {
        return query;
    }

    public String getUrl() {
        return baseUrl + query;
    }

    public String getSource() {
        if (baseUrl == null) {
            return null;
        }
        Matcher m = MULTIPLE_URLS.matcher(baseUrl);
        if (m.matches()) {
            return m.group(2) + " " + query;
        }
        return getUrl();
    }

}
