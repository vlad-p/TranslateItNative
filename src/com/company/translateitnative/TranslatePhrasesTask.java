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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TranslatePhrasesTask extends AsyncTask<String, String, String> {
	
	private WeakReference<ListView> resultsListViewReference;
	private Context mainActivityContext;
	private String translationType;
	
	public TranslatePhrasesTask(Context context, ListView v, String typeOfTranslation) {
		resultsListViewReference = new WeakReference<ListView>(v);
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
        }
        
        if (! results.isEmpty()) {
        	if (resultsListViewReference != null) {
	        	final ListView translationsList = resultsListViewReference.get();
	        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivityContext, android.R.layout.simple_list_item_1, results);
				translationsList.setAdapter(adapter);
	        }
		} else {
			CharSequence msg = "No translations found.";
			int duration = Toast.LENGTH_SHORT;
			
			Toast displayMessage = Toast.makeText(mainActivityContext, msg, duration);
			displayMessage.show();
		}
    }
	
	protected String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}

