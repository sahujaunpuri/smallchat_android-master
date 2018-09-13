package net.smallchat.im.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.SessionTable;
import net.smallchat.im.R;
import net.smallchat.im.friendcircle.FriendCircleActivity;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.meeting.MettingActivity;
import net.smallchat.im.scan.CaptureActivity;

/**
 * 发现Fragment的界面
 * @author dl
 */
public class FoundFragment extends Fragment  implements android.view.View.OnClickListener, AdapterView.OnItemClickListener {

	/**
	 * 定义全局变量
	 */
	private View mView;

	private RelativeLayout mQRCodeScanLayout,mFriendsLoopLayout,mMeetingLayout;
	private Context mParentContext;
	private TextView mNewsFriendsLoopIcon,mNewMeetingIcon;



	/**
	 * 导入控件
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mParentContext = (Context)FoundFragment.this.getActivity();
		//PinYin.main();
	}

	/**
	 * 加载控件
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_found, container, false);
		return mView;
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ListView listView = (ListView) view.findViewById(R.id.fragment_found_list);


		LayoutInflater inflater = LayoutInflater.from(getActivity());




		View found_friend_circle = inflater.inflate(R.layout.found_friend_circle, listView, false);
		listView.addHeaderView(found_friend_circle);



		View found_scan = inflater.inflate(R.layout.found_scan, listView, false);

		listView.addHeaderView(found_scan);






		View found_meeting= inflater.inflate(R.layout.found_meeting, listView, false);

		listView.addHeaderView(found_meeting);

//		//小程序
//		View found_smallapp= inflater.inflate(R.layout.found_smallapp, listView, false);
//
//		listView.addHeaderView(found_smallapp);

		listView.setAdapter(null);

	}

	/**
	 * 初始化界面
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);



		mQRCodeScanLayout = (RelativeLayout)mView.findViewById(R.id.app_qrcode_scan_content);
		mQRCodeScanLayout.setOnClickListener(this);


		mFriendsLoopLayout = (RelativeLayout)mView.findViewById(R.id.app_friend_circle_content);
		mFriendsLoopLayout.setOnClickListener(this);


		mMeetingLayout = (RelativeLayout)mView.findViewById(R.id.app_news_content);
		mMeetingLayout.setOnClickListener(this);


		mNewsFriendsLoopIcon = (TextView)mView.findViewById(R.id.friends_message_count);
		mNewMeetingIcon = (TextView)mView.findViewById(R.id.app_news_message_count);


		register();
	}

	/**
	 * 注册界面通知
	 */
	private void register(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_SHOW_NEW_FRIENDS_LOOP);
		filter.addAction(GlobalParam.ACTION_HIDE_NEW_FRIENDS_LOOP);
		filter.addAction(GlobalParam.ACTION_SHOW_NEW_MEETING);
		filter.addAction(GlobalParam.ACTION_HIDE_NEW_MEETING);


		mParentContext.registerReceiver(mReBoradCast, filter);
	}

	/**
	 * 处理通知
	 */
	BroadcastReceiver mReBoradCast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent!=null){
				String action = intent.getAction();
				if(action.equals(GlobalParam.ACTION_SHOW_NEW_FRIENDS_LOOP)){
					if(mNewsFriendsLoopIcon!=null){
						int count = IMCommon.getFriendsLoopTip(mParentContext);
						if(count!=0){
							mNewsFriendsLoopIcon.setVisibility(View.VISIBLE);
							mNewsFriendsLoopIcon.setText(count+"");
						}
					}
				}else if(action.equals(GlobalParam.ACTION_HIDE_NEW_FRIENDS_LOOP)){
					if(mNewsFriendsLoopIcon!=null){
						mNewsFriendsLoopIcon.setVisibility(View.GONE);
					}
				}else if(action.equals(GlobalParam.ACTION_SHOW_NEW_MEETING)){
					if(mNewMeetingIcon!=null){
						//查询会议消息数量
						SQLiteDatabase db = DBHelper.getInstance(mParentContext).getReadableDatabase();
						SessionTable table = new SessionTable(db);
						int count = table.queryMeetingSessionCount();
						mNewMeetingIcon.setVisibility(View.VISIBLE);
						/*if(count!=0){
							mNewMeetingIcon.setVisibility(View.VISIBLE);
							//mNewMeetingIcon.setText(count+"");
						}*/
					}
				}else if(action.equals(GlobalParam.ACTION_HIDE_NEW_MEETING)){
					if(mNewMeetingIcon!=null){
						mNewMeetingIcon.setVisibility(View.GONE);
					}
				}
			}
		}
	};


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.app_friend_circle_content://点击朋友圈应用
				Intent intent = new Intent();
				intent.setClass(mParentContext, FriendCircleActivity.class);
				startActivity(intent);
				break;
			case R.id.app_qrcode_scan_content:{//发现扫一扫
				Intent intentQrcodeScan = new Intent();
				intentQrcodeScan.setClass(mParentContext, CaptureActivity.class);
				startActivity(intentQrcodeScan);
				break;
			}
			case R.id.app_news_content:
				Intent meeting = new Intent();
				meeting.setClass(mParentContext, MettingActivity.class);
				startActivity(meeting);
				break;

			default:
				break;
		}
	}

	/**
	 * 销毁页面
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mReBoradCast!=null){
			mParentContext.unregisterReceiver(mReBoradCast);
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}
}
