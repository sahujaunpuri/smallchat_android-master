package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Meeting implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public IMResponseState status;
	public PageInfo pageInfo;
	public List<MeetingItem> childList;


	public Meeting() {
		super();
	}

	public Meeting(String reString) {
		super();
		try {
			JSONObject parentObj = new JSONObject(reString);
			if(!parentObj.isNull("data")){
				
				JSONArray childJson = parentObj.getJSONArray("data");
				if(childJson != null && childJson.length()>0){
					childList = new ArrayList<MeetingItem>();
					for (int i = 0; i<childJson.length(); i++) {
						childList.add(new MeetingItem(childJson.getJSONObject(i)));
					}
				}
				


			}

			if(!parentObj.isNull("state")){
				this.status = new IMResponseState(parentObj.getJSONObject("state"));
			}
			if(!parentObj.isNull("pageInfo")){
				this.pageInfo = new PageInfo(parentObj.getJSONObject("pageInfo"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
