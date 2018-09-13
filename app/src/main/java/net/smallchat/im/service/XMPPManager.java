package net.smallchat.im.service;

import android.util.Log;


import net.smallchat.im.service.type.XmppType;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.roster.Roster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


/**
 *
 * 功能： 连接IM服务器.. <br />
 * 地点：无穷大软件<br />
 * 版本：ver 1.0<br />
 *
 * guoxin
 * @since
 */
public class XMPPManager {
	public static final String LOGTAG = "XMPP";
	/** 连接状态 */
	public static final int XMPP_CONNECT_STATE = 0x1;
	/** 登录状态 */
	public static final int XMPP_LOGINING_STATE = 0x2;
	/** 登录成功状态 */
	public static final int XMPP_LOGINED_STATE = 0x3;
	/** 认证失败 */
	public static final int XMPP_AUTH_ERR = 0xc;

	private String username;
	private String password;

	private boolean running = false;
	private Future<?> futureTask;
	private IMXMPPTCPConnection connection;
	private List<Runnable> taskList;

	private IMMessageManager imMessageLisener;
	private IMRosterLisenerImpl snsRosterLisenerImpl;

	private IMService.TaskSubmitter taskSubmitter;
	private IMService.TaskTracker taskTracker;
	private ConnectionListener connectionListener;

	private IMService imService;

	private ApiServiceThread phpServiceThread;

	private int connectState = 0x0;

	private XMPPManager(IMService imService){
		this.imService = imService;
		taskSubmitter = imService.getTaskSubmitter();
		taskTracker = imService.getTaskTracker();
		taskList = new ArrayList<Runnable>();
		System.setProperty("smack.debugEnabled", "true");
	}

	public XMPPManager(IMService imService, String username, String password) {
		this(imService);
		this.username = username;
		this.password = password;

		connectionListener = new PersistentConnectionListener(this);
		imMessageLisener = new IMMessageManager(XMPPManager.this);
	}

	public synchronized void connect() {
		Log.d(LOGTAG, "connect()...");
		// 提交登录任务.
		submitLoginTask();
	}

	public void disconnect() {
		Log.d(LOGTAG, "disconnect()...");
		if(phpServiceThread != null){
			phpServiceThread.runState = false;
			phpServiceThread = null;
		}
		// 取消登录
		getIMService().saveXmppType(XmppType.XMPP_STATE_REAUTH);
		terminatePersistentConnection();
	}

	public Future<?> getFutureTask() {
		return futureTask;
	}

	public List<Runnable> getTaskList() {
		return taskList;
	}

	public boolean isConnected() {
		return connection != null && connection.isConnected();
	}

	public IMMessageManager getChatMessageLisener() {
		return imMessageLisener;
	}

	public boolean isAuthenticated() {
		return connection != null && connection.isConnected()
				&& connection.isAuthenticated();
	}

	public IMXMPPTCPConnection getConnection() {
		if(connection==null){
			connection= IMXMPPTCPConnection.getInstance();
		}
		return connection;
	}

	public void setConnection(IMXMPPTCPConnection connection) {

		this.connection = connection;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * 开始重新连接
	 *
	 * 作者:fighter <br />
	 * 创建时间:2013-4-16<br />
	 * 修改时间:<br />
	 */
	public void startReconnectionThread() {
		Log.d(LOGTAG, "重新连接");
		taskList.clear();
		taskTracker.count = taskList.size();
		addTask(new Runnable() {


			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					// TODO: handle exception
				}
				//				terminatePersistentConnection(); // 断开连接

				Log.d(LOGTAG, "开始连接");
				connect() ;
				runTask();
			}
		});

