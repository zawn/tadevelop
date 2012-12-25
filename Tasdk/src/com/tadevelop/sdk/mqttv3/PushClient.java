/*
 * Name   PushClient.java
 * Author ZhangZhenli
 * Created on 2012-11-13, 上午11:26:38
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk.mqttv3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import android.content.Context;
import android.util.Log;

import com.tadevelop.sdk.BuildConfig;
import com.tadevelop.sdk.util.Utils;

/**
 * 继承自{@link MqttClient} 实现在Android平台上的连接的建立\重连\消息发送,初始化MQTT环境
 * 
 * @author ZhangZhenli
 */
public class PushClient extends MqttClient {

	private static String mServerURI;
	private static String mClientId;
	private static MqttClientPersistence mPersistence;
	private static PushClient mPushClient;
	private static MqttConnectOptions mConnectOptions;
	private static PushCallback mCallback;
	private static int mFailureRecords = 0;

	/**
	 * @param serverURI
	 * @param clientId
	 * @param persistence
	 * @throws MqttException
	 */
	public PushClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
		super(serverURI, clientId, persistence);
	}

	private static final String TAG = "PushClient.java";

	public static PushClient getInstance(Context context, PushCallback callback, PushConfig config) {
		if (mPushClient == null) {
			initMqttClient(context, callback, config);
		}
		return mPushClient;
	}

	public static PushClient getNewInstance(Context context, PushCallback callback, PushConfig config) {
		mPushClient = null;
		mFailureRecords = 0;
		return initMqttClient(context, callback, config);
	}

	private static PushClient initMqttClient(Context context, PushCallback callback, PushConfig config) {
		File logFile = new File(Utils.getDiskFilesDir(context), "mqtt-trace.properties");
		Properties traceProperties = new Properties();
		if (!logFile.exists()) {
			try {
				traceProperties.load(context.getAssets().open("mqtt-trace.properties"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String directory = traceProperties.getProperty("org.eclipse.paho.client.mqttv3.trace.outputName");
			traceProperties.setProperty("org.eclipse.paho.client.mqttv3.trace.outputName", Utils.getDiskFilesDir(context, directory)
					.getAbsolutePath());
			try {
				traceProperties.store(new FileOutputStream(logFile), null);
			} catch (FileNotFoundException e1) {
				Log.e(TAG, "FileNotFoundException");
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		System.setProperty("org.eclipse.paho.client.mqttv3.trace", logFile.getAbsolutePath());
		try {
			traceProperties.load(new FileInputStream(logFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String tmpDir = traceProperties.getProperty("org.eclipse.paho.client.mqttv3.trace.outputName");

		mServerURI = config.getServerUrl();

		try {
			mPersistence = new MqttDefaultFilePersistence(tmpDir);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		}
		// Construct the object that contains connection parameters
		// such as cleansession and LWAT
		mConnectOptions = new MqttConnectOptions();
		mConnectOptions.setCleanSession(false);
		mConnectOptions.setKeepAliveInterval(60);
		mCallback = callback;
		mClientId = config.getClientId();

		try {
			// Construct the MqttClient instance
			mPushClient = new PushClient(mServerURI, mClientId, mPersistence);
			// Set this wrapper as the callback handler
			mPushClient.setCallback(mCallback);
			return mPushClient;
		} catch (MqttException e) {
			e.printStackTrace();
			Log.e(TAG, "Unable to set up client: " + e.toString());
		}
		return null;
	}

	/**
	 * 连接服务器,该方法会在连接失败后尝试重连,最终还是无法连接则抛出异常
	 * 
	 * @throws MqttException
	 */
	public void connectToServer() throws MqttException {
		Log.i(TAG, "Connected to " + mServerURI + " with client ID " + mClientId);
		if (this.isConnected()) {
			try {
				this.disconnect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
		try {
			this.connect(mConnectOptions);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			if (!this.isConnected()) {
				mFailureRecords++;
				if (mFailureRecords > 3) {
					e.printStackTrace();
					throw e;
				} else {
					Log.e(TAG, "Unable to connect to the server, and later try to reconnect..." + mFailureRecords);
					try {
						Thread.sleep(mFailureRecords * 1000);
					} catch (InterruptedException e1) {
					}
					this.connectToServer();
				}
			} else {
				Log.e(TAG, "Have been connected to the server");
				mFailureRecords = 0;
			}
		}
		mFailureRecords = 0;
		try {
			this.subscribe(mClientId + "/#", 2);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		if (BuildConfig.DEBUG) {
			Log.i(TAG, "Successfully connected to the server");
		}
	}

	public void reConnectIfNecessary() {
		Log.i(TAG, "reConnectIfNecessary");
	}

	/**
	 * 检查连接是否有效,无效则重新连接,否则维持现有连接
	 * 
	 * @param type 当前连接类型,{@link android.net.NetworkInfo}.getType()
	 * @throws MqttException
	 */
	public void reconnectIfNecessary(int type) throws MqttException {
		Log.i(TAG, "reConnectIfNecessary");
		// TODO 优化重练逻辑
		// keepAlive();
		reconnect();
	}

	/**
	 * 重新连接服务器
	 * 
	 * @throws MqttException
	 */
	public void reconnect() throws MqttException {
		Log.i(TAG, "reconnect start");
		try {
			if (this.isConnected()) {
				if (BuildConfig.DEBUG)
					Log.i(TAG, "reconnect disconnect start");
				this.disconnect(-1);
				if (BuildConfig.DEBUG)
					Log.i(TAG, "reconnect disconnect end");
			} else {
				Log.i(TAG, "The client is not connected");
			}
		} catch (MqttException e) {
			e.printStackTrace();
		} finally {
			this.connectToServer();
		}
		Log.i(TAG, "reconnect end");
	}

	/**
	 * Performs a single publish<br />
	 * 发布单条消息,注意该消息将不被保留
	 * 
	 * @param topicName
	 * @param qos
	 * @param payload
	 * @return
	 * @throws MqttException
	 */
	public MqttDeliveryToken publish(PushMessage message) throws MqttException {
		return publish(message.getTopicName(), message.toMqttMessage());
	}

	/**
	 * Performs a single publish<br />
	 * 发布单条消息,注意该消息将不被保留
	 * 
	 * @param topicName
	 * @param qos
	 * @param payload
	 * @return
	 * @throws MqttException
	 */
	public MqttDeliveryToken publish(String topicName, MqttMessage message) throws MqttException {
		// Get an instance of the topic
		final MqttTopic topic = this.getTopic(topicName);
		// Publish the message
		String time = new Timestamp(System.currentTimeMillis()).toString();
		Log.i(TAG, "Publishing at: " + time + " to topic \"" + topicName + "\" qos " + message.getQos());
		MqttDeliveryToken token = topic.publish(message);
		mCallback.onPublish(token, new PushMessage(topicName, message));
		// Wait until the message has been delivered to the server
		token.waitForCompletion();
		return token;
	}

	/**
	 * Performs a single publish<br />
	 * 发布单条消息,注意该消息将不被保留
	 * 
	 * @param topicName
	 * @param qos
	 * @param payload
	 * @throws MqttException
	 */
	public void publish(String topicName, int qos, byte[] payload) throws MqttException {
		publish(topicName, qos, payload, false);
	}

	/**
	 * Performs a single publish<br />
	 * 发布单条消息
	 * 
	 * @param topicName the topic to publish to
	 * @param qos the qos to publish at
	 * @param payload the payload of the message to publish
	 * @param retained
	 * @throws MqttException
	 */
	public void publish(String topicName, int qos, byte[] payload, boolean retained) throws MqttException {
		// Get an instance of the topic
		final MqttTopic topic = this.getTopic(topicName);
		// Publish the message
		String time = new Timestamp(System.currentTimeMillis()).toString();
		Log.i(TAG, "Publishing at: " + time + " to topic \"" + topicName + "\" qos " + qos);
		MqttDeliveryToken token = topic.publish(payload, qos, retained);
		// Wait until the message has been delivered to the server
		token.waitForCompletion();
	}

	/**
	 * 主动向服务器发送Ping包,
	 */
	public void keepAlive() {
		Log.i(TAG, "PushClient.keepAlive()");
		throw new UnsupportedOperationException("This method does not implemented");
	}
}
