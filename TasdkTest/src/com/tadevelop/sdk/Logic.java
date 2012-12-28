/*
 * Name   Logic.java
 * Author ZhangZhenli
 * Created on 2012-7-24, 下午4:41:48
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author ZhangZhenli
 */
public abstract class Logic<T> {

	protected Handler mHandler;
	protected static Context mContext;
	private String TAG = "Logic";

	/**
	 * 构造函数
	 * 
	 * @param mHandler
	 * @param mContext
	 */
	public Logic(Handler mHandler, Context mContext) {
		this.mHandler = mHandler;
		this.mContext = mContext;
	}

	public void sendHandlerMessage(int msg) {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(msg);
		}
	}

	public void sendHandlerMessage(Message msg) {
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}

	public void log(String msg) {
		Log.i(TAG, msg);
	}

	/**
	 * 逻辑方法执行主体
	 */
	public abstract void run();

	public void onDataObtain(T t) {
		log(t.toString());
	}

	public void onDataError(T t) {
	}
}
