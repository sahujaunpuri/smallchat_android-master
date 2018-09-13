package net.smallchat.im.receiver;

import net.smallchat.im.Entity.ChatMessage;

/**
 * 
 * 功能：推送信息,好友之间的信息推送. <br />
 * 日期：2013-4-27<br />
 * 地点：无穷大软件<br />
 * 版本：ver 1.0<br />
 * 
 * guoxin
 * @since
 */
public interface PushMessage {
	void pushMessage(ChatMessage msg, String group);
	void pushMessage(ChatMessage msg);
}
