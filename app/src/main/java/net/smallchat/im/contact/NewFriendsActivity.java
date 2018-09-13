package net.smallchat.im.contact;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.NewFriendItem;
import net.smallchat.im.Entity.UserList;
import net.smallchat.im.R;
import net.smallchat.im.adapter.NewFriendAdapter;
import net.smallchat.im.fragment.ContactsFragment;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.SystemContactGlobal;
import net.smallchat.im.api.IMException;

/**
 * 新的朋友
 * @author dongli
 *
 */
public class NewFriendsActivity extends BaseActivity implements OnClickListener, OnItemClickListener{
	private LinearLayout mFootView,mSearchHeader;
	private ListView mListView;
	private TextView mHintText;
	private Dialog  mPhoneDialog;

	private List<NewFriendItem> mUserList = new ArrayList<NewFriendItem>();
	private NewFriendAdapter mAdapter;
	private SystemContactGlobal mSystemContactGlobal;


	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_GET_CONTACT_DATA:
				IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, 
						mContext.getResources().getString(R.string.get_dataing));
				break;
			case GlobalParam.MSG_SHOW_LISTVIEW_DATA:
				if(mSystemContactGlobal.getPhoneString()!=null && !mSystemContactGlobal.getPhoneString().equals("")){
					checkFriends();
				}else{
					if(IMCommon.getNewFriendItemResult(mContext)!=null 
							&& IMCommon.getNewFriendItemResult(mContext).size()>0){
						mUserList.addAll(IMCommon.getNewFriendItemResult(mContext));
						updateListView();
					}

					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
				break;
			case GlobalParam.MSG_SHOW_LOAD_DATA:
				updateListView();
				IMCommon.saveNewFriendsResult(mContext, mUserList,mSystemContactGlobal.getContactCount());
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:
				if (mAdapter!=null) {
					mAdapter.notifyDataSetChanged();
				}
				IMCommon.saveNewFriendsResult(mContext, mUserList,mSystemContactGlobal.getContactCount());
				break;
			case GlobalParam.MSG_VALID_FRIENDS://发送好友请求
				int validPosition = msg.arg1;
				if (validPosition <= mUserList.size()) {
					createDialog(mContext,
							mContext.getResources().getString(R.string.request_doing)
							,mUserList.get(validPosition).uid,mContext.getResources().getString(R.string.send));
					//mUserList.get(validPosition).uid
					//applyFriends();

				}
				break;
			case GlobalParam.MSG_AGREE_ADD_FRIENDS_REQUEST:
				int pos = msg.arg1;
				if (pos <= mUserList.size()) {
					agreeFriend(mUserList.get(pos).uid);

				}
				break;
			case GlobalParam.MSG_CHECK_STATE:
				IMResponseState returnStatus = (IMResponseState)msg.obj;
				if (returnStatus == null) {
					hideProgressDialog();
					Toast.makeText(mContext, "提交数据失败!",Toast.LENGTH_LONG).show();
					return;
				}
				if(returnStatus.code!=0){
					Toast.makeText(mContext, returnStatus.errorMsg,Toast.LENGTH_LONG).show();
					return;
				}
				String controlId= returnStatus.uid;

				changeNewFriendsState(controlId,returnStatus.changeType);
				//mHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_FRIENDS);
				break;
			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				int excuteType = msg.arg1;
				String prompt = (String) msg.obj;
				if(prompt != null && !prompt.equals("")){
					Toast.makeText(mContext, prompt, Toast.LENGTH_LONG).show();
				}else {
					if(excuteType == 1){
						Toast.makeText(mContext,mContext.getResources().getString(R.string.add_block_failed),Toast.LENGTH_LONG).show();
					}else if(excuteType == 2){
						Toast.makeText(mContext,mContext.getResources().getString(R.string.delete_friend_failed),Toast.LENGTH_LONG).show();
					}else if(excuteType == 3){
						Toast.makeText(mContext,mContext.getResources().getString(R.string.no_search_user),Toast.LENGTH_LONG).show();
					}
				}

				break;
			default:
				break;
			}
		}

	};

	/**
	 * 
	 * @param uid
	 * @param changeType  0-添加 1-已添加 2-等待验证
	 */
	private void changeNewFriendsState(String uid,int changeType){
		for (int i = 0; i < mUserList.size(); i++) {
			if(mUserList.get(i).uid.equals(uid)){
				mUserList.get(i).type = changeType;//2;//更改为等待验证
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
				}
				int count =mUserList.size();
				if(mSystemContactGlobal!=null && mSystemContactGlobal.getContactCount()!=0){
					count = mSystemContactGlobal.getContactCount();
				}
				IMCommon.saveNewFriendsResult(mContext, mUserList,count);
				break;
			}
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		mContext = this;
		IMCommon.saveContactTip(mContext, 0);
		mContext.sendBroadcast(new Intent(ContactsFragment.ACTION_HIDE_NEW_FRIENDS));
		
		mContext.sendBroadcast(new Intent(GlobalParam.ACTION_HIDE_CONTACT_NEW_TIP));
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_REFRESH_NEW_FRIENDS);
		registerReceiver(mReceiver, filter);
		initCompent();
	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if(intent.getAction().equals(GlobalParam.ACTION_REFRESH_NEW_FRIENDS)){
					if(mUserList!=null && mUserList.size()>0){
						mUserList.clear();
					}
					if(IMCommon.getNewFriendItemResult(mContext)!=null
							&& IMCommon.getNewFriendItemResult(mContext).size()>0){
						mUserList.addAll(IMCommon.getNewFriendItemResult(mContext));
					}
					if(mAdapter!=null){
						mAdapter.notifyDataSetChanged();
					}else{
						updateListView();
					}
				}
			}
		}
	};



	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}


	private void initCompent(){
		setTitleContent(R.drawable.back_btn, R.drawable.add_contact_btn, R.string.new_friends);
		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);
		LinearLayout.LayoutParams addBtnParams = new LinearLayout.LayoutParams(
				FeatureFunction.dip2px(mContext,52),FeatureFunction.dip2px(mContext, 55));
		addBtnParams.gravity = Gravity.CENTER_VERTICAL;
		mRightBtn.setLayoutParams(addBtnParams);

		mListView = (ListView)findViewById(R.id.result_list);
		mListView.setDivider(null);
		mListView.setCacheColorHint(0);
		mListView.setOnCreateContextMenuListener(this);

		mListView.setOnItemClickListener(this);

		mSystemContactGlobal = new SystemContactGlobal(mContext,mHandler);
	}

	private void checkFriends(){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					UserList userList = IMCommon.getIMServerAPI().getNewFriend(mSystemContactGlobal.getPhoneString());
					if (mUserList!=null && mUserList.size()>0) {
						mUserList.clear();
					}
					if (userList!=null && userList.newFriendList!=null && userList.newFriendList.size()>0) {
						mUserList.addAll(userList.newFriendList);//获取到新的朋友
						//获取系统中保存的新的朋友
						List<NewFriendItem> lastNewFriendsList = IMCommon.getNewFriendItemResult(mContext);

						//获取手机联系人列表
						List<Login> contactList = mSystemContactGlobal.getmUserList();
						boolean isExitsLastData = false;
						for (int i = 0; i < mUserList.size(); i++) {
							String currentUid = mUserList.get(i).uid;
							String currentPhone = mUserList.get(i).phone;
							if(lastNewFriendsList!=null && lastNewFriendsList.size()>0){
								isExitsLastData = true;
								for (int j = 0; j < lastNewFriendsList.size(); j++) {
									lastNewFriendsList.get(j).colorBgtype = 0;
									if(null!=lastNewFriendsList.get(j)) {

										if (currentUid.equals(lastNewFriendsList.get(j).uid)) {
											mUserList.get(i).colorBgtype = 0;
											if (lastNewFriendsList.get(j).type != 0) {
												mUserList.get(i).type = lastNewFriendsList.get(j).type;
											}
											break;
										}

									}
								}
							}

							//取得手机联系人的姓名
							if(contactList!=null && contactList.size()>0){
								for (int k = 0; k < contactList.size(); k++) {
									if(currentPhone.equals(contactList.get(k).phone)){
										mUserList.get(i).contactName = contactList.get(k).nickname;
										break;
									}
								}
							}


						}
						if(isExitsLastData){
							for (int l = 0; l < lastNewFriendsList.size(); l++) {
								boolean isExits = true;
								for (int m = 0; m < mUserList.size(); m++) {
									if(mUserList.get(m).uid.equals(lastNewFriendsList.get(l).uid)){
										isExits= false;
									}
									if(m == mUserList.size() -1 ){
										if(isExits){
											mUserList.add(lastNewFriendsList.get(l));
											break;
										}
									}
								}
							}
						}
						List<NewFriendItem> newList = new ArrayList<NewFriendItem>();
						List<NewFriendItem> oldList = new ArrayList<NewFriendItem>();
						for (int i = 0; i < mUserList.size(); i++) {
							if (mUserList.get(i).colorBgtype == 1) {
								newList.add(mUserList.get(i));
							}else if(mUserList.get(i).colorBgtype == 0){
								oldList.add(mUserList.get(i));
							}
						}
						if(mUserList!=null && mUserList.size()>0){
							mUserList.clear();
						}
						mUserList.addAll(newList);
						mUserList.addAll(oldList);
						mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_LOAD_DATA);

					}else{
						mUserList.addAll(IMCommon.getNewFriendItemResult(mContext));
						mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_LOAD_DATA);
					}
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mHandler,GlobalParam.MSG_TIME_OUT_EXCEPTION,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}


	private void updateListView(){
		mAdapter = new NewFriendAdapter(mContext,mUserList,mHandler);
	
		mListView.setAdapter(mAdapter); 

	}


	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			NewFriendsActivity.this.finish();
			break;
		case R.id.right_btn:
			Intent intent = new Intent();
			intent.setClass(mContext, AddActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		//arg2 = arg2-1;
		if (0<=arg2 && arg2<mUserList.size()) {
			Intent intent = new Intent();
			intent.setClass(mContext, UserInfoActivity.class);
			intent.putExtra("type",2);
			intent.putExtra("uid",mUserList.get(arg2).uid);
			startActivityForResult(intent, 1);
		}
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1 && resultCode == 2){
			if(data == null){
				return;
			}
			String uid = data.getStringExtra("uid");
			int changeType = data.getIntExtra("changeType",0);
			changeNewFriendsState(uid,changeType);
		}
	}

	/**
	 * 发送添加好友请求
	 */
	private void applyFriends(final String uid,final String reason){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,"正在发送请求，请稍后...");
					IMResponseState status = IMCommon.getIMServerAPI().applyFriends(
							IMCommon.getUserId(mContext),uid,reason);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					if(status !=null){
						status.uid = uid;
						status.changeType = 2;
					}
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,status);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, GlobalParam.MSG_TIME_OUT_EXCEPTION,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	/**
	 * 同意好友请求
	 *  changeType 0-添加 1-已添加 2-等待验证
	 * @param pos
	 */
	private void agreeFriend(final String uid){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().agreeFriends(uid);
						if(state != null && state.code == 0){
							mContext.sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
							if(state !=null){
								state.uid = uid;
								state.changeType = 1;
							}
							IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,state);
						}else {
							IMCommon.sendMsg(mHandler ,GlobalParam.MSG_LOAD_ERROR,state.errorMsg,1);
						}
					} catch (IMException e) {
						e.printStackTrace();
						mHandler.sendEmptyMessage(GlobalParam.MSG_TIME_OUT_EXCEPTION);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
	}

	
	/**
	 * 
	 * @param context
	 * @param cardTitle
	 * @param okTitle
	 */
	protected void createDialog(Context context, String cardTitle,final String uid,final String okTitle) {
		mPhoneDialog = new Dialog (context,R.style.dialog);
		LayoutInflater factor = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View serviceView = factor.inflate(R.layout.card_dialog, null);

		mPhoneDialog.setContentView(serviceView);
		mPhoneDialog.show();
		mPhoneDialog.setCancelable(false);	
		mPhoneDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT
				, LayoutParams.WRAP_CONTENT);

	
		final TextView phoneEdit=(TextView)serviceView.findViewById(R.id.card_title);
		phoneEdit.setText(cardTitle);
		final EditText reasonEdit = (EditText)serviceView.findViewById(R.id.reason_edit);
		reasonEdit.setVisibility(View.VISIBLE);

		Button okBtn=(Button)serviceView.findViewById(R.id.yes);
		okBtn.setText(okTitle);


		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String reasonString = reasonEdit.getText().toString();
				if(reasonString!=null && !reasonString.equals("")){
					if(reasonString.length()>15){
						Toast.makeText(mContext, "申请信息长度在１５个字以内", Toast.LENGTH_LONG).show();
						return;
					}
					if (mPhoneDialog!=null) {
						mPhoneDialog.dismiss();
						mPhoneDialog=null;
					}
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "请求发送中,请稍后...");
					applyFriends(uid,reasonString);
				}else{
					if (mPhoneDialog!=null) {
						mPhoneDialog.dismiss();
						mPhoneDialog=null;
					}
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "请求发送中,请稍后...");
					applyFriends(uid,reasonString);
					//Toast.makeText(mContext,R.string.please_input_request_reason_hint, Toast.LENGTH_LONG).show();
				}
			}
		});

		Button Cancel = (Button)serviceView.findViewById(R.id.no);
		Cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPhoneDialog!=null) {
					mPhoneDialog.dismiss();
					mPhoneDialog = null;
				}
			}
		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		if(mUserList == null || mUserList.size() == 0){
			return;
		}
		if (info.position > mUserList.size()){
			return;
		}
		menu.add(Menu.NONE,0, 0,mContext.getResources().getString(R.string.del));
	}


	@Override 
	public boolean onContextItemSelected(MenuItem item) {  

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 

		int longClickItemIndex = info.position - mListView.getHeaderViewsCount();
		if(longClickItemIndex < mUserList.size()){
			int menuItemIndex = item.getItemId();  
			switch (menuItemIndex){
			case 0:
				mUserList.remove(longClickItemIndex);
				if(mAdapter!=null){
					mAdapter.notifyDataSetChanged();
				}
				IMCommon.saveNewFriendsResult(mContext, mUserList, mUserList.size());
				break;
		
			default:
				break;
			}
		}

		return true;  
	} 

}
