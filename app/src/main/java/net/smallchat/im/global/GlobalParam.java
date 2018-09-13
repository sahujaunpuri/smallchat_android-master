package net.smallchat.im.global;

import android.view.View;

import net.smallchat.im.R;

public class GlobalParam {
	
	
	/**
	 * 用户被迫下线通知
	 */
	public static final String ACTION_SHOW_TOAST = "net.smallchat.im.intent.action.show.toast";
	
	/**
	 * 销毁当前页面通知
	 */
	public static final String ACTION_DESTROY_CURRENT_ACTIVITY = "net.smallchat.im.intent.action.destroy.current.activity";
	
	public final static String ACTION_NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
	/** 通过广播属性列表 */
	public static final String ACTION_UPDATE_SESSION_COUNT = "net.smallchat.im.intent.action.ACTION_UPDATE_SESSION_COUNT";
	/** 通过广播刷新会议数据 *//*
	public static final String ACTION_UPDATE_MEETING_SESSION_COUNT = "net.smallchat.im.intent.action.update.meeting.session.count";
*/
	
	
	/** 通过广播刷新通知 */
	public static final String ACTION_REFRESH_NOTIFIY = "net.smallchat.im.intent.action.ACTION_REFRESH_NOTIFIY";
	/** 切换到首页 */
	public static final String ACTION_CALLBACK = "net.smallchat.im.intent.action.ACTION_CALLBACK";
	/** 刷新好友列表 */
	public static final String ACTION_REFRESH_FRIEND = "net.smallchat.im.intent.action.ACTION_REFRESH_FRIEND";
	/** 注销 */
	public static final String ACTION_LOGIN_OUT = "net.smallchat.im.intent.action.ACTION_LOGIN_OUT";
	
	public final static String ACTION_SHOW_REGISTER_REQUEST = "net.smallchat.im.intent.register.sendRequest";
	public final static String SWITCH_TAB = "im_switch_tab";
	public final static String CANCLE_COMPLETE_USERINFO_ACTION = "net.smallchat.im.cancle.complete.userinfo";
	public final static String EXIT_ACTION = "im_exit";
	//public final static String RESET_TAB = "im_reset_tab_action";
	public final static String SWITCH_LANGUAGE_ACTION = "im_switch_language_action";
	public final static String REFRESH_ALIAS_ACTION = "im_refresh_alias_action";
	public final static String REFRESH_SESSION_ACTION = "im_refresh_session_action";
	
	
	//更新新的朋友列表数据
	public final static String ACTION_REFRESH_NEW_FRIENDS_LIST = "im_refresh_new_friends_list_action";
	
	
	
	//取消订阅号右上角的泡泡
	public final static String ACTION_CANCLE_NEW_ORDER = "im_action_cancle_order_tip";
	//取消服务号右上角的泡泡
	public final static String ACTION_CANCLE_NEW_SERVICE = "im_action_cancle_service_tip";
	
	//取消陌生人右上角的泡泡
	public final static String ACTION_CANCLE_NEW_OUTLANDER = "im_action_cancle_outlander_tip";
	
	//当记事本发布一条动态信息时，刷新在群聊的聊天界面中
	public final static String ACTION_REFRESH_NOTEPAD_RECORD = "im_action_refresh_notepad_record";
	
	
	//刷新微博的评论、收藏、赞、转发数
	public final static String ACTION_REFRESH_WEIBO_COUNT = "im_action_refresh_weibo_count";
	public final static String ACTION_COLLECTION_WEIBO = "im_action_collection_weibo";
	public final static String ACTION_LIKE_WEIBO = "im_action_like_weibo";
	
	//更改投票后的状态
	public final static String ACTION_CHANGE_ISVOTE = "im_action_change_isvote";
	
	public final static String ACTION_REFRESH_NEW_FRIENDS = "im_action_refresh_new_friends_list";
	
	//更新群组备注名
	public final static String ACTION_RESET_GROUP_NAME ="im_action_reset_group_name";
	
	//群组用户更改自己的群昵称
	public final static String ACTION_RESET_MY_GROUP_NAME ="im_action_reset_my_group_name";
	
	//朋友圈有新的评论和赞时在发现模块的朋友圈中显示泡泡
	public final static String ACTION_SHOW_NEW_FRIENDS_LOOP ="im_action_show_new_friends_loop";
	public final static String ACTION_HIDE_NEW_FRIENDS_LOOP ="im_action_hide_new_friends_loop";
	
