package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginResult implements Serializable{

	private static final long serialVersionUID = 113454353454L;
	
	public Login mLogin;
	public IMResponseState mState;

	
	public LoginResult(){}
	
	public LoginResult(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			
			if(!json.isNull("data")){
				mLogin = new Login(json.getJSONObject("data"));
			}
			
			if(!json.isNull("state")){
				mState = new IMResponseState(json.getJSONObject("state"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
