package net.smallchat.im.album;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.FriendsLoop;
import net.smallchat.im.Entity.FriendsLoopItem;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.LoginResult;
import net.smallchat.im.Entity.MorePicture;
import net.smallchat.im.Entity.PopItem;
import net.smallchat.im.mine.MyMovingMessageListActivity;
import net.smallchat.im.R;
import net.smallchat.im.components.RotateImageActivity;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.adapter.MyAlbumAdpater;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.friendcircle.FriendCircleActivity;
import net.smallchat.im.friendcircle.SendFriendCircleActivity;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.MyPullToRefreshListView;
import net.smallchat.im.widget.PopWindows;
import net.smallchat.im.widget.RoundImageView;
import net.smallchat.im.widget.MyPullToRefreshListView.OnChangeStateListener;
import net.smallchat.im.widget.PopWindows.PopWindowsInterface;

/**
 * 我的相册
 * @author dongli
 *
 */
public class MyAlbumActivity extends BaseActivity implements OnClickListener, RecyclerListener, OnChangeStateListener{

	/*
	 * 定义全局变量
	 */
	public static final int MSG_SHOW_IMAGE = 0x00023;
	public static final int MSG_SHOW_PROFILE_HEADER= 0x00024;
	private static final int REQUEST_GET_URI = 101;
	public static final int REQUEST_GET_BITMAP = 102;
	public static final int REQUEST_GET_IMAGE_BY_CAMERA = 103;

	private View mSearchHeader;
	private RelativeLayout mTitleLayout;

	private MyPullToRefreshListView mContainer;
	private TextView mRefreshViewLastUpdated;
	private LinearLayout mCategoryLinear;
	private boolean mIsRefreshing = false;
	private ListView mListView;


	private ImageView mHeaderBg,mPicBtn;
	private TextView mSysDataBtn,mUserNameText,mSignText;
	private RoundImageView mHeaderIcon;
	private LinearLayout mFootView;
	private DisplayMetrics mMetric;

	private ImageLoader mImageLoader;

	private FriendsLoop mMyAlbum;
	private List<FriendsLoopItem> mDataList = new ArrayList<FriendsLoopItem>();
	private MyAlbumAdpater mAdapter;


	private List<MorePicture> mListpic = new ArrayList<MorePicture>(); 
	private String mToUserID;
	private int mType;
	private Bitmap mBitmap;	

	private Login mLogin;
	private String mHeadUrl;

	private static  int ICON_SIZE_HEIGHT ;
	private static  int ICON_SIZE_WIDTH ;

	private String mCropImgPath;
	private String mFrontCover;
	private String TEMP_FILE_NAME="moving.jpg";


	private List<PopItem> mPopList = new ArrayList<PopItem>();
	private PopWindows mPopWindows;

	
	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_SHOW_SELECT_BG_DIALOG:
				selectImg();
				break;
			case GlobalParam.SHOW_SCROLLREFRESH:
				if (mIsRefreshing) {
					mContainer.onRefreshComplete();
					break;
				}
				if(mMyAlbum!=null){
					mMyAlbum = null;
				}
				mIsRefreshing = true;
				getLoopData(GlobalParam.LIST_LOAD_REFERSH);
				break;

