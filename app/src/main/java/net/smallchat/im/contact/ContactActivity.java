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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.CheckFriends;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.R;
import net.smallchat.im.adapter.SearchResultAdapter;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.SystemContactGlobal;
import net.smallchat.im.api.IMException;

/**
 * 手机联系人
 * @author dongli
 *
 */
public class ContactActivity extends BaseActivity implements OnClickListener, OnItemClickListener{
	
	/*
	 * 定义全局变量
	 */
	public final static String REFRESH_SYSTEM_CONTACT_ACTION = "im_refresh_system_contact_action";
	
	private LinearLayout mFootView,mSearchHeader;
	private ListView mListView;
	private TextView mHintText;
	private EditText mSearchContent;
	private RelativeLayout mSearchLayout;
	private Dialog  mPhoneDialog;

	private List<Login> mUserList = new ArrayList<Login>();
	private List<Login> mList = new ArrayList<Login>();
	private List<Login> mSearchList = new ArrayList<Login>();
	private SearchResultAdapter mAdapter;

	private SystemContactGlobal mSystemContactGlobal;


	/*
	 * 处理消息
	 */
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
				if(mSystemContactGlobal.getmUserList()!=null && mSystemContactGlobal.getmUserList().size()>0){
					mUserList.addAll(mSystemContactGlobal.getmUserList());
					if(mList!=null && mList.size()>0){
						mList.clear();
					}
					mList.addAll(mUserList);
					updateListView();
					mHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_FRIENDS);
				}else{
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}

				break;
			case GlobalParam.MSG_CHECK_FRIENDS:
				checkFriends();
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:
				if (mAdapter!=null) {
					mAdapter.setData(mUserList);
					mAdapter.notifyDataSetChanged();
				}
				mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				break;
			case GlobalParam.MSG_CANCLE_FRIENDS://删除好友
				int canclePosition = msg.arg1;
				if (canclePosition <= mUserList.size()) {
					cancleFriends(mUserList.get(canclePosition).uid);
				}

				break;
			case GlobalParam.MSG_VALID_FRIENDS://发送好友请求
				int validPosition = msg.arg1;
				if (validPosition <= mUserList.size()) {
					createDialog(mContext,
							mContext.getResources().getString(R.string.request_doing)
							,mList.get(validPosition).uid,mContext.getResources().getString(R.string.send));
				}
				break;
			case GlobalParam.MSG_CHECK_STATE:
				IMResponseState returnStatus = (IMResponseState)msg.obj;
				if (returnStatus == null) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.commit_data_error),Toast.LENGTH_LONG).show();
					return;
				}
				if(returnStatus.code!=0){
					Toast.makeText(mContext, returnStatus.errorMsg,Toast.LENGTH_LONG).show();
					return;
				}
				mHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_FRIENDS);
				break;
			case GlobalParam.MSG_CLICK_LISTENER:
				int position = msg.arg1;
				if(position!=-1){
					Login  login = mList.get(position);
					switch (login.isAccount) {
					case 0://没有账号 - 发送短信邀请
						Intent inviteIntent = new Intent();
						inviteIntent.setClass(mContext, InviteActivity.class);
						inviteIntent.putExtra("entity", login);
						mContext.startActivity(inviteIntent);
						break;
					case 1://有账号
						Intent intent = new Intent();
						intent.setClass(mContext, UserInfoActivity.class);
						intent.putExtra("type",2);
						intent.putExtra("uid",login.uid);
						startActivity(intent);
						break;
					default:
						break;
					}
				}
				break;
			default:
				break;
			}
		}

	};
	
	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		mContext = this;
		IntentFilter filter = new IntentFilter();
		filter.addAction(REFRESH_SYSTEM_CONTACT_ACTION);
		registerReceiver(refreshBrodCast, filter);

		initCompent();
	}

	/*
	 * 处理消息
	 */
	BroadcastReceiver refreshBrodCast = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent!=null){
				String action = intent.getAction();
				if(action.equals(REFRESH_SYSTEM_CONTACT_ACTION)){
					int pos = intent.getIntExtra("pos",-1);
					int status = intent.getIntExtra("status",-1);
					if(pos!=-1 && status !=-1&& (mList!=null && mList.size()>0)){
						mList.get(pos).isfriend = status;
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}
					}
				}
			}
		}
	};
	
	/*
	 * 示例化控件
	 */
	private void initCompent(){


		setTitleContent(R.drawable.back_btn, 0, R.string.add_friend);
		mLeftBtn.setOnClickListener(this);

		mSearchLayout = (RelativeLayout)findViewById(R.id.searchlayout);
		mSearchLayout.setVisibility(View.VISIBLE);

		mSearchContent = (EditText) findViewById(R.id.searchcontent);
		mSearchContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString() != null && !s.toString().equals("")){

					if(mSearchList != null){
						mSearchList.clear();
					}

					for (int i = 0; i < mUserList.size(); i++) {
						if(mUserList.get(i).nickname.contains(s.toString())){
							mSearchList.add(mUserList.get(i));
						}
					}

					if(mList != null){
						mList.clear();
					}
					if(mSearchList != null){
						mList.addAll(mSearchList);
					}
					if (mAdapter!=null) {
						mAdapter.setData(mSearchList);
						mAdapter.notifyDataSetChanged();
					}


				}else {

					if(mList != null){
						mList.clear();
					}

					if(mUserList != null){
						mList.addAll(mUserList);
					}

					if (mAdapter!=null) {
						mAdapter.setData(mUserList);
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		});

		mListView = (ListView)findViewById(R.id.result_list);
		mListView.setDivider(null);
		mListView.setCacheColorHint(0);
		mListView.setOnItemClickListener(this);
		mSystemContactGlobal = new SystemContactGlobal(mContext,mHandler);

	}

	/*
	 * 导入手机通讯录
	 */
	private void checkFriends(){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					CheckFriends userList = IMCommon.getIMServerAPI().getContactUserList(mSystemContactGlobal.getPhoneString());
					if (userList!=null && userList.childList!=null && userList.childList.size()>0) {
						if (mUserList!=null && mUserList.size()>0) {
							for (int i = 0; i < mUserList.size(); i++) {
								String phone = mUserList.get(i).phone;
								for (int j = 0; j < userList.childList.size(); j++) {
									if(userList.childList.get(j).phone.equals(phone)){
										mUserList.get(i).isfriend = userList.childList.get(j).isfriend;
										if(userList.childList.get(j).userID!=null && !userList.childList.get(j).userID.equals("")){
											mUserList.get(i).uid = userList.childList.get(j).userID;
										}
										mUserList.get(i).isAccount = userList.childList.get(j).type;
										break;
									}
								}
							}
						}
						mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
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
		mAdapter = new SearchResultAdapter(mContext,mList,true,mHandler,false);
	
		mListView.setAdapter(mAdapter); 

	}



	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			ContactActivity.this.finish();
			break;
		default:
			break;
		}
	}

	/*
	 * 子项点击
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		//arg2 = arg2-1;
		if (0<=arg2 && arg2<mList.size()) {
			Login item = mList.get(arg2);
			switch (item.isAccount) {
			case 0://没有账号 - 发送短信邀请
				Intent inviteIntent = new Intent();
				inviteIntent.setClass(mContext, InviteActivity.class);
				inviteIntent.putExtra("entity", item);
				mContext.startActivity(inviteIntent);
				break;
			case 1://有账号
				Intent intent = new Intent();
				intent.setClass(mContext, UserInfoActivity.class);
				intent.putExtra("type",2);
				intent.putExtra("pos", arg2);
				intent.putExtra("uid",mUserList.get(arg2).uid);
				startActivity(intent);

				break;
			default:
				break;
			}
		}
	}

	/**
	 * 发送添加好友请求
	 */
	private void applyFriends(final String uid,final String reason){
		if (!IMCommon.getNetWorkState()) {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mHandler, GlobalParam.SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.send_request));
					IMResponseState status = IMCommon.getIMServerAPI().applyFriends(
							IMCommon.getUserId(mContext),uid,reason);
					mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,status);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_TIME_OUT_EXCEPTION,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	/**
	 * 取消好友关系
	 */
	private void cancleFriends(final String uid){
		if (!IMCommon.getNetWorkState()) {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mHandler, GlobalParam.SHOW_PROGRESS_DIALOG,"正在发送请求，请稍后...");
					IMResponseState status = IMCommon.getIMServerAPI().cancleFriends(uid);
					mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,status);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_TIME_OUT_EXCEPTION,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void notifyChanged(){
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
	}


	/**
	 * 创建对话框
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
				/*mContext.getResources().getDimensionPixelSize(R.dimen.bind_phone_height)*/
				, LayoutParams.WRAP_CONTENT);

		/*
		TextView signId=(TextView) serviceView
				.findViewById(R.tid.sign_id);
		signId.setText(string[0]);*/
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
					if (mPhoneDialog!=null) {
						mPhoneDialog.dismiss();
						mPhoneDialog=null;
					}
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "请求发送中,请稍后...");
					applyFriends(uid,reasonString);
				}else{
					Toast.makeText(mContext,R.string.please_input_request_reason_hint, Toast.LENGTH_LONG).show();
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

	/*
	 * 页面销毁释放通知
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mReceiver!=null){
			unregisterReceiver(refreshBrodCast);
		}
	}


}
