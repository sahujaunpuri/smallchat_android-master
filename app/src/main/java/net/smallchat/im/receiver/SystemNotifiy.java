package net.smallchat.im.receiver;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.NotifiyMessage;
import net.smallchat.im.Entity.MessagePrivacyMode;
import net.smallchat.im.api.IMException;
import net.smallchat.im.friendcircle.FriendCircleActivity;
import net.smallchat.im.R;
import net.smallchat.im.MainActivity;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.mine.MyGroupListActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.RoomTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.NewFriendItem;
import net.smallchat.im.Entity.NotifiyType;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.IMMessage;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.fragment.ContactsFragment;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.service.IMService;

public class SystemNotifiy extends AbstractNotifiy{
	private static final String LOGTAG = "XMPP";
	public static final int NOTION_ID = 10023;

	private Context mContext;

	public SystemNotifiy(IMService context) {
		super(context);
		mContext = context;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	@Override
	public void notifiy(IMMessage message) {
		Log.d(LOGTAG,"收到系统消息");
		if(message instanceof NotifiyMessage){
			NotifiyMessage notifiyMessage = (NotifiyMessage) message;
			SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();

			SessionTable sessionTable = new SessionTable(db);
			MessageTable messageTable = new MessageTable(db);
			Session session = null;
			String msg = "";
			//int validType = -1; //0-添加 1-已添加 2-等待验证 3-同意对方的请求
			Log.d(LOGTAG,"系统消息 类型"+ notifiyMessage.getType());
			switch (notifiyMessage.getType()) {
				case NotifiyType.SYSTEM_MSG:
					Log.d(LOGTAG,"系统消息");
				msg = ChatApplication.getInstance().getResources().getString(R.string.system_info);
				break;
				case NotifiyType.DELETE_FRIEND:
					Log.d(LOGTAG,"删除好友通知");
					mContext.sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
					return;
				case NotifiyType.ADD_FRIEND_APPLY://申请添加 - 添加  0
				case NotifiyType.ADD_FRIEND_ACCEPT://同意添加 - 1
					int validType = -1; //0-添加 1-已添加 2-等待验证 3-同意对方的请求
					if(notifiyMessage.getType() == NotifiyType.ADD_FRIEND_APPLY){
						msg = ChatApplication.getInstance().getResources().getString(R.string.apply_friend);
						mContext.sendBroadcast(new Intent(ContactsFragment.ACTION_SHOW_NEW_FRIENDS));
						mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_CONTACT_NEW_TIP));
						IMCommon.saveContactTip(mContext, 1);
						validType = 3;
					}else if(notifiyMessage.getType() == NotifiyType.ADD_FRIEND_ACCEPT){
						validType = 1;
						mContext.sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
					}

					checkFriendsNotify(notifiyMessage.getUser(),validType, notifiyMessage.getContent());
					Intent refreshIntent = new Intent(GlobalParam.ACTION_REFRESH_NEW_FRIENDS);
					mContext.sendBroadcast(refreshIntent);

					if(notifiyMessage.getType() == NotifiyType.ADD_FRIEND_ACCEPT){
						return;
					}
					//msg = BMapApiApp.getInstance().getResources().getString(R.string.add_friend_success);
					break;
				case NotifiyType.ADD_FRIEND_REFUSE:
					msg = ChatApplication.getInstance().getResources().getString(R.string.refused_friend_success);
					return;

				case NotifiyType.EXIT_ROOM://用户退出群
					Log.d(LOGTAG,"用户推出群聊");
					mContext.sendBroadcast(new Intent(MyGroupListActivity.REFRESH_ROOM_ACTION));
					return;
				case NotifiyType.GROUP_KICK_OUT://管理员删除用户
					Log.d(LOGTAG,"管理员踢出用户");
					if(notifiyMessage.getUserId().equals(IMCommon.getUserId(mContext))){
						session = sessionTable.query(notifiyMessage.roomID, ChatType.GroupMessage);
						if(session != null){
							messageTable.delete(notifiyMessage.roomID, ChatType.GroupMessage);
							sessionTable.delete(notifiyMessage.roomID, ChatType.GroupMessage);

							mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
							mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
							NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
							notificationManager.cancel(0);
						}
					}


					//msg = mContext.getString(R.string.you_have_been_kick_out_group);
					Intent kickIntent = new Intent(GlobalParam.BE_KICKED_ACTION);
					kickIntent.putExtra("id", notifiyMessage.roomID);
					kickIntent.putExtra("uid", notifiyMessage.getUserId());
					mContext.sendBroadcast(kickIntent);
					mContext.sendBroadcast(new Intent(MyGroupListActivity.REFRESH_ROOM_ACTION));

					return;
				case NotifiyType.CHANGE_ROOM_NAME://管理员编辑会话名称
					RoomTable roomTable = new RoomTable(db);
					SessionTable sesTable = new SessionTable(db);
					Room oldRoom = roomTable.query(notifiyMessage.roomID);
					Session ses= sesTable.query(notifiyMessage.roomID, ChatType.GroupMessage);
					if(ses!=null){
						ses.name = notifiyMessage.roomName;
						sessionTable.update(ses, ChatType.GroupMessage);
					}
					if(oldRoom != null){
						oldRoom.groupName = notifiyMessage.roomName;
						roomTable.update(oldRoom);
					}
					Intent intent = new Intent(GlobalParam.ACTION_RESET_GROUP_NAME);
					intent.putExtra("group_id", notifiyMessage.roomID);
					intent.putExtra("group_name", notifiyMessage.roomName);
					mContext.sendBroadcast(intent);
					return;
				case NotifiyType.ROOM_EDIT_MY_NICKNAME://群组用户修改自己的群昵称
					Intent groupNameIntent = new Intent(GlobalParam.ACTION_RESET_MY_GROUP_NAME);
					groupNameIntent.putExtra("my_group_nickname", notifiyMessage.userName);
					groupNameIntent.putExtra("group_id", notifiyMessage.roomID);
					groupNameIntent.putExtra("uid", notifiyMessage.getUserId());
					mContext.sendBroadcast(groupNameIntent);
					break;
				case NotifiyType.JOIN_ROOM:// 一个用户加入会话
					Log.d(LOGTAG,"用户加入");
					return;
				case NotifiyType.DELETE_ROOM://管理员删除会话
					Log.d(LOGTAG,"管理员删除群");
					session = sessionTable.query(notifiyMessage.roomID, ChatType.GroupMessage);
					if(session != null){
						messageTable.delete(notifiyMessage.roomID, ChatType.GroupMessage);
						sessionTable.delete(notifiyMessage.roomID, ChatType.GroupMessage);

						mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
						mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
					}
					NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.cancel(0);

					//msg = mContext.getString(R.string.you_have_been_kick_out_group);
					Intent kicIntent = new Intent(GlobalParam.ROOM_BE_DELETED_ACTION);
					kicIntent.putExtra("roomID", notifiyMessage.roomID);
					mContext.sendBroadcast(kicIntent);
					mContext.sendBroadcast(new Intent(MyGroupListActivity.REFRESH_ROOM_ACTION));
					return;
				case NotifiyType.ADD_LIKE://赞
					Log.d(LOGTAG,"点赞通知");
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MOVING_DETAIL));
					if(notifiyMessage.getUserId().equals(IMCommon.getUserId(mContext))){
						return;
					}
					List<NotifiyMessage> list = IMCommon.getMovingResult(mContext);
					if(list == null ){
						list = new ArrayList<NotifiyMessage>();
					}
					boolean isExitsi = false;
					if(list!=null && list.size()>0){
						for (int i = 0; i < list.size(); i++) {
							if(list.get(i).shareId  == notifiyMessage.shareId && list.get(i).getType() == notifiyMessage.getType()){
								isExitsi= true;
							}

							if(i == list.size() -1 ){
								if(!isExitsi){
									list.add(0, notifiyMessage);
									break;
								}
							}
						}
					}else{
						list.add(0, notifiyMessage);
					}

