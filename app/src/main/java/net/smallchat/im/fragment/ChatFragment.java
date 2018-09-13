package net.smallchat.im.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import net.smallchat.im.R;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.Entity.UserList;
import net.smallchat.im.adapter.ChatTabAdapter;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.SystemContactGlobal;
import net.smallchat.im.api.IMException;


/**
 * 聊天Fragment的界面
 * @author dl
 */
public class ChatFragment extends Fragment implements OnItemClickListener {
	/**
	 * 定义全局变量
	 */
	
	private View mView;
	private boolean mIsRegisterReceiver = false;
	//private Button mEditBtn;
	private ListView mListView;
	private ChatTabAdapter mAdapter;
	public final static int SHOW_PROGRESSDIALOG = 11101;
	public final static int HIDE_PROGRESSDIALOG = 11102;
	public final static int SHOW_REFRESHING_INDECATOR = 11103;
	public final static int HIDE_REFRESHING_INDECATOR = 11104;
	public final static int SHOW_LOADINGMORE_INDECATOR = 11105;
	public final static int HIDE_LOADINGMORE_INDECATOR = 11106;
	public final static int NETWORK_ERROR		= 11107;
	public final static int SHOW_MSG_NOTIFY		= 11108;
	public final static int SHOW_SCROLLREFRESH  = 11117;
	public final static int HIDE_SCROLLREFRESH  = 11118;
	private List<Session> mSessionList;
	private List<Login> mUserList = new ArrayList<Login>();

	public static final String ACTION_REFRESH_SESSION = "im_action_refresh_session";
	public static final String ACTION_RESET_SESSION_COUNT = "im_action_reset_session_count";
	public final static String DELETE_SESSION_ACTION = "im_delete_session_action";
	public final static String CREATE_MUC_SUCCESS = "im_create_muc_success_action";
	public final static String JOIN_MUC_SUCCESS = "im_join_muc_success";
	public final static String CLEAR_REFRESH_ADAPTER = "im_clear_refresh_adapter";
	public final static String DELETE_ROOM_SUCCESS = "im_delete_room_success";
	public final static String DELETE_ROOM_FAILED = "im_delete_room_failed";
	public final static String REFRESH_ROOM_NAME_ACTION = "im_refresh_room_name_action";
	public final static String ACTION_CHECK_NEW_FRIENDS = "im_action_check_new_friends";
	private SystemContactGlobal mContact ;
	private int mContactCount;


	private int mDeletePos = -1;



	private Context mParentContext;
	private ProgressDialog mProgressDialog;



