package com.tadevelop.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.tadevelop.sdk.app.InitialActivity;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity.java";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	/*
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(TAG, "MainActivity.onNewIntent()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/*
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "MainActivity.onRestart()");
	}

	/*
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "MainActivity.onStart()");
	}

	/*
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "MainActivity.onResume()");
	}

	/*
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "MainActivity.onPause()");
	}

	/*
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "MainActivity.onStop()");
	}

	/*
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "MainActivity.onDestroy()");
	}

	/*
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.i(TAG, "MainActivity.onBackPressed()");
	}

	public void onButtonClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.switch_to_Activity2:
			Log.i(TAG, "MainActivity.onButtonClick() switch_to_Activity2");
			InitialActivity.switchActivity(this, Activity2.class);
			Log.i(TAG, "MainActivity.onButtonClick() switch_to_Activity2: End");
			break;
//		case R.id.activity5_button_start_a1:
//			intent = new Intent(Activity5.this, Activity1.class);
//			startActivity(intent);
//			break;
//		case R.id.activity5_button_satart_a4:
//			intent = new Intent(Activity5.this, Activity4.class);
//			startActivity(intent);
//			break;
//		case R.id.activity5_button_switch_2:
//			MiTask.switchActivity(this, Activity2.class);
//			break;
//		case R.id.activity5_button_manual_clear:
//			intent = new Intent(getApplicationContext(), MiTask.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			intent.putExtra(MiTask.CURRENT_LAUNCH_ACTIVITY, MiTask.class);
//			startActivity(intent);
//			break;
		default:
			Log.e(TAG, "Activity5.onButtonClick() Did not match the Id!");
			break;
		}
	}

}
