/*
 * Name   HttpOriginalTest.java
 * Author ZhangZhenli
 * Created on 2012-12-28, 下午4:20:05
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.Debug;
import android.test.ActivityInstrumentationTestCase2;
import cn.mimessage.and.sdk.net.bridge.DefaultJSONListener;
import cn.mimessage.and.sdk.net.bridge.IHttpListener;
import cn.mimessage.and.sdk.net.parser.json.DefaultJSONParser.JSONDataHolder;
import cn.mimessage.and.sdk.net.parser.json.IJSONParseOverListener;
import cn.mimessage.and.sdk.net.request.json.JSONRequest;

import com.google.api.client.util.Key;

/**
 * 
 * @author ZhangZhenli
 */
public class HttpOriginalTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private static MainActivity mMainActivity;
	private static final int MAX_RESULTS = 3;
	private static int isOver = 0;
	private static Object overLock = new Object();

	/**
	 * @param activityClass
	 */
	public HttpOriginalTest() {
		super(MainActivity.class);
	}

	private static final String TAG = "HttpOriginalTest.java";
	private ExecutorService singleThreadExecutor;

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		mMainActivity = getActivity();

	}

	/*
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * {@link com.tadevelop.sdk.MainActivity#onStart()} 的测试方法。
	 */
	public void testHttp() {
		singleThreadExecutor = Executors.newSingleThreadExecutor();
		Debug.startMethodTracing("HttpOriginal");
		isOver = 100;
		for (int i = 0; i < 100; i++) {
			TestThread task = new TestThread();
			singleThreadExecutor.submit(task);
			// // try {
			// // Thread.sleep(1);
			// // } catch (InterruptedException e) {
			// // }
		}
		synchronized (overLock) {
			while (isOver != 0) {
				try {
					overLock.wait(500);
				} catch (InterruptedException e) {
				}
			}
		}
		Debug.stopMethodTracing();
	}

	public static class TestThread implements Runnable {
		private boolean isParse = false;
		private Object parseLock = new Object();

		@Override
		public void run() {
			isParse = false;
			DefaultJSONListener mJSONListener;
			mJSONListener = new DefaultJSONListener(new JSONListener());
			UpgradeRequest upgradeRequest = new UpgradeRequest(mJSONListener, mMainActivity);
			upgradeRequest.httpGet();
			synchronized (parseLock) {
				while (!isParse) {
					try {
						parseLock.wait(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			synchronized (overLock) {
				isOver--;
				overLock.notifyAll();
			}
		}

		public class JSONListener implements IJSONParseOverListener {

			/*
			 * @see cn.mimessage.and.sdk.net.parser.json.IJSONParseOverListener#onParseOver(java.util.Map)
			 */
			@Override
			public void onParseOver(Map<String, JSONDataHolder> jsonParcel) {
				try {
					List<Map<String, JSONDataHolder>> list = jsonParcel.get("items").getList();
					ActivityFeed activityFeed = new ActivityFeed();
					if (list != null) {
						for (int i = 0; i < list.size(); i++) {
							Activity activity = new Activity();
							;
							Map<String, JSONDataHolder> temp = list.get(i);
							activity.setId(temp.get("id").getString());
							activity.setUrl(temp.get("url").getString());
							ActivityObject activityObject = new ActivityObject();
							activityObject.setContent(temp.get("object").getMap().get("content").getString());
							activityObject.setPlusoners((new PlusOners()).setTotalItems(temp.get("object").getMap()
									.get("plusoners").getMap().get("totalItems").getInt()));
							activity.setActivityObject(activityObject);
							activityFeed.activities.add(activity);
						}
					}
					// List<Activity> activities = activityFeed.getActivities();
					// for (Activity activity2 : activities) {
					// System.out.println();
					// System.out.println("-----------------------------------------------");
					// System.out.println("HTML Content: " + activity2.getActivityObject().getContent());
					// System.out.println("+1's: " + activity2.getActivityObject().getPlusOners().getTotalItems());
					// System.out.println("URL: " + activity2.getUrl());
					// System.out.println("ID: " + activity2.getId());
					// }
				} catch (NullPointerException e) {
				}
				synchronized (parseLock) {
					isParse = true;
					parseLock.notifyAll();
				}

			}

			/*
			 * @see cn.mimessage.and.sdk.net.parser.json.IJSONParseOverListener#parserJSONError(int, java.lang.String)
			 */
			@Override
			public void parserJSONError(int errorCode, String why) {
				// TODO 自动生成的方法存根

			}

		}

	}

	public static class UpgradeRequest extends JSONRequest {

		public UpgradeRequest(IHttpListener listener, Context mContext) {
			super(GlobalConfig.getInstance(mContext), listener);
		}

		@Override
		public String getPrefix() {
			return "http://192.168.1.2:8099/";
		}

		@Override
		public String getAction() {
			return "rest/Test/get";
		}

		@Override
		public List<NameValuePair> getPostParams() {
			return null;
		}
	}

	/** Feed of Google+ activities. */
	public static class ActivityFeed {

		/** List of Google+ activities. */
		@Key("items")
		private List<Activity> activities = new ArrayList<HttpOriginalTest.Activity>();

		public List<Activity> getActivities() {
			return activities;
		}
	}

	/** Google+ activity. */
	public static class Activity {

		private String id;

		/**
		 * @return id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param id 要设置的 id
		 */
		public void setId(String id) {
			this.id = id;
		}

		/** Activity URL. */
		@Key
		private String url;

		public String getUrl() {
			return url;
		}

		/**
		 * @param url 要设置的 url
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * @param activityObject 要设置的 activityObject
		 */
		public void setActivityObject(ActivityObject activityObject) {
			this.activityObject = activityObject;
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

		/**
		 * @return plusoners
		 */
		public PlusOners getPlusoners() {
			return plusoners;
		}

		/**
		 * @param plusoners 要设置的 plusoners
		 */
		public void setPlusoners(PlusOners plusoners) {
			this.plusoners = plusoners;
		}

		/**
		 * @param content 要设置的 content
		 */
		public void setContent(String content) {
			this.content = content;
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

		/**
		 * @param totalItems 要设置的 totalItems
		 * @return
		 */
		public PlusOners setTotalItems(long totalItems) {
			this.totalItems = totalItems;
			return this;
		}

		public long getTotalItems() {
			return totalItems;
		}
	}
}
