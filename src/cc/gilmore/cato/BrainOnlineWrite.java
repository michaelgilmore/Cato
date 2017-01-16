package cc.gilmore.cato;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;

public class BrainOnlineWrite extends AsyncTask<String, Void, Void>{

	private boolean responseWritten = false;
	
	public boolean writeSucceeded() {
		return responseWritten;
	}
	
    @Override
    protected Void doInBackground(String... arg0) {
    	String input = arg0[0]; 
    	String response = arg0[1];
    	
    	//initialize
    	responseWritten = false;

    	saveResponseOnline(input, response);
        
    	return null;
    }

	private void saveResponseOnline(String input, String response) {
		String urlString = "http://gilmore.cc/services/remember_response.py?request=" + input + "&response=" + response;
		URL url = null;
        responseWritten = false;
        
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			System.out.println("Failed to create new URL for " + urlString);
			url = null;
		}
		if(url == null) {
			System.out.println("Failed to create URL object " + urlString);
			return;
		}
		
		try {
//	        url.openStream();
	        HttpGet httpGet = new HttpGet(urlString);
	        responseWritten = true;
		} catch (IOException e) {
			System.out.println("Failed to open URL stream " + urlString);
		}
	}
}
