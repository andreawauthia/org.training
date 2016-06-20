package org.training.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DoodleTaskActivator {

	private final FillDoodleTask doodleTask;
	
	@Autowired
	public DoodleTaskActivator(final FillDoodleTask doodleTask){
		this.doodleTask = doodleTask;
	}
	
	@Scheduled(cron = "0 0 23 * * SUN")
	public void ActivateTask(){
		doodleTask.setEnabled(true);
	}
}
