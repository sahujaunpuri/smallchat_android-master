package net.smallchat.im.global;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.NewFriendItem;
import net.smallchat.im.Entity.NotifiyMessage;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.api.IMServerAPI;
import net.smallchat.im.service.IMService;


public class IMCommon {
	private static Boolean mIsNetWorkAvailable = false;
	public static final String LANGUAGE_SHARED = "language_shared";
	public static final String LANGUAGE_CHOOSED = "language_choosed";
	public static final String RUSSIAN = "ru";
	public static final String CHINESE = "zh";
	public static final String ENGLISH = "en";
	private static IMServerAPI mIMServerAPI = new IMServerAPI();
	private static String mSystemServerPrefix = "";
	private static String mSystemName = "";
	private static String mUid = "";

	public static final String SERVER_SHARED = "server_shared";
	public static final String SERVER_PREFIX = "server_prefix";

	public static final String LOGIN_SHARED = "login_shared";
	public static final String LOGIN_RESULT = "login";

	public static final String NEW_TIP_SHARED = "new_tip_shared";
	public static final String FRIENDS_LOOP_TIP = "friends_loop_tip";
	public static final String READ_FRIENDS_LOOP_TIP = "read_friends_loop_tip";
	public static final String READ_MEETING_TIP = "read_meeting_tip";
	public static final String CONTACT_TIP = "contact_tip";
	public static final String LOGIN_ID = "login_id";

	public static final String MOVING_SHARED = "moving_shared";
	public static final String MOVING_RESULT = "moving";


	public static final int LOAD_SIZE = 20;
	public static boolean mChatInit = false;
	public static final String SHOWGUDIEVERSION = "version_shared";

	public static final String REMENBER_SHARED = "remenber_shared";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String ISREMENBER = "isRemenber";


	public static final String NOTIFICATION_TIME_SHARED = "notification_time_shared";
	public static final String NOTIFICATION_TIME = "notification_time";

	public static final long NOTIFICATION_INTERVAL = 5000;				//通知震动时间间隔	

	public static final String MESSAGE_NOTIFY_SHARED = "message_notify_shared";
	public static final String MESSAGE_NOTIFY = "message_notify";
	public static final String SOUND = "sound";

	public static final String CAMER_URL_SHARED = "camer_url_shared";
	public static final String CAMER_URL = "camer_url";
	public static final String CAMER_ARRAY_URL = "camer_array_url";
	
	
	private static  double mCurrentLat=0;//30.739198684692383;// -1;
	private static  double mCurrentLng=0;//103.97882080078125;// -1;

	public static final String LOCATION_SHARED = "location_shared";
	public static final String LAT = "lat";
	public static final String LNG = "lng";

	public static int mScreenWidth;
	public static int mScreenHeight;

	public static final int MESSAGE_CONTENT_LEN = 5000;

	public static final int PAY_SUCCESS = 11356;

	public static boolean isApplicationActive = false;

	public static void setNetWorkState(boolean state){
		mIsNetWorkAvailable = state;
	}

	public static boolean getNetWorkState(){
		return mIsNetWorkAvailable;
	}

