package cc.gilmore.cato;

import java.util.ArrayList;
import java.util.Date;

import cc.gilmore.cato.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener, OnInitListener {

	private Brain brain = new Brain();
	private TextToSpeech tts;
	private String previousInput;
	
	protected static final int VOICE_RECOGNITION_EVENT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.button1).setOnClickListener(this);

		Intent checkIntent = new Intent(); 
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA); 
		startActivityForResult(checkIntent, RESULT_OK);

		tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);
	}

	@Override
	public void onInit(int status) {
		brain.syncWithServer();
		
		/*
		 * A proactive greeting is not working out. It is tripping on itself at startup.
		 *
		if (status == TextToSpeech.SUCCESS) {
			say("yes?");
			listen();
		}
		else if (status == TextToSpeech.ERROR) {
			System.out.println("Sorry! Text To Speech failed...");
		}
		*/
	}

	@Override
	public void onStart() {
		super.onStart();
		say("yes?");
		listen();
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
		try {
			startActivityForResult(i, VOICE_RECOGNITION_EVENT);
		} catch (Exception e) {
			Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode==VOICE_RECOGNITION_EVENT  && resultCode==RESULT_OK) {
			ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			String whatSheSaid = thingsYouSaid.get(0);

			if(brain.isWaitingForAResponse()) {
				if(previousInput == null) {
					say("I just spaced for a minute");
					brain.stopWaiting();
				}
				else {
					brain.saveResponseToInput(previousInput, whatSheSaid);
					previousInput = null;
				}
			}
			else {
				String response = brain.getResponseFor(whatSheSaid);
				if(response == null || response.equals("")) {
					//do nothing
				}
				else {
					String screenResponse = response;
					
					writeToScreen(screenResponse);
	
					if(brain.isResponseSpoken()) {
						String whatToSayBack = response.replace(Brain.MY_NAME, Brain.MY_PHONETIC_NAME);
						say(whatToSayBack);
					}
					
					if(brain.isWaitingForAResponse()) {
						previousInput = whatSheSaid;
						listen();
					}
				}
			}
		}
	}

	private void writeToScreen(String screenResponse) {
		((TextView)findViewById(R.id.text1)).setText(screenResponse);
	}

	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
	
	private void say(String msg) {
		int retVal = tts.speak(msg, TextToSpeech.QUEUE_ADD, null);
		if(retVal != TextToSpeech.SUCCESS) {
			String errMsg = "ERROR: speak() returned " + retVal;
			writeToScreen(errMsg);
			System.out.println(errMsg);
		}
	}

	private void listen() {
		
		final long delay = 500;//ms empirically determined 20141230
		final long threshold = 5000;//ms
		long totalWait = 0;
		
		while(tts.isSpeaking() && totalWait < threshold) {
			//Need to let any prompts finish before starting to listen.
			//It doesn't filter itself out.
			try {
	            Thread.sleep(delay);
	            totalWait += delay;
	            System.out.println("Waiting to listen...");
	        } catch (InterruptedException e) {
	        	//do nothing for now...
	            System.out.println("Failing to wait");
	        }
		}

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());

        // Given an hint to the recognizer about what the user is going to say
        //There are two form of language model available
        //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
        //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

        //Start the Voice recognizer activity for the result.
        startActivityForResult(intent, VOICE_RECOGNITION_EVENT);
    }
}
