package com.derekziemba.ztorch.activities;

import com.derekziemba.ztorch.Global;
import com.derekziemba.ztorch.R;
import com.derekziemba.ztorch.Tools;
import com.derekziemba.ztorch.TorchConfig;
import com.stericson.RootTools.RootTools;

public class MainActivity extends Activity {
	private SeekBar mBrightnessSlider = null;	
	private Button mSetDefaultBrightnessValueButton = null;
	private ToggleButton mToggleButton = null;
	
	private Button addDoubleButton = null;
	private Button addSingleButton = null;
	private Button subtractDoubleButton = null;
	private Button subtractSingleButton = null;
	

	private int mAbsMaxVBrightness = TorchConfig.getAbsMaxBrightness();

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

		initButtonViews();
		initPreferences();
		initTextViews();
		
		registerReceiver(mFlashValueUpdatedReceiver,new IntentFilter(Global.FLASH_VALUE_UPDATED_BROADCAST_NAME));

		if (mBrightnessSlider != null) {	
			mBrightnessSlider.setMax(mAbsMaxVBrightness);
			brightnessSliderListener();
			mBrightnessSlider.setProgress(TorchConfig.getCurrentBrightness(getApplicationContext()));
		}
		
		if(mSetDefaultBrightnessValueButton != null) {	defaultBrightnessButtonListener();	}	
		if(mToggleButton != null) {	enableTorchToggleListener();	}		
		if(addDoubleButton != null) {	addDoubleButtonListener();	}
		if(addSingleButton != null) {	addSingleButtonListener();	}		
		if(subtractSingleButton != null) {	subtractSingleButtonListener();	}	
		if(subtractDoubleButton != null) {	subtractDoubleButtonListener();	}
		
