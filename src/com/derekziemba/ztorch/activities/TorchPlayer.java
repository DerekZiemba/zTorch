package com.derekziemba.ztorch.activities;

import com.derekziemba.root.FileTools;
import com.derekziemba.torchplayer.TorchControlThread;
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
	
	public final String DEFAULT_INSTRUCTION = "1/350, 2/70, 3/70, 4/70, 5/70, 6/85, 7/80, 8/90, 9/90, 10/90," +
			" 11/100, 12/100, 13/100,14/100, 15/200,(0/200&15/200)*4, (15/150&0/120)*3, (14/120&0/100)*3," +
			" (13/100&0/90)*3, (12/90&0/75)*3, (11/75&0/50)*3, (10/50&0/35)*2, (9/35&0/25)*2, (8/35&0/25)*2," +
			" (7/30&0/25)*3, (6/30&0/25)*3, (5/30&0/25)*4, (4/30&0/25)*4, (3/30&0/25)*4, (2/30&0/25) *4," +
			" (1/30&0/25) *3, (1/25&0/25) *3, (1/20&0/20) *8 , (1/15&0/15) *6 , (1/10&0/20) *4 ," +
			" (15/20&0/25)*10, (15/40&0/20)*8";
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
		
		String playins = FileTools.getFileString(this,PLAY_INSTRUCTION);//Z.prefs(this).getString(PLAY_INSTRUCTION,DEFAULT_INSTRUCTION);
		
		
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
		//Z.prefs(getApplicationContext()).edit().putString(PLAY_INSTRUCTION,newplayins).commit();
		FileTools.writeFile(getApplicationContext(),PLAY_INSTRUCTION,newplayins);
		
		boolean on = ((ToggleButton) v).isChecked();
		
		if(on){
			tct.config(newplayins, repeatCheckBox.isChecked() ? Options.REPEAT : Options.NOOP);
			torchThread = new Thread(tct);
			torchThread.setPriority(Thread.MAX_PRIORITY);
			torchThread.start();
		}
		else {
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
