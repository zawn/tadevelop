/*
 * Name   PushMessage.java
 * Author ZhangZhenli
 * Created on 2012-10-8, 下午8:19:22
 *
 * Copyright (c) 2012 NanJing YiWuXian Network Technology Co., Ltd. All rights reserved
 *
 */
package com.tadevelop.sdk.mqttv3;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 
 * @author ZhangZhenli
 */
public class PushMessage implements Serializable {

	private static final long serialVersionUID = 708456467437832908L;

	private String topicName;
	private String content;
	private int qos = 2;
	private boolean retained = false;
	private boolean dup = false;

	/**
	 * @param topic
	 */
	public PushMessage(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * @param topicName
	 * @param content
	 */
	public PushMessage(String topicName, String content) {
		this(topicName);
		this.content = content;
	}

	public PushMessage(String topicName, MqttMessage message) throws MqttException {
		this(topicName, message.getPayload(), message.getQos());
	}

	/**
	 * @param name
	 * @param string
	 * @param qos2
	 */
	public PushMessage(String topicName, String content, int qos) {
		this(topicName, content);
		this.setQos(qos);
	}

	/**
	 * @param topicName2
	 * @param payload
	 * @param qos
	 */
	public PushMessage(String topicName, byte[] payload, int qos) {
		this(topicName, payload);
		this.setQos(qos);
	}

	/**
	 * @param topicName2
	 * @param payload
	 */
	public PushMessage(String topicName, byte[] payload) {
		this(topicName);
		try {
			this.content = new String(payload, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return topicName
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * @param topicName 要设置的 topicName
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content 要设置的 content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return qos
	 */
	public int getQos() {
		return qos;
	}

	/**
	 * @param qos 要设置的 qos
	 */
	public void setQos(int qos) {
		this.qos = qos;
	}

	/**
	 * @return retained
	 */
	public boolean isRetained() {
		return retained;
	}

	/**
	 * @param retained 要设置的 retained
	 */
	public void setRetained(boolean retained) {
		this.retained = retained;
	}

	/**
	 * @return dup
	 */
	public boolean isDup() {
		return dup;
	}

	/**
	 * @param dup 要设置的 dup
	 */
	public void setDup(boolean dup) {
		this.dup = dup;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (dup ? 1231 : 1237);
		result = prime * result + qos;
		result = prime * result + (retained ? 1231 : 1237);
		result = prime * result + ((topicName == null) ? 0 : topicName.hashCode());
		return result;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PushMessage other = (PushMessage) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (dup != other.dup)
			return false;
		if (qos != other.qos)
			return false;
		if (retained != other.retained)
			return false;
		if (topicName == null) {
			if (other.topicName != null)
				return false;
		} else if (!topicName.equals(other.topicName))
			return false;
		return true;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PushMessage [topicName=").append(topicName).append(", content=").append(content)
				.append(", qos=").append(qos).append(", retained=").append(retained).append(", dup=").append(dup)
				.append("]");
		return builder.toString();
	}

	public MqttMessage toMqttMessage() {
		MqttMessage m = new MqttMessage();
		try {
			m.setPayload(this.content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		m.setQos(this.qos);
		m.setRetained(this.retained);
		return m;

	}

}
