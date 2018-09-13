package net.smallchat.im.meeting;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.contact.ChooseUserActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MeetingItem;
import net.smallchat.im.R;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 会议详情
 * @author dongli
 *
 */
public class MettingDetailActivity extends BaseActivity {

	/*
	 * 定义全局变量
	 */
	public final static String ACTION_SHOW_NEW_MEETING_TIP ="im_action_show_new_meeting_tip";
	public final static String ACTION_HIDE_NEW_MEETING_TIP ="im_action_hide_new_meeting_tip";
	
	private TextView mMetTitleTextView,mMetHostTextView,mMetStartTimeTextView,
	mMetEndTimeTextView,mMetTopicTextView,mMessageCount,mJoinTextView;
	private Button mValicMetBtn,mManagerBtn;
	private RelativeLayout mJoinMetBtn;

	private Dialog  mPhoneDialog;
	private int mMetId;
	private MeetingItem mMeetingItem;

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.metting_detail_view);
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_UPDATE_MEETING_DETAIL);
		filter.addAction(GlobalParam.ACTION_DESTROY_MEETING_PAGE);
		filter.addAction(ACTION_SHOW_NEW_MEETING_TIP);
		filter.addAction(ACTION_HIDE_NEW_MEETING_TIP);
		registerReceiver(RefreshReceiver, filter);
		mMetId = getIntent().getIntExtra("met_id", 0);
		initCompent();
	}

	/*
	 * 处理通知
	 */
	BroadcastReceiver RefreshReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent!=null){
				String action = intent.getAction();
				if(action!=null && !action.equals("")){
					if(action.equals(GlobalParam.ACTION_UPDATE_MEETING_DETAIL)){
						getMetDetail();
					}else if(action.equals(GlobalParam.ACTION_DESTROY_MEETING_PAGE)){
						MettingDetailActivity.this.finish();
					}else if(action.equals(ACTION_SHOW_NEW_MEETING_TIP)){
						SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
						MessageTable messageTable = new MessageTable(db);
						int count = messageTable.queryUnreadCountByID(mMetId+"",ChatType.MeetingMessage);
						if(count!=0){
							mMessageCount.setVisibility(View.VISIBLE);
							mMessageCount.setText(count+"");
						}
					}else if(action.equals(ACTION_HIDE_NEW_MEETING_TIP)){
						mMessageCount.setVisibility(View.GONE);
					}
				}
			}
		}
	};
	
	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,R.string.meeting_detail);
		mLeftBtn.setOnClickListener(this);
		mMetTitleTextView = (TextView)findViewById(R.id.met_name);
		mMetHostTextView = (TextView)findViewById(R.id.host_metting);
		mMetStartTimeTextView = (TextView)findViewById(R.id.start_time_content);
		mMetEndTimeTextView = (TextView)findViewById(R.id.end_time_content);
		mMetTopicTextView = (TextView)findViewById(R.id.met_topic);
		mMessageCount = (TextView)findViewById(R.id.message_count);
		mJoinTextView = (TextView)findViewById(R.id.join_met_text);

		mJoinMetBtn = (RelativeLayout)findViewById(R.id.join_met_btn);
		mValicMetBtn = (Button)findViewById(R.id.valid_btn);
		mManagerBtn = (Button)findViewById(R.id.manager_met_btn);

		mJoinMetBtn.setOnClickListener(this);
		mValicMetBtn.setOnClickListener(this);
		mManagerBtn.setOnClickListener(this);
		getMetDetail();
	}

	/*
	 * 给控件设置文本
	 */
	private void update(MeetingItem item){
		if(item == null ){
			return;	
		}
		mMetTitleTextView.setText(item.metName);
		mMetHostTextView.setText(item.creatorName);
		if(item.metStartTime!=0){
			mMetStartTimeTextView.setText(FeatureFunction.formartTime(item.metStartTime,"yyyy-MM-dd HH:mm"));
		}
		if(item.metEndTime!=0){
			mMetEndTimeTextView.setText(FeatureFunction.formartTime(item.metEndTime,"yyyy-MM-dd HH:mm"));
		}

		mMetTopicTextView.setText(item.metTopic);
		if(item.metEndTime<System.currentTimeMillis()/1000){
			mValicMetBtn.setVisibility(View.GONE);
			mManagerBtn.setVisibility(View.GONE);
			mJoinMetBtn.setVisibility(View.GONE);
		}else{
			SQLiteDatabase db = DBHelper.getInstance(mContext).getReadableDatabase();
			MessageTable messageTable = new MessageTable(db);
			int count = messageTable.queryUnreadCountByID(mMetId+"",ChatType.MeetingMessage);
			
			if(item.role.equals("1")){//管理员
				mValicMetBtn.setVisibility(View.VISIBLE);
				mManagerBtn.setVisibility(View.VISIBLE);
				mJoinMetBtn.setVisibility(View.VISIBLE);
				mJoinTextView.setText(mContext.getResources().getString(R.string.join_met));
				mJoinMetBtn.setBackground(mContext.getResources().getDrawable(R.drawable.invite_friends_btn));
				mJoinTextView.setTextColor(mContext.getResources().getColor(R.color.application_black));
				if(count!=0){
					mMessageCount.setVisibility(View.VISIBLE);
					mMessageCount.setText(count+"");
				}else{
					mMessageCount.setVisibility(View.GONE);
				}
			}else{
				mValicMetBtn.setVisibility(View.GONE);
				mManagerBtn.setVisibility(View.GONE);
				if(count!=0){
					mMessageCount.setVisibility(View.VISIBLE);
					mMessageCount.setText(count+"");
				}else{
					mMessageCount.setVisibility(View.GONE);
				}
				mJoinMetBtn.setVisibility(View.VISIBLE);
				if(item.isjoin == 1){
					mJoinTextView.setText(mContext.getResources().getString(R.string.join_met));
					mJoinMetBtn.setBackground(mContext.getResources().getDrawable(R.drawable.invite_friends_btn));
					mJoinTextView.setTextColor(mContext.getResources().getColor(R.color.application_black));
				}else{
					mJoinTextView.setText(mContext.getResources().getString(R.string.apply_met));
					mJoinMetBtn.setBackground(mContext.getResources().getDrawable(R.drawable.red_btn));
					mJoinTextView.setTextColor(mContext.getResources().getColor(R.color.white));
				}
			}
		}
		
	}


	/*
	 * 获取会议详情内容
	 */
	private void getMetDetail(){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}

		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, 
							mContext.getResources().getString(R.string.send_request));
					MeetingItem item = IMCommon.getIMServerAPI().mettingDetail(mMetId);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_LOAD_DATA,item);
				} catch (NotFoundException e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	/*
	 * 申请会议
	 */
	private void applyMeeting(final String reseon){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().applyMeeting(mMetId,reseon);
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						IMCommon.sendMsg(mHandler,GlobalParam.MSG_CHECK_APPLY_METTING,state);
					} catch (IMException e) {
						e.printStackTrace();
						mHandler.sendEmptyMessage(GlobalParam.MSG_TIME_OUT_EXCEPTION);
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
	}

	
	


	/*
	 * 按钮点击事件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			MettingDetailActivity.this.finish();
			break;
		case R.id.join_met_btn:
			if(mMeetingItem == null){
				return;
			}
			if(mMeetingItem.isjoin == 1){
				Login user = new Login();
				user.uid = mMeetingItem.id+"";
				user.nickname = mMeetingItem.metName;
				user.headSmall = mMeetingItem.metSmallLog;
				user.mIsRoom = ChatType.MeetingMessage;
				Intent intent = new Intent(mContext, ChatMainActivity.class);
				intent.putExtra("data", user);
				startActivity(intent);
			}else {
				createDialog(mContext, mContext.getResources().getString(R.string.apply_met_reason),
						mContext.getResources().getString(R.string.ok));
			}
			break;
		case R.id.valid_btn:
			if(mMeetingItem == null){
				return;
			}
			Intent intent = new Intent();
			intent.setClass(mContext, ChooseUserActivity.class);
			intent.putExtra("join_meeting", 1);
			intent.putExtra("meeting_id", mMetId);
			intent.putExtra("meet_name", mMeetingItem.metName);
			intent.putExtra("meet_url", mMeetingItem.metSmallLog);
			startActivity(intent);
			break;
		case R.id.manager_met_btn:
			Intent mangIntent = new Intent();
			mangIntent.setClass(mContext, ManagerMeetingActivity.class);
			mangIntent.putExtra("met_id",mMetId );
			startActivity(mangIntent);
			break;

		default:
			break;
		}
	}
	
	/**
	 * 创建对话框
	 * @param context
	 * @param cardTitle
	 * @param type 1-加入黑名单 2-添加好友申请 
	 * @param okTitle
	 */
	protected void createDialog(Context context, String cardTitle,final String okTitle) {
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
					if (mPhoneDialog!=null) {
						mPhoneDialog.dismiss();
						mPhoneDialog=null;
					}
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.send_request));
					applyMeeting(reasonString);
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
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_SHOW_LOAD_DATA:
				MeetingItem item = (MeetingItem) msg.obj;
				if(item != null && item.status!=null && item.status.code == 0){
					mMeetingItem = item;
					update(item);
				}
				break;
			case GlobalParam.MSG_CHECK_APPLY_METTING:
				IMResponseState status = (IMResponseState)msg.obj;
				if(status == null){
					Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
					return;
				}
				String hintMsg = status.errorMsg;
				if(status.code == 0){
					hintMsg = mContext.getResources().getString(R.string.apply_met_success);
				}else{
					if(hintMsg == null || hintMsg.equals("")){
						hintMsg = mContext.getResources().getString(R.string.commit_data_error);
					}
				}
				Toast.makeText(mContext, hintMsg, Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}

	};

	/*
	 * 页面销毁释放通知
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(RefreshReceiver!=null){
			unregisterReceiver(RefreshReceiver);
		}
	}
	
	

}
