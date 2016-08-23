package org.training.webservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.training.utilities.ConfigUtils;

@RestController
@RequestMapping("/ConfigurePlayers")
public class ConfigurePlayers {

	private ConfigUtils configUtils;
	
	@Autowired
	public ConfigurePlayers(final ConfigUtils configUtils) {
		this.configUtils = configUtils;
	}
	
	@RequestMapping(value = "/getPlayer", method = RequestMethod.GET)
	public @ResponseBody String ReadPlayerConfiguration(){
		return configUtils.getPlayerConfiguration();
	}
	
	@RequestMapping(value = "/add/{fullname}/{value}", method = RequestMethod.GET)
	public @ResponseBody String AddPlayer(@PathVariable String fullname, @PathVariable String value){
		configUtils.modifyPlayer(fullname, value);
		return ReadPlayerConfiguration();
	}
	
	@RequestMapping(value = "/remove/{fullname}", method = RequestMethod.GET)
	public @ResponseBody String rmPlayer(@PathVariable String fullname){
		configUtils.removePlayer(fullname);
		return ReadPlayerConfiguration();
	}
}
