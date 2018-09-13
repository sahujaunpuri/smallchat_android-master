package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IMProject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public IMResponseState state;
	
	public List<IMProjectItem> childList;

	public IMProject() {
		super();
	}
	
	public IMProject(String reqString) {
		super();
		if(reqString == null || reqString.equals("")){
			return;
		}
		try {
			JSONObject json = new JSONObject(reqString);
			if(!json.isNull("state")){
				this.state = new IMResponseState(json.getJSONObject("state"));
			}
			if(!json.isNull("data")){
				String dataString = json.getString("data");
				if(dataString!=null && !dataString.equals("")){
					JSONArray jsonArray = json.getJSONArray("data");
					if(jsonArray!=null && jsonArray.length()>0){
						childList = new ArrayList<IMProjectItem>();
						for (int i = 0; i < jsonArray.length(); i++) {
							childList.add(new IMProjectItem(jsonArray.getJSONObject(i)));
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
