package no.rodland.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 * Used to set up oauth.  nearly identical to example in: http://twitter4j.org/en/code-examples.html#oauth
 *
 * @author fmr
 * @since Oct 28, 2010 9:33:54 AM
 */
public class Auth {

    public static void main(String args[]) throws Exception {
        try {
            authorize(args[0], args[1]);
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            usage();
        }
    }

    private static void usage() {
        System.out.println("Usage: java Auth <consumer-key> <consumer-secret-key>");
    }

    public static void authorize(String consumerKey, String consumerSecretKey) throws TwitterException, IOException {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecretKey);

        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == accessToken) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
            String pin = br.readLine();
            try {
                if (pin.length() > 0) {
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                }
                else {
                    accessToken = twitter.getOAuthAccessToken();
                }
            }
            catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                }
                else {
                    te.printStackTrace();
                }
            }
        }
        storeAccessToken(twitter.verifyCredentials().getId(), accessToken);
    }

    private static void storeAccessToken(int useId, AccessToken accessToken) {
        System.out.println("token = " + accessToken.getToken());
        System.out.println("tokensecret = " + accessToken.getTokenSecret());
    }
}
