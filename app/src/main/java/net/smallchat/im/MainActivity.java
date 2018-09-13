package net.smallchat.im;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.UploadImg;
import net.smallchat.im.Entity.Version;
import net.smallchat.im.Entity.VersionInfo;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.contact.AddActivity;
import net.smallchat.im.about.AppUpgrade;
import net.smallchat.im.contact.ChooseUserActivity;
import net.smallchat.im.contact.NewFriendsActivity;
import net.smallchat.im.exception.ExceptionHandler;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.fragment.ContactsFragment;
import net.smallchat.im.fragment.FoundFragment;
import net.smallchat.im.fragment.MineFragment;
import net.smallchat.im.friendcircle.SendFriendCircleActivity;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.mediacall.MediaCallCommon;
import net.smallchat.im.mine.CompleteUserInfoActvity;
import net.smallchat.im.api.IMException;
import net.smallchat.im.receiver.NotifySystemMessage;
import net.smallchat.im.scan.CaptureActivity;
import net.smallchat.im.service.IMService;
import net.smallchat.im.widget.PagerSlidingTabStrip;
import net.smallchat.im.widget.SelectAddPopupWindow;
import net.smallchat.im.widget.SelectPicPopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 高仿微信的主界面
 * 
 * http://blog.csdn.net/guolin_blog/article/details/26365683
 * 
 * @author guolin
 */
public class MainActivity extends FragmentActivity implements OnClickListener{

	private static final String LOGTAG = "XMPP";
	//标记是否打开登录界面
	private boolean isOpenLogin=false;
	/**
	 * 定义全局变量
	 */
	private File mCameraTempimageFile=null;
	private List<UploadImg> mImageList = new ArrayList<UploadImg>();

	private boolean mIsRegisterReceiver = false;
	
	
	protected ImageView  mSearchBtn,mAddBtn,mMoreBtn;
	private TextView mTitleView;
	private RelativeLayout mTitleLayout;
	
	protected AlertDialog mUpgradeNotifyDialog;
	private Version mVersion;
	protected AppUpgrade mClientUpgrade;


	private List<PagerContent> mContentList;

	/**
	 * PagerSlidingTabStrip的实例
	 */
	private PagerSlidingTabStrip tabs;

	/**
	 * 获取当前屏幕的密度
	 */
	private DisplayMetrics dm;

	// 自定义的弹出框类
	SelectPicPopupWindow menuWindow; // 弹出框
	SelectAddPopupWindow topAddMenuWindow; // 弹出框



	private Timer mTimer;
	private StartServiceTask mServiceTask;

	private ViewPager mPager;
	private Context mContext;

	/**
	 * 导入控件
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mContentList = initPagerContent();
		setContentView(R.layout.activity_main);
		registerNetWorkMonitor();
		setActionBarLayout(/*R.layout.title_layout*/);
		Thread.currentThread().setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		FeatureFunction.newFolder(Environment.getExternalStorageDirectory() + FeatureFunction.PUB_TEMP_DIRECTORY);

		dm = getResources().getDisplayMetrics();
		mPager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);



		mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		tabs.setViewPager(mPager);
		setTabsValue();


