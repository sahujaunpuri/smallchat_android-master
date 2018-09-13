package net.smallchat.im.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UploadImageResult implements Serializable{

	private static final long serialVersionUID = -1436465487871L;

	public UploadImage data;
	public int code;
	public String msg;

	public static UploadImageResult convertToObject(String json) {
		try {
			return com.alibaba.fastjson.JSONObject.parseObject(json, UploadImageResult.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertToJSON(UploadImageResult obj) {
		String json = com.alibaba.fastjson.JSONObject.toJSON(obj).toString();
		return json;
	}
}
