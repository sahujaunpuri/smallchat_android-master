package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login implements Serializable{

	private static final long serialVersionUID = 14634155464564L;

	/*	
	 * 
	 {
  "data": {
    "uid": "200601",
    "phone": "13689084790",
    "password": "855180",
    "nickname": "",
    "headSmall": "",
    "headLarge": "",
    "gender": "2",
    "sign": "",
    "province": "",
    "city": "",
    "isfriend": 0,
    "createtime": "1407311351"
  },
  "state": {
    "code": 0,
    "msg": "",
    "debugMsg": "",
    "url": "User\/Api\/regist"
  }
}
	 */
	public String uid; //UID 用户的唯一ID
	public int nameType; //0-普通用户 1-操作栏 2-星标朋友
	public int userType; // 0-普通用户 1-黑名单用户 2-星标朋友
	public String sort;//排序字段
	public String sortName;//排序字段名称
	public String phone;//用户的手机号码
	public String openfirePwd;//operfire密码
	public String nickname;//用户昵称

	public String headSmall;//小头像
	public String headLarge;//大头像
	public int gender;//性别 0-男 1-女 2-未填写

	public float money;//钱余额
	/**
	 * 0-看 1-不看 当前用户是否看另个用户的朋友圈
	 */
	public int fauth1 ; 
	/**
	 * 0-看 1-不看 当前用户不让另个用户查看我的朋友圈
	 */
	public int fauth2; 
	public int isGetMsg;//0-不接收 1-接收
	public String sign; //个性签名
	public int newFriends; //1-有新的朋友 0-无新的朋友
	/**
	 * 省id
	 */
	public String provinceid;//省id
	/**
	 * 市id
	 */
	public String cityid;//市id
	public int isfriend;
	public long createtime;//用户创建时间

	public String password = "";//本地用户密码
	
	public String content;//申请理由
	
	
	
	public String name;//用户的姓名
	public String userPic;
	
	
	
	
	
	
	public String remark; //备注名
	
	/*public int isfollow;
	public int followers;
	public int fansers;*/
	public List<Picture> picList;

	/*public int visit;*/
	

	public boolean isShow = false;
	public int groupId = -999;
	public String groupName = "";
	public int isAccount;
	
	//隐私设置
	public boolean isAcceptNew= true;
	public boolean isOpenVoice= true;
	public boolean isOpenShake = true;
	
	/**
	 * 验证朋友请求
	 */
	public boolean isValidFriendAppley = true;
	
	/**
	 * 回复即添加对方为朋友
	 */
	public boolean isReplyAndFriend = true;
	/**
	 * 向我推荐通讯录朋友
	 */
	public boolean isTuiJianContact=true;

	//搜索用户时用到的字段
	public int mIsRoom;
	public int isOwner;
	public List<String> headUrlList;
	
	public String cover;
	
	public Room room;


	//加密消息
	public int encryptMode=0;
	//隐私模式
	public int privacyMode=0;

	/*new Login(String.valueOf(contactId),number,
			String.valueOf(photoId),name,index,"",0,*/
	
	
	
	
	public Login(){}

	public Login(String uid, String sort, String phone, String headSmall,
			String nickname) {
		super();
		this.uid = uid;
		this.sort = sort;
		this.phone = phone;
		this.headSmall = headSmall;
		this.nickname = nickname;
	}
	public Login( String sort, String headSmall,
			String nickname,String remarkName,int nameType) {
		super();
		this.sort = sort;
		this.headSmall = headSmall;
		this.nickname = nickname;
		this.remark = remarkName;
		this.nameType = nameType;
	}
	public Login( String sort, String headSmall,
			String nickname,String remarkName,int nameType,int newFriends) {
		super();
		this.sort = sort;
		this.headSmall = headSmall;
		this.nickname = nickname;
		this.remark = remarkName;
		this.nameType = nameType;
		this.newFriends = newFriends;
	}
	
	//newFriends

	public Login(String uid, String name, int mIsRoom,int isOwner,List<String> headUrlList) {
		super();
		this.uid = uid;
		this.name = name;
		this.mIsRoom = mIsRoom;
		this.isOwner = isOwner;
		this.headUrlList = headUrlList;
	}
	
	
	public Login(String uid, String name, int mIsRoom,int isOwner,String headSmallImag) {
		super();
		this.uid = uid;
		this.nickname = name;
		this.mIsRoom = mIsRoom;
		this.isOwner = isOwner;
		this.headSmall = headSmallImag;
	}
	
	public Login(Room room,String nickName,String headSmall,int isRoom) {
		super();
		this.room = room;
		this.nickname = nickName;
		this.headSmall = headSmall;
		this.mIsRoom = isRoom;
	}
	
	

	public Login(JSONObject json) {
		try {
			if(!json.isNull("uid")){
				uid = json.getString("uid");
			}
			if(!json.isNull("sort")){
				sort = json.getString("sort");
				if(sort.matches("[A-Z]") || sort.matches("[a-z]")){
					sort=sort.toUpperCase();
				}else{
					sort = "#";
				}
			}

			if(!json.isNull("phone")){
				phone = json.getString("phone");
			}
			if(!json.isNull("content")){
				content = json.getString("content");
				sign = content;
			}
			
			if(!json.isNull("password")){
				openfirePwd = json.getString("password");
			}
			if(!json.isNull("nickname")){
				nickname = json.getString("nickname");
			}
			
			

			if(!json.isNull("headSmall")){
				headSmall = json.getString("headSmall");
				/*if(small!=null && !small.equals("")){
					headSmall = IMServerAPI.HEAD_URL + small;
				}*/
			}

			if(!json.isNull("headLarge")){
				headLarge = json.getString("headLarge");
				/*if(large!=null && !large.equals("")){
					headLarge = IMServerAPI.HEAD_URL + large;
				}*/
			}
			
			if(!json.isNull("gender")){
				gender = json.getInt("gender");
			}
			
			if(!json.isNull("sign")){
				sign = json.getString("sign");
			}
			/*if(sign == null || sign.equals("")){
				sign = "还未签名  签一个";
			}*/

			
			if(!json.isNull("province")){
				provinceid = json.getString("province");
			}

			if(!json.isNull("city")){
				cityid = json.getString("city");
			}
			if(!json.isNull("isfriend")){
				isfriend = json.getInt("isfriend");
			}
			
			if(!json.isNull("isblack")){
				userType = json.getInt("isblack");
			}
			
			if(!json.isNull("getmsg")){
				isGetMsg = json.getInt("getmsg");
			}
			
			if(!json.isNull("createtime")){
				createtime = json.getLong("createtime");
			}
		
			if(!json.isNull("cover")){
				this.cover = json.getString("cover");
			}

			
			//以下内容暂不需要
			if(!json.isNull("name")){
				name = json.getString("name");
			}
			
			
			

		
			picList = new ArrayList<Picture>();
			if(!json.isNull("picture1")){
				String picUrl = json.getString("picture1");
				if(picUrl!=null && !picUrl.equals("")){
					picList.add(new Picture(picUrl,""));
				}
				
			}
			if(!json.isNull("picture2")){
				String picUrl = json.getString("picture2");
				if(picUrl!=null && !picUrl.equals("")){
					picList.add(new Picture(picUrl,""));
				}
				
			}
			if(!json.isNull("picture3")){
				String picUrl = json.getString("picture3");
				if(picUrl!=null && !picUrl.equals("")){
					picList.add(new Picture(picUrl,""));
				}
			}
			
			
			if(!json.isNull("picture")){
				picList = new ArrayList<Picture>();
				JSONArray jsonArray = json.getJSONArray("picture");
				if(jsonArray.length()>0){
					userPic = json.getString("picture");
					for (int i = 0; i < jsonArray.length(); i++) {
						picList.add(Picture.getInfo(jsonArray.getString(i)));
					}
				}
			}
			if(!json.isNull("isstar")){
				int isStar = json.getInt("isstar");
				if(isStar == 1){
					userType = 2;
				}
			}
			
			if(!json.isNull("verify")){
				this.isValidFriendAppley = json.getInt("verify")==1?true:false;
			}
			
			if(!json.isNull("fauth1")){
				this.fauth1 = json.getInt("fauth1");
			}
			if(!json.isNull("fauth2")){
				this.fauth2 = json.getInt("fauth2");
			}
			
			
			if(!json.isNull("createtime")){
				createtime = json.getLong("createtime");
			}
		
			
			
			if(!json.isNull("remark")){
				remark = json.getString("remark");
			}


            if(!json.isNull("encryptMode")){
                encryptMode = json.getInt("encryptMode");
            }

            if(!json.isNull("privacyMode")){
                privacyMode = json.getInt("privacyMode");
            }
			
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
