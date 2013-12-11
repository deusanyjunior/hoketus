package br.usp.ime.compmus.dj.hoketus;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HoketusMainActivity extends Activity {

	private Context context = this;
	private static final String TAG = "Hoketus";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_hoketus_main);
    
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	
    Button button_user = (Button) findViewById(R.id.button_user);
    button_user.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intentClient = new Intent(context, HoketusUserActivity.class);
			startActivity(intentClient);
		}
	});
    
    Button button_speaker = (Button) findViewById(R.id.button_speaker);
    button_speaker.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intentServer = new Intent(context, HoketusSpeakerSelectActivity.class);
			startActivity(intentServer);
		}
	});
    
  }

} 