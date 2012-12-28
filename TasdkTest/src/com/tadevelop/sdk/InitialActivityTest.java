/*
 * Name   InitialActivityTest.java
 * Author ZhangZhenli
 * Created on 2012-12-25, 下午3:28:39
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk;

import java.sql.Timestamp;

import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;

import com.tadevelop.sdk.app.InitialActivity;

/**
 * 
 * 
 * @author ZhangZhenli
 */
public class InitialActivityTest extends ActivityInstrumentationTestCase2<InitialActivity> {

	private InitialActivity mActivity;
	private Activity mMainActivity;
	private ActivityMonitor mMonitor;

	/**
	 * @param activityClass
	 */
	public InitialActivityTest() {
		super(InitialActivity.class);
	}

	private static final String TAG = "InitialActivityTest.java";

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		Log.i(TAG, "InitialActivityTest.setUp() : Begin");
		// TODO 自动生成的方法存根
		super.setUp();
		// 注册最开始的活动并运行
		// 在页面还没有跳转前,注册新活动的监视
		mMonitor = getInstrumentation().addMonitor(MainActivity.class.getName(), null, false);
		if (mActivity != null) {
			mActivity.finish();
			setActivity(null);
		}

		mActivity = getActivity();
		Log.i(TAG, "InitialActivityTest.setUp() : End");
	}

	public void testOnHandleIntent() {
		Log.i(TAG, "InitialActivityTest.testOnHandleIntent(): 1");
		// 等待活动开始
		mMainActivity = getInstrumentation().waitForMonitorWithTimeout(mMonitor, 5);
		assertNotNull(mMainActivity);
		assertFalse(mActivity.isFinishing());
		// getInstrumentation().removeMonitor(mMonitor);
		ActivityMonitor mMonitor2 = getInstrumentation().addMonitor(Activity2.class.getCanonicalName(), null, false);
		assertFalse(mMainActivity.isFinishing());
		Log.i(TAG, "1__  " + new Timestamp(System.currentTimeMillis()).toString());
		Log.i(TAG, "InitialActivityTest.testOnHandleIntent():" + Thread.currentThread().getName());
		// TouchUtils.clickView(InitialActivityTest.this, mMainActivity.findViewById(R.id.switch_to_Activity2));
		mMainActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, "2__  " + new Timestamp(System.currentTimeMillis()).toString());
				Thread.currentThread().getName();
				Log.i(TAG, "InitialActivityTest.testOnHandleIntent().新建 Runnable() {...}.run():"
						+ Thread.currentThread().getName());
				InitialActivity.switchActivity(mMainActivity, Activity2.class);
			}
		});
		Log.i(TAG, "3__  " + new Timestamp(System.currentTimeMillis()).toString());
		Activity activity2 = getInstrumentation().waitForMonitor(mMonitor2);
		assertNotNull(activity2);
		assertFalse(activity2.isFinishing());
		assertTrue(mMainActivity.isFinishing());
		assertTrue(mActivity.isTaskRoot());
		Log.i(TAG, "4__  " + new Timestamp(System.currentTimeMillis()).toString());
		// ActivityMonitor mMonitor3 = getInstrumentation().addMonitor(InitialActivity.class.getCanonicalName(), null,
		// true);
		sendKeys(KeyEvent.KEYCODE_BACK);
		// getInstrumentation().waitForMonitor(mMonitor3);
		getInstrumentation().waitForIdleSync();
		assertTrue(activity2.isFinishing());
	}

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		Log.i(TAG, "InitialActivityTest.tearDown()");
	}
}
