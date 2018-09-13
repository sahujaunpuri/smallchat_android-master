package net.smallchat.im.Entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MenuList implements Serializable{

	private static final long serialVersionUID = -21746454545461L;
	public IMResponseState mState;
	public List<Menu> mMenuList;
	public PageInfo mPageInfo;

	public MenuList(){}
	public MenuList(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("state")){
				mState = new IMResponseState(json.getJSONObject("state"));
			}
			
			if(mState != null && mState.code == 0){
				if(!json.isNull("data")){
					JSONArray array = json.getJSONArray("data");
					if(array != null && array.length() != 0){
						mMenuList = new ArrayList<Menu>();
						for (int i = 0; i < array.length(); i++) {
							Menu m = new Menu(array.getJSONObject(i));
							mMenuList.add(m);
						}
					}
				}
			}
			if(!json.isNull("pageInfo")){
				mPageInfo = new PageInfo(json.getJSONObject("pageInfo"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
