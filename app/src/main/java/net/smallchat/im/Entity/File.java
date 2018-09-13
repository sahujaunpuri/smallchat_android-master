package net.smallchat.im.Entity;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 文件信息
 * @author dongli
 *
 */
public class File implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public String type;
	public String url;
	public String ext;
	public String filename;
	public long size;




	public File(String filename, String ext,String type, int size,String url) {
		super();
		this.filename = filename;
		this.ext =ext;
		this.type =type;
		this.size =size;
		this.url =url;

	}

	public File() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static File getInfo(String json) {
		try {
            Log.d("FILE","FILE JSON="+json);
            return JSONObject.parseObject(json, File.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(File info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}
	
}
