package no.rodland.twitter.util;

/**
 * Created by IntelliJ IDEA.
 *
 * @author fmr
 * @since Oct 28, 2010 1:38:58 PM
 */
public class OAuth {

    private final String consumerKey;
    private final String consumerKeySecret;
    private final String accessKey;
    private final String accessKeySecret;

    public OAuth(String consumerKey, String consumerKeySecret) {
        this(consumerKey, consumerKeySecret, null, null);
    }

    public OAuth(String consumerKey, String consumerKeySecret, String accessKey, String accessKeySecret) {
        this.consumerKey = consumerKey;
        this.consumerKeySecret = consumerKeySecret;
        this.accessKey = accessKey;
        this.accessKeySecret = accessKeySecret;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerKeySecret() {
        return consumerKeySecret;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public boolean isAnon() {
        return accessKey == null;
    }
}

