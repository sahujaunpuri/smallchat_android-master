package net.smallchat.im.service;

import android.util.Log;

import net.smallchat.im.global.FeatureFunction;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;


public class XMPPInvitationListener implements InvitationListener {
	
	public static final String LOGTAG = "XMPPInvitationListener";
	private XMPPManager xmppManager;
	
	public XMPPInvitationListener(XMPPManager xmppManager) {
		super();
		this.xmppManager = xmppManager;
	}
//
//	@Override
//	public void invitationReceived(Connection conn, String room,
//								   String inviter, String reason, String password, Message message) {
//		Log.d("XMPPInvitationListener", "邀请用户回调");
//		String roomName = room.split("@")[0];
//		long time = System.currentTimeMillis();
//		xmppManager.getChatMessageLisener().joinRoom(roomName, FeatureFunction.getTimeDate(time));
//	}

	@Override
	public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {
		Log.d("XMPPInvitationListener", "邀请用户回调");
		String roomName = room.getRoom();//.split("@")[0]
		long time = System.currentTimeMillis();
		xmppManager.getChatMessageLisener().joinRoom(roomName, FeatureFunction.getTimeDate(time));
	}
}