	//会议有新的内容时显示泡泡
	public final static String ACTION_SHOW_NEW_MEETING ="im_action_fragment_show_new_meeting";
	public final static String ACTION_HIDE_NEW_MEETING ="im_action_fragment_hide_new_meeting";
	
	//管理员同意登陆用户的申请会议请求后会议详情
	public final static String ACTION_UPDATE_MEETING_DETAIL ="im_action_update_meeting_detail";
	//刷新会议列表
	public final static String ACTION_REFRESH_MEETING_LIST ="im_action_refresh_meeting_list";
	
	//
	public final static String ACTION_DESTROY_MEETING_PAGE ="im_action_destroy_meeting_detail";
	
	//显示发现按钮旁边的泡泡
	public final static String ACTION_SHOW_FOUND_NEW_TIP ="im_action_show_found_new_tip";
	//隐藏发现按钮旁边的泡泡
	public final static String ACTION_HIDE_FOUND_NEW_TIP ="im_action_hide_found_new_tip";
		
	
	//显示通讯录按钮旁边的泡泡
	public final static String ACTION_SHOW_CONTACT_NEW_TIP ="im_action_show_contact_new_tip";
	
	//隐藏通讯录按钮旁边的泡泡
	public final static String ACTION_HIDE_CONTACT_NEW_TIP ="im_action_hide_contact_new_tip";
	
	//刷新聊天列表中的用户头像
	public final static String ACTION_REFRESH_CHAT_HEAD_URL ="im_action_refresh_chat_head_url";
	

	public final static String BE_KICKED_ACTION = "im_be_kicked_action";
	public final static String BE_EXIT_ACTION = "im_exit_action";
	public final static String ROOM_BE_DELETED_ACTION = "im_room_be_deleted_action";
	public final static String INVITED_USER_INTO_ROOM_ACTION = "im_invited_user_into_room_action";
	public final static String LOGIN_ACTION = "im_login_action";
	
	//在分享详情页删除分享内容时，释放相应的页面
	public final static String ACTION_DESTROY_ACTIVITY_DEL_SHARE= "im_login_action";
	
	public final static String ACTION_REFRESH_MYALBUM_MESSAGE = "im_action_refresh_myalbum_message";
	
	//刷新个人相册详情数据
	public final static String ACTION_REFRESH_MOVING_DETAIL= "im_action_refresh_moving_detail";
	
	//刷新收藏列表
	public final static String ACTION_REFRESH_MY_FAVORITE= "im_action_refresh_my_favorite";


	//刷新隐私模式开关
	public final static String ACTION_REFRESH_CHAT_PRIVACY_MODE = "im_action_refresh_chat_privacy_mode";


	//异地登录
	public final static String ACTION_DIFFERENT_PLACES_LOGIN= "im_action_different_places_login";





	public final static int RESET_PASSWORD_REQUEST = 2314;
	public final static int SHOW_PROGRESS_DIALOG  = 11112;
	public final static int HIDE_PROGRESS_DIALOG  = 11113;
	public final static int MSG_NETWORK_ERROR = 11306;
	public final static int MSG_TIME_OUT_EXCEPTION = 11307;
	public final static int MSG_LOAD_ERROR=11818;
	public final static int LIST_LOAD_FIRST = 501;
	public final static int LIST_LOAD_REFERSH = 502;
	public final static int LIST_LOAD_MORE = 503;	
	public final static int SHOW_LOADINGMORE_INDECATOR = 11105;
	public final static int HIDE_LOADINGMORE_INDECATOR = 11106;
	public final static int LOGIN_REQUEST = 2312;
	public final static int SHOW_SCROLLREFRESH  = 11117;
	public final static int HIDE_SCROLLREFRESH  = 11118;
	public final static int MSG_SHOW_HEADER_IMG  = 11119;
	public final static int MSG_UPLOAD_STATUS  = 11120;
	public final static int MSG_CHECK_STATE  = 11121;
	public final static int MSG_CHECK_SHARE_APPNEWS_STATE  = 11122;
	public final static int MSG_SHOW_LISTVIEW_DATA  = 111221;
	public final static int MSG_CHECK_FRIENDS = 0x00021;
	public final static int MSG_CANCLE_FRIENDS = 0x00025;
	public final static int MSG_VALID_FRIENDS = 0x00024;
	public final static int MSG_AGREE_ADD_FRIENDS_REQUEST = 0x00029;
	public final static int MSG_SHOW_LOAD_DATA = 0x00026;
	
