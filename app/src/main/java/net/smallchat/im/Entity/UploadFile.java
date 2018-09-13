package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class UploadFile implements Serializable {

	private static final long serialVersionUID = 1L;


	public String  fileId="";  //fileId
	public String  fromId="";  //用户ID
	public String  toId="";  //发送到ID
	public String  fileUrl="";    // 下载地址
	public String  fileType="";   // 文件类型  0 TXT 1 PPT 2 DOC 3 EXCEL 4  PDF 5 ZIP 6 RAR 7 exe 8 apk 9 其他
	public String  fileName="";   // 文件名称
	public String  fileExt="";   // 文件后缀
	public String  fileSize="";  // 文件大小

	public UploadFile(){}


	public static UploadFile convertToObject(String json) {
		try {
			return JSONObject.parseObject(json, UploadFile.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String convertToJSON(UploadFile obj) {
		String json = JSONObject.toJSON(obj).toString();
		return json;
	}

}
