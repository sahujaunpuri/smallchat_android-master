package net.smallchat.im.receiver;

import java.util.List;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MessageSendState;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.Entity.SessionList;
import net.smallchat.im.global.IMCommon;

import org.json.JSONObject;
import net.smallchat.im.service.XMPPManager;

/**
 * 
 * 功能： 接收到发送的消息.通过广播发送出去 <br />
 * 日期：2013-5-6<br />
 * 地点：无穷大软件<br />
 * 版本：ver 1.0<br />
 * 
 * guoxin
 * @since
 */
public class NotifyChatMessage implements NotifyMessage {
	private static final String TAG = "XMPP";

	/**
	 * 聊天服务发来聊天信息, 广播包<br/>
	 * 附加参数: {@link NotifyChatMessage#EXTRAS_NOTIFY_CHAT_MESSAGE}
	 */
	public static final String ACTION_NOTIFY_CHAT_MESSAGE = "net.smallchat.im.sns.notify.ACTION_NOTIFY_CHAT_MESSAGE";
	/**
	 * 某消息列表有更新，注意查收
	 * 附加参数: {@link NotifyChatMessage#EXTRAS_NOTIFY_SESSION_MESSAGE}
	 */
	public static final String ACTION_NOTIFY_SESSION_MESSAGE = "net.smallchat.im.sns.notify.ACTION_NOTIFY_SESSION_MESSAGE";

	/**
	 * 更新语音转文字成功之后语音消息对应的文本信息通知
	 */
	public static final String ACTION_CHANGE_VOICE_CONTENT = "com.teamchat.chat.intent.action.ACTION_CHANGE_VOICE_CONTENT";

	
	/**
	 * 附加信息<br/> {@link ChatMessage}
	 */
	public static final String EXTRAS_NOTIFY_CHAT_MESSAGE = "extras_message";
	/**
	 * 附加信息<br/> {@link SessionList}
	 */
	public static final String EXTRAS_NOTIFY_SESSION_MESSAGE = "extras_session";

	private ChatMessageNotifiy chatMessageNotifiy;
	public XMPPManager xmppManager;
	public Login userInfoVo;
	

	public NotifyChatMessage(XMPPManager xmppManager) {
		super();
		this.xmppManager = xmppManager;
		this.userInfoVo = xmppManager.getIMService().getUserInfoVo();
		chatMessageNotifiy = new ChatMessageNotifiy(xmppManager.getIMService());
	}

	@Override
	public void notifyMessage(String msg) {
		Log.e(TAG, msg);
		try {
			
			if(msg == null || msg.equals("" )
					|| msg.equals("This room is not anonymous.")){
				return;
			}
			
			ChatMessage info = new ChatMessage(new JSONObject(msg));
			
			if(info.chatType != ChatType.PrivateMessage && info.getFromId().equals(IMCommon.getUserId(xmppManager.getIMService()))){
				return;
			}
			info.sendState = MessageSendState.SEND_STATE_SUCCESS;

			if (info != null) {
				Log.d("im_message_content","保存消息内容：messageType="+info.messageType+"\t"+info.content);
				saveMessageInfo(info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveMessageInfo(ChatMessage info) {
		
		if(info.messageType == MessageType.AUDIO){
			info.setSendState(4);
		}
		
		SQLiteDatabase dbDatabase = DBHelper.getInstance(xmppManager.getIMService()).getWritableDatabase();
		MessageTable table = new MessageTable(dbDatabase);
		table.insert(info);
		
		Session session = new Session();
		if(info.chatType == ChatType.PrivateMessage){//私聊消息
			session.setFromId(info.fromId);
			session.name = info.fromName;
			session.heading = info.fromUrl;
			session.lastMessageTime = info.time;
		}else {//群聊消息
			session.setFromId(info.toId);
			session.name = info.toName;
			session.heading = info.toUrl;
			session.lastMessageTime = info.time;
		}
		
		session.type = info.chatType;
		SessionTable sessionTable = new SessionTable(dbDatabase);
		Session existSession = sessionTable.query(session.getFromId(), info.chatType);
		if(existSession != null){
			if(existSession.isTop!=0){
				List<Session> exitsSesList = sessionTable.getTopSessionList();
				if(exitsSesList!=null && exitsSesList.size()>0){
					for (int i = 0; i < exitsSesList.size(); i++) {
						Session ses = exitsSesList.get(i);
						if(ses.isTop>1){
							ses.isTop = ses.isTop-1;
							sessionTable.update(ses, ses.type);
						}
					}
				}
				session.isTop = sessionTable.getTopSize();
			}
			sessionTable.update(session, info.chatType);
		}else {
			sessionTable.insert(session);
		}
		
			sendBroad(info);
		
	}

	private void sendBroad(ChatMessage info) {
		Log.d(TAG, "sendBroad()");
		
		/*Intent refreshIntent = new Intent(ChatsTab.ACTION_REFRESH_SESSION);
		refreshIntent.putExtra("message", info);
		xmppManager.getIMService().sendBroadcast(refreshIntent);*/
		
		Intent intent = new Intent(ACTION_NOTIFY_CHAT_MESSAGE);
		intent.putExtra(EXTRAS_NOTIFY_CHAT_MESSAGE, info);
		//intent.putExtra(EXTRAS_NOTIFY_SESSION_MESSAGE, sessionList);
		chatMessageNotifiy.notifiy(info);
		if (xmppManager != null && xmppManager.getIMService() != null) {
			xmppManager.getIMService().sendBroadcast(intent);
		}
	}
}
