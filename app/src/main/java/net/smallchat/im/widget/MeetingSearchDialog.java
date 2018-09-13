package net.smallchat.im.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.meeting.MettingDetailActivity;
import net.smallchat.im.R;
import net.smallchat.im.Entity.MeetingItem;
import net.smallchat.im.adapter.MettingAdapter;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;

public class MeetingSearchDialog extends Dialog implements OnItemClickListener, android.view.View.OnClickListener{
	//  
	private Context mContext;
	private EditText mContentEdit;
	private ListView mListView;
	private TextView mNoValueHint;
	private List<MeetingItem> mUserList = new ArrayList<MeetingItem>();
	private List<MeetingItem> mSearchList = new ArrayList<MeetingItem>();
	private Button mCancleBtn;
	private MettingAdapter mAdapter;
	private int mIsHide; //0-搜索会话表和用户表 1-搜索会议表 2-搜索名称
	private int mFromPage; //1-来自会话详情页面
	private String mToId;
	private int mChatTyp;

	public interface OnFinishClick
	{
		void onFinishListener();
	}


	private OnFinishClick mOnFinishClick;
	public void setOnClearClickLister( OnFinishClick alertDo){
		this.mOnFinishClick = alertDo;
	}

	public MeetingSearchDialog(Context context, List<MeetingItem> userList,int isHide) {
		super(context, R.style.ContentOverlay);
		mContext = context;
		mUserList = userList;
		mIsHide = isHide;
	}

	public MeetingSearchDialog(Context context, List<MeetingItem> userList) {
		super(context, R.style.ContentOverlay);
		mContext = context;
		mUserList = userList;
	}
	public MeetingSearchDialog(Context context,int isHide) {
		super(context, R.style.ContentOverlay);
		mContext = context;
		this.mIsHide = isHide;
	}

	public MeetingSearchDialog(Context context,int isHide, OnFinishClick alertDo,int frompage,
			String toId,int chatType) {
		super(context, R.style.ContentOverlay);
		mContext = context;
		this.mIsHide = isHide;
		this.mOnFinishClick = alertDo;
		mFromPage = frompage;
		mToId = toId;
		mChatTyp = chatType;
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
		mNoValueHint = (TextView)findViewById(R.id.no_value_hint);
		mSearchList = new ArrayList<MeetingItem>();
		mContentEdit = (EditText) findViewById(R.id.searchcontent);
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
				//if (s.toString() != null && !s.toString().equals("")) {
				getSearchData(s.toString());
				//}
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


	private void getSearchData(final String inputContent){
		new Thread(){
			public void run() {

				List<MeetingItem> tempList = null;
				IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_LOAD_DATA,tempList);
				if(inputContent!=null && !inputContent.equals("")){
					/*IMCommon.sendMsg(mHandler,GlobalParam.SHOW_PROGRESS_DIALOG, 
							mContext.getResources().getString(R.string.refreshloading));*/
					tempList = new ArrayList<MeetingItem>();
					if(mIsHide == 3){
						if(mUserList!=null && mUserList.size()>0){
							for (int i = 0; i < mUserList.size(); i++) {
								String name = mUserList.get(i).metName;
								if((name!=null && !name.equals("")) && name.contains(inputContent)){
									mUserList.get(i).searchContent = inputContent;
									tempList.add(mUserList.get(i));
								}
							}
						}
					}
					/*	mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);*/
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_LOAD_DATA,tempList);
				}


			};
		}.start();
	}

	private void updateListView() {

		/*	if(mSearchList != null && mSearchList.size() != 0){
			mListView.setVisibility(View.VISIBLE);
		}*/

		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
			return;
		}

		if (mSearchList != null) {
			mAdapter = new MettingAdapter(mContext, mSearchList,null);
			mListView.setAdapter(mAdapter);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		Intent metDetailIntent = new Intent();
		metDetailIntent.setClass(mContext, MettingDetailActivity.class);
		metDetailIntent.putExtra("met_id",mSearchList.get(arg2).id);
		mContext.startActivity(metDetailIntent);

		MeetingSearchDialog.this.dismiss();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancle_btn:
			if(mOnFinishClick!=null){
				mOnFinishClick.onFinishListener();
			}
			hideKeyBoard();
			MeetingSearchDialog.this.dismiss();
			break;

		default:
			break;
		}

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
				if(mOnFinishClick!=null){
					mOnFinishClick.onFinishListener();
				}
				hideKeyBoard();
				MeetingSearchDialog.this.dismiss();
			}
		}  
		return super.onTouchEvent(event);
	}




	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if(mSearchList == null || mSearchList.size()<=0){
				if(mOnFinishClick!=null){
					mOnFinishClick.onFinishListener();
				}
				MeetingSearchDialog.this.dismiss();
			}
		}
		return super.dispatchKeyEvent(event);
	}



	protected CustomProgressDialog mProgressDialog;

	public Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.SHOW_PROGRESS_DIALOG:
				String dialogMsg = (String)msg.obj;
				showProgressDialog(dialogMsg);
				break;
			case GlobalParam.HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
				String hintMsg = (String)msg.obj;
				if(hintMsg!=null && !hintMsg.equals("")){
					Toast.makeText(mContext,hintMsg,Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_SHOW_LOAD_DATA:
				if(mSearchList!=null && mSearchList.size()>0){
					mSearchList.clear();
					if(mAdapter!=null){
						mAdapter.notifyDataSetChanged();
					}
				}
				List<MeetingItem> tempList = (List<MeetingItem>)msg.obj;
				if(tempList!=null && tempList.size()>0){
					mSearchList.addAll(tempList);
					updateListView();
				}

				if(mSearchList!=null && mSearchList.size()>0){
					mListView.setVisibility(View.VISIBLE);
					mNoValueHint.setVisibility(View.GONE);
				}else{
					mListView.setVisibility(View.GONE);
					if((mToId!=null && !mToId.equals(""))
							&& mChatTyp !=0){
						mNoValueHint.setVisibility(View.VISIBLE);
					}
				}

				break;
			}
		}
	};

	public void showProgressDialog(String msg,Context context){
		mProgressDialog = new CustomProgressDialog(mContext);
		mProgressDialog.setMessage(msg);
		mProgressDialog.show();
	}
	public void showProgressDialog(String msg){
		mProgressDialog = new CustomProgressDialog(mContext);
		mProgressDialog.setMessage(msg);
		mProgressDialog.show();
	}

	public void hideProgressDialog(){
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
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
