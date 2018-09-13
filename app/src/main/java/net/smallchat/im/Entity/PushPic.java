package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import static net.smallchat.im.config.APIConfig.getHeadUrl;


public class PushPic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String path;
	public String bPath;
	public String key;
	public PushPic(int id, String path, String key) {
		super();
		this.id = id;
		this.path = path;
		this.key = key;
	}
	public PushPic() {
		super();
	}
	
	public PushPic(String reString) {
		super();
		if(reString == null || reString.equals("")){
			return;
		}
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("id")){
				this.id = json.getInt("id");
			}
			if(!json.isNull("path")){
				String imgUrl = json.getString("path");
				if(imgUrl!=null && !imgUrl.equals("")){
					this.path = getHeadUrl()+imgUrl;
				}
			}
			if(!json.isNull("bpath")){
				String imgUrl = json.getString("bpath");
				if(imgUrl!=null && !imgUrl.equals("")){
					this.bPath = getHeadUrl()+imgUrl;
				}
			}
			if(!json.isNull("key")){
				this.key = json.getString("key");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
}
