package org.training.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import org.hornetq.utils.json.JSONArray;
import org.hornetq.utils.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class MailUtils {

	@Autowired
	private ApplicationContext context;

	public String ReadMime(MimeMessage m) throws IOException, MessagingException {
		Object contentObject = m.getContent();
		if (contentObject instanceof Multipart) {
			Multipart content = (Multipart) contentObject;
			int count = content.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart part = content.getBodyPart(i);
				if (part.isMimeType("text/plain")) {
					return (String) part.getContent();
				} else if (part.isMimeType("text/html")) {
					String html = (String) part.getContent();
					return Jsoup.parse(html).text();
				}
			}
		} else if (contentObject instanceof String) // a simple text message
		{
			return (String) contentObject;
		}
		return "";
	}

	public String ReadHashOption(String URL) throws Exception {
		String HashOption = "";
		URL oracle = new URL(URL);
		BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			if (inputLine.contains("optionsHash")) {
				String[] tab = inputLine.split(",");
				for (String str : tab) {
					if (str.contains("optionsHash")) {
						String[] strs = str.split(":");
						if (strs.length > 1) {
							HashOption = strs[1].replaceAll("\"", "");
						}
					}
				}
				break;
			}
		}
		in.close();
		return HashOption;
	}

	public String getListOfRegisteredPlayer(String URL) throws Exception{
		String htmlLines = "";
		URL oracle = new URL(URL);
		BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			htmlLines+=inputLine;
		}
		JSONObject json = new JSONObject(htmlLines);
		String result = "";
		JSONArray participants = json.getJSONObject("poll").getJSONArray("participants");
		for (int i = 0; i< participants.length(); i++){
			result +=participants.getJSONObject(i).getString("name")+"<br>";
		}
		return result;
	}

	public void deleteAttachedFile(String filename) throws IOException {
		Resource resource = context.getResource("classpath:temp/" + filename);
		if (resource != null) {
			resource.getFile().delete();
		}
	}
}
