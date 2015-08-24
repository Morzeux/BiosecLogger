package com.morzeux.bioseclogger.logic;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.util.Log;
import biosecLogger.core.UserLoggerManager;

/**
 * Controller to sends email to specific address from specific gmail address.
 * Also with attachments.
 * 
 * @author Stefan Smihla
 * 
 */
public enum EmailSender {
	INSTANCE;

	private Session session = null;
	private String username = null;

	/**
	 * Returns instance of email sender.
	 * 
	 * @return instance of EmaiLSender class
	 */
	public static EmailSender getInstance() {
		return INSTANCE;
	}

	/**
	 * Connects to gmail smtp server.
	 * 
	 * @param username
	 *            gmail username
	 * @param password
	 *            gmail password
	 */
	public void setSession(final String username, final String password) {
		this.username = username;

		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");

		session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
	}

	/**
	 * Creates email message content
	 * 
	 * @param body
	 *            email body content
	 * @param attachment
	 *            path to attachment
	 * @return multipart email content
	 * @throws MessagingException
	 *             raises on error during message packing
	 */
	private Multipart createContent(String body, String attachment)
			throws MessagingException {
		Multipart multipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(new File(
				ExtendedStorageHandler.getExternalPath(attachment)));
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(attachment);
		multipart.addBodyPart(messageBodyPart);

		return multipart;
	}

	/**
	 * Sends email to receiver.
	 * 
	 * @param alias
	 *            source alias
	 * @param to
	 *            destination email address
	 * @param subject
	 *            email subject
	 * @param messageText
	 *            email body
	 * @param attachment
	 *            path to attachment
	 * @return true on success, otherwise false
	 */
	public boolean sendEmail(String alias, String to, String subject,
			String messageText, String attachment) {
		Message message = new MimeMessage(session);

		try {
			message.setFrom(new InternetAddress(username, alias));
			message.addRecipient(RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setContent(createContent(messageText, attachment));

			Transport.send(message);
			return true;
		} catch (UnsupportedEncodingException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		} catch (MessagingException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return false;
	}
}
