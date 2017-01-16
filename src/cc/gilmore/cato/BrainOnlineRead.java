package cc.gilmore.cato;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

//https://developer.android.com/reference/android/os/AsyncTask.html
public class BrainOnlineRead extends AsyncTask<String, Void, Void>{

	private String response;
	
	public String getResponse() {
		return response;
	}
	
    @Override
    protected Void doInBackground(String... arg0) {
    	if(arg0.length == 0) {
    		response = callWebService("get_memory");
    	}
    	else {
    		response = callWebService(arg0[0]);
    	}
        return null;
    }

	private String callWebService(String webService) {
		String response = "";
		String wsURLName = webService.trim().replace(" ",  "_");
		String urlString = "http://gilmore.cc/services/" + wsURLName + ".py";
		
		URL url = null;
        
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			response = "Failed to read service URL " + urlString;
		}
		if(url == null) {
			return "Failed to create URL object " + urlString;
		}
		
		InputStream is = null;

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
}
