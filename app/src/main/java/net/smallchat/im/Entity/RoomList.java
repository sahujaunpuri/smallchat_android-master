package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomList implements Serializable{

	private static final long serialVersionUID = 115645454645L;
	
	public List<Room> mRoomList;
	public IMResponseState mState;
	
	public RoomList(){}
	
	public RoomList(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("data")){
				String jsonData = json.getString("data");
				if(jsonData!=null && !jsonData.equals("")
						&& jsonData.startsWith("[")){
					JSONArray array = json.getJSONArray("data");
					if(array != null && array.length() != 0){
						mRoomList = new ArrayList<Room>();
						for (int i = 0; i < array.length(); i++) {
							mRoomList.add(new Room(array.getJSONObject(i)));
						}
					}
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
