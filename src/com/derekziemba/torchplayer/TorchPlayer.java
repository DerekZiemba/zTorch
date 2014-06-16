package com.derekziemba.torchplayer;

import java.util.Timer;

import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.R.id;
import com.derekziemba.ztorch.R.layout;
import com.derekziemba.ztorch.R.menu;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

public class TorchPlayer extends Activity {
	
	public final String DEFAULT_INSTRUCTION = "1/2s, 6/1s, 0/1s, 11/.5s, 0/.5s, 9/.5s, 0/300, 12/300, 0/250, 10/250, 0/200, 6/150, 0/150, 4/100";
	public static final String PLAY_INSTRUCITON = "play_instruction";
	
	private Button playButton = null;
	private EditText playInstructions = null;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_torch_player);

		//if (savedInstanceState == null) {
		//	getFragmentManager().beginTransaction()
		//			.add(R.id.container, new PlaceholderFragment()).commit();
		//}
		
		playInstructions = (EditText) findViewById(R.id.TorchPlayerInstructions);
		playInstructions.setText(getText(R.string.editable).toString().replace("%s",DEFAULT_INSTRUCTION));

		playButton = (Button) findViewById(R.id.TorchPlayerPlay);
		handler = new Handler();

		if(playButton != null) {	
			playButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(getApplicationContext(),"Feature Not Ready, Will Crash App", Toast.LENGTH_SHORT).show();
					//playThis(getText(R.string.editable).toString());
				}
				
			});
		}	
	}		

	public void playThis(String command) {
		Player player = new Player();
		player.setBehavior(command);
		new Thread(player).start();
	}
	
	class Player implements Runnable {
		BrightnessBehavior behaviorScheme;

		public void setBehavior(String command) {
			behaviorScheme = new BrightnessBehavior(command);			
		}

		@Override
		public void run() {
			for( final BrightnessTime bt : behaviorScheme.getSteps()) {
				TorchConfig.setTorch(getApplicationContext(),bt.getLevel());
				try{
					Thread.sleep(bt.getTime());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
				handler.post(new Runnable() {
					@Override
					public void run() {
						TorchConfig.setTorch(getApplicationContext(),bt.getLevel());
					}
				});
			}
		}		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
