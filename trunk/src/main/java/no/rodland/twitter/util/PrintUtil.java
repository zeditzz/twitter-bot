package no.rodland.twitter.util;

import twitter4j.User;
import no.rodland.twitter.Posting;

import java.util.List;


public class PrintUtil {
    public static String print(User user) {
        return "User{" +
                "id=" + user.getId() +
                ", name='" + user.getName() + '\'' +
                ", screenName='" + user.getScreenName() + '\'' +
                ", location='" + user.getLocation() + '\'' +
                ", description='" + user.getDescription() + '\'' +
                ", profileImageUrl='" + user.getProfileImageURL() + '\'' +
                ", url='" + user.getURL() + '\'' +
                ", isProtected=" + user.isProtected() +
                ", followersCount=" + user.getFollowersCount() +
                ", statusCreatedAt=" + user.getStatusCreatedAt() +
                ", statusId=" + user.getStatusId() +
                ", statusText='" + user.getStatusText() + '\'' +
                ", statusSource='" + user.getStatusSource() + '\'' +
                ", statusInReplyToStatusId=" + user.getStatusInReplyToStatusId() +
                ", statusInReplyToUserId=" + user.getStatusInReplyToUserId() +
                ", statusInReplyToScreenName='" + user.getStatusInReplyToScreenName() + '\'' +
                '}';
    }

    public static void printPostings(List<Posting> postings) {
        for (Posting posting : postings) {
            System.out.println(posting);
        }
    }
}
