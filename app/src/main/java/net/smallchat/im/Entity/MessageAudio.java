package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class MessageAudio implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public int time=0;
	public String url="";
	public String localPath="";
	public int isReadVoice=0;

	public MessageAudio() {
		super();
	}

	public static MessageAudio getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MessageAudio.class);//toJavaObject(JSONObject.parseObject(json),
					//Card.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MessageAudio info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
