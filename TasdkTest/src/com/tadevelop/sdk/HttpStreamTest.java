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
import java.io.InputStream;

import android.os.Debug;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.taveloper.http.test.pojo.ActivityFeed;
import com.taveloper.http.test.pojo.parse.ActivityFeedParse;

/**
 * 
 * @author ZhangZhenli
 */
public class HttpStreamTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mMainActivity;
	private static final int MAX_RESULTS = 3;
	static boolean isFinish = false;
	private static com.fasterxml.jackson.core.JsonFactory factory;
	static final Object blockLock = new Object();

	/**
	 * @param activityClass
	 */
	public HttpStreamTest() {
		super(MainActivity.class);
	}

	private static final String TAG = "HttpGoogleTest.java";

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		factory = new com.fasterxml.jackson.core.JsonFactory();
		mMainActivity = getActivity();
	}

	/**
	 * {@link com.tadevelop.sdk.MainActivity#onStart()} 的测试方法。
	 */
	public void testHttp() {
		Debug.startMethodTracing("HttpParseManual-03", 100 * 1024 * 1024);
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

		@Override
		public void run() {
			HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
			HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory();
			for (int i = 0; i < 5; i++) {
				GenericUrl url = new GenericUrl("http://192.168.1.2:8099/TestAgent/rest/Test/get");
				try {
					HttpRequest httpRequest = httpRequestFactory.buildRequest(HttpMethods.GET, url, null);
					HttpResponse httpResponse = httpRequest.execute();
					InputStream feed = httpResponse.getContent();
					ActivityFeed activityFeed = readJsonStream(feed);
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

		private ActivityFeed readJsonStream(InputStream in) throws JsonParseException, IOException {
			com.fasterxml.jackson.core.JsonParser parser = factory.createJsonParser(in);
			try {
				JsonToken token = parser.getCurrentToken();
				if (token != null) {
					parser.nextToken();
				}
				JsonToken currentToken = parser.getCurrentToken();
				if (currentToken == JsonToken.START_OBJECT) {
					// System.out.println("JsonToken.START_OBJECT");
					return (ActivityFeed) (new ActivityFeedParse()).readJson(parser);
				} else if (currentToken == JsonToken.START_ARRAY) {
					System.out.println("JsonToken.START_ARRAY");
				}
			} finally {
				parser.close();
				in.close();
			}
			return null;
		}
	}

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
