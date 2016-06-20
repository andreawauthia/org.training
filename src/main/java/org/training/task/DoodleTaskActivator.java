package org.training.task;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.training.utilities.GmailUtils;

@Service
public class DoodleTaskActivator {

	private final FillDoodleTask doodleTask;
	
	private final GmailUtils gmailUtils;
	
	@Autowired
	public DoodleTaskActivator(final FillDoodleTask doodleTask, final GmailUtils gmailUtils){
		this.doodleTask = doodleTask;
		this.gmailUtils = gmailUtils;
	}
	
	@Scheduled(cron = "0 0 23 * * SUN")
	public void ActivateTask(){		
		try {
			gmailUtils.sendMessage("Activation doodle task", null);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doodleTask.setEnabled(true);
	}
}
