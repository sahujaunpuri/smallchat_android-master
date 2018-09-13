package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class UploadAudio implements Serializable {

	private static final long serialVersionUID = 1L;

	public String  fromId="";  //用户ID
	public String  toId="";  //发送到ID
	public String  fileUrl="";    // 下载地址
	public String  fileName="";   // 文件名称
	public String  fileExt="";   // 文件后缀
	public String  fileSize="";  // 文件大小
	public String  audioTime="";  // 语音时间长度

	public UploadAudio(){}


	public static UploadAudio convertToObject(String json) {
		try {
			return JSONObject.parseObject(json, UploadAudio.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertToJSON(UploadAudio obj) {
		String json = JSONObject.toJSON(obj).toString();
		return json;
	}

}
