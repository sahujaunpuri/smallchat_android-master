package net.smallchat.im.mine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.LoginResult;
import net.smallchat.im.R;
import net.smallchat.im.components.TreeViewActivity;
import net.smallchat.im.components.multi_image_selector.MultiImageSelector;
import net.smallchat.im.components.scrollerpicker.SelectContryhday;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.api.IMException;
import net.smallchat.im.utils.ImageUtils;

/**
 * 完善用户资料
 * @author dongli
 *
 */
public class CompleteUserInfoActvity extends BaseActivity implements OnClickListener{

	private static final String TEMP_FILE_NAME = "header.jpg";
	private RelativeLayout mHeaderLayout,mAddrLayout,mSexLayout,
			mSignLayout,mNickNameLayout;

	private TextView mSexTextView,mAddrTextView,mSiTextView,mNickNameTextView;
	private ImageView mImageView;

	private String mInputNickName,mInputAddr,mInputSign;
	private int mInputSex = 2;

	/**
	 * // 省id
	 */
	private String mProvinceid;
	private String mProvice;

	/**
	 * //市id
	 */
	private String mCityid;
	private String mCity;
	/**
	 * // 课程id
	 */
	private String  mCourseid;

	private int mType;

	private Login mLogin;
	private Bitmap mBitmap;
	private String mImageFilePath;
	private SelectContryhday contry;
	private ImageLoader mImageLoader;
	private String mHeadUrl;


	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case GlobalParam.MSG_CHECK_STATE:
					LoginResult loginResult = (LoginResult) msg.obj;
					if (loginResult == null) {
						Toast.makeText(mContext,mContext.getResources().getString(R.string.commit_data_error), Toast.LENGTH_LONG)
								.show();
						return;
					}
					if (loginResult.mState.code != 0) {
						Toast.makeText(mContext, loginResult.mState.errorMsg,
								Toast.LENGTH_LONG).show();
						return;
					}
					Login login = loginResult.mLogin;
					login.password = IMCommon.getLoginResult(mContext).password;
					IMCommon.saveLoginResult(mContext, login);
					setResult(GlobalParam.SHOW_COMPLETE_RESULT);
					CompleteUserInfoActvity.this.finish();

					break;
				case GlobalParam.MSG_SHOW_LOAD_DATA:
					if(mInputAddr != null && !mInputAddr.equals("")){
						mAddrTextView.setText(mInputAddr);
					}

