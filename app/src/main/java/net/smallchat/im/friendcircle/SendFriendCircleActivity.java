package net.smallchat.im.friendcircle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.MoreFile;
import net.smallchat.im.components.AreaActivity;
import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MapInfo;
import net.smallchat.im.Entity.UploadImg;
import net.smallchat.im.components.LocationActivity;
import net.smallchat.im.R;
import net.smallchat.im.components.ShowImageActivity;
import net.smallchat.im.adapter.UploadPicAdapter;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.components.multi_image_selector.MultiImageSelector;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.MyGridView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 发布一条动态信息
 * @author dongli
 *
 */
public class SendFriendCircleActivity extends BaseActivity implements OnClickListener, OnItemClickListener{

	public static final int MSG_SHOW_IMAGE = 0x00023;
	public static final int REQUEST_GET_BITMAP = 102;
	public static final int REQUEST_GET_IMAGE_BY_CAMERA = 103;
	private static final int RESQUEST_CODE = 100;
	private static final int RESQUEST_AREA_CODE = 104;
	private static final String TAG = "SendCircle";
	private File mCameraTempimageFile=null;



	private EditText mContentEdit;


	private int IMAGE_MAX = 9;
	private MyGridView mGridView;
	private UploadPicAdapter mAdapter;
	private List<UploadImg> mImageList = new ArrayList<UploadImg>();
	private DisplayMetrics mMetrics;
	private int mWidth = 0;
	private ImageView mLocationIcon,mAreaIcon;
	private TextView mLocationAddress;
	private LinearLayout mLocationLayout,mAreaLayout;

	private String mInputTitle;

	private String mInputContetn;

	private String mLat,mLng,mAddress,mAreaUid;

	private String mJumpImageUrl;
	private List<Login> mSelectUserList ;

