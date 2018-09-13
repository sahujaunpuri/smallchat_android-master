package net.smallchat.im.service;

import android.util.Log;

import net.smallchat.im.Entity.Login;
import net.smallchat.im.service.type.XmppTypeManager;

/**
 * 定时刷新后台，保持在线
 */
public class ApiServiceThread implements Runnable{
	private static final String TAG = "XMPP";
	private XmppTypeManager manager;
	private Login userInfoVo;
	public static final long TIME = 300000;
	
	public boolean runState = true;
	
	public ApiServiceThread(XmppTypeManager manager, Login userInfoVo) {
		super();
		this.manager = manager;
		this.userInfoVo = userInfoVo;
	}

	@Override
	public void run() {
		while(runState){
			connect();
			try {
				Thread.sleep(TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void connect(){

		try {
			//目前该功能已经废弃
			//Log.d(TAG, "connect()");
			//String result = userInfoApi.online(userInfoVo.getUid());
			//Log.d(TAG, "connect:online:" + result);
		} catch (Exception e) {
			Log.e(TAG, "connect()", e);
		}
	}

}
