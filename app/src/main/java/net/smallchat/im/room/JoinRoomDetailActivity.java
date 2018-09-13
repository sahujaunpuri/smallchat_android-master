package net.smallchat.im.room;

import java.util.List;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.RoomTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.R;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

/**
 * 待加入的群详情
 * @author dongli
 *
 */
public class JoinRoomDetailActivity extends BaseActivity {
	
	private TextView mGroupNameTextView,mGroupContactCountTextView;
	private Button mJoinBtn;
	private LinearLayout mGroupHeadlerLayout;
	
	private Room mGroupDetail;
	private ImageLoader mImageLoader;

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.join_room_detail);
		mGroupDetail = (Room)getIntent().getSerializableExtra("room");
		mImageLoader = new ImageLoader();
		initCompent();
	}
	
	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,0);
		mLeftBtn.setOnClickListener(this);
		
		mGroupNameTextView = (TextView)findViewById(R.id.group_name);
		mGroupContactCountTextView = (TextView)findViewById(R.id.group_contact_count);
		mGroupHeadlerLayout = (LinearLayout)findViewById(R.id.group_header);
		mJoinBtn = (Button)findViewById(R.id.join_btn);
		mJoinBtn.setOnClickListener(this);
		
		setText();
	}
	
	/*
	 * 给控件设置文本
	 */
	private void setText(){
		mGroupNameTextView.setText(mGroupDetail.groupName);
		mGroupContactCountTextView.setText("(共"+mGroupDetail.groupCount+"人)");
		setHeader();
	}

	/*
	 * 显示用户头像
	 */
	private void setHeader(){
		if(mGroupDetail!=null && (mGroupDetail.mUserList!=null
				&& mGroupDetail.mUserList.size()>0)){
			
		}
	
		int count = 4;
		if(mGroupDetail.mUserList.size()<4){
			count = mGroupDetail.mUserList.size();
		}
		if(count>0){
			if(mGroupHeadlerLayout.getChildCount() != 0){
				mGroupHeadlerLayout.removeAllViews();
			}
			boolean single = count % 2 == 0 ? false : true;
			int row = !single ? count / 2 : count / 2 + 1;
			for (int i = 0; i < row; i++) {
				LinearLayout outLayout = new LinearLayout(mContext);
				outLayout.setOrientation(LinearLayout.HORIZONTAL);
				int width = FeatureFunction.dip2px(mContext, 23);
				outLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, width));
				int padding = FeatureFunction.dip2px(mContext, 1);
				if(single && i == 0){
					LinearLayout layout = new LinearLayout(mContext);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
					layout.setPadding(padding, padding, padding, padding);
					layout.setLayoutParams(params);
					ImageView imageView = new ImageView(mContext);
					imageView.setImageResource(R.drawable.contact_default_header);
					mImageLoader.getBitmap(mContext, imageView, null, mGroupDetail.mUserList.get(0).headSmall, 0, false, true);
					imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
					layout.addView(imageView);
					outLayout.setGravity(Gravity.CENTER_HORIZONTAL);
					outLayout.addView(layout);
				}else {
					for (int j = 0; j < 2; j++) {
						LinearLayout layout = new LinearLayout(mContext);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
						layout.setPadding(padding, padding, padding, padding);
						layout.setLayoutParams(params);
						ImageView imageView = new ImageView(mContext);
						imageView.setImageResource(R.drawable.contact_default_header);
						if(single){
							mImageLoader.getBitmap(mContext, imageView, null, mGroupDetail.mUserList.get(2 * i + j - 1).headSmall, 0, false, true);
						}else {
							mImageLoader.getBitmap(mContext, imageView, null, mGroupDetail.mUserList.get(2 * i + j).headSmall, 0, false, true);
						}
						imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
						layout.addView(imageView);
						outLayout.addView(layout);
					}
				}
				mGroupHeadlerLayout.addView(outLayout);
			}
		}
	}
	
	

	/**
	 * 邀请用户加入群
	 * @param userList
	 */
	private void join(){
		new Thread(){
			@Override
			public void run(){
				if(IMCommon.verifyNetwork(mContext)){
					try {
						Room state = IMCommon.getIMServerAPI().join(mGroupDetail.groupId);
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						if(state != null && state.state!=null && state.state.code == 0){
							IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE, state);
						}else {
							Message msg=new Message();
							msg.what=GlobalParam.MSG_LOAD_ERROR;
							if(state != null && state.state.errorMsg != null && !state.state.errorMsg.equals("")){
								msg.obj = state.state.errorMsg;
							}else {
								msg.obj = mContext.getString(R.string.operate_failed);
							}
							mHandler.sendMessage(msg);
						}
					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
								mContext.getResources().getString(R.string.timeout));
					}catch (Exception e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);

					}

				}else {
					mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
				}
			}
		}.start();
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
			JoinRoomDetailActivity.this.finish();
			break;
		case R.id.join_btn:
			join();
			break;

		default:
			break;
		}
	}
	

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GlobalParam.MSG_CHECK_STATE:
				Room room = (Room)msg.obj;
				if(room!=null){
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();

					Intent destroy = new Intent(ChatMainActivity.DESTORY_ACTION);
					destroy.putExtra("type", 1);
					sendBroadcast(destroy);
					String roomId = room.groupId;
					List<Login> roomUsrList = room.mUserList;
					RoomTable roomTab = new RoomTable(db);
					roomTab.insert(room);
					String groupHeadUrl="";
					if (roomUsrList != null ) {
						UserTable userTable = new UserTable(db);

						for (int j = 0; j < roomUsrList.size(); j++) {

							if(room.groupCount-1 == j){
								groupHeadUrl+=roomUsrList.get(j).headSmall;
							}else{
								groupHeadUrl+=roomUsrList.get(j).headSmall+",";
							}


							Login user = userTable.query(roomUsrList.get(j).uid);
							if(user == null){
								userTable.insert(roomUsrList.get(j), -999);
							}
						}
					}

					Session session = new Session();
					session.type = 300;
					session.name = room.groupName;
					session.heading = groupHeadUrl;
					session.lastMessageTime = System.currentTimeMillis();
					session.setFromId(room.groupId);
					session.mUnreadCount = 0;

					SessionTable table = new SessionTable(db);
					table.insert(session);
					sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));



				
					Login user = new Login();
					user.uid = room.groupId;
					user.nickname = room.groupName;
					user.headSmall = groupHeadUrl;
					user.mIsRoom = 300;
					Intent intent = new Intent(mContext, ChatMainActivity.class);
					intent.putExtra("data", user);

					startActivity(intent);
					JoinRoomDetailActivity.this.finish();

				}
				break;
			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				String error_Detail = (String)msg.obj;
				if(error_Detail != null && !error_Detail.equals("")){
					Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(mContext,R.string.load_error,Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	};

	
	

}
