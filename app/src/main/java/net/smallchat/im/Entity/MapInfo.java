package net.smallchat.im.Entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import org.json.JSONException;

public class MapInfo implements Serializable{
	private static final long serialVersionUID = -1153484834874L;
	private String city;
	private String addr;
	private String lat;
	private String lng;

	public MapInfo(String city, String addr, String lat, String lng) {
		super();
		this.city = city;
		this.addr = addr;
		this.lat = lat;
		this.lng = lng;
	}

	public MapInfo() {
		super();
	}
	
	public static MapInfo getInfo(String json) {
		try {
			return JSONObject.toJavaObject(JSONObject.parseObject(json),
					MapInfo.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public MapInfo(String reqString) {
		super();
		if(reqString == null || reqString.equals("")){
			return;
		}
		try {
			org.json.JSONObject json = new org.json.JSONObject(reqString);
			if(json!=null ){
				if(!json.isNull("lng")){
					this.lng = json.getString("lng");
				}
				if(!json.isNull("lat")){
					this.lat = json.getString("lat");
				}
				if(!json.isNull("addr")){
					this.addr = json.getString("addr");
				}
				if(!json.isNull("city")){
					this.city = json.getString("city");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String getInfo(MapInfo info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

	public String getCtiy() {
		return city;
	}

	public String getAddr() {
		return addr;
	}

	public String getLat() {
		return lat;
	}

	public String getLng() {
		return lng;
	}

	public void setCtiy(String ctiy) {
		this.city = ctiy;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public void setLon(String lng) {
		this.lng = lng;
	}

	@Override
	public String toString() {
		return "MapInfo [city=" + city + ", addr=" + addr + ", lat=" + lat
				+ ", lng=" + lng + "]";
	}

}