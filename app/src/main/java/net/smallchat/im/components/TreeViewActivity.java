package net.smallchat.im.components;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Country;
import net.smallchat.im.R;
import net.smallchat.im.components.treeview.TreeElement;
import net.smallchat.im.components.treeview.TreeElementParser;
import net.smallchat.im.components.treeview.TreeView;
import net.smallchat.im.components.treeview.TreeView.LastLevelItemClickListener;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.MyPullToRefreshListView;
import net.smallchat.im.widget.MyPullToRefreshTreeView;
import net.smallchat.im.widget.MyPullToRefreshTreeView.OnChangeStateListener;

/**
 * 城市列表
 * @author dongli
 *
 */
public class TreeViewActivity extends BaseActivity implements OnChangeStateListener{
	
	private TreeView mTreeView;
	private MyPullToRefreshTreeView mContainer;
	private TextView mRefreshViewLastUpdated;
	private boolean mIsRefreshing = false;
	private int mType;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GlobalParam.SHOW_PROGRESS_DIALOG:
				String dialogMsg = (String) msg.obj;
				showProgressDialog(dialogMsg);
				break;
			case GlobalParam.HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
				break;

			case GlobalParam.SHOW_SCROLLREFRESH:
				if (mIsRefreshing) {
					mContainer.onRefreshComplete();
					break;
				}
				mIsRefreshing = true;
				refreshMenu();
				break;

			case GlobalParam.HIDE_SCROLLREFRESH:
				mIsRefreshing = false;
				mContainer.onRefreshComplete();
				initTreeView();
				break;

			case GlobalParam.MSG_LOAD_ERROR:
				String error_Detail = (String) msg.obj;
				if (error_Detail != null && !error_Detail.equals("")) {
					Toast.makeText(mContext, error_Detail, Toast.LENGTH_LONG)
					.show();
				} else {
					Toast.makeText(mContext, R.string.load_error,
							Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				Toast.makeText(mContext, R.string.network_error,
						Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_TIME_OUT_EXCEPTION:

				String message = (String) msg.obj;
				if (message == null || message.equals("")) {
					message = ChatApplication.getInstance().getResources().getString(R.string.timeout);
				}
				Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
				break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.contact_subject);
		mType = getIntent().getIntExtra("type",GlobleType.TreeViewActivity_City_TYPE );
		initCompent();
	}
	
	private void initCompent(){
		setTitleContent(R.drawable.back_btn, 0, 0);
		
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.titlelayout);
		layout.setVisibility(View.VISIBLE);
		mLeftBtn.setOnClickListener(this);
		
		mRefreshViewLastUpdated = (TextView) findViewById(R.id.pull_to_refresh_time);
		mContainer = (MyPullToRefreshTreeView)findViewById(R.id.container);
		mTreeView = mContainer.getList();
		mTreeView.setLastLevelItemClickCallBack(mItemClickCallBack);// 设置节点点击事件监听
		mContainer.setOnChangeStateListener(this);
		initTreeView();
		
	}

	LastLevelItemClickListener mItemClickCallBack = new LastLevelItemClickListener() {

		@Override
		public void onLastLevelItemClick(int position,
				net.smallchat.im.components.treeview.TreeViewAdapter adapter) {
			if(mType == GlobleType.TreeViewActivity_City_TYPE){
				//addr shengid shiid
				TreeElement element = (TreeElement) adapter.getItem(position);
				if(!element.isHasChild()){
					Intent intent = new Intent();
					intent.putExtra("addr", element.getParentTitle()+" "+element.getTitle());
					intent.putExtra("provice", element.getParentTitle());
					intent.putExtra("city", element.getTitle());
					intent.putExtra("shengid",element.getParentId());
					intent.putExtra("shiid",element.getId());
					setResult(RESULT_OK,intent);
					TreeViewActivity.this.finish();
				}
			}else if(mType == GlobleType.TreeViewActivity_Project_TYPE){//课程
				TreeElement element = (TreeElement) adapter.getItem(position);
				if(!element.isHasChild()){
					Intent intent = new Intent();
					intent.putExtra("subject", element.getTitle());
					intent.putExtra("projectid",element.getId());
					setResult(RESULT_OK,intent);
					TreeViewActivity.this.finish();
				}
			}else if(mType == GlobleType.TreeViewActivity_Subject_TYPE){//行业
				TreeElement element = (TreeElement) adapter.getItem(position);
				if(!element.isHasChild()){
					Intent intent = new Intent();
					intent.putExtra("hangyue", element.getTitle());
					intent.putExtra("subjectid",element.getId());
					setResult(RESULT_OK,intent);
					TreeViewActivity.this.finish();
				}
			}
		
		}// 创建节点点击事件监听

	};
	
	private void refreshMenu(){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			if(mIsRefreshing){
				mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
			}
			return;
		}
		new Thread(){
			public void run() {
				try {
					
					if(mType == GlobleType.TreeViewActivity_City_TYPE){
						ChatApplication.setContryList(IMCommon.getIMServerAPI().getCityAndContryUser());
					}
					mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
				
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					if(mIsRefreshing){
						mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
					}
				}
			};
		}.start();
		
	}
	
	private void initTreeView(){
		titileTextView.setBackgroundColor(Color.parseColor("#00000000"));
		List<TreeElement> treeElements  = null;
		if(mType == GlobleType.TreeViewActivity_City_TYPE){
			titileTextView.setText(mContext.getResources().getString(R.string.select_city));
			if(ChatApplication.getContryList()!=null){
				List<Country>  contry = ChatApplication.getContryList().mCountryList;
				if(contry!= null &&contry.size()>0){
					treeElements = TreeElementParser.getTreeMenuElements(contry);// 解析读出的文件资源内容
				}
			}
			
		}
		if(treeElements!=null && treeElements.size()>0){
			mTreeView.initData(this, treeElements);// 初始化数据
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			TreeViewActivity.this.finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onChangeState(MyPullToRefreshTreeView container, int state) {
		mRefreshViewLastUpdated.setText(FeatureFunction.getRefreshTime());
		switch (state) {
		case MyPullToRefreshListView.STATE_LOADING:
			mHandler.sendEmptyMessage(GlobalParam.SHOW_SCROLLREFRESH);
			break;
		}
	}


}
