package net.smallchat.im.global;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

import net.smallchat.im.Entity.CountryList;
import net.smallchat.im.MainActivity;
import net.smallchat.im.api.IMException;

import java.util.List;

/**
 * 1、为了打开客户端的日志，便于在开发过程中调试，需要自定义一个 Application。
 * 并将自定义的 application 注册在 AndroidManifest.xml 文件中。<br/>
 * 2、为了提高 push 的注册率，您可以在 Application 的 onCreate 中初始化 push。你也可以根据需要，在其他地方初始化 push。
 *
 * @author wangkuiwei
 */
public class ChatApplication extends Application {

    private static CountryList mCountryList = null;

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep TAG
    public static final String TAG = "ChatApplication";

    private static ChatMiPushHandler sHandler = null;
    private static MainActivity sMainActivity = null;
    public static ChatApplication instance;

    public static ChatApplication getInstance()
    {
        if(instance!=null){
            return instance;
        }else {
            return  null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息

        if (sHandler == null) {
            sHandler = new ChatMiPushHandler(getApplicationContext());
        }

        IMCommon.verifyNetwork(instance);

        SDKInitializer.initialize(getApplicationContext());

        if (IMCommon.getUserId(instance) != null && !IMCommon.getUserId(instance).equals("")) {
            new Thread() {
                public void run() {
                    try {
                        mCountryList = IMCommon.getIMServerAPI()
                                .getCityAndContryUser();
                    } catch (IMException e) {
                        e.printStackTrace();
                    }
                }

                ;
            }.start();
        }



    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public static CountryList getContryList() {
        return mCountryList;
    }

    public static void setContryList(CountryList contryList) {
        mCountryList = contryList;
    }
    public static ChatMiPushHandler getHandler() {
        return sHandler;
    }

    public static void setMainActivity(MainActivity activity) {
        sMainActivity = activity;
    }

    public static class ChatMiPushHandler extends Handler {

        private Context context;

        public ChatMiPushHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
            if (sMainActivity != null) {
                //sMainActivity.refreshLogInfo();
                //收到推送消息,进行处理

            }
            if (!TextUtils.isEmpty(s)) {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        }
    }
}