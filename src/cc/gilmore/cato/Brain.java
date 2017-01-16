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

	private final String CALL_WEBSERVICE_TAG = "call webservice";
	private final String CALL_WEB_SERVICE_TAG = "call web service";
	
	private HashMap<String, String> learnedResponses = null;
	private boolean waitingForResponse = false;
	private boolean spokenResponse = false;
	
	
	public String getResponseFor(String input) {
		
		if(input == null || input.length() == 0) {
			return "What did you say?";
		}
		
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
			String webService = "";
			if(response.startsWith(CALL_WEBSERVICE_TAG)) {
				webService = response.replace(CALL_WEBSERVICE_TAG, "");
			}
			if(response.startsWith(CALL_WEB_SERVICE_TAG)) {
				webService = response.replace(CALL_WEB_SERVICE_TAG, "");
			}
			if(webService.length() > 0 ) {
				BrainOnlineRead bor = new BrainOnlineRead();
				bor.execute(webService);
				response = bor.getResponse();
			}
		}
		
		if(response == null) {
			waitingForResponse = true;
			return "How should I respond to " + input;
		}
		
		return response;
	}
	
	public boolean isWaitingForAResponse() {
		return waitingForResponse;
	}
	
	public void stopWaiting() {
		waitingForResponse = false;
	}
	
	public boolean saveResponseToInput(String input, String response) {
		if(learnedResponses == null) {
			learnedResponses = new HashMap<String, String>();
		}
		
		learnedResponses.put(input,  response);
		
		BrainOnlineWrite bow = new BrainOnlineWrite();
		bow.execute(input, response);
		
		stopWaiting();
		
		return bow.writeSucceeded();
	}

	public boolean isResponseSpoken() {
		return spokenResponse;
	}

	public void syncWithServer() {
		if(learnedResponses == null) {
			learnedResponses = new HashMap<String, String>();
		}

		try {
			BrainOnlineRead bor = new BrainOnlineRead();
			bor.execute();
			String memory = bor.getResponse();
			HashMap<String, String> memoryMap = convertMemoryToHashMap(memory);
			learnedResponses.putAll(memoryMap);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, String> convertMemoryToHashMap(String memory) {
		HashMap<String, String> memoryHashMap = new HashMap<String, String>();
		
		if(memory ==  null || memory.length() == 0) {
			return memoryHashMap;
		}
		
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
