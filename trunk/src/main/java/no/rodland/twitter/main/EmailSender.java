package no.rodland.twitter.main;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Just a simple interface for sending emails.
 *
 * @author fmr
 * @since 20110317 08:33
 */
public class EmailSender {

    private final String smtpUser;
    private final String smtpPassword;
    private final String smtpHost;
    private final String from;
    public static final boolean DEBUG = false;

    public EmailSender(String smtpUser, String smtpPassword, String smtpHost, String from) {
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
        this.smtpHost = smtpHost;
        this.from = from;
    }

    public void send(String subject, String message, String... recipients) throws MessagingException {

        //Set the host smtp address
        Properties props = new Properties();
        //props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "false");

        if (useAuth()) {
            props.put("mail.user", smtpUser);
            props.put("mail.smtp.password", smtpPassword);
            props.put("mail.smtp.auth", "true");
        }

        Session session = Session.getDefaultInstance(props);
        session.setDebug(DEBUG);

        // create a message
        Message msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setContent(message, "text/plain");

        if (useAuth()) {
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpHost, smtpUser, smtpPassword);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
        }
        else {
            Transport.send(msg);
        }
    }

    private boolean useAuth() {
        return smtpUser != null && smtpPassword != null;
    }
}
