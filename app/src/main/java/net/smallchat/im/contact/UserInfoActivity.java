package net.smallchat.im.contact;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.LoginResult;
import net.smallchat.im.Entity.NewFriendItem;
import net.smallchat.im.Entity.Picture;
import net.smallchat.im.Entity.PopItem;
import net.smallchat.im.R;
import net.smallchat.im.album.MyAlbumActivity;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.fragment.ChatFragment;
import net.smallchat.im.fragment.ContactsFragment;
import net.smallchat.im.friendcircle.SetFriendCircleAuthActivity;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.api.IMException;
import net.smallchat.im.search.SearchResultActivity;
import net.smallchat.im.widget.PopWindows;
import net.smallchat.im.widget.PopWindows.PopWindowsInterface;

/**
 * 好友资料
 * @author dongli
 *
 */
public class UserInfoActivity extends BaseActivity implements OnClickListener{

	private ImageView mHeaderView,mSexIcon;
	private Button mSendBtn,mAddFriendBtn;

	private TextView mUserNameView, mDescNameView, mAddrView, mSignView,
	mPhoneView;
	private LinearLayout mAlbumLayout;

	private RelativeLayout mSingLayout,mPhotoLayout,mAddrLayout;

	private ImageLoader mImageLoader = new ImageLoader();
	private Login mLogin;
	private int mType = 0;
	private int mIsBlackJump =0;
	private String mSearchUid;
	private static final int REQUEST_CODE = 5412;
	private static final int REMARK_REQUEST = 5143;
	private int mNewImgWidth;
	private DisplayMetrics mMetric;
	private int mIsHide;
	private String mSearchName,mAddr;
	private String mRemarkName;


