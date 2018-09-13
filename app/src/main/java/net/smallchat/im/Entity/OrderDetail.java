package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderDetail implements Serializable{

	private static final long serialVersionUID = -1454386741515L;
	public Order mOrder;
	public IMResponseState mState;
	
	public OrderDetail(){}
	
	public OrderDetail(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("data")){
				JSONArray array = json.getJSONArray("data");
				if(array != null && array.length() > 0){
					mOrder = new Order(array.getJSONObject(0));
				}
			}
			
			if(!json.isNull("state")){
				mState = new IMResponseState(json.getJSONObject("state"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
