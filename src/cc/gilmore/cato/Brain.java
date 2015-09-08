package cc.gilmore.cato;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Brain {
	
	public static final String MY_NAME = "Cato";
	public static final String MY_PHONETIC_NAME = "Kayto";

	private final String CALL_WEB_SERVICE_TAG = "call webservice";
	
	private HashMap<String, String> learnedResponses = null;
	private boolean waitingForResponse = false;
	private boolean spokenResponse = false;
	
	
	public String getResponseFor(String input) {
		
		String response = null;
		spokenResponse = true;
		
		if(input.equals(MY_NAME)) {
			response = "Yes?";
		}
		else if(input.contains("what is your name") || input.contains("who are you")) {
			response = "My name is " + MY_NAME;
		}
		else if(input.contains("what can you do")) {
			response = "I can learn. Ask me a question. If I don't know the answer I'll ask you. Then next time I am asked I will know the answer.";
		}
		//search learned responses
		else if(learnedResponses != null && learnedResponses.containsKey(input)) {
			response = learnedResponses.get(input);
		}
		else if(input.equals("cancel") || input.equals("never mind") || input.equals("nothing") || input.equals("forget about it") || input.equals("i wasn't talking to you") || input.equals("go back to sleep")) {
			return "";
		}
		
		//handle all web service calls here
		if(response != null) {
			if(response.startsWith(CALL_WEB_SERVICE_TAG)) {
				String webService = response.replace(CALL_WEB_SERVICE_TAG, "");
				response = callWebService(webService);
			}
		}
		
		if(response == null) {
			waitingForResponse = true;
			return "How should I respond to " + input;
		}
		
		return response;
	}
	
	private String callWebService(String webService) {
		String response = "";
		String wsURLName = webService.trim().replace(" ",  "_");
		String urlString = "http://gilmore.cc/services/" + wsURLName + ".py";
		
		URL url = null;
        InputStream is = null;
        
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			response = "Failed to read service URL " + urlString;
		}
		if(url == null) {
			return "Failed to create URL object " + urlString;
		}
		
		try {
	        is = url.openStream();
		} catch (IOException e) {
			response = "Failed to open URL stream " + urlString;
		}
		if(is == null) {
			return "Failed to create InputStream object " + urlString;
		}
		
		try {
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        String line;
	        while ((line = br.readLine()) != null) {
	        	response = response + line;
	        }
		} catch (IOException e) {
			e.printStackTrace();
			response = "Failed to read input stream " + urlString;
		}
		return response;
	}

	public boolean isWaitingForAResponse() {
		return waitingForResponse;
	}
	
	public void stopWaiting() {
		waitingForResponse = false;
	}
	
	public void saveResponseToInput(String input, String response) {
		if(learnedResponses == null) {
			learnedResponses = new HashMap<String, String>();
		}
		
		learnedResponses.put(input,  response);
		saveResponseOnline(input, response);
		
		stopWaiting();
	}

	private void saveResponseOnline(String input, String response) {
		String urlString = "http://gilmore.cc/services/remember_response.py?request=" + input + "&response=" + response;
		
		URL url = null;
        
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			System.out.println("Failed to create new URL for " + urlString);
		}
		if(url == null) {
			System.out.println("Failed to create URL object " + urlString);
		}
		
		try {
	        url.openStream();
		} catch (IOException e) {
			System.out.println("Failed to open URL stream " + urlString);
		}
	}

	public boolean isResponseSpoken() {
		return spokenResponse;
	}

	public void syncWithServer() {
		String memory = callWebService("get_memory");
		HashMap<String, String> memoryMap = convertMemoryToHashMap(memory);
		if(learnedResponses == null) {
			learnedResponses = new HashMap<String, String>();
		}
		learnedResponses.putAll(memoryMap);
	}

	private HashMap<String, String> convertMemoryToHashMap(String memory) {
		HashMap<String, String> memoryHashMap = new HashMap<String, String>();
		
		//20150106 Current memory format ('request1', 'response1')('request2', 'response2')
		Pattern pattern = Pattern.compile("\\('(.+?)',\\s*'(.+?)'\\)");
		Matcher matcher = pattern.matcher(memory);

		String request;
		String response;
		
		while (matcher.find()) {
			request = matcher.group(1);
			response = matcher.group(2);
			memoryHashMap.put(request, response);
	    }
		
		return memoryHashMap;
	}
}
