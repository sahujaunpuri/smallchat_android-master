package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppNews implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public List<AppNewsItem> childList;
	public IMResponseState state;
	public PageInfo pageInfo;
	public AppNews() {
		super();
	}
	public AppNews(String reString) {
		super();
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("data")){
				String ar = json.getString("data");
				if(ar!=null && !ar.equals("") && ar.startsWith("[")){
					JSONArray array = json.getJSONArray("data");
					if(array != null){
						childList = new ArrayList<AppNewsItem>();
						for (int i = 0; i < array.length(); i++) {
							childList.add(AppNewsItem.getInfo(array.getString(i)));
						}
					}
				}
			}
			
			if(!json.isNull("state")){
				state = new IMResponseState(json.getJSONObject("state"));
			}
			
			if(!json.isNull("pageInfo")){
				pageInfo = new PageInfo(json.getJSONObject("pageInfo"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
}
