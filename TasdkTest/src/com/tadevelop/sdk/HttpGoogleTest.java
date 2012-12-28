/*
 * Name   HttpGoogleTest.java
 * Author ZhangZhenli
 * Created on 2012-12-27, 下午7:59:56
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk;

import java.io.IOException;
import java.util.List;

import android.os.Debug;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

/**
 * 
 * @author ZhangZhenli
 */
public class HttpGoogleTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mMainActivity;
	private static final int MAX_RESULTS = 3;
	static boolean isFinish = false;
	static final Object blockLock = new Object();

	/**
	 * @param activityClass
	 */
	public HttpGoogleTest() {
		super(MainActivity.class);
	}

	private static final String TAG = "HttpGoogleTest.java";

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		mMainActivity = getActivity();
	}

	/**
	 * {@link com.tadevelop.sdk.MainActivity#onStart()} 的测试方法。
	 */
	public void testHttp() {
		Debug.startMethodTracing("HttpGoogle");
		Thread thread = new Thread(new GoogleHttpTest(), "GoogleHttpGsonTest");
		thread.start();
		synchronized (blockLock) {
			while (!isFinish) {
				try {
					blockLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		Debug.stopMethodTracing();
	}

	public static class GoogleHttpTest implements Runnable {
		private JsonFactory mJsonFactory;

		@Override
		public void run() {
			HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
			HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory();
			mJsonFactory = new GsonFactory();
			for (int i = 0; i < 100; i++) {
				GenericUrl url = new GenericUrl("http://192.168.1.2:8099/rest/Test/get");
				JsonObjectParser parser = new JsonObjectParser(mJsonFactory);
				try {
					HttpRequest httpRequest = httpRequestFactory.buildRequest(HttpMethods.GET, url, null);
					httpRequest.setParser(parser);
					HttpHeaders headers = null;
					httpRequest.setHeaders(headers);
					HttpResponse httpResponse = httpRequest.execute();
					ActivityFeed feed = httpResponse.parseAs(ActivityFeed.class);
					Log.i(TAG, "HttpGoogleTest.GoogleHttpTest.run(): " + i);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			synchronized (blockLock) {
				isFinish = true;
				blockLock.notifyAll();
			}
		}
	}

	/** Feed of Google+ activities. */
	public static class ActivityFeed {

		/** List of Google+ activities. */
		@Key("items")
		private List<Activity> activities;

		public List<Activity> getActivities() {
			return activities;
		}
	}

	/** Google+ activity. */
	public static class Activity extends GenericJson {

		/** Activity URL. */
		@Key
		private String url;

		public String getUrl() {
			return url;
		}

		/** Activity object. */
		@Key("object")
		private ActivityObject activityObject;

		public ActivityObject getActivityObject() {
			return activityObject;
		}
	}

	/** Google+ activity object. */
	public static class ActivityObject {

		/** HTML-formatted content. */
		@Key
		private String content;

		public String getContent() {
			return content;
		}

		/** People who +1'd this activity. */
		@Key
		private PlusOners plusoners;

		public PlusOners getPlusOners() {
			return plusoners;
		}
	}

	/** People who +1'd an activity. */
	public static class PlusOners {

		/** Total number of people who +1'd this activity. */
		@Key
		private long totalItems;

		public long getTotalItems() {
			return totalItems;
		}
	}

	private static void parseResponse(HttpResponse response) throws IOException {
		ActivityFeed feed = response.parseAs(ActivityFeed.class);
		if (feed.getActivities().isEmpty()) {
			System.out.println("No activities found.");
		} else {
			System.out.println("activities found.");
			// if (feed.getActivities().size() == MAX_RESULTS) {
			// System.out.print("First ");
			// }
			// System.out.println(feed.getActivities().size() + " activities found:");
			// for (Activity activity : feed.getActivities()) {
			// System.out.println();
			// System.out.println("-----------------------------------------------");
			// System.out.println("HTML Content: " + activity.getActivityObject().getContent());
			// System.out.println("+1's: " + activity.getActivityObject().getPlusOners().getTotalItems());
			// System.out.println("URL: " + activity.getUrl());
			// System.out.println("ID: " + activity.get("id"));
			// }
		}
	}

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
