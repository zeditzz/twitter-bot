package no.rodland.twitter.util;

import twitter4j.User;
import no.rodland.twitter.Posting;

import java.util.List;


@SuppressWarnings({"UnusedDeclaration"})
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
                ", statusCreatedAt=" + user.getStatus().getCreatedAt() +
                ", statusId=" + user.getStatus().getId() +
                ", statusText='" + user.getStatus().getText()+ '\'' +
                ", statusSource='" + user.getStatus().getSource()+ '\'' +
                ", statusInReplyToStatusId=" + user.getStatus().getInReplyToStatusId() +
                ", statusInReplyToUserId=" + user.getStatus().getInReplyToUserId() +
                ", statusInReplyToScreenName='" + user.getStatus().getInReplyToScreenName() + '\'' +
                '}';
    }

    public static void printPostings(List<Posting> postings) {
        for (Posting posting : postings) {
            System.out.println(posting);
        }
    }
}
