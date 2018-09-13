package net.smallchat.im.Entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import org.json.JSONException;

public class MovingPic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String urllarge;
	public String urlsmall;
	public String messageType;
	
	
	
	public MovingPic(String urlsmall, String messageType) {
		super();
		this.urlsmall = urlsmall;
		this.messageType = messageType;
	}
	
	
	

	public MovingPic(String urllarge, String urlsmall, String messageType) {
		super();
		this.urllarge = urllarge;
		this.urlsmall = urlsmall;
		this.messageType = messageType;
	}




	public MovingPic() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MovingPic(String json) {
		super();
		if(json == null || json.equals("")){
			return;
		}
		
		try {
			org.json.JSONObject parentJson = new org.json.JSONObject(json);
			if(!parentJson.isNull("messageType")){
				messageType = parentJson.getString("messageType");
			}
			if(!parentJson.isNull("fileUrl")){
				urllarge = parentJson.getString("fileUrl");
			}
			
			if(!parentJson.isNull("thumbUrl")){
				urlsmall = parentJson.getString("thumbUrl");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static MovingPic getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MovingPic.class);//toJavaObject(JSONObject.parseObject(json),
					//Card.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MovingPic info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