	private int mCheckId = 0;


	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {


				case GlobalParam.MSG_CHECK_STATE:
					IMResponseState status = (IMResponseState)msg.obj;
					if (status == null) {
						Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
						return;
					}

					String hintMsg = status.errorMsg;

					if(status.code!=0){
						if(hintMsg==null || hintMsg.equals("")){
							hintMsg = mContext.getResources().getString(R.string.send_moving_error);
						}
						Toast.makeText(mContext, hintMsg,Toast.LENGTH_LONG).show();
						return;
					}

					if(hintMsg == null || hintMsg.equals("")){
						hintMsg = mContext.getResources().getString(R.string.send_moving_success);
					}
					Toast.makeText(mContext, hintMsg,Toast.LENGTH_LONG).show();
					SendFriendCircleActivity.this.finish();
					sendBroadcast(new Intent(FriendCircleActivity.MSG_REFRESH_MOVIINF));
					break;
				default:
					break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_friend_circle);
		mContext = this;
		mJumpImageUrl = getIntent().getStringExtra("moving_url");
		mMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
		mWidth = mMetrics.widthPixels;
		initCompent();
	}

	private void initCompent(){
		setRightTextTitleContent(R.drawable.back_btn,R.string.send, R.string.share);
		mLeftBtn.setOnClickListener(this);
		mRightTextBtn.setOnClickListener(this);

		mContentEdit = (EditText)findViewById(R.id.content);

		mContentEdit.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mContentEdit.invalidate();
				mContentEdit.requestLayout();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				int length = s.length();
				updateWordCount(length);
			}

		});

		mLocationIcon = (ImageView)findViewById(R.id.location_icon);
		mAreaIcon = (ImageView)findViewById(R.id.area_icon);
		mAreaLayout = (LinearLayout)findViewById(R.id.area_layout);
		mLocationLayout = (LinearLayout)findViewById(R.id.loacation_layout);
		mLocationLayout.setOnClickListener(this);
		mAreaLayout.setOnClickListener(this);

		mLocationAddress = (TextView)findViewById(R.id.loaction_addr);



		mGridView = (MyGridView) findViewById(R.id.gridview);
		mGridView.setOnItemClickListener(this);

		if(mJumpImageUrl!=null && !mJumpImageUrl.equals("")){
			mImageList.add(new UploadImg(mJumpImageUrl, 0));
		}
		mImageList.add(new UploadImg("", 1));

		mAdapter = new UploadPicAdapter(mContext, mImageList, mWidth);
		mGridView.setAdapter(mAdapter);

	}





	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mImageList != null){
			for (int i = 0; i < mImageList.size(); i++) {
				if(!TextUtils.isEmpty(mImageList.get(i).mPicPath)){
					ImageView view = (ImageView) mGridView.findViewWithTag(mImageList.get(i).mPicPath);
					if(view != null){
						view.setImageBitmap(null);
					}
				}
			}
		}

		if(mAdapter != null){
			FeatureFunction.freeBitmap(mAdapter.getImageBuffer());
		}
	}


	private boolean  checkText(){
		boolean isCheck = true;
		String hinMsg = "";
		mInputContetn = mContentEdit.getText().toString();

		if ((mInputContetn == null || mInputContetn.equals(""))
				&& mImageList.size() == 1) {
			isCheck = false;
			hinMsg = mContext.getResources().getString(R.string.please_wirte_content);
		}


		if (!isCheck && hinMsg!=null && !hinMsg.equals("")) {
			Toast.makeText(mContext, hinMsg,Toast.LENGTH_LONG).show();
		}
		return isCheck ;
	}
	//发送朋友圈
	private void send(){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		if (!checkText()) {
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler,BASE_SHOW_PROGRESS_DIALOG,"正在提交数据,请稍后...");
					List<MoreFile> picList = null;
					if (mImageList!=null && mImageList.size()>0 ) {
						Log.d(TAG,"正在提交数据,请稍后...,ImageList=="+mImageList.size());
						picList = new ArrayList<MoreFile>();

						for (int i = 0; i <mImageList.size(); i++) {
							if(mImageList.get(i).mType != 1){
								String key = "picture";
								if (i > 0) {
									int index = i+1;
									key = key+index;
								}
								picList.add(new MoreFile(key,mImageList.get(i).mPicPath));
							}

						}
					}
					IMResponseState status =IMCommon.getIMServerAPI().addShare(picList, mInputContetn, mLng,mLat, mAddress, mAreaUid);

					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,status);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}


	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.left_btn:
				SendFriendCircleActivity.this.finish();
				break;
			case R.id.loacation_layout:
				Intent intent = new Intent(this, LocationActivity.class);
				startActivityForResult(intent, RESQUEST_CODE);
				break;
			case R.id.area_layout:
				Intent areaIntent = new Intent(this, AreaActivity.class);
				if(mSelectUserList!=null && mSelectUserList.size()>0){
					areaIntent.putExtra("userlist",(Serializable)mSelectUserList);
				}
				areaIntent.putExtra("checkId", mCheckId);
				startActivityForResult(areaIntent, RESQUEST_AREA_CODE);
				break;
			case R.id.right_text_btn://发送一条动态信息
				send();
				break;

			default:
				break;
		}
	}

	private void selectImg(){
		MMAlert.showAlert(mContext, mContext.getResources().getString(R.string.select_image),
				mContext.getResources().getStringArray(R.array.camer_item),
				null, new OnAlertSelectId() {

					@Override
					public void onClick(int whichButton) {
						Log.e("whichButton", "whichButton: "+whichButton);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
			case REQUEST_GET_IMAGE_BY_CAMERA://单独拍照处理

				if (resultCode == RESULT_OK) {
					mCameraTempimageFile=MultiImageSelector.create().getCameraTempFile();
					String picPath="";

					if(mCameraTempimageFile != null) {
						// notify system the image has change
						sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mCameraTempimageFile)));
						picPath=mCameraTempimageFile.getAbsolutePath();
					}
					if (mImageList != null && mImageList.size() > 0) {
						mImageList.clear();
					}
					if(picPath!=null&&!picPath.isEmpty()) {
						UploadImg img = new UploadImg(picPath, 0);
						mImageList.add(img);
						mAdapter.notifyDataSetChanged();

					}

				}

				break;

			case REQUEST_GET_BITMAP://处理选择完图片

				if (resultCode == RESULT_OK) {
					ArrayList<String> mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
					StringBuilder sb = new StringBuilder();
					Log.d(TAG,"mSelectPath size =="+mSelectPath.size());
					if (mImageList != null && mImageList.size() > 0) {
						mImageList.clear();
					}
					for (String picPath : mSelectPath) {
						if (picPath != null && !picPath.equals("")) {
							UploadImg img = new UploadImg(picPath, 0);
							mImageList.add(img);
							mAdapter.notifyDataSetChanged();
						}
					}

				}

				break;
			case RESQUEST_CODE:
				if (data != null && RESULT_OK == resultCode) {
					Bundle bundle = data.getExtras();
					if (bundle != null) {

						MapInfo mapInfo = (MapInfo) data.getSerializableExtra("mapInfo");
						if (mapInfo == null) {
							mLocationIcon.setBackgroundResource(R.drawable.share_location_icon);
							Toast.makeText(mContext, mContext.getString(R.string.get_location_failed), Toast.LENGTH_SHORT).show();
							mLocationAddress.setText(mContext.getResources().getString(R.string.location_current));
							return;
						}
						mLocationAddress.setText(mapInfo.getAddr());
						mLat = mapInfo.getLat();
						mLng = mapInfo.getLng();
						mLocationIcon.setBackgroundResource(R.drawable.share_location_icon_check);
						mAddress = mapInfo.getAddr();

					}
				}
				break;
			case RESQUEST_AREA_CODE:
				if (data != null && RESULT_OK == resultCode) {
					Bundle bundle = data.getExtras();
					mAreaUid = "";
					if (bundle != null) {
						mAreaUid = data.getStringExtra("area_uid");
						mCheckId = data.getIntExtra("checkId", 0);
						if (mCheckId == 1 && (mAreaUid != null && !mAreaUid.equals("")
								&& !mAreaUid.startsWith(",") && !mAreaUid.endsWith(","))) {
							mAreaIcon.setBackgroundResource(R.drawable.share_area_check);
						} else {
							mAreaIcon.setBackgroundResource(R.drawable.share_area);
						}
						mSelectUserList = (List<Login>) data.getSerializableExtra("userlist");
					}
				}
				break;
			default:
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	private void getImageFromCamera() {
		//启用类微信的图片选择器,只开启摄像头
		MultiImageSelector.create().startCamera(this, REQUEST_GET_IMAGE_BY_CAMERA);
	}

	/**
	 * 从相册选择图片
	 */
	private void getImageFromGallery() {
		//启用类微信的图片选择器
		MultiImageSelector.create()
				.showCamera(true)//显示摄像头
				.count(9)//发9张图片
				.multi()//多图选择
				.start(this, REQUEST_GET_BITMAP);
	}

	private void updateWordCount(int length){
		/*if(mWordLimitText != null){
			length = WORD_LIMIT - length;
			mWordLimitText.setText(String.valueOf(length));
		}*/
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(arg2 < mImageList.size()){
			if(mImageList.get(arg2).mType == 0){
				if(mAdapter.getIsDelete()){
					HashMap<String, Bitmap> hashMap = mAdapter.getImageBuffer();
					String path = mImageList.get(arg2).mPicPath;
					ImageView view = (ImageView) mGridView.findViewWithTag(path);
					mImageList.remove(arg2);
					if(view != null){
						view.setImageBitmap(null);
					}
					if (hashMap.get(path) != null) {
						Bitmap bitmap = hashMap.get(path);
						if(bitmap != null && !bitmap.isRecycled()){
							bitmap.recycle();
							bitmap = null;
						}

						hashMap.remove(path);
					}
					if(mImageList.get(mImageList.size() - 1).mType != 1){
						mImageList.add(new UploadImg("", 1));
					}
					mAdapter.notifyDataSetChanged();
				}else{
					Intent showImageIntent = new Intent();
					showImageIntent.setClass(mContext, ShowImageActivity.class);
					showImageIntent.putExtra("type",1);
					showImageIntent.putExtra("pos",arg2);
					showImageIntent.putExtra("img_list",(Serializable)mImageList);
					startActivityForResult(showImageIntent, 1);
				}

			}else if(mImageList.get(arg2).mType == 1){
				if(mAdapter.getIsDelete()){
					mAdapter.setIsDelete(false);
					mAdapter.notifyDataSetChanged();
				}else {
					if(mImageList.size() - 1 >= IMAGE_MAX){
						Toast.makeText(mContext, mContext.getString(R.string.upload_image_max), Toast.LENGTH_SHORT).show();
						return;
					}
					selectImg();
				}

			}
		}else {
			if(mAdapter.getIsDelete()){
				mAdapter.setIsDelete(false);
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	/*@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(arg2 < mImageList.size()){
			if(mImageList.get(arg2).mType == 0){
				if(!mAdapter.getIsDelete()){
					mAdapter.setIsDelete(true);
					mAdapter.notifyDataSetChanged();
				}
				return true;
			}
		}

		return false;
	}*/
}
