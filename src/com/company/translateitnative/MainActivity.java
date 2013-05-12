package com.company.translateitnative;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.view.MotionEvent;
import java.util.List;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
	private static final String DEBUG_TAG = "TranslateItNative"; 
	
	private int languageFrom;
	private int languageTo;
	
	private OnItemSelectedListener changeFromLanguage = new OnItemSelectedListener() {
		@Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        languageFrom = position;
//	        Log.i(DEBUG_TAG, "-" + languageFrom + "-");
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> parentView) {
	        // tbd
	    } 
	};
	
	private OnItemSelectedListener changeToLanguage = new OnItemSelectedListener() {
		@Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        languageTo = position;
//	        Log.i(DEBUG_TAG, "-" + languageTo + "-");
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> parentView) {
	        // tbd
	    } 
	};
	
	private OnClickListener switchLanguages = new OnClickListener() {
		public void onClick(View v) {
			Spinner languageFromSpinner = (Spinner) findViewById(R.id.language_from);
			Spinner languageToSpinner = (Spinner) findViewById(R.id.language_to);
			
			languageFromSpinner.setSelection(languageTo);
			languageToSpinner.setSelection(languageFrom);
		}
	};
	
	private OnClickListener searchButtonClick = new OnClickListener() {
		public void onClick(View v) {
			translate();
		}
	};
	
	private TextView.OnEditorActionListener submitSearchQuery = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			/*if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) { 
				translate();
			}*/
			/*if (actionId == EditorInfo.IME_ACTION_NEXT) {
				translate();
			}*/
			Log.i(DEBUG_TAG, "-" + actionId + "-");
//			return true;
			if (   actionId == EditorInfo.IME_ACTION_SEARCH
				|| actionId == EditorInfo.IME_ACTION_DONE
				|| event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
		        
//				Log.i(DEBUG_TAG, "123");
				
				// the user is done typing. 
				translate();
				
				return true;               
		    }
			// pass on to other listeners
		    return false;
		}
	};
	
	private OnTabChangeListener switchTab = new OnTabChangeListener() {
		public void onTabChanged (String tabId) {
			List<String> empty = new ArrayList<String>();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, empty);
			
			if (tabId == "tabPhrases") {
				ListView resultPhrases = (ListView) findViewById(R.id.resultsPhrases);
				resultPhrases.setAdapter(adapter);
			} else if (tabId == "tabWords") {
				ListView resultWords = (ListView) findViewById(R.id.resultsWords);
				resultWords.setAdapter(adapter);
			}
			
			EditText searchQueryInput = (EditText) findViewById(R.id.search_query_input);
			searchQueryInput.setText("");
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Spinner languageFrom = (Spinner) findViewById(R.id.language_from);
		Spinner languageTo = (Spinner) findViewById(R.id.language_to);
		// default to english
		languageFrom.setSelection(1);
		languageFrom.setOnItemSelectedListener(changeFromLanguage);
		// default to russian
		languageTo.setSelection(5);
		languageTo.setOnItemSelectedListener(changeToLanguage);
		
		ImageButton switchLanguagesButton = (ImageButton) findViewById(R.id.switch_languages);
		switchLanguagesButton.setOnClickListener(switchLanguages);
		
		Button searchForTranslations = (Button) findViewById(R.id.search_button);
		searchForTranslations.setOnClickListener(searchButtonClick);
		
		EditText searchQueryInputField = (EditText) findViewById(R.id.search_query_input);
		searchQueryInputField.setOnEditorActionListener(submitSearchQuery);
		
		searchQueryInputField.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				v.requestFocusFromTouch();
				return false;
			}
		});
		
		// tabs functionality
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		
		TabSpec resultsWords = tabHost.newTabSpec("tabWords");
		resultsWords
			.setContent(R.id.tabWords)
			.setIndicator("Words");
		
		TabSpec resultsPhrases = tabHost.newTabSpec("tabPhrases");
		resultsPhrases
			.setContent(R.id.tabPhrases)
			.setIndicator("Phrases");
		
		tabHost.addTab(resultsWords);
		tabHost.addTab(resultsPhrases);
		
		tabHost.setOnTabChangedListener(switchTab);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void translate() {
//		ListView translationsList;
		
		EditText searchQueryInputField = (EditText) findViewById(R.id.search_query_input);
		String textToTranslate = searchQueryInputField.getText().toString();
		
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchQueryInputField.getWindowToken(), 0);
		
		Log.i(DEBUG_TAG, textToTranslate);
		
		String[] languageCodes = getResources().getStringArray(R.array.language_codes);
		String languageFromCode = languageCodes[languageFrom];
		String languageToCode = languageCodes[languageTo];
		
		Log.i(DEBUG_TAG, languageFromCode + " " + languageToCode);
		
