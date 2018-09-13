package net.smallchat.im.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.IMCommon;

public class NewFriendItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String phone;
	public String loginId;
	public String uid;
	public String name;
	public int verify;
	public String headSmall;
	public int type; //0-添加 1-已添加 2-等待验证
	public int fromtype;//0-系统推送 1-好有申请
	public String contactName;
	public int colorBgtype =1 ;
	
	public NewFriendItem() {
		super();
	}
	public NewFriendItem(JSONObject json) {
		super();
		try {
			if(json ==null || json.equals("")){
				return;
			}
			loginId = IMCommon.getUserId(ChatApplication.getInstance());
			if(!json.isNull("phone")) {
				phone = json.getString("phone");
			}
			if(!json.isNull("type")) {
				type = json.getInt("type");
			}
			if(!json.isNull("uid")) {
				uid = json.getString("uid");
			}
			if(!json.isNull("nickname")) {
				name = json.getString("nickname");
			}
			if(!json.isNull("name")){
				name = json.getString("name");
			}

			String head = "";
			if(!json.isNull("headSmall")) {
				head =json.getString("headSmall");
			}
			if(head!=null && !head.equals("")){
				headSmall = /*IMInfo.HEAD_URL+*/head;
			}
			if(!json.isNull("verify")) {
				verify = json.getInt("verify");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public NewFriendItem(String phone, String uid, String name,
			String headSmall, int type, String contactName,String loginId,
			int fromType) {
		super();
		this.loginId = loginId;
		this.phone = phone;
		this.uid = uid;
		this.name = name;
		this.headSmall = headSmall;
		this.type = type;
		this.contactName = contactName;
		this.fromtype = fromType;
	}
	
	
	
	
	
}
