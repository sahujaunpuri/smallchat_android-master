package net.smallchat.im.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.receiver.MultiMessageListener;
import net.smallchat.im.receiver.NotifyChatMessage;
import net.smallchat.im.receiver.NotifyMessage;
import net.smallchat.im.receiver.NotifySystemMessage;
import net.smallchat.im.receiver.PushChatMessage;
import net.smallchat.im.receiver.PushMessage;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;

/**
 * 
 * 功能：聊天监听.监听服务端信息(聊天信息，系统消息等...) <br />
 * 日期：2013-5-5<br />
 * 地点：无穷大软件<br />
 * 版本：ver 1.0<br />
 * 
 * guoxin
 * @since
 */
public class IMMessageManager implements ChatManagerListener {
	private static final String SYSTEM_USER = "beautyas";
	
	private XMPPManager xmppManager;
	private ChatMessageListener chatListener;
	
//	private LruMemoryCache<String, Chat> chatCache = new LruMemoryCache<String, Chat>(6);
	
	private NotifyChatMessage chatMessage;
	private NotifySystemMessage systemMessage;
	
	private PushChatMessage pushChatMessage;
	
	public IMMessageManager(XMPPManager xmppManager) {
		super();
		this.xmppManager = xmppManager;
		chatListener = new ChatListenerImpl();
		chatMessage = new NotifyChatMessage(xmppManager);
		systemMessage = new NotifySystemMessage(xmppManager);
		pushChatMessage = new PushChatMessage(xmppManager);
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		if(!createdLocally){
			chat.addMessageListener(chatListener);
		}
//		chatCache.put(chat.getParticipant().split("@")[0], chat);
	}
	
