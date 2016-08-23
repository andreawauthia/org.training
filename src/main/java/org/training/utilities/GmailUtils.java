package org.training.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyThreadRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class GmailUtils {

	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GmailUtils.class);

	private final ApplicationContext context;

	private final Environment environment;

	/** Directory to store user credentials for this application. */
	private java.io.File DATA_STORE_DIR;

	/** Global instance of the {@link FileDataStoreFactory}. */
	private FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private JsonFactory JSON_FACTORY;

	/** Global instance of the HTTP transport. */
	private HttpTransport HTTP_TRANSPORT;

	/** Application name. */
	private String APPLICATION_NAME;

	private List<String> SCOPES;

	@PostConstruct
	private void init() {
		try {
			JSON_FACTORY = JacksonFactory.getDefaultInstance();
			APPLICATION_NAME = "Futsal Cheat";
			DATA_STORE_DIR = new File(getClass().getResource("/config/gmail-java-fustalcheat.json").getFile());
			SCOPES = Arrays.asList(new String[] { GmailScopes.GMAIL_INSERT, GmailScopes.GMAIL_LABELS,
					GmailScopes.MAIL_GOOGLE_COM, GmailScopes.GMAIL_MODIFY, GmailScopes.GMAIL_READONLY });

			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			logger.fatal(t.toString());
			System.exit(1);
		}
	}

	@Autowired
	public GmailUtils(final ApplicationContext context, final org.springframework.core.env.Environment environment) {
		this.context = context;
		this.environment = environment;
	}

	/**
	 * Send an email
	 */
	public void sendMessage(String BodyText, MimeMultipart multiPart) throws MessagingException, IOException {
		Gmail service = getGmailService();
		Message message = createMessageWithEmail(BodyText, multiPart);

		message = service.users().messages().send("me", message).execute();
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public Credential authorize() throws IOException {
		logger.debug("get credentials");
		// Load client secrets.
		InputStream in = context.getResource("classpath:client/client_secret.json").getInputStream();
		if (in != null) {
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

			// Build flow and trigger user authorization request.
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
					clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
			Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
					.authorize("user");

			logger.debug("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
			return credential;
		} else {
			throw new IOException("secret not found");
		}
	}

	/**
	 * Build and return an authorized Gmail client service.
	 * 
	 * @return an authorized Gmail client service
	 * @throws IOException
	 */
	public Gmail getGmailService() throws IOException {
		Credential credential = authorize();
		return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}

	public void modifyThread(Gmail service, String userId, String threadId, List<String> labelsToRemove)
			throws IOException {

		ModifyThreadRequest mods = new ModifyThreadRequest().setRemoveLabelIds(labelsToRemove);
		service.users().threads().modify(userId, threadId, mods).execute();
	}

	/**
	 * Create a Message from an email
	 *
	 * @param email
	 *            Email to be set to raw of message
	 * @return Message containing base64url encoded email.
	 * @throws IOException
	 * @throws MessagingException
	 */
	private Message createMessageWithEmail(String bodyText, MimeMultipart multiPart)
			throws MessagingException, IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		createEmail(bodyText, multiPart).writeTo(bytes);
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	/**
	 * Create a MimeMessage using the parameters provided.
	 *
	 * @param to
	 *            Email address of the receiver.
	 * @param from
	 *            Email address of the sender, the mailbox account.
	 * @param subject
	 *            Subject of the email.
	 * @param bodyText
	 *            Body text of the email.
	 * @return MimeMessage to be used to send email.
	 * @throws MessagingException
	 */
	private MimeMessage createEmail(String bodyText, MimeMultipart multiPart) throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress("andrea.wauthia@gmail.com"));
		email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress("andrea.wauthia@gmail.com"));
		email.setSubject("FutsalCheat report");
		email.setText(bodyText, StandardCharsets.UTF_8.toString(), "html");
		if (multiPart != null) {
			email.setContent(multiPart);
		}
		return email;
	}
}