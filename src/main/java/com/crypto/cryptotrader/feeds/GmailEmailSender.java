package com.crypto.cryptotrader.feeds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

@Component
public class GmailEmailSender {
	public static final String SUBJECT = "New signal from AlanMasters - ";
	public static final String AUTHORIZED_USER = "me";

	@Autowired
	private Gmail gmailService;

	@Value("${email.to}")
	private String to;
	@Value("${email.from}")
	private String from;

	public void sendEmail(String emailContent, String creationTime, String imageUrl) throws IOException,
			MessagingException {
		Message message = createEmail(emailContent, creationTime, imageUrl);
		gmailService.users().messages().send(AUTHORIZED_USER, message).execute();
	}

	public Message createEmail(String bodyText, String creationTime, String imageUrl)
			throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(RecipientType.TO, new InternetAddress(to));
		email.setSubject(SUBJECT + creationTime);
		MimeMultipart multipart = new MimeMultipart("related");
		BodyPart messageBodyPart = new MimeBodyPart();
		String htmlText = "<div>"+ bodyText +"</div>";
		if (!StringUtils.isEmpty(imageUrl)) {
			htmlText = htmlText + "<img src=\"cid:image\">";
			messageBodyPart.setContent(htmlText, "text/html");
			multipart.addBodyPart(messageBodyPart);
			messageBodyPart = new MimeBodyPart();
			URL url = new URL(imageUrl);
			URLConnection openConnection = url.openConnection();
			openConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			openConnection.connect();
			DataSource discordImageDataSource = new ByteArrayDataSource(openConnection.getInputStream(), openConnection.getContentType());;

			messageBodyPart.setDataHandler(new DataHandler(discordImageDataSource));
			messageBodyPart.setHeader("Content-ID", "<image>");
		} else {
			messageBodyPart.setContent(htmlText, "text/html");
		}
		multipart.addBodyPart(messageBodyPart);
		email.setContent(multipart);

		return createMessageWithEmail(email);
	}

	public Message createMessageWithEmail(MimeMessage emailContent)
			throws MessagingException, IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

}
