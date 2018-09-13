package net.smallchat.im.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;

/**
 * 会议管理
 * @author dongli
 *
 */
public class ManagerMeetingActivity extends BaseActivity {

	private Button mCheckApplyMetBtn,mJoinMetUserBtn;

	private int mMeetintId;
	
	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.manager_metting_viwe);
		mMeetintId = getIntent().getIntExtra("met_id",0);
		initCompent();
	}

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,R.string.manager_met);
		mLeftBtn.setOnClickListener(this);

		mCheckApplyMetBtn = (Button)findViewById(R.id.check_apply_met);
		mJoinMetUserBtn = (Button)findViewById(R.id.join_met_user);

		mCheckApplyMetBtn.setOnClickListener(this);
		mJoinMetUserBtn.setOnClickListener(this);

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
			ManagerMeetingActivity.this.finish();
			break;
		case R.id.check_apply_met:
			Intent intent = new Intent();
			intent.setClass(mContext, ApplyMetListActivity.class);
			intent.putExtra("met_id",mMeetintId);
			startActivity(intent);
			break;
		case R.id.join_met_user:
			Intent topIntent = new Intent();
			topIntent.setClass(mContext, ApplyMetListActivity.class);
			topIntent.putExtra("met_id",mMeetintId);
			topIntent.putExtra("type", 1);
			startActivity(topIntent);
		break;

		default:
			break;
		}
	}


}
