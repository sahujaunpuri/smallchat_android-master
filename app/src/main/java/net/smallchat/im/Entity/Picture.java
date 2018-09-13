package net.smallchat.im.Entity;

import java.io.Serializable;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Picture implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String key;
	public String originUrl;
	public String smallUrl;

	
	
	public Picture(String small, String origin) {
		super();
		this.smallUrl = small;
		this.originUrl = origin;
	}

	public Picture() {
		super();
	}

	public static Picture getInfo(String json) {
		try {
			return JSON.parseObject(json, Picture.class);
			/*return JSONObject.toJavaObject(JSONObject.parseObject(json),
					Picture.class);*/
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(Picture info) {
		String json = null;
		try {
			 json =JSONObject.toJSONString(info).toString();//JSON.toJSONString(info);
		} catch (Exception e) {
			e.printStackTrace();
			json = null;
		}
		Log.e("picture",json);
		return json;
	}
	@Override
	public String toString() {
		return "Picture [thumbUrl=" + smallUrl + ", originUrl=" + originUrl + "]";
	}

	
}