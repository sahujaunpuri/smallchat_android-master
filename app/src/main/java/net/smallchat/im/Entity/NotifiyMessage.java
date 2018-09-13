package net.smallchat.im.Entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * 功能： 系统通知信息 <br />
 * 	客户端收到的通知格式
	{"type":"1","content":"0","sent":"{xxxx}"}
	type 信息类型：
	1-系统消息，2-好友申请，3-申请查看资料，4-被评论,5-查看资料,6-删除好友
	content 消息内容：针对系统消息  其余5种类型没有值
	sent  发送人 用户信息.
 * 日期：2013-5-29<br />
 * 地点：无穷大软件<br />
 * 版本：ver 1.0<br />
 * 
 * guoxin
 * @since
 */
public class NotifiyMessage extends IMMessage {
	
	/** 拒绝添加好友 */
	public static final int STATE_REFUSED = 2;
	/** 同意添加好友 */
	public static final int STATE_ADDED = 1;
	/** 消息为完成的状态 */
	public static final int STATE_NO_FINISH = 0;
	private static final long serialVersionUID = -5731925495114017054L;
	
	private int type;
	private String content;
	private String sent;
	private long time;
	
	private int processed = STATE_NO_FINISH;
	private Login mUser;
	private String mUserID = "";
	public String roomID = "";
	public String roomName="";
	public String userName;
	
	//share
	public int shareId;
	public String shareUid;
	public String shareContent;
	public String sharePicture;
	public double   shareLng;
	public double  shareLat;
	public String shareAddress;
	public int    sharevisible;
	public long  shareCreateTime;
	
	
	//touser
	
	public String toUid;
	public String toName;
	public String toHeadsmall;
	
	public NotifiyMessage(){}
	
	public NotifiyMessage(String reString){
		try {
			JSONObject json = new JSONObject(reString);
			if(!json.isNull("type")){
				type = json.getInt("type");
			}
			
			if(!json.isNull("content")){
				content = json.getString("content");
			}
			
			if(!json.isNull("time")){
				time = json.getLong("time");
			}
			
			if(!json.isNull("user")){
				mUser = new Login(json.getJSONObject("user"));
				mUserID = mUser.uid;
				userName = mUser.name;
			}
			if(!json.isNull("other")){
				String otherString = json.getString("other");
				if(otherString!=null && !otherString.equals("")){
					JSONObject obj = json.getJSONObject("other");
					if(!obj.isNull("id")){
						roomID = obj.getString("id");
					}
					if(!obj.isNull("name")){
						roomName = obj.getString("name");
					}
					if(!obj.isNull("share")){
						JSONObject jso = obj.getJSONObject("share");
						if(!jso.isNull("id")){
							shareId = jso.getInt("id");
						}
						if(!jso.isNull("uid")){
							shareUid = jso.getString("uid");
						}
						if(!jso.isNull("content")){
							shareContent = jso.getString("content");
						}
						if(!jso.isNull("picture")){
							String picString = jso.getString("picture");
							if(picString!=null && !picString.equals("")){
								JSONArray array = new JSONArray(picString);
								if(array.length()>0){
									JSONObject picJson = array.getJSONObject(0);
									if(!picJson.isNull("thumbUrl")){
										sharePicture = picJson.getString("thumbUrl");
									}
								}
								
							}
							
						}
						if(!jso.isNull("lng")){
							shareLng = jso.getDouble("lng");
							/*String lng = jso.getString("lng");
							if(lng!=null && !lng.equals("")){
								shareLng = Long.parseLong(lng);
							}*/
						
						}
						if(!jso.isNull("lat")){
							shareLat = jso.getDouble("lat");
							/*String lat = jso.getString("lat");
							if(lat!=null && !lat.equals("")){
								shareLat = Long.parseLong(lat);
							}*/
						}
						if(!jso.isNull("address")){
							shareAddress = jso.getString("address");
						}
						if(!jso.isNull("creatime")){
							shareCreateTime = jso.getLong("creatime");
						}
					}
					
					if(!obj.isNull("touser")){
						JSONObject jso = obj.getJSONObject("share");
						if(!jso.isNull("uid")){
							toUid = jso.getString("uid");
						}
						if(!jso.isNull("name")){
							toName = jso.getString("name");
						}
						if(!jso.isNull("headSmall")){
							toHeadsmall = jso.getString("headSmall");
						}
					}
				}
			}
			
			/*if(type == 20 || type == 21 || type == 22 || type == 23 || type == 24){
				roomID = content;
			}*/
			/*if(!json.isNull("roomID")){
				roomID = json.getString("roomID");
			}*/
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	public int getProcessed() {
		return processed;
	}

	public void setProcessed(int processed) {
		this.processed = processed;
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}


	public Login getUser() {
		return mUser;
	}

	public void setUser(Login user) {
		this.mUser = user;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @return the sent
	 */
	public String getSent() {
		return sent;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @param sent the sent to set
	 */
	public void setSent(String sent) {
		this.sent = sent;
	}
	
	public void setUserId(String userid){
		this.mUserID = userid;
	}
	
	public String getUserId(){
		return mUserID;
	}
}