			case GlobalParam.HIDE_SCROLLREFRESH:
				mIsRefreshing = false;
				mContainer.onRefreshComplete();
				updateListView();
				break;
			case GlobalParam.MSG_SHOW_HEADER_IMG://显示背景图
				if(mHeaderIcon!=null){
					mHeaderIcon.setImageBitmap(null);
					if (mHeadUrl!=null && !mHeadUrl.equals("")) {
						mHeaderIcon.setImageResource(R.drawable.contact_default_header);
						mHeaderIcon.setTag(mHeadUrl);
						mImageLoader.getBitmap(mContext, mHeaderIcon, null,mHeadUrl, 0,true,false);
					}else{
						mHeaderIcon.setImageResource(R.drawable.contact_default_header);
					}
				}
				if(mLogin !=null){
					mUserNameText.setText(mLogin.nickname);
					mSignText.setText(mLogin.sign);
				}
				break;
			case GlobalParam.MSG_UPLOAD_STATUS:
				IMResponseState returnStatus = (IMResponseState)msg.obj;
				if (returnStatus == null) {
					Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
					return;
				}
				if (returnStatus.code !=0) {
					Toast.makeText(mContext, returnStatus.errorMsg,Toast.LENGTH_LONG).show();
					return;
				}
				mFrontCover = returnStatus.frontCover;
			case GlobalParam.MSG_CHECK_STATE://没有更多数据移除footview
				hideProgressDialog();
				if (mFootView != null && mListView.getFooterViewsCount()>0) {
					mListView.removeFooterView(mFootView); 
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case GlobalParam.MSG_SHOW_LISTVIEW_DATA:

				updateListView();
				break;
			case GlobalParam.LIST_LOAD_REFERSH:
				mMyAlbum = null;
				Handler handler = new Handler();
				handler.postDelayed(new Runnable(){
					@Override
					public void run() {
						getLoopData(GlobalParam.LIST_LOAD_REFERSH);
					}
				}, 2000);
				break;
			case MSG_SHOW_IMAGE:
				if (mBitmap!=null) {
					mHeaderBg.setImageBitmap(mBitmap);
				}
				showModifybgDialog();
				break;
			case MSG_SHOW_PROFILE_HEADER:
				if(mLogin!=null ){
					mImageLoader.getBitmap(mContext, mHeaderBg, null,mLogin.cover, 0,true,false);
				}
				break;
			case GlobalParam.HIDE_LOADINGMORE_INDECATOR:

				if (mListView.getFooterViewsCount() != 0) {
					mListView.removeFooterView(mFootView);
				}
				if (mAdapter != null){
					mAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}
	};


	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_album_view);
		mContext = this;
		mToUserID = getIntent().getStringExtra("toUserID");
		if(mToUserID == null || mToUserID.equals("")){
			mToUserID = IMCommon.getUserId(mContext);
		}
		mImageLoader = new ImageLoader();
		mCropImgPath =FeatureFunction.PUB_TEMP_DIRECTORY+"temp.jpg";
		mMetric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mMetric);
		ICON_SIZE_WIDTH =mMetric.widthPixels;
		if(ICON_SIZE_WIDTH>640){
			ICON_SIZE_WIDTH = 640;
		}
		ICON_SIZE_HEIGHT = (ICON_SIZE_WIDTH/3)*2;//x:y 3:2
		initCompent();
		registerReceiver();
		getLoopData(GlobalParam.LIST_LOAD_FIRST);
	}
	
	/*
	 * 注册通知
	 */
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_REFRESH_WEIBO_COUNT);
		filter.addAction(GlobalParam.ACTION_COLLECTION_WEIBO);
		filter.addAction(GlobalParam.ACTION_LIKE_WEIBO);
		filter.addAction(FriendCircleActivity.MSG_REFRESH_MOVIINF);
		registerReceiver(broadcaset, filter);
	}


	/** 处理通知 */
	private BroadcastReceiver broadcaset = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(GlobalParam.ACTION_REFRESH_WEIBO_COUNT)){

			}else if(action.equals(FriendCircleActivity.MSG_REFRESH_MOVIINF)){
				if(mMyAlbum!=null){
					mMyAlbum = null;
				}
				getLoopData(GlobalParam.LIST_LOAD_FIRST);
			}
		}
	};

	
	/*
	 * 实例化控件
	 */
	private void initCompent(){
		if(mToUserID.equals(IMCommon.getUserId(mContext))){
			setTitleContent(R.drawable.back_btn, R.drawable.more_btn, R.string.my_album);
			mRightBtn.setOnClickListener(this);
		}else{
			setTitleContent(R.drawable.back_btn, 0, R.string.my_album);
		}
		

		mTitleLayout = (RelativeLayout)findViewById(R.id.title_layout);
		mTitleLayout.setOnClickListener(this);
		

		String[] itemMenuBg = mContext.getResources().getStringArray(R.array.my_album_item);
		for (int i = 0; i < itemMenuBg.length; i++) {
			mPopList.add(new PopItem(i+1, itemMenuBg[i],null));
		}

		mPopWindows = new PopWindows(mContext, mPopList, mTitleLayout, new PopWindowsInterface() {

			@Override
			public void onItemClick(int position, View view) {
				switch (position) {
				case 1://消息列表
					Intent intent = new Intent();
					intent.setClass(mContext, MyMovingMessageListActivity.class);
					startActivity(intent);
					break;
				default:
					break;
				}
			}
		});

		mLeftBtn.setOnClickListener(this);
		
		mCategoryLinear = (LinearLayout)findViewById(R.id.category_linear);
		mRefreshViewLastUpdated = (TextView) findViewById(R.id.pull_to_refresh_time);
		mContainer = (MyPullToRefreshListView) findViewById(R.id.container);
		mListView = mContainer.getList();

		mListView.setDivider(null);
		mListView.setCacheColorHint(0);
		mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));
		mListView.setHeaderDividersEnabled(false);
		mListView.setRecyclerListener(this);
		mContainer.setOnChangeStateListener(this);
		if (mListView.getHeaderViewsCount()==0) {
			mSearchHeader=LayoutInflater.from(this).inflate(R.layout.friend_circle_header,null);
			mHeaderIcon = (RoundImageView)mSearchHeader.findViewById(R.id.header_icon);
			mHeaderBg = (ImageView)mSearchHeader.findViewById(R.id.img_bg);
			RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, ICON_SIZE_HEIGHT);
			mHeaderBg.setLayoutParams(params);
			

			mUserNameText = (TextView)mSearchHeader.findViewById(R.id.login_user_name);
			mSignText = (TextView)mSearchHeader.findViewById(R.id.sign);
			RelativeLayout.LayoutParams param  = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ICON_SIZE_WIDTH*144/217);
			mHeaderBg.setLayoutParams(param);
			mHeaderIcon.setOnClickListener(this);
			if(mToUserID!=null && !mToUserID.equals("") && mToUserID.equals(IMCommon.getUserId(mContext))){
				mHeaderBg.setOnClickListener(this);
			}

			mListView.addHeaderView(mSearchHeader);

		}

		mListView.setOnScrollListener(new OnScrollListener() {

			/**
			 * Callback method to be invoked while the list view or grid view is being scrolled. If the
			 * view is being scrolled, this method will be called before the next frame of the scroll is
			 * rendered. In particular, it will be called before any calls to
			 * {@link Adapter#getView(int, View, ViewGroup)}.
			 *
			 * @param view The view whose scroll state is being reported
			 *
			 * @param scrollState The current scroll state.
			 *  One of {@link #SCROLL_STATE_IDLE},//已经停止 0
			 * {@link #SCROLL_STATE_TOUCH_SCROLL} //正在滚动 1
			 *  or {@link #SCROLL_STATE_FLING}. //开始滚动  2
			 */
			boolean isLastRow = false;
			boolean isFirstRow = false;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				//正在滚动时回调，回调2-3次，手指没抛则回调2次。scrollState = 2的这次不回调
				//回调顺序如下
				//第1次：scrollState = SCROLL_STATE_TOUCH_SCROLL(1) 正在滚动
				//第2次：scrollState = SCROLL_STATE_FLING(2) 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
				//第3次：scrollState = SCROLL_STATE_IDLE(0) 停止滚动    

				if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
					mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_HEADER_IMG);
				}

				//当滚到最后一行且停止滚动时，执行加载
				if (view.getLastVisiblePosition() == mDataList.size()  && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					//Log.e("FriendsLoopActivity-onScrollStateChanged", "加载更多数据");
					boolean isLoadMore = (mMyAlbum!=null && mMyAlbum.pageInfo!=null 
							&& mMyAlbum.pageInfo.currentPage == mMyAlbum.pageInfo.totalPage)?false:true;
					if (isLoadMore) {
						if (mFootView == null) {
							mFootView = (LinearLayout) LayoutInflater.from(mContext)
									.inflate(R.layout.hometab_listview_footer, null);
						}

						ProgressBar pb = (ProgressBar)mFootView.findViewById(R.id.hometab_addmore_progressbar);
						pb.setVisibility(View.VISIBLE);		 		
						TextView more = (TextView)mFootView.findViewById(R.id.hometab_footer_text);
						more.setText(mContext.getString(R.string.add_more_loading));
						if (mListView.getFooterViewsCount() == 0) {
							mListView.addFooterView(mFootView);	
						}

						Log.e("load_more","true");
						getLoopData(GlobalParam.LIST_LOAD_MORE);
						isLastRow = false;
					}

				}
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getFirstVisiblePosition() == 0) {


				}
			}


			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//判断是否滚到最后一行
				/*if (firstVisibleItem + visibleItemCount == totalItemCount
						&& totalItemCount>0) {
					//Log.e("FriendsLoopActivity-onScroll","滚到最后一行");
					isLastRow = true;
				}*/
			}
		});
		
		mAdapter = new MyAlbumAdpater(mContext, mDataList, mHandler,mMetric,mToUserID);
		mListView.setAdapter(mAdapter);
	}

	/*
	 * 获取我的相册数据
	 */
	private void getLoopData(final int loadType){
		if (!IMCommon.getNetWorkState()) {
			switch (loadType) {
			case GlobalParam.LIST_LOAD_FIRST:
				mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
				break;

			case GlobalParam.LIST_LOAD_MORE:
				mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
				break;
			case GlobalParam.LIST_LOAD_REFERSH:
				mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
				break;

			default:
				break;
			}
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					if (loadType == GlobalParam.LIST_LOAD_FIRST) {
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
								mContext.getResources().getString(R.string.get_dataing));
						LoginResult loginResult = IMCommon.getIMServerAPI().getUserInfo(mToUserID);
						if (loginResult!=null && loginResult.mState.code == 0) {
							mLogin = loginResult.mLogin;
							mHeadUrl = mLogin.headSmall;
							mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_HEADER_IMG);
						}
					}
					boolean isExitsData = true;
					if (mMyAlbum!=null && mMyAlbum.pageInfo.currentPage == mMyAlbum.pageInfo.totalPage) {
						isExitsData = false;
					}
					int page = 0;
					if (loadType == GlobalParam.LIST_LOAD_FIRST) {
						page = 1;
					}else if(loadType == GlobalParam.LIST_LOAD_MORE){
						page = mMyAlbum.pageInfo.currentPage+1;
					}
					if (isExitsData) {
						//mMoving = QiyueCommon.getQiyueInfo().getMovingData(QiyueCommon.getUserId(mContext),page);
						mMyAlbum = IMCommon.getIMServerAPI().myHomeList(page,mToUserID);
						if ((loadType == GlobalParam.LIST_LOAD_FIRST || loadType == GlobalParam.LIST_LOAD_REFERSH)
								&& mDataList!=null && mDataList.size()>0) {
							mDataList.clear();
						}
						if (mMyAlbum != null && mMyAlbum.childList!=null && mMyAlbum.childList.size() > 0) {
							isExitsData = true;
							mDataList.addAll(mMyAlbum.childList); 
						} else{
							if(mToUserID.equals(IMCommon.getUserId(mContext))){
							
								if(mDataList.size()<=0){
									mDataList.add(new FriendsLoopItem(System.currentTimeMillis()/1000));
								}
							}
							isExitsData = false;
						}
						
					}

					if (loadType == GlobalParam.LIST_LOAD_FIRST) {
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}

					switch (loadType) {
					case GlobalParam.LIST_LOAD_FIRST:
						mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_LISTVIEW_DATA);
						mHandler.sendEmptyMessage(MSG_SHOW_PROFILE_HEADER);
						break;

					case GlobalParam.LIST_LOAD_MORE:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
						break;
					case GlobalParam.LIST_LOAD_REFERSH:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						break;

					default:
						break;
					}
					if (!isExitsData) {
						mHandler.sendEmptyMessage(GlobalParam.MSG_CHECK_STATE);
					}

				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_NETWORK_ERROR, 
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					switch (loadType) {
					case GlobalParam.LIST_LOAD_FIRST:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						break;

					case GlobalParam.LIST_LOAD_MORE:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_LOADINGMORE_INDECATOR);
						break;
					case GlobalParam.LIST_LOAD_REFERSH:
						mHandler.sendEmptyMessage(GlobalParam.HIDE_SCROLLREFRESH);
						break;

					default:
						break;
					}
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}


	private void updateListView(){

		if (mDataList == null || mDataList.size() == 0) {
			return;
		}

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

		
		mListView.setAdapter(mAdapter);

	}



	@Override
	public void onMovedToScrapHeap(View arg0) {

	}

	private void doChoose(final boolean isGallery, final Intent data,String tempFileName,int type) {
		if(isGallery){
			originalImage(data,tempFileName,type);
		}else {
			if(data != null){
				originalImage(data,tempFileName,type);
			}else{
				// Here if we give the uri, we need to read it
				String path = /*Environment.getExternalStorageDirectory() + */FeatureFunction.PUB_TEMP_DIRECTORY+tempFileName+".jpg";
				if(type == 0){
					getCropImageIntent(Uri.fromFile(new File(path)));
				}else if(type == 1){
					Intent intent = new Intent(mContext, RotateImageActivity.class);
					intent.putExtra("path", path);
					intent.putExtra("type", type);
					startActivityForResult(intent, REQUEST_GET_BITMAP);
				}
		
			}
		}
	}

	private void originalImage(Intent data,String tempFileName,int type) {
		/*
		 * switch (requestCode) {
		 */
		// case FLAG_CHOOSE:
		Uri uri = data.getData();
		if (uri != null) {
			//Log.d("may", "uri=" + uri + ", authority=" + uri.getAuthority());
			if (!TextUtils.isEmpty(uri.getAuthority())) {
				Cursor cursor = getContentResolver().query(uri,
						new String[] { MediaStore.Images.Media.DATA }, null, null,
						null);
				if (null == cursor) {
					Toast.makeText(mContext, R.string.no_found, Toast.LENGTH_SHORT).show();
					return;
				}
				cursor.moveToFirst();
				String imageFilePath = cursor.getString(cursor
						.getColumnIndex(MediaStore.Images.Media.DATA));
				Log.d("may", "path=" + imageFilePath);
				if(type == 0){
					getCropImageIntent(uri);
				}else if(type == 1){
					Intent intent = new Intent(mContext,RotateImageActivity.class);
					intent.putExtra("path", imageFilePath);
					intent.putExtra("type", type);
					startActivityForResult(intent, REQUEST_GET_BITMAP);
				}

				//ShowBitmap(false);


			} else {
				Log.d("may", "path=" + uri.getPath());
				Intent intent = new Intent(mContext,RotateImageActivity.class);
				intent.putExtra("path", uri.getPath());
				intent.putExtra("type", type);
				startActivityForResult(intent, REQUEST_GET_BITMAP);
				//mImageFilePath = uri.getPath();
				//ShowBitmap(false);
			}
		}
		else {
			Intent intent = new Intent(mContext, RotateImageActivity.class);
			intent.putExtra("path", FeatureFunction.PUB_TEMP_DIRECTORY+tempFileName+".jpg");
			intent.putExtra("type", type);
			startActivityForResult(intent, REQUEST_GET_BITMAP);
		}
	}

	Uri imgUri ;
	public void  getCropImageIntent(Uri photoUri) {
		imgUri = Uri.fromFile(new File(mCropImgPath));
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("scale", true);
		intent.putExtra("aspectX", 3);
		intent.putExtra("aspectY", 2);
		intent.putExtra("outputX", ICON_SIZE_WIDTH);
		intent.putExtra("outputY", ICON_SIZE_HEIGHT);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
		intent.putExtra("noFaceDetection", false);
		startActivityForResult(intent,REQUEST_GET_BITMAP);
	}

	@Override
	protected void onDestroy() {
		if (mBitmap!=null) {
			mHeaderBg.setImageBitmap(null);
			mBitmap.recycle();
		}
		if(broadcaset!=null){
			unregisterReceiver(broadcaset);
		}
		if(mAdapter != null){
			
		}
		super.onDestroy();
	}

	private void freeBitmap(HashMap<String, Bitmap> cache){
		if(cache.isEmpty()){
			return;
		}
		for(Bitmap bitmap:cache.values()){
			if(bitmap != null && !bitmap.isRecycled()){
				bitmap.recycle();
				bitmap = null;

			}
		}
		cache.clear();
		System.gc();
	}


	private void showModifybgDialog(){

		AlertDialog.Builder builder = new AlertDialog.Builder(MyAlbumActivity.this);	
		builder.setIcon(R.drawable.ic_dialog_alert);
		builder.setTitle("你确定要更换背景吗？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//这里添加点击确定后的逻辑
				//showDialog("你选择了确定");
				uploadBg();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//这里添加点击确定后的逻辑
				//showDialog("你选择了取消");
				/*	if (mMoving!=null && mMoving.frontCover!=null && !mMoving.frontCover.equals("")) {
					mImageLoader.getBitmap(mContext, mHeaderBg,null,mMoving.frontCover, 0, false,false);
				}else{*/
				mHeaderBg.setImageResource(R.drawable.head_img);
				//}

			}
		});
		builder.create().show();

	}

	private void uploadBg(){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
	
	}






	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			if(mFrontCover!=null && !mFrontCover.equals("")){
				Intent intent = new Intent();
				intent.putExtra("front_cover", mFrontCover);
				setResult(RESULT_OK,intent );
			}

			MyAlbumActivity.this.finish();
			break;
		case R.id.img_bg:
			break;
		case R.id.header_icon:
			if((mLogin == null || mLogin.equals("") )
					||( mLogin.uid ==null || mLogin.uid.equals("")) ){
				Toast.makeText(mContext, R.string.user_not_exits,Toast.LENGTH_LONG).show();
				return;
			}
			Intent intent = new Intent();
			if(mToUserID.equals(IMCommon.getUserId(mContext))){
				intent.setClass(mContext, UserInfoActivity.class);
				intent.putExtra("user", mLogin);
				intent.putExtra("type",1);
			}else{
				intent.setClass(mContext, UserInfoActivity.class);
				intent.putExtra("type",2);
				intent.putExtra("uid", mLogin.uid);
			}





			startActivity(intent);
			break;
		case R.id.right_btn:
			mPopWindows.showGroupPopView(mPopList,Gravity.RIGHT,R.drawable.no_top_arrow_bg,R.color.white,0);
			break;

		default:
			break;
		}
	}

	
	@Override
	public void onChangeState(MyPullToRefreshListView container, int state) {
		mRefreshViewLastUpdated.setText(FeatureFunction.getRefreshTime());
		switch (state) {
		case MyPullToRefreshListView.STATE_LOADING:
			mHandler.sendEmptyMessage(GlobalParam.SHOW_SCROLLREFRESH);
			break;
		}
	}




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(arg0, arg1, arg2);
		switch (requestCode) {
		case REQUEST_GET_URI: 
			if (resultCode == RESULT_OK) {
				doChoose(true, data);
			}

			break;

		case REQUEST_GET_IMAGE_BY_CAMERA:
			if(resultCode == RESULT_OK){
				doChoose(false, data);
			}
			break;

		case REQUEST_GET_BITMAP:
			if(resultCode == RESULT_OK){
				String path = data.getStringExtra("path");
				if(!TextUtils.isEmpty(path)){
					Intent sendMovingIntent = new Intent();
					sendMovingIntent.setClass(mContext, SendFriendCircleActivity.class);
					sendMovingIntent.putExtra("moving_url",path);
					mContext.startActivity(sendMovingIntent);
				}
			}

			break;

		default:
			break;
		}
	}
	private void selectImg(){
		MMAlert.showAlert(mContext,mContext.getResources().getString(R.string.select_image),
				mContext.getResources().getStringArray(R.array.camer_item), 
				null, new OnAlertSelectId() {

			@Override
			public void onClick(int whichButton) {
				switch (whichButton) {
				case 0:
					getImageFromGallery();
					break;
				case 1:
					getImageFromCamera();
					break;
				default:
					break;
				}
			}
		});
	}


	private void getImageFromCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if(FeatureFunction.newFolder(Environment.getExternalStorageDirectory() + FeatureFunction.PUB_TEMP_DIRECTORY)){
			File out = new File(Environment.getExternalStorageDirectory() + FeatureFunction.PUB_TEMP_DIRECTORY, TEMP_FILE_NAME);
			Uri uri = Uri.fromFile(out);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

			startActivityForResult(intent, REQUEST_GET_IMAGE_BY_CAMERA);
		}

	}

	private void getImageFromGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");

		startActivityForResult(intent, REQUEST_GET_URI);
	}

	private void doChoose(final boolean isGallery, final Intent data) {
		if(isGallery){
			originalImage(data);
		}else {
			if(data != null){
				originalImage(data);
			}else{
				// Here if we give the uri, we need to read it

				String path = Environment.getExternalStorageDirectory() + FeatureFunction.PUB_TEMP_DIRECTORY+TEMP_FILE_NAME;
				String extension = path.substring(path.indexOf("."), path.length());
				if(FeatureFunction.isPic(extension)){
					//startPhotoZoom(Uri.fromFile(new File(path)));
					Intent intent = new Intent(mContext, RotateImageActivity.class);
					intent.putExtra("path", path);
					intent.putExtra("type", 0);
					startActivityForResult(intent, REQUEST_GET_BITMAP);
				}
				//mImageFilePath = FeatureFunction.PUB_TEMP_DIRECTORY+TEMP_FILE_NAME;
				//ShowBitmap(false);
			}
		}
	}

	private void originalImage(Intent data) {
		/*
		 * switch (requestCode) {
		 */
		// case FLAG_CHOOSE:
		Uri uri = data.getData();
		//Log.d("may", "uri=" + uri + ", authority=" + uri.getAuthority());
		if (!TextUtils.isEmpty(uri.getAuthority())) {
			Cursor cursor = getContentResolver().query(uri,
					new String[] { MediaStore.Images.Media.DATA }, null, null,
					null);
			if (null == cursor) {
				//Toast.makeText(mContext, R.string.no_found, Toast.LENGTH_SHORT).show();
				return;
			}
			cursor.moveToFirst();
			String path = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			Log.d("may", "path=" + path);
			String extension = path.substring(path.lastIndexOf("."), path.length());
			if(FeatureFunction.isPic(extension)){
				Intent intent = new Intent(mContext, RotateImageActivity.class);
				intent.putExtra("path", path);
				startActivityForResult(intent, REQUEST_GET_BITMAP);

				//startPhotoZoom(data.getData());

			}else {
				//Toast.makeText(mContext, R.string.please_choose_pic, Toast.LENGTH_SHORT).show();
			}
			//ShowBitmap(false);


		} else {
			Log.d("may", "path=" + uri.getPath());
			String path = uri.getPath();
			String extension = path.substring(path.lastIndexOf("."), path.length());
			if(FeatureFunction.isPic(extension)){
				Intent intent = new Intent(mContext, RotateImageActivity.class);
				intent.putExtra("path", path);
				startActivityForResult(intent, REQUEST_GET_BITMAP);
			}else {
				//Toast.makeText(mContext, R.string.please_choose_pic, Toast.LENGTH_SHORT).show();
			}
			//mImageFilePath = uri.getPath();
			//ShowBitmap(false);
		}
	}

}
