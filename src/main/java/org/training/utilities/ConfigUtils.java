package org.training.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ConfigUtils {

	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConfigUtils.class);

	private String playerPrefix = "players.";

	private final ApplicationContext context;

	@Autowired
	public ConfigUtils(final ApplicationContext context) {
		this.context = context;
	}

	public String[] getAllAvailablePlayer() {
		try {
			List<String> players = new ArrayList<>();
			Properties prop = getPlayerProperties();
			for (Object k : prop.keySet()) {
				String key = (String) k;
				if (key.contains(playerPrefix)) {
					if (prop.getProperty(key).equals("oui")) {
						players.add(key.replace(playerPrefix, ""));
					}
				}
			}
			return players.toArray(new String[players.size()]);
		} catch (IOException e) {
			logger.error(e.getMessage());
			return new String[] { "andrea.wauthia", "daniel.nogueira", "xavier.koppe" };
		}

	}

	public String getPlayerConfiguration() {
		try {
			String result = "";
			Properties prop = getPlayerProperties();
			for (Object k : prop.keySet()) {
				String key = (String) k;
				if (key.contains(playerPrefix)) {
					result += key.replace(playerPrefix, "") + ": " + prop.getProperty(key)+"<br>";

				}
			}
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return "Error";
		}
	}

	/**
	 * Add or modify player
	 * 
	 * @param fullname
	 * @param value
	 */
	public void modifyPlayer(String fullname, String value) {
		try {
			Properties prop = getPlayerProperties();
			String properyName = playerPrefix + fullname;
			if ("true".equals(value)||"yes".equals(value)){
				value = "oui";
			}
			if ("false".equals(value)||"false".equals(value)){
				value = "oui";
			}
			prop.setProperty(properyName, value);
			Store(prop);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void removePlayer(String fullname) {
		try {
			Properties prop = getPlayerProperties();
			String properyName = playerPrefix + fullname;
			
			prop.remove(properyName);
			Store(prop);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void createDoodleScreenShot(byte[] content) throws IOException, MessagingException{
		Resource resource = context.getResource("classpath:temp/");
		Path filepath = Paths.get(resource.getFile().getAbsolutePath(), "doodleScreenshot.txt");
		File file = new File(filepath.toString());
		if (file.exists()) {
			file.delete();
		}
		Files.write(filepath, content, StandardOpenOption.CREATE_NEW);
	}
	
	public String getDoodleScreenShot() throws IOException{
		Resource resource = context.getResource("classpath:temp/");
		Path filepath = Paths.get(resource.getFile().getAbsolutePath(), "doodleScreenshot.txt");
		byte[] encoded = Files.readAllBytes(Paths.get(filepath.toString()));
		  return new String(encoded);
	}
	
	private void Store(Properties prop) throws FileNotFoundException, IOException{
		Resource resource = context.getResource("classpath:config/players.properties");
		FileOutputStream out = new FileOutputStream(resource.getFile());
		prop.store(out, null);
	}

	private Properties getPlayerProperties() throws IOException {
		Properties prop = new Properties();
		Resource resource = context.getResource("classpath:config/players.properties");
		InputStream stream = resource.getInputStream();
		prop.load(stream);

		return prop;
	}
}
