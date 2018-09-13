package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class Voice implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public String time;
	public String url;
	public String messageType;




	public Voice(String time, String url, String messageType) {
		super();
		this.time = time;
		this.url = url;
		this.messageType = messageType;
	}

	public Voice() {
		super();
	}

	public static Voice getInfo(String json) {
		try {
			return JSONObject.parseObject(json, Voice.class);//toJavaObject(JSONObject.parseObject(json),
					//Card.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(Voice info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
