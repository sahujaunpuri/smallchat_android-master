package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class FavoriteItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String uid;
	public String fid;
	public String otherid;
	public String content;
	public long createtime;
	public String headSmall;
	public String nicknaem;
	public int messageType;
	

	public FavoriteItem() {
		super();
	}
	
	public FavoriteItem(String reqString) {
		super();
		try {
			if(reqString == null || reqString.equals("")){
				return;
			}
			JSONObject json = new JSONObject(reqString);
			if(!json.isNull("id")){
				this.id = json.getInt("id");
			}
			this.uid = json.getString("uid");
			this.fid = json.getString("fid");
			this.otherid = json.getString("otherid");
			this.content = json.getString("content");
			if(this.content!=null && !this.content.equals("")){
				JSONObject obj = new JSONObject(this.content);
				if(!obj.isNull("messageType")){
					messageType = obj.getInt("messageType");
				}
			}
			this.createtime = json.getLong("createtime");
			this.headSmall = json.getString("headSmall");
			this.nicknaem = json.getString("nickname");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
