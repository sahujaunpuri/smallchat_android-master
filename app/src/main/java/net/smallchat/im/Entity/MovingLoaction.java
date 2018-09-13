package net.smallchat.im.Entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class MovingLoaction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String lat;
	public String lng;
	public String address;
	public String messageType;
	
	


	public MovingLoaction(String lat, String lng, String address,
			String messageType) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.address = address;
		this.messageType = messageType;
	}

	public MovingLoaction() {
		super();
	}

	public static MovingLoaction getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MovingLoaction.class);//toJavaObject(JSONObject.parseObject(json),
					//Card.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MovingLoaction info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
