package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class TopicItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String content;
	public int hot;
	public TopicItem() {
		super();
	}
	public TopicItem(int id, String content, int hot) {
		super();
		this.id = id;
		this.content = content;
		this.hot = hot;
	}
	
	public TopicItem(String jsonObj) {
		try {
			if(jsonObj == null || jsonObj.equals("")){
				return;
			}
			JSONObject json = new JSONObject(jsonObj);
			this.id = json.getInt("id");
			this.content = json.getString("content");
			this.hot = json.getInt("hot");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
}
