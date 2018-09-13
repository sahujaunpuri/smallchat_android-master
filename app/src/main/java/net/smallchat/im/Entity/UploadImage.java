package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class UploadImage implements Serializable {

	private static final long serialVersionUID = 1L;

	public String  fromId="";  //用户ID
	public String  toId="";  //发送到ID
	public String  fileUrl="";    // 下载地址
	public String  fileName="";   // 文件名称
	public String  fileExt="";   // 文件后缀
	public String  thumbUrl ="";  // 缩略图URL
	public String  imageHeight="";  // 图像高度
	public String  imageWidth="";  // 图像宽度

	public UploadImage(){}


	public static UploadImage convertToObject(String json) {
		try {
			return JSONObject.parseObject(json, UploadImage.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertToJSON(UploadImage obj) {
		String json = JSONObject.toJSON(obj).toString();
		return json;
	}

}
