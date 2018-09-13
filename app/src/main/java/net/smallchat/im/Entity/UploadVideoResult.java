package net.smallchat.im.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UploadVideoResult implements Serializable{

	private static final long serialVersionUID = -1436465487871L;

	public UploadVideo data;
	public int code;
	public String msg;

	public static UploadVideoResult convertToObject(String json) {
		try {
			return com.alibaba.fastjson.JSONObject.parseObject(json, UploadVideoResult.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertToJSON(UploadVideoResult obj) {
		String json = com.alibaba.fastjson.JSONObject.toJSON(obj).toString();
		return json;
	}

}
