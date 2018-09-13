package net.smallchat.im.Entity;

import java.io.Serializable;


import org.json.JSONException;
import org.json.JSONObject;

public class VisitEntity implements Serializable{

	/**
	 * {"data":{"visit":"3","count":93,"interactive":0},"state":{"code":0,"msg":"","debugMsg":""}}
	 */
	private static final long serialVersionUID = 1L;
	
	public int visit;
	public int count;
	public int interactive=0;
	public IMResponseState state;
	public VisitEntity() {
		super();
	}
	public VisitEntity(String reqString) {
		super();
		if(reqString == null || reqString.equals("")){
			return;
		}
		try {
			JSONObject json = new JSONObject(reqString);
			if(!json.isNull("data")){
				JSONObject childObj = json.getJSONObject("data");
				this.visit = childObj.getInt("visit");
				this.count = childObj.getInt("count");
				this.interactive = childObj.getInt("interactive");
			}
			if(!json.isNull("state")){
				state = new IMResponseState(json.getJSONObject("state"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