		if (TorchConfig.checkCompatibility(this)) {
			int progress = mBrightnessSlider.getProgress();
			updateTorchBroadcast(progress);
		} 
		else {
			if (!RootTools.isRootAvailable()) {	Tools.createErrorDialog(this, Tools.Errors.NO_ROOT);	} 
			else if (!RootTools.isAccessGiven()) { Tools.createErrorDialog(this, Tools.Errors.NO_ROOT_ACCESS);	} 
		}
	}
	
	/*******************************************************************************
	 * Main Activity Initialization methods
	 *******************************************************************************/
	private void initButtonViews() {
		mBrightnessSlider = (SeekBar) findViewById(R.id.flashlightBrightnessSlider);
		mSetDefaultBrightnessValueButton = (Button) findViewById(R.id.widgetLevelButton);
		mToggleButton = (ToggleButton) findViewById(R.id.flashlightToggleButton);
		addDoubleButton = (Button) findViewById(R.id.ppButton);
		subtractDoubleButton = (Button) findViewById(R.id.nnButton);
		subtractSingleButton = (Button) findViewById(R.id.nButton);
		addSingleButton = (Button) findViewById(R.id.pButton);
	}
	
	private void initPreferences() {
		mAbsMaxVBrightness = TorchConfig.getBrightnessLimitValue(this);
	}
	
	private void initTextViews() {
		txtCurrentBrightnessValue = (TextView)findViewById(R.id.textViewBrightnessValue);
		txtCurrentBrightnessValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP,32);
		txtDefaultWidgetBrightness = (TextView)findViewById(R.id.textViewDefaultBrightness);
		txtUsersBrightnessLimit = (TextView)findViewById(R.id.textViewCurrentLimit);
		textViewLimitSlider = (TextView)findViewById(R.id.textViewCurrentLimitSlider);	
		
		txtCurrentBrightnessValue.setText(getText(R.string.current_level_value).toString().replace("%s", String.valueOf(0)) );
		
		txtDefaultWidgetBrightness.setText(getText(R.string.current_default_brightness).toString().replace("%s", 
				String.valueOf(Global.getDefaultBrightness(getApplicationContext()))) );
		
		txtUsersBrightnessLimit.setText(getText(R.string.current_limit_val).toString().replace("%s", 
				String.valueOf( TorchConfig.getBrightnessLimitValue(getApplicationContext())) +
				" /" + String.valueOf(TorchConfig.getAbsMaxBrightness())) );
		
		textViewLimitSlider.setText(getText(R.string.current_limit_val).toString().replace("%s", 
				String.valueOf(TorchConfig.getBrightnessLimitValue(getApplicationContext()) )));
	}
	
	/*******************************************************************************
	 * Main Activity Brightness Slider Listener
	 *******************************************************************************/
	private void brightnessSliderListener() {
		mBrightnessSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {	
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(!fromUser){  return;	}				
				updateTorchBroadcast(progress);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
		});
	}

	/*******************************************************************************
	 * Main Activity Enable Torch Toggle Listener
	 *******************************************************************************/
	private void enableTorchToggleListener() {
		mToggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int progress = TorchConfig.getCurrentBrightness(getApplicationContext());
				int brightness = Global.getDefaultBrightness(getApplicationContext());	
	
				if (progress == 0) {
					updateTorchBroadcast(brightness);
					Toast.makeText(MainActivity.this, R.string.toast_toggle_on, Toast.LENGTH_SHORT).show();
				} 
				else {
					updateTorchBroadcast(0);
					Toast.makeText(MainActivity.this, R.string.toast_toggle_off, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}	
	
	/*******************************************************************************
	 * Main Activity Button Listeners
	 *******************************************************************************/
	private void defaultBrightnessButtonListener() {
		mSetDefaultBrightnessValueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int progress = TorchConfig.getCurrentBrightness(getApplicationContext());
				if (progress == 0) {
					Toast.makeText(MainActivity.this, R.string.toast_set_widget_off, Toast.LENGTH_SHORT).show();
				} else {
					Global.setDefaultBrightness(getApplicationContext(), progress);
					txtDefaultWidgetBrightness.setText(getText(R.string.current_default_brightness).toString().replace("%s", String.valueOf(progress)) );
					Toast.makeText(MainActivity.this, R.string.toast_done, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}	
	private void addDoubleButtonListener() {
		addDoubleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TorchConfig.setTorch(getApplicationContext(), true);
			}
		});
	}
	private void addSingleButtonListener() {
		addSingleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int progress = TorchConfig.getCurrentBrightness(getApplicationContext());
				if(progress < TorchConfig.getBrightnessLimitValue(getApplicationContext())) {
					progress++;
					TorchConfig.setTorch(getApplicationContext(), progress);
				}
			}
		});
	}
	private void subtractSingleButtonListener() {
		subtractSingleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int progress = TorchConfig.getCurrentBrightness(getApplicationContext());
				if(progress > 0) {
					progress--;
					TorchConfig.setTorch(getApplicationContext(), progress);
				}
			}
		});
	}	
	private void subtractDoubleButtonListener() {
		subtractDoubleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TorchConfig.setTorch(getApplicationContext(), false);
			}
		});
	}	
	
	
	/*******************************************************************************
	 * Main Activity Broadcasters
	 *******************************************************************************/
	
	BroadcastReceiver mFlashValueUpdatedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Global.FLASH_VALUE_UPDATED_BROADCAST_NAME.equals(intent.getAction())) {	
				int value = intent.getIntExtra(Global.KEY_NEW_VALUE, 0);	
				mBrightnessSlider.setProgress(value);
				mToggleButton.setChecked((value>0));
				txtCurrentBrightnessValue.setText(getText(R.string.current_level_value).toString().replace("%s", String.valueOf(value)) );
				Global.setNotification(context, value);
			}
		}
	};
	
	
	public void updateTorchBroadcast(int value) {
		TorchConfig.setTorch(getApplicationContext(), value);
		Intent broadcastIntent = new Intent(Global.FLASH_VALUE_UPDATED_BROADCAST_NAME);
		broadcastIntent.putExtra(Global.KEY_NEW_VALUE, value);
		sendBroadcast(broadcastIntent);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*******************************************************************************
	 * Main Activity SetNewMaximum menu popup
	 *******************************************************************************/
	public void setNewMaximum() {
		updateTorchBroadcast(0);
		final View content = LayoutInflater.from(this).inflate(R.layout.new_maximum_layout, null);
		final SeekBar seekBar = (SeekBar)content.findViewById(R.id.seekbar);
		seekBar.setMax(TorchConfig.getAbsMaxBrightness());
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			final TextView textView = (TextView)content.findViewById(R.id.instructions_textview);

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				textView.setText(getText(R.string.set_max_torch_limit_instructions).toString().replace("%s", String.valueOf(progress)) );
				updateTorchBroadcast(progress);
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
					mAbsMaxVBrightness = seekBar.getProgress();
					if(mAbsMaxVBrightness == 0) { mAbsMaxVBrightness = 1; }
					TorchConfig.setBrightnessLimitValue(getApplicationContext(),mAbsMaxVBrightness);
					if(mAbsMaxVBrightness < Global.getDefaultBrightness(getApplicationContext())){
						Global.setDefaultBrightness(getApplicationContext(),mAbsMaxVBrightness);
					}
					updateTorchBroadcast(0);
					finish();
					startActivity(getIntent());
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					updateTorchBroadcast(0);
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					updateTorchBroadcast(0);
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