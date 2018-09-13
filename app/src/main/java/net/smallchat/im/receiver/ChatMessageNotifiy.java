package net.smallchat.im.receiver;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.RoomTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.IMMessage;
import net.smallchat.im.Entity.UnReadSessionInfo;
import net.smallchat.im.MainActivity;
import net.smallchat.im.R;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.meeting.MettingDetailActivity;
import net.smallchat.im.service.IMService;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ChatMessageNotifiy extends AbstractNotifiy{
	private static final String LOGTAG = "XMPP";
	
	private Context mContext;
	public ChatMessageNotifiy(IMService context) {
		super(context);
		mContext = context;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void notifiy(IMMessage message) {
		Log.d(LOGTAG, "receive message ....");
		ChatMessage chatMessage = null;
		if(message instanceof ChatMessage){
			chatMessage = (ChatMessage) message;
		}else{
			return;
		}

		String fromUid = chatMessage.fromId;

		String msg = null;

		switch (chatMessage.messageType) {
			case MessageType.IMAGE:
				msg = chatMessage.fromName + " <" + mContext.getString(R.string.get_image_message) + " > ";
				break;
			case MessageType.TEXT:
				msg = chatMessage.fromName + " : " +  chatMessage.getContent();
				break;
			case MessageType.AUDIO:
				msg = chatMessage.fromName + " <" + mContext.getString(R.string.get_voice_message) + " > ";
				break;
			case MessageType.VIDEO:
				msg = chatMessage.fromName + " <" + mContext.getString(R.string.get_smallvideo_message) + " > ";
				break;
			case MessageType.REDPACKET:
				msg = chatMessage.fromName + " <" + mContext.getString(R.string.get_redpacket_message) + " > ";
				break;
			case MessageType.LOCATION:
				msg = chatMessage.fromName + " <" + mContext.getString(R.string.get_location_message) + " > ";
				break;

			default:
				break;
		}

		Log.d("im_msg_content=",msg);

		Notification.Builder builder = new Notification.Builder(mContext);
		builder.setSmallIcon(R.drawable.tab_bar_icon_comment_d);
		builder.setTicker(chatMessage.fromName + mContext.getResources().getString(R.string.send_one_msg));
		builder.setWhen(System.currentTimeMillis());

		//notification.icon = R.drawable.ic_launcher; // 设置通知的图标
		long currentTime = System.currentTimeMillis();
		int def = 0;
		if (currentTime - IMCommon.getNotificationTime(mContext) > IMCommon.NOTIFICATION_INTERVAL) {
			if(/*IMCommon.getLoginResult(mContext).isAcceptNew*/IMCommon.getOpenSound(mContext)){
				if(IMCommon.getLoginResult(mContext).isOpenVoice){
					def |= Notification.DEFAULT_SOUND;
				}
				if(IMCommon.getLoginResult(mContext).isOpenShake){
					def |= Notification.DEFAULT_VIBRATE;
				}
			}
			IMCommon.saveNotificationTime(mContext, currentTime);
		}
		def |= Notification.DEFAULT_LIGHTS;
		builder.setDefaults(def);
		builder.setAutoCancel(true);

		int acceptId =  chatMessage.getFromId().hashCode();
		Login user = new Login();
		SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
		int isGetMsg = 0;
		if(chatMessage.chatType == ChatType.PrivateMessage){
			acceptId = chatMessage.getFromId().hashCode();
			user.uid = chatMessage.getFromId();
			user.nickname = chatMessage.fromName;
			user.headSmall = chatMessage.fromUrl;
			UserTable userTable = new UserTable(dbDatabase);
			Login dbLogin = userTable.query(chatMessage.getFromId());
			if(dbLogin!=null){
				isGetMsg = dbLogin.isGetMsg;
			}
		}else {
			acceptId = chatMessage.getToId().hashCode();
			user.uid = chatMessage.getToId();
			user.nickname = chatMessage.toName;
			user.headSmall = chatMessage.toUrl;
			RoomTable roomTable = new RoomTable(dbDatabase);
			Room room = roomTable.query(chatMessage.getToId());
			if(room!=null ){
				isGetMsg = room.isgetmsg;
			}
		}

		try {
			ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			if(cn.getClassName().equals(cn.getPackageName() + ".ChatMainActivity")){
				if(FeatureFunction.isAppOnForeground(mContext)){
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(chatMessage.chatType == ChatType.MeetingMessage){
			Intent intent = new Intent(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP);
			intent.putExtra("found_type", 1);
			mContext.sendBroadcast(intent);
			mContext.sendBroadcast(new Intent(MettingDetailActivity.ACTION_SHOW_NEW_MEETING_TIP));
		}else{
			mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
			mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
		}

		if(isGetMsg == 0){
			return;
		}
		if(!FeatureFunction.isAppOnForeground(mContext) && !IMCommon.getLoginResult(mContext).isAcceptNew){
			return;
		}

		Intent intent = new Intent(mContext, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("data", user);
		intent.putExtra("chatnotify", true);
		intent.putExtra("type", chatMessage.chatType);

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, chatMessage.getToId().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		//notification.setLatestEventInfo(mContext, mContext.getString(R.string.get_new_message), msg, contentIntent);
		//getNotificationManager().notify(acceptId, notification);

		SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
		SessionTable table = new SessionTable(db);
		UnReadSessionInfo sessionInfo = table.queryUnReadSessionInfo();

		String notificationTitle = "";
		String notificationContent = "";
		int badgeCount = 1 ;
		if (sessionInfo.sessionCount > 1) {
			notificationTitle = mContext.getString(R.string.chat_app_name);

			notificationContent = sessionInfo.sessionCount + mContext.getString(R.string.contact_count)
					+ mContext.getString(R.string.send_in) + sessionInfo.msgCount
					+ mContext.getString(R.string.msg_count_tip);
			badgeCount=sessionInfo.msgCount;
		}
		else {
			notificationTitle = chatMessage.fromName;

			if (sessionInfo.msgCount > 1) {
				notificationContent = mContext.getString(R.string.send_in) + sessionInfo.msgCount
						+ mContext.getString(R.string.msg_count_tip);
				badgeCount=sessionInfo.msgCount;


			}
			else {
				notificationContent = chatMessage.getContent();
				badgeCount=1;
			}
		}

		ShortcutBadger.applyCount(mContext, badgeCount); //显示左面图标数字
		builder.setContentIntent(contentIntent);
		builder.setContentTitle(notificationTitle);
		builder.setContentText(notificationContent);
		getNotificationManager().notify(0, builder.getNotification());

	}
}