	public boolean joinRoom(String roomName, Date date){
		
		String jid=roomName + "@conference."
				+ xmppManager.getConnection().getServiceName();
		MultiUserChatManager mcum=MultiUserChatManager.getInstanceFor(xmppManager.getConnection());
		MultiUserChat muc =mcum.getMultiUserChat(jid);

		DiscussionHistory history = new DiscussionHistory();
		//history.setSince(date);
		history.setMaxChars(0);
		muc.addMessageListener(new MultiMessageListener(xmppManager));
		muc.addParticipantStatusListener(new XMPPParticipantStatusListener(xmppManager));
		try {
			try {
				muc.join(xmppManager.getUsername(), null, history, SmackConfiguration.getDefaultPacketReplyTimeout());
			} catch (SmackException.NoResponseException e) {
				e.printStackTrace();
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
			}
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public MultiUserChat createMUC(String groupName){
		try {
			// 创建一个MultiUserChat
			String jid=groupName + "@conference." + xmppManager.getConnection().getServiceName();
			MultiUserChatManager mcum=MultiUserChatManager.getInstanceFor(xmppManager.getConnection());
			MultiUserChat muc =mcum.getMultiUserChat(jid);
			// 创建聊天室
			muc.create(groupName); // roomName房间的名字
			// 获得聊天室的配置表单
			Form form = muc.getConfigurationForm();
			// 根据原始表单创建一个要提交的新表单。
			Form submitForm = form.createAnswerForm();
			// 向要提交的表单添加默认答复
			/*
			 * for (Iterator<FormField> fields = form.getFields(); fields
			 * .hasNext();) { FormField field = (FormField) fields.next(); if
			 * (!FormField.TYPE_HIDDEN.equals(field.getType()) &&
			 * field.getVariable() != null) { // 设置默认值作为答复
			 * submitForm.setDefaultAnswer(field.getVariable()); } } //
			 * 设置聊天室的新拥有者 List<String> owners = new ArrayList<String>();
			 * owners.add(xmppManager.getConnection().getUser());// 用户JID
			 * submitForm.setAnswer("muc#roomconfig_roomowners", owners); //
			 * 设置聊天室是持久聊天室，即将要被保存下来
			 * submitForm.setAnswer("muc#roomconfig_persistentroom", false); //
			 * 房间仅对成员开放 submitForm.setAnswer("muc#roomconfig_membersonly",
			 * false); // 允许占有者邀请其他人
			 * submitForm.setAnswer("muc#roomconfig_allowinvites", true); //
			 * 进入是否需要密码
			 * //submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
			 * true); // 设置进入密码
			 * //submitForm.setAnswer("muc#roomconfig_roomsecret", "password");
			 * // 能够发现占有者真实 JID 的角色 //
			 * submitForm.setAnswer("muc#roomconfig_whois", "anyone"); // 登录房间对话
			 * submitForm.setAnswer("muc#roomconfig_enablelogging", true); //
			 * 仅允许注册的昵称登录 submitForm.setAnswer("x-muc#roomconfig_reservednick",
			 * true); // 允许使用者修改昵称
			 * submitForm.setAnswer("x-muc#roomconfig_canchangenick", false); //
			 * 允许用户注册房间 submitForm.setAnswer("x-muc#roomconfig_registration",
			 * false); // 发送已完成的表单（有默认值）到服务器来配置聊天室
			 * submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
			 * true); // 发送已完成的表单（有默认值）到服务器来配置聊天室
			 */
			// muc.sendConfigurationForm(form);

			List<String> owners = new ArrayList<String>();
			owners.add(xmppManager.getConnection().getUser());// 用户JID
			// 设置聊天室的新拥有者
			submitForm.setAnswer("muc#roomconfig_roomowners", owners);
			submitForm.setAnswer("muc#roomconfig_membersonly", false);
			// 设置聊天室是持久聊天室，即将要被保存下来
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			// 登录房间对话
			//submitForm.setAnswer("muc#roomconfig_enablelogging", true);
			// 发送已完成的表单（有默认值）到服务器来配置聊天室
			muc.sendConfigurationForm(submitForm);

			return muc;
		} catch (XMPPException e) {
			e.printStackTrace();
		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		} catch (SmackException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public boolean initMUC(String groupName, MultiUserChat muc, List<Login> userList){
		try {
			muc.addMessageListener(new MultiMessageListener(xmppManager));
			muc.join(xmppManager.getUsername());
			for (int i = 0; i < userList.size(); i++) {
				SQLiteDatabase db = DBHelper.getInstance(xmppManager.getIMService()).getWritableDatabase();
				//RoomUserTable table = new RoomUserTable(db);
				//table.insert(groupName, userList.get(i).uid);
				UserTable userTable = new UserTable(db);
				Login login = userTable.query(userList.get(i).uid);
				if(login == null){
					userTable.insert(userList.get(i), userList.get(i).groupId);
				}

				try {
					muc.invite(userList.get(i).uid + "@" + xmppManager.getConnection().getServiceName(),  "");
				} catch (SmackException.NotConnectedException e) {
					e.printStackTrace();
				}
			}
			
			return true;
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		} catch (XMPPException.XMPPErrorException e) {
			e.printStackTrace();
		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		}

		return false;
		
	}
	
	public boolean inviteUser(String groupName, MultiUserChat muc, List<Login> userList){
		try {
			for (int i = 0; i < userList.size(); i++) {
				SQLiteDatabase db = DBHelper.getInstance(xmppManager.getIMService()).getWritableDatabase();
				//RoomUserTable table = new RoomUserTable(db);
				//table.insert(groupName, userList.get(i).uid);
				UserTable userTable = new UserTable(db);
				Login login = userTable.query(userList.get(i).uid);
				if(login == null){
					userTable.insert(userList.get(i), userList.get(i).groupId);
				}
				
				muc.invite(userList.get(i).uid + "@" + xmppManager.getConnection().getServiceName(),  "");
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean kickParticipant(MultiUserChat muc, String uid){
		try {
			muc.kickParticipant(uid, "");
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public boolean destoryRoom(String roomName){
		try {
			String jid = roomName + "@conference." + xmppManager.getConnection().getServiceName();
			MultiUserChatManager mcum=MultiUserChatManager.getInstanceFor(xmppManager.getConnection());
			MultiUserChat muc =mcum.getMultiUserChat(jid);

			muc.destroy("", jid);
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public boolean exitRoom(String roomName){
		try {
			String jid = roomName + "@conference." + xmppManager.getConnection().getServiceName();
			MultiUserChatManager mcum=MultiUserChatManager.getInstanceFor(xmppManager.getConnection());
			MultiUserChat muc =mcum.getMultiUserChat(jid);
			muc.leave();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 创建一个会话.
	 * @param chatID
	 * @return 没有连接状态时,返回空.
	 * 作者:fighter <br />
	 * 创建时间:2013-5-5<br />
	 * 修改时间:<br />
	 */
	public Chat createChat(String chatID){
		Chat chat = null;
//			chatCache.get(chatID);
//		if(chat == null){
			try {
				Presence presence = new Presence(Presence.Type.available);
				presence.setStatus("我是在线状态");
				IMXMPPTCPConnection connection =xmppManager.getConnection();
				connection.sendStanza(presence);
				ChatManager chatmanager = ChatManager.getInstanceFor(connection);
				String userJID=chatID + "@"+ xmppManager.getConnection().getServiceName();
				chat = chatmanager.createChat(userJID, chatListener);

			} catch (Exception e) {
				e.printStackTrace();
			}
//		}
		
//		if(chat != null){
//			chatCache.put(chatID, chat);
//		}
		
		return chat;
	}
	
	/**
	 * 通过服务端自己的接口发送消息
	 */
	public void sendMsg(ChatMessage info){
		
	}

	/**
	 * 通过 openfire 给指定的某人发送消息
	 * @param chatMessage
	 * @param group
	 * @return
	 */
	public boolean sendMessage(ChatMessage chatMessage, String group) {
		boolean flag = false;
		Chat chat = createChat(chatMessage.getToId());
		
		if(chat != null){
			try {
				JSONObject json = (JSONObject) JSON.toJSON(chatMessage);
				json.remove("id");
				json.remove("sendState");
				json.remove("readState");
				json.remove("sessionId");
				json.remove("pullTime");
				if(json.containsKey("isOutlander")){
					json.remove("isOutlander");
				}
				if(MessageType.LOCATION == chatMessage.messageType
						|| MessageType.IMAGE == chatMessage.messageType/*
						||MessageType.APPNEWS == chatMessage.type*/){
					json.put("content", JSONObject.parseObject(chatMessage.getContent()));
				}
				
				/*if(IMCommon.getLoginResult(ChatApplication.getInstance())!=null){
					if(IMCommon.getLoginResult(ChatApplication.getInstance()).type >=2 ){
						json.put("isUser", 1);
					}
				}*/
				chat.sendMessage(json.toJSONString());

				flag = true;
			} catch (IllegalStateException e) {
				// 没连接上服务器
				e.printStackTrace();
				flag = false;
				xmppManager.startReconnectionThread();
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
				flag = false;
			}
		}
		chatMessage.setSendState(flag? 1 : 0);
		return flag;
	}

	/**
	 * 通过 openfire 给指定的某人发送消息
	 * @param chatMessage
	 * @return
	 */
	public boolean sendMessage(ChatMessage chatMessage) {
		boolean flag = false;
		Chat chat = createChat(chatMessage.getToId());

		if(chat != null){
			try {
				JSONObject json = (JSONObject) JSON.toJSON(chatMessage);
				json.remove("id");
				json.remove("sendState");
				json.remove("readState");
				json.remove("sessionId");
				json.remove("pullTime");
				if(json.containsKey("isOutlander")){
					json.remove("isOutlander");
				}
				if(MessageType.LOCATION == chatMessage.messageType
						|| MessageType.IMAGE == chatMessage.messageType/*
						||MessageType.APPNEWS == chatMessage.type*/){
					json.put("content", JSONObject.parseObject(chatMessage.getContent()));
				}

				/*if(IMCommon.getLoginResult(ChatApplication.getInstance())!=null){
					if(IMCommon.getLoginResult(ChatApplication.getInstance()).type >=2 ){
						json.put("isUser", 1);
					}
				}*/
				chat.sendMessage(json.toJSONString());

				flag = true;
			} catch (IllegalStateException e) {
				// 没连接上服务器
				e.printStackTrace();
				flag = false;
				xmppManager.startReconnectionThread();
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
				flag = false;
			}
		}
		chatMessage.setSendState(flag? 1 : 0);
		return flag;
	}

	/**
	 * 发送聊天信息
	 * @param pushMessage
	 * @param msg
	 * @param group
	 */
	public void pushMessage(PushMessage pushMessage, ChatMessage msg, String group){
		pushMessage.pushMessage(msg, group);
	}

	/**
	 * 接收到消息,通过广播发送发送.
	 * @param notifyMessage
	 * @param content
	 */
	public void notityMessage(NotifyMessage notifyMessage, String content){
		notifyMessage.notifyMessage(content);
	}
	
	public NotifySystemMessage getSystemMessage() {
		return systemMessage;
	}

	public PushChatMessage getPushChatMessage() {
		return pushChatMessage;
	}
	
	public NotifyChatMessage getNotifyChatMessage() {
		return chatMessage;
	}

	/**
	 * 功能：聊天对象的单对单对话监听
	 */
	class ChatListenerImpl implements ChatMessageListener{

		@Override
		public void processMessage(Chat chat, Message message) {
			// jid 为  chatId@domin/chat组成
			String chatId = chat.getParticipant().split("@")[0];  // 发来消息的用户
			String content = message.getBody();					// 发送来的内容.
			if(SYSTEM_USER.equals(chatId)){//系统消息
				notityMessage(systemMessage, content);
			}else{

				if(!TextUtils.isEmpty(content) && content.startsWith("{")){
					Log.d("ChatListenerImpl", content);
				}
				notityMessage(chatMessage, content);
			}
		}
	}

}
