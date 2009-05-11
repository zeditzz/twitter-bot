/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package no.rodland.twitter;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import twitter4j.TwitterException;
import twitter4j.http.HttpClient;
import twitter4j.http.Response;

import java.util.*;

import org.apache.log4j.Logger;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * <p>
 * FeedMonitor is a simple feed monitoring application.<br>
 * FeedMonitor monitors specified feeds and reports newly posted entries to the specified Twitter account every 10 minutes.<br>
 * It is possible to specify multiple configuration files.<br>
 * Numeric parameter will be recognized as monitoring interverval in minutes.<br>
 * Usage: java twitter4j.examples.FeedMonitor [config_file_path ..] [interval(min)]<br>
 * <br>
 * If no configuration file path is specified, FeedMonitor will look for default configuration file name - &quot;feedmonitor.properties&quot;.<br>
 * The configuration file format is Java standard properties file format with following properties:<br>
 * feedurl : the feed URL you want to monitor<br>
 * id : Twitter id<br>
 * password : Twitter password<br>
 * <br>
 * <hr>
 * e.g. a sample properties for monitoring CSS latest news every 10 minutes
 * <pre style="border: solid 1px black;background-color:#AAF">
 * feedurl=http://rss.cnn.com/rss/cnn_latest.rss
 * id=YOUR_TWITTER_ID
 * password=YOUR_TWITTER_PASSWORD</pre>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class RSSRetriever {
    private static final Logger log = Logger.getLogger(RSSRetriever.class);
    private static final String HTTP_AGENT = "NetNewsWire/3.1.7 (Mac OS X; http://www.newsgator.com/Individuals/NetNewsWire/)";
    private final List<FeedUrl> feedurls;
    private final HttpClient http = new HttpClient();
    // private String fileName;
    private final Set<String> linkSet = new HashSet<String>();   // only posting 1 unique link pr session
    private final Set<String> titleSet = new HashSet<String>();  // only posting 1 unique title pr session

    public RSSRetriever(List<FeedUrl> feedUrls) {
        this.feedurls = feedUrls;
    }

    public List<Posting> retrieve() {
        List<Posting> entries = new ArrayList<Posting>();
        http.setUserAgent(HTTP_AGENT);
        for (FeedUrl feedurl : feedurls) {
            String source = feedurl.getSource();
            log.info("Checking feed from " + source);
            try {
                Response res = http.get(feedurl.getUrl());
                List myEntries = new SyndFeedInput().build(res.asDocument()).getEntries();
                int added = 0;
                for (Object myEntry : myEntries) {
                    Posting posting = new Posting((SyndEntry) myEntry, source);
                    String url = posting.getUrl();
                    String title = posting.getTitle();
                    if (titleSet.contains(title)) {
                        log.info("removed duplicate (title): " + title + " " + url);
                    } else {
                        titleSet.add(title);
                        if (linkSet.contains(url)) {
                            log.info("removed duplicate (url): " + title + " " + url);
                        } else {
                            added++;
                            linkSet.add(url);
                            entries.add(posting);
                        }
                    }
                }
                log.info(added + " entries added from " + source + " (dropped " + (myEntries.size() - added) + " dups)");

            }
            catch (TwitterException te) {
                log.info("Failed to fetch the feed:" + te.getMessage());
            }

            catch (FeedException fe) {
                log.info("Failed to parse the feed:" + fe.getMessage());
            }

        }
        return entries;
    }



}