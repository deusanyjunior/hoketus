package br.usp.ime.compmus.dj.webserver;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import br.usp.ime.compmus.utils.JSONfunctions;

import android.util.Log;

public class WebServerMethods {

	private URL webserver;
	private String model;
	private String[] params;
	
	private int lastID;
	
	public WebServerMethods(URL ws, String md, String[] ps) {
		setWebserver(ws);
		model = md;
		params = ps;
		lastID = 0;
	}
	
	public int getLastNoteFromWS() {
		int lastIdFromWS = -1;
		JSONObject results = JSONfunctions.getJSONfromURL(getWebserver()+".json");
		JSONObject resultsObject = null;
		
		try {
			if (results != null && !results.isNull(model)) {
				resultsObject = results.getJSONObject(model);
				if (resultsObject != null && !resultsObject.isNull("id")) {
					lastIdFromWS = resultsObject.getInt("id");					
				}
			} else {
				lastIdFromWS = -1;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			lastIdFromWS = -1;
		}
		Log.i("WebServerMethods", "getLastIdFromWS "+lastIdFromWS);
		return lastIdFromWS;
	}
	
	public boolean setLastIdFromWS() {
		boolean lastIdSetted = false;
		int lastNoteFromWS = -1;
		
		if ( (lastNoteFromWS = getLastNoteFromWS()) != -1 ) {
			Log.i("WebServerMethods", "setLastIdFromWS "+lastNoteFromWS);
			this.lastID = lastNoteFromWS;
			lastIdSetted = true;
		}

		return lastIdSetted;
	}
	
	public int getLastId() {
		Log.i("WebServerMethods", "getLastId "+lastID);
		return lastID;
	}
	
	public void setLastId(int note) {
		Log.i("WebServerMethods", "setLastId "+lastID);
		lastID = note;
	}
	
	public String[] getNextId() {
		String[] nextId = null;
		int id = -1;
		
		if (lastID != -1) {
			id = lastID+1;
			
			JSONObject resultsObject = null;
			JSONObject nextIdObject = null;
			resultsObject = JSONfunctions.getJSONfromURL(getWebserver()+"/"+id+".json");
			try {
				if (resultsObject != null && !resultsObject.isNull(model)) {
					nextIdObject = resultsObject.getJSONObject(model);
					if (nextIdObject != null && !nextIdObject.isNull("id")) {
						// set nextId
						nextId = new String[params.length];
						for(int i = 0; i < nextId.length; i++) {
							nextId[i] =  nextIdObject.get(params[i]).toString();
						}
//						nextId[0] = nextIdObject.get("created_at").toString();
//						nextId[1] = nextIdObject.getString("dbm");
						// update the last Id
						setLastId(id);
					} else {
						return null;
					}
				} else {
					return null;
				}
			} catch (JSONException e) {
				Log.i("WebServerMethods", "getNextId JSONException");
				e.printStackTrace();
				return null;
			}	
		}
		
		return nextId;
	}

	public URL getWebserver() {
		return webserver;
	}

	private void setWebserver(URL ws) {
		if(ws.toString().startsWith("http://") && !ws.toString().endsWith("/")) {
			this.webserver = ws;			
		} else {
			try {
				this.webserver = new URL("");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
