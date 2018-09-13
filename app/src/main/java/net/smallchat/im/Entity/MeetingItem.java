package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class MeetingItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int id;
	public String uid;
	public String metName;
	public String metSmallLog;
	public String metTopic;
	public long metStartTime;
	public long metEndTime;
	public long createtime;
	public String role; // 1-登陆用户创建
	public String creatorName;
	public int memberCount; //参会人数
	public String metLargeLog;
	public int isjoin; // 1-加入该会议
	public int unread; //未读消息数
	public int applyCount;//申请人数

	public String searchContent;//保存搜索的内容
	
	public IMResponseState status;

	public MeetingItem() {
		super();
	}

	public MeetingItem(JSONObject obj) {
		super();
		init(obj);
		
	}

	private void init(JSONObject obj){
		if(obj == null){
			return;
		}
		try {
			this.id = obj.getInt("id");
			this.uid = obj.getString("uid");
			this.metName = obj.getString("name");
			this.metSmallLog = obj.getString("logo");
			this.metTopic = obj.getString("content");
			this.metStartTime = obj.getLong("start");
			this.metEndTime = obj.getLong("end");
			this.createtime = obj.getLong("createtime");
			this.role = obj.getString("role");
			this.creatorName = obj.getString("creator");
			this.memberCount = obj.getInt("memberCount");
			this.metLargeLog = obj.getString("logolarge");
			this.isjoin = obj.getInt("isjoin");
			this.applyCount = obj.getInt("applyCount");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public MeetingItem (String reqString){
		if(reqString == null || reqString.equals("")){
			return;
		}
		try {
			JSONObject json = new JSONObject(reqString);
			if(!json.isNull("data")){

				JSONObject obj = json.getJSONObject("data");
				if(obj != null){
					init(obj);
				}
			}

			if(!json.isNull("state")){
				this.status = new IMResponseState(json.getJSONObject("state"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
