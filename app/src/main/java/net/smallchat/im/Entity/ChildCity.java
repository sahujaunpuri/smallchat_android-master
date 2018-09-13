package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class ChildCity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String text;
	public List<Login> userList;
	
	public ChildCity() {
		super();
	}
	public ChildCity(JSONObject json) {
		super();
		try {
			if(json == null || json.equals("")){
				return;
			}
			id = json.getInt("id");
			text = json.getString("City");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	

}