					IMCommon.saveMoving(mContext, list);

					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MYALBUM_MESSAGE));
					try {
						ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
						ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
						if(cn.getClassName().equals(cn.getPackageName() + ".FriendCircleActivity")){
							if(FeatureFunction.isAppOnForeground(mContext)){
								//return;
							}
						}else{
							mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}


					mContext.sendBroadcast(new Intent(FriendCircleActivity.MSG_REFRESH_MOVIINF));
					return;
				case NotifiyType.CANCLE_LIKE://取消赞
					Log.e(LOGTAG,"取消赞");
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MOVING_DETAIL));
					if(notifiyMessage.getUserId().equals(IMCommon.getUserId(mContext))){
						return;
					}
					List<NotifiyMessage> cancleLlist = IMCommon.getMovingResult(mContext);
					List<NotifiyMessage> tempCancleLlist = new ArrayList<NotifiyMessage>();
					for (NotifiyMessage notifiy : cancleLlist) {
						if(notifiy.shareId == notifiy.shareId && notifiy.getType() == NotifiyType.ADD_LIKE){

						}else{
							tempCancleLlist.add(notifiy);
						}
					}

					IMCommon.saveMoving(mContext, tempCancleLlist);
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MYALBUM_MESSAGE));
					mContext.sendBroadcast(new Intent(FriendCircleActivity.MSG_REFRESH_MOVIINF));
					return;
				case NotifiyType.REPLY://评论
					Log.d(LOGTAG,"评论通知");
					if(notifiyMessage.getUserId().equals(IMCommon.getUserId(mContext))){
						return;
					}
					List<NotifiyMessage> replyLlist = IMCommon.getMovingResult(mContext);
					if(replyLlist == null ){
						replyLlist = new ArrayList<NotifiyMessage>();
					}
					replyLlist.add(0, notifiyMessage);
					IMCommon.saveMoving(mContext, replyLlist);
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MYALBUM_MESSAGE));
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MYALBUM_MESSAGE));
					try {
						ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
						ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
						if(cn.getClassName().equals(cn.getPackageName() + ".FriendCircleActivity")){
							if(FeatureFunction.isAppOnForeground(mContext)){
								//return;
							}
						}else{
							mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}


					mContext.sendBroadcast(new Intent(FriendCircleActivity.MSG_REFRESH_MOVIINF));
					return;
				case NotifiyType.METTING_APPLY://申请加入会议
					Log.d(LOGTAG,"申请加入会议");
					//found_type
					Intent applyIntent = new Intent(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP);
					applyIntent.putExtra("found_type", 2);
					mContext.sendBroadcast(applyIntent);
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
					return;

				case NotifiyType.METTING_APPLY_AGREE://同意申请加入会议
				case NotifiyType.METTING_APPLY_REFUSE://不同意申请加入会议
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_MEETING_DETAIL));
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
					return;

				case NotifiyType.MEETING_USER_KICK_OUT://用户被踢出会议
			/*	if(notifiyMessage.getUserId().equals(IMCommon.getUserId(mContext))){*/
					session = sessionTable.query(notifiyMessage.roomID, ChatType.MeetingMessage);
					if(session != null){
						messageTable.delete(notifiyMessage.roomID, ChatType.MeetingMessage);
						sessionTable.delete(notifiyMessage.roomID, ChatType.MeetingMessage);

						mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
						mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
						NotificationManager snotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
						snotificationManager.cancel(0);
					}
				/*}*/


					//msg = mContext.getString(R.string.you_have_been_kick_out_group);
					Intent meetKickIntent = new Intent(GlobalParam.BE_KICKED_ACTION);
					meetKickIntent.putExtra("id", notifiyMessage.roomID);
					meetKickIntent.putExtra("type", 1);
					meetKickIntent.putExtra("hintMsg", notifiyMessage.getContent());
					meetKickIntent.putExtra("uid", notifiyMessage.getUserId());
					mContext.sendBroadcast(meetKickIntent);
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_DESTROY_MEETING_PAGE));
					return;

				case NotifiyType.All_USER_ACCEPT_KICK_OUT://所有用户都会受到踢出参会人员的通知
					return;

				case NotifiyType.REVOKE_MESSAGE_APPLY://申请撤回消息
					Log.d("SYS_NOTIFY","申请撤回消息");
					revokeMessageNotify(messageTable,notifiyMessage.getUserId(),notifiyMessage.getContent());

					try {
						IMCommon.getIMServerAPI().revokeMessage(602,notifiyMessage.getContent(),notifiyMessage.getUserId());
					} catch (IMException e) {
						e.printStackTrace();
					}


					return;
				case NotifiyType.REVOKE_MESSAGE_SUCCESS://撤回消息成功
					Log.d("SYS_NOTIFY","撤回消息 成功");
					revokeMessageNotify(messageTable,notifiyMessage.getUserId(),notifiyMessage.getContent());
					return;
				case NotifiyType.REVOKE_MESSAGE_FAILED://撤回消息失败
					Log.d("SYS_NOTIFY","撤回失败");
					//revokeMessageNotify(messageTable,notifiyMessage.getUserId(),notifiyMessage.getContent());
					return;
				case NotifiyType.PRIVACY_MODE_OPEN: {//开启隐私模式
					Log.d("SYS_NOTIFY","开启隐私模式");
					privacyMode(notifiyMessage.getUserId(), MessagePrivacyMode.PrivacyModeEnabled);

					Intent chatPrivacyModeIntent = new Intent(GlobalParam.ACTION_REFRESH_CHAT_PRIVACY_MODE);
					chatPrivacyModeIntent.putExtra("uid", notifiyMessage.getUserId());
					chatPrivacyModeIntent.putExtra("flag", MessagePrivacyMode.PrivacyModeEnabled);
					mContext.sendBroadcast(chatPrivacyModeIntent);


					};
					return;
				case NotifiyType.PRIVACY_MODE_CLOSE: {//关闭隐私模式

					Log.d("SYS_NOTIFY","关闭隐私模式");

					privacyMode(notifiyMessage.getUserId(), MessagePrivacyMode.Normal);
					//通知更新


					Intent chatPrivacyModeIntent = new Intent(GlobalParam.ACTION_REFRESH_CHAT_PRIVACY_MODE);
					chatPrivacyModeIntent.putExtra("uid", notifiyMessage.getUserId());
					chatPrivacyModeIntent.putExtra("flag", MessagePrivacyMode.Normal);
					mContext.sendBroadcast(chatPrivacyModeIntent);
					};
					return;
				case NotifiyType.DIFFERENT_PLACES_LOGIN: {//异地登录
					Log.d("SYS_NOTIFY","异地登录");
					//异地登录动作
					Intent DifferentPlacesLoginIntent = new Intent(GlobalParam.ACTION_DIFFERENT_PLACES_LOGIN);
					mContext.sendBroadcast(DifferentPlacesLoginIntent);
				};
				return;
				default:
					return;
			}
			Intent intent2= new Intent(NotifySystemMessage.ACTION_NOTIFY_SYSTEM_MESSAGE);
			//updateintent.putExtra(NotifySystemMessage.EXTRAS_NOTIFY_SYSTEM_MESSAGE, notifiyMessage);
			mContext.sendBroadcast(intent2);

			try {
				ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
				ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
				if(cn.getClassName().equals(cn.getPackageName() + ".NewFriendsActivity")){
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			//isAcceptNew
			if(!IMCommon.getLoginResult(mContext).isAcceptNew){
				return;
			}


			// Notification
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher; // 设置通知的图标
			long currentTime = System.currentTimeMillis();
			if (currentTime - IMCommon.getNotificationTime(mContext) > IMCommon.NOTIFICATION_INTERVAL) {
				if(/*IMCommon.getLoginResult(mContext).isAcceptNew*/IMCommon.getOpenSound(mContext)){
					if(IMCommon.getLoginResult(mContext).isOpenVoice){
						notification.defaults |= Notification.DEFAULT_SOUND;
					}
					if(IMCommon.getLoginResult(mContext).isOpenShake){
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					}
				}
				IMCommon.saveNotificationTime(mContext, currentTime);
			}

			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// 音频将被重复直到通知取消或通知窗口打开。
			// notification.flags |= Notification.FLAG_INSISTENT;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.when = currentTime;

			Intent intent = new Intent(getService(), MainActivity.class);
			intent.putExtra("notify", true);

			PendingIntent contentIntent = PendingIntent.getActivity(getService(), NOTION_ID,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			//
//			notification.setLatestEventInfo(getService(), mContext.getString(R.string.has_new_notification), msg,
//					contentIntent);
//			getNotificationManager().notify(NOTION_ID, notification);
			//for android 6.0 通知栏
			Notification noti = new Notification.Builder(getService())
					.setContentTitle(mContext.getString(R.string.has_new_notification))
					.setContentText(msg)
					.setContentIntent(contentIntent).build();
		}
	}

	/**
	 * 撤回消息
	 * @param userId
	 * @param messageTag
	 */
	private void revokeMessageNotify(MessageTable messageTable,String userId,String messageTag){
		messageTable.delete(messageTag);

		//发送消息，撤回成功

	}

	/**
	 * 隐私模式/阅后即焚
	 * @param userId
	 * @param flag
	 */
	private void privacyMode(String userId,int flag){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
		UserTable userTable = new UserTable(db);
		userTable.updateIsPrivacyMode(userId,flag);
	}

	/**
	 * 保存新的朋友数据
	 * @param login
	 * @param type 0-添加 1-已添加 2-等待验证
	 */
	private void checkFriendsNotify(Login login,int type,String content){
		List<NewFriendItem> mUserList = new ArrayList<NewFriendItem>();
		mUserList.add(new NewFriendItem(login.phone,login.uid,login.name,login.headSmall,
				type,content,IMCommon.getUserId(mContext),1));

		//获取系统中保存的新的朋友
		List<NewFriendItem> lastNewFriendsList = IMCommon.getNewFriendItemResult(mContext);

		boolean isExitsLastData = false;
		for (int i = 0; i < mUserList.size(); i++) {
			String currentUid = mUserList.get(i).uid;
			String currentPhone = mUserList.get(i).phone;
			if(lastNewFriendsList!=null && lastNewFriendsList.size()>0){
				isExitsLastData = true;
				for (int j = 0; j < lastNewFriendsList.size(); j++) {
					if(lastNewFriendsList.get(j).uid.equals(currentUid)){
						mUserList.get(i).colorBgtype = 0;
						/*	if(lastNewFriendsList.get(j).type!=0){
							mUserList.get(i).type = lastNewFriendsList.get(j).type;
						}*/
						break;
					}
				}
			}
		}
		if(isExitsLastData){
			for (int l = 0; l < lastNewFriendsList.size(); l++) {
				boolean isExits = true;
				for (int m = 0; m < mUserList.size(); m++) {
					if(mUserList.get(m).uid.equals(lastNewFriendsList.get(l).uid)){
						isExits= false;
					}
					if(m == mUserList.size() -1 ){
						if(isExits){
							mUserList.add(lastNewFriendsList.get(l));
							break;
						}
					}
				}
			}
		}
		List<NewFriendItem> newList = new ArrayList<NewFriendItem>();
		List<NewFriendItem> oldList = new ArrayList<NewFriendItem>();
		for (int i = 0; i < mUserList.size(); i++) {
			if (mUserList.get(i).colorBgtype == 1) {
				newList.add(mUserList.get(i));
			}else if(mUserList.get(i).colorBgtype == 0){
				oldList.add(mUserList.get(i));
			}
		}
		if(mUserList!=null && mUserList.size()>0){
			mUserList.clear();
		}
		mUserList.addAll(newList);
		mUserList.addAll(oldList);
		IMCommon.saveNewFriendsResult(mContext, mUserList,mUserList.size());
	}

}
