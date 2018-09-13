package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import org.json.JSONException;

import java.io.Serializable;

public class MessageLocation implements Serializable{
	private static final long serialVersionUID = -1153484834874L;
	public String city="";
	public String address="";
	public Double lat=0.0;
	public Double lng=0.0;

	public MessageLocation(String city, String address, Double lat, Double lng) {
		super();
		this.city = city;
		this.address = address;
		this.lat = lat;
		this.lng = lng;

	}

	public MessageLocation() {
		super();
	}

	public static MessageLocation getInfo(String json) {
		try {
			return JSONObject.toJavaObject(JSONObject.parseObject(json),
					MessageLocation.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public MessageLocation(String reqString) {
		super();
		if(reqString == null || reqString.equals("")){
			return;
		}
		try {
			org.json.JSONObject json = new org.json.JSONObject(reqString);
			if(json!=null ){
				if(!json.isNull("lng")){
					this.lng = json.getDouble("lng");
				}
				if(!json.isNull("lat")){
					this.lat = json.getDouble("lat");
				}
				if(!json.isNull("address")){
					this.address = json.getString("address");
				}
				if(!json.isNull("city")){
					this.city = json.getString("city");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String getInfo(MessageLocation info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}




}