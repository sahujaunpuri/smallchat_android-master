package net.smallchat.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import net.smallchat.im.service.IMService;

public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String LOGTAG = "XMPP";

    private IMService imService;

    public ConnectivityReceiver(IMService imService) {
        this.imService = imService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "ConnectivityReceiver.onReceive()...");
        String action = intent.getAction();
        Log.d(LOGTAG, "action=" + action);

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            Log.d(LOGTAG, "Network Type  = " + networkInfo.getTypeName());
            Log.d(LOGTAG, "Network State = " + networkInfo.getState());
            if (networkInfo.isConnected()) {
                Log.i(LOGTAG, "Network connected");
                // 重新连接服务器
                if(imService != null)
                imService.getXmppManager().startReconnectionThread();
            }
        } else {
            Log.e(LOGTAG, "Network unavailable");
            // 取消连接
            if(imService != null)
            imService.getXmppManager().disconnect();
        }
    }

}
