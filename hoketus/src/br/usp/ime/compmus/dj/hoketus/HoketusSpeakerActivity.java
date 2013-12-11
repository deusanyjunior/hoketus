package br.usp.ime.compmus.dj.hoketus;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import br.usp.ime.compmus.dj.webserver.WebServerMethods;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class HoketusSpeakerActivity extends Activity {

	private static Context context;
	private static final String TAG = "Hoketus";
	private static boolean isRunning = false;
	private static boolean getHotspotList = true;
	
	private PdUiDispatcher dispatcher;
	private PdService pdService = null;
	
	File pdFile = null;
	static int patchHandle = 0;
	
	Timer timerWifiScan;
	static WifiManager mainWifi;
//	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	
	private static String webserver = "http://wscompmus.deusanyjunior.dj/";
	private static String hoketusHotSpot = "hoketus0";
	private static int hoketusId = 0;
	private static final String[] hoketusParams = {"dbm"};
	
	private static WebServerMethods nextIdReceiver;
	private static boolean nextIdReceiverRunning = true;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hoketus_speaker);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		context = this;
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && bundle.containsKey("hoketus")) {
			hoketusId = bundle.getInt("hoketus");
			hoketusHotSpot = "hoketus".concat(Integer.toString(hoketusId));
		}	
		
		// Wifi
//		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		receiverWifi = new WifiReceiver();
//		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//				
		
		// PD
		bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
		
		// Webserver Methods	
		WSStart wsStart = new WSStart();
		wsStart.execute();
		
//		boolean started = startNextIdReceiver();
//		if (started) {
//			Toast.makeText(this, "Connected to webserver: "+nextIdReceiver.getWebserver(), Toast.LENGTH_SHORT).show();
//		} else {
//			Toast.makeText(this, "Could no connect to webserver", Toast.LENGTH_LONG).show();
//		}
		
		isRunning = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
    protected void onResume() {
		Log.i(TAG, "onResume()");
    	super.onResume();
//    	registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//		WifiScan wifiScan = new WifiScan();
//		wifiScan.execute();
    }
	
	@Override
    protected void onPause() {
		Log.i(TAG, "onPause()");
    	super.onPause();
//    	unregisterReceiver(receiverWifi);
    }
    
    @Override
    protected void onStop() {
    	Log.i(TAG, "onStop()");
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
    	stopNextIdReceiver();
        isRunning = false;
        PdBase.closePatch(patchHandle);
    	unbindService(pdConnection);
    	Log.i(TAG, "onDestroy()");
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
    		Intent intent = new Intent(HoketusSpeakerActivity.this,
    								HoketusSpeakerActivity.class);
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
    	switch (hoketusId) {
		case 0:
			patchFile = new File(dir, "patch_cel1.pd");
			break;
		case 1:
			patchFile = new File(dir, "patch_cel2.pd");
			break;
		case 2:
			patchFile = new File(dir, "patch_cel3.pd");
			break;
		case 3:
			patchFile = new File(dir, "patch_cel4.pd");
			break;
		default:
			patchFile = new File(dir, "patch_cel1.pd");
			break;
		}    	
    	patchHandle = PdBase.openPatch(patchFile.getAbsolutePath());
    }
  
	
// WebServer Methods
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
//	private boolean startNextIdReceiver() {
//		boolean started = false;
//		try {
//			if ( webserver != null) {
//				nextIdReceiver = new WebServerMethods(new URL(webserver.concat(hoketusHotSpot).concat("s")), hoketusHotSpot, hoketusParams);
//				
//				if (isNetworkAvailable()) {
//					if (nextIdReceiver.setLastIdFromWS()) {
//						nextIdReceiverRunning = true;
//						WSNextIdReceiver wsnNextIdReceiver = new WSNextIdReceiver();
//						wsnNextIdReceiver.execute();
//						started =  true;
//					}			
//				}
//			}
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			started = false;
//			return started;
//		}
//		return started;
//	}
	
	private void stopNextIdReceiver() {
		nextIdReceiverRunning = false;
	}
	
	private class WSStart extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			
			boolean started = false;
			try {
				if ( webserver != null) {
					nextIdReceiver = new WebServerMethods(new URL(webserver.concat(hoketusHotSpot).concat("s")), hoketusHotSpot, hoketusParams);
					
					if (isNetworkAvailable()) {
						if (nextIdReceiver.setLastIdFromWS()) {
							nextIdReceiverRunning = true;
//							WSNextIdReceiver wsnNextIdReceiver = new WSNextIdReceiver();
//							wsnNextIdReceiver.execute();
							started =  true;
						}			
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				started = false;
				return started;
			}
			return started;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result) {
				connected();
				WSNextIdReceiver nextIdReceiver = new WSNextIdReceiver();
				nextIdReceiver.execute();
			} else {
				notConnected();
			}
		}
	}
	
	public static void connected() {
		Toast.makeText(context, "Connected to webserver: "+nextIdReceiver.getWebserver(), Toast.LENGTH_SHORT).show();
	}
	
	public static void notConnected() {
		Toast.makeText(context, "Could no connect to webserver", Toast.LENGTH_LONG).show();
	}
	
	
	private class WSNextIdReceiver extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
				
			if(nextIdReceiverRunning) {				
				String[] nextId = nextIdReceiver.getNextId();
				if( nextId != null ) {
					Log.w(TAG, "ReaderThread: "+nextId[0]);
					float dbm = 0.0f;
					try {
						dbm = Float.parseFloat(nextId[0]);
					} catch (NumberFormatException n) {
						dbm = -1;
					}
					PdBase.sendFloat("webserver", dbm);
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(nextIdReceiverRunning) {	
				WSNextIdReceiver nextIdReceiver = new WSNextIdReceiver();
				nextIdReceiver.execute();
			}
		}
	}
	

}
