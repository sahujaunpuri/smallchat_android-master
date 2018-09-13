package net.smallchat.im.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import net.smallchat.im.R;
import net.smallchat.im.room.RoomDetailActivity;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.adapter.SearchUserListAdapter;
import net.smallchat.im.components.sortlist.ClearEditText;

public class SearchDialog extends Dialog implements OnItemClickListener, android.view.View.OnClickListener{
	
	private Context mContext;
	private ClearEditText mContentEdit;
	private ListView mListView;
	private List<Login> mUserList;
	private List<Login> mSearchList;
	private Button mCancleBtn;
	private SearchUserListAdapter mAdapter;
	private int mIsHide;
	public SearchDialog(Context context, List<Login> userList,int isHide) {
		super(context, R.style.ContentOverlay);
		mContext = context;
		mUserList = userList;
		mIsHide = isHide;
	}
	
	public SearchDialog(Context context, List<Login> userList) {
		super(context, R.style.ContentOverlay);
		mContext = context;
		mUserList = userList;
	}
	public SearchDialog(Context context) {
		super(context, R.style.ContentOverlay);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_search_dialog);
		initComponent();
	}

	private void initComponent() {
		mCancleBtn = (Button)findViewById(R.id.cancle_btn);
		mCancleBtn.setOnClickListener(this);
		mSearchList = new ArrayList<Login>();
		mContentEdit = (ClearEditText) findViewById(R.id.searchcontent);
		 mContentEdit.setOnClearClickLister(new ClearEditText.OnClearClick() {
			
			@Override
			public void onClearListener() {
				
			}
		});
		
		mContentEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(mUserList == null || mUserList.size()<=0){
					return;
				}
				List<Login> tempList = new ArrayList<Login>();
				if (s.toString() != null && !s.toString().equals("")) {
					
					for (int i = 0; i < mUserList.size(); i++) {
						if ( mUserList.get(i).nickname.contains(s.toString())) {
							tempList.add(mUserList.get(i));
						}
					}

				}
				
				if (mSearchList != null) {
					mSearchList.clear();
				}
				
				mSearchList.addAll(tempList);
				updateListView();
				if(tempList == null || tempList.size()>0){
					mListView.setVisibility(View.VISIBLE);
				}else{
					mListView.setVisibility(View.GONE);
				}
			}
		});

		mListView = (ListView) findViewById(R.id.contact_list);
		mListView.setVisibility(View.GONE);
		mListView.setDivider(null);
		mListView.setCacheColorHint(0);
		mListView.setOnItemClickListener(this);
		mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));
		/*mListView.setOnTouchListener(this);*/
	}
	
	private void updateListView() {
		
		if(mSearchList != null && mSearchList.size() != 0){
			mListView.setVisibility(View.VISIBLE);
		}
		
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
			return;
		}

		if (mSearchList != null) {
			mAdapter = new SearchUserListAdapter(mContext, mSearchList,mIsHide);
			mListView.setAdapter(mAdapter);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(mSearchList.get(arg2).mIsRoom!=100){
			Intent intent = new Intent(mContext, RoomDetailActivity.class);
			intent.putExtra("room", mSearchList.get(arg2).room);
			intent.putExtra("groupurl", mSearchList.get(arg2).headSmall);
			mContext.startActivity(intent);
		}else{
			Intent intent = new Intent(mContext, UserInfoActivity.class);
			intent.putExtra("user", mSearchList.get(arg2));
			intent.putExtra("type", 1);
			mContext.startActivity(intent);
		}
		
		
		SearchDialog.this.dismiss();
	}
	@Override
	public void onClick(View v) {
		hideKeyBoard();
		SearchDialog.this.dismiss();
	}

	
	private void hideKeyBoard(){
		if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
			InputMethodManager manager= (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
		}  
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){  
			if(mSearchList == null || mSearchList.size()<=0){
				hideKeyBoard();
				SearchDialog.this.dismiss();
			}
		}  
		return super.onTouchEvent(event);
	}
	
	
	
	
	/*@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){  
			if(mSearchList == null || mSearchList.size()<=0){
				hideKeyBoard();
				SearchDialog.this.dismiss();
			}
		}  
		return super.onTouchEvent(event);  
	}*/
}
