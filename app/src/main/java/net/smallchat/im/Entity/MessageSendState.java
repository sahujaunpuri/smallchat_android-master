package net.smallchat.im.Entity;


public class MessageSendState{
	private static final long serialVersionUID = -4274108350647182194L;
	//消息发送成功与否的状态  0 失败 1 成功, 2 正在发送， 4， 正在下载，5 正在撤回 6 撤回失败 7 撤回成功
	public static final int SEND_STATE_FAIL=0;//发送失败
	public static final int SEND_STATE_SUCCESS =1;//发送成功
	public static final int SEND_STATE_SENDING=2;//正在发送
	public static final int SEND_STATE_REVOKING=5;//正在撤回
	public static final int SEND_STATE_REVOKE_FAIL=6;//撤回失败

}
