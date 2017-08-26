package org.training.webservices;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.training.task.DoodleTaskActivator;
import org.training.task.FillDoodleTask;
import org.training.utilities.ConfigUtils;

@RestController
@RequestMapping("/DoodleInfos")
public class DoodleInfos {

	private ConfigUtils configUtils;
	
	private final FillDoodleTask doodleTask;
	
	
	@Autowired
	public DoodleInfos(final ConfigUtils configUtils, final FillDoodleTask doodleTask){
		this.configUtils = configUtils;
		this.doodleTask = doodleTask;
	}
	
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public @ResponseBody String getLastDoodle(){
		try {
			return configUtils.getDoodleScreenShot();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	
	@RequestMapping(value = "/configure", method = RequestMethod.GET)
	public @ResponseBody void configureDoodle(@RequestParam boolean isEnabled){
			doodleTask.setEnabled(isEnabled);
	}
	
}
