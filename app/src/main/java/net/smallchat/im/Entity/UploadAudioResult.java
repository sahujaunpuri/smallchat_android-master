package net.smallchat.im.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UploadAudioResult implements Serializable{

	private static final long serialVersionUID = -1436465487871L;

	public UploadAudio data;
	public int code;
	public String msg;

	public static UploadAudioResult convertToObject(String json) {
		try {
			return com.alibaba.fastjson.JSONObject.parseObject(json, UploadAudioResult.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertToJSON(UploadAudioResult obj) {
		String json = com.alibaba.fastjson.JSONObject.toJSON(obj).toString();
		return json;
	}

}
