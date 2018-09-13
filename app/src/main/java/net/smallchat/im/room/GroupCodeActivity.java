package net.smallchat.im.room;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.ImageLoader;

/**
 * 群二维码页
 * @author dongli
 *
 */
public class GroupCodeActivity extends BaseActivity {

	private final static int SAVE_SUCCESS = 5126;
	private TextView mTipTextView;
	private LinearLayout mGroupHeadlerLayout,mCodeAllLayout;
	private ImageView mCodeImageView;
	private TextView mUserNameTextView;
	private String mRoomId;
	private Bitmap mBitmap;
	private String[] mShowString;

	private ImageLoader mImageLoader;

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case  GlobalParam.MSG_SHOW_HEADER_IMG:
				mCodeImageView.setImageBitmap(mBitmap);
				break;
			case SAVE_SUCCESS:
				Toast.makeText(mContext, mContext.getString(R.string.save_picture_to_ablun), Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.group_code_view);
		mContext = this;
		mImageLoader = new ImageLoader();
		mRoomId = getIntent().getStringExtra("room_id");
		mShowString = getIntent().getStringArrayExtra("sString");
		initCompent();
	}

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,R.drawable.more_btn,R.string.group_chat_qrcode_card);
		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);
		mTipTextView = (TextView)findViewById(R.id.tip);
		mCodeImageView = (ImageView)findViewById(R.id.qrcode_icon);
		mGroupHeadlerLayout = (LinearLayout)findViewById(R.id.group_header);
		mCodeAllLayout = (LinearLayout)findViewById(R.id.code_all_layout);
		mUserNameTextView = (TextView)findViewById(R.id.username);
		new Thread(){
			public void run() {
				mBitmap = FeatureFunction.create2DCode(mRoomId, 800, 800);
				if(mBitmap!=null){
					mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_HEADER_IMG);
				}
			};
		}.start();
		if(mShowString!=null && mShowString.length==2){
			String showName = mShowString[0];
			String[] hearUrlArray = mShowString[1].split(",");
			int count = hearUrlArray.length;
			if(count>0){
				if(mGroupHeadlerLayout.getChildCount() != 0){
					mGroupHeadlerLayout.removeAllViews();
				}
				boolean single = count % 2 == 0 ? false : true;
				int row = !single ? count / 2 : count / 2 + 1;
				for (int i = 0; i < row; i++) {
					LinearLayout outLayout = new LinearLayout(mContext);
					outLayout.setOrientation(LinearLayout.HORIZONTAL);
					int width = FeatureFunction.dip2px(mContext, 23);
					outLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, width));
					int padding = FeatureFunction.dip2px(mContext, 1);
					if(single && i == 0){
						LinearLayout layout = new LinearLayout(mContext);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
						layout.setPadding(padding, padding, padding, padding);
						layout.setLayoutParams(params);
						ImageView imageView = new ImageView(mContext);
						imageView.setImageResource(R.drawable.contact_default_header);
						mImageLoader.getBitmap(mContext, imageView, null, hearUrlArray[0], 0, false, true);
						imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
						layout.addView(imageView);
						outLayout.setGravity(Gravity.CENTER_HORIZONTAL);
						outLayout.addView(layout);
					}else {
						for (int j = 0; j < 2; j++) {
							LinearLayout layout = new LinearLayout(mContext);
							LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
							layout.setPadding(padding, padding, padding, padding);
							layout.setLayoutParams(params);
							ImageView imageView = new ImageView(mContext);
							imageView.setImageResource(R.drawable.contact_default_header);
							if(single){
								mImageLoader.getBitmap(mContext, imageView, null, hearUrlArray[2 * i + j - 1], 0, false, true);
							}else {
								mImageLoader.getBitmap(mContext, imageView, null, hearUrlArray[2 * i + j], 0, false, true);
							}
							imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
							layout.addView(imageView);
							outLayout.addView(layout);
						}
					}
					mGroupHeadlerLayout.addView(outLayout);
				}
				mUserNameTextView.setText(showName);
			}
		}else{
			mUserNameTextView.setText(mRoomId);
		}
	}


	/*
	 * 显示更多菜单
	 */
	private void showMoreDialog(){
		MMAlert.showAlert(mContext, null, mContext.getResources().
				getStringArray(R.array.group_card_item), 
				null, new OnAlertSelectId() {

			@Override
			public void onClick(int whichButton) {
				Log.e("whichButton", "whichButton: "+whichButton);
				switch (whichButton) {
				case 0://保存二维码到手机
					if(mBitmap!=null){
						savePicture();
					}
					break;

				default:
					break;
				}
			}
		});
	}
	
	
	/*
	 * 保存二维码到本地
	 */
	private void savePicture(){
		new Thread(){
			@Override
			public void run(){
				String fileName = FeatureFunction.getPhotoFileName(0);
				
			
				mCodeAllLayout.setDrawingCacheEnabled(true);  
				mCodeAllLayout.buildDrawingCache(true);
				Bitmap bitmap=mCodeAllLayout.getDrawingCache();
				Bitmap bitmapTwo=Bitmap.createBitmap(bitmap);
				mCodeAllLayout.setDrawingCacheEnabled(false);
				mCodeAllLayout.destroyDrawingCache();
				 
				if(bitmapTwo == null){
					return;
				}
				
				String uri = MediaStore.Images.Media.insertImage(getContentResolver(), bitmapTwo, fileName, "");
				String filePath = FeatureFunction.getFilePathByContentResolver(mContext, Uri.parse(uri));
				int index = filePath.lastIndexOf("/");
				String filePrefix = filePath.substring(0, index + 1) + fileName;
				try {
					File bitmapFile = new File(filePrefix);
					FileOutputStream bitmapWriter;
					bitmapWriter = new FileOutputStream(bitmapFile);
					if (bitmapTwo.compress(Bitmap.CompressFormat.JPEG, 100, bitmapWriter)) {
						File oldfile = new File(filePath);
						if(oldfile.exists()){
							oldfile.delete();
						}
						bitmapWriter.flush();
						bitmapWriter.close();
						Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);     
						Uri refreshUri = Uri.fromFile(bitmapFile);     
						intent.setData(refreshUri);     
						mContext.sendBroadcast(intent);  
						mHandler.sendEmptyMessage(SAVE_SUCCESS);
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
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
			GroupCodeActivity.this.finish();
			break;
		case R.id.right_btn:

			showMoreDialog();
			break;

		default:
			break;
		}
	}

	
	/*
	 * 释放图片
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mBitmap!=null){
			mCodeImageView.setImageBitmap(null);
			mBitmap.recycle();
		}
	}




}
