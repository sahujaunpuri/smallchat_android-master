package net.smallchat.im.Entity;



public final class ChatType {

    /**
     * 私聊会话
     */
    public static final int PrivateMessage = 100;

    /**
     * 群聊会话
     */
    public static final int GroupMessage = 300;


    /**
     * 会议会话
     */
    public static final int MeetingMessage = 500;

    /**
     * 系统通知
     */
    public static final int SystemMessage = 1;


    /**
     * 服务通知
     */
    public static final int ServiceMessage = 2;

    /**
     * 支付通知
     */
    public static final int PayMessage = 3;

    /**
     * 文件传输
     */
    public static final int FileTransferMessage = 4;


    /**
     * 内容推送（微商发现，新闻资讯推送）
     */
    public static final int ContentPushMessage = 5;



}