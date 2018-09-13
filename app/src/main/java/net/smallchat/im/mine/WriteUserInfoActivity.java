package net.smallchat.im.mine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobleType;

/**
 * 填写资料数据
 * @author dongli
 *
 */
public class WriteUserInfoActivity extends BaseActivity implements OnClickListener{

	private EditText mNameText;
	private RadioGroup mSexGroup;

	private String mContent;
	private int mType,mSexType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.remark_friend);
		initComponent();
	}

	/**
	 * 初始化控件
	 */
	private void initComponent(){
		mContent = getIntent().getStringExtra("content");
		mType = getIntent().getIntExtra("type",0); 
		mSexType = getIntent().getIntExtra("sextype",0);
		if(mType == GlobleType.MODIFY_GROUP_INFO ){
			setTitleContent(R.drawable.back_btn, R.drawable.ok_btn, R.string.group_chat_name);
		}else if(mType == GlobleType.MODIFY_GROUP_NICKNAME){
			setTitleContent(R.drawable.back_btn, R.drawable.ok_btn, R.string.group_chat_my_nickname);
		}
		else{
			setTitleContent(R.drawable.back_btn, R.drawable.ok_btn, 0);
		}
		titileTextView.setBackgroundColor(Color.parseColor("#00000000"));

		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);

		mNameText = (EditText) findViewById(R.id.markname);

		mNameText = (EditText) findViewById(R.id.markname);
		mNameText.setFocusable(true); 
		mNameText.setFocusableInTouchMode(true); 
		mNameText.requestFocus(); 


		if(mType == GlobleType.COMPLETE_SEX){
			titileTextView.setText(mContext.getResources().getString(R.string.sex));
			mNameText.setHint(mContext.getResources().getString(R.string.sex));
		}else if(mType == GlobleType.COMPLETE_EMAIL){
			titileTextView.setText(mContext.getResources().getString(R.string.profile_email));//
			mNameText.setHint(mContext.getResources().getString(R.string.profile_email));
		}else if(mType == GlobleType.COMPLETE_COMPANY){
			titileTextView.setText(mContext.getResources().getString(R.string.compnay));//
			mNameText.setHint(mContext.getResources().getString(R.string.compnay));
		}else if(mType == GlobleType.COMPLETE_SIGN){
			mNameText.setSingleLine(false);
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, FeatureFunction.dip2px(mContext,120));
			int margin = FeatureFunction.dip2px(mContext, 10);
			param.setMargins(margin, margin,margin, 0);
			mNameText.setGravity(Gravity.TOP|Gravity.LEFT);
			mNameText.setPadding(10,10,10,10);
			mNameText.setLayoutParams(param);
			titileTextView.setText(mContext.getResources().getString(R.string.what_up));//
			mNameText.setHint(mContext.getResources().getString(R.string.what_up));
		}else if(mType == GlobleType.COMPLETE_NICKNAME){
			titileTextView.setText(mContext.getResources().getString(R.string.nickname));//
			mNameText.setHint(mContext.getResources().getString(R.string.nickname));
		}else if(mType == GlobleType.COMPLETE_MET_TITLE){
			titileTextView.setText(mContext.getResources().getString(R.string.metting_title));//
			mNameText.setHint(mContext.getResources().getString(R.string.metting_title));
		}

		if(mContent!=null && !mContent.equals("")){
			mNameText.setText(mContent);
			mNameText.setSelection(mContent.length());
		}

		mSexGroup = (RadioGroup)findViewById(R.id.sex_group);
		mSexGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.manl:
					mSexType = 0;
					break;
				case R.id.femanl:
					mSexType = 1;
					break;
				case R.id.center_sex:
					mSexType = 2;
					break;

				default:
					break;
				}

			}
		});
		if(mType == GlobleType.COMPLETE_SEX ){
			mNameText.setVisibility(View.GONE);
			mSexGroup.setVisibility(View.VISIBLE);
			if(mSexType == 0){
				mSexGroup.check(R.id.manl);
			}else if(mSexType == 1){
				mSexGroup.check(R.id.femanl);
			}else if(mSexType == 2){
				mSexGroup.check(R.id.center_sex);
			}

		}else{
			mNameText.setVisibility(View.VISIBLE);
			mSexGroup.setVisibility(View.GONE);
		}

		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				InputMethodManager inputManager = 
						(InputMethodManager)mNameText.
						getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
				inputManager.showSoftInput(mNameText, 0);
			}
		}, 100);



	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			this.finish();
			if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
				InputMethodManager manager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
			}  
			break;

		case R.id.right_btn:
			if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
				InputMethodManager manager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
			}  
			String content = mNameText.getText().toString().trim();
			if(mType == GlobleType.COMPLETE_ADDR || mType == GlobleType.COMPLETE_COMPANY
					|| mType == GlobleType.COMPLETE_EMAIL || mType == GlobleType.MODIFY_GROUP_INFO
					|| mType == GlobleType.COMPLETE_NICKNAME || mType == GlobleType.MODIFY_GROUP_NICKNAME){
				if(content.equals("")){
					Toast.makeText(mContext, mContext.getResources().getString(R.string.vlaue_is_null), Toast.LENGTH_SHORT).show();
					return;
				}
			}
			Intent intent = new Intent();
			if(mType == GlobleType.COMPLETE_NICKNAME){
				if(content!=null && !content.equals("")){
					if( content.length()>=2 && content.length()<=8){
						intent.putExtra("nickname", content);
					}else{
						Toast.makeText(mContext, mContext.getResources().
								getString(R.string.nickname_length_hint),Toast.LENGTH_LONG).show();
						return;
					}
				}

			}else if(mType == GlobleType.COMPLETE_SEX){
				intent.putExtra("sex", mSexType);
			}else if(mType == GlobleType.COMPLETE_ADDR){
				intent.putExtra("addr", content);
			}else if(mType == GlobleType.COMPLETE_COMPANY){
				intent.putExtra("company", content);
			}else if(mType == GlobleType.COMPLETE_EMAIL){
				intent.putExtra("email", content);
			}else if(mType == GlobleType.COMPLETE_HANGYUE){
				if(content!=null && !content.equals("")){
					intent.putExtra("hangyue", content);
				}
			}else if(mType == GlobleType.COMPLETE_SIGN){
				if(content!=null && !content.equals("")){
					if(content.length()<=30){
						intent.putExtra("sign", content);
					}else{
						Toast.makeText(mContext, mContext.getResources().
								getString(R.string.sign_length_hint),Toast.LENGTH_LONG).show();
						return;
					}

				}else{
					intent.putExtra("sign", "");
				}
				
			}else if(mType == GlobleType.COMPLETE_SUBJECT){
				if(content!=null && !content.equals("")){
					intent.putExtra("subject", content);
				}
			}else if(mType == GlobleType.MODIFY_GROUP_INFO){
				if(content!=null && !content.equals("")){
					if( content.length()>=2 && content.length()<=8){
						intent.putExtra("group_name", content);
					}else{
						Toast.makeText(mContext, mContext.getResources().
								getString(R.string.group_chat_name_length_hint),Toast.LENGTH_LONG).show();
						return;
					}
				}
			}else if(mType == GlobleType.MODIFY_GROUP_NICKNAME){
				if(content!=null && !content.equals("")){
					if( content.length()>=2 && content.length()<=8){
						intent.putExtra("group_nick_name", content);
					}else{
						Toast.makeText(mContext, mContext.getResources().
								getString(R.string.group_chat_nickname_length_hint),Toast.LENGTH_LONG).show();
						return;
					}
				}
			}else if(mType == GlobleType.COMPLETE_MET_TITLE){
				if(content!=null && !content.equals("")){
					if(2<=content.length() && content.length()<=15){
						intent.putExtra("met_title", content);
					}else{
						Toast.makeText(mContext, mContext.getResources().
								getString(R.string.met_title_check_hint),Toast.LENGTH_LONG).show();
						return;
					}

				}
			}
			setResult(RESULT_OK, intent);
			WriteUserInfoActivity.this.finish();
			break;

		default:
			break;
		}
	}






}

