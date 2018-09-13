package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class DeliveryInfo implements Serializable{

	private static final long serialVersionUID = -14564545455L;
	
	public Delivery mDelivery;
	public IMResponseState mState;
	
	public DeliveryInfo(){
		
	}
	
	public DeliveryInfo(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("data")){
				mDelivery = new Delivery(json.getJSONObject("data"));
			}
			
			if(!json.isNull("state")){
				mState = new IMResponseState(json.getJSONObject("state"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
