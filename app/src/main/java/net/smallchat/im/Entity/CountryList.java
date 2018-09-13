package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

public class CountryList implements Serializable{

	private static final long serialVersionUID = 1946436545154L;
	
	public List<Country> mCountryList;
	public IMResponseState mState;
	public PageInfo mPageInfo;

	public CountryList(){}
	
	public CountryList(String reString){
		try {
			if(reString == null || reString.equals("")){
				return;
			}
			//JSONObject json = new JSONObject(reString);
			/*if(!json.isNull("data")){*/
				JSONArray array = new JSONArray(reString);
				if(array != null){
					mCountryList = new ArrayList<Country>();
					for (int i = 0; i < array.length(); i++) {
						mCountryList.add(new Country(array.getJSONObject(i)));
						
					}
				}
		/*	}*/
		/*	
			if(!json.isNull("state")){
				state = new IMResponseState(json.getJSONObject("state"));
			}
			
			if(!json.isNull("pageInfo")){
				mPageInfo = new PageInfo(json.getJSONObject("pageInfo"));
			}*/
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
}
