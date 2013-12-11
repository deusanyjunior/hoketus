package br.usp.ime.compmus.dj.hoketus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;
import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import br.usp.ime.compmus.utils.JSONfunctions;


import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;

public class HoketusUserActivity extends Activity {

//	private Context context = this;
	private static final String TAG = "Hoketus";

	private static boolean isRunning = false;
	private static boolean getHotspotList = true;

	private PdUiDispatcher dispatcher;
	private PdService pdService = null;
	public View multiTouchView;

	File pdFile = null;
	static int patchHandle = 0;
	
	ArrayList<String> str = new ArrayList<String>();
	ListAdapter adapter;
	protected Handler handler = new Handler();
	String errorMessage;

	Timer timerWifiScan;
	static WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	public static long initialScanTime = 0;

	private static HashMap<String, Integer> hotspotHash;
	private static int[] hotspots;
	public static String webserver = "http://wscompmus.deusanyjunior.dj/";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_hoketus_client);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   

		hotspotHash = new HashMap<String, Integer>();
		hotspotHash.put("hoketus0", -100);
		hotspotHash.put("hoketus1", -100);
		hotspotHash.put("hoketus2", -100);
		hotspotHash.put("hoketus3", -100);
		hotspotHash.put("hoketus4", -100);
		hotspots = new int[5];
		for(int i = 0; i < 5; i++) {
			hotspots[i] = -100;
		}

		// Wifi
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// PD
		bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);

		// Hoketus
		isRunning = true;
	}

	@Override
	protected void onResume() {
		super.onResume();    	
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		WifiScan wifiScan = new WifiScan();
		wifiScan.execute();
		Log.e(TAG, "onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiverWifi);
		Log.e(TAG, "onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e(TAG, "onStop()");
	}

	@Override
	public void onDestroy() {
		isRunning = false;
		PdBase.closePatch(patchHandle);
		unbindService(pdConnection);
		Log.e(TAG, "onDestroy()");
		super.onDestroy();
	}	


// Pure Data

	private final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pdService = ((PdService.PdBinder)service).getService();
			try {
				initPd();
				loadPatch();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};

	private void initPd() throws IOException {
		// Configure the audio glue
		int sampleRate = AudioParameters.suggestSampleRate();
		pdService.initAudio(sampleRate, 1, 2, 10.0f);
		pdService.startAudio();
		start();
		// Create and install the dispatcher
		dispatcher = new PdUiDispatcher();
		PdBase.setReceiver(dispatcher);
	}

	private void start() {
		if (!pdService.isRunning()) {
			Intent intent = new Intent(HoketusUserActivity.this,
					HoketusUserActivity.class);
			pdService.startAudio(intent, R.drawable.ic_launcher,
					"HKTS", "Return to HKTS.");
		}
	}

	private void loadPatch() throws IOException {
		// Hear the sound
		File dir = getFilesDir();
		File patchFile;
		IoUtils.extractZipResource(
				getResources().openRawResource(R.raw.hktsm8), dir, true);
		patchFile = new File(dir, "patch_user.pd");
		patchHandle = PdBase.openPatch(patchFile.getAbsolutePath());
	}

	
// Wifi Methods

	public static String getHotspotList() {
		StringBuilder hotspotList = new StringBuilder();
		
		for( Entry<String, Integer> entry : hotspotHash.entrySet() ) {
			if(entry.getValue() != -100) {
				hotspotList.append(" h"+entry.getKey().charAt(7)+" "+entry.getValue());
			}
		}
		return hotspotList.toString();
	}


	/**
	 * Code based: 
	 * http://www.androidsnippets.com/scan-for-wireless-networks
	 */

	private class WifiScan extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			
			Log.i(TAG, "Scan in background.. ");
			if(isRunning) {			
				mainWifi.startScan();
				initialScanTime = SystemClock.elapsedRealtime();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(!isRunning) {
				Log.i(TAG, "Scan finished!");
			}
		}
	}

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			long elapsedTime = SystemClock.elapsedRealtime();
			Log.i(TAG, "Scan duration: "+(elapsedTime-initialScanTime));
			WifiScan wifiScan = new WifiScan();
			wifiScan.execute();

			wifiList = mainWifi.getScanResults();
			for(int i = 0; i < wifiList.size(); i++){
				PdBase.sendFloat("sensorW-"+wifiList.get(i).SSID, wifiList.get(i).level);
				
				if(hotspotHash.containsKey( wifiList.get(i).SSID ) ) {
					hotspotHash.put(wifiList.get(i).SSID, wifiList.get(i).level);
					Log.i(TAG, "Hotspot found: "+wifiList.get(i));
					WSWifiLevelSender wswifilevelsender = new WSWifiLevelSender();
					wswifilevelsender.execute(new String[] { wifiList.get(i).SSID, Integer.toString(wifiList.get(i).level) });
					hotspots[Character.getNumericValue(wifiList.get(i).SSID.charAt(7))] = 100;
				}
			}
			
			for(int i = 0; i < 5; i++) {
				if(hotspots[i] != 100) {
					PdBase.sendFloat("sensorW-hoketus"+i, -100);
					hotspotHash.put("hoketus"+i, -100);
				}
				hotspots[i] = -100;
			}
		}
	}

	private class WSWifiLevelSender extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				JSONObject internalJsonObject = new JSONObject();
				internalJsonObject.put("dbm", Integer.parseInt(params[1]));

				JSONObject jsonObject = new JSONObject();
				jsonObject.put(params[0], internalJsonObject);

				JSONfunctions.sendJSONToURL(webserver.concat(params[0].concat("s")), jsonObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
} 