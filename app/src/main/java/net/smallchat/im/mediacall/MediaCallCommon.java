package net.smallchat.im.mediacall;

import android.content.Context;

import net.smallchat.im.Entity.Login;

/**
 * Created by it on 2017/8/19.
 */

public class MediaCallCommon {


    public static final String CALLTYPE = "call_type";
    public static final String CALLID = "callId";
    public static final String CALLER = "caller";
    public static final String REMOTE = "remote";
    public static final String MEETING_NO = "meeting_no";
    public static final int SYNC_OFFLINE_MSG_ALL = -1;
    public static boolean isOpenOfflineSwitch = false;

    public static enum CallType {
        VOICE,
        VIDEO,
        DIRECT;
        private CallType() {
        }
    }
    private static MediaCallAction mediaCallAction=null;
    public static void init(Context context){
        mediaCallAction=new WangYiYunXinSDK();
        mediaCallAction.init(context);
    }

    /**
     * 根据呼叫类型通话
     * @param ctx 上下文
     * @param callType 呼叫类型
     * @param nickname 昵称
     * @param contactId 号码
     */
    public static void callVideoAction(Context ctx , MediaCallCommon.CallType callType , String nickname, String contactId){
       if(mediaCallAction==null){
           MediaCallCommon.init(ctx);

       }
        mediaCallAction.callVideoAction(ctx,callType,nickname,contactId);
    }


    /**
     * 根据呼叫类型通话
     * @param ctx 上下文
     * @param callType 呼叫类型
     * @param nickname 昵称
     * @param contactId 号码
     */
    public static void callVoipAction(Context ctx , MediaCallCommon.CallType callType ,String nickname, String contactId){
        if(mediaCallAction==null) {
            MediaCallCommon.init(ctx);
        }
        mediaCallAction.callVoipAction(ctx,callType,nickname,contactId);
    }

    /***
     * 视频会议
     * @param ctx
     * @param longin
     */
    public static void makeVideoMeeting(Context ctx, Login longin){

    }

    /**
     * 语音会议
     * @param ctx
     * @param longin
     */
    public static void makeVoiceMeeting(Context ctx, Login longin){

    }

    /**
     * 静音设置
     */
    public static void setMute()
    {
        mediaCallAction.setMute();
    }

    public static boolean getMute(){
        return mediaCallAction.getMute();
    }

    /**
     * 设置免提状态
     */
    public  static void setHandFree()
    {
        mediaCallAction.setHandFree();
    }
    /**
     *返回免提状态
     */
    public static boolean getHandFree()
    {
        return false;

    }
    public static void releaseMuteAndHandFree(){
        mediaCallAction.releaseMuteAndHandFree();
    }

    /**
     * 释放通话
     *
     * @param callId
     *            通话唯一标识
     */
    public static void releaseCall(String callId) {
        mediaCallAction.releaseCall(callId);
    }

    /**
     * 接听来电
     * @param callId 通话唯一标识
     */
    public static void acceptCall(String callId) {
        mediaCallAction.acceptCall(callId);
    }

    /**
     * 拒接来电
     * @param callId 通话唯一标识
     */
    public static void rejectCall(String callId) {
        mediaCallAction.releaseCall(callId);
    }


    /**
     * 当前是否正在进行VoIP通话
     * @return 是否通话
     */
    public static boolean isHoldingCall() {
       return mediaCallAction.isHoldingCall();
    }
}
