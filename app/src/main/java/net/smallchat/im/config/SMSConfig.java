package net.smallchat.im.config;

import net.smallchat.im.R;
import net.smallchat.im.global.ChatApplication;

public class SMSConfig {
    //邀请好友下载APP短信内容，可以配置到string文件中，实现中文英文双语言设置
    public static final String INVITE_MESSAGE_SMS = ChatApplication.getInstance().getResources().getString(R.string.invite_message_sms);
    public static final String INVITE_MESSAGE = ChatApplication.getInstance().getResources().getString(R.string.invite_message);
}
