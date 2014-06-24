package com.derekziemba.ztorch.activities;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;



import com.derekziemba.torchplayer.Torch;
import com.derekziemba.ztorch.Z;
import com.derekziemba.ztorch.R;

public class MainActivity extends Activity {
	
	/*******************************************************************************
	 * Main Activity Broadcasters
	 *******************************************************************************/
	BroadcastReceiver mFlashValueUpdatedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Z.FLASH_VALUE_UPDATED_BROADCAST_NAME.equals(intent.getAction())) {	
				int value = intent.getIntExtra(Torch.CURRENT_BRIGHTNESS, 0);	//TODO:
				mBrightnessSlider.setProgress(value);
				mToggleButton.setChecked((value>0));
				txtCurrentBrightnessValue.setText(getText(R.string.current_level_value).toString().replace("%s", value+"") );
				Z.setNotif(context, value);
			}
		}
	};
	
	int glevel = 0;
	
	private SeekBar mBrightnessSlider = null;	
	private ToggleButton mToggleButton = null;

	private TextView txtCurrentBrightnessValue = null; 
	private TextView txtUsersBrightnessLimit = null; 
	private TextView textViewLimitSlider = null; 
	private TextView txtDefaultWidgetBrightness = null; 
		
	
	/*******************************************************************************
	 * Main Activity Method
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 *******************************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBrightnessSlider = (SeekBar) findViewById(R.id.flashlightBrightnessSlider);
		mToggleButton = (ToggleButton) findViewById(R.id.flashlightToggleButton);
				
		txtCurrentBrightnessValue = (TextView)findViewById(R.id.textViewBrightnessValue);
		txtCurrentBrightnessValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP,32);
		txtCurrentBrightnessValue.setText(getText(R.string.current_level_value).toString().replace("%s", "0"));
		

		txtDefaultWidgetBrightness = (TextView)findViewById(R.id.textViewDefaultBrightness);
		txtDefaultWidgetBrightness.setText(getText(R.string.default_brightness).toString()
				.replace("%s", Z.getInt(this,Z.DEFAULT_BRIGHTNESS)+"") );
		
		txtUsersBrightnessLimit = (TextView)findViewById(R.id.textViewCurrentLimit);
		txtUsersBrightnessLimit.setText(getText(R.string.current_limit_val).toString()
				.replace("%s", Z.getInt(this,Torch.LIMIT_VALUE) +" /" + Torch.AbsMaxLvl));
		
		textViewLimitSlider = (TextView)findViewById(R.id.textViewCurrentLimitSlider);			
		textViewLimitSlider.setText(getText(R.string.current_limit_val).toString()
				.replace("%s", Z.getInt(this,Torch.LIMIT_VALUE)+"" ));
		
		registerReceiver(mFlashValueUpdatedReceiver,new IntentFilter(Z.FLASH_VALUE_UPDATED_BROADCAST_NAME));

		if (mBrightnessSlider != null) {	
			mBrightnessSlider.setMax(Z.getInt(this,Torch.LIMIT_VALUE));
			/*
			 * If the slider is null its safe to assume the activity was just launched and none 
			 * of the on screen values are correct, so we will just set the current brightness again
			 * which will cause the BroadcastReceiver to refire and correct the values. 
			 * 
			 * Additionally, upon the app launching for the first time, causing this to fire will cause
			 * the phone to prompt for root access and check compatability.  The Torch class does this
			 * every time a new Torch value is sent. This behavior results in a slight delay. 
			 */
			Torch.setTorch(this, Z.getInt(this,Torch.CURRENT_BRIGHTNESS));
			
			mBrightnessSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {	
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if(!fromUser){  return;	}
					/*
					 * Because a bored user might find it fun (me) to quickly slide the seekbeacon back and 
					 * forth really fast with minimal delay in the Torch response time, we are going to 
					 * bypass device validation and the broadcast.  Allowing a response time of less than 3ms.  
					 * We still set the textview though, otherwise it looks bad.  
					 */
					Torch.setLevelCovertly(progress);
					txtCurrentBrightnessValue.setText(getText(R.string.current_level_value).toString().replace("%s", progress+"") );
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) { 
				};
				@Override public void onStopTrackingTouch(SeekBar seekBar) {
					/*
					 * We will send the broadcast after the user takes their finger off
					 */
					Torch.setTorch(getApplicationContext(), seekBar.getProgress());	
				};
			});

		}

	}
	

	public void torchToggleInteraction(View v) {
		if(((ToggleButton)v).isChecked()) {
			Torch.setTorch(this,Z.getInt(this,Z.DEFAULT_BRIGHTNESS));
		}
		else {
			Torch.setTorch(this,0);
		}
	}	
	
	public void saveDefaultBrightnessButtonInteraction(View v) {
		int lvl = mBrightnessSlider.getProgress();
		if(lvl==0) {
			Toast.makeText(MainActivity.this, R.string.toast_set_widget_off, Toast.LENGTH_SHORT).show();
		}
		else {
			Z.putInt(this,Z.DEFAULT_BRIGHTNESS, lvl);
			txtDefaultWidgetBrightness.setText(getText(R.string.default_brightness).toString().replace("%s", lvl+"") );
		}

	}	

	
	/*******************************************************************************
	 * Main Activity Options button Menu
	 *******************************************************************************/	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_about:
			Intent aboutIntention = new Intent(this,AboutActivity.class);
			this.startActivity(aboutIntention);
			return true;
		case R.id.set_maximum:
			setNewMaximum();
			return true;
		case R.id.action_settings:
			Intent settingsIntention = new Intent(this,SettingsActivity.class);
			this.startActivityForResult(settingsIntention,0);
			return true;
		case R.id.action_torch_player:
			Intent torchplayerIntention = new Intent(this,TorchPlayer.class);
			this.startActivityForResult(torchplayerIntention,0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*******************************************************************************
	 * Main Activity SetNewMaximum menu popup
	 *******************************************************************************/
	public void setNewMaximum() {
		Torch.setTorch(this,0);

		final View content = LayoutInflater.from(this).inflate(R.layout.new_maximum_layout, null);
		final SeekBar seekBar = (SeekBar)content.findViewById(R.id.seekbar);
		seekBar.setMax(Torch.AbsMaxLvl);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			final TextView textView = (TextView)content.findViewById(R.id.instructions_textview);

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				textView.setText(getText(R.string.set_max_torch_limit_instructions).toString().replace("%s", String.valueOf(progress)) );
				Torch.setTorch(getApplicationContext(),progress);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
		});
		
		//Initial redraw
		seekBar.setProgress(0);

		new AlertDialog.Builder(this)
			.setTitle(R.string.set_maximum)
			.setView(content)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					glevel = seekBar.getProgress();
					if(glevel == 0) { glevel = 1; }
					Z.putInt(getApplicationContext(),Torch.LIMIT_VALUE,glevel);
					if(glevel < Z.getInt(getApplicationContext(), Z.DEFAULT_BRIGHTNESS)){
						Z.putInt(getApplicationContext(),Z.DEFAULT_BRIGHTNESS,glevel);
					}
					Torch.setTorch(getApplicationContext(),0);
					finish();
					startActivity(getIntent());
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Torch.setTorch(getApplicationContext(),0);
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Torch.setTorch(getApplicationContext(),0);
				}
			})
			.show();
	}
		
	public void showAbout() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.action_about)
		.setMessage(R.string.about_text);
		alertDialog.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK) {
	        setResult(RESULT_OK);
	        finish();
		    startActivity(getIntent());
	    }
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mFlashValueUpdatedReceiver);
		super.onDestroy();
	}
	
}