	public static boolean verifyNetwork(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null) {
			if (activeNetInfo.isConnected()) {
				setNetWorkState(true);
				return true;
			} else {
				setNetWorkState(false);
				return false;
			}
		} else {
			setNetWorkState(false);
			return false;
		}
	}

	public static IMServerAPI getIMServerAPI(){
		return mIMServerAPI;
	}

	public static void setServer(String server){
		mSystemServerPrefix = server;
	}
	public static void saveNewFriendsResult(Context context, List<NewFriendItem> list,int count){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}

		SharedPreferences preferences = context.getSharedPreferences("LAST_TIME", mode);
		Editor editor = preferences.edit();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(3000);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(list);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 将Product对象放到OutputStream中
		// 将Product对象转换成byte数组，并将其进行base64编码
		String server = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		// 将编码后的字符串写到base64.xml文件中
		editor.putString("new_friends_list", server);
		editor.putString("last_time", FeatureFunction.formartTime(System.currentTimeMillis()/1000, "yyyy-MM-dd HH:mm:ss"));
		editor.putInt("contact_count", count);
		editor.commit();
	}
	//判断是否登录
	public  static boolean checkLogin(){
		if(TextUtils.isEmpty(IMCommon.getUserId(ChatApplication.getInstance())))
		{
			return false;
		}else {
			return true;
		}
	}

	/**
	 * 退出登录
	 * @param context
	 */
	public static void logout(Context context){
		context.sendBroadcast(new Intent(GlobalParam.SWITCH_TAB));

		SharedPreferences preferences = context.getSharedPreferences(IMCommon.LOGIN_SHARED, 0);
		Editor editor = preferences.edit();
		editor.remove(IMCommon.LOGIN_RESULT);
		editor.commit();
		IMCommon.setUid("");
		IMCommon.setServer("");

		SharedPreferences server = context.getSharedPreferences(IMCommon.SERVER_SHARED, 0);
		Editor serverEditor = server.edit();
		serverEditor.remove(IMCommon.SERVER_PREFIX);
		serverEditor.commit();

		Intent serviceIntent = new Intent(context, IMService.class);
		context.stopService(serviceIntent);
		context.sendBroadcast(new Intent(GlobalParam.ACTION_LOGIN_OUT));
		//清楚通知栏所有的通知
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}
	public static List<NewFriendItem> getNewFriendItemResult(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences("LAST_TIME", mode);
		String str = preferences.getString("new_friends_list", "");
		List<NewFriendItem> tempList = null;
		if(str.equals("")){
			return null;
		}
		// 对Base64格式的字符串进行解码
		byte[] base64Bytes = Base64.decode(str.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			// 从ObjectInputStream中读取Product对象
			// AddNewWord addWord= (AddNewWord ) ois.readObject();
			tempList =  (List<NewFriendItem>) ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<NewFriendItem> list = new ArrayList<NewFriendItem>();
		if(tempList!=null && tempList.size()>0){
			for (int i = 0; i < tempList.size(); i++) {
				if(tempList.get(i).loginId!=null && !tempList.get(i).loginId.equals("")){
					if(tempList.get(i).loginId.equals(IMCommon.getUserId(context))){
						list.add(tempList.get(i));
					}
				}

			}
		}
		return list;
	}

	public static boolean getOpenSound(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(MESSAGE_NOTIFY_SHARED, mode);
		boolean isOpen = true;
		isOpen = preferences.getBoolean(SOUND, true);
		return isOpen;
	}
	
	

	public static void saveNotificationTime(Context context, long time){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NOTIFICATION_TIME_SHARED, mode);
		Editor editor = preferences.edit();
		editor.putLong(NOTIFICATION_TIME, time);
		editor.commit();
	}
	public static long getNotificationTime(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NOTIFICATION_TIME_SHARED, mode);
		return preferences.getLong(NOTIFICATION_TIME, 0);
	}

	public static void saveLoginResult(Context context, Login login){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(LOGIN_SHARED, mode);
		Editor editor = preferences.edit();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(3000);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(login);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 将Product对象放到OutputStream中
		// 将Product对象转换成byte数组，并将其进行base64编码
		String server = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		// 将编码后的字符串写到base64.xml文件中
		editor.putString(LOGIN_RESULT, server);

		editor.commit();
	}

	public static Login getLoginResult(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(LOGIN_SHARED, mode);
		String str = preferences.getString(LOGIN_RESULT, "");
		Login login = null;
		if(str.equals("")){
			return null;
		}
		// 对Base64格式的字符串进行解码
		byte[] base64Bytes = Base64.decode(str.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			// 从ObjectInputStream中读取Product对象
			// AddNewWord addWord= (AddNewWord ) ois.readObject();
			login =  (Login) ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return login;
	}

	public static void setUid(String uid){
		mUid = uid;
	}


	public static String getUserId(Context context){
		String uid = "";
		if(mUid.equals("")){
			Login login = getLoginResult(context);
			if(login != null){
				uid = login.uid;
			}
		}else {
			uid = mUid;
		}
		return uid;
	}

	/**
	 * 保存有新的朋友圈标示
	 * @param context
	 * @param login
	 */
	public static void saveFriendsLoopTip(Context context, int tip){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NEW_TIP_SHARED, mode);
		Editor editor = preferences.edit();
		editor.putInt(FRIENDS_LOOP_TIP, tip);
		editor.putString(LOGIN_ID, IMCommon.getUserId(context));
		editor.commit();
	}

	public static int getFriendsLoopTip(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NEW_TIP_SHARED, mode);
		int isTip = 0;
		String loginId = preferences.getString(LOGIN_ID,"");
		if((loginId!=null && !loginId.equals("")) && loginId.equals(getUserId(context))){
			isTip = preferences.getInt(FRIENDS_LOOP_TIP, 0);
		}
		return isTip;
	}


	/*
	 * 保存是否阅读了发现模块的新消息提示
	 */
	public static void saveReadFriendsLoopTip(Context context, boolean isReadFriendsLoopTip){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NEW_TIP_SHARED, mode);
		Editor editor = preferences.edit();
		editor.putBoolean(READ_FRIENDS_LOOP_TIP, isReadFriendsLoopTip);
		editor.putString(LOGIN_ID, IMCommon.getUserId(context));
		editor.commit();
	}
	public static void saveReadMeetingTip(Context context,	boolean isReadMeetingTip){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NEW_TIP_SHARED, mode);
		Editor editor = preferences.edit();
		editor.putBoolean(READ_MEETING_TIP, isReadMeetingTip);
		editor.putString(LOGIN_ID, IMCommon.getUserId(context));
		editor.commit();
	}

	public static boolean getIsReadFoundTip(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NEW_TIP_SHARED, mode);
		boolean isRead = false;
		String loginId = preferences.getString(LOGIN_ID,"");
		if((loginId!=null && !loginId.equals("")) && loginId.equals(getUserId(context))){
			if(getFriendsLoopTip(context) == 0 &&
					preferences.getBoolean(READ_MEETING_TIP, false)){
				isRead = true;
			}else{
				if(preferences.getBoolean(READ_FRIENDS_LOOP_TIP, false)
						&& preferences.getBoolean(READ_MEETING_TIP, false)){
					isRead = true;
				}
			}
			
		}
		return isRead;
	}



	/**
	 * 保存有新的朋友标示
	 * @param context
	 * @param movingList
	 */

	public static void saveContactTip(Context context, int tip){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(NEW_TIP_SHARED, mode);
		Editor editor = preferences.edit();
		editor.putInt(CONTACT_TIP, tip);
		editor.putString(LOGIN_ID, IMCommon.getUserId(context));
		editor.commit();
	}
	public static int getContactTip(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		int isTip = 0;
		SharedPreferences preferences = context.getSharedPreferences(NEW_TIP_SHARED, mode);
		String loginId = preferences.getString(LOGIN_ID,"");
		if((loginId!=null && !loginId.equals("")) && loginId.equals(getUserId(context))){
			isTip = preferences.getInt(CONTACT_TIP, 0);
		}

		return isTip;
	}



	public static void saveMoving(Context context, List<NotifiyMessage> movingList){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(MOVING_SHARED, mode);
		Editor editor = preferences.edit();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(3000);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(movingList);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 将Product对象放到OutputStream中
		// 将Product对象转换成byte数组，并将其进行base64编码
		String server = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		// 将编码后的字符串写到base64.xml文件中
		editor.putString(MOVING_RESULT, server);

		editor.commit();
	}

	public static List<NotifiyMessage> getMovingResult(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(MOVING_SHARED, mode);
		String str = preferences.getString(MOVING_RESULT, "");
		List<NotifiyMessage> list = null;
		if(str.equals("")){
			return null;
		}
		// 对Base64格式的字符串进行解码
		byte[] base64Bytes = Base64.decode(str.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			// 从ObjectInputStream中读取Product对象
			// AddNewWord addWord= (AddNewWord ) ois.readObject();
			list =  (List<NotifiyMessage>) ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}



	public static void sendMsg (Handler hander,int what,String string){
		Message hintMsg = new Message();
		hintMsg.what = what;
		hintMsg.obj = string;
		hander.sendMessage(hintMsg);
	}

	public static void sendMsg (Handler hander,int what,Object string){
		Message hintMsg = new Message();
		hintMsg.what = what;
		hintMsg.obj = string;
		hander.sendMessage(hintMsg);
	}
	public static void sendMsg (Handler hander,int what,int objcetId){
		Message hintMsg = new Message();
		hintMsg.what = what;
		hintMsg.arg1 = objcetId;
		hander.sendMessage(hintMsg);
	}

	public static void sendMsg (Handler hander,int what,Object objcetId,int arg1){
		Message hintMsg = new Message();
		hintMsg.what = what;
		hintMsg.arg1 = arg1;
		hintMsg.obj = objcetId;
		hander.sendMessage(hintMsg);
	}

	public static void sendMsg (Handler hander,int what,Object objcetId,int arg1,int arg2){
		Message hintMsg = new Message();
		hintMsg.what = what;
		hintMsg.arg1 = arg1;
		hintMsg.arg2 = arg2;
		hintMsg.obj = objcetId;
		hander.sendMessage(hintMsg);
	}

	public static long getTimestamp(String time){

		try {
			java.util.Date  date1 =  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			.parse(time+" 00:00:00");
			/*java.util.Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			.parse("1970/01/01 08:00:00");
			long l = date1.getTime() - date2.getTime() > 0 ? date1.getTime()
					- date2.getTime() : date2.getTime() - date1.getTime();
					long rand = (int)(Math.random()*1000);
			 */
			long l = date1.getTime();
			return l/1000;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static void setCurrentLat(double lat){
		mCurrentLat = lat;
	}

	public static void setCurrentLng(double lng){
		mCurrentLng = lng;
	}

	public static double getCurrentLat(Context context){
		double lat = 0;
		if(mCurrentLat > 0){
			lat = mCurrentLat;
		}else {
			int mode = Context.MODE_WORLD_WRITEABLE;
			if(Build.VERSION.SDK_INT >= 11){
				mode = Context.MODE_MULTI_PROCESS;
			}
			SharedPreferences preferences = context.getSharedPreferences(LOCATION_SHARED,mode);
			String latStr = preferences.getString(LAT, "0");
			lat = Double.parseDouble(latStr);
			mCurrentLat = lat;
		}

		return lat;
	}

	public static double getCurrentLng(Context context){
		double lng = 0;
		if(mCurrentLng > 0){
			lng = mCurrentLng;
		}else {
			int mode = Context.MODE_WORLD_WRITEABLE;
			if(Build.VERSION.SDK_INT >= 11){
				mode = Context.MODE_MULTI_PROCESS;
			}
			SharedPreferences preferences = context.getSharedPreferences(LOCATION_SHARED, mode);
			String latStr = preferences.getString(LNG, "0");
			lng = Double.parseDouble(latStr);
			mCurrentLng = lng;
		}

		return lng;
	}

	public static void appOnStop(Context context){
		if (!FeatureFunction.isAppOnForeground(context) && IMCommon.isApplicationActive) {
			//app 进入后台
			IMCommon.isApplicationActive = false;
			Log.e("TAG", "app 进入后台");
			
		}
	}

	public static void appOnResume(Context context){
		if (!IMCommon.isApplicationActive) {
			//app 从后台唤醒，进入前台
			Log.e("TAG", "app 从后台唤醒，进入前台");
			IMCommon.isApplicationActive = true;

		
			int mode = Context.MODE_WORLD_WRITEABLE;
			if(Build.VERSION.SDK_INT >= 11){
				mode = Context.MODE_MULTI_PROCESS;
			}
			SharedPreferences sharePreferences = context.getSharedPreferences("LAST_TIME", mode);
			String lastTime = sharePreferences.getString("last_time","");
			int contactCount = sharePreferences.getInt("contact_count",0);
			String currentTime = FeatureFunction.formartTime(System.currentTimeMillis()/1000, "yyyy-MM-dd HH:mm:ss");
			try {
				if((lastTime==null || lastTime.equals("")) || !(FeatureFunction.jisuan(lastTime, currentTime))){
					//发送检测新的朋友通知
					Intent intent = new Intent(ChatFragment.ACTION_CHECK_NEW_FRIENDS);
					intent.putExtra("count",contactCount);
					context.sendBroadcast(intent);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断是否为有效的电话号码
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNum(String mobiles)
	{
		Pattern pattern = Pattern.compile("^(1(3|5|8|7)[0-9]{9})$");//"^13/d{9}||15[8,9]/d{8}$");
		Matcher m = pattern.matcher(mobiles);  
		// Log.e("手机格式验证",""+ m.matches());
		if(m.matches() && mobiles.length()==11){
			return true;
		}
		return false;
	}

	

	/**
	 * 保存单张拍照图片url
	 * @param context
	 * @param movingList
	 */

	public static void saveCamerUrl(Context context, String url){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(CAMER_URL_SHARED, mode);
		Editor editor = preferences.edit();
		editor.putString(CAMER_URL, url);
		editor.commit();
	}
	public static String getCamerUrl(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(CAMER_URL_SHARED, mode);
		String camerUrl = preferences.getString(CAMER_URL,"");
		return camerUrl;
	}
	
	
	/**
	 * 保存发布图片的url
	 * @param context
	 * @param movingList
	 */
	public static void saveCamerArrayUrl(Context context,String[] urlString){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(CAMER_URL_SHARED, mode);
		Editor editor = preferences.edit();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(3000);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(urlString);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 将Product对象放到OutputStream中
		// 将Product对象转换成byte数组，并将其进行base64编码
		String server = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		// 将编码后的字符串写到base64.xml文件中
		editor.putString(CAMER_ARRAY_URL, server);

		editor.commit();
	}

	/**
	 * 获取发布图片的url
	 * @param context
	 * @return
	 */
	public static String[] getCamerArrayUrl(Context context){
		int mode = Context.MODE_WORLD_WRITEABLE;
		if(Build.VERSION.SDK_INT >= 11){
			mode = Context.MODE_MULTI_PROCESS;
		}
		SharedPreferences preferences = context.getSharedPreferences(CAMER_URL_SHARED, mode);
		String str = preferences.getString(CAMER_ARRAY_URL, "");
		String[] urlString = null;
		if(str.equals("")){
			return null;
		}
		// 对Base64格式的字符串进行解码
		byte[] base64Bytes = Base64.decode(str.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			// 从ObjectInputStream中读取Product对象
			// AddNewWord addWord= (AddNewWord ) ois.readObject();
			urlString =   (String[]) ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return urlString;
	}



}