//		mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
//		tabs.setViewPager(mPager);
//		setTabsValue();


		if(IMCommon.getLoginResult(mContext) == null){
			Intent intent = new Intent(mContext, LoginActivity.class);
			startActivityForResult(intent, GlobalParam.LOGIN_REQUEST);
		}else {
			loginXMPP();

			sessionPromptUpdate();
			Intent intent = new Intent(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP);
			intent.putExtra("found_type", 1);
			mContext.sendBroadcast(intent);
			if(IMCommon.getFriendsLoopTip(mContext)!=0){
				//有新朋友圈发现小红点显示
				tabs.setNewMsgTip(1, 2);
				mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_NEW_FRIENDS_LOOP));
			}
		}
	}

	
	
	/**
	 * 自定义titlebar
	 */
	public void setActionBarLayout( /*int layoutId */){

	   /* ActionBar actionBar = getActionBar( );

	    if( null != actionBar ){

	        actionBar.setDisplayShowHomeEnabled( false );

	        actionBar.setDisplayShowCustomEnabled(true);
*/

	   /*     LayoutInflater inflator = (LayoutInflater)   this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	        View v = inflator.inflate(layoutId, null);*/

	      /*  ActionBar.LayoutParams layout = new     ActionBar.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	        actionBar.setCustomView(v,layout);
	        */
			mTitleLayout = (RelativeLayout)findViewById(R.id.title_layout);
		

	        mTitleView = (TextView)findViewById(R.id.title);
	        mTitleView.setText(mContext.getResources().getString(R.string.chat_app_name));
	        
	        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
	        mAddBtn = (ImageView)findViewById(R.id.add_btn);
	        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
	        
	        mSearchBtn.setVisibility(View.GONE);
	        mAddBtn.setVisibility(View.VISIBLE);
	        mMoreBtn.setVisibility(View.GONE);
	        

	        mAddBtn.setOnClickListener(this);
	        mMoreBtn.setOnClickListener(this);
	        

	  /*  }*/

	}

	
	

	/**
	 * 检测用户是否输入昵称
	 * @param login
	 * @return
	 */
	private boolean checkValue(Login login){
		boolean ischeck = true;
		if(login == null || login.equals("") ){
			ischeck = false;
		}else{
			if (/*(login.headSmall == null || login.headSmall.equals(""))
					|| */(login.nickname== null || login.nickname.equals(""))
					) {
				ischeck = false;
			}
		}
		return ischeck;

	}

	private void loginIM() {
		//登录视频与语音通话服务器
		MediaCallCommon.init(this);
	}

	/**
	 * 连接到xmpp
	 */
	private void loginXMPP(){

		startGuidePage();

		mServiceTask = new StartServiceTask(mContext);
		mTimer = new Timer("starting");
		mTimer.scheduleAtFixedRate(mServiceTask, 0, 5000);
		
		// Login IM server
		loginIM();
	}

	/**
	 * 检测用是否填写昵称，如果没有则跳转到完善资料页进行填写
	 */
	private void startGuidePage(){
		Login login = IMCommon.getLoginResult(mContext);

		if(checkValue(login)){
			/*SharedPreferences preferences = this.getSharedPreferences(reSearchCommon.SHOWGUDIEVERSION, 0);
				int version = preferences.getInt("app_version", 0);
				//version = 0;
				if (version != FeatureFunction.getAppVersion(mContext)) {
					Intent intent = new Intent();
					intent.setClass(SmallVideoRecorderActivity.this, GuideActivity.class);
					startActivityForResult(intent, GlobalParam.SHOW_GUIDE_REQUEST);
					//isShowGudie = true;
				}else {*/
			checkUpgrade();//检测新版本
			/*}*/
		}else{//跳转到完善资料页
			Intent completeIntent = new Intent();
			completeIntent.setClass(mContext, CompleteUserInfoActvity.class);
			completeIntent.putExtra("login", login);
			startActivityForResult(completeIntent, GlobalParam.SHOW_COMPLETE_REQUEST);
		}

		//return isShowGudie;
	}

	/**
	 * 开启聊天服务
	 * @author dongli
	 *
	 */
	private final class StartServiceTask extends TimerTask{
		private Context context;
		StartServiceTask(Context context){
			this.context = context;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(getBaseContext(), IMService.class);
			this.context.startService(intent);
		}
	}

	/**
	 * 注册通知
	 */
	private void registerNetWorkMonitor() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_NETWORK_CHANGE);
		filter.addAction(GlobalParam.EXIT_ACTION);
		filter.addAction(GlobalParam.ACTION_REFRESH_NOTIFIY);
		filter.addAction(GlobalParam.ACTION_UPDATE_SESSION_COUNT);
		filter.addAction(GlobalParam.ACTION_CALLBACK);
		filter.addAction(GlobalParam.ACTION_REFRESH_FRIEND);
		filter.addAction(GlobalParam.ACTION_LOGIN_OUT);
		filter.addAction(NotifySystemMessage.ACTION_VIP_STATE);
		filter.addAction(GlobalParam.CANCLE_COMPLETE_USERINFO_ACTION);
		filter.addAction(GlobalParam.ACTION_SHOW_TOAST);
		filter.addAction(GlobalParam.ACTION_SHOW_REGISTER_REQUEST);
		filter.addAction(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP);
		filter.addAction(GlobalParam.ACTION_HIDE_FOUND_NEW_TIP);
		filter.addAction(GlobalParam.ACTION_SHOW_CONTACT_NEW_TIP);
		filter.addAction(GlobalParam.ACTION_HIDE_CONTACT_NEW_TIP);
		//filter.addAction(GlobalParam.ACTION_UPDATE_MEETING_SESSION_COUNT);
		registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;
	}

	/**
	 * 检测通知类型，进行不同的操作
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if (action.equals(GlobalParam.ACTION_NETWORK_CHANGE)) {//网络通知
				boolean isNetConnect = false;
				ConnectivityManager connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				NetworkInfo activeNetInfo = connectivityManager
						.getActiveNetworkInfo();
				if (activeNetInfo != null) {
					if (activeNetInfo.isConnected()) {
						isNetConnect = true;
						/*Toast.makeText(
								context,
								"xxxxxxxxxxxxxxxxxxxxxxxxx"
										+ activeNetInfo.getTypeName(),
								Toast.LENGTH_SHORT).show();*/
					} else {
						/*Toast.makeText(
								context,
								"xxxxxxxxxxxxxxxxxxxxxxxxx" + " "
										+ activeNetInfo.getTypeName(),
								Toast.LENGTH_SHORT).show();*/
					}
				} else {
					/*Toast.makeText(context, mContext.getResources().getString(R.string.network_error),
							Toast.LENGTH_SHORT).show();*/
				}
				IMCommon.setNetWorkState(isNetConnect);
			} else if (action.equals(GlobalParam.SWITCH_TAB)){//却换到第一个标签
				mPager.setCurrentItem(0);
				//tabs.set
				//mTabHost.setCurrentTabByTag(CHATS);
			}else if(action.equals(GlobalParam.EXIT_ACTION)){//退出登录
				//reSearchCommon.CancelNotifyAlarm(mContext);
				moveTaskToBack(true);
				System.exit(0);
				//moveTaskToBack(true);
				//System.exit(0);
			}else if(GlobalParam.CANCLE_COMPLETE_USERINFO_ACTION.equals(action)){//跳转到登陆界面
				Intent loginIntent = new Intent(mContext, LoginActivity.class);
				startActivityForResult(loginIntent, GlobalParam.LOGIN_REQUEST);
			}else if(GlobalParam.ACTION_UPDATE_SESSION_COUNT.equals(action)){//消息未读消息数
				sessionPromptUpdate();
			}
			else if(GlobalParam.ACTION_LOGIN_OUT.equals(action)){//切换用户登陆

				IMCommon.saveLoginResult(ChatApplication.getInstance(),null);
                IMCommon.setUid("");

				mPager.setCurrentItem(0);
				try {
					mTimer.cancel();
				} catch (Exception e) {
				}
				if(!isOpenLogin) {
					Log.d("error", "action ===" + action);
					mPager.setCurrentItem(0);
					try {
						mTimer.cancel();
					} catch (Exception e) {
					}
					//启动登录页面
					Log.d("info", "ACTION_LOGIN_OUT 切换账号 启动登录界面");
					Log.d("Login", "LoginActivity 3======================");
					isOpenLogin=true;
					Intent loginIntent = new Intent(mContext, LoginActivity.class);

					startActivityForResult(loginIntent, GlobalParam.LOGIN_REQUEST);
				}

			}else if(GlobalParam.ACTION_SHOW_TOAST.equals(action)){//显示账号在其他设备登陆的通知
				String hintMsg = intent.getStringExtra("toast_msg");
				if(hintMsg!=null && !hintMsg.equals("")){
					Toast.makeText(mContext, hintMsg,Toast.LENGTH_LONG).show();
					//强制退出
					Log.d("error","强制踢下线");
					IMCommon.logout(mContext);

					ChatApplication.getInstance().sendBroadcast(new Intent(GlobalParam.ACTION_DESTROY_CURRENT_ACTIVITY));
					Intent serviceIntent = new Intent(ChatApplication.getInstance(), IMService.class);
					ChatApplication.getInstance().stopService(serviceIntent);

					ChatApplication.getInstance().sendBroadcast(new Intent(GlobalParam.ACTION_LOGIN_OUT));

				}
			}else if(GlobalParam.ACTION_SHOW_REGISTER_REQUEST.equals(action)){//注册账号成功后，登陆到xmpp
				//Log.e("SmallVideoRecorderActivity-onActivityResult", "注册成功,完善资料！+++++++");
				loginXMPP();

				sessionPromptUpdate();
			}else if(action.equals(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP)){//显示朋友圈和会议有新的消息
				int type = intent.getIntExtra("found_type", 0);
				if(type ==1){
					meetingPromptUpdate();
				}else if(type  == 2){//有新的会议通知，显示小红点
					tabs.setNewMsgTip(1, 2);
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_NEW_MEETING));
				}else{//朋友圈有新的动态
					int tipCount = IMCommon.getFriendsLoopTip(mContext);
					tipCount = tipCount+1;
					IMCommon.saveFriendsLoopTip(mContext, tipCount);
					tabs.setNewMsgTip(1, 2);
					/*Intent fondIntent = new Intent(GlobalParam.ACTION_SHOW_NEW_FRIENDS_LOOP);
					fondIntent.putExtra("count",tipCount);*/
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_NEW_FRIENDS_LOOP));
				}
				
			}else if(action.equals(GlobalParam.ACTION_HIDE_FOUND_NEW_TIP)){
				//隐藏发现按钮旁边的小红点
				
				int type = intent.getIntExtra("found_type",0);
				if(type !=0){
					IMCommon.saveFriendsLoopTip(mContext, 0);
				}
				if(IMCommon.getIsReadFoundTip(mContext)){
					tabs.hideMsgTip(2);
				}
			}else if(action.equals(GlobalParam.ACTION_SHOW_CONTACT_NEW_TIP)){
				Log.d(LOGTAG,"新的联系人：ACTION_SHOW_CONTACT_NEW_TIP");
				//显示有新的联系人
				IMCommon.saveContactTip(mContext, 1);
				//新联系人小红点通知
				tabs.setNewMsgTip(1, 1);
			}else if(action.equals(GlobalParam.ACTION_HIDE_CONTACT_NEW_TIP)){
				//隐藏有新的联系人小红点
				IMCommon.saveContactTip(mContext, 0);
				tabs.hideMsgTip(1);
			}
			
		}
	};


	//显示未读显示数
	public void sessionPromptUpdate(){
		SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
		SessionTable table = new SessionTable(db);
		int count = table.querySessionCount(ChatType.MeetingMessage);
		//未读消息 小红点显示
		tabs.setNewMsgTip(count, 0);
	}
	
	/**
	 * 查询未读会议数据
	 */
	public boolean meetingPromptUpdate(){
		boolean isExits = false;
		SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
		SessionTable table = new SessionTable(db);
		int count = table.queryMeetingSessionCount();
		//未读会议 小红点显示
		tabs.setNewMsgTip(count, 2);
		if(count!=0){
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_NEW_MEETING));
				}
			}, 1000);
			
			isExits = true;
		}
		return isExits;
	}


	/**
	 * 对PagerSlidingTabStrip的各项属性进行赋值。
	 */
	private void setTabsValue() {
		// 设置Tab是自动填充满屏幕的
		tabs.setShouldExpand(true);
		// 设置Tab的分割线是透明的
		tabs.setDividerColor(Color.TRANSPARENT);
		// 设置Tab底部线的高度
		tabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, dm));
		// 设置Tab Indicator的高度
		tabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,5, dm));
		// 设置Tab标题文字的大小
		tabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 14, dm));
		// 设置Tab Indicator的颜色
		tabs.setIndicatorColor(Color.parseColor("#ff0000"));
		// 设置选中Tab文字的颜色 (这是我自定义的一个方法)
		tabs.setSelectedTextColor(Color.parseColor("#45c01a"));
		// 取消点击Tab时的背景色
		tabs.setTabBackground(0);
	}

	private List<PagerContent> initPagerContent() {
		List<PagerContent> list = new ArrayList<PagerContent>();
		list.add(new PagerContent(R.string.main_tab_chat, R.drawable.main_tab_icon_chat, ChatFragment.class));
		list.add(new PagerContent(R.string.main_tab_contact, R.drawable.main_tab_icon_contact, ContactsFragment.class));
		list.add(new PagerContent(R.string.main_tab_found, R.drawable.main_tab_icon_found, FoundFragment.class));
		list.add(new PagerContent(R.string.main_tab_mine, R.drawable.main_tab_icon_mine, MineFragment.class));
		return list;
	}
	/**
	 * fragment 适配器
	 * @author dongli
	 *
	 */
	public class MyPagerAdapter extends FragmentPagerAdapter  implements PagerSlidingTabStrip.IconTabProvider  {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mContentList.get(position).mTitle;
		}

		@Override
		public int getPageIconResId(int position) {
			return mContentList.get(position).mIconId;
		}

		@Override
		public int getCount() {
			return mContentList != null ? mContentList.size() : 0;
		}

		@Override
		public Fragment getItem(int position) {
			if(position < getCount()){
				PagerContent content = mContentList.get(position);
				if(content.fragment == null ) {
					content.fragment = Fragment.instantiate(mContext, content.fragmentClass.getName(), content.bundle);
				}

				return content.fragment;
			}
			return  null;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.e("destroyItem", "destroyItem");
			super.destroyItem(container, position, object);
		}

		@Override
		public long getItemId(int position) {
			//mCurrentTabIndex = position;
			return super.getItemId(position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.e("instantiateItem", "instantiateItem");
			return super.instantiateItem(container, position);
		}
	}

	private class PagerContent{
		public String mTitle;
		public int mIconId;
		public Fragment fragment;
		public Class fragmentClass;
		public Bundle bundle;

		public int itemId;

		public PagerContent(int titleId, int iconId, Class _class) {

			itemId = titleId;
			mTitle = getString(titleId);
			mIconId = iconId;
			fragmentClass = _class;
			bundle = null;
		}

		public PagerContent(int titleId, int iconId, Class _class, Bundle b) {
			itemId = titleId;
			mTitle = getString(titleId);
			mIconId = iconId;
			fragmentClass = _class;
			bundle = b;
		}

		public void setTitle(String title){
			mTitle = title;
		}

		public void setIcon(int id){
			mIconId = id;
		}

	}


	public void topAddMenu(final Activity context,View view) {
		if (topAddMenuWindow != null && topAddMenuWindow.isShowing()) {
			topAddMenuWindow.dismiss();
			topAddMenuWindow = null;
		}
		topAddMenuWindow = new SelectAddPopupWindow(MainActivity.this, topAddMenuitemsOnClick);
		// 显示窗口

		// 计算坐标的偏移量
		int xoffInPixels = topAddMenuWindow.getWidth() - view.getWidth()+10;
		topAddMenuWindow.showAsDropDown(view, -xoffInPixels, 0);
	}

	// 为弹出窗口实现监听类
	private OnClickListener topAddMenuitemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.chat_layout:
				Intent intent = new Intent();
				intent.setClass(mContext,ChooseUserActivity.class);
				intent.putExtra("jumpfrom",1);
				mContext.startActivity(intent);
				break;
			case R.id.add_friend:
				Intent addIntent = new Intent();
				addIntent.setClass(mContext, AddActivity.class);
				startActivity(addIntent);
				break;
			case R.id.shao_layout:
				Intent scanIntent = new Intent(mContext, CaptureActivity.class);
				startActivity(scanIntent);
				break;
			case R.id.photo_share:
				selectImg();
				break;

			default:
				break;
			}
			if (topAddMenuWindow != null && topAddMenuWindow.isShowing()) {
				topAddMenuWindow.dismiss();
				topAddMenuWindow = null;
			}
		}
	};
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			//exitDialog();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
	

	/**
	 * 销毁页面
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mIsRegisterReceiver){
			mIsRegisterReceiver = false;
			unregisterReceiver(mReceiver);
		}
		// Verify picture cache files whose created date more than Fifteen days.
		System.exit(0);
	}

	/**
	 * 页面返回结果
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case GlobalParam.LOGIN_REQUEST:
			if (resultCode == GlobalParam.RESULT_EXIT) {// dl repair
				//moveTaskToBack(true);
				MainActivity.this.finish();
				return;
			}else if(resultCode == RESULT_OK){
				isOpenLogin=false;
				new Handler().postDelayed(new Runnable(){
					@Override
					public void run() {
						int mode = Context.MODE_WORLD_WRITEABLE;
						if(Build.VERSION.SDK_INT >= 11){
							mode = Context.MODE_MULTI_PROCESS;
						}
						SharedPreferences sharePreferences = mContext.getSharedPreferences("LAST_TIME", mode);
						String lastTime = sharePreferences.getString("last_time","");
						int contactCount = sharePreferences.getInt("contact_count",0);
						String currentTime = FeatureFunction.formartTime(System.currentTimeMillis()/1000, "yyyy-MM-dd HH:mm:ss");
						try {
							if((lastTime==null || lastTime.equals("")) || !(FeatureFunction.jisuan(lastTime, currentTime))){
								//发送检测新的朋友通知
								Intent checkIntent = new Intent(ChatFragment.ACTION_CHECK_NEW_FRIENDS);
								checkIntent.putExtra("count",contactCount);
								mContext.sendBroadcast(checkIntent);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 2000);
				
				/**
				 * 连接到xmpp、初始化页面
				 */
				loginXMPP();
				mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
				tabs.setViewPager(mPager);
				setTabsValue();
				sendBroadcast(new Intent(GlobalParam.SWITCH_TAB));
				sessionPromptUpdate();
				Intent sintent = new Intent(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP);
				sintent.putExtra("found_type", 1);
				mContext.sendBroadcast(sintent);
				if(IMCommon.getFriendsLoopTip(mContext)!=0){
					//新朋友圈消息 发现显示小红点
					tabs.setNewMsgTip(1, 2);
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_NEW_FRIENDS_LOOP));
				}
			}
			break;
		case GlobalParam.SHOW_GUIDE_REQUEST:
			if(resultCode == RESULT_OK){

				checkUpgrade();

			}
			break;
		case GlobalParam.SHOW_COMPLETE_REQUEST:
			if(resultCode == GlobalParam.SHOW_COMPLETE_RESULT){
				loginXMPP();
				mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
				tabs.setViewPager(mPager);
				setTabsValue();
				sessionPromptUpdate();
				Intent sintent = new Intent(GlobalParam.ACTION_SHOW_FOUND_NEW_TIP);
				sintent.putExtra("found_type", 1);
				mContext.sendBroadcast(sintent);
				if(IMCommon.getFriendsLoopTip(mContext)!=0){
					//有新朋友圈 发现显示小红点
					tabs.setNewMsgTip(1, 2);
					mContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_NEW_FRIENDS_LOOP));
				}
			}
			break;
		default:
			break;
		}

		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if(intent == null){
			return ;
		}

		boolean isChatNotify = intent.getBooleanExtra("chatnotify", false);
		boolean isNotify = intent.getBooleanExtra("notify", false);
		if(isChatNotify){
			Login user = (Login)intent.getSerializableExtra("data");
			user.mIsRoom = intent.getIntExtra("type", 100);
			Intent chatIntent = new Intent(mContext, ChatMainActivity.class);
			chatIntent.putExtra("data", user);
			startActivity(chatIntent);
		}else if(isNotify){
			Intent chatIntent = new Intent(mContext, NewFriendsActivity.class);
			startActivity(chatIntent);
		}else {
			sendBroadcast(new Intent(GlobalParam.SWITCH_TAB));
		}

		super.onNewIntent(intent);
	}

	/**
	 *  拍照分享
	 */
	private void selectImg(){
		Intent intent = new Intent();
		intent.setClass(mContext, SendFriendCircleActivity.class);
		startActivity(intent);
	}



	/**
	 * 处理消息
	 */
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.SHOW_UPGRADE_DIALOG:
				showUpgradeDialog();
				break;
			case GlobalParam.NO_NEW_VERSION:
				Toast.makeText(getApplicationContext(), mContext.getResources().getString(R.string.no_version), Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
				return;

			case GlobalParam.MSG_TIME_OUT_EXCEPTION:
				String message=(String)msg.obj;
				if (message==null || message.equals("")) {
					message=mContext.getResources().getString(R.string.timeout);
				}
				Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}

	};

	/**
	 * 检测更新
	 */
	private void checkUpgrade(){
		new Thread(){
			@Override
			public void run() {
				if(IMCommon.verifyNetwork(mContext)){
					try {

						VersionInfo versionInfo = IMCommon.getIMServerAPI().checkUpgrade(FeatureFunction.getAppVersionName(mContext));
						if(versionInfo != null && versionInfo.mVersion!=null && versionInfo.mState != null && versionInfo.mState.code == 0){
							mClientUpgrade = new AppUpgrade();
							mVersion = versionInfo.mVersion;
							if(mClientUpgrade.compareVersion(FeatureFunction.getAppVersionName(mContext), mVersion.version)){
								mHandler.sendEmptyMessage(GlobalParam.SHOW_UPGRADE_DIALOG);
							}else{
								//mHandler.sendEmptyMessage(NO_NEW_VERSION);
							}
						}
					} catch (IMException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}


	/**
	 * 初始化版本更新对话框
	 */
	private void showUpgradeDialog() {
		LayoutInflater factor = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View serviceView = factor.inflate(R.layout.client_dialog, null);
		TextView titleTextView = (TextView) serviceView.findViewById(R.id.title);
		titleTextView.setText(mContext.getResources().getString(R.string.check_new_version));
		TextView contentView = (TextView) serviceView.findViewById(R.id.updatelog);
		contentView.setText(mVersion.discription);
		Button okBtn = (Button)serviceView.findViewById(R.id.okbtn);
		okBtn.setText(mContext.getResources().getString(R.string.upgrade));
		okBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {

				showDownloadApkDilog();//下载新的版本

				if (mUpgradeNotifyDialog != null){
					mUpgradeNotifyDialog.dismiss();
					mUpgradeNotifyDialog = null;
				}
			}
		});

		Button cancelBtn = (Button)serviceView.findViewById(R.id.cancelbtn);
		cancelBtn.setText(mContext.getResources().getString(R.string.cancel));
		cancelBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {//隐藏版本更新对话框
				if (mUpgradeNotifyDialog != null){
					mUpgradeNotifyDialog.dismiss();
					mUpgradeNotifyDialog = null;
				}
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		mUpgradeNotifyDialog = builder.create();
		mUpgradeNotifyDialog.show();
		mUpgradeNotifyDialog.setContentView(serviceView);
		FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		layout.setMargins(FeatureFunction.dip2px(mContext, 10), 0, FeatureFunction.dip2px(mContext, 10), 0);
		serviceView.setLayoutParams(layout);
	}

	private void showDownloadApkDilog() {
		if (mVersion != null) {
			try {
				Uri uri = Uri.parse(mVersion.downloadUrl);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			} catch (Exception e) {
				/*Toast.makeText(mContext, R.string.upgradfail,
						Toast.LENGTH_LONG).show();*/
			}

		}
	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.tid.search_btn:
//			MainSearchDialog dialog = new MainSearchDialog(mContext,0);
//			dialog.show();
//			break;
		case R.id.add_btn:
			topAddMenu(MainActivity.this,mTitleLayout);
			break;
		
		default:
			break;
		}
	}



}