package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class VersionInfo implements Serializable{

	private static final long serialVersionUID = -1157875456456L;
	public IMResponseState mState;
	public Version mVersion;
	
	public VersionInfo(){
		
	}
	
	public VersionInfo(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("data")){
				mVersion = new Version(json.getJSONObject("data"));
			}
			
			if(!json.isNull("state")){
				mState = new IMResponseState(json.getJSONObject("state"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
