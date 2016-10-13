package com.example.adira;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender extends javax.mail.Authenticator {
	private String user;
	private String password;
	private Session session;
	static {
		Security.addProvider(new JSSEProvider());
	}

	public EmailSender(final String user, final String password) {
		this.user = user;
		this.password = password;

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");

//		session = Session.getInstance(props, new javax.mail.Authenticator(){
//			protected PasswordAuthentication getPasswordAuthentication(){
//				return new PasswordAuthentication(user, password);
//			}
//		});
		session = Session.getDefaultInstance(props,this);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}

	public synchronized boolean sendMail(String subject, String body,
			String sender, String recipients) throws Exception {
		boolean sent = true;
		try {
			MimeMessage message = new MimeMessage(session);
			DataHandler handler = new DataHandler(new ByteArrayDataSource(
					body.getBytes(), "text/html"));
			message.setSender(new InternetAddress(sender,"Adira"));
			message.setSubject(subject);
			message.setDataHandler(handler);
			if (recipients.indexOf(',') > 0)
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(recipients));
			else
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(recipients));
			Transport.send(message);
		} catch (Exception e) {
			sent = false;
			e.printStackTrace();
		}
		return sent;
	}
	public synchronized boolean sendMailwithAttachment(String subject, String body,
			String sender, String recipients,String filelocation,String filename) throws Exception {
		boolean sent = true;
		try {
			Multipart _multipart; 
			_multipart = new MimeMultipart(); 

			MimeMessage message = new MimeMessage(session);
			DataHandler handler = new DataHandler(new ByteArrayDataSource(
					body.getBytes(), "text/html"));
			message.setSender(new InternetAddress(sender,"Adira"));
			message.setSubject(subject);
			message.setDataHandler(handler);
			BodyPart messageBodyPart = new MimeBodyPart(); 
		    DataSource source = new FileDataSource(filelocation); 
		    messageBodyPart.setDataHandler(new DataHandler(source)); 
		    messageBodyPart.setFileName(filename); 
		    _multipart.addBodyPart(messageBodyPart);

		    BodyPart messageBodyPart2 = new MimeBodyPart(); 
		    messageBodyPart2.setText(subject); 

		    _multipart.addBodyPart(messageBodyPart2); 
		    message.setContent(_multipart);
			if (recipients.indexOf(',') > 0)
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(recipients));
			else
				message.setRecipient(Message.RecipientType.TO,
						new InternetAddress(recipients));
			
			Transport.send(message);
		} catch (Exception e) {
			sent = false;
			e.printStackTrace();
		}
		return sent;
	}
	public class ByteArrayDataSource implements DataSource {
		private byte[] data;
		private String type;

		public ByteArrayDataSource(byte[] data, String type) {
			super();
			this.data = data;
			this.type = type;
		}

		public ByteArrayDataSource(byte[] data) {
			super();
			this.data = data;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getContentType() {
			if (type == null)
				return "application/octet-stream";
			else
				return type;
		}

		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		public String getName() {
			return "ByteArrayDataSource";
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("Not Supported");
		}
	}
}
