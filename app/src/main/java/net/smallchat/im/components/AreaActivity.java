package net.smallchat.im.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.ChatDetailEntity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.R;
import net.smallchat.im.adapter.ChatPersonAdapter;
import net.smallchat.im.contact.AddPersonActivity;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.widget.MyGridView;

/**
 * 可见范围
 * @author dongli
 *
 */
public class AreaActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener{

	/*
	 * 定义全局变量
	 */
	private static final int GET_USER_REQUEST = 5124;
	private LinearLayout mMenuLayout;
	private MyGridView mGridView;
	private ChatPersonAdapter mAdapter;
	private List<Login> mUserList = new ArrayList<Login>();
	private List<ChatDetailEntity> mList = new ArrayList<ChatDetailEntity>();
	private String mUidString="";
	private int mCheckSelectId =0;

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.area_view);
		mCheckSelectId = getIntent().getIntExtra("checkId",0);
		initCompent();
	}

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,R.string.select_watch_area);
		mLeftBtn.setOnClickListener(this);
		mMenuLayout = (LinearLayout)findViewById(R.id.menu_item);
		showMenu(mContext.getResources().getStringArray(R.array.area_item),mCheckSelectId);


		mGridView = (MyGridView) findViewById(R.id.gridview);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		
		if(mCheckSelectId == 0){
			mGridView.setVisibility(View.GONE);
		}else if(mCheckSelectId == 1){
			mGridView.setVisibility(View.VISIBLE);
		}

		List<Login> mTempList = (List<Login>) getIntent().getSerializableExtra("userlist");

		if(mTempList != null && mTempList.size() != 0){
			mUserList.addAll(mTempList);
			for (int j = 0; j < mTempList.size(); j++) {
				mList.add(new ChatDetailEntity(mTempList.get(j), 0));
			}
		}
		mList.add(new ChatDetailEntity(null, 1));
		

		mAdapter = new ChatPersonAdapter(mContext, mList);
		mGridView.setAdapter(mAdapter);
	}

	/*
	 * 显示可见范围选择项 公开和私密
	 */
	private void showMenu(String[] menuArray,int checkId){

		if(mMenuLayout!=null && mMenuLayout.getChildCount()!=0){
			mMenuLayout.removeAllViews();
		}
		if(menuArray == null || menuArray.length<=0){
			return;
		}
		LayoutInflater flater = (LayoutInflater) getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < menuArray.length; i++) {
			View view = flater.inflate(R.layout.neary_location_item,null);
			RelativeLayout layout = (RelativeLayout)view.findViewById(R.id.top_layout);
			layout.setPadding(10, 20,10,20);
			TextView title = (TextView)view.findViewById(R.id.location_text);
			title.setPadding(5, 0, 5, 0);
			final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checklocation);
			ImageView splite = (ImageView)view.findViewById(R.id.splite);
			if(i == menuArray.length-1){
				splite.setVisibility(View.GONE);
			}
			checkBox.setVisibility(View.VISIBLE);
			if(i == checkId){
				checkBox.setChecked(true);
			}else{
				checkBox.setChecked(false);
			}

			title.setText(menuArray[i]);
			final int pos = i;
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					for (int j = 0; j < mMenuLayout.getChildCount(); j++) {
						if(j !=pos){
							View view = mMenuLayout.getChildAt(j);
							CheckBox checkBox = (CheckBox) view.findViewById(R.id.checklocation);
							checkBox.setChecked(false);
						}
					}
					checkBox.setChecked(true);
					mCheckSelectId = pos;
					if(pos == 0){
						mGridView.setVisibility(View.GONE);
					}else if(pos == 1){
						mGridView.setVisibility(View.VISIBLE);
					}
				}
			});
			mMenuLayout.addView(view);
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
			if(mCheckSelectId == 0){
				mUidString="";
				Intent intent = new Intent();
				intent.putExtra("checkId",mCheckSelectId);
				setResult(RESULT_OK, intent);
				AreaActivity.this.finish();
			}else if(mCheckSelectId == 1){
				if(mUserList!=null && mUserList.size()>0){
					for (int j = 0; j < mUserList.size(); j++) {

						if(j!=mUserList.size() -1){
							mUidString +=mUserList.get(j).uid+",";
						}else{
							mUidString +=mUserList.get(j).uid;
						}
					}
					Intent intent = new Intent();
					intent.putExtra("area_uid", mUidString);
					intent.putExtra("userlist",(Serializable)mUserList);
					intent.putExtra("checkId",mCheckSelectId);
					setResult(RESULT_OK, intent);
					AreaActivity.this.finish();
				}else{
					Toast.makeText(mContext,"请选择用户!",Toast.LENGTH_LONG).show();
				}
				
			}
			break;

		default:
			break;
		}
	}

	/*
	 * 子项点击事件
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int arg2,
			long id) {
		if(arg2 < mList.size()){
			if(mList.get(arg2).mType == 0){
				if(mAdapter.getIsDelete()){
					String uid = mList.get(arg2).mLogin.uid;
					mList.remove(arg2);
					if(mUserList!=null && mUserList.size()>0){
						for (int i = 0; i < mUserList.size(); i++) {
							if(uid.equals(mUserList.get(i).uid)){
								mUserList.remove(i);
								break;
							}
						}
					}
			
					if(mAdapter != null){
						mAdapter.notifyDataSetChanged();
					}
				}else {
					if(!mList.get(arg2).mLogin.uid.equals(IMCommon.getUserId(mContext))){
						Intent intent = new Intent(mContext, UserInfoActivity.class);
						intent.putExtra("user", mList.get(arg2).mLogin);
						startActivity(intent);
					}
				}

			}else if(mList.get(arg2).mType == 1){
				if(mAdapter.getIsDelete()){
					mAdapter.setIsDelete(false);
					mAdapter.notifyDataSetChanged();
				}else {
					Intent intent = new Intent(mContext, AddPersonActivity.class);
					intent.putExtra("type", 1);
					if(mUserList !=null &&mUserList.size()>0){
						intent.putExtra("users", (Serializable)mUserList);
					}
					startActivityForResult(intent,GET_USER_REQUEST);
				}

			}else {
				if(mAdapter.getIsDelete()){
					mAdapter.setIsDelete(false);
					mAdapter.notifyDataSetChanged();
				}else {
					mAdapter.setIsDelete(true);
					mAdapter.notifyDataSetChanged();
				}

			}
		}else {
			if(mAdapter.getIsDelete()){
				mAdapter.setIsDelete(false);
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	/*
	 * 子项长按事件
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(arg2 < mUserList.size()){
			if(mList.get(arg2).mType  == 0){
				if(!mAdapter.getIsDelete()){
					mAdapter.setIsShowAddBtn(true);
					mAdapter.setIsDelete(true);
					mAdapter.notifyDataSetChanged();
				}
				return true;
			}
		}

		return false;
	}
	
	/*
	 * 页面回调事件
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case GET_USER_REQUEST://显示选择的用户
			if(resultCode == RESULT_OK){
				List<Login> userList = (List<Login>) data.getSerializableExtra("userlist");

				if(userList != null  && userList.size() != 0){

					int addPos = 0;
					if(mUserList == null || mUserList.size() == 0){
						addPos = 0;
					}else {
						addPos = mUserList.size();
					}

					if(mUserList == null){
						mUserList = new ArrayList<Login>();
					}

					mUserList.addAll(userList);
					for (int i = 0; i < userList.size(); i++) {
						mList.add(i + addPos, new ChatDetailEntity(userList.get(i), 0));
					}
				
					if(mAdapter != null){
						mAdapter.notifyDataSetChanged();
					}else {
						mAdapter = new ChatPersonAdapter(mContext, mList);
						mGridView.setAdapter(mAdapter);
					}
				}

			}
			break;

		}

	}
}


