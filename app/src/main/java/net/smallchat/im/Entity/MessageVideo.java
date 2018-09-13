package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 小视频
 * @author dongli
 *
 */
public class MessageVideo implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public String thumb="";
    public String url="";
    public long size=0;
    public long time=0;
	public String localPath="";



	public MessageVideo(String thumb, String url, long size, int time) {
		super();
		this.thumb=thumb;
		this.url=url;
		this.size=size;
		this.time=time;
	}
	public MessageVideo() {
		super();
	}

	public static MessageVideo getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MessageVideo.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MessageVideo info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}
	
}
