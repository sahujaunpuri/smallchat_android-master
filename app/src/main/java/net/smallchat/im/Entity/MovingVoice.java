package net.smallchat.im.Entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class MovingVoice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String time;
	public String url;
	public String messageType;
	
	


	public MovingVoice(String time, String url, String messageType) {
		super();
		this.time = time;
		this.url = url;
		this.messageType = messageType;
	}

	public MovingVoice() {
		super();
	}

	public static MovingVoice getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MovingVoice.class);//toJavaObject(JSONObject.parseObject(json),
					//Card.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MovingVoice info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
