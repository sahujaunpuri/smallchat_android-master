package net.smallchat.im.mediacall;

import android.content.Context;

/**
 * 多媒体通话接口封装，用于集成多种语音视频通话接口
 * Created by it on 2017/8/19.
 */

public interface MediaCallAction {

    public  abstract  void init(Context ctx);
    /**
     * 根据呼叫类型通话
     *
     * @param ctx       上下文
     * @param callType  呼叫类型
     * @param nickname  昵称
     * @param contactId 号码
     */
    public abstract void callVideoAction(Context ctx, MediaCallCommon.CallType callType, String nickname, String contactId);


    /**
     * 根据呼叫类型通话
     *
     * @param ctx       上下文
     * @param callType  呼叫类型
     * @param nickname  昵称
     * @param contactId 号码
     */
    public abstract void callVoipAction(Context ctx, MediaCallCommon.CallType callType, String nickname, String contactId);


    /**
     * 静音设置，自动判断是静音还是取消静音。
     */
    public abstract void setMute();

    /**
     * 获取静音状态
     * @return
     */
    public abstract boolean getMute();


    /**
     * 设置免提状态
     */
    public abstract void setHandFree();
    /**
     *返回免提状态
     */
    public abstract  boolean getHandFree();
    public abstract void releaseMuteAndHandFree();

    /**
     * 释放通话
     *
     * @param callId
     *            通话唯一标识
     */
    public abstract void releaseCall(String callId);

    /**
     * 接听来电
     * @param callId 通话唯一标识
     */
    public abstract void acceptCall(String callId);

    /**
     * 拒接来电
     * @param callId 通话唯一标识
     */
    public abstract void rejectCall(String callId);


    /**
     * 当前是否正在进行VoIP通话
     * @return 是否通话
     */
    public abstract boolean isHoldingCall();
}