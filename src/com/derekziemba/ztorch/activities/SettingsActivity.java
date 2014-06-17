package com.derekziemba.ztorch.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.derekziemba.torchplayer.TorchConfig;
import com.derekziemba.ztorch.Z;
import com.derekziemba.ztorch.R;

public class SettingsActivity extends Activity {
	
	private class ButtonWrapper {
		private Button button = null;
		public int titleId = 0;
		public int messageId = 0;
		

		public ButtonWrapper(Button data, int title, int msg) {
			this.button = data;
			this.titleId = title;
			this.messageId = msg;
		}
		
		public Button getButton() {
			return this.button;
		}	
		
	}
	
	private ButtonWrapper qMaxLimit = null;
	private ButtonWrapper qDefaultLevel = null;
	private ButtonWrapper qQuickIncrements = null;
	private ButtonWrapper qDoubleTap = null;
	private ButtonWrapper qRapidTap = null;
	private ButtonWrapper qTapTime = null;
	private ButtonWrapper qPersistNotif= null;
	private ButtonWrapper qMinNotif = null;
	
	private Button setMaxLevelButton = null;
	private Button setDefaultLevelButton = null;
	private Button setQuickIncButton = null;
	private Button setTapTimeButton = null;
	
	private ToggleButton doubleTapToggle = null;
	private ToggleButton rapidTapToggle = null;
	private ToggleButton persistNotifToggle = null;
	private ToggleButton minNotifToggle = null;
	
	private TextView valueMaxLevelTXTVIEW = null; 
	private TextView valueDefaultLevelTXTVIEW = null; 
	private TextView valueQuickIncrementTXTVIEW = null; 
	private TextView valueTapTimeTXTVIEW = null; 
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		initQuestionButtons();
		initSetButtons();
		initToggleButtons();
		initTextViews();
		refreshTextViews();
		