	public final static int REGISTER_REQUEST = 2313;
	public final static int MSG_CHECK_VALID_ERROR = 0x00029;
	public static final int REQUEST_GET_IMAGE_BY_CAMERA = 1002;
	public static final int REQUEST_GET_URI = 101;
	public static final int REQUEST_GET_BITMAP = 124;
	
	public final static int SHOW_UPGRADE_DIALOG = 10001;
	public final static int NO_NEW_VERSION = 11315;
	public static final int SHOW_GUIDE_REQUEST = 6541;
	public static final int SHOW_COMPLETE_REQUEST = 6542;
	public static final int SHOW_COMPLETE_RESULT = 6543;
	public final static int RESULT_EXIT = 702;
	
	//更改群聊公开群
	public final static int MSG_GROUP_PUBLISH_CHECK = 0x0001;
	//更改群聊消息接受
	public final static int MSG_GROUP_ISGET_CHECK = 0x0002;
	public final static int MSG_GET_CONTACT_DATA = 0x00027;
	
	public final static int MSG_UPDATEA_TIP_TIME = 0x00028;
	public final static int MSG_CREAT_ROOM_SUCCESS = 0x00030;
	
	
	//处理通讯录朋友的按钮事件
	public final static int MSG_CLICK_LISTENER = 0x00031;
	//处理修改群名称
	public final static int MSG_CHECK_GROUP_NAME=0x00032;
	//处理修改我的群昵称
	public final static int MSG_CHECK_MY_GROUP_NAME=0x00045;
	
	//处理星标朋友
	public final static int MSG_CHECK_STAR  = 0x00033;
	//处理星标朋友
	public final static int MSG_CHECK_FRIENDS_LOOP_AUTH  = 0x00045;
	
	//处理朋友圈评论泡泡的显示
	public final static int MSG_CHECK_FRIENDS_LOOP_POP_STATUS  = 0x00034;
	//处理朋友圈收藏对话框的显示
	public final static int MSG_SHOW_FRIENDS_FAVORITE_DIALOG  = 0x00035;
	//处理朋友圈底部评论框的显示
	public final static int MSG_SHOW_BOTTOM_COMMENT_MENU = 0x00036;
	public final static int MSG_COMMENT_PRAISE = 0x00037;
	public final static int MSG_COMMENT_STATUS = 0x00038;
	public final static int MSG_PRAISE_STATUS = 0x00039;
	
	public final static int MSG_CHECK_FAVORITE_STATUS = 0x00040;
	public final static int MSG_CHECK_DEL_SHARE_STATUS = 0x00041;
	
	public final static int MSG_CLEAR_EDITEXIT_STATUS = 0x00042;
	
	//弹出选择图片对话框
	public final static int MSG_SHOW_SELECT_BG_DIALOG  = 0x00043;
	public final static int NO_SD_CARD = 0x00044;
	
	//处理删除自己的分享内容
	public final static int MSG_DEL_FRIENDS_CIRCLE = 0x00045;
	
	//处理申请参会结果
	public final static int MSG_CHECK_APPLY_METTING = 0x00046;
	
	//同意用户申请请求
	public final static int MSG_AGREE_APPLY_METTING = 0x00047;
	//不同意用户申请请求
	public final static int MSG_DIS_AGREE_APPLY_METTING = 0x00048;
	
	//清空listview 数据
	public final static int MSG_CLEAR_LISTENER_DATA = 0x00049;
	
	//检测邀请用户参会
	public final static int MSG_CHECK_INVALID_MEETING  = 0x00050;

	//更新加好友个人信息界面
	public final static int MSG_UPDATE_ADD_FRIEND_DETAIL  = 0x00051;

	//处理朋友圈文字被单击的时候，显示全部朋友圈nearing
	public final static int MSG_SHOW_FRIENDS_FULL_TEXT  = 0x00052;

	//登录成功
	public final static int MSG_LOGIN_SUCCESS  = 0x00081;
	//登录失败
	public final static int MSG_LOGIN_FAILED  = 0x00082;



}
