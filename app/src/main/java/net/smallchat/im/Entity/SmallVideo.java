package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 名片信息
 * @author dongli
 *
 */
public class SmallVideo implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public String thumb;
    public String url;
    public long size;



	public SmallVideo(String thumb, String url, long size) {
		super();
		this.thumb=thumb;
		this.url=url;
		this.size=size;
	}
	public SmallVideo() {
		super();
	}

	public static SmallVideo getInfo(String json) {
		try {
			return JSONObject.parseObject(json, SmallVideo.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(SmallVideo info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}
	
}
