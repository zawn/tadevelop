/*
 * Name   PushConnectivityReceiver.java
 * Author ZhangZhenli
 * Created on 2012-11-7, 下午3:55:08
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk.mqttv3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.tadevelop.sdk.BuildConfig;

/**
 * 
 * @author ZhangZhenli
 */
public class PushConnectivityReceiver extends BroadcastReceiver {

	private static final String TAG = "PushConnectivityReceiver.java";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG) {
			Log.i(TAG, "Start onReceive");
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Log.i(TAG, "CONNECTIVITY_ACTION: " + extras.getString(ConnectivityManager.CONNECTIVITY_ACTION));
				Log.i(TAG, "EXTRA_EXTRA_INFO: " + extras.getString(ConnectivityManager.EXTRA_EXTRA_INFO));
				Log.i(TAG, "EXTRA_NO_CONNECTIVITY: " + extras.getString(ConnectivityManager.EXTRA_NO_CONNECTIVITY));
				Log.i(TAG, "EXTRA_REASON: " + extras.getString(ConnectivityManager.EXTRA_REASON));

			}
		}
		notifyContextService(context);
		if (BuildConfig.DEBUG)
			Log.i(TAG, "End   onReceive");
	}

	public void notifyContextService(final Context context) {
		if (BuildConfig.DEBUG)
			Log.i(TAG, "notifyContextService");
		final Intent service = new Intent(context, PushService.class);
		service.setAction(PushIntent.CONNECT_CHANGE);
		context.startService(service);
	}
}