		questionListener();
		toggleListener();
		buttonListener();
	}
	
	/*******************************************************************************
	 * Activity Initialization methods
	 *******************************************************************************/
	private void initQuestionButtons() {
		qMaxLimit		 = new ButtonWrapper((Button) findViewById(R.id.questionMaxLevel_BUTTON), R.string.max_level, R.string.question_max_level);
		qDefaultLevel	 = new ButtonWrapper((Button) findViewById(R.id.questionDefaultLevel_BUTTON), R.string.default_level, R.string.question_default_level);
		qQuickIncrements = new ButtonWrapper((Button) findViewById(R.id.questionQuickIncrements_BUTTON), R.string.quick_increments,	R.string.question_quick_increments	);
		qDoubleTap		 = new ButtonWrapper((Button) findViewById(R.id.questionDoubleTap_BUTTON), R.string.double_tap,	R.string.question_double_tap	);
		qRapidTap		 = new ButtonWrapper((Button) findViewById(R.id.questionRapidTap_BUTTON), R.string.rapid_tap, 	R.string.question_rapid_tap		);
		qTapTime		 = new ButtonWrapper((Button) findViewById(R.id.questionTapTime_BUTTON), R.string.tap_time, 	R.string.question_tap_time		);
		qPersistNotif	 = new ButtonWrapper((Button) findViewById(R.id.questionPersistNotif_BUTTON), R.string.persistent_notif, 	R.string.question_persistent_notif	);
		qMinNotif		 = new ButtonWrapper((Button) findViewById(R.id.questionMinNotif_BUTTON), R.string.minimize_notif, 	R.string.question_minimize_notif	);
	}
	
	private void initSetButtons() {
		setMaxLevelButton		= (Button) findViewById(R.id.setMaxLevel_BUTTON);
		setDefaultLevelButton	= (Button) findViewById(R.id.setDefaultLevel_BUTTON);
		setQuickIncButton		= (Button) findViewById(R.id.setQuickIncrements_BUTTON);
		setTapTimeButton 		= (Button) findViewById(R.id.setTapTime_BUTTON);
	}
	
	private void initToggleButtons() {
		doubleTapToggle 	= (ToggleButton) findViewById(R.id.setDoubleTap_TOGGLE);
		rapidTapToggle		= (ToggleButton) findViewById(R.id.setRapidTap_TOGGLE);
		persistNotifToggle	= (ToggleButton) findViewById(R.id.setPersistNotif_TOGGLE);
		minNotifToggle		= (ToggleButton) findViewById(R.id.setMinNotif_TOGGLE);
	}
	
	private void initTextViews() {
		valueMaxLevelTXTVIEW		= (TextView) findViewById(R.id.valueMaxLevel_TXTVIEW);
		valueDefaultLevelTXTVIEW	= (TextView) findViewById(R.id.valueDefaultLevel_TXTVIEW);
		valueQuickIncrementTXTVIEW	= (TextView) findViewById(R.id.valueQuickIncrements_TXTVIEW);
		valueTapTimeTXTVIEW 		= (TextView) findViewById(R.id.valueTapTime_TXTVIEW);
	}
	
	private void refreshTextViews() {
		valueMaxLevelTXTVIEW.setText(getText(R.string.current_limit_val).toString().replace("%s", 
				String.valueOf( TorchConfig.getBrightnessLimitValue(getApplicationContext())) +
				" /" + String.valueOf(TorchConfig.getAbsMaxBrightness())) );
		
		valueDefaultLevelTXTVIEW.setText(getText(R.string.value_default_level).toString().replace("%s", 
				String.valueOf(Z.getDefaultBrightness(getApplicationContext()) )));
		
		valueQuickIncrementTXTVIEW.setText(getText(R.string.value_quick_increments).toString().replace("%s", 
				String.valueOf(TorchConfig.getIncrementSteps(getApplicationContext()) )));
		
		valueTapTimeTXTVIEW.setText(getText(R.string.value_tap_time).toString().replace("%s",
				String.valueOf(Z.getTapTime(this))));
	}
	
	
	/*******************************************************************************
	 *Activity Question Button Listeners and Alert Dialog Popup Description
	 *******************************************************************************/
	private void questionListener() {
		if(qMaxLimit != null) 		{	questionListener(qMaxLimit);	}	
		if(qDefaultLevel != null) 	{	questionListener(qDefaultLevel);}	
		if(qQuickIncrements != null){	questionListener(qQuickIncrements);	}	
		if(qDoubleTap != null) 		{	questionListener(qDoubleTap);	}	
		if(qRapidTap != null) 		{	questionListener(qRapidTap);	}	
		if(qTapTime != null) 		{	questionListener(qTapTime);	}	
		if(qPersistNotif != null) 	{	questionListener(qPersistNotif);	}	
		if(qMinNotif != null) 		{	questionListener(qMinNotif);	}	
	}
	
	private void questionListener(final ButtonWrapper bw) {
		bw.getButton().setOnClickListener(new View.OnClickListener()  {
			@Override
			public void onClick(View v) {	
				showDescription(bw);
			};
		});
	}

	private void showDescription(ButtonWrapper bw) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
		.setTitle(bw.titleId)
		.setMessage(bw.messageId)
		.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				dialog.dismiss();	
			}
		});
		alertDialog.show();
		
	}	
	/*******************************************************************************
	 *Activity Toggle Button Listeners
	 *******************************************************************************/
	private void toggleListener() {
		if(doubleTapToggle != null)		{	doubleTapListener();	}	
		if(rapidTapToggle != null)		{	rapidTapListener();		}	
		if(persistNotifToggle != null)	{	persistNotifListener();	}	
		if(minNotifToggle != null)		{	minNotifListener();		}	
	}
	
	private void doubleTapListener() {
		doubleTapToggle.setChecked(Z.getDoubleTap(this));
		doubleTapToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getApplicationContext();
				
				if(Z.getDoubleTap(context)) {
					Z.setDoubleTap(context,false);
				}
				else {
					if(Z.getRapidTap(context)){
						Z.setRapidTap(context,false);
						rapidTapToggle.setChecked(false);
					}
					Z.setDoubleTap(context,true);
				}
			}
		});
	}
	
	private void rapidTapListener() {
		rapidTapToggle.setChecked(Z.getRapidTap(this));
		rapidTapToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getApplicationContext();
				if(Z.getRapidTap(context)) {
					Z.setRapidTap(context,false);
				}
				else {
					if(Z.getDoubleTap(context)){
						Z.setDoubleTap(context,false);
						doubleTapToggle.setChecked(false);
					}
					Z.setRapidTap(context,true);
				}
			}
		});
	}
	
	private void persistNotifListener() {
		persistNotifToggle.setChecked(Z.getPersistNotif(this));
		persistNotifToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getApplicationContext();			
				if(Z.getPersistNotif(context)) {	Z.setPersistNotif(context,false);	}
				else {	Z.setPersistNotif(context,true);	}
			}
		});
	}
	
	private void minNotifListener() {
		minNotifToggle .setChecked(Z.getMinimizeNotif(this));
		minNotifToggle .setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getApplicationContext();
				if(Z.getMinimizeNotif(context)) {	Z.setMinimizeNotif(context,false);	}
				else {	Z.setMinimizeNotif(context,true);	}
			}
		});
	}	
	
	
	/*******************************************************************************
	 *Activity Push Button Listeners
	 *******************************************************************************/	
	private void buttonListener() {
		if(setMaxLevelButton != null) {	maxLevelListener();	}	
		if(setDefaultLevelButton != null) {	defaultLevelListener();	}	
		if(setQuickIncButton != null) {	quickIncListener();	}	
		if(setTapTimeButton != null) {	tapTimeListener();	}	
	}	
	
	private void maxLevelListener() {
		setMaxLevelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {	setNewMaxLevel();	}
		});
	}

	private void defaultLevelListener() {
		setDefaultLevelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {	setNewDefaultLevel();	}
		});
	}
	
	private void quickIncListener() {
		setQuickIncButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {	setNewQuickIncrement();	}
		});
	}
	
	private void tapTimeListener() {
		setTapTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {	setNewTapTime();	}
		});
	}
	

	/*******************************************************************************
	 * Push Button Dialogs
	 *******************************************************************************/
	private void setNewMaxLevel() {
		final SeekbarUpdateSetting sus = new SeekbarUpdateSetting(this, 0, 
				TorchConfig.getBrightnessLimitValue(getApplicationContext()), TorchConfig.getAbsMaxBrightness());
		
		sus.create(R.string.set_maximum, R.string.set_max_torch_limit_instructions, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int mAbsMaxVBrightness = sus.progress();
				if(mAbsMaxVBrightness == 0) { mAbsMaxVBrightness = 1; }
				TorchConfig.setBrightnessLimitValue(getApplicationContext(),mAbsMaxVBrightness);
				if(mAbsMaxVBrightness < Z.getDefaultBrightness(getApplicationContext())){
					Z.setDefaultBrightness(getApplicationContext(),mAbsMaxVBrightness);
				}
				refreshTextViews();
			}
		});
	}
	
	private void setNewDefaultLevel() {
		final SeekbarUpdateSetting sus = new SeekbarUpdateSetting(this, 0, 
				Z.getDefaultBrightness(getApplicationContext()), TorchConfig.getAbsMaxBrightness());
		
		sus.create(R.string.default_level, R.string.set_default_level, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int mAbsMaxVBrightness = sus.progress();
				if(mAbsMaxVBrightness == 0) { mAbsMaxVBrightness = 1; }
				Z.setDefaultBrightness(getApplicationContext(),mAbsMaxVBrightness);
				refreshTextViews();
			}
		});
	}
	
	private void setNewQuickIncrement() {
		final SeekbarUpdateSetting sus = new SeekbarUpdateSetting(this, 0, 
				TorchConfig.getIncrementSteps(getApplicationContext()), TorchConfig.getBrightnessLimitValue(getApplicationContext()));
		
		sus.create(R.string.quick_increments, R.string.set_quick_increments, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int x = sus.progress();
				if(x == 0) { x = 1; }
				int max = Z.getDefaultBrightness(getApplicationContext());
				if(x > max){
					Toast.makeText(getApplicationContext(), R.string.quick_increment_to_high, Toast.LENGTH_SHORT).show();
					x = max;
				}					
				TorchConfig.setIncrementSteps(getApplicationContext(),x);
				refreshTextViews();
			}
		});
	}
	
	
	private void setNewTapTime() {
		final SeekbarUpdateSetting sus = new SeekbarUpdateSetting(this, 0, 
				Z.getTapTime(getApplicationContext()), Z.max_tap_time);
		
		sus.create(R.string.tap_time, R.string.set_tap_time, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int x = sus.progress();
				if(x == 0) { x = 100; }		
				Z.setTapTime(getApplicationContext(),x);
				refreshTextViews();
			}
		});
	}
		
	
	
	/*******************************************************************************
	 * Class for making seekbar dialogs to change settings
	 *******************************************************************************/
	private class SeekbarUpdateSetting {
		private Context context = null;
		private View content = null;
		private SeekBar seekBar = null;
		private int min, current, text, title = 0;
		private int max = 1;
		
		/**
		 * 
		 * @param cont = getApplicationContext()
		 * @param min = minimum on seekbar 
		 * @param current = initially set seekbar to this value 
		 * @param max = maximum seekbar value
		 */
		public SeekbarUpdateSetting(Context cont, int min, int current, int max){
			this.context = cont;
			this.content = LayoutInflater.from(context).inflate(R.layout.new_maximum_layout, null);
			this.seekBar = (SeekBar)content.findViewById(R.id.seekbar);
			this.min = min;
			this.current = current;
			this.max = max;

		}
		
		public int progress() {
			return seekBar.getProgress();
		}
		
		/**
		 * 
		 * @param titleId = Alert Dialog Title
		 * @param textId = Alert Dialog Instruction text and Current Progress: %s
		 * @param action = On Click action
		 */
		public void create(int titleId, int textId, DialogInterface.OnClickListener action ) {
			this.title = titleId;
			this.text = textId;
			seekBar.setMax(max);
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				final TextView textView = (TextView)content.findViewById(R.id.instructions_textview);

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					textView.setText(getText(text).toString().replace("%s", String.valueOf(progress)) );
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { }
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { }
			});
			//Initial redraw
			seekBar.setProgress(current);
			
			new AlertDialog.Builder(context)
			.setTitle(title)
			.setView(content)
			.setPositiveButton(R.string.ok, action)
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
				}
			})
			.show();
		}
	}

	@Override
	public void onBackPressed() {
	    setResult(RESULT_OK);
	    finish();
	}
	

}



