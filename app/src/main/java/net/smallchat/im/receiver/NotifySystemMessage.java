package net.smallchat.im.receiver;

import android.util.Log;

import net.smallchat.im.Entity.NotifiyMessage;
import net.smallchat.im.service.XMPPManager;

public class NotifySystemMessage implements NotifyMessage{
	private static final String TAG = "XMPP";
	/**
	 * 聊天服务发送系统消息广播包
	 */
	public static final String ACTION_NOTIFY_SYSTEM_MESSAGE = "net.smallchat.im.sns.notify.ACTION_NOTIFY_SYSTEM_MESSAGE";
	
	/**
	 * 附加标识
	 */
	public static final String EXTRAS_NOTIFY_SYSTEM_TAG = "extra_tag";
	
	/**
	 * 附加信息
	 */
	public static final String EXTRAS_NOTIFY_SYSTEM_MESSAGE = "extras_message";

	/**
	 * VIP 状态发生变化
	 */
	public static final String ACTION_VIP_STATE = "net.smallchat.im.sns.notify.ACTION_VIP_STATE";
	public static final String EXTRAS_VIP = "extra_vip";
	
	private XMPPManager xmppManager;
	private SystemNotifiy systemNotifiy;
	
	
	public NotifySystemMessage(XMPPManager xmppManager) {
		super();
		this.xmppManager = xmppManager;
		this.systemNotifiy = new SystemNotifiy(xmppManager.getIMService());
	}


	@Override
	public void notifyMessage(String msg) {
		Log.e(TAG, "notitySystemMessage()："+msg);
		try {
			NotifiyMessage notifiyMessage = new NotifiyMessage(msg);
			this.systemNotifiy.notifiy(notifiyMessage);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "notityMessage()", e);
		}
	}

}
