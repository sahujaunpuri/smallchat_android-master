package net.smallchat.im.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.GroupTable;
import net.smallchat.im.Entity.Group;
import net.smallchat.im.Entity.GroupList;
import net.smallchat.im.Entity.Login;

import net.smallchat.im.R;

import net.smallchat.im.components.sortlist.CharacterParser;
import net.smallchat.im.components.sortlist.PinYin;
import net.smallchat.im.components.sortlist.PinyinComparator;
import net.smallchat.im.components.sortlist.SideBar;
import net.smallchat.im.components.sortlist.SortAdapter;
import net.smallchat.im.contact.NewFriendsActivity;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.mine.MyGroupListActivity;
import net.smallchat.im.api.IMException;

import net.smallchat.im.widget.MyPullToRefreshListView;
import net.smallchat.im.widget.MyPullToRefreshListView.OnChangeStateListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 通讯录Fragment的界面
 *
 * @author dl
 */
public class ContactsFragment extends Fragment implements OnChangeStateListener {

	/**
	 * 定义全局变量
	 */

	private boolean mIsRegisterReceiver = false;
	//显示新的朋友有上角的泡泡
	public final static String ACTION_SHOW_NEW_FRIENDS = "im_action_show_new_friends_tip";
	//取消新的朋友有上角的泡泡
	public final static String ACTION_HIDE_NEW_FRIENDS = "im_action_hide_new_friends_tip";
	public final static String REFRESH_FRIEND_ACTION = "im_refresh_action";

	private MyPullToRefreshListView mContainer;
	private TextView mRefreshViewLastUpdated;
	private LinearLayout mCategoryLinear;
	private boolean mIsRefreshing = false;

	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter mAdapter;


	private static boolean mInit;
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	public  List<Login> mSourceDateList=new ArrayList<Login>();

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	private View mView;
	private Context mParentContext;
	private ProgressDialog mProgressDialog;

	private List<Group> mGroupList = new ArrayList<Group>();
	private GroupList mGroup;

	private String mNewFriendsString,mChatString;

