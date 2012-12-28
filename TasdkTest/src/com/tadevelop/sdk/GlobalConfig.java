/*
 * Name   GlobalConfig.java
 * Author ZhangZhenli
 * Created on 2012-12-15, 下午6:35:50
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk;

import org.apache.http.HttpHost;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import cn.mimail.sdk.util.Utils;
import cn.mimessage.and.sdk.net.YiYouHttpConnectionFactory;
import cn.mimessage.and.sdk.profile.Config;
import cn.mimessage.and.sdk.profile.DeviceInfos;
import cn.mimessage.and.sdk.sdcard.DefaultPersistentStore;
import cn.mimessage.and.sdk.sdcard.IPersistentStore;
import cn.mimessage.and.sdk.thread.IThreadPool;
import cn.mimessage.and.sdk.thread.ThreadPool;
import cn.mimessage.and.sdk.util.audio.AudioPlayer;
import cn.mimessage.and.sdk.util.audio.IAudioPlayer;
import cn.mimessage.and.sdk.util.log.LogX;

/**
 * 
 * @author ZhangZhenli
 */
public class GlobalConfig extends Config {

	private static final String TAG = "GlobalConfig.java";

	private String url = "42.121.4.114/";

	// 系统参数配置区域
	private static final int THREADS_IN_THREADPOOL = 10;
	public static final String VERSION = "0.0.1"; // 当前软件版本号
	public String http_prefix = "http://" + url;
	public String https_prefix = "https://" + url;
	public String http_image_catentries_prefix = "http://image3.suning.cn/content/catentries/";

	// 系统配置中组件定义区域
	private static Context mContext;
	private static Config instance;

	private IAudioPlayer mAudioPlayer;
	private YiYouHttpConnectionFactory connectionFactory;
	private IPersistentStore persistentStore;
	private IThreadPool mThreadPool;
	private final int pixelsPerInch = 1;
	private HttpHost mProxyHost;

	public GlobalConfig(Context context) {
		if (instance != null) {
			((GlobalConfig) instance).clear();
		}

		mContext = context;

		// if (mContext != null) {
		// TelephonyManager phoneMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		// DeviceInfos.CARRIER = phoneMgr.getSimOperator();
		// DeviceInfos.PHONE_NUMBER = phoneMgr.getLine1Number();
		// DeviceInfos.SDK_VERSION = Build.VERSION.SDK;
		// DeviceInfos.MODEL = Build.MODEL;
		// DeviceInfos.CPU_ABI = Build.CPU_ABI;
		// DeviceInfos.DEVICE = Build.DEVICE;
		// DeviceInfos.BRAND = Build.BRAND;
		// DeviceInfos.HARDWARE_INFO = Utils.getCpuFrequency();
		// }

//		showDeviceInfo();

		connectionFactory = new YiYouHttpConnectionFactory(this);
		mThreadPool = new ThreadPool(THREADS_IN_THREADPOOL);
		// persistentStore = new DefaultPersistentStore();
		// // mDBHelper = new DBHelper(mContext);
		// mAudioPlayer = new AudioPlayer(mContext);
		// // try {
		// // mDBHelper.getWritableDatabase();
		// // } catch (SQLiteException e) {
		// // e.printStackTrace();
		// // }
		// pixelsPerInch = mContext.getResources().getDisplayMetrics().densityDpi;
		setConfig(this);
	}

	public void clear() {
		if (mThreadPool != null) {
			mThreadPool.terminateAllThread();
		}
		if (connectionFactory != null && connectionFactory.getClient() != null) {
			connectionFactory.getClient().close();
		}
	}

	public static void setConfig(GlobalConfig config) {
		instance = config;
	}

	public void enableProxy(String host, int port) {
		final HttpHost h = new HttpHost(host, port);
		mProxyHost = h;
		connectionFactory.setProxy(h);
	}

	public void disableProxy() {
		mProxyHost = null;
		connectionFactory.setProxy(null);
	}

	public boolean isViaProxy() {
		return mProxyHost == null;
	}

	public static GlobalConfig getInstance(Context context) {
		if (instance == null) {
			new GlobalConfig(context);
		}
		return (GlobalConfig) instance;
	}

	public static String getPlatformID() {
		String manufacturer = Build.MANUFACTURER;
		String device = Build.DEVICE;
		String model = Build.MODEL;
		StringBuilder platformID = new StringBuilder().append(manufacturer.replace('-', '_'))
				.append(device.replace('-', '_')).append(model.replace('-', '_'));
		return platformID.toString();
	}

	@Override
	public int getPixelsPerInch() {
		return pixelsPerInch;
	}

	@Override
	public double getScreenDensityScale() {
		return mContext.getResources().getDisplayMetrics().density;
	}

	@Override
	public String getVersion() {
		return "0.0.0";
	}

	@Override
	public String getNetworkType() {
		NetworkInfo networkInfo = ((ConnectivityManager) mContext.getSystemService("connectivity"))
				.getActiveNetworkInfo();
		String type = "Unknown";
		if (networkInfo != null) {
			switch (networkInfo.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				type = "WiFi";
				break;
			case ConnectivityManager.TYPE_MOBILE:
				switch (((TelephonyManager) mContext.getSystemService("phone")).getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
					type = "GPRS";
					break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					type = "UMTS";
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					type = "EDGE";
				default:
					break;
				}
				break;
			default:
				type = "Unknown";
			}
		}
		return type;
	}

	@Override
	public IPersistentStore getPersistentStore() {
		return persistentStore;
	}

	@Override
	public YiYouHttpConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public HttpHost getProxyHost() {
		return mProxyHost;
	}

	// public DBHelper getDBHelper() {
	// return mDBHelper;
	// }

	public Context getContext() {
		return mContext;
	}

	public IThreadPool getThreadPool() {
		return mThreadPool;
	}

	public IAudioPlayer getAudioPlayer() {
		return mAudioPlayer;
	}

	// -------------------Ignore Area-------------------
	private void showDeviceInfo() {
		LogX.v(this, "*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***");
		LogX.d(this, "CARRIER      : " + DeviceInfos.CARRIER);
		LogX.d(this, "PHONE_NUMBER : " + DeviceInfos.PHONE_NUMBER);
		LogX.d(this, "SDK_VERSION  : " + DeviceInfos.SDK_VERSION);
		LogX.d(this, "MODEL        : " + DeviceInfos.MODEL);
		LogX.d(this, "CPU_ABI      : " + DeviceInfos.CPU_ABI);
		LogX.d(this, "DEVICE       : " + DeviceInfos.DEVICE);
		LogX.d(this, "BRAND        : " + DeviceInfos.BRAND);
		LogX.d(this, DeviceInfos.HARDWARE_INFO);
		LogX.v(this, "*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***");
	}
}