	/**
	 * 导入控件
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView =inflater.inflate(R.layout.chat_tab, container, false);  
		return mView;
	}



	/**
	 * 初始化控件
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mParentContext = (Context)ChatFragment.this.getActivity();
		initRegister();


		initComponent();
	}

	/**
	 * 注册通知
	 */
	private void initRegister(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.SWITCH_LANGUAGE_ACTION);
		filter.addAction(ACTION_REFRESH_SESSION);
		filter.addAction(ACTION_RESET_SESSION_COUNT);
		filter.addAction(DELETE_SESSION_ACTION);
		filter.addAction(CREATE_MUC_SUCCESS);
		filter.addAction(JOIN_MUC_SUCCESS);
		filter.addAction(CLEAR_REFRESH_ADAPTER);
		filter.addAction(DELETE_ROOM_SUCCESS);
		filter.addAction(DELETE_ROOM_FAILED);
		filter.addAction(GlobalParam.REFRESH_ALIAS_ACTION);
		filter.addAction(GlobalParam.BE_KICKED_ACTION);
		filter.addAction(GlobalParam.BE_EXIT_ACTION);
		filter.addAction(GlobalParam.ROOM_BE_DELETED_ACTION);
		filter.addAction(GlobalParam.INVITED_USER_INTO_ROOM_ACTION);
		filter.addAction(GlobalParam.ACTION_CANCLE_NEW_ORDER);
		filter.addAction(GlobalParam.ACTION_CANCLE_NEW_SERVICE);
		filter.addAction(GlobalParam.ACTION_CANCLE_NEW_OUTLANDER);
		filter.addAction(ACTION_CHECK_NEW_FRIENDS);
		filter.addAction(GlobalParam.ACTION_RESET_GROUP_NAME);
		filter.addAction(GlobalParam.ACTION_REFRESH_CHAT_HEAD_URL);
		mParentContext.registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;
	}


	/**
	 * 处理通知，根据不同类型做相应的更改
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null){
				String action = intent.getAction();
				Log.e("ChatFragment_onReceive", "onReceive");
				if(action.equals(GlobalParam.SWITCH_LANGUAGE_ACTION)//重新加载聊天记录
						|| action.equals(ACTION_REFRESH_SESSION)){
					initSession(false);
				}else if(action.equals(ACTION_CHECK_NEW_FRIENDS)){	//检测是否有新的朋友
					mContactCount = intent.getIntExtra("count", 0);
					mContact = new SystemContactGlobal(mParentContext,mHandler);
				}else if(action.equals(DELETE_ROOM_SUCCESS)){//删除群成功
					String froomId = intent.getStringExtra("froom_id");
					SQLiteDatabase db = DBHelper.getInstance(mParentContext).getWritableDatabase();
					MessageTable messageTable = new MessageTable(db);
					messageTable.delete(froomId,300);
					SessionTable sessionTable = new SessionTable(db);
					sessionTable.delete(froomId,300);
					for (int i = 0; i <mSessionList.size(); i++) {
						if(mSessionList.get(i).getFromId().equals(froomId)){
							mSessionList.remove(i);
							break;
						}
					}
					mAdapter.notifyDataSetChanged();
					hideProgressDialog();
					Toast.makeText(mParentContext,mParentContext.getResources().getString(R.string.operate_success), Toast.LENGTH_SHORT).show();

				}else if(action.equals(GlobalParam.ACTION_RESET_GROUP_NAME)){//刷新群名称
					String groupId = intent.getStringExtra("group_id");
					String groupName = intent.getStringExtra("group_name");
					if((groupId!=null && !groupId.equals(""))
							&& (groupName!=null && !groupName.equals(""))){
						for (int i = 0; i < mSessionList.size(); i++) {
							if(mSessionList.get(i).getFromId().equals(groupId)){
								mSessionList.get(i).name = groupName;
								if(mAdapter!=null){
									mAdapter.notifyDataSetChanged();
								}
							}
						}
					}
				}else if(action.equals(GlobalParam.ACTION_REFRESH_CHAT_HEAD_URL)){//刷新聊天记录的头像
					String oldUrl = intent.getStringExtra("oldurl");
					String newUrl = intent.getStringExtra("newurl");
					for (int i = 0; i < mSessionList.size(); i++) {
						String headUrl = mSessionList.get(i).heading;
						if(headUrl!=null && !headUrl.equals("")){
							String[] urlArray = headUrl.split(",");
							if(urlArray.length>0){
								for (int j = 0; j < urlArray.length; j++) {
									if(urlArray[j].equals(oldUrl)){
										urlArray[j] = newUrl;
										String newHeadUrl="";
										for (int j2 = 0; j2 < urlArray.length; j2++) {
											if(i == urlArray.length- 1){
												newHeadUrl += urlArray[j2];
												continue;
											}
											newHeadUrl += urlArray[j2]+",";
										}
										mSessionList.get(i).heading = newHeadUrl;
										SQLiteDatabase db = DBHelper.getInstance(mParentContext).getWritableDatabase();
										SessionTable sessionTable = new SessionTable(db);
										sessionTable.update(mSessionList.get(i), mSessionList.get(i).type);
										break;
									}
								}
							}
						}
					}
					if(mAdapter!=null){
						mAdapter.notifyDataSetChanged();
					}
					
				}
			}
		}

	};

	/**
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_GET_CONTACT_DATA:
				Log.e("ChatsTAb_mHandler","get system contact dataing...");
				break;
			case GlobalParam.MSG_SHOW_LISTVIEW_DATA:
				if(mContact.getContactCount()==mContactCount){
					return;
				}
				if(IMCommon.getLoginResult(mParentContext)!=null
						&& IMCommon.getLoginResult(mParentContext).isTuiJianContact){
					checkNewFriends();
				}

				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				Toast.makeText(mParentContext,R.string.network_error,Toast.LENGTH_LONG).show();
				hideProgressDialog();
				break;
			case GlobalParam.MSG_TIME_OUT_EXCEPTION:
				hideProgressDialog();
				String timeOutMsg = (String)msg.obj;
				Toast.makeText(mParentContext, timeOutMsg, Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 删除消息
	 * @param pos
	 */
	private void delete(int pos){
		if(pos>=mSessionList.size()|| pos<0){
			return;
		}
		SQLiteDatabase db = DBHelper.getInstance(mParentContext).getWritableDatabase();
		MessageTable messageTable = new MessageTable(db);
		messageTable.delete(mSessionList.get(pos).getFromId(), mSessionList.get(pos).type);
		SessionTable sessionTable = new SessionTable(db);
		sessionTable.delete(mSessionList.get(pos).getFromId(),mSessionList.get(pos).type);
		mSessionList.remove(pos);
		mAdapter.notifyDataSetChanged();
		mParentContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));
		NotificationManager notificationManager = (NotificationManager) mParentContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);

	}


	/**
	 * 销毁页面
	 */
	@Override
	public void onDestroy() {
		if(mIsRegisterReceiver){
			mParentContext.unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}



	/**
	 * 实例化控件
	 */
	private void initComponent(){

		mListView = (ListView) mView.findViewById(R.id.chats_list);
		mListView.setCacheColorHint(0);
		mListView.setDivider(null/*getResources().getDrawable(R.drawable.order_devider_line)*/);
		mListView.setOnItemClickListener(this);
		mListView.setOnCreateContextMenuListener(this);
		initSession(true);

	}


	/**
	 * 获取消息数据
	 * @param isFirst
	 */
	private void initSession(boolean isFirst){
		SQLiteDatabase db = DBHelper.getInstance(mParentContext).getReadableDatabase();
		SessionTable table = new SessionTable(db);
		List<Session> tempList = new ArrayList<Session>();
		if(mSessionList!=null && mSessionList.size()>0){
			mSessionList.clear();
			if(mAdapter!=null){
				mAdapter.notifyDataSetChanged();
			}
		}
		tempList = table.query();
		if(mSessionList == null){
			mSessionList = new ArrayList<Session>();
		}/*else if(mSessionList.size() != 0){
			sort(mSessionList);
		}*/
		if(tempList!=null){
			mSessionList.addAll(tempList);
		}
		if(isFirst){
			mAdapter = new ChatTabAdapter(mParentContext, mSessionList);
			mListView.setAdapter(mAdapter);
		}else{
			updateListView();
		}
		
	}

	/**
	 * 对消息记录排序
	 * @param sessionlist
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sort(List<Session> sessionlist) {
		Collections.sort(sessionlist, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ( (Long)((Session) o2).lastMessageTime)
						.compareTo((Long) ((Session) o1).lastMessageTime);
			}
		});
	}

	/**
	 * 显示消息数据
	 */
	private void updateListView(){
		if(mAdapter != null){
			mAdapter.setData(mSessionList);
			mAdapter.notifyDataSetChanged();
			return;
		}

		mAdapter = new ChatTabAdapter(mParentContext, mSessionList);
		mListView.setAdapter(mAdapter);
	}

	/**
	 * 检测是否有新的朋友
	 */
	private void checkNewFriends(){
		if (!IMCommon.getNetWorkState()) {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					UserList userList = IMCommon.getIMServerAPI().getNewFriend(mContact.getPhoneString());
					if (userList!=null && userList.newFriendList!=null && userList.newFriendList.size()>0) {
						SharedPreferences sharePreferences = mParentContext.getSharedPreferences("LAST_TIME", 0);
						Editor editor = sharePreferences.edit();

						editor.putString("last_time", FeatureFunction.formartTime(System.currentTimeMillis()/1000, "yyyy-MM-dd HH:mm:ss"));
						editor.putInt("contact_count", mContact.getContactCount());
						editor.commit();
						mParentContext.sendBroadcast(new Intent(ContactsFragment.ACTION_SHOW_NEW_FRIENDS));
						mParentContext.sendBroadcast(new Intent(GlobalParam.ACTION_SHOW_CONTACT_NEW_TIP));
						IMCommon.saveContactTip(mParentContext, 1);
					}
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mHandler,GlobalParam.MSG_TIME_OUT_EXCEPTION,
							mParentContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}



	/**
	 * listview 子项点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		Login user = new Login();
		user.uid = mSessionList.get(position).getFromId();
		user.nickname = mSessionList.get(position).name;
		user.headSmall = mSessionList.get(position).heading;
		user.mIsRoom = mSessionList.get(position).type;
		Intent intent = new Intent(mParentContext, ChatMainActivity.class);
		intent.putExtra("data", user);
		startActivity(intent);
	}


	/**
	 * 显示提示框
	 * @param msg
	 */
	public void showProgressDialog(String msg){
		mProgressDialog = new ProgressDialog(mParentContext);
		mProgressDialog.setMessage(msg);
		mProgressDialog.show();
	}

	/**
	 * 隐藏提示框
	 */
	public void hideProgressDialog(){
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}



	/**
	 * 初始化listview 子项长按菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		if(mSessionList == null || mSessionList.size() == 0){
			return;
		}
		if (info.position > mSessionList.size()){
			return;
		}
		//，转发，收藏菜单
		if(mSessionList.get(info.position).isTop !=0 ){
			menu.add(Menu.NONE, 0, 0, mParentContext.getResources().getString(R.string.cancle_top_item));
		}else{
			menu.add(Menu.NONE, 0, 0, mParentContext.getResources().getString(R.string.set_top_item));
		}
		
		menu.add(Menu.NONE, 1, 1,mParentContext.getResources().getString(R.string.del_chat));
	}


	/**
	 * listview 子项长按菜单选择事件
	 */
	@Override 
	public boolean onContextItemSelected(MenuItem item) {  

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 

		int longClickItemIndex = info.position - mListView.getHeaderViewsCount();
		if(longClickItemIndex < mSessionList.size()){
			int menuItemIndex = item.getItemId();  
			Session session = mSessionList.get(longClickItemIndex);
			switch (menuItemIndex){
			case 0:
				if(session.isTop == 0){// 不置顶 -置顶
					
					SQLiteDatabase db = DBHelper.getInstance(mParentContext).getWritableDatabase();
					SessionTable sessionTable = new SessionTable(db);
					session.isTop =  sessionTable.getTopSize()+1;
					sessionTable.update(session, session.type);
					mSessionList.remove(longClickItemIndex);
					mSessionList.add(0, session);
					mAdapter.notifyDataSetChanged();
				}else{
					SQLiteDatabase db = DBHelper.getInstance(mParentContext).getWritableDatabase();
					SessionTable sessionTable = new SessionTable(db);
					List<Session> exitsSesList = sessionTable.getTopSessionList();
					if(exitsSesList!=null && exitsSesList.size()>0){
						for (int i = 0; i < exitsSesList.size(); i++) {
							Session ses = exitsSesList.get(i);
							if(ses.isTop>1){
								ses.isTop = ses.isTop-1;
								sessionTable.update(ses, ses.type);
							}
						}
					}
					session.isTop = 0;
					sessionTable.update(session, session.type);
					mSessionList.remove(longClickItemIndex);
					mAdapter.notifyDataSetChanged();
					initSession(false);
				}
				break;
			case 1:
				delete(longClickItemIndex);
				break;
			default:
				break;
			}
		}

		return true;  
	} 


	//onCreateContextMenu



}
