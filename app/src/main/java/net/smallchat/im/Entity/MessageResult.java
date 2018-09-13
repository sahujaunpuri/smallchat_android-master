package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageResult implements Serializable{

	private static final long serialVersionUID = -1436465487871L;
	
	public ChatMessage mChatMessage;
	public IMResponseState mState;
	
	public MessageResult(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("state")){
				mState = new IMResponseState(json.getJSONObject("state"));
			}
			
			if(!json.isNull("data")){
				mChatMessage = new ChatMessage(json.getJSONObject("data"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
