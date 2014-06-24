package com.derekziemba.ztorch.activities;

//import com.derekziemba.misc.FileTools;
import com.derekziemba.torchplayer.TorchPlayerWorker;
import com.derekziemba.torchplayer.TorchPlayerWorker.Options;
import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.Z;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

public class TorchPlayer extends Activity {
	
	public final String DEFAULT_INSTRUCTION = " 1/300, (1/100 & 2/70 & 3/70 " +
			"& 4/70 & 5/70 & 6/85 & 7/80 & 8/90 & 9/90 &10/90 & 11/90 & 12/90 & 13/90 &14/90& " +
			"15/90)*3, 15/200, (0/200&15/200)*3,  (15/140& 0/100)*3, (14/100&0/90)*3, (13/90& 0/85)*3 " +
			", (12/80&0/75)*3, (11/75&0/70)*3, (10/65&0/60)*3, (9/60&0/55)*3, (8/55&0/50)*3, " +
			"(7/50&0/45)*3, (6/45&0/40)*4, (5/40&0/35)*4, (4/35&0/30)*5, (3/30&0/25)*6, (2/30&0/25) *5," +
			" (1/30&0/25) *4, (1/25&0/25) *4, (1/20&0/20)*4, (1/15&0/15)*4,(1/10&0/20)*3,(15/45&0/25)*14," +
			"(15/15&0/55)*15, ";

	public static final String PLAY_INSTRUCTION = "play_instruction";
	
	private EditText playInstructions = null;
	private CheckBox repeatCheckBox = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_torch_player);

		playInstructions = (EditText) findViewById(R.id.TorchPlayerInstructions);
		//String playins = FileTools.getFileString(this,PLAY_INSTRUCTION);//
		String playins = Z.sharedPrefs(this).getString(PLAY_INSTRUCTION,DEFAULT_INSTRUCTION);	
		playInstructions.setText(getText(R.string.editable).toString().replace("%s",playins));
		repeatCheckBox = (CheckBox) findViewById(R.id.repeatCheckBox);
	}		
	
	public void onToggleClicked(View v) {
		String newplayins = playInstructions.getText().toString();
		Z.sharedPrefs(getApplicationContext()).edit().putString(PLAY_INSTRUCTION,newplayins).commit();
		//FileTools.writeFile(getApplicationContext(),PLAY_INSTRUCTION,newplayins);

		if(((ToggleButton) v).isChecked()){
			TorchPlayerWorker.play(newplayins, repeatCheckBox.isChecked() ? Options.REPEAT : Options.NOOP);
		} 
		else {
			TorchPlayerWorker.get().thread.interrupt();
		}
	}	
	
	/*

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/

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
