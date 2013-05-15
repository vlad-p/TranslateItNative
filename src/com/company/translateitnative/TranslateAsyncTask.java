package com.company.translateitnative;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TranslateAsyncTask extends AsyncTask<String, String, String> {
	
	private Context mainActivityContext;
	private WeakReference<View> mainAppWindowReference;
	private String translationType;
	
	public TranslateAsyncTask(Context context, View v, String typeOfTranslation) {
		mainAppWindowReference = new WeakReference<View>(v);
		mainActivityContext = context;
		translationType = typeOfTranslation;
	}
	
	@Override
	protected String doInBackground(String... uri) {
		StrictMode.ThreadPolicy policy = new StrictMode.
            ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
                
		URL requestURL;
		HttpURLConnection urlConnection = null;
		String responseString = null;
		
		try {
			requestURL = new URL(uri[0]);
			urlConnection = (HttpURLConnection) requestURL.openConnection();
			InputStream response = new BufferedInputStream(urlConnection.getInputStream());
			responseString = convertStreamToString(response);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			urlConnection.disconnect();
		}
		
		return responseString;
	}
	
	@Override
	protected void onPreExecute() {
		if (mainAppWindowReference != null) {
    		final View mainAppWindow = mainAppWindowReference.get();
    		if (translationType == "phrases") {
    			mainAppWindow.findViewById(R.id.getPhrasesProgress).setVisibility(View.VISIBLE);
    		} else if (translationType == "words") {
    			mainAppWindow.findViewById(R.id.getWordsProgress).setVisibility(View.VISIBLE);
    		}
		}
	}
	
	@Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        
        List<String> results = new ArrayList<String>();
        
        if (translationType == "phrases") {
	        try {
				JSONObject translationsObject = new JSONObject(result);
				JSONArray translations = translationsObject.getJSONArray("text");
				results.add(translations.getString(0));
			} catch (JSONException e) {
			    e.printStackTrace();
			}
        } else if (translationType == "words") {
        	try {
				JSONObject translationsObject = new JSONObject(result);
				JSONArray translations = translationsObject.getJSONArray("Translations");
				
				for (int i = 0; i < translations.length(); i++) {
					JSONObject translation = translations.getJSONObject(i);
					results.add(translation.getString("TranslatedText"));
				}
			} catch (JSONException e) {
			    e.printStackTrace();
			}
        }
        
        if (! results.isEmpty()) {
        	if (mainAppWindowReference != null) {
        		final View mainAppWindow = mainAppWindowReference.get();
        		ListView translationsList = null;
        		
        		if (translationType == "phrases") {
        			translationsList = (ListView) mainAppWindow.findViewById(R.id.resultsPhrases);
    	        } else if (translationType == "words") {
    	        	translationsList = (ListView) mainAppWindow.findViewById(R.id.resultsWords);
    	        }
        		
	        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivityContext, android.R.layout.simple_list_item_1, results);
				translationsList.setAdapter(adapter);
	        }
		} else {
			CharSequence msg = "No translations found.";
			int duration = Toast.LENGTH_SHORT;
			
			Toast displayMessage = Toast.makeText(mainActivityContext, msg, duration);
			displayMessage.show();
		}
        
        if (mainAppWindowReference != null) {
        	final View mainAppWindow = mainAppWindowReference.get();
	        if (translationType == "phrases") {
	        	mainAppWindow.findViewById(R.id.getPhrasesProgress).setVisibility(View.GONE);
	        } else if (translationType == "words") {
	        	mainAppWindow.findViewById(R.id.getWordsProgress).setVisibility(View.GONE);
	        }
        }
    }
	
	protected String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}

