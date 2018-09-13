package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Version implements Serializable{

	private static final long serialVersionUID = 19874543545465L;
	/**
	 * "tid":"6","version":"3","url":"http://localhost/dinodirect/Public/apk/dalong_V1.5.apk","discription":"dvvdd","type":"OED"
	 */
	public String version;
	public String downloadUrl;
	public String discription;
	public boolean hasNewVersion = false;
	
	public Version(){}
	
	public Version(JSONObject json){
		try {
			
			if(!json.isNull("currVersion")){
				version = json.getString("currVersion");
			}
			
			if(!json.isNull("url")){
				downloadUrl = json.getString("url");
			}
			
			if(!json.isNull("description")){
				discription = json.getString("description");
			}
			
			if(!json.isNull("hasNewVersion")){
				hasNewVersion = json.getInt("hasNewVersion") == 1 ? true : false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