					break;
				default:
					break;
			}
		}

	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.complete_user_info);
		mContext = this;
		mLogin = (Login) getIntent().getSerializableExtra("login");
		mImageLoader = new ImageLoader();
		initCompent();

		//CountryList contryList = ChatApplication.getContryList();

	}


	@Override
	protected void onResume() {
		super.onResume();
	}


	private void initCompent(){
		setTitleContent(0, R.drawable.ok_btn, R.string.complete_user_info);
		//mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);

		mHeaderLayout = (RelativeLayout)findViewById(R.id.new_header_layout);
		mAddrLayout = (RelativeLayout)findViewById(R.id.addr_layout);
		mSexLayout = (RelativeLayout)findViewById(R.id.sex_layout);
		mNickNameLayout = (RelativeLayout)findViewById(R.id.nickname_layout);
		mSignLayout = (RelativeLayout)findViewById(R.id.sign_layout);
		mHeaderLayout.setOnClickListener(this);
		mNickNameLayout.setOnClickListener(this);
		mAddrLayout.setOnClickListener(this);
		mSexLayout.setOnClickListener(this);
		mSignLayout.setOnClickListener(this);


		mNickNameTextView = (TextView)findViewById(R.id.nickname_content);
		mSexTextView = (TextView)findViewById(R.id.sex_content);
		mAddrTextView = (TextView)findViewById(R.id.addr_content);
		mSiTextView = (TextView)findViewById(R.id.sign_content);
		mImageView = (ImageView)findViewById(R.id.new_header_icon);
		//getMenuTitle();
		setText();

	}


	private void setText(){
		if(mLogin == null || mLogin.equals("")){
			return;
		}
		mHeadUrl = mLogin.headSmall;
		if(mLogin.headSmall!=null && !mLogin.headSmall.equals("")){
			mImageView.setTag(mLogin.headSmall);
			mImageLoader.getBitmap(mContext, mImageView, null,mLogin.headSmall,0,false,true);
		}
		mInputSex = mLogin.gender;
		if(mLogin.gender == 0){
			mSexTextView.setText(mContext.getResources().getString(R.string.man));
		}else if(mLogin.gender == 1){
			mSexTextView.setText(mContext.getResources().getString(R.string.femal));
		}else if(mLogin.gender == 2){
			mSexTextView.setText(mContext.getResources().getString(R.string.no_limit));
		}

		mProvice = mLogin.provinceid;
		mCity = mLogin.cityid;
		if((mProvice !=null && !mProvice.equals(""))
				&& (mCity!=null && !mCity.equals(""))){
			mAddrTextView.setText(mProvice+"  "+ mCity+" ");
		}


		mInputNickName = mLogin.nickname;
		if(mInputNickName!=null && !mInputNickName.equals("")){
			mNickNameTextView.setText(mInputNickName+" ");
		}


		mInputSign = mLogin.sign;
		if(mInputSign!=null && !mInputSign.equals("")){
			mSiTextView.setText(mInputSign+" ");
		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			/*sendBroadcast(new Intent(GlobalParam.CANCLE_COMPLETE_USERINFO_ACTION));
			this.finish();*/
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void completeUserInfo(){
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
		}
		new Thread(){
			public void run() {

				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, R.string.commit_dataing);
				/*	
					String file,String nickname,int gender,
					String sign,String provinceid,String city*/
					//TODO 增加邮箱地址
					LoginResult login = IMCommon.getIMServerAPI().modifyUserInfo(mImageFilePath,mInputNickName,"",
							mInputSex,mInputSign,mProvice, mCity);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE, login);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
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

			case R.id.right_btn:
				if( mInputNickName == null || mInputNickName.equals("")){
					Toast.makeText(mContext, "昵称为必填项!",Toast.LENGTH_LONG).show();
					return;
				}
				completeUserInfo();
				break;
			case R.id.new_header_layout:
				selectImg();
				break;
			case R.id.addr_layout:

				Intent intent = new Intent();
				intent.setClass(mContext, TreeViewActivity.class);
				intent.putExtra("type",GlobleType.TreeViewActivity_City_TYPE);
				mType = GlobleType.COMPLETE_ADDR;
				startActivityForResult(intent, 1);
				break;
			case R.id.nickname_layout:
				Intent nickNameIntent = new Intent();
				nickNameIntent.setClass(mContext, WriteUserInfoActivity.class);
				nickNameIntent.putExtra("content", mInputNickName);
				nickNameIntent.putExtra("type",GlobleType.COMPLETE_NICKNAME);
				mType = GlobleType.COMPLETE_NICKNAME;
				startActivityForResult(nickNameIntent, 1);
				break;
			case R.id.sex_layout:
				MMAlert.showAlert(mContext, "", mContext.getResources().
								getStringArray(R.array.sex_array),
						null, new OnAlertSelectId() {

							@Override
							public void onClick(int whichButton) {
								switch (whichButton) {
									case 0:
										mInputSex = 0;
										mSexTextView.setText(mContext.getResources().getString(R.string.man));
										break;
									case 1:
										mInputSex = 1;
										mSexTextView.setText(mContext.getResources().getString(R.string.femal));
										break;
									default:
										break;
								}
							}
						});
				break;


			case R.id.sign_layout:
				Intent signIntent = new Intent();
				signIntent.setClass(mContext, WriteUserInfoActivity.class);
				signIntent.putExtra("content", mInputSign);
				signIntent.putExtra("type",GlobleType.COMPLETE_SIGN);
				mType = GlobleType.COMPLETE_SIGN;
				startActivityForResult(signIntent, 1);
				break;
//			case R.tid.hangyue_layout:
//				Intent hangyueIntent = new Intent();
//				hangyueIntent.setClass(mContext, TreeViewActivity.class);
//				hangyueIntent.putExtra("type",GlobleType.TreeViewActivity_Subject_TYPE);
//				mType = GlobleType.COMPLETE_HANGYUE;
//				startActivityForResult(hangyueIntent, 1);
//				break;
//			case R.tid.subject_layout:
//				Intent subjectIntent = new Intent();
//				subjectIntent.setClass(mContext, TreeViewActivity.class);
//				subjectIntent.putExtra("type",GlobleType.TreeViewActivity_Project_TYPE);
//				mType = GlobleType.COMPLETE_SUBJECT;
//				startActivityForResult(subjectIntent, 1);
//				break;
			default:
				break;
		}
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mImageLoader.getImageBuffer().containsKey(mLogin.headSmall)){
			mImageView.setImageDrawable(null);
			if(mImageLoader.getImageBuffer().get(mLogin.headSmall)!=null){
				mImageLoader.getImageBuffer().get(mLogin.headSmall).recycle();
			}
		}


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 1 && resultCode == RESULT_OK){
			if(mType == GlobleType.COMPLETE_SEX){
				mInputSex = data.getIntExtra("sex",0);
				if(mInputSex == 0){
					mSexTextView.setText("男 ");
				}else if(mInputSex == 1){
					mSexTextView.setText("女 ");
				}else if(mInputSex == 2){
					mSexTextView.setText("不限 ");
				}

			}else if(mType == GlobleType.COMPLETE_NICKNAME){
				mInputNickName = data.getStringExtra("nickname");
				mNickNameTextView.setText(mInputNickName+" ");
			}
			else if(mType == GlobleType.COMPLETE_ADDR){
				mInputAddr = data.getStringExtra("addr");
				//省id
				mProvinceid = data.getStringExtra("shengid");
				mProvice = data.getStringExtra("provice");
				mCity = data.getStringExtra("city");
				//市id 
				mCityid = data.getStringExtra("shiid");
				mAddrTextView.setText(mInputAddr+" ");
			}else if(mType == GlobleType.COMPLETE_EMAIL){

			}else if(mType == GlobleType.COMPLETE_COMPANY){

			}else if(mType == GlobleType.COMPLETE_SIGN){
				mInputSign = data.getStringExtra("sign");
				mSiTextView.setText(mInputSign+" ");
			}
		}

		switch (requestCode) {
			case GlobalParam.REQUEST_GET_URI:
				if (resultCode == RESULT_OK) {
					ArrayList<String> mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
					StringBuilder sb = new StringBuilder();
					for(String picPath: mSelectPath){
						if(picPath != null && !picPath.equals("")){
							doChoose(true,picPath);
						}
					}
				}

				break;

			case GlobalParam.REQUEST_GET_IMAGE_BY_CAMERA:
				if(resultCode == RESULT_OK){
					File mCameraTempimageFile=MultiImageSelector.create().getCameraTempFile();
					doChoose(false, mCameraTempimageFile.getAbsolutePath());
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 选择头像对话框
	 */
	private void selectImg(){
		MMAlert.showAlert(mContext, "", mContext.getResources().
						getStringArray(R.array.camer_item),
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

	/**
	 * 调用系统相机拍摄照片
	 */
	private void getImageFromCamera() {
		//启用类微信的图片选择器,只开启摄像头
		MultiImageSelector.create().startCamera(this, GlobalParam.REQUEST_GET_IMAGE_BY_CAMERA);
	}

	/**
	 * 选择手机相册中的图片
	 */
	private void getImageFromGallery() {
		//启用类微信的图片选择器
		MultiImageSelector.create()
				.showCamera(true)//显示摄像头
				.count(1)//发10张图片
				.single()//单选
				.start(this, GlobalParam.REQUEST_GET_URI);
	}

	/*
	 * 处理选择的图片
	 */
	private void doChoose(final boolean isGallery, final String  path) {
		startPhotoZoom(path);
	}

	/*
	 * 裁剪图片
	 */
	public void startPhotoZoom(String path) {
		mImageView.setImageBitmap(null);
		if(mBitmap != null && !mBitmap.isRecycled()){
			mBitmap.recycle();
			mBitmap = null;
		}
		//ImageUtils.getSmallBitmap(path);
		Bitmap bitmap= BitmapFactory.decodeFile(path);
		//裁减成200x200的正方形
		mBitmap = ImageUtils.centerSquareScaleBitmap(bitmap,200);

		mImageView.setImageBitmap(mBitmap);
		mImageFilePath = FeatureFunction.saveTempBitmap(mBitmap, "header.jpg");
	}
}