	private int mPos;
	private int mIsLogin;
	private PopWindows mPopWindows;
	private Dialog  mPhoneDialog;
	private List<PopItem> mPopList = new ArrayList<PopItem>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.profile_layout);

		mLogin = (Login)getIntent().getSerializableExtra("user");
		mSearchUid = getIntent().getStringExtra("uid");
		mIsBlackJump = getIntent().getIntExtra("is_black_jump",0);
		mType = getIntent().getIntExtra("type", 0);
		mIsHide = getIntent().getIntExtra("ishide",0);
		mIsLogin = getIntent().getIntExtra("isLogin",0);
		mSearchName = getIntent().getStringExtra("username");
		mPos= getIntent().getIntExtra("pos", -1);
		mMetric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mMetric);
		mNewImgWidth = 60;


		initComponent();
	}

	private void initComponent(){
		if(mLogin!=null && mLogin.uid.equals(IMCommon.getUserId(mContext))){
			setTitleContent(R.drawable.back_btn,0, R.string.profile_detail);
		}else{
			setTitleContent(R.drawable.back_btn, R.drawable.more_btn, R.string.profile_detail);
		}

		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);



		mSendBtn = (Button) findViewById(R.id.message_btn);
		mAddFriendBtn = (Button)findViewById(R.id.add_friends);
		mSendBtn.setOnClickListener(this);
		mAddFriendBtn.setOnClickListener(this);

		if(mIsHide == 1){
			mAddFriendBtn.setVisibility(View.GONE);
		}

		if(mIsLogin == 1){
			mRightBtn.setVisibility(View.GONE);
			mAddFriendBtn.setVisibility(View.GONE);
		}else{

			String[] menuString = null;
			if(mIsBlackJump == 1 ){
				menuString = mContext.getResources().getStringArray(R.array.black_user_more_item);
			}else{
				menuString = mContext.getResources().getStringArray(R.array.chat_more_item);
			}
			String[] itemMenuBg = mContext.getResources().getStringArray(R.array.user_more_array);
			for (int i = 0; i < menuString.length; i++) {
				mPopList.add(new PopItem(i+1, menuString[i],itemMenuBg[i]));
			}

			mPopWindows = new PopWindows(mContext, mPopList, mRightBtn, new PopWindowsInterface() {

				@Override
				public void onItemClick(int position, View view) {
					switch (position) {
					case 1://备注名
						Intent intent = new Intent(mContext, RemarkActivity.class);
						intent.putExtra("fuid", mLogin.uid);
						intent.putExtra("re_name", mRemarkName);
						startActivityForResult(intent, REMARK_REQUEST);
						break;
					case 2://标为星标朋友
						setStar();
					
						break;
					case 3://设置朋友圈权限
						Intent loopIntent = new Intent();
						loopIntent.setClass(mContext, SetFriendCircleAuthActivity.class);
						loopIntent.putExtra("entity",mLogin);
						startActivityForResult(loopIntent, 1);
						break;
					case 4://发送该名片
						Intent chooseUserIntent = new Intent();
						chooseUserIntent.setClass(mContext, ChooseUserActivity.class);
						chooseUserIntent.putExtra("cardType",1);
						chooseUserIntent.putExtra("cardLogin", mLogin);
						startActivity(chooseUserIntent);
						break;
					case 5://加入黑名单
						if(mLogin!=null && mLogin.userType == 1){
							addBlock();
						}else{
							createDialog(mContext,
									mContext.getResources().getString(R.string.move_to_dialog_hint)
									,1,"加入");
						}

						break;
						
					case 6://删除
						String remarkName = mLogin.remark;
						if(remarkName == null || remarkName.equals("")){
							remarkName = mLogin.nickname;
						}
						createDialog(mContext,mContext.getResources().getString(R.string.del_friends_dialog_hint_start)
								+remarkName+mContext.getResources().getString(R.string.del_friends_dialog_hint_end),3
								,"删除");
						break;

					default:
						break;
					}
				}
			});

		}

		mSexIcon = (ImageView)findViewById(R.id.sex_image);
		mHeaderView = (ImageView) findViewById(R.id.header);
		mUserNameView = (TextView) findViewById(R.id.name);
		mDescNameView = (TextView) findViewById(R.id.desc_name);

		mAddrView = (TextView) findViewById(R.id.addr_content);
		mAddrLayout = (RelativeLayout)findViewById(R.id.addr_layout);


		mSignView = (TextView) findViewById(R.id.sign_content);
		mPhoneView = (TextView) findViewById(R.id.phone_content);
		mAlbumLayout = (LinearLayout)findViewById(R.id.new_photo_layout);

		mSingLayout = (RelativeLayout)findViewById(R.id.sign_layout);
		mPhotoLayout = (RelativeLayout)findViewById(R.id.photo_layout);

		if(mType != 2){
			if(mLogin != null){
				showProfile();
			}
		}else {
			IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,mContext.getResources().getString( R.string.add_more_loading));
			if(mSearchUid!=null && !mSearchUid.equals("")){
				searchUserByID(mSearchUid,1);
			}else if(mSearchName != null && !mSearchName.equals("")){
				searchUserByID(mSearchName,2);
			}

		}
	}

	private void showProfile(){
		if(mLogin == null || mLogin.uid == null || mLogin.uid.equals("")){
			return;
		}
		String[] menuString;
		if(mLogin.userType == 1){
			if(mPopList!=null && mPopList.size()>0){
				mPopList.clear();
			}
			menuString = mContext.getResources().getStringArray(R.array.black_user_more_item);
		}else{
			menuString = mContext.getResources().getStringArray(R.array.chat_more_item);
		}
		String[] itemMenuBg = mContext.getResources().getStringArray(R.array.user_more_array);
		if(mPopList!=null && mPopList.size()>0){
			mPopList.clear();
		}
		for (int i = 0; i < menuString.length; i++) {
			mPopList.add(new PopItem(i+1, menuString[i],itemMenuBg[i]));
		}
		if(mLogin.userType == 2){
			mPopList.get(1).option = "取消星标朋友";
		}else{
			mPopList.get(1).option = "标为星标朋友";
		}
		if(mLogin.uid.equals(IMCommon.getUserId(mContext))){
			mRightBtn.setVisibility(View.GONE);
			mSendBtn.setVisibility(View.GONE);
			mAddFriendBtn.setVisibility(View.GONE);
		}else {
			mAddFriendBtn.setVisibility(View.GONE);
			if(mLogin.isfriend == 1){
				mRightBtn.setVisibility(View.VISIBLE);
				mPhotoLayout.setVisibility(View.VISIBLE);
				mSignView.setVisibility(View.VISIBLE);
				mSendBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.send_msg));
			}else {
				if(mLogin.userType == 1){
					if(mPopList!=null && mPopList.size()>0){
						mPopList.clear();
					}
					menuString = mContext.getResources().getStringArray(R.array.black_user_more_item);
					//menuString = mContext.getResources().getStringArray(R.array.chat_more_item);
					for (int i = 0; i < menuString.length; i++) {
						mPopList.add(new PopItem(i+1, menuString[i],itemMenuBg[i]));
					}
					if(mLogin.userType == 2){
						mPopList.get(1).option = "取消星标朋友";
					}else{
						mPopList.get(1).option = "标为星标朋友";
					}
					mRightBtn.setVisibility(View.VISIBLE);
					mPhotoLayout.setVisibility(View.VISIBLE);
					mSingLayout.setVisibility(View.GONE);
					mSendBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.send_msg));
				}else{
					mRightBtn.setVisibility(View.GONE);
					mPhotoLayout.setVisibility(View.GONE);
					mSingLayout.setVisibility(View.GONE);
					mSendBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.add_to_contact));
				}

				//mAddFriendBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.add_to_friend));
			}
		
		}

		if(mLogin != null){
			if(mLogin.headSmall != null && !mLogin.headSmall.equals("")){
				mImageLoader.getBitmap(mContext, mHeaderView, null, mLogin.headSmall, 0, false, true);
			}
			//showAddr();
			if((mLogin.provinceid==null || mLogin.provinceid.equals(""))
					&& (mLogin.cityid == null || mLogin.cityid.equals(""))){
				mAddrLayout.setVisibility(View.GONE);
			}else{
				mAddrView.setText(mLogin.provinceid+" "+mLogin.cityid);
			}
			if(mLogin.gender == 0 || mLogin.gender == 2){
				mSexIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.boy));
			}else if(mLogin.gender == 1){
				mSexIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.girl));
			}
			if(mLogin.remark!=null && !mLogin.remark.equals("")){
				mUserNameView.setText(mLogin.remark);
				mRemarkName = mLogin.remark;
				mDescNameView.setText(mContext.getResources().getString(R.string.nickname)
						+": "+mLogin.nickname);
			}else{
				mUserNameView.setText(mLogin.nickname);
			}

			if(mLogin.sign == null || mLogin.sign.equals("")){
				mSingLayout.setVisibility(View.GONE);
			}else{
				mSignView.setText(mLogin.sign);
			}

			mPhoneView.setText(mLogin.phone);
			
		}
		showImage(mLogin.picList);
	}




	private void showImage(List<Picture> picList) {
		if (picList != null) {
			mAlbumLayout.removeAllViews();

			mAlbumLayout.setOnClickListener(this);
			int size = picList.size();
			if (size > 0) {
				for (int i = 0; i < size; i++) {

					LinearLayout layout = new LinearLayout(mContext);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							FeatureFunction.dip2px(mContext, 60), FeatureFunction.dip2px(mContext, 60));
					layout.setLayoutParams(params);
					params.gravity = Gravity.CENTER_VERTICAL;
					int margin = FeatureFunction.dip2px(mContext, 5);
					layout.setPadding(margin, margin, margin, margin);

					ImageView imageView = new ImageView(mContext);
					imageView.setLayoutParams(new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT,
							LinearLayout.LayoutParams.FILL_PARENT));

					imageView.setScaleType(ScaleType.CENTER_CROP);
					imageView.setImageResource(R.drawable.noraml_album);
					final String url = picList.get(i).smallUrl;
					if (url != null && !url.equals("")) {
						mImageLoader.getBitmap(mContext, imageView, null, url,
								0, true, false);

						imageView.setTag(url);
					}
					layout.addView(imageView);

					mAlbumLayout.addView(layout);
				}
			}
		}
	}

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
				int type = msg.arg1;
				if(type == 1){

					//更新用户列表
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
					UserTable table = new UserTable(db);
					if(mLogin.userType == 0){
						mLogin.userType = 1;
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.add_block_success),Toast.LENGTH_LONG).show();
					}else if(mLogin.userType == 1){
						mLogin.userType = 0;
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.remove_block_success),Toast.LENGTH_LONG).show();
					}

					table.update(mLogin);
					sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));

					Intent resultIntent = new Intent(SearchResultActivity.REFRSCH_ITEM_ACTION);
					resultIntent.putExtra("uid", mLogin.uid);
					resultIntent.putExtra("user_type",mLogin.userType);
					sendBroadcast(resultIntent);

					showProfile();
				}else if(type == 2){
					sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
					Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.delete_friend_success),Toast.LENGTH_LONG).show();
					UserInfoActivity.this.finish();
				}else if(type == 4){
					Intent intent = new Intent();
					intent.putExtra("uid", mLogin.uid);
					setResult(2,intent);
					String hintMsg = (String) msg.obj;
					if(hintMsg!=null && !hintMsg.equals("")){
						Toast.makeText(mContext, hintMsg, Toast.LENGTH_LONG).show();
						checkFriendsNotify(mLogin,2);
					}
				}else {
					if(mLogin != null){
						showProfile();
					}else {
						String prmopt = (String) msg.obj;
						if(prmopt != null && !prmopt.equals("")){
							Toast.makeText(mContext, prmopt, Toast.LENGTH_SHORT).show();
							UserInfoActivity.this.finish();
						}
					}
				}
				break;
			case GlobalParam.MSG_CHECK_STATE:
				IMResponseState state = (IMResponseState)msg.obj;
				if(state == null || state.equals("")){
					Toast.makeText(mContext, R.string.focus_user_failed,Toast.LENGTH_LONG).show();
					return;
				}
				if(state.code == 0){
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
					UserTable table = new UserTable(db);
					table.update(mLogin);
					if(mLogin!=null){
						showProfile();
					}
				}else{
					Toast.makeText(mContext, state.errorMsg,Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_SHOW_LOAD_DATA:
				if(mAddr!=null && !mAddr.equals("")){
					mAddrView.setText(mAddr);
				}
				break;
			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				int excuteType = msg.arg1;
				String prompt = (String) msg.obj;
				if(prompt != null && !prompt.equals("")){
					Toast.makeText(mContext, prompt, Toast.LENGTH_LONG).show();
				}else {
					if(excuteType == 1){
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.add_block_failed),Toast.LENGTH_LONG).show();
					}else if(excuteType == 2){
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.delete_friend_failed),Toast.LENGTH_LONG).show();
					}else if(excuteType == 3){
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.no_search_user),Toast.LENGTH_LONG).show();
					}else if(excuteType == 4){
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.send_friend_apply_failed),Toast.LENGTH_LONG).show();
					}
				}

				break;
			case  GlobalParam.MSG_CHECK_STAR:
				LoginResult starLogin = (LoginResult)msg.obj;
				if(starLogin == null || starLogin.mState == null){
					Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
					return;
				}
				if(starLogin.mState.code == 0){
					SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
					UserTable table = new UserTable(db);
					if(mLogin.userType == 0){
						mLogin.userType = 2;
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.set_star_success),Toast.LENGTH_LONG).show();
					}else if(mLogin.userType == 2){
						mLogin.userType = 0;
						Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.remove_star_success),Toast.LENGTH_LONG).show();
					}

					table.update(mLogin);
					sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
					showProfile();
				}else{
					String hintMsg = starLogin.mState.errorMsg;
					if(hintMsg == null || hintMsg.equals("")){
						hintMsg = mContext.getResources().getString(R.string.commit_data_error);
					}
					Toast.makeText(mContext, hintMsg,Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_CHECK_FRIENDS_LOOP_AUTH:
			
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

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.message_btn:
			if(mLogin != null){
				if(mLogin.isfriend == 1 || mLogin.userType == 1){

					Intent intent = new Intent(mContext, ChatMainActivity.class);
					mLogin.mIsRoom = 100;
					intent.putExtra("data", mLogin);
					startActivity(intent);
					UserInfoActivity.this.finish();
				}else{//添加到通讯录
					if(mLogin.isfriend == 0){
						createDialog(mContext,
								mContext.getResources().getString(R.string.request_doing)
								,2,mContext.getResources().getString(R.string.send));
					}
				}

			}

			break;
		case R.id.add_friends://添加好友
			if(mLogin!=null){
				if(mLogin.isfriend == 0){
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,R.string.add_more_loading);
					createDialog(mContext,
							mContext.getResources().getString(R.string.request_doing)
							,2,mContext.getResources().getString(R.string.send));
				}
			}
			break;
		case R.id.focus:
			cancelAddFocus();
			break;

		case R.id.left_btn:
			this.finish();
			break;

		case R.id.right_btn:
			mPopWindows.showGroupPopView(mPopList,Gravity.RIGHT,R.drawable.pop_bg,R.color.white,0);
			break;

		case R.id.new_photo_layout:
			if(mLogin == null){
				return;
			}
			Intent intent = new Intent();
			intent.setClass(mContext, MyAlbumActivity.class);
			intent.putExtra("toUserID", mLogin.uid);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	private void addBlock(){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().addBlock(mLogin.uid);
						if(state != null && state.code == 0){
							Message message = new Message();
							message.what = GlobalParam.HIDE_PROGRESS_DIALOG;
							message.arg1 = 1;
							mHandler.sendMessage(message);
						}else {
							Message message = new Message();
							message.what = GlobalParam.MSG_LOAD_ERROR;
							message.arg1 = 1;
							if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
								message.obj = state.errorMsg;
							}
							mHandler.sendMessage(message);
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

	private void deleteFriend(){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().cancleFriends(mLogin.uid);
						if(state != null && state.code == 0){

							mLogin.isfriend = 0;
							mLogin.groupId = -999;
							SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
							UserTable table = new UserTable(db);
							table.update(mLogin);

							MessageTable messageTable = new MessageTable(db);

							messageTable.delete(mLogin.uid, 100);
							SessionTable sessionTable = new SessionTable(db);

							sessionTable.delete(mLogin.uid, 100);


							Intent chatIntent = new Intent(ChatMainActivity.REFRESH_ADAPTER);
							chatIntent.putExtra("id", mLogin.uid);
							mContext.sendBroadcast(chatIntent);
							mContext.sendBroadcast(new Intent(ChatFragment.ACTION_REFRESH_SESSION));
							mContext.sendBroadcast(new Intent(GlobalParam.ACTION_UPDATE_SESSION_COUNT));

							List<NewFriendItem> list = IMCommon.getNewFriendItemResult(mContext);
							if(list!=null && list.size()>0){
								for (int i = 0; i < list.size(); i++) {
									if(list.get(i).uid.equals(mLogin.uid)){
										list.get(i).type = 0;
										IMCommon.saveNewFriendsResult(mContext, list,0);
										break;
									}
								}
							}

							Message message = new Message();
							message.what = GlobalParam.HIDE_PROGRESS_DIALOG;
							message.arg1 = 2;
							mHandler.sendMessage(message);
						}else {
							Message message = new Message();
							message.what = GlobalParam.MSG_LOAD_ERROR;
							message.arg1 = 2;
							if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
								message.obj = state.errorMsg;
							}
							mHandler.sendMessage(message);
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

	private void addFriend(final String reseon){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().applyFriends(IMCommon.getUserId(mContext), mLogin.uid,reseon);
						if(state != null && state.code == 0){
							String hintMsg = "";
							int checkStatus = 4;
							if(state.errorMsg.equals(mContext.getResources().getString(R.string.add_friends_success))){
								sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
								checkStatus = 3;
								searchUserByID(mLogin.uid,1);
							}else{
								hintMsg= mContext.getResources().getString(R.string.send_friend_apply_success);
							}
							if(mPos!=-1){
								Intent intent = new Intent(ContactActivity.REFRESH_SYSTEM_CONTACT_ACTION);
								intent.putExtra("pos",mPos);
								intent.putExtra("status",checkStatus);
								sendBroadcast(intent);
							}
						
							IMCommon.sendMsg(mHandler,GlobalParam.HIDE_PROGRESS_DIALOG,hintMsg,4);

						}else {
							Message message = new Message();
							message.what = GlobalParam.MSG_LOAD_ERROR;
							message.arg1 = 4;
							if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
								message.obj = state.errorMsg;
							}

							mHandler.sendMessage(message);
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

	private void searchUserByID(final String uid,final int searchType){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						LoginResult loginResult = null;
						if(searchType == 1){
							loginResult = IMCommon.getIMServerAPI().getUserInfo(uid);
						}else if(searchType == 2){
							loginResult = IMCommon.getIMServerAPI().getUserByName(mSearchName);
						}
						if(loginResult != null && loginResult.mState != null && loginResult.mState.code == 0){
							mLogin = loginResult.mLogin;
							Message message = new Message();
							message.what = GlobalParam.HIDE_PROGRESS_DIALOG;
							message.arg1 = 3;
							if(loginResult.mState.errorMsg != null && !loginResult.mState.errorMsg.equals("")){
								message.obj = loginResult.mState.errorMsg ;
							}
							mHandler.sendMessage(message);
						}else {
							Message message = new Message();
							message.what = GlobalParam.MSG_LOAD_ERROR;
							message.arg1 = 3;
							if(loginResult != null && loginResult.mState != null && loginResult.mState.errorMsg != null && !loginResult.mState.errorMsg.equals("")){
								message.obj = loginResult.mState.errorMsg;
							}

							mHandler.sendMessage(message);
							UserInfoActivity.this.finish();
						}
					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
								mContext.getResources().getString(e.getStatusCode()));
						UserInfoActivity.this.finish();
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
			UserInfoActivity.this.finish();
		}
	}

	private void cancelAddFocus(){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, R.string.commit_dataing);
					IMResponseState state = IMCommon.getIMServerAPI().addfocus(mLogin.uid);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,state);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR, 
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}

	private void setStar(){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, 
							mContext.getResources().getString(R.string.commit_dataing));
					LoginResult state = IMCommon.getIMServerAPI().setStar(mLogin.uid);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STAR,state);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR, 
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CODE:
			if(resultCode == RESULT_OK){
				String name = data.getStringExtra("name");
				mLogin.groupName = name;
			}
			break;
		case REMARK_REQUEST:
			if(resultCode == RESULT_OK){
				String markName = data.getStringExtra("markName");
				SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
				UserTable table = new UserTable(db);
				mLogin.remark = markName;
				if(mLogin.remark!=null && !mLogin.remark.equals("")){
					mUserNameView.setText(mLogin.remark);
					mRemarkName = mLogin.remark;
					mDescNameView.setText(mContext.getResources().getString(R.string.nickname)
							+": "+mLogin.nickname);
				}else{
					mUserNameView.setText(mLogin.nickname);
					mDescNameView.setVisibility(View.GONE);
					mDescNameView.setText("");
					
				}
				table.update(mLogin);
				sendBroadcast(new Intent(ContactsFragment.REFRESH_FRIEND_ACTION));
			}
			break;
		case 1:
			if(resultCode == 2){
				if(data!=null){
					mLogin = (Login) data.getSerializableExtra("entity");
				}
			}
			break;


		default:
			break;
		}
	}

	/**
	 * 
	 * @param context
	 * @param cardTitle
	 * @param type 1-加入黑名单 2-添加好友申请 
	 * @param okTitle
	 */
	protected void createDialog(Context context, String cardTitle,final int type,final String okTitle) {
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
		if(type == 2){
			reasonEdit.setVisibility(View.VISIBLE);
		}

		Button okBtn=(Button)serviceView.findViewById(R.id.yes);
		okBtn.setText(okTitle);


		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(type !=2){
					if (mPhoneDialog!=null) {
						mPhoneDialog.dismiss();
						mPhoneDialog=null;
					}
				}

				if(type == 1){
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "请求发送中,请稍后...");
					addBlock();
				}else if(type == 2){
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
						addFriend(reasonString);
					}else{
						if (mPhoneDialog!=null) {
							mPhoneDialog.dismiss();
							mPhoneDialog=null;
						}
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "请求发送中,请稍后...");
						addFriend(reasonString);
						//Toast.makeText(mContext,R.string.please_input_request_reason_hint, Toast.LENGTH_LONG).show();
					}
				}else {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "请求发送中,请稍后...");
					deleteFriend();
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

	/**
	 * 
	 * @param login
	 * @param type 0-添加 1-已添加 2-等待验证 3-同意好友的请求
	 */
	private void checkFriendsNotify(Login login,int type){
		List<NewFriendItem> mUserList = new ArrayList<NewFriendItem>();
		mUserList.add(new NewFriendItem(login.phone,login.uid,login.nickname,login.headSmall,
				type,"",IMCommon.getUserId(mContext),1));

		//获取系统中保存的新的朋友
		List<NewFriendItem> lastNewFriendsList = IMCommon.getNewFriendItemResult(mContext);

		boolean isExitsLastData = false;
		for (int i = 0; i < mUserList.size(); i++) {
			String currentUid = mUserList.get(i).uid;
			String currentPhone = mUserList.get(i).phone;
			if(lastNewFriendsList!=null && lastNewFriendsList.size()>0){
				isExitsLastData = true;
				for (int j = 0; j < lastNewFriendsList.size(); j++) {
					if(lastNewFriendsList.get(j).uid.equals(currentUid)){
						mUserList.get(i).colorBgtype = 0;
						if(lastNewFriendsList.get(j).type!=0){
							mUserList.get(i).type = lastNewFriendsList.get(j).type;
						}
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
		IMCommon.saveNewFriendsResult(mContext, mUserList,mUserList.size());
	}




}
