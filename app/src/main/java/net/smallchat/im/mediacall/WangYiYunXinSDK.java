package net.smallchat.im.mediacall;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import net.smallchat.im.Entity.Login;
import net.smallchat.im.global.IMCommon;

public class WangYiYunXinSDK implements MediaCallAction {
    public static final String TAG="WangYiYunXinSDK";
    public static String getLastwords(String srcText, String p) {
        try {
            String[] array = TextUtils.split(srcText, p);
            int index = (array.length - 1 < 0) ? 0 : array.length - 1;
            return array[index];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void init(Context context) {

        // 已经初始化成功，后续开发业务代码。
        Log.i(TAG, "初始化SDK及登陆代码完成");
    }

    /**
     * 根据呼叫类型通话
     * @param ctx 上下文
     * @param callType 呼叫类型
     * @param nickname 昵称
     * @param contactId 号码
     */
    @Override
    public  void callVideoAction(Context ctx , MediaCallCommon.CallType callType ,String nickname, String contactId) {
//        // VoIP呼叫
//        Intent callAction = new Intent(ctx , VideoActivity.class);
//        if(callType == MediaCallCommon.CallType.VIDEO) {
//            callAction = new Intent(ctx , VideoActivity.class);
//
//        }
//        callAction.putExtra(VideoActivity.EXTRA_CALL_NAME , nickname);
//        callAction.putExtra(VideoActivity.EXTRA_CALL_NUMBER , contactId);
//        //callAction.putExtra(ECDevice.CALLTYPE , callType);
//        callAction.putExtra(VideoActivity.EXTRA_OUTGOING_CALL , true);
//        ctx.startActivity(callAction);
    }


    /**
     * 根据呼叫类型通话
     * @param ctx 上下文
     * @param callType 呼叫类型
     * @param nickname 昵称
     * @param contactId 号码
     */
    @Override
    public void callVoipAction(Context ctx , MediaCallCommon.CallType callType ,String nickname, String contactId) {
//        // VoIP呼叫
//        Intent callAction = new Intent(ctx , VoIPCallActivity.class);
//        callAction.putExtra(VideoActivity.EXTRA_OUTGOING_CALL , true);
//        VoIPCallHelper.mHandlerVideoCall = false;
//        callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NAME , nickname);
//        callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NUMBER , contactId);
//        //callAction.putExtra(ECDevice.CALLTYPE , callType);
//        callAction.putExtra(VoIPCallActivity.EXTRA_OUTGOING_CALL , true);
//
//        ctx.startActivity(callAction);
    }
    public static boolean isSelf(Context context, String id){
        if(TextUtils.isEmpty(id)) {
            return false;
        }

        Login login = IMCommon.getLoginResult(context);
        return id.equals(login.uid);
    }

    @Override
    public void setMute() {

    }

    @Override
    public boolean getMute() {
        return false;
    }

    @Override
    public void setHandFree() {

    }

    @Override
    public boolean getHandFree() {
        return false;
    }

    @Override
    public void releaseMuteAndHandFree() {

    }

    @Override
    public void releaseCall(String callId) {

    }

    @Override
    public void acceptCall(String callId) {

    }

    @Override
    public void rejectCall(String callId) {

    }

    @Override
    public boolean isHoldingCall() {
        return false;
    }

}
