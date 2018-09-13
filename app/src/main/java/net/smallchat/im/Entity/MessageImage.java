package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class MessageImage implements Serializable {

	private static final long serialVersionUID = 1L;
	public String largeUrl ="";//大图
	public String smallUrl ="";//小图
	public int width=0;
	public int height=0;
	public String localPath="";

	public MessageImage(){}

	public static MessageImage getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MessageImage.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MessageImage info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