		runTask();
	}

	/**
	 * 终止连接
	 *
	 * 作者:fighter <br />
	 * 创建时间:2013-4-16<br />
	 * 修改时间:<br />
	 */
	public void terminatePersistentConnection() {
		Log.d(LOGTAG, "terminatePersistentConnection()...");
		Runnable runnable = new Runnable() {

			final XMPPManager xmppManager = XMPPManager.this;

			@Override
			public void run() {
				if (xmppManager.isConnected()) {
					Log.d(LOGTAG, "terminatePersistentConnection()... run()");
					xmppManager.disconnect();
				}
				xmppManager.runTask();
			}

		};
		addTask(runnable);
	}

	public void runTask() {
		Log.d(LOGTAG, "runTask()...");
		synchronized (taskList) {
			running = false;
			futureTask = null;
			if (!taskList.isEmpty()) {
				Runnable runnable = taskList.get(0);
				taskList.remove(0);
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				if (futureTask == null) {
					taskTracker.decrease();
				}
			}
		}
		taskTracker.decrease();
		Log.d(LOGTAG, "runTask()...done");
	}

	// 开始连接.
	private void submitConnectTask() {
		Log.d(LOGTAG, "submitConnectTask()...");
		addTask(new ConnectTask());
	}

	private void submitLoginTask() {
		Log.d(LOGTAG, "submitLoginTask()...");
		// 连接
		submitConnectTask();
		addTask(new LoginTask());
	}

	private void addTask(Runnable runnable) {
		Log.d(LOGTAG, "addTask(runnable)...");
		taskTracker.increase();
		synchronized (taskList) {
			if (taskList.isEmpty() && !running) {
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				if (futureTask == null) {
					taskTracker.decrease();
				}
			} else {
				taskList.add(runnable);
			}
		}
		Log.d(LOGTAG, "addTask(runnable)... done");
	}

	/**
	 * A runnable task to connect the server.
	 */
	private class ConnectTask implements Runnable {

		final XMPPManager xmppManager;

		private ConnectTask() {
			this.xmppManager = XMPPManager.this;
		}

		@Override
		public void run() {
			Log.i(LOGTAG, "ConnectTask.run()...");
			connectState = XMPP_CONNECT_STATE;
			if (!xmppManager.isAuthenticated()) {
				connection = IMXMPPTCPConnection.getInstance();
				try {
					// Connect to the server
					if(connection.isConnected()&&connection.isAuthenticated()){
						return;
					}else if(connection.isConnected()&&!connection.isAuthenticated()){

						Log.i(LOGTAG, "XMPP connected successfully");
					}else {
						try {
							connection.connect();
						} catch (IOException e) {
							e.printStackTrace();
							Log.e(LOGTAG, "XMPP connection failed", e);
						} catch (XMPPException e) {
							e.printStackTrace();
							Log.e(LOGTAG, "XMPP connection failed", e);
						}
					}
					// 连接成功,开始登录
					xmppManager.runTask();
				} catch (SmackException e) {
					Log.e(LOGTAG, "XMPP connection failed", e);
					//					terminatePersistentConnection();
					startReconnectionThread();
				}

			} else {
				Log.i(LOGTAG, "XMPP connected already");
				xmppManager.runTask();
			}
		}
	}

	/**
	 * A runnable task to log into the server.
	 */
	private class LoginTask implements Runnable {
		final XMPPManager xmppManager;

		private LoginTask() {
			this.xmppManager = XMPPManager.this;
		}

		@Override
		public void run() {
			Log.i(LOGTAG, "LoginTask.run()...");
			connectState = XMPP_LOGINING_STATE;
			// 1. 是否登录的
			if (!xmppManager.isAuthenticated()) {
				try {
					// 2.登录
					xmppManager.getConnection().login(
							xmppManager.getUsername(),
							xmppManager.getPassword(), "android");
					Log.d(LOGTAG, "Loggedn in successfully");
					Log.i("XMPP", "Login successfully");
					loginSuccess();
					xmppManager.runTask();

				} catch (XMPPException e) {
					Log.e(LOGTAG, "LoginTask.run()... xmpp error");
					Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
							+ e.getMessage());
					String INVALID_CREDENTIALS_ERROR_CODE = "401";
					String errorMessage = e.getMessage();
					if (errorMessage != null
							&& errorMessage
							.contains(INVALID_CREDENTIALS_ERROR_CODE)) {
						// TODO 还没有注册或者用户名密码错误!
						connectState = XMPP_AUTH_ERR;
						// 认证错误.
						getIMService().saveXmppType(XmppType.XMPP_STATE_AUTHERR);
					}
					xmppManager.startReconnectionThread();

				} catch (Exception e) {
					Log.e(LOGTAG, "LoginTask.run()... other error");
					Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
							+ e.getMessage());
					xmppManager.startReconnectionThread();
				}

			} else {
				Log.i(LOGTAG, "Logged in already");
				xmppManager.runTask();
				connectState = XMPP_LOGINED_STATE;

				getIMService().saveXmppType(XmppType.XMPP_STATE_AUTHENTICATION);
			}
		}
	}


	public IMService getIMService() {
		return imService;
	}

	private void loginSuccess() {
		connectState = XMPP_LOGINED_STATE;
		// 认证成功.
		getIMService().saveXmppType(XmppType.XMPP_STATE_AUTHENTICATION);
		// connection listener
		if (connectionListener != null) {
			getConnection().addConnectionListener(connectionListener);
		}
		ChatManager chat=ChatManager.getInstanceFor(getConnection());
		chat.addChatListener(imMessageLisener);

		if(snsRosterLisenerImpl == null){
			snsRosterLisenerImpl = new IMRosterLisenerImpl(XMPPManager.this);
		}
		Roster roster=Roster.getInstanceFor(getConnection());
		roster.addRosterListener(snsRosterLisenerImpl);

		if(phpServiceThread == null){
			phpServiceThread = new ApiServiceThread(getIMService().getXmppTypeManager(), getIMService().getUserInfoVo());
			new Thread(new ApiServiceThread(getIMService().getXmppTypeManager(), getIMService().getUserInfoVo()))
					.start();
		}
	}

	public int getConnectState() {
		return connectState;
	}

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}
}