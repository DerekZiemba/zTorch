package com.derekziemba.torchplayer;


import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.Z;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

public class TorchPlayer extends Activity {
	
	public final String DEFAULT_INSTRUCTION = "1/1s, 12/1s, 0/1s, 11/.5s, 0/.5s, 9/.5s, 0/300, 12/300, 0/250, 10/250, 0/200, 6/150, 0/150, 4/100, 0/100, 9/100, 0/100, 9/100, 0/100";
	//public final String DEFAULT_INSTRUCTION = "1/2000";
	public static final String PLAY_INSTRUCTION = "play_instruction";
	
	private ToggleButton playButton = null;
	private EditText playInstructions = null;
	private CheckBox repeatCheckBox = null;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_torch_player);

		playInstructions = (EditText) findViewById(R.id.TorchPlayerInstructions);
		String playins = Z.prefs(this).getString(PLAY_INSTRUCTION,DEFAULT_INSTRUCTION);
		playInstructions.setText(getText(R.string.editable).toString().replace("%s",playins));

		playButton = (ToggleButton) findViewById(R.id.TorchPlayerPlay);

		if(playButton != null) {	
			playButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Toast.makeText(getApplicationContext(),"Feature Not Ready, Will Crash App", Toast.LENGTH_SHORT).show();
					String newplayins = playInstructions.getText().toString();
					Z.prefs(getApplicationContext()).edit().putString(PLAY_INSTRUCTION,newplayins).commit();
					playThis3(newplayins);
				}			
			});
		}	
	}		
	
	public void playThis2(String command) {
		//Log.v("playThis", "Command is: " + command);
		BrightnessBehavior behaviorScheme = new BrightnessBehavior(command);
		for( BrightnessTime bt : behaviorScheme.getSteps()) {
			//Log.i("BrightnessTime: ","Setting: " + bt.toString());
			TorchConfig.setTorch(getApplicationContext(),bt.getLevel());
			try{
				Thread.sleep(bt.getTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}

	public void playThis(String command) {
		Player player = new Player();
		player.setBehavior(command);
		new Thread(player).start();
	}
	
	public void playThis3(String command) {
		TorchControlThread bc = new TorchControlThread(command);
		new Thread(bc).start();
	}
	
	class Player implements Runnable {
		BrightnessBehavior behaviorScheme;

		public void setBehavior(String command) {
			behaviorScheme = new BrightnessBehavior(command);			
		}

		@Override
		public void run() {
			for( final BrightnessTime bt : behaviorScheme.getSteps()) {
				//TorchConfig.setTorch(getApplicationContext(),bt.getLevel());
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
