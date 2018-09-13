package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class DeliveryItem implements Serializable{

	private static final long serialVersionUID = -113436545436L;
	
	/**
	 * "deliveryInfoTime": "2013-11-24T16:24:12Z",
        "deliveryInfoContent": "正在配货",
        "deliveryInfoType": 0
	 */
	public String deliveryInfoTime;
	public String deliveryInfoContent;
	public String deliveryInfoType;

	public DeliveryItem(){
		
	}
	
	public DeliveryItem(JSONObject json){
		try {
			if(!json.isNull("deliveryInfoTime")){
				deliveryInfoTime = json.getString("deliveryInfoTime");
			}
			
			if(!json.isNull("deliveryInfoContent")){
				deliveryInfoContent = json.getString("deliveryInfoContent");
			}
			
			if(!json.isNull("deliveryInfoType")){
				deliveryInfoType = json.getString("deliveryInfoType");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
