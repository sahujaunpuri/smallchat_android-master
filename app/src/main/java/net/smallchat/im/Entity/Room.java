package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.IMCommon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Room implements Serializable{

	private static final long serialVersionUID = 156464416456564L;
	public String groupId; //群id
	public String groupName;//群昵称
	public int groupCount; //群总共的人数
	public String uid;      //群创建者id
	public String creatAuth; //群创建者昵称

	public String groupnickname; //用户所在群的昵称
	public int isjoin; //是否加入
	public int role;

	public int isShowNickname;
	public int isgetmsg = 1;
	public int isOwner;
	public long sendTime = 0;
	public long createTime;

	//加密消息
	public int encryptMode;
	//隐私模式
	public int privacyMode;


	public List<Login> mUserList;


	public IMResponseState state;

	public Room(){}

	public Room(String reqString){
		if(reqString == null || reqString.equals("")){
			return;
		}
		try {
			JSONObject json = new JSONObject(reqString);
			if(!json.isNull("state")){
				state = new IMResponseState(json.getJSONObject("state"));
			}

			if(!json.isNull("data")){
				String dataString = json.getString("data");
				if(dataString!=null && !dataString.equals("")){
					if(dataString.startsWith("{")){
						JSONObject childObj = json.getJSONObject("data");

						if(!childObj.isNull("id")){
							this.groupId = childObj.getString("id");
						}
						if(!childObj.isNull("name")){
							this.groupName = childObj.getString("name");
						}

						if(!childObj.isNull("count")){
							this.groupCount = childObj.getInt("count");
						}

						if(!childObj.isNull("uid")){
							this.uid = childObj.getString("uid");
						}
						if(!childObj.isNull("creator")){
							this.creatAuth = childObj.getString("creator");
						}

						if(!childObj.isNull("mynickname")){
							this.groupnickname = childObj.getString("mynickname");
						}

						if(!childObj.isNull("isjoin")){
							this.isjoin = childObj.getInt("isjoin");
						}

						if(!childObj.isNull("role")){
							this.role = childObj.getInt("role");
						}

						if(!childObj.isNull("getmsg")){
							this.isgetmsg = childObj.getInt("getmsg");
						}

						if(uid!=null && !uid.equals("")){
							if(uid.equals(IMCommon.getUserId(ChatApplication.getInstance()))){
								isOwner = 1;
							}
						}

						if(!childObj.isNull("createtime")){
							this.createTime = childObj.getLong("createtime");
						}

						if (!childObj.isNull("list")) {

							JSONArray user = childObj.getJSONArray("list");
							if (user != null && user.length() != 0) {
								mUserList = new ArrayList<Login>();
								for (int i = 0; i < user.length(); i++) {
									mUserList.add(new Login(user.getJSONObject(i)));
								}
							}

						}



						if(!childObj.isNull("encryptMode")){
							privacyMode = childObj.getInt("encryptMode");
						}

						if(!childObj.isNull("privacyMode")){
							privacyMode = childObj.getInt("privacyMode");
						}


					}else if(dataString.startsWith("[")){

					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Room(JSONObject json){

		try {
			if(!json.isNull("state")){
				state = new IMResponseState(json.getJSONObject("state"));
			}



			if(!json.isNull("id")){
				this.groupId = json.getString("id");
			}
			if(!json.isNull("name")){
				this.groupName = json.getString("name");
			}

			if(!json.isNull("count")){
				this.groupCount = json.getInt("count");
			}

			if(!json.isNull("uid")){
				this.uid = json.getString("uid");
			}
			if(!json.isNull("creator")){
				this.creatAuth = json.getString("creator");
			}

			if(!json.isNull("mynickname")){
				this.groupnickname = json.getString("mynickname");
			}

			if(!json.isNull("isjoin")){
				this.isjoin = json.getInt("isjoin");
			}

			if(!json.isNull("role")){
				this.role = json.getInt("role");
			}

			if(!json.isNull("getmsg")){
				this.isgetmsg = json.getInt("getmsg");
			}

			if(uid!=null && !uid.equals("")){
				if(uid.equals(IMCommon.getUserId(ChatApplication.getInstance()))){
					isOwner = 1;
				}
			}

			if(!json.isNull("createtime")){
				this.createTime = json.getLong("createtime");
			}

			if (!json.isNull("list")) {

				JSONArray user = json.getJSONArray("list");
				if (user != null && user.length() != 0) {
					mUserList = new ArrayList<Login>();
					for (int i = 0; i < user.length(); i++) {
						mUserList.add(new Login(user.getJSONObject(i)));
					}
				}

			}


			if(!json.isNull("data")){
				String dataString = json.getString("data");
				if(dataString!=null && !dataString.equals("")){
					if(dataString.startsWith("{")){
						JSONObject childObj = json.getJSONObject("data");

						if(!childObj.isNull("id")){
							this.groupId = childObj.getString("id");
						}
						if(!childObj.isNull("name")){
							this.groupName = childObj.getString("name");
						}

						if(!childObj.isNull("count")){
							this.groupCount = childObj.getInt("count");
						}

						if(!childObj.isNull("uid")){
							this.uid = childObj.getString("uid");
						}
						if(!childObj.isNull("creator")){
							this.creatAuth = childObj.getString("creator");
						}

						if(!childObj.isNull("mynickname")){
							this.groupnickname = childObj.getString("mynickname");
						}

						if(!childObj.isNull("isjoin")){
							this.isjoin = childObj.getInt("isjoin");
						}

						if(!childObj.isNull("role")){
							this.role = childObj.getInt("role");
						}

						if(!childObj.isNull("getmsg")){
							this.isgetmsg = childObj.getInt("getmsg");
						}

						if(uid!=null && !uid.equals("")){
							if(uid.equals(IMCommon.getUserId(ChatApplication.getInstance()))){
								isOwner = 1;
							}
						}

						if(!childObj.isNull("createtime")){
							this.createTime = json.getLong("createtime");
						}

						if (!childObj.isNull("list")) {

							JSONArray user = childObj.getJSONArray("list");
							if (user != null && user.length() != 0) {
								mUserList = new ArrayList<Login>();
								for (int i = 0; i < user.length(); i++) {
									mUserList.add(new Login(user.getJSONObject(i)));
								}
							}

						}
					}else if(dataString.startsWith("[")){

					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
