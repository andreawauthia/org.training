package org.training.webservices;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.training.utilities.ConfigUtils;

@RestController
@RequestMapping("/DoodleInfos")
public class DoodleInfos {

	private ConfigUtils configUtils;
	
	@Autowired
	public DoodleInfos(final ConfigUtils configUtils){
		this.configUtils = configUtils;
	}
	
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public @ResponseBody String getLastDoodle(){
		try {
			return configUtils.getDoodleScreenShot();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	
}
