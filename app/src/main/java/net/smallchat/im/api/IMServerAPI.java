package net.smallchat.im.api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.smallchat.im.Entity.AddGroup;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.CheckFriends;
import net.smallchat.im.Entity.CountryList;
import net.smallchat.im.Entity.Favorite;
import net.smallchat.im.Entity.FriendsLoop;
import net.smallchat.im.Entity.FriendsLoopItem;
import net.smallchat.im.Entity.GroupList;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.LoginResult;
import net.smallchat.im.Entity.Meeting;
import net.smallchat.im.Entity.MeetingItem;
import net.smallchat.im.Entity.MenuList;
import net.smallchat.im.Entity.MessageResult;
import net.smallchat.im.Entity.MoreFile;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.RoomList;
import net.smallchat.im.Entity.RoomUsrList;
import net.smallchat.im.Entity.UploadAudio;
import net.smallchat.im.Entity.UploadAudioResult;
import net.smallchat.im.Entity.UploadFile;
import net.smallchat.im.Entity.UploadFileResult;
import net.smallchat.im.Entity.UploadImage;
import net.smallchat.im.Entity.UploadImageResult;
import net.smallchat.im.Entity.UploadVideo;
import net.smallchat.im.Entity.UploadVideoResult;
import net.smallchat.im.Entity.UserList;
import net.smallchat.im.Entity.VersionInfo;
import net.smallchat.im.config.APIConfig;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.IMCommon;

import static net.smallchat.im.config.APIConfig.getApiServer;
import static net.smallchat.im.config.APIConfig.getAppKey;


public class IMServerAPI implements Serializable{
	private static final String TAG ="IM_APP_API";
	private static final long serialVersionUID = 1651654562644564L;


	public String sendRequest(String url, IMParameters params, String httpMethod, int loginType) throws IMException{
		String rlt = null;
		Log.d(TAG,"sendRequest url="+url);
		rlt = Utility.openUrl(url, httpMethod, params,loginType);
		Log.d(TAG,"API_RESPONSE="+rlt);
		if(rlt != null && rlt.length() != 0){
			int c = rlt.indexOf("{");
			if(c != 0){
				rlt = rlt.subSequence(c, rlt.length()).toString();
			}
		}

		return rlt;

	}

	public String requestProtocol(String url, IMParameters params, String httpMethod) throws IMException{
		String rlt = null;
		rlt = Utility.openUrl(url, httpMethod, params,0);
		return rlt;

	}