	/**
	 * 加载控件
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		mView =inflater.inflate(R.layout.activity_contact_main, container, false);

		return mView;

	}

	/**
	 * 实例化控件
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerBoardCast();
		mNewFriendsString = mParentContext.getResources().getString(R.string.new_friends_menu);
		mChatString= mParentContext.getResources().getString(R.string.group_chat_menu);
		sideBar = (SideBar)mView.findViewById(R.id.sidrbar);
		dialog = (TextView)mView.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		//设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				int position = mAdapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					sortListView.setSelection(position);
				}
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						sideBar.setBackgroundColor(Color.parseColor("#00000000"));
						sideBar.setChoose(-1);
						sideBar.invalidate();
						dialog.setVisibility(View.INVISIBLE);
					}
				}, 2000);
			}
		});

		mCategoryLinear = (LinearLayout)mView.findViewById(R.id.category_linear);
		mRefreshViewLastUpdated = (TextView) mView.findViewById(R.id.pull_to_refresh_time);
		mContainer = (MyPullToRefreshListView) mView.findViewById(R.id.container);
		sortListView = mContainer.getList();
		sortListView.setDivider(null);
		sortListView.setCacheColorHint(0);
		sortListView.setHeaderDividersEnabled(false);

		sortListView.setOnItemClickListener(new OnItemClickListener() {

			/**
			 * listview 子项点击事件
			 */
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Login login = (Login) mAdapter.getItem(position);
				if(login.remark!=null && !login.remark.equals("")){
					if(login.remark.equals(mParentContext.getResources().getString(R.string.new_friends))){
						//进入新的朋友页面
						Intent newFriendIntent = new Intent();
						newFriendIntent.setClass(mParentContext, NewFriendsActivity.class);
						startActivity(newFriendIntent);
					}else if(login.remark.equals(mParentContext.getResources().getString(R.string.group_chat))){
						//进入我的群组页面
						Intent groupListIntent = new Intent();
						groupListIntent.setClass(mParentContext, MyGroupListActivity.class);
						startActivity(groupListIntent);
					}else{//进入好友资料页面
						Intent userInfoIntent = new Intent();
						userInfoIntent.setClass(mParentContext, UserInfoActivity.class);
						userInfoIntent.putExtra("type",2);
						userInfoIntent.putExtra("uid", login.uid);
						startActivity(userInfoIntent);
					}
				}else{//跳转到用户信息页面
					Intent userInfoIntent = new Intent();
					userInfoIntent.setClass(mParentContext, UserInfoActivity.class);
					userInfoIntent.putExtra("type",2);
					userInfoIntent.putExtra("uid", login.uid);
					startActivity(userInfoIntent);
				}

			}
		});
		sortListView.setSelector(mParentContext.getResources().getDrawable(R.drawable.transparent_selector));
		mContainer.setOnChangeStateListener(this);
		if(mSourceDateList!=null && mSourceDateList.size()>0){
			mSourceDateList.clear();
		}
		/*if(mSourceDateList!=null && mSourceDateList.size()>0){
			updateListView();
		}else{*/
		getData();
		/*}*/

	}


	/**
	 * 注册通知
	 */
	private void registerBoardCast(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.SWITCH_LANGUAGE_ACTION);
		filter.addAction(REFRESH_FRIEND_ACTION);
		filter.addAction(GlobalParam.REFRESH_ALIAS_ACTION);

		filter.addAction(GlobalParam.ACTION_CANCLE_NEW_ORDER);
		filter.addAction(GlobalParam.ACTION_CANCLE_NEW_SERVICE);
		filter.addAction(ACTION_SHOW_NEW_FRIENDS);
		filter.addAction(ACTION_HIDE_NEW_FRIENDS);
		mParentContext.registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;
	}

	/**
	 * 处理通知
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.e("ContactsFragment", context+"");
			if (intent != null) {
				if (intent.getAction().equals(
						GlobalParam.SWITCH_LANGUAGE_ACTION)) {//刷新通讯录列表
					if(mSourceDateList!=null && mSourceDateList.size()>0){
						mSourceDateList.clear();
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}
					}
					getUserList(GlobalParam.LIST_LOAD_FIRST);
				} else if (intent.getAction().equals(REFRESH_FRIEND_ACTION)) {//刷新通讯录列表
					getUserList(GlobalParam.LIST_LOAD_FIRST);
				}else if(intent.getAction().equals(GlobalParam.REFRESH_ALIAS_ACTION)){//刷新通讯录列表
					if(mSourceDateList.size() != 0){
						mSourceDateList.clear();
					}
					SQLiteDatabase db = DBHelper.getInstance(mParentContext).getReadableDatabase();
					GroupTable table = new GroupTable(db);
					mGroupList = table.query();
					if(mGroupList != null && mGroupList.size() != 0){
						List<Login> tempList = new ArrayList<Login>();
						tempList.add(new Login("↑","",mNewFriendsString,mNewFriendsString,1,IMCommon.getContactTip(mParentContext)));
						tempList.add(new Login("↑","",mChatString,mChatString,1));
						for (int i = 0; i < mGroupList.size(); i++) {
							if(mGroupList.get(i).mUserList != null && mGroupList.get(i).mUserList.size()>0){
								tempList.addAll(mGroupList.get(i).mUserList);
							}
						}

						updateListView();
					}
				}else if(intent.getAction().equals(ACTION_SHOW_NEW_FRIENDS)){//显示因的朋友
					if(mSourceDateList!=null && mSourceDateList.size()>0){
						mSourceDateList.get(0).newFriends = 1;
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}

					}


				}else if(intent.getAction().equals(ACTION_HIDE_NEW_FRIENDS)){//隐藏新的朋友
					if(mSourceDateList!=null && mSourceDateList.size()>0){
						mSourceDateList.get(0).newFriends =0 /*IMCommon.getContactTip(mParentContext)*/;
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}

					}

				}
			}
		}
	};



	/** Fragment第一次附属于Activity时调用,在onCreate之前调用 */

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mInit = true;

		//Log.e("ContactsFragment-onAttach", "onAttach_insert+++");
	}

	/**
	 * fragemnt 创建事件
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setHasOptionsMenu(true); 
		mParentContext = (Context)ContactsFragment.this.getActivity();
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();
		//Log.e("ContactsFragment-onCreate", "onCreate_insert+++");
	}

	@Override
	public void onResume() {
		super.onResume();
		//Log.e("ContactsFragment-onResume", "onResume_insert+++");


	}


	/**
	 * 获取通讯录数据
	 */
	private void getData(){

		SQLiteDatabase db = DBHelper.getInstance(mParentContext).getReadableDatabase();
		GroupTable table = new GroupTable(db);
		mGroupList = table.query();
		if(mGroupList != null && mGroupList.size() != 0){
			List<Login> tempList = new ArrayList<Login>();
			tempList.add(new Login("↑","",mNewFriendsString,mNewFriendsString,1,IMCommon.getContactTip(mParentContext)));
			tempList.add(new Login("↑","",mChatString,mChatString,1));

			for (int i = 0; i < mGroupList.size(); i++) {
				if(mGroupList.get(i).mStarList!=null
						&& mGroupList.get(i).mStarList.size()>0){
					tempList.addAll(mGroupList.get(i).mStarList);
				}
			}
			for (int i = 0; i < mGroupList.size(); i++) {
				if(mGroupList.get(i).mUserList != null && mGroupList.get(i).mUserList.size()>0){
					tempList.addAll(mGroupList.get(i).mUserList);
				}
			}
			mSourceDateList.addAll(tempList);
			updateListView();
		}else {
			mGroupList = new ArrayList<Group>();
			Message message = new Message();
			message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
			message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
			mHandler.sendMessage(message);
			getUserList(GlobalParam.LIST_LOAD_FIRST);
		}
	}


	/**
	 * 获取通讯录人员列表
	 * @param loadType
	 */
	private void getUserList(final int loadType) {
		new Thread() {

			@Override
			public void run() {
				if (IMCommon.verifyNetwork(mParentContext)) {
					new Thread() {
						public void run() {
							try {

								mGroup = IMCommon.getIMServerAPI().getUserList();

								if (mGroup != null) {
									if (mGroup.mState != null && mGroup.mState.code == 0) {

										if (loadType != GlobalParam.LIST_LOAD_MORE) {
											if (mGroupList != null) {
												mGroupList.clear();
											}
										}

										List<Login> tempList = new ArrayList<Login>();
										tempList.add(new Login("↑","",mNewFriendsString,mNewFriendsString,1,IMCommon.getContactTip(mParentContext)));
										tempList.add(new Login("↑","",mChatString,mChatString,1));
										if (mGroup.mGroupList != null) {
											mGroupList.addAll(mGroup.mGroupList);
											SQLiteDatabase db = DBHelper.getInstance(mParentContext).getWritableDatabase();
											GroupTable table = new GroupTable(db);
											table.insert(mGroup.mGroupList);

											for (int i = 0; i < mGroup.mGroupList.size(); i++) {
												if(mGroupList.get(i).mStarList!=null
														&& mGroupList.get(i).mStarList.size()>0){
													tempList.addAll(mGroupList.get(i).mStarList);
												}
											}
											for (int j = 0; j < mGroup.mGroupList.size(); j++) {
												if(mGroupList.get(j).mUserList != null){
													tempList.addAll(mGroupList.get(j).mUserList);
												}
											}
										}

										IMCommon.sendMsg(mHandler, GlobalParam.MSG_CLEAR_LISTENER_DATA,tempList);
									} else {
										Message msg = new Message();
										msg.what = GlobalParam.MSG_LOAD_ERROR;
										if (mGroup.mState != null && mGroup.mState.errorMsg != null && !mGroup.mState.errorMsg.equals("")) {
											msg.obj = mGroup.mState.errorMsg;
										} else {
											msg.obj = ChatApplication.getInstance().getResources().getString(R.string.load_error);
										}
										mHandler.sendMessage(msg);
									}
								} else {
									mHandler.sendEmptyMessage(GlobalParam.MSG_LOAD_ERROR);
								}

							} catch (IMException e) {
								e.printStackTrace();
								Message msg = new Message();
								msg.what = GlobalParam.MSG_TIME_OUT_EXCEPTION;
								msg.obj = ChatApplication.getInstance().getResources().getString(R.string.timeout);
								mHandler.sendMessage(msg);
							}

							switch (loadType) {
								case GlobalParam.LIST_LOAD_FIRST:
									mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
									break;
								case GlobalParam.LIST_LOAD_MORE:
									mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);

								case GlobalParam.LIST_LOAD_REFERSH:
									mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
									break;

								default:
									break;
							}
						}
					}.start();
				} else {
					switch (loadType) {
						case GlobalParam.LIST_LOAD_FIRST:
							mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
							break;
						case GlobalParam.LIST_LOAD_MORE:
							mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);

						case GlobalParam.LIST_LOAD_REFERSH:
							mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
							break;

						default:
							break;
					}
					mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
				}
			}

		}.start();
	}

	private void updateListView(){
		filledData();
		//根据a-z排序
		Collections.sort(mSourceDateList, pinyinComparator);
		/*	if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}else{*/
		mAdapter = new SortAdapter(getActivity(), mSourceDateList);
		sortListView.setAdapter(mAdapter);
		/*}*/


	}

	/**
	 * 显示通讯录数据
	 */
	private void refreshUpdateListView(){
		filledData();
		//根据a-z排序
		Collections.sort(mSourceDateList, pinyinComparator);
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}else{
			mAdapter = new SortAdapter(getActivity(), mSourceDateList);
			sortListView.setAdapter(mAdapter);
		}


	}


	/**
	 * 为ListView填充数据
	 */
	private void filledData(){

		try {
			for(int i=0; i<mSourceDateList.size(); i++){
				String name="";
				if(mSourceDateList.get(i).nameType == 1  ){//0-普通用户 1-操作栏 2-星标朋友
					name = mSourceDateList.get(i).nickname;
				}else{
					name = mSourceDateList.get(i).remark;
				}
				if(name == null || name.equals("")){
					name = mSourceDateList.get(i).nickname;
				}


				//汉字转换成拼音
				/*String pinyin;
				pinyin = characterParser.getSelling(name);
				String sortString = pinyin.substring(0, 1).toUpperCase();*/
				String sortString  = mSourceDateList.get(i).sort;
				String sName = mSourceDateList.get(i).sortName;
				if(sName!=null && !sName.equals("")){
					if(sName.equals("星标朋友")){

					}else{

					}
				}else{
					if(sortString.matches("↑")){
						mSourceDateList.get(i).sort = "↑";
						mSourceDateList.get(i).sortName = "";
						mSourceDateList.get(i).remark = name.substring(1,name.length());
					}
					else if(sortString.matches("[A-Z]") || sortString.matches("[a-z]")){
						String sort = PinYin.getPingYin(name.trim());
						if(sort==null || sort.length()<=0){
							sort = "#";
						}else{
							sort = sort.substring(0, 1).toUpperCase();
						}
						mSourceDateList.get(i).sort = sort;
						mSourceDateList.get(i).sortName = sort;
					}else{
						mSourceDateList.get(i).sortName = "#";
						mSourceDateList.get(i).sort = "#";
					}
				}


			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}




	/**
	 * 下拉刷新通讯录数据
	 */
	@Override
	public void onChangeState(MyPullToRefreshListView container, int state) {
		mRefreshViewLastUpdated.setText(FeatureFunction.getRefreshTime());
		switch (state) {
			case MyPullToRefreshListView.STATE_LOADING:
				mHandler.sendEmptyMessage(GlobalParam.SHOW_SCROLLREFRESH);
				break;
		}
	}


	/**
	 * 处理消息
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

				case GlobalParam.SHOW_PROGRESS_DIALOG:
					String dialogMsg = (String) msg.obj;
					showProgressDialog(dialogMsg);
					break;
				case GlobalParam.HIDE_PROGRESS_DIALOG:
					hideProgressDialog();
					updateListView();
					break;

				case GlobalParam.SHOW_SCROLLREFRESH:
					if (mIsRefreshing) {
						mContainer.onRefreshComplete();
						break;
					}
					mIsRefreshing = true;
					getUserList(GlobalParam.LIST_LOAD_REFERSH);
					break;

				case GlobalParam.HIDE_SCROLLREFRESH:
					mIsRefreshing = false;
					mContainer.onRefreshComplete();
					//updateListView();
					refreshUpdateListView();
					break;
				case GlobalParam.MSG_CLEAR_LISTENER_DATA:

					if(mSourceDateList != null && mSourceDateList.size()>0){
						mSourceDateList.clear();
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}
					}

					List<Login> tempList = (List<Login>)msg.obj;
					if(tempList!=null && tempList.size()>0){
						mSourceDateList.addAll(tempList);
					}
					break;

				case GlobalParam.MSG_LOAD_ERROR:
					String error_Detail = (String) msg.obj;
					if (error_Detail != null && !error_Detail.equals("")) {
						Toast.makeText(mParentContext, error_Detail, Toast.LENGTH_LONG)
								.show();
					} else {
						Toast.makeText(mParentContext, R.string.load_error,
								Toast.LENGTH_LONG).show();
					}
					break;
				case GlobalParam.MSG_NETWORK_ERROR:
					Toast.makeText(mParentContext, R.string.network_error,
							Toast.LENGTH_LONG).show();
					break;
				case GlobalParam.MSG_TIME_OUT_EXCEPTION:

					String message = (String) msg.obj;
					if (message == null || message.equals("")) {
						message = ChatApplication.getInstance().getResources().getString(R.string.timeout);
					}
					Toast.makeText(mParentContext, message, Toast.LENGTH_LONG).show();
					break;

				default:
					break;

			}
		}
	};



	/**
	 * 显示提示对话框
	 * @param msg
	 */
	public void showProgressDialog(String msg){
		mProgressDialog = new ProgressDialog(mParentContext);
		mProgressDialog.setMessage(msg);
		mProgressDialog.show();
	}

	/**
	 * 隐藏提示对话框
	 */
	public void hideProgressDialog(){
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	/**
	 * 销毁页面
	 */
	@Override
	public void onDestroy() {
		if (mIsRegisterReceiver) {
			mParentContext.unregisterReceiver(mReceiver);
		}
		freeBitmapCaches();
		super.onDestroy();
	}

	/**
	 * 释放头像
	 */
	private void freeBitmapCaches(){
		if(mSourceDateList == null || mSourceDateList.size()>0){

		}
		for(int i = 0; i < mSourceDateList.size(); i++){
			if(mSourceDateList.get(i).headSmall != null && !mSourceDateList.get(i).headSmall.equals("")){

				if(mAdapter != null && mAdapter.getImageBuffer() != null){

					ImageView imageView = (ImageView) sortListView.findViewWithTag(mSourceDateList.get(i).headSmall);
					if(imageView != null){
						imageView.setImageBitmap(null);
						imageView.setImageResource(R.drawable.contact_default_header);
					}

					Bitmap bitmap = mAdapter.getImageBuffer().get(mSourceDateList.get(i).headSmall);
					if (bitmap != null && !bitmap.isRecycled()) {
						bitmap.recycle();
						bitmap = null;
						mAdapter.getImageBuffer().remove(mSourceDateList.get(i).headSmall);
					}

				}

			}
		}

	}







}
