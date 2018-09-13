package net.smallchat.im.receiver;

import android.util.Log;

import net.smallchat.im.service.XMPPManager;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

public class MultiMessageListener implements MessageListener {
	
	public static final String LOGTAG = "XMPP";
	private XMPPManager xmppManager;
	
	public MultiMessageListener(XMPPManager xmppManager) {
		super();
		this.xmppManager = xmppManager;
	}
	@Override
	public void processMessage(Message message) {
		Log.e(LOGTAG, message.getBody());
		xmppManager.getChatMessageLisener().notityMessage(xmppManager.getChatMessageLisener().getNotifyChatMessage(), message.getBody());

	}
}
