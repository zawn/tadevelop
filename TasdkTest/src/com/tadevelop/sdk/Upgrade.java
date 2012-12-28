package com.tadevelop.sdk;

import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.mimessage.and.sdk.net.bridge.DefaultJSONListener;
import cn.mimessage.and.sdk.net.bridge.IHttpListener;
import cn.mimessage.and.sdk.net.parser.json.DefaultJSONParser.JSONDataHolder;
import cn.mimessage.and.sdk.net.parser.json.IJSONParseOverListener;
import cn.mimessage.and.sdk.net.request.json.JSONRequest;

public class Upgrade extends Logic implements IJSONParseOverListener {

	private static final String TAG = "Upgrade.java";

	private DefaultJSONListener mJSONListener;

	public Upgrade(Handler mHandler, Context mContext) {
		super(mHandler, mContext);
		mJSONListener = new DefaultJSONListener(this);
	}

	@Override
	public void onParseOver(Map<String, JSONDataHolder> jsonParcel) {
		try {
			List<Map<String, JSONDataHolder>> list = jsonParcel.get("version").getList();
			UpgradePojo upgradePojo = null;
			if (list != null) {
				Map<String, JSONDataHolder> temp = list.get(0);
				upgradePojo = new UpgradePojo();
				upgradePojo.setVersion(temp.get("version").getInt());
				upgradePojo.setUrl(temp.get("url").getString());
				upgradePojo.setForces(temp.get("forces").getInt());
				upgradePojo.setIntro(temp.get("intro").getString());
				Log.i(TAG, upgradePojo.toString());
				Bundle data = new Bundle();
				data.putSerializable("versionInfo", upgradePojo);
				Message msg = new Message();
				msg.setData(data);
				msg.what = 0;
				sendHandlerMessage(msg);
			}
		} catch (NullPointerException e) {
			int error = (int) jsonParcel.get("error").getInt();
			if (error == 1000000001) {
				// 版本号错误
				sendHandlerMessage(1);
			}
		}
	}

	@Override
	public void parserJSONError(int errorCode, String why) {
		sendHandlerMessage(2);
	}

	@Override
	public void run() {
		UpgradeRequest upgradeRequest = new UpgradeRequest(mJSONListener);
		upgradeRequest.httpGet();
	}

	public static class UpgradeRequest extends JSONRequest {

		public UpgradeRequest(IHttpListener listener) {
			super(GlobalConfig.getInstance(mContext), listener);
		}

		@Override
		public String getPrefix() {
			return GlobalConfig.getInstance(mContext).http_prefix;
		}

		@Override
		public String getAction() {
			return ServerUrl.GET_UPGRADE + GlobalConfig.VERSION;
		}

		@Override
		public List<NameValuePair> getPostParams() {
			return null;
		}
	}
}
