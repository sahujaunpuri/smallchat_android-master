package net.smallchat.im.Entity;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 文件信息
 * @author dongli
 *
 */
public class MessageFile implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public String type;
	public String url;
	public String ext;
	public String filename;
	public long size;
	public String icon;
	public String localPath="";




	public MessageFile(String filename, String ext, String type, int size, String url,String icon) {
		super();
		this.filename = filename;
		this.ext =ext;
		this.type =type;
		this.size =size;
		this.url =url;
		this.icon=icon;

	}

	public MessageFile() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static MessageFile getInfo(String json) {
		try {
            return JSONObject.parseObject(json, MessageFile.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MessageFile info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}
	
}
