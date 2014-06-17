package com.derekziemba.torchplayer;


import com.derekziemba.torchplayer.TorchControlThread.Options;
import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.Z;

import android.app.Activity;
import android.os.Bundle;
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
	public static final String REPEAT_MODE = "repeat_mode";
	
	private EditText playInstructions = null;
	private CheckBox repeatCheckBox = null;
	private Thread torchThread = null;
	TorchControlThread tct = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_torch_player);

		playInstructions = (EditText) findViewById(R.id.TorchPlayerInstructions);
		String playins = Z.prefs(this).getString(PLAY_INSTRUCTION,DEFAULT_INSTRUCTION);
		playInstructions.setText(getText(R.string.editable).toString().replace("%s",playins));

		repeatCheckBox = (CheckBox) findViewById(R.id.repeatCheckBox);
		
		tct = new TorchControlThread();
		
		if(repeatCheckBox != null) {
			repeatCheckBox.setChecked(Z.prefs(this).getBoolean(REPEAT_MODE,false));
			repeatCheckBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Z.prefs(getApplicationContext()).edit().putBoolean(REPEAT_MODE,repeatCheckBox.isChecked()).commit();
				}
			});
		}

	}		
	
	
	public void onToggleClicked(View v) {
		//Toast.makeText(getApplicationContext(),"Feature Not Ready, Will Crash App", Toast.LENGTH_SHORT).show();
		String newplayins = playInstructions.getText().toString();
		Z.prefs(getApplicationContext()).edit().putString(PLAY_INSTRUCTION,newplayins).commit();
		
		boolean on = ((ToggleButton) v).isChecked();
		
		if(on){
			tct.config(newplayins, repeatCheckBox.isChecked() ? Options.REPEAT : Options.NOOP);
			tct.requestResume();
			torchThread = new Thread(tct);
			torchThread.start();
		}
		else {
			tct.requestStop();
			torchThread.interrupt();
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

	@Override
	public void onBackPressed() {
	    finish();
	}
}
