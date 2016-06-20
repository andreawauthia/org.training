package org.training.task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.hornetq.utils.json.JSONObject;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.training.utilities.ConfigUtils;
import org.training.utilities.GmailUtils;
import org.training.utilities.HttpUtils;
import org.training.utilities.MailUtils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import lombok.Getter;
import lombok.Setter;

@Component
public class FillDoodleTask implements DisposableBean {

	final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(FillDoodleTask.class);

	@Getter
	@Setter
	private static Lock lock;

	@PostConstruct
	private void init() {
		setLock(new ReentrantLock());
	}

	@Getter
	@Setter
	private boolean isEnabled = true;

	private final GmailUtils gmailUtils;

	private final HttpUtils httpUtils;

	private final MailUtils mailUtils;

	private final ConfigUtils configUtils;
	

	@Autowired
	public FillDoodleTask(final GmailUtils gmailUtils, final HttpUtils httpUtils, final MailUtils mailUtils,
			final ConfigUtils configUtils) {
		this.gmailUtils = gmailUtils;
		this.httpUtils = httpUtils;
		this.mailUtils = mailUtils;
		this.configUtils = configUtils;
		try {
			gmailUtils.sendMessage("doodle task initialized",null);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Scheduled(fixedDelay = 5000)
	public void FillFutsalDoodle() throws Exception {
		if (isEnabled()) {
			lock.lock();
			try {
				logger.info("Begin");
				String[] participantsName = configUtils.getAllAvailablePlayer();
				String user = "me";
				String mailQuery = "in:inbox is:unread subject:Futsal";
				String doodleinvitationbegin = "https://doodle.com/poll/";
				String doodleinvitationend = "?tmail=poll_invitecontact_participant_invitation";
				String doodleTab = "http://doodle.com/np/data?timeZoneClient=Europe%2FBerlin&token=&locale=fr_FR&pollId=";

				// Build a new authorized API client service.
				Gmail service = gmailUtils.getGmailService();
				List<Message> futsalMessages = ReadFutsalMessages(service, user, mailQuery);

				if (futsalMessages != null && !futsalMessages.isEmpty()) {

					for (Message message : futsalMessages) {

						Message fullmessage = service.users().messages().get(user, message.getThreadId())
								.setFormat("raw").execute();
						byte[] messagebytes = Base64.decodeBase64(fullmessage.getRaw());

						Properties props = new Properties();
						Session session = Session.getDefaultInstance(props, null);

						MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(messagebytes));
						String mailcontent = mailUtils.ReadMime(email);
						if (mailcontent != null && !mailcontent.isEmpty()) {
							if (mailcontent.indexOf(doodleinvitationend) > -1) {
								logger.info("Message found!");
								synchronized (FillDoodleTask.class) {
									String pollId = mailcontent.substring(
											mailcontent.indexOf(doodleinvitationbegin) + doodleinvitationbegin.length(),
											mailcontent.indexOf(doodleinvitationend));
									addPlayers(pollId, participantsName, "y");
									gmailUtils.modifyThread(service, user, message.getThreadId(),
											java.util.Arrays.asList("UNREAD"));
									
									String url = doodleTab+ pollId;
									byte[] content = mailUtils.getListOfRegisteredPlayer(url).getBytes(StandardCharsets.UTF_8);
									configUtils.createDoodleScreenShot(content);
									gmailUtils.sendMessage("Insertion successfully performed",null);
									setEnabled(false);
									break;
								}
							}
						}
					}
					logger.info("No message found1");
				} else {
					logger.info("No message found2");
				}
			} finally {
				lock.unlock();
			}
		}
	}

	private List<Message> ReadFutsalMessages(Gmail service, String user, String mailQuery) throws IOException {
		// Print the labels in the user's account.

		ListMessagesResponse response = service.users().messages().list(user).setQ(mailQuery).execute();

		return response.getMessages();
	}

	private void addPlayers(String pollId, String[] participantsName, String preference) throws Exception {
		logger.debug("Beginning of doodle insertion");

		StringBuilder doodleTableUrl = new StringBuilder().append("http://doodle.com/poll/").append(pollId)
				.append("#table");
		StringBuilder doodleUrl = new StringBuilder().append("http://doodle.com/np/new-polls/").append(pollId)
				.append("/participants");
		String hashOption = "";
		hashOption = mailUtils.ReadHashOption(doodleTableUrl.toString());
		;
		if (hashOption != null && !hashOption.isEmpty()) {
			for (String participant : participantsName) {

				String parameters = "name=" + participant + "&preferences=" + preference
						+ "&shownCalendars=&optionsHash=" + hashOption
						+ "&onCalendarView=false&token=&locale=en_LU&adminKey=&targetCalendarId=";

				String returned = httpUtils.excutePost(doodleUrl.toString(), parameters,
						"application/x-www-form-urlencoded");
				if (returned.equals("409") && preference.equals("y")) {
					addPlayers(pollId, new String[] { participant }, "n");
					if (participant.equals(participantsName[participantsName.length - 1])) {
						break;
					}
				}
				if (returned.equals("409") && preference.equals("n")) {
					throw new HttpClientErrorException(HttpStatus.values()[Integer.valueOf(returned)]);
				}
				JSONObject o = new JSONObject(returned);
				String participantId = o.getString("id");

				StringBuilder getUrl = new StringBuilder("http://doodle.com/polls/notifications?participantId=")
						.append(participantId).append("&pollId=").append(pollId);

				int response = httpUtils.executeGet(getUrl.toString());

				if (response > 400) {
					throw new HttpClientErrorException(HttpStatus.values()[response]);
				}
			}
		}
		logger.debug("End of doodle insertion");
	}

	@Override
	public void destroy() throws Exception {
		try {
			gmailUtils.sendMessage("doodle task destroyed",null);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
