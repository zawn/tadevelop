/*
 * Name   PushCallback.java
 * Author ZhangZhenli
 * Created on 2012-11-13, 上午11:25:45
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk.mqttv3;

import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 包装MqttCallback回调函数
 * 
 * @author ZhangZhenli
 */
public abstract class PushCallback implements MqttCallback {

	private static final String TAG = "PushCallback.java";
	private static final boolean DEBUG = true;
	public Context mContext;
	private Class<?> mServerClazz;
	private PushMessage mCurrentMessage;

	/**
	 * 构造函数
	 * 
	 * @param context 应用程序上下文
	 * @param serverCls 具体的服务类
	 */
	public PushCallback(PushService context) {
		this.mContext = context;
		mServerClazz = context.getClass();
	}

	@Override
	public void connectionLost(Throwable cause) {
		// This method is called when the connection to the server is lost.
		Log.e(TAG, "PushCallback.connectionLost()");
		cause.printStackTrace();
		// TODO:合理连接丢失重试机制
		final Intent service = new Intent(mContext, mServerClazz);
		service.setAction(PushIntent.CONNECT_LOST);
		mContext.startService(service);
	}

	@Override
	final public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
		// Called when a message arrives from the server.
		if (DEBUG)
			Log.i(TAG, "PushCallback.messageArrived()");
		this.mCurrentMessage = new PushMessage(topic.getName(), message);
		if (DEBUG) {
			String time = new Timestamp(System.currentTimeMillis()).toString();
			Log.i(TAG, "Time:\t" + time + "  Topic:\t" + topic.getName() + "  Message:\t" + new String(message.getPayload(), "UTF-8") + "  QoS:\t"
					+ message.getQos());
		}
		onReceiver(mCurrentMessage);
		Log.i(TAG, "MessageArrived End");
	}

	@Override
	final public void deliveryComplete(MqttDeliveryToken token) {
		// Here use synchronous delivery, by using the token.waitForCompletion() call in the work thread.
		if (DEBUG)
			Log.i(TAG, "PushCallback.deliveryComplete()");
		onComplete(token);
	}

	/**
	 * 接收到消息时候的回调函数
	 * 
	 * @param message 接收到的消息的内容
	 */
	protected abstract void onReceiver(PushMessage message);

	/**
	 * 在发送完消息后回调的函数
	 * 
	 * @param token 发送消息所得到的TOKEN,用于确认消息发送成功
	 * 
	 * @param message 发送的消息
	 */
	protected abstract void onPublish(MqttDeliveryToken token, PushMessage message);

	/**
	 * 在消息发送成功后调用
	 * 
	 * @param token 成功发送的消息的TOKEN,该TOKEN与{@code onPublish}返回的Token对应
	 */
	protected abstract void onComplete(MqttDeliveryToken token);

	/**
	 * 尝试发送消息失败时调用
	 * @param message
	 */
	protected abstract void onError(PushMessage message);
}
