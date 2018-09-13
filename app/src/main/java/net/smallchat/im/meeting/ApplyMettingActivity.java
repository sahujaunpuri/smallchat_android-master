package net.smallchat.im.meeting;

import java.io.File;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.R;
import net.smallchat.im.mine.WriteUserInfoActivity;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/**
 * 创建会议
 * @author dongli
 *
 */
public class ApplyMettingActivity extends BaseActivity {

	/*
	 * 定义全局变量
	 */
	private static final String TEMP_FILE_NAME = "met_pic.jpg";


	private TextView mMetTitleTextView,mStartTimeTextView,mEndTimeTextView;
	private EditText mMetTopicEdit;
	private ImageView mImageView;

	private RelativeLayout mHeaderLayout,mMettingTitleLayout,mStartTimeLayout,mEndTimeLayout;
	private Bitmap mBitmap;
	private String mImageFilePath,mInputMetTitle,mInputMetTopic;
	private long mInputEndTime,mInputStartTiime;

	private int mStartYear = 0;
	private int mStartMonth = 0;
	private int mStartDay = 0;
	private int mStartHour = 0;
	private int mStartMinute = 0;
	
	private int mEndYear = 0;
	private int mEndMonth = 0;
	private int mEndDay = 0;
	private int mEndHour = 0;
	private int mEndMinute = 0;
	


	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.apply_metting_view);
		initCompent();
	

	}

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setRightTextTitleContent(R.drawable.back_btn,R.string.ok,R.string.create_metting);
		mLeftBtn.setOnClickListener(this);
		mRightTextBtn.setOnClickListener(this);
		mMetTopicEdit = (EditText)findViewById(R.id.metting_topic);
		mMetTitleTextView = (TextView)findViewById(R.id.nickname_content);

		mStartTimeTextView = (TextView)findViewById(R.id.start_time_content);
		mEndTimeTextView = (TextView)findViewById(R.id.end_time_content);

		mHeaderLayout = (RelativeLayout)findViewById(R.id.new_header_layout);
		mMettingTitleLayout = (RelativeLayout)findViewById(R.id.metting_title_layout);
		mStartTimeLayout = (RelativeLayout)findViewById(R.id.start_time_layout);
		mEndTimeLayout = (RelativeLayout)findViewById(R.id.end_time_layout);

		mHeaderLayout.setOnClickListener(this);
		mMettingTitleLayout.setOnClickListener(this);
		mStartTimeLayout.setOnClickListener(this);
		mEndTimeLayout.setOnClickListener(this);

		mImageView = (ImageView)findViewById(R.id.new_header_icon);
	}

	/*
	 * 判断是否输入内容以及输入的内容是否满足要求
	 */
	private boolean checkValue(){
		boolean isCheck = true;
		String hintMsg = "";
		String startTime = mStartTimeTextView.getText().toString().trim();
		String endTime = mEndTimeTextView.getText().toString().trim();
		 if(mInputMetTitle == null || mInputMetTitle.equals("")){
			isCheck = false;
			hintMsg = mContext.getResources().getString(R.string.please_input_met_title);
		}else if(startTime == null || startTime.equals("")){
			isCheck = false;
			hintMsg = mContext.getResources().getString(R.string.please_select_start_time);
		}else if(endTime == null || endTime.equals("")){
			isCheck = false;
			hintMsg = mContext.getResources().getString(R.string.please_select_start_time);
		}else if(mInputMetTopic== null || mInputMetTopic.equals("")){
			isCheck = false;
			hintMsg = mContext.getResources().getString(R.string.please_input_met_topic);
		}else{
			mInputStartTiime = FeatureFunction.getTimeStamp(startTime);
			mInputEndTime = FeatureFunction.getTimeStamp(endTime);
			if(mInputStartTiime >= mInputEndTime){
				isCheck = false;
				hintMsg = mContext.getString(R.string.choose_time_error);
			}
		}
		
		if(!isCheck && hintMsg!=null && !hintMsg.equals("")){
			Toast.makeText(mContext, hintMsg,Toast.LENGTH_LONG).show();
		}
		return isCheck;
	}

	/*
	 * 创建会议
	 */
	private void applyMet(){
		if(!checkValue()){
			return;
		}
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				new Thread(){
					public void run() {
						try {
							IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, 
									mContext.getResources().getString(R.string.send_request));
							IMResponseState state = IMCommon.getIMServerAPI().createMetting(mImageFilePath,
									mInputMetTitle, mInputMetTopic, mInputStartTiime/1000, mInputEndTime/1000);
							mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
							IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE, state);
						}catch (IMException e) {
							IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
									mContext.getResources().getString(e.getStatusCode()));
						} 
						catch (Exception e) {
							mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						}
						
					};
				}.start();
			};
		}.start();
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
			ApplyMettingActivity.this.finish();
			break;
		case R.id.metting_title_layout://点击会议标题事件
			Intent intent = new Intent();
			intent.setClass(mContext, WriteUserInfoActivity.class);
			intent.putExtra("type", GlobleType.COMPLETE_MET_TITLE);
			String content = mMetTitleTextView.getText().toString();
			if(content!=null && !content.equals("")){
				intent.putExtra("content", content);
			}
			startActivityForResult(intent, 1);
			break;
		case R.id.start_time_layout://开始时间
			showStartTimeDialog();
			break;
		case R.id.end_time_layout://结束时间
			showEndTimeDialog();
			break;
		case R.id.right_text_btn:
			mInputMetTopic= mMetTopicEdit.getText().toString();
			applyMet();
			break;
		case R.id.new_header_layout://选择会议头像
			selectImg();
			break;

		default:
			break;
		}
	}

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_CHECK_STATE:
				IMResponseState status = (IMResponseState)msg.obj;
				if(status == null){
					Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
					return;
				}
				String hintMsg  = status.errorMsg;
				if(hintMsg == null || hintMsg.equals("")){
					hintMsg = mContext.getResources().getString(R.string.commit_data_error);
				}
				if(status.code == 0){
					//刷新会议列表
					sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MEETING_LIST));
					ApplyMettingActivity.this.finish();
					return;
				}
				break;

			default:
				break;
			}
		}
		
	};
	
	//选择会议头像
	private void selectImg(){
		MMAlert.showAlert(mContext, "", mContext.getResources().
				getStringArray(R.array.camer_item), 
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


	/*
	 * 处理选择的头像和输入的会议标题
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1){
			if(resultCode ==RESULT_OK){
				if(data!=null){
					String metTitle = data.getStringExtra("met_title");
					if(metTitle!=null && !metTitle.equals("")){
						mInputMetTitle = metTitle;
						mMetTitleTextView.setText(metTitle);
					}
				}
			}
		}
		switch (requestCode) {
		case GlobalParam.REQUEST_GET_URI: 
			if (resultCode == RESULT_OK) {
				doChoose(true, data);
			}

			break;

		case GlobalParam.REQUEST_GET_IMAGE_BY_CAMERA:
			if(resultCode == RESULT_OK){
				doChoose(false, data);
			}
			break;
		case GlobalParam.REQUEST_GET_BITMAP:
			if(resultCode == RESULT_OK){

				Bundle extras = data.getExtras();
				if (extras != null) {

					mImageView.setImageBitmap(null);
					if(mBitmap != null && !mBitmap.isRecycled()){
						mBitmap.recycle();
						mBitmap = null;
					}

					mBitmap = extras.getParcelable("data");
					mImageView.setImageBitmap(mBitmap);
					File file = new File(Environment.getExternalStorageDirectory() + FeatureFunction.PUB_TEMP_DIRECTORY + TEMP_FILE_NAME);
					if(file != null && file.exists()){
						file.delete();
						file = null;
					}

					mImageFilePath = FeatureFunction.saveTempBitmap(mBitmap, "header.jpg");
				}

			}
			break;	
		default:
			break;
		}
	}

	/*
	 * 拍一张
	 */
	private void getImageFromCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if(FeatureFunction.newFolder(Environment.getExternalStorageDirectory() + FeatureFunction.PUB_TEMP_DIRECTORY)){
			File out = new File(Environment.getExternalStorageDirectory() + FeatureFunction.PUB_TEMP_DIRECTORY, TEMP_FILE_NAME);
			Uri uri = Uri.fromFile(out);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

			startActivityForResult(intent, GlobalParam.REQUEST_GET_IMAGE_BY_CAMERA);
		}

	}

	/*
	 * 从相册中选一张
	 */
	private void getImageFromGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");

		startActivityForResult(intent, GlobalParam.REQUEST_GET_URI);
	}

	/*
	 * 处理选择的图片
	 */
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
					startPhotoZoom(Uri.fromFile(new File(path)));
				}else {
					//Toast.makeText(mContext, R.string.please_choose_pic, Toast.LENGTH_SHORT).show();
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
				/*Intent intent = new Intent(mContext, ChooseHeaderActivity.class);
				intent.putExtra("path", path);
				startActivityForResult(intent, REQUEST_GET_BITMAP);*/

				startPhotoZoom(data.getData());

			}else {
				//Toast.makeText(mContext, R.string.please_choose_pic, Toast.LENGTH_SHORT).show();
			}
			//ShowBitmap(false);


		} else {
			Log.d("may", "path=" + uri.getPath());
			String path = uri.getPath();
			String extension = path.substring(path.lastIndexOf("."), path.length());
			if(FeatureFunction.isPic(extension)){
				startPhotoZoom(uri);
			}else {
				//Toast.makeText(mContext, R.string.please_choose_pic, Toast.LENGTH_SHORT).show();
			}
			//mImageFilePath = uri.getPath();
			//ShowBitmap(false);
		}
	}

	/*
	 * 裁剪图片
	 */
	public void startPhotoZoom(Uri uri) {
		/*
		 * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * 直接在里面Ctrl+F搜：CROP ，之前小马没仔细看过，其实安卓系统早已经有自带图片裁剪功能,
		 * 是直接调本地库的，小马不懂C C++  这个不做详细了解去了，有轮子就用轮子，不再研究轮子是怎么
		 * 制做的了...吼吼
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		//下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 180);
		intent.putExtra("outputY", 180);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, GlobalParam.REQUEST_GET_BITMAP);
	}

	/*
	 * 页面销毁释放图片
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mBitmap!=null && !mBitmap.isRecycled()){
			mImageView.setImageDrawable(null);
			mBitmap.recycle();
		}
	}
	
	/*
	 * 显示选择会议开始时间控件
	 */
	private void showStartTimeDialog (){
		View view = LayoutInflater.from(mContext).inflate(R.layout.time_dialog, null);
		final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
		final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
		timePicker.setAddStatesFromChildren(true);
		timePicker.setIs24HourView(true);
		if(mStartHour == 0){
			Calendar c = Calendar.getInstance();  
			mStartHour = c.get(Calendar.HOUR_OF_DAY);
			mStartMinute = c.get(Calendar.MINUTE);
		}else {
			timePicker.setCurrentHour(mStartHour);
			timePicker.setCurrentMinute(mStartMinute);
		}
		Calendar c = null;  
		int year = 0;
		int month = 0;
		int day = 0;
		
		c = Calendar.getInstance();
		mStartYear = c.get(Calendar.YEAR);  
		mStartMonth = c.get(Calendar.MONTH);  
		mStartDay = c.get(Calendar.DAY_OF_MONTH); 
		
		year = mStartYear;
		month = mStartMonth;
		day = mStartDay;
		
		datePicker.init(year, month, day, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		AlertDialog dialog = builder.create();
		dialog.setButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				datePicker.clearFocus();
				timePicker.clearFocus();
				mStartYear = datePicker.getYear();
				mStartMonth = datePicker.getMonth();
				mStartDay = datePicker.getDayOfMonth();
				mStartHour = timePicker.getCurrentHour();
				mStartMinute = timePicker.getCurrentMinute();
				String time = FeatureFunction.showTimedate(mStartYear, mStartMonth, mStartDay, mStartHour, mStartMinute);
				if(TextUtils.isEmpty(time)){
					Toast.makeText(mContext, mContext.getString(R.string.choose_pass_time), Toast.LENGTH_LONG).show();
					return;
				}
				mStartTimeTextView.setText(time);
			}
		});
		dialog.setButton2(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		String title = "";
		title = mContext.getString(R.string.meeting_start_time);
		dialog.setTitle(title);
		dialog.setView(view, 0, FeatureFunction.dip2px(mContext, 5), 0, FeatureFunction.dip2px(mContext, 5));
		
		dialog.show();
	}
	
	/*
	 * 显示选择会议结束时间控件
	 */
	private void showEndTimeDialog (){
		View view = LayoutInflater.from(mContext).inflate(R.layout.time_dialog, null);
		final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
		final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
		timePicker.setAddStatesFromChildren(true);
		timePicker.setIs24HourView(true);
		if(mEndHour == 0){
			Calendar c = Calendar.getInstance();  
			mEndHour = c.get(Calendar.HOUR_OF_DAY);
			mEndMinute = c.get(Calendar.MINUTE);
		}else {
			timePicker.setCurrentHour(mEndHour);
			timePicker.setCurrentMinute(mEndMinute);
		}
		Calendar c = null;  
		int year = 0;
		int month = 0;
		int day = 0;
		
		c = Calendar.getInstance();
		mEndYear = c.get(Calendar.YEAR);  
		mEndMonth = c.get(Calendar.MONTH);  
		mEndDay = c.get(Calendar.DAY_OF_MONTH); 
		
		year = mEndYear;
		month = mEndMonth;
		day = mEndDay;
		
		datePicker.init(year, month, day, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		AlertDialog dialog = builder.create();
		dialog.setButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				datePicker.clearFocus();
				timePicker.clearFocus();
				mEndYear = datePicker.getYear();
				mEndMonth = datePicker.getMonth();
				mEndDay = datePicker.getDayOfMonth();
				mEndHour = timePicker.getCurrentHour();
				mEndMinute = timePicker.getCurrentMinute();
				String time = FeatureFunction.showTimedate(mEndYear, mEndMonth, mEndDay, mEndHour, mEndMinute);
				if(TextUtils.isEmpty(time)){
					Toast.makeText(mContext, mContext.getString(R.string.choose_pass_time), Toast.LENGTH_LONG).show();
					return;
				}
				mEndTimeTextView.setText(time);
			}
		});
		dialog.setButton2(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		String title = "";
		title = mContext.getString(R.string.meeting_end_time);
		dialog.setTitle(title);
		dialog.setView(view, 0, FeatureFunction.dip2px(mContext, 5), 0, FeatureFunction.dip2px(mContext, 5));
		
		dialog.show();
	}



}
