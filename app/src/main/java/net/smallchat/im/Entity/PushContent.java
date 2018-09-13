package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PushContent implements Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String uid;
	public String title;
	public String content;
	public long createtime;
	public List<PushPic> picList;
	public PushContent() {
		super();}

	public PushContent(String reqString) {
		super();
		if(reqString == null || reqString.equals("")){
			return;
		}
		try {
			JSONObject json = new JSONObject(reqString);
			this.uid = json.getString("uid");
			this.title = json.getString("title");
			this.content = json.getString("content");
			this.createtime = json.getLong("createtime");
			if(!json.isNull("pic")){
				String picContent = json.getString("pic");
				if(picContent!=null && !picContent.equals("")){
					picList = new ArrayList<PushPic>();
					JSONArray array = json.getJSONArray("pic");
					if (array!=null && array.length()>0) {
						for (int i = 0; i < array.length(); i++) {
							picList.add(new PushPic(array.getString(i)) );
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
}
