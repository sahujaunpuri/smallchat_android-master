package net.smallchat.im.components;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;

/**
 * 图片旋转
 * @author dongli
 *
 */
public class RotateImageActivity extends BaseActivity implements OnClickListener{
	
	private static final int MSG_SHOW_IMAGE = 11103;
	
	private LinearLayout mBottomToolBar;
	private ImageView mImageView;
	private ImageView mRotateLeft;
	private ImageView mRotateRight;
	private boolean isLoaded = false;
	private Bitmap mBitmap;
	private String mImageFilePath;
	private RelativeLayout mRelativeLayout;
	final GetterHandler mAnimHandler = new GetterHandler();
	private File mFile;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rotate_image);
		mContext = this;
		initComponent();
	}

	private void initComponent(){
		setRightTextTitleContent(R.drawable.back_btn,R.string.ok ,0);
		mLeftBtn.setOnClickListener(this);
		mRightTextBtn.setOnClickListener(this);
		mImageFilePath = getIntent().getStringExtra("path");
		mImageView = (ImageView) findViewById(R.id.imageview);
		mImageView.setOnClickListener(this);
		if (mImageFilePath!=null && !mImageFilePath.equals("")) {
			Log.e("RotateImageActivity", "mImageFilePath:"+mImageFilePath);
			mFile = new File(mImageFilePath);
			ShowBitmap();
		}
		mBottomToolBar  = (LinearLayout)findViewById(R.id.imageviewer_toolbar);
		mRotateLeft = (ImageView) findViewById(R.id.imageviewer_imageview_rotateleft);
		mRotateRight = (ImageView) findViewById(R.id.imageviewer_imageview_rotateright);
		mRotateLeft.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				mRotateRight.setEnabled(true);
				if (mImageView == null || !isLoaded) {
					return;
				}
				mBitmap = rotate(mBitmap, -90);
				mImageView.setImageBitmap(mBitmap);
				mImageView.invalidate();
			}
		});

		mRotateRight.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				mRotateLeft.setEnabled(true);
				if (mImageView == null || !isLoaded) {
					return;
				}
				//mRotation = mRotation + 90;
				mBitmap = rotate(mBitmap, 90);
				mImageView.setImageBitmap(mBitmap);
				mImageView.invalidate();
			}
		});
		
		mRelativeLayout = (RelativeLayout) findViewById(R.id.showZoomInOutLayout);
		mRelativeLayout.setOnTouchListener(new OnTouchListener(){

			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					showOnScreenControls();
					scheduleDismissOnScreenControls();
					break;

				case MotionEvent.ACTION_MOVE:
					break;

				case MotionEvent.ACTION_UP:
					break;

				default:
					break;
				}
				return false;
			}
        });

	}
	
	public Bitmap rotate(Bitmap bmp, float degree){
    	Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
		if(bmp != null && !bmp.isRecycled() && bmp != bm){
			bmp.recycle();
        }
		bmp = bm;
        return bmp;  
    }
	
	private Handler mHandler =  new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        
	        case GlobalParam.SHOW_PROGRESS_DIALOG:
	        	String str = (String)msg.obj;
	        	showProgressDialog(str);
	            break;
	        
	        case GlobalParam.HIDE_PROGRESS_DIALOG:
	        	hideProgressDialog();
	            break;
	       
	        case MSG_SHOW_IMAGE:
	        	if (mBitmap != null) {
	        		mImageView.setImageBitmap(mBitmap);
	        		isLoaded = true;
				}
	        	mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
	        	break;
	  
	        default:
	            break;
	        }
	    
	    }
	};

    class GetterHandler extends Handler {
        private static final int IMAGE_GETTER_CALLBACK = 1;

        @Override
        public void handleMessage(Message message) {
            switch(message.what) {
                case IMAGE_GETTER_CALLBACK:
                    ((Runnable) message.obj).run();
                    break;
            }
        }

        public void postGetterCallback(Runnable callback) {
           postDelayedGetterCallback(callback, 0);
        }

        public void postDelayedGetterCallback(Runnable callback, long delay) {
            if (callback == null) {
                throw new NullPointerException();
            }
            Message message = Message.obtain();
            message.what = IMAGE_GETTER_CALLBACK;
            message.obj = callback;
            sendMessageDelayed(message, delay);
        }

        public void removeAllGetterCallbacks() {
            removeMessages(IMAGE_GETTER_CALLBACK);
        }
    }
    
    private final Runnable mDismissOnScreenControlRunner = new Runnable() {
        public void run() {
            hideOnScreenControls();
        }
    };
    
	

	
	@Override
	protected void onDestroy() {
		if(mImageView != null){
			mImageView.setImageBitmap(null);
		}
		if(mBitmap != null && !mBitmap.isRecycled()){
			mBitmap.recycle();
		}
		super.onDestroy();
	}

	private void ShowBitmap(){
		
		new Thread(){
			@Override
			public void run(){
				Message message = new Message();
				message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
				message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
				mHandler.sendMessage(message);
				mBitmap = scalePicture(mImageFilePath);
				mHandler.sendEmptyMessage(MSG_SHOW_IMAGE);
			}
		}.start();
	
	}
	
	private Bitmap scalePicture(String filename) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds= true;
			bitmap = BitmapFactory.decodeFile(filename, opt);  
			int picWidth  = opt.outWidth;  
			int picHeight = opt.outHeight;  
			
			int width = 1024;
			  
			//isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2  
			opt.inSampleSize = 1;  
			if (picWidth > picHeight) {
				if (picWidth > width){
					opt.inSampleSize = picWidth / width;
				}
			} else {
				if (picHeight > width){
					opt.inSampleSize = picHeight / width;
				}
			}
			
			
			//这次再真正地生成一个有像素的，经过缩放了的bitmap  
			opt.inJustDecodeBounds = false;  
			bitmap = BitmapFactory.decodeFile(filename, opt);
			int afterwidth = bitmap.getWidth();
			int afterheight = bitmap.getHeight();
			float con = 1.0f;
			int bigger = afterheight > afterwidth ? afterheight : afterwidth;
			if (bigger > width){
				con = (float)width / bigger;
			}
			
			bitmap = Bitmap.createScaledBitmap(bitmap, (int)(con * afterwidth), (int)(con * afterheight), true);  
			//Log.e("bitmap", bitmap.getWidth() + "/" + bitmap.getHeight());
			mImageFilePath = FeatureFunction.saveTempBitmap(bitmap, mFile.getName());
		} catch (Exception e) {
			// TODO: handle exception]
			e.printStackTrace();
		}
		return bitmap;
	}
	
	
	
	private void hideOnScreenControls() {

        if (mBottomToolBar.getVisibility() == View.VISIBLE) {
            Animation a = new AlphaAnimation(1, 0);;
            a.setDuration(500);
            mBottomToolBar.startAnimation(a);
            mBottomToolBar.setVisibility(View.INVISIBLE);
        }

    }

    private void showOnScreenControls() {
        //if (mPaused) return;
        // If the view has not been attached to the window yet, the
        // zoomButtonControls will not able to show up. So delay it until the
        // view has attached to window.
        if (mBottomToolBar.getVisibility() != View.VISIBLE) {
            Animation animation = new AlphaAnimation(0, 1);
            animation.setDuration(500);
            mBottomToolBar.startAnimation(animation);
            mBottomToolBar.setVisibility(View.VISIBLE);
        }
    }
    
    private void scheduleDismissOnScreenControls() {
        mHandler.removeCallbacks(mDismissOnScreenControlRunner);
        mHandler.postDelayed(mDismissOnScreenControlRunner, 2000);
    }

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			RotateImageActivity.this.finish();
			break;
		case R.id.right_text_btn:
			Message message = new Message();
			message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
			message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
			mHandler.sendMessage(message);
			
			mImageFilePath = FeatureFunction.saveTempBitmap(mBitmap, mFile.getName());
			mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
			Intent intent = new Intent();
			intent.putExtra("path", mImageFilePath);
			if(mBitmap!=null && !mBitmap.isRecycled()){
				intent.putExtra("width",mBitmap.getWidth());
				intent.putExtra("height", mBitmap.getHeight());
			}
			
			setResult(RESULT_OK, intent);
			RotateImageActivity.this.finish();
			break;
			
		case R.id.imageview:
			showOnScreenControls();
			scheduleDismissOnScreenControls();
			break;
		default:
			break;
		}
	}

}
