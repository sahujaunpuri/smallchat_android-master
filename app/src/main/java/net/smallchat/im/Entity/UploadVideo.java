package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class UploadVideo implements Serializable {

	private static final long serialVersionUID = 1L;

	public String  fromId="";  //用户ID
	public String  toId="";  //发送到ID
	public String  fileUrl="";    // 下载地址
	public String  fileName="";   // 文件名称
	public String  fileExt="";   // 文件后缀
	public String  thumbUrl="";  // 缩略图URL
    public String  fileSize="";  // 文件大小
	public String  videoTime="";  // 视频时间长度

	public UploadVideo(){}


	public static UploadVideo convertToObject(String json) {
		try {
			return JSONObject.parseObject(json, UploadVideo.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertToJSON(UploadVideo obj) {
		String json = JSONObject.toJSON(obj).toString();
		return json;
	}

}
