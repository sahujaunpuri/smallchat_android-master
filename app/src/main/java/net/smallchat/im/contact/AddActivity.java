package net.smallchat.im.contact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.UserList;
import net.smallchat.im.R;
import net.smallchat.im.config.SMSConfig;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.search.SearchResultActivity;
import net.smallchat.im.utils.ShareToFriend;

/**
 * 添加朋友
 * @author dongli
 *
 */
public class AddActivity extends BaseActivity implements OnClickListener{

	/**
	 * 定义全局变量
	 */
	private RelativeLayout mFindLayout,mContactLayout,mOrderLayout,
	mPressBtnAddFriendLayout,mScanLayout, mGroupLayout,mAddOtherFriendLayout;
	private TextView  mScanTextView, mGroupTextView;
	private EditText mNameContent;
	private RelativeLayout mSearchBtn;


	private boolean mIsRegisterReceiver = false;
	public final static String DESTORY_ACTION = "im_add_destory_action";

	/**
	 * 导入控件
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.add_page);
		IntentFilter filter = new IntentFilter();
		filter.addAction(DESTORY_ACTION);
		registerReceiver(mReceiver, filter);
		mIsRegisterReceiver = true;
		initComponent();
	}

	/**
	 * 处理通知事件
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			if(intent != null){
				String action = intent.getAction();
				if(action.equals(DESTORY_ACTION)){
					AddActivity.this.finish();
				}
			}
		}
	};

	/**
	 * 页面销毁时销毁通知
	 */
	@Override
	protected void onDestroy() {
		if(mIsRegisterReceiver){
			unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}

	/**
	 * 实例化控件
	 */
	private void initComponent(){
		setTitleContent(R.drawable.back_btn,0,R.string.add_friend);
		mLeftBtn.setOnClickListener(this);
		mFindLayout = (RelativeLayout) findViewById(R.id.findlayout);
		mContactLayout = (RelativeLayout)findViewById(R.id.add_contact_friend_layout);
		mAddOtherFriendLayout = (RelativeLayout)findViewById(R.id.add_other_friend_layout);

		mOrderLayout  = (RelativeLayout)findViewById(R.id.order_num);
		mPressBtnAddFriendLayout = (RelativeLayout)findViewById(R.id.add_friends);


		mFindLayout.setOnClickListener(this);
		mContactLayout.setOnClickListener(this);
		mAddOtherFriendLayout.setOnClickListener(this);
		mOrderLayout.setOnClickListener(this);
		mPressBtnAddFriendLayout.setOnClickListener(this);


		mScanLayout = (RelativeLayout) findViewById(R.id.scanlayout);
		mScanLayout.setOnClickListener(this);
		mGroupLayout = (RelativeLayout) findViewById(R.id.grouplayout);
		mGroupLayout.setOnClickListener(this);

		mScanTextView = (TextView) findViewById(R.id.scan);
		mGroupTextView = (TextView) findViewById(R.id.group);

		mScanTextView.setText(ChatApplication.getInstance().getResources().getString(R.string.scan_qr_code));
		mGroupTextView.setText(ChatApplication.getInstance().getResources().getString(R.string.group));

		mNameContent = (EditText) findViewById(R.id.name);
		mNameContent.setHint(ChatApplication.getInstance().getResources().getString(R.string.input_phone));



		mSearchBtn = (RelativeLayout) findViewById(R.id.searchbtn);
		mSearchBtn.setOnClickListener(this);
	}


	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn://返回按钮
			AddActivity.this.finish();
			break;
	
		case R.id.add_contact_friend_layout://添加手机联系人
			Intent contactIntent = new Intent(mContext, ContactActivity.class);
			startActivity(contactIntent);
			break;
			case R.id.add_other_friend_layout://邀请微信联系人
				ShareToFriend.shareTo(this, SMSConfig.INVITE_MESSAGE);
				break;
		case R.id.searchbtn://搜索联系人

			String name = mNameContent.getText().toString().trim();
			if(name.equals("")){
				Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.input_user_name), Toast.LENGTH_SHORT).show();
				return;
			}

			Message message = new Message();
			message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
			message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
			mHandler.sendMessage(message);

			findUser(name);
			break;


		default:
			break;
		}
	}
	
	/**
	 * 查找用户
	 * @param name 输入的内容
	 */
	private void findUser(final String name){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						UserList loginResult = IMCommon.getIMServerAPI().search_number(name,1);
						if(loginResult != null){
							if(loginResult.mState != null && loginResult.mState.code == 0){
								mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
								if(loginResult.mUserList != null && loginResult.mUserList.size()>0){
									if(loginResult.mUserList.size() == 1){//显示搜索出来的用户
										Intent intent = new Intent(mContext, UserInfoActivity.class);
										intent.putExtra("user", loginResult.mUserList.get(0));
										mContext.startActivity(intent);
									}else{//搜索的用户有多个，跳转到可以加载更多的页面
										Intent intent = new Intent();
										intent.setClass(mContext, SearchResultActivity.class);
										intent.putExtra("user_list",(Serializable)loginResult);
										intent.putExtra("searchContent",name);
										startActivity(intent);
									}
							
								}else {
									Message msg=new Message();
									msg.what=GlobalParam.MSG_LOAD_ERROR;
									if(loginResult.mState.errorMsg != null && !loginResult.mState.errorMsg.equals("")){
										msg.obj = loginResult.mState.errorMsg;
									}else {
										msg.obj = ChatApplication.getInstance().getResources().getString(R.string.no_search_user);
									}
									mHandler.sendMessage(msg);
								}
							}else {
								Message msg=new Message();
								msg.what=GlobalParam.MSG_LOAD_ERROR;
								if(loginResult.mState != null && loginResult.mState.errorMsg != null && !loginResult.mState.errorMsg.equals("")){
									msg.obj = loginResult.mState.errorMsg;
								}else {
									msg.obj = ChatApplication.getInstance().getResources().getString(R.string.load_error);
								}
								mHandler.sendMessage(msg);
							}
						}else {
							mHandler.sendEmptyMessage(GlobalParam.MSG_LOAD_ERROR);
						}
						
					} catch (IMException e) {
						e.printStackTrace();
						Message msg=new Message();
						msg.what=GlobalParam.MSG_TIME_OUT_EXCEPTION;
						msg.obj= ChatApplication.getInstance().getResources().getString(R.string.timeout);
						mHandler.sendMessage(msg);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
	}
	
	
	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				
			case GlobalParam.SHOW_PROGRESS_DIALOG:
				String dialogMsg = (String)msg.obj;
				showProgressDialog(dialogMsg);
				break;
			case GlobalParam.HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
				//Toast.makeText(mContext,Strings.getString(R.string.feedback_success),Toast.LENGTH_LONG).show();
				break;
				
			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				String error_Detail = (String)msg.obj;
				if(error_Detail != null && !error_Detail.equals("")){
					Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.load_error),Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				hideProgressDialog();
				Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_TIME_OUT_EXCEPTION:
				hideProgressDialog();
				String message=(String)msg.obj;
				if (message==null || message.equals("")) {
					message= ChatApplication.getInstance().getResources().getString(R.string.timeout);
				}
				Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
}
