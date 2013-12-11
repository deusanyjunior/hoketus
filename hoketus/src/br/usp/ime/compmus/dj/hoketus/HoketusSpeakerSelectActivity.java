package br.usp.ime.compmus.dj.hoketus;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HoketusSpeakerSelectActivity extends Activity {

	private Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hoketus_speaker_select);
		
		Button button_hoketus0 = (Button) findViewById(R.id.button_hoketus0);
		button_hoketus0.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, HoketusSpeakerActivity.class);
				intent.putExtra("hoketus", 0);
				startActivity(intent);
			}
		});
		
		Button button_hoketus1 = (Button) findViewById(R.id.button_hoketus1);
		button_hoketus1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, HoketusSpeakerActivity.class);
				intent.putExtra("hoketus", 1);
				startActivity(intent);
			}
		});
		
		Button button_hoketus2 = (Button) findViewById(R.id.button_hoketus2);
		button_hoketus2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, HoketusSpeakerActivity.class);
				intent.putExtra("hoketus", 2);
				startActivity(intent);
			}
		});
		
		Button button_hoketus3 = (Button) findViewById(R.id.button_hoketus3);
		button_hoketus3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, HoketusSpeakerActivity.class);
				intent.putExtra("hoketus", 3);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hoketus_speaker_select, menu);
		return true;
	}

}