//		List<String> results = new ArrayList<String>();
		
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		String currentTabTag = tabHost.getCurrentTabTag();
		
		Log.i(DEBUG_TAG, currentTabTag);
		
		if (currentTabTag == "tabWords") {
//			translationsList = (ListView) findViewById(R.id.resultsWords);
			
			String url = "http://translateit.hostei.com/ajax/test_translation_service.php";
			String charset = "UTF-8";			
			String query = null;
			
			try {
				query = String.format("languageFrom=%s&languageTo=%s&textToTranslate=%s&maxTranslations=%s", 
					 URLEncoder.encode(languageFromCode, charset), 
					 URLEncoder.encode(languageToCode, charset),
					 URLEncoder.encode(textToTranslate, charset),
					 URLEncoder.encode("10", charset));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			View mainAppWindow = (View) findViewById(R.id.main_app_window);
			TranslatePhrasesTask task = new TranslatePhrasesTask(this, mainAppWindow, "words");
			task.execute(url + "?" + query);
			
		} else if (currentTabTag == "tabPhrases") {
			String url = "https://translate.yandex.net/api/v1.5/tr.json/translate";
			String charset = "UTF-8";
			String key = "trnsl.1.1.20130502T083517Z.364dbfbe6df7c456.b87249b5fc8661ebddbcb8a09eea56c995f868a8";
			String lang = languageFromCode + "-" + languageToCode;
			String text = textToTranslate;
			String query = null;
			
			try {
				query = String.format("key=%s&lang=%s&text=%s", 
					 URLEncoder.encode(key, charset), 
					 URLEncoder.encode(lang, charset),
					 URLEncoder.encode(text, charset));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
//			translationsList = (ListView) findViewById(R.id.resultsPhrases);
			View mainAppWindow = (View) findViewById(R.id.main_app_window);
			TranslatePhrasesTask task = new TranslatePhrasesTask(this, mainAppWindow, "phrases");
			task.execute(url + "?" + query);
			
			/*translationsList = (ListView) findViewById(R.id.resultsPhrases);
			results = translatePhrase(textToTranslate, languageFromCode, languageToCode);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results);
			translationsList.setAdapter(adapter);
			
			if (results.isEmpty()) {
				Context appContext = getApplicationContext();
				CharSequence msg = "No translations found.";
				int duration = Toast.LENGTH_SHORT;
				
				Toast displayMessage = Toast.makeText(appContext, msg, duration);
				displayMessage.show();
			}*/
		}
	}
	
	public List<String> translateWord(String wordToTranslate) {
		return null;
		// tbd
	}
	
/*	public List<String> translatePhrase(String wordToTranslate, String languageFromCode, String languageToCode) {
		List<String> results = new ArrayList<String>();
		
		String url = "https://translate.yandex.net/api/v1.5/tr.json/translate";
		String charset = "UTF-8";
		String key = "trnsl.1.1.20130502T083517Z.364dbfbe6df7c456.b87249b5fc8661ebddbcb8a09eea56c995f868a8";
		String lang = languageFromCode + "-" + languageToCode;
		String text = wordToTranslate;
		
		try {
			StrictMode.ThreadPolicy policy = new StrictMode.
                    ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    
			String query = String.format("key=%s&lang=%s&text=%s", 
			     URLEncoder.encode(key, charset), 
			     URLEncoder.encode(lang, charset),
			     URLEncoder.encode(text, charset));
			
			URL requestURL = new URL(url + "?" + query);
			
			HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
			try {		
				InputStream response = new BufferedInputStream(urlConnection.getInputStream());
				String responseString = convertStreamToString(response);			
	//			Log.i(DEBUG_TAG, responseString);
				
				try {
					JSONObject translationsObject = new JSONObject(responseString);
					JSONArray translations = translationsObject.getJSONArray("text");
					results.add(translations.getString(0));
				} catch (JSONException e) {
				    e.printStackTrace();
				}
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				urlConnection.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}*/
	
	/*public void translate() {
		EditText searchQueryInputField = (EditText) findViewById(R.id.search_query_input);
		String word = searchQueryInputField.getText().toString();
		Log.i(DEBUG_TAG, word);
		
		String[] languageCodes = getResources().getStringArray(R.array.language_codes);
		String languageFromCode = languageCodes[languageFrom];
		String languageToCode = languageCodes[languageTo];
		
		Log.i(DEBUG_TAG, languageFromCode + " " + languageToCode);
		
		List<String> results = new ArrayList<String>();
		
		String globalDictionaryString = "{'en': {'ru': {'table': [{ 'type': 'word', 'translation': '�������' },{ 'type': 'word', 'translation': '����' },{ 'type': 'word', 'translation': '����������' },{ 'type': 'term', 'translation': '������' },{ 'type': 'term', 'translation': '��������' }],'browser': [{ 'type': 'term', 'translation': '�������' },{ 'type': 'term', 'translation': '��������� ���������' },{ 'type': 'word', 'translation': '����������, ��������������� ������' }],'checkbox': [{ 'type': 'word', 'translation': '������' },{ 'type': 'term', 'translation': '���-����' }]}},'ru': {'en': {'�������': [{ 'type': 'word', 'translation': 'table' },{ 'type': 'word', 'translation': 'chart' },{ 'type': 'word', 'translation': 'schedule' },{ 'type': 'term', 'translation': 'spreadsheet' },{ 'type': 'term', 'translation': 'array' }],'�������': [{ 'type': 'term', 'translation': 'browser' }],'������': [{ 'type': 'word', 'translation': 'flag' },{ 'type': 'term', 'translation': 'checkbox' }]}}}";
		try {
			JSONObject globalDictionaryObject = new JSONObject(globalDictionaryString);
			JSONObject requestedFromDictionary = globalDictionaryObject.getJSONObject(languageFromCode);
			JSONObject requestedToDictionary = requestedFromDictionary.getJSONObject(languageToCode);
			JSONArray translations = requestedToDictionary.getJSONArray(word);
			for (int i = 0; i < translations.length(); i++) {
				JSONObject translation = translations.getJSONObject(i);
				results.add(translation.getString("translation"));
			}
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results);		
		ListView translationsList = (ListView) findViewById(R.id.listView1);
		translationsList.setAdapter(adapter);
		
		if (results.isEmpty()) {
			Context appContext = getApplicationContext();
			CharSequence msg = "No translations found.";
			int duration = Toast.LENGTH_SHORT;
			
			Toast displayMessage = Toast.makeText(appContext, msg, duration);
			displayMessage.show();
		}
		
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchQueryInputField.getWindowToken(), 0);
	}*/
	
	public static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}