	/**
	 * 用户注册协议
	 *  /user/apiother/regist
	 * @throws IMException
	 */
	public String getProtocol() throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		String url = getApiServer() + "/user/apiother/regist";
		String reString = requestProtocol(url, bundle, Utility.HTTPMETHOD_POST);
		if(reString != null && !reString.equals("") ){
			Log.e("reString", reString);
			return reString;
		}
		return null;

	}


	/**
	 * 获取验证码 /user/apiother/getCode
	 * @param tel
	 * @param type
	 * @return
	 * @throws IMException
	 */
	public IMResponseState getVerCode(String tel, int type) throws IMException{
		if (tel == null || tel.equals("")) {
			return null;
		}
		IMParameters bundle = new IMParameters();
		bundle.add("phone",tel);
		if(type!=0){
			bundle.add("type",String.valueOf(type));
		}
		String url = getApiServer() + "/user/apiother/getCode";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,0);
		try {
			if(reString != null && !reString.equals("null") && !reString.equals("")){
				return new IMResponseState(new JSONObject(reString));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 3.	验证验证码(/user/apiother/checkCode)
	 * @param phone	true	string	手机号
	 * @param code	true	string	验证码
	 */

	public IMResponseState checkVerCode(String phone, String code) throws IMException{
		if (phone == null || phone.equals("")) {
			return null;
		}

		IMParameters bundle = new IMParameters();
		bundle.add("phone",phone);
		bundle.add("code",String.valueOf(code));
		String url = getApiServer() + "/user/apiother/getCode";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,0);
		try {
			if(reString != null && !reString.equals("null") && !reString.equals("")){
				return new IMResponseState(new JSONObject(reString));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	//二、登陆注册
	/**
	 *
	 * 1、注册
	 ①　非学员注册(/user/api/regist)
	 1、HTTP请求方式 GET/POST
	 2、是否需要登录 false
	 3、支持格式 JSON
	 参数	必选	类型	说明
	 phone	true	string	用户的手机号
	 password	true	string	密码
	 name	true	string	用户姓名
	 validCode	true	string	邀请码验证码
	 */
	public LoginResult register(String phone,String password,String validCode) throws IMException{
		LoginResult register = null;
		IMParameters bundle = new IMParameters();
		if ((phone == null || phone.equals(""))
				|| (password == null || password.equals(""))) {
			return null;
		}
		bundle.add("phone",phone);
		bundle.add("password",password);

		String url = getApiServer() + "/user/api/regist";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,0);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new LoginResult(reString);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}
		return register;

	}

	/**
	 * 用户登录 /user/api/login
	 * @param phone
	 * @param password
	 * @return
	 * @throws IMException
	 */
	public LoginResult getLogin(String phone, String password) throws IMException {
		IMParameters bundle = new IMParameters();
		bundle.add("phone", phone);
		bundle.add("password", password);
		String url = getApiServer() + "/user/api/login";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,0);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			return new LoginResult(reString.trim());
		}

		return null;

	}

	/**
	 * 重置密码(/api/index/resetpassword)
	 * @param account  账号
	 * @param verCode  验证码
	 * @param findType  找回类型 0 手机短信  1 邮箱找回
	 * @return
	 * @throws IMException
	 */
	public IMResponseState resetPassword(String account, String password, String verCode, int findType) throws IMException{
		String findTypeString=(findType==0)?"0":"1";//判断type类型
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());//接口KEY
		bundle.add("account", account);//账号类型
		bundle.add("findtype",findTypeString);//找回方式 0 手机号 1 邮箱
		bundle.add("vercode", verCode);//验证码
		bundle.add("password", password);//新密码
		String url = getApiServer() + "/user/api/resetpassword";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,0);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * 更改用户资料 post方式请求
	 * /user/api/edit
	 * @param file
	 * @param nickname
	 * @param email
	 * @param gender
	 * @param sign
	 * @param provinceid
	 * @param city
	 * @return
	 * @throws IMException
	 */

	public LoginResult modifyUserInfo(
			String file,String nickname,String email,int gender,
			String sign,String provinceid,String city) throws IMException{
		IMParameters bundle = new IMParameters();

		//必填选项
		bundle.add("appkey", getAppKey());
		if(file!=null && !file.equals("") && file.length()>0){
			List<MoreFile> listpic = new ArrayList<MoreFile>();
			listpic.add(new MoreFile("picture",file));
			bundle.addImage("imageUpload", listpic);
		}

		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		if(nickname !=null && !nickname.equals("")){
			bundle.add("nickname",nickname);
		}
		if(email !=null && !email.equals("")){
			bundle.add("email",email);
		}
		bundle.add("gender",String.valueOf(gender));

		bundle.add("sign",sign);

		if(provinceid!=null && !provinceid.equals("")){
			bundle.add("province", provinceid);
		}
		if(city!=null && !city.equals("")){
			bundle.add("city", city);
		}

		String url = getApiServer() + "/user/api/edit";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new LoginResult(reString);
		}
		return null;
	}






	/**
	 * 根据id获取用户资料
	 * @param uid
	 * @return
	 * @throws IMException
	 */
	public LoginResult getUserInfo(String uid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("fuid", uid);
		bundle.add("uid", String.valueOf(IMCommon.getUserId(ChatApplication.getInstance())));
		String url = getApiServer() +  "/user/api/detail";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new LoginResult(reString);
		}

		return null;
	}


	/**
	 * 16.	设置星标朋友(/user/api/setStar)
	 * fuid	true	int	用户id
	 */
	public LoginResult setStar(String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		if(fuid == null || fuid.equals("")){
			return null;
		}
		bundle.add("fuid", fuid);
		bundle.add("uid", String.valueOf(IMCommon.getUserId(ChatApplication.getInstance())));
		String url = getApiServer() +  "/user/api/setStar";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new LoginResult(reString);
		}

		return null;
	}

	//四、通讯录
	//1.

	/**
	 * 朋友列表(/user/api/friendList)
	 * @return
	 * @throws IMException
	 */

	public GroupList getUserList() throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		bundle.add("uid", String.valueOf(IMCommon.getUserId(ChatApplication.getInstance())));
		String url = getApiServer() +"/user/api/friendList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new GroupList(reString);
		}

		return null;
	}

	//2、添加朋友
	// 1.1

	/**
	 * 好友申请(/user/api/applyAddFriend)
	 * @param userID
	 * @param fuid
	 * @param reason
	 * @return
	 * @throws IMException
	 */

	public IMResponseState applyFriends(String userID, String fuid, String reason) throws IMException{
		IMParameters bundle = new IMParameters();
		if((userID == null || userID.equals(""))
				|| (fuid == null || fuid.equals(""))
			/*|| (reason == null || reason.equals(""))*/){
			return null;
		}
		bundle.add("uid",userID);
		bundle.add("fuid",fuid);
		bundle.add("content",reason);
		bundle.add("appkey",getAppKey());
		String url = getApiServer() + "/user/api/applyAddFriend";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 同意加为好友(/user/api/agreeAddFriend)
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState agreeFriends(String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid",fuid);
		bundle.add("appkey",getAppKey());
		String url = getApiServer() + "/user/api/agreeAddFriend";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 拒绝加为好友(/user/api/refuseAddFriend)
	 * @param toUid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState denyFriends(String toUid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid",toUid);
		bundle.add("appkey",getAppKey());
		String url = getApiServer() + "/user/api/refuseAddFriend";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 删除好友(/user/api/deleteFriend)
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState cancleFriends(String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid",fuid);
		bundle.add("appkey",getAppKey());
		String url = getApiServer() + "/user/api/deleteFriend";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	//1.2搜号码
	/**
	 * 通过手机号或昵称搜索(/user/api/search)
	 */
	int id = 0;
	public UserList search_number(String search,int page) throws IMException{
		id = id+1;
		IMParameters bundle = new IMParameters();
		bundle.add("search", search);

		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("page", String.valueOf(page));
		String url = getApiServer() + "/user/api/search";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		Log.e("search_number","id:"+id);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new UserList(reString,0);
		}

		return null;
	}

	//1.3 从手机通讯录列表添加
	/**
	 * ① 导入手机通讯录(/user/api/importContact)
	 */
	public CheckFriends getContactUserList(String phone) throws IMException{
		if (phone == null || phone.equals("") || phone.contains("null")) {
			return null;
		}
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("phone",phone);
		bundle.add("uid", String.valueOf(IMCommon.getUserId(ChatApplication.getInstance())));
		String url = getApiServer() + "/user/api/importContact";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new CheckFriends(reString);
		}

		return null;
	}



	//3、新的朋友

	/**
	 * 新的朋友(/api/user/newfriend)
	 * @param phone
	 * @return
	 * @throws IMException
	 */

	public UserList getNewFriend(String phone) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		if(phone == null || phone.equals("")
				|| phone.startsWith(",")){
			return null;
		}
		bundle.add("phone", phone);

		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/user/api/newFriend";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			//Log.e("addFriend", reString);
			return new UserList(reString,1);

		}

		return null;
	}


	/**
	 * 添加关注与取消关注(/api/publics/follow)
	 * @param subUserID
	 * @return
	 * @throws IMException
	 */
	public IMResponseState addFocus(String subUserID) throws IMException{
		IMParameters bundle = new IMParameters();

		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("publics_id", subUserID);
		bundle.add("appkey",getAppKey());
		String url = getApiServer() + "/api/publics/follow";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}


	/**
	 * 朋友分组(/api/user/group)
	 * @param type
	 * @return
	 * @throws IMException
	 */
	public UserList getContactGroupList(int type) throws IMException{

		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("type",String.valueOf(type));
		String url = getApiServer() +"/api/user/group";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") ){
			//Log.e("getContactGroupList", reString);
			try {
				return new UserList(reString,0);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return  null;
	}


	/**
	 * 添加关注与取消关注(/api/user/follow)
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState addfocus(String fuid/*,int type*/) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid", fuid);
		/*	bundle.add("type",String.valueOf(type));*/
		String url = getApiServer() + "/api/user/follow";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			//Log.e("reString", reString);

			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	// dalong


	/**
	 * 加入黑名单 /user/api/black
	 * @param blackUid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState addBlock(String blackUid) throws IMException {
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid", blackUid);
		String url = getApiServer() + "/user/api/black";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			//Log.e("reString", reString);

			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	/**
	 * 举报好友 /api/user/jubao
	 * @param fuid
	 * @param content
	 * @param type
	 * @return
	 * @throws IMException
	 */
	public IMResponseState reportedFriend(String fuid, String content, int type) throws IMException {
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid", fuid);
		bundle.add("type",String.valueOf(type));
		bundle.add("content", content);
		String url = getApiServer() +  "/api/user/jubao";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			//Log.e("reString", reString);

			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	/**
	 * 获取黑名单列表/user/api/blackList
	 * @return
	 * @throws IMException
	 */
	public UserList getBlockList(/*int page*/) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		/*	bundle.add("page", String.valueOf(page));
		bundle.add("pageSize", String.valueOf(Common.LOAD_SIZE));*/
		String url = getApiServer() + "/user/api/blackList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);

		if(reString != null && !reString.equals("") && !reString.equals("null")){
			//Log.e("reString", reString);

			return new UserList(reString,0);
		}

		return null;
	}

	/**
	 * 取消黑名单 /user/api/black
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState cancelBlock(String fuid) throws IMException {
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid", fuid);
		String url = getApiServer() +"/user/api/black";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			//Log.e("reString", reString);

			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 添加收藏(/user/api/favorite)
	 * @param fuid
	 * @param groupId
	 * @param content
	 * @return
	 * @throws IMException
	 */
	public IMResponseState favoreiteMoving(String fuid, String groupId,
										   String content) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		if((content == null || content.equals(""))
				&& (fuid == null || fuid.equals(""))
				&& (groupId == null || groupId.equals(""))){
			return null;
		}
		bundle.add("content", content);
		if(fuid!=null && !fuid.equals("")){
			bundle.add("fuid", fuid);
		}

		if(groupId!=null && !groupId.equals("")){
			bundle.add("otherid", groupId);
		}

		String url = getApiServer() +"/user/api/favorite";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			Log.e("favoreiteMoving", reString);
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * 收藏列表(/user/api/favoriteList)
	 * @param page
	 * @return
	 * @throws IMException
	 */
	public Favorite favoriteList(int page) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		if(page!=0){
			bundle.add("page",String.valueOf(page));
		}
		bundle.add("count", "20");
		String url = getApiServer() +"/user/api/favoriteList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			Log.e("favoriteList", reString);
			return new Favorite(reString);
		}
		return null;
	}


	/**
	 * 删除收藏(/user/api/deleteFavorite)
	 * @param favoriteid
	 * @return
	 * @throws IMException
	 */

	public IMResponseState canclefavMoving(int favoriteid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		if(favoriteid == 0){
			return null;
		}
		bundle.add("favoriteid", String.valueOf(favoriteid));

		String url = getApiServer() +"/user/api/deleteFavorite";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			Log.e("favoreiteMoving", reString);
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * 创建组
	 * @param name
	 * @return
	 * @throws IMException
	 */
	public AddGroup AddGroup(String name) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("teamName", name);
		bundle.add("action", "addTeam");
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "friend/Index/action";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new AddGroup(reString);
		}

		return null;
	}

	/**
	 * 检测更新 /version/api/update
	 * @param version
	 * @return
	 * @throws IMException
	 */
	public VersionInfo checkUpgrade(String version) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		if(version == null || version.equals("")){
			return null;
		}
		bundle.add("os", "android");
		bundle.add("version", version.substring(1));
		String url = getApiServer() +"/version/api/update";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new VersionInfo(reString);
		}

		return null;
	}


	/**
	 * 创建临时会话并添加用户 /session/api/add
	 * @param groupname
	 * @param uids
	 * @return
	 * @throws IMException
	 */
	public Room createRoom(String groupname, String uids) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		Log.e("createRoom", "groupName:"+groupname);
		bundle.add("name", groupname);
		bundle.add("uids", uids);
		String url = getApiServer() + "/session/api/add";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new Room(reString);
		}

		return null;
	}


	/**
	 *  添加用户到一个会话(/session/api/addUserToSession)
	 * @param sessionid
	 * @param uids
	 * @return
	 * @throws IMException
	 */
	public IMResponseState inviteUsers(String sessionid, String uids) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", sessionid);
		bundle.add("uids", uids);
		String url = getApiServer() + "/session/api/addUserToSession";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * /session/api/join
	 * @param sessionid     true int 群组id
	 * @return
	 * @throws IMException
	 */
	public Room join(String sessionid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", sessionid);
		String url = getApiServer() + "/session/api/join";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new Room(reString);
		}

		return null;
	}


	/**
	 * 把用户从某个群踢出(/session/api/remove)
	 * @param sessionid
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState kickParticipant(String sessionid, String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", String.valueOf(sessionid));
		bundle.add("fuid", fuid);
		String url = getApiServer() +"/session/api/remove";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	/**
	 * 获取某个用户的所在的群（房间列表）
	 * /session/api/userSessionList
	 * @param fuid	false	String	不传则查看自己的。传了则查看别人的
	 * @return
	 * @throws IMException
	 */
	public RoomList getRoomList(String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() +"/session/api/userSessionList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new RoomList(reString);
		}

		return null;
	}

	/**
	 * 获取某个房间的用户列表(/api/group/getGroupUserList)
	 * @param groupid			房间ID
	 * @return
	 * @throws IMException
	 */
	public RoomUsrList getRoomUserList(String groupid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("groupid", groupid);
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/api/group/getGroupUserList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new RoomUsrList(reString);
		}

		return null;
	}

	/**
	 * ④　删除群(/session/api/delete)
	 * @param sessionid			群组id
	 * @return
	 * @throws IMException
	 */
	public IMResponseState deleteRoom(String sessionid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", String.valueOf(sessionid));
		String url = getApiServer() + "/session/api/delete";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 退出房间(/session/api/quit)
	 * @param sessionid				房间ID
	 * @return
	 * @throws IMException
	 */
	public IMResponseState exitRoom(String sessionid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", sessionid);
		String url = getApiServer() + "/session/api/quit";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 修改群资料(/session/api/edit)
	 * @param sessionid
	 * @param name
	 * @return
	 * @throws IMException
	 */

	public IMResponseState modifyGroupNickName(String sessionid, String name)
			throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", sessionid);
		bundle.add("name", name);
		String url = getApiServer() + "/session/api/edit";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}


	/**
	 * 修改我的群昵称 /session/api/setNickname
	 * @param sessionid
	 * @param groupnickname
	 * @return
	 * @throws IMException
	 */
	public IMResponseState modifyMyNickName(String sessionid, String groupnickname)
			throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", sessionid);
		bundle.add("mynickname", groupnickname);
		String url = getApiServer() + "/session/api/setNickname";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}


	/**
	 * 设置群类型(/api/group/ispublic)
	 * @param groupid
	 * @param ispublic
	 * @return
	 * @throws IMException
	 */
	public IMResponseState isPublicGroup(String groupid, int ispublic)
			throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("groupid", groupid);
		bundle.add("ispublic", String.valueOf(ispublic));
		String url = getApiServer() + "/api/group/ispublic";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	/**
	 * 设置是否接收消息(/session/api/getmsg)
	 * @param groupid
	 * @param isgetmsg
	 * @return
	 * @throws IMException
	 */
	public IMResponseState isGetGroupMsg(String groupid, int isgetmsg)
			throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", groupid);
		bundle.add("isgetmsg", String.valueOf(isgetmsg));
		String url = getApiServer() + "/session/api/getmsg";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 设置是否接收另一用户的消息(/user/api/setGetmsg)
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState setMsg(String fuid)
			throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid", fuid);
		String url = getApiServer() + "/user/api/setGetmsg";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	/**
	 * 会话详细(/session/api/detail)
	 * @param sessionid
	 * @return
	 * @throws IMException
	 */
	public Room getRommInfoById(String sessionid)
			throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("sessionid", sessionid);
		String url = getApiServer() + "/session/api/detail";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new Room(reString);
		}

		return null;
	}






	// 获取省市

	/**
	 * 省市(/user/apiother/areaList)
	 * @return
	 * @throws IMException
	 */
	public CountryList getCityAndContryUser() throws IMException{
		String reString = FeatureFunction.getAssestsFile("AreaCode");
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			return new CountryList(reString);
		}
		return null;
	}


	/**
	 * 修改备注名(/user/api/remark )
	 * @param fuid
	 * @param remark
	 * @return
	 * @throws IMException
	 */
	public IMResponseState remarkFriend(String fuid, String remark) throws IMException {
		IMParameters bundle = new IMParameters();
		bundle.add("appkey", getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("fuid", fuid);
		bundle.add("remark", remark);
		String url = getApiServer() + "/user/api/remark";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	//5. 朋友圈

	/**
	 * 设置用户封面图 post方式请求
	 * @param userID
	 * @param listpic
	 * @return
	 * @throws IMException
	 */
	public IMResponseState uploadUserBg(String userID, List<MoreFile> listpic) throws IMException{
		IMResponseState status = null;
		IMParameters bundle = new IMParameters();
		if(listpic!=null && listpic.size()>0){
			bundle.addImage("imageUpload", listpic);
		}
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/friend/api/setCover";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				JSONObject jsonObj = new JSONObject(reString);
				if (jsonObj!=null && !jsonObj.equals("") && !jsonObj.equals("null")) {
					status = new IMResponseState(jsonObj);
				}
			} catch (Exception e) {
				e.printStackTrace();
				//return null;
			}
		}
		return status;
	}

	/**
	 * 发布分享(/friend/api/add)
	 * @param picList
	 * @param content
	 * @param lng
	 * @param lat
	 * @param address
	 * @param visible
	 * @return
	 * @throws IMException
	 */

	public IMResponseState addShare(List<MoreFile> picList, String content,
									String lng, String lat, String address, String visible) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if((picList == null || picList.size()<=0)
				&& (content == null || content.equals(""))){
			return null;
		}

		if(picList!=null && picList.size()>0){
			bundle.addImage("imageUpload", picList);
		}

		if(content !=null && !content.equals("")){
			bundle.add("content", content);
		}

		if(lng!=null && !lng.equals("")){
			bundle.add("lng",lng);
		}

		if(lat != null && !lat.equals("")){
			bundle.add("lat",lat);
		}

		if(address!=null && !address.equals("")){
			bundle.add("address",address);
		}

		if(visible!=null && !visible.equals("")
				&& !visible.startsWith(",")){
			bundle.add("visible",visible);
		}

		String url = getApiServer() + "/friend/api/add";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

	/**
	 * 删除分享(/friend/api/delete)
	 * @param fsid
	 * @return
	 * @throws IMException
	 */

	public IMResponseState deleteShare(int fsid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(fsid == 0){
			return null;
		}

		bundle.add("fsid",String.valueOf(fsid));

		String url = getApiServer() + "/friend/api/delete";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {////
				return new IMResponseState(new JSONObject(reString));

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 分享详细(/friend/api/detail)
	 * @param fsid
	 * @return
	 * @throws IMException
	 */
	public FriendsLoopItem shareDetail(int fsid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(fsid == 0){
			return null;
		}

		bundle.add("fsid",String.valueOf(fsid));

		String url = getApiServer() + "/friend/api/detail";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new FriendsLoopItem(reString);

			} catch (Exception e) {
				e.printStackTrace();
				return  null;
			}
		}
		return  null;
	}

	/**
	 * 朋友圈列表(/friend/api/shareList)
	 * @param page
	 * @return
	 * @throws IMException
	 */
	public FriendsLoop shareList(int page) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		if(page!=0){
			bundle.add("page",String.valueOf(page));
		}
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/friend/api/shareList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new FriendsLoop(reString);
		}
		return null;
	}


	/**
	 * 朋友相册(/friend/api/userAlbum)
	 * @param page
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public FriendsLoop myHomeList(int page,String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		if(fuid!=null && !fuid.equals(IMCommon.getUserId(ChatApplication.getInstance()))){
			bundle.add("fuid",fuid);
		}
		if(page!=0){
			bundle.add("page",String.valueOf(page));
		}
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/friend/api/userAlbum";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new FriendsLoop(reString);
		}
		return null;
	}

	/**
	 * 添加 取消赞(/friend/api/sharePraise)
	 * @param fsid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState sharePraise(int fsid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(fsid == 0){
			return null;
		}

		bundle.add("fsid",String.valueOf(fsid));

		String url = getApiServer() + "/friend/api/sharePraise";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 回复(/friend/api/shareReply)
	 * @param fsid
	 * @param toUid
	 * @param content
	 * @return
	 * @throws IMException
	 */
	public IMResponseState shareReply(int fsid, String toUid, String content) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(fsid == 0 ||( toUid == null || toUid.equals(""))){
			return null;
		}

		bundle.add("content", content);
		bundle.add("fsid",String.valueOf(fsid));
		bundle.add("fuid", toUid);

		String url = getApiServer() + "/friend/api/shareReply";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}


	/**
	 * 删除回复(/friend/api/deleteReply)
	 * @param replyid
	 * @return
	 * @throws IMException
	 */

	public IMResponseState deleteReply(int replyid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(replyid == 0){
			return null;
		}

		bundle.add("replyid",String.valueOf(replyid));

		String url = getApiServer() + "/friend/api/deleteReply";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 设置朋友圈权限(/friend/api/setFriendCircleAuth)
	 * @param type
	 * @param fuid
	 * @return
	 * @throws IMException
	 */

	public IMResponseState setFriendCircleAuth(int type, String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());

		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(type == 0 || (fuid == null || fuid.equals(""))){
			return null;
		}

		bundle.add("type",String.valueOf(type));
		bundle.add("fuid", fuid);

		String url = getApiServer() + "/friend/api/setFriendCircleAuth";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	//7.设置

	/**
	 * 意见反馈 /user/api/feedback
	 * @param content
	 * @return
	 * @throws IMException
	 */
	public IMResponseState feedback(String content) throws IMException {
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("content", content);
		String url = getApiServer() + "/user/api/feedback";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				JSONObject json = new JSONObject(reString);
				if(!json.isNull("state")){
					return new IMResponseState(json.getJSONObject("state"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	/**
	 * 修改密码(/user/api/editPassword)
	 * @param oldpassword
	 * @param newpassword
	 * @return
	 * @throws IMException
	 */
	public IMResponseState editPasswd(String oldpassword, String newpassword) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		if((oldpassword == null || oldpassword.equals(""))
				|| (newpassword == null || newpassword.equals(""))){
			return null;
		}
		bundle.add("oldpassword", oldpassword);
		bundle.add("newpassword", newpassword);
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/user/api/editPassword";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 帮助中心(/user/apiother/help)
	 * @return
	 * @throws IMException
	 */
	public String getHelpHtml() throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		String url = getApiServer() + "/user/apiother/help";
		String reString = requestProtocol(url, bundle, Utility.HTTPMETHOD_POST);
		if(reString != null && !reString.equals("") && !reString.equals("null") ){
			return reString;
		}
		return null;
	}

	/**
	 * 根据姓名获取用户详细(/api/user/getUserByName)
	 * @param name
	 * @return
	 * @throws IMException
	 */
	public LoginResult getUserByName(String name) throws IMException {
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		if(name == null || name.equals("")){
			return null;
		}
		bundle.add("name", name);
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/api/user/getUserByName";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			return new LoginResult(reString.trim());
		}

		return null;

	}


	/**
	 * 设置加好友是否需要验证(/user/api/setVerify)
	 * @param verify
	 * @return
	 * @throws IMException
	 */
	public IMResponseState setVerify(int verify) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/user/api/setVerify";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}


	/**
	 * 发送消息接口
	 * @param chatMessage
	 * @param isForward
	 * @return
	 * @throws IMException
	 */
	public MessageResult sendMessage(ChatMessage chatMessage, boolean isForward) throws IMException{
		Log.d(TAG,"sendMessage in api messageType="+ chatMessage.messageType);
		IMParameters bundle = new IMParameters();
		if(chatMessage == null){
			return null;
		}
		bundle.add("chatType", String.valueOf(chatMessage.chatType));
		bundle.add("tag", chatMessage.tag);
		if(!TextUtils.isEmpty(chatMessage.fromName)){
			bundle.add("fromName", chatMessage.fromName);
		}
		if(!TextUtils.isEmpty(chatMessage.fromId)){
			bundle.add("fromId", chatMessage.fromId);
		}

		if(!TextUtils.isEmpty(chatMessage.fromUrl)){
			bundle.add("fromUrl", chatMessage.fromUrl);
		}
		bundle.add("toId", chatMessage.toId);
		if(!TextUtils.isEmpty(chatMessage.toName)){
			bundle.add("toName", chatMessage.toName);
		}

		if(!TextUtils.isEmpty(chatMessage.toUrl)){
			bundle.add("toUrl", chatMessage.toUrl);
		}
		bundle.add("messageType", String.valueOf(chatMessage.messageType));

		bundle.add("encryptMode", String.valueOf(chatMessage.encryptMode));

		bundle.add("privacyMode", String.valueOf(chatMessage.privacyMode));

		//转换成对象到content
		chatMessage.convertObjectToContent();
		//content
		if(!TextUtils.isEmpty(chatMessage.content)){
			bundle.add("content", chatMessage.content);
		}
		Log.d(TAG,"im send message api content="+chatMessage.content);
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/user/api/sendMessage";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST, 1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new MessageResult(reString);
		}

		return null;
	}


	//会议

	/**
	 * 创建会议(/meeting/api/add)
	 * @param picture
	 * @param name
	 * @param content
	 * @param start
	 * @param end
	 * @return
	 * @throws IMException
	 */
	public IMResponseState createMetting(String picture, String name, String content,
										 long start, long end) throws IMException{
		IMParameters bundle = new IMParameters();
		if(picture!=null && !picture.equals("")){
			List<MoreFile> listPic = new ArrayList<MoreFile>();
			listPic.add(new MoreFile("picture",picture));
			bundle.addImage("imageUpload", listPic);
		}
		if((name == null || name.equals("")) || (content == null || content.equals(""))
				|| start == 0 || end == 0){
			return null;
		}
		bundle.add("name", name);
		bundle.add("content", content);
		bundle.add("start",String.valueOf(start));
		bundle.add("end",String.valueOf(end));

		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/meeting/api/add";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}


		return null;
	}


	/**
	 * 会议详细(/meeting/api/detail)
	 * @param meetingid
	 * @return
	 * @throws IMException
	 */
	public MeetingItem mettingDetail(int meetingid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(meetingid == 0){
			return null;
		}
		bundle.add("meetingid",String.valueOf(meetingid));
		String url = getApiServer() + "/meeting/api/detail";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			return new MeetingItem(reString);
		}
		return null;
	}


	/**
	 * 会议列表(/meeting/api/meetingList)
	 * @param type
	 * @param page
	 * @return
	 * @throws IMException
	 */
	public Meeting meetingList(int type,int page) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid",IMCommon.getUserId(ChatApplication.getInstance()));
		if(type == 0){
			return null;
		}
		bundle.add("type",String.valueOf(type));
		bundle.add("page", String.valueOf(page));
		String url = getApiServer() + "/meeting/api/meetingList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") ){
			return new Meeting(reString);

		}
		return null;
	}


	/**
	 * 申请加入会议(/meeting/api/apply)
	 * @param meetingid
	 * @param reasion
	 * @return
	 * @throws IMException
	 */
	public IMResponseState applyMeeting(int meetingid, String reasion) throws IMException{
		IMParameters bundle = new IMParameters();
		if( meetingid == 0 || (reasion == null || reasion.equals(""))){
			return null;
		}
		bundle.add("meetingid", String.valueOf(meetingid));
		bundle.add("content", reasion);
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/meeting/api/apply";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}


	/**
	 * 同意申请加入会议(/meeting/api/agreeApply)
	 * @param meetingid
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState agreeApplyMeeting(int meetingid, String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		if( meetingid == 0 || (fuid == null || fuid.equals(""))){
			return null;
		}
		bundle.add("meetingid", String.valueOf(meetingid));
		bundle.add("fuid", fuid);
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/meeting/api/agreeApply";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}


	/**
	 * 不同意申请加入会议(/meeting/api/disagreeApply)
	 * @param meetingid
	 * @param fuid
	 * @return
	 * @throws IMException
	 */

	public IMResponseState disagreeApplyMeeting(int meetingid, String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		if( meetingid == 0 || (fuid == null || fuid.equals(""))){
			return null;
		}
		bundle.add("meetingid", String.valueOf(meetingid));
		bundle.add("fuid", fuid);
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/meeting/api/disagreeApply";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * 邀请加入会议(/meeting/api/invite)
	 * @param meetingid
	 * @param uids
	 * @return
	 * @throws IMException
	 */
	public IMResponseState inviteMeeting(int meetingid, String uids) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		if(meetingid == 0
				|| (uids == null || uids.equals("")
				|| uids.startsWith(",") || uids.endsWith(","))){
			return null;
		}
		bundle.add("meetingid", String.valueOf(meetingid));
		bundle.add("uids", uids);
		String url = getApiServer() + "/meeting/api/invite";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}


	/**
	 * 会议的用户申请列表(/meeting/api/meetingApplyList)
	 * @param page
	 * @param meetingid
	 * @return
	 * @throws IMException
	 */
	public UserList meetingApplyList(int page,int meetingid) throws IMException{
		IMParameters bundle = new IMParameters();
		if( meetingid == 0){
			return null;
		}
		bundle.add("meetingid", String.valueOf(meetingid));
		bundle.add("page", String.valueOf(page));
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/meeting/api/meetingApplyList";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			return new UserList(reString,0);
		}
		return null;
	}


	/**
	 * 用户活跃度排行(/meeting/api/huoyue)
	 * @param page
	 * @param meetingid
	 * @return
	 * @throws IMException
	 */
	public UserList huoyueList(int page,int meetingid) throws IMException{
		IMParameters bundle = new IMParameters();
		if( meetingid == 0){
			return null;
		}
		bundle.add("meetingid", String.valueOf(meetingid));
		bundle.add("page", String.valueOf(page));
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/meeting/api/huoyue";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/){
			return new UserList(reString,0);
		}
		return null;
	}


	/**
	 * 移除用户(/meeting/api/remove)
	 * @param meetingid
	 * @param fuid
	 * @return
	 * @throws IMException
	 */
	public IMResponseState removeMetUser(int meetingid, String fuid) throws IMException{
		IMParameters bundle = new IMParameters();
		if( meetingid == 0 || (fuid == null || fuid.equals(""))){
			return null;
		}
		bundle.add("meetingid", String.valueOf(meetingid));
		bundle.add("fuid", fuid);
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		String url = getApiServer() + "/meeting/api/remove";
		try {
			String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST, 1);
			if (reString != null && !reString.equals("") && !reString.equals("null") /* && reString.startsWith("{")*/) {
				try {
					return new IMResponseState(new JSONObject(reString));
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取个人菜单
	 * @param pid
	 * @return
	 * @throws IMException
	 */
	public MenuList getMenu(String pid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("appkey",getAppKey());
		bundle.add("uid", String.valueOf(IMCommon.getUserId(ChatApplication.getInstance())));
		bundle.add("pid", pid);
		//这里测试不要放置静态文件，因为绝大多数服务器，不允许静态文件响应post请求，否则会出现405错误 405 Not Allowed
		String url = getApiServer() +"/user/api/getMenu";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			return new MenuList(reString);
		}
		return null;
	}




	/**
	 *  消息撤回(/user/api/revokeMessage)
	 * @param messageTag	true	string	消息TAG
	 * @param touid true String 发送到的对象uid
	 */
	public IMResponseState revokeMessage(int type,String messageTag, String touid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		if(messageTag == null || messageTag.equals("")||touid==null||touid.equals("")){
			return null;
		}
		bundle.add("touid", touid);
		bundle.add("tag", messageTag);
		bundle.add("type", String.valueOf(type));
		String url = getApiServer() +"/user/api/revokeMessage";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 *  阅后即焚/隐私模式开关(/user/api/privacyMode)
	 * @param touid true String 发送到的对象uid
	 */
	public IMResponseState privacyMode(int status, String touid) throws IMException{
		IMParameters bundle = new IMParameters();
		bundle.add("uid", IMCommon.getUserId(ChatApplication.getInstance()));
		bundle.add("touid", touid);
		bundle.add("status", String.valueOf(status));
		String url = getApiServer() +"/user/api/privacyMode";
		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {
				return new IMResponseState(new JSONObject(reString));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}


    /**
     * 上传文件
     * @param msg
     * @param path
     * @return
     * @throws IMException
     */
	public UploadFile uploadChatFile(ChatMessage msg, String path) throws IMException{
        IMParameters bundle = new IMParameters();
		//APP参数
		bundle.add("appkey", getAppKey());
        //业务参数
        bundle.add("uid", msg.getFromId());
        bundle.add("fromId", msg.getFromId());
        bundle.add("toId", msg.getToId());
        //文件参数
        if(!TextUtils.isEmpty(path)){
            List<MoreFile> fileList = new ArrayList<MoreFile>();
            fileList.add(new MoreFile("file_upload", path));
            bundle.addFile("fileUpload", fileList);
        }
        //接口地址
		String url = APIConfig.getStorageServer() +"/storage/upload_file";

		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")  && reString.startsWith("{")){
			try {

				UploadFileResult ret=UploadFileResult.convertToObject(reString);
				if(ret.code==0){//成功
					return ret.data;
				}else {
					//出错了。
					return null;
				}


			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * 上传图片
	 * @param msg
	 * @param path
	 * @return
	 * @throws IMException
	 */
	public UploadImage uploadChatImage(ChatMessage msg, String path) throws IMException{
		IMParameters bundle = new IMParameters();
		//APP参数
		bundle.add("appkey", getAppKey());
		//业务参数
		bundle.add("uid", msg.getFromId());
		bundle.add("fromId", msg.getFromId());
		bundle.add("toId", msg.getToId());
		//文件参数
		if(!TextUtils.isEmpty(path)){
			List<MoreFile> fileList = new ArrayList<MoreFile>();
			fileList.add(new MoreFile("image_upload", path));
			bundle.addImage("imageUpload", fileList);
		}
		//接口地址
		String url = APIConfig.getStorageServer() +"/storage/upload_image";

		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {

				UploadImageResult ret=UploadImageResult.convertToObject(reString);
				if(ret.code==0){//成功
					return ret.data;
				}else {
					//出错了。
					return null;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}



	/**
	 * 上传语音
	 * @param msg
	 * @param path
	 * @return
	 * @throws IMException
	 */
	public UploadAudio uploadChatAudio(ChatMessage msg, String path) throws IMException{
		IMParameters bundle = new IMParameters();
		//APP参数
		bundle.add("appkey", getAppKey());
		//业务参数
		bundle.add("uid", msg.getFromId());
		bundle.add("fromId", msg.getFromId());
		bundle.add("toId", msg.getToId());
		bundle.add("audioTime", ""+msg.audioData.time);
		//文件参数
		if(!TextUtils.isEmpty(path)){
			List<MoreFile> fileList = new ArrayList<MoreFile>();
			fileList.add(new MoreFile("audio_upload", path));
			bundle.addAudio("audioUpload", fileList);
		}
		//接口地址
		String url = APIConfig.getStorageServer() +"/storage/upload_audio";

		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {

				UploadAudioResult ret=UploadAudioResult.convertToObject(reString);
				if(ret.code==0){//成功
					return ret.data;
				}else {
					//出错了。
					return null;
				}


			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}



	/**
	 * 上传视频
	 * @param msg
	 * @param path
	 * @return
	 * @throws IMException
	 */
	public UploadVideo uploadChatVideo(ChatMessage msg, String path) throws IMException{
		IMParameters bundle = new IMParameters();
		//APP参数
		bundle.add("appkey", getAppKey());
		//业务参数
		bundle.add("uid", msg.getFromId());
		bundle.add("fromId", msg.getFromId());
		bundle.add("toId", msg.getToId());

		bundle.add("videoTime", ""+msg.videoData.time);
		//文件参数
		if(!TextUtils.isEmpty(path)){
			List<MoreFile> fileList = new ArrayList<MoreFile>();
			fileList.add(new MoreFile("video_upload", path));
			bundle.addVideo("videoUpload", fileList);
		}

		String thumbPath=msg.videoData.thumb;
		//文件参数
		if(!TextUtils.isEmpty(thumbPath)){
			List<MoreFile> fileList = new ArrayList<MoreFile>();
			fileList.add(new MoreFile("image_upload", thumbPath));
			bundle.addImage("imageUpload", fileList);
		}

		//接口地址
		String url = APIConfig.getStorageServer() +"/storage/upload_video";

		String reString = sendRequest(url, bundle, Utility.HTTPMETHOD_POST,1);
		if(reString != null && !reString.equals("") && !reString.equals("null")){
			try {

				UploadVideoResult ret=UploadVideoResult.convertToObject(reString);
				if(ret.code==0){//成功
					return ret.data;
				}else {
					//出错了。
					return null;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}