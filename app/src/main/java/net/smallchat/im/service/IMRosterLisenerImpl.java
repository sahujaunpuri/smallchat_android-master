package net.smallchat.im.service;

import java.util.Collection;

import android.content.Intent;
import android.text.TextUtils;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;


/**
 * 
 * 功能： 好友状态监听. <br />
 * 地点：无穷大软件<br />
 * 版本：ver 1.0<br />
 * 
 * guoxin
 * @since
 */
public class IMRosterLisenerImpl implements RosterListener {
	/**
	 * 好友上线 <br/> {@link IMRosterLisenerImpl#EXTRAS_ROSTER}
	 */
	public static final String ACTION_AVAILABLE = "net.smallchat.im.sns.roster.ACTION_AVAILABLE";
	/**
	 * 好友下线 <br/> {@link IMRosterLisenerImpl#EXTRAS_ROSTER}
	 */
	public static final String ACTION_UNAVAILABLE = "net.smallchat.im.sns.roster.ACTION_UNAVAILABLE";
	/**
	 * 被好友删除 <br/> {@link IMRosterLisenerImpl#EXTRAS_ROSTER}
	 */
	public static final String ACTION_DELETEED = "net.smallchat.im.sns.roster.ACTION_DELETEED";
	/**
	 * 添加好友 <br/> {@link IMRosterLisenerImpl#EXTRAS_ROSTER}
	 */
	public static final String ACTION_ADDED = "net.smallchat.im.sns.roster.ACTION_ADDED";
	/**
	 * 好友动态更新 <br/> {@link IMRosterLisenerImpl#EXTRAS_ROSTER}
	 */
	public static final String ACTION_UPDATE = "net.smallchat.im.sns.roster.ACTION_UPDATE";
	/**
	 * 附加信息: 用户 ChatID
	 */
	public static final String EXTRAS_ROSTER = "EXTRAS_ROSTER";
	
	
	private XMPPManager xmppManager;
	
	public IMRosterLisenerImpl(XMPPManager xmppManager) {
		super();
		this.xmppManager = xmppManager;
	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		for (String jid : addresses) {
			if(!TextUtils.isEmpty(jid)){
				sendPresence(Presence.Type.subscribed, jid);
				sendPresence(Presence.Type.subscribe, jid);
				Intent intent = new Intent(ACTION_ADDED);
				intent.putExtra(EXTRAS_ROSTER, jid2ChatId(jid));
				sendBroadcast(intent);
			}
		}
	}
	
	private void sendPresence(Presence.Type type, String to){
		Presence presence = new Presence(type);
		presence.setTo(to);
		try {
			xmppManager.getConnection().sendPacket(presence);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendBroadcast(Intent intent){
		try {
			xmppManager.getIMService().sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		// TODO Auto-generated method stub
		for (String jid : addresses) {
			if(!TextUtils.isEmpty(jid)){
				sendPresence(Presence.Type.unsubscribed, jid);
				sendPresence(Presence.Type.unsubscribe, jid);
				
				Intent intent = new Intent(ACTION_DELETEED);
				intent.putExtra(EXTRAS_ROSTER, jid2ChatId(jid));
				sendBroadcast(intent);
			}
		}
	}


	@Override
	public void presenceChanged(Presence presence) {
		String chatId = jid2ChatId(presence.getFrom());
		Intent intent = new Intent();
		switch (presence.getType()) {
		case available:
			intent.setAction(ACTION_AVAILABLE);
			intent.putExtra(EXTRAS_ROSTER, chatId);
			break;
		case unavailable:
			intent.setAction(ACTION_UNAVAILABLE);
			intent.putExtra(EXTRAS_ROSTER, chatId);
			break;

		default:
			break;
		}
		
		sendBroadcast(intent);
//		System.out.println("form:" + presence.getFrom());
//		System.out.println("to:" + presence.getTo());
//		System.out.println("Type:" + presence.getType().name());
//		System.out.println("Status:" + presence.getStatus());
	}

	
	private String jid2ChatId(String jid){
		return jid.split("@")[0];
	}
	
}
