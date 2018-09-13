package net.smallchat.im.about;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.R;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/*
 * 举报页面
 */
public class ReportActivity extends BaseActivity implements OnClickListener{

	private EditText mReportedContentText;
	private TextView mOrderHint,mSelectType;

	private LinearLayout mTypeLayout;

	private String mFuid;
	private int mType;
	private String[] mArrayList;
	private String mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.reported_page);
		initComponent();
	}

	private void initComponent(){

		mFuid = getIntent().getStringExtra("fuid");
		mType = getIntent().getIntExtra("type",GlobleType.REPORTED_USER_TYPE );

		setTitleContent(R.drawable.back_btn, R.drawable.language_choosed, R.string.reported);
		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);

		mReportedContentText = (EditText) findViewById(R.id.reported);
		mReportedContentText.setHint(mContext.getResources().getString(R.string.reported_content));

		mOrderHint = (TextView)findViewById(R.id.text_hint);
		mSelectType = (TextView)findViewById(R.id.select_type);
		mTypeLayout = (LinearLayout)findViewById(R.id.reported_type_layout);
		if(mType == GlobleType.REPORTED_USER_TYPE){
			mReportedContentText.setVisibility(View.VISIBLE);
			mOrderHint.setVisibility(View.GONE);
			mSelectType.setVisibility(View.GONE);
			mTypeLayout.setVisibility(View.GONE);
		}else if(mType == GlobleType.REPORTED_SUB_TYPE){
			mArrayList = mContext.getResources().getStringArray(R.array.reported_item);
			mReportedContentText.setVisibility(View.GONE);
			mOrderHint.setVisibility(View.VISIBLE);
			mSelectType.setVisibility(View.VISIBLE);
			mTypeLayout.setVisibility(View.VISIBLE);
			showitem();
		}

	}
	
	
	private void showitem() {
		if (mArrayList != null) {
			mTypeLayout.removeAllViews();

			int size = mArrayList.length;
			if (size > 0) {
				 LayoutInflater Inflater = (LayoutInflater)mContext.getSystemService(
				            Context.LAYOUT_INFLATER_SERVICE);
				for (int i = 0; i < size; i++) {
					 View view = Inflater.inflate(R.layout.reported_type_item,null);
					TextView title = (TextView)view.findViewById(R.id.title);
					title.setText(mArrayList[i]);
					final RelativeLayout layout = (RelativeLayout)view.findViewById(R.id.other_layout);
					if(i == 0){
						view.findViewById(R.id.other_check).setVisibility(View.VISIBLE);
						mContent = mArrayList[i];
					}
					final int index = i;
					layout.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							for (int j = 0; j < mTypeLayout.getChildCount(); j++) {
								Log.e("ReportActivity", "j:"+j+"index:"+index);
								ImageView checkView = (ImageView)mTypeLayout.getChildAt(j).findViewById(R.id.other_check);
								if(j == index){
									mContent = mArrayList[j];
									checkView.setVisibility(View.VISIBLE);
								}else {
									checkView.setVisibility(View.GONE);
								}
							}
						}
					});
					mTypeLayout.addView(view);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			this.finish();
			break;

		case R.id.right_btn:
			if(mType == GlobleType.REPORTED_USER_TYPE){
				mContent = mReportedContentText.getText().toString().trim();
				if(mContent.equals("")){
					Toast.makeText(mContext, mContext.getResources().getString(R.string.input_reported_content), Toast.LENGTH_SHORT).show();
					return;
				}
			}else if(mType == GlobleType.REPORTED_SUB_TYPE){
				if(mContent.equals("")){
					Toast.makeText(mContext, mContext.getResources().getString(R.string.select_reported_content), Toast.LENGTH_SHORT).show();
					return;
				}
			}
			
			
			Message message = new Message();
			message.obj = mContext.getResources().getString(R.string.add_more_loading);
			message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
			mHandler.sendMessage(message);

			reportedFriend(mContent);
			break;

		default:
			break;
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
				Toast.makeText(mContext,mContext.getResources().getString(R.string.reported_friend_success),Toast.LENGTH_LONG).show();
				
				ReportActivity.this.finish();
				break;

			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				String hintMsg = (String)msg.obj;
				if(hintMsg ==null && hintMsg.equals("")){
					hintMsg =mContext.getResources().getString(R.string.reported_friend_failed);
				}
				Toast.makeText(mContext,hintMsg,Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				hideProgressDialog();
				Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_TIME_OUT_EXCEPTION:
				hideProgressDialog();
				String message=(String)msg.obj;
				if (message==null || message.equals("")) {
					message=mContext.getResources().getString(R.string.timeout);
				}
				Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
				break;
			}
		}
	};


	/*
	 * 举报
	 */
	private void reportedFriend(final String content){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().reportedFriend(mFuid, content,mType);
						if(state != null && state.code == 0){
							if(state.code == 0){
								mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
							}else if(state.code == 1){
								IMCommon.sendMsg(mHandler, GlobalParam.MSG_LOAD_ERROR,state.errorMsg);
							}
						}else {
							
							mHandler.sendEmptyMessage(GlobalParam.MSG_LOAD_ERROR);
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

}
