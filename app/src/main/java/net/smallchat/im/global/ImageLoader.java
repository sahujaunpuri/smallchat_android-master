package net.smallchat.im.global;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import net.smallchat.im.Entity.CommentUser;
import net.smallchat.im.album.MyAlbumActivity;
import net.smallchat.im.utils.ImageUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;


public class ImageLoader {

	public final static String TAG = "MyImageLoader";
	HashMap<String, Bitmap> mImageBuffer = null;
	HashMap<String, Bitmap> mHeaderBuffer = null;
	public static final int WEIBO_IMAGE_SIZE = 5;
	public final static String SDCARD_PICTURE_CACHE_PATH = "/IM/pic_cache/";
	private final static long FIFTEEN_DAYS = 10 * 24 * 60 * 60 * 1000;
	public static int gScreenWidth = -1;
	public static int gScreenHeight = -1;

	public ImageLoader(){
		mImageBuffer  = new HashMap<String, Bitmap>();
		mHeaderBuffer = new HashMap<String, Bitmap>();
	}

	//见图片转换成圆形图片
	//android之将图片转化为圆形图片
	//public Bitmap getYuan(Bitmap bitmap){
	//bitmap = ((BitmapDrawable)imageView1.getDrawable()).getBitmap();
	//bitmap = getRoundedCornerBitmap(bitmap);
	//imageView1.setImageBitmap(bitmap);

	public HashMap<String, Bitmap> getImageBuffer(){
		return mImageBuffer;
	}

	public static Bitmap getYuan(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = bitmap.getWidth() / 2;

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		if (bitmap != null && !bitmap.isRecycled() && bitmap != output) {
			bitmap.recycle();
		}
		bitmap = output;
		return bitmap;
	}




	public Bitmap getBitmap(Context context, final Object view,
							ProgressBar progressBar, final String imageURL, int resID, boolean isCache ,boolean isHead) {

		if (imageURL == null) {
			Log.e(TAG, "imageURL is NULL");

			return null;
		}
		if (mImageBuffer.containsKey(imageURL)) {
			Bitmap bitmap = mImageBuffer.get(imageURL);

			if (bitmap != null) {
				if (view instanceof ImageView) {
					//Bitmap bmp; 
					/*if(isHead){
						if (isYuan) {
							bmp=getYuan(bitmap);
						}else{
							  bmp = getRoundedCornerBitmap(bitmap, 8);
						}
					}else{
						bmp = mImageBuffer.get(imageURL);
					}*/
					//bmp = mImageBuffer.get(imageURL);
					ImageView imageView = (ImageView) view;
					//ImageUtils.setThumbnailImageFromBitMap(imageView,bitmap);
					imageView.setImageBitmap(bitmap);
					if(progressBar != null){
						progressBar.setVisibility(View.GONE);
					}
				}
				else if (view instanceof RemoteViews) {
					// Reference been Widget user profile
					RemoteViews imageView = (RemoteViews) view;
					imageView.setImageViewBitmap(resID, bitmap);
				}else if(view instanceof RelativeLayout){
					RelativeLayout layout=(RelativeLayout)view;
					BitmapDrawable bd=new BitmapDrawable(bitmap);
					layout.setBackgroundDrawable(bd);
				}else {
					Log.d(TAG, "Unkown view get bitmap!");
				}

				return bitmap;
			} else {
				Log.d(TAG, "Image buffer exist empty bitmap object!");
			}
		}

		try {
			Object str[] = new Object[8];
			str[0] = imageURL;
			str[1] = view;
			str[2] = context;
			str[3] = resID;
			str[4] = isHead;
			str[5] = true;
			str[6] = progressBar;
			//new CanvasImageTask(isCache).execute(str);
			CanvasImageTask task = new CanvasImageTask(isCache);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				task.execute(str);
			} else {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str);
			}
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Bitmap getHeaderBitmap(Context context, final Object view,
								  final String imageURL, int resID, boolean isCache ,boolean isHead,boolean isYuan) {

		if (imageURL == null) {
			Log.d(TAG, "getBitmap, imageURL is null!");
			return null;
		}
		if (mHeaderBuffer.containsKey(imageURL)) {
			Bitmap bitmap = mHeaderBuffer.get(imageURL);

			if (bitmap != null) {
				if (view instanceof ImageView) {
					//Bitmap bmp; 
					/*if(isHead){
						if (isYuan) {
							bmp=getYuan(bitmap);
						}else{
							  bmp = getRoundedCornerBitmap(bitmap, 8);
						}
					}else{
						bmp = mImageBuffer.get(imageURL);
					}*/
					//bmp = mImageBuffer.get(imageURL);
					ImageView imageView = (ImageView) view;
					imageView.setImageBitmap(bitmap);
				} else if (view instanceof RemoteViews) {
					// Reference been Widget user profile
					RemoteViews imageView = (RemoteViews) view;
					imageView.setImageViewBitmap(resID, bitmap);
				}else if(view instanceof RelativeLayout){
					RelativeLayout layout=(RelativeLayout)view;
					BitmapDrawable bd=new BitmapDrawable(bitmap);
					layout.setBackgroundDrawable(bd);
				}else {
					Log.d(TAG, "Unkown view get bitmap!");
				}

				return bitmap;
			} else {
				Log.d(TAG, "Image buffer exist empty bitmap object!");
			}
		}

		try {
			Object str[] = new Object[8];
			str[0] = imageURL;
			str[1] = view;
			str[2] = context;
			str[3] = resID;
			str[4] = isHead;
			str[5] = isYuan;
			str[6] = false;
			str[7] = null;
			new CanvasImageTask(isCache).execute(str);
		} catch (RejectedExecutionException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return null;
	}

	//放大缩小图片  
	public static Bitmap zoomBitmap(Bitmap bitmap,int w,int h){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float)w / width);
		float scaleHeight = ((float)h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		if (newbmp!=null && !newbmp.isRecycled()) {
			if (bitmap!=null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
		return newbmp;
	}

	//获得圆角图片的方法  
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx){

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
				.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		if(bitmap != null && !bitmap.isRecycled() && bitmap != output){
			bitmap.recycle();
		}
		bitmap = output;
		return bitmap;
	}

	/**
	 * Down load bitmap from net and save it to cache
	 * @param urlString
	 * 		The address of bitmap
	 * @param file
	 * 		Bitmap will been saved path
	 * @return
	 * 		null indicator downloadVoice load bitmap is fail
	 */
	private Bitmap loadImageFromUrl(String urlString, File file) {
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		InputStream is = null;

		if(urlString == null || urlString.equals("")){
			return bitmap;
		}

		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			/*conn.setDoInput(true);  
            conn.setRequestMethod("GET");  
            conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
			if (conn.getResponseCode() != 200) {
	            return null;
            }*/
			is = conn.getInputStream();
			// Get the length
			int length = (int) conn.getContentLength();
			if (length != -1) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				byte[] temp = new byte[512];
				int readLen = 0;
				int destPos = 0;
				while ((readLen = is.read(temp)) > 0) {
					bos.write(temp, 0, readLen);
					destPos += readLen;
				}

				byte[] imgData = new byte[destPos];
				System.arraycopy(bos.toByteArray(), 0, imgData, 0, destPos);
				//设置最大显示值，图像在安卓中，最大不能超过4096*4096，遇到超大尺寸读图片直接缩放
				int targetW = 480;
				int targetH = 800;


				BitmapFactory.Options bmOptions = new BitmapFactory.Options();
				bmOptions.inJustDecodeBounds = true;
				int photoW = bmOptions.outWidth;
				int photoH = bmOptions.outHeight;



				//获取抽样比例
				int scaleFactor = 1;
				if ((targetW > 0) || (targetH > 0)) {
					scaleFactor = Math.min(photoW / targetW, photoH / targetH);
					if (scaleFactor == 0) {
						scaleFactor = 1;
					} else if (scaleFactor == 1) {//最少采样边长的1/2
						scaleFactor += 1;
					}
				}

				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize = scaleFactor;
				bmOptions.inPurgeable = true;






				bitmap = BitmapFactory.decodeByteArray(imgData, 0,
						imgData.length,bmOptions);
				// Save to cache
				if (file != null){
					writeBitmapToCache(imgData, file);
				}
			}


		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}

		return bitmap;
	}

	public void showpraiseUser(Context context,LinearLayout view,List<CommentUser> list){
		if(list == null || list.size()<=0){
			return;
		}
		new CanvasTexViewTask(context, view, list);
	}



	/**
	 * Write the special data to file
	 *
	 * @param imgData
	 * @param file
	 *            Save data, maybe is SDCard or cache
	 */
	private void writeBitmapToCache(byte[] imgData, File file) {

		FileOutputStream fos = null;
		BufferedOutputStream outPutBuffer = null;

		if (file != null) {
			try {
				fos = new FileOutputStream(file);

				outPutBuffer = new BufferedOutputStream(fos);
				outPutBuffer.write(imgData);
				outPutBuffer.flush();
				fos.flush();

			} catch (IOException e) {
				Log.d(TAG, e.toString());
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
					imgData = null;
					System.gc();

					if (outPutBuffer != null) {
						outPutBuffer.close();
					}
				} catch (IOException e) {
					Log.d(TAG, e.toString());
				}
			}
		}
	}



	class CanvasTexViewTask extends AsyncTask<Object, Void, List<CommentUser>> {
		/** After load picture, will set it to this view */
		private LinearLayout gView;


		private Context mContext;


		List<CommentUser> mList;

		public CanvasTexViewTask(Context context,LinearLayout view,List<CommentUser> list) {
			gView = view;
			mContext = context;
			this.mList = list;
		}


		@Override
		protected List<CommentUser> doInBackground(Object... str) {
			Log.e("CanvasTexViewTask","doInBackground");
			/*gView = str[0];
			mContext = (Context) str[1];
			string = str[2].toString();*/
			return mList;
		}

		/**
		 * After load picture is success, system will call this function to
		 * update UI
		 *
		 */
		@Override
		protected void onPostExecute(List<CommentUser> list) {
			Log.e("CanvasTexViewTask","onPostExecute");
			if (list != null && list.size()>0) {
				for (int i = 0; i < list.size(); i++) {
					TextView tv = new TextView(mContext);
					LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
					tv.setLayoutParams(param);
					tv.setText(list.get(i).nickname);
					final int pos = i;
					tv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent profileAlbumIntent = new Intent();
							profileAlbumIntent.setClass(mContext,MyAlbumActivity.class);
							profileAlbumIntent.putExtra("toUserID",mList.get(pos).uid);
							mContext.startActivity(profileAlbumIntent);

						}
					});
					gView.addView(tv);
					if (i!=list.size()-1) {
						TextView spliteTv = new TextView(mContext);
						LinearLayout.LayoutParams spliteTvparam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
						spliteTv.setLayoutParams(spliteTvparam);
						spliteTv.setText(",");
						gView.addView(spliteTv);
					}
				}
			}
		}
	}

	class CanvasImageTask extends AsyncTask<Object, Void, Bitmap> {
		/** After load picture, will set it to this view */
		private Object gView;

		/** According to this URL to downloadVoice load picture */
		private String url;
		private Context mContext;

		/** Down load and generate bitmap object */
		Bitmap bmp = null;

		/** Whether need write picture to cache */
		private boolean mIsCache = true;



		private int mResId;
		private boolean mIsHead;
		private boolean mIsNeedWriteFile;
		private ProgressBar mProgressBar;

		public CanvasImageTask(boolean isCache) {
			mIsCache = isCache;
		}

		/**
		 * If special URL file had not been cached, it will downloadVoice load from net,
		 * or load it from cache
		 *
		 * @param str
		 *            Include view object, URL and context
		 */
		@Override
		protected Bitmap doInBackground(Object... str) {
			// Decode parameters
			url = str[0].toString();
			gView = str[1];
			mContext = (Context) str[2];
			mResId = (Integer)str[3];
			mIsHead=(Boolean)str[4];
			mIsNeedWriteFile = (Boolean)str[5];
			mProgressBar = (ProgressBar) str[6];
			String fileName = new MD5().getMD5ofStr(url);
			File file = null;
			if (mIsNeedWriteFile) {
				if (!mImageBuffer.containsKey(url)){
					if (mIsCache) {
						if (str.length > 2) {
							file = new File(mContext.getCacheDir(), fileName);
						}
					} else {
						if (FeatureFunction.checkSDCard() && str.length > 2) {
							if (FeatureFunction.newFolder(Environment.getExternalStorageDirectory()
									+ SDCARD_PICTURE_CACHE_PATH)) {
								file = new File(Environment.getExternalStorageDirectory() + SDCARD_PICTURE_CACHE_PATH, fileName);
							}
						}
					}
					if (file != null && file.exists()) {
						bmp = FeatureFunction.tryToDecodeImageFile(file.getPath(),1, true);
						//mImageBuffer.put(url, bmp);
					}else {
						bmp = loadImageFromUrl(url, file);
						//mImageBuffer.put(url, bmp);
					}
				}
				else {
					bmp = loadImageFromUrl(url, file);
				}
			}
			else {
				if (!mHeaderBuffer.containsKey(url)){
					bmp = loadImageFromUrl(url, file);
				}
			}
			return bmp;
		}

		/**
		 * After load picture is success, system will call this function to
		 * update UI
		 *
		 */
		@Override
		protected void onPostExecute(Bitmap bm) {

			if (bm != null) {
				Bitmap bitmap = bm;
				if (gView instanceof ImageView) {
					ImageView imageView = (ImageView) gView;
					if (mIsHead) {
						/*if (mIsYuan) {
							//bitmap =getYuan(bm);
							bitmap = getRoundedCornerBitmap(bm, 16);
						}else{
							bitmap = getRoundedCornerBitmap(bm, 8);
						}*/
						bitmap = getRoundedCornerBitmap(bm, 15);
					}else {
						bitmap = bm;
					}
				/*	bitmap = resizeBitmap(bitmap, gScreenWidth - 15,
							gScreenHeight - 100);*/
					// bitmap = resizeBitmap(bitmap, Helper.gScreenWidth,
					// Helper.gScreenHeight);
					if(mProgressBar != null){
						mProgressBar.setVisibility(View.GONE);
					}
					imageView.setImageBitmap(bitmap);
				}else if(gView instanceof RelativeLayout){
					bitmap=bm.copy(Config.ARGB_8888, true);
					RelativeLayout relayout=(RelativeLayout)gView;
					BitmapDrawable bd=new BitmapDrawable(bm);
					relayout.setBackgroundDrawable(bd);
				}

				if (mIsNeedWriteFile) {
					mImageBuffer.put(url, bitmap);
				}
				else {
					mHeaderBuffer.put(url, bitmap);
				}

			}else{
				Log.e("ImageLoader", "bitmap is null "+url);
			}
		}
	}


	public static Bitmap resizeBitmap(Bitmap bm, int maxWidth, int maxHeight) {
		Bitmap BitmapOrg = bm;
		int width = 0, height = 0;
		if (BitmapOrg == null) {
			return null;
		} else {
			width = BitmapOrg.getWidth();
			height = BitmapOrg.getHeight();
		}

		if (width < 0 || height < 0) {
			return null;
		}
		float scaleWidth = (float) width;
		float scaleHeight = (float) height;

		float xscale = (float) maxWidth / (float) width;
		float yscale = (float) maxHeight / (float) height;
		float scale = 1.0F;

		if (xscale < 1.0F || yscale < 1.0F) {
			scale = Math.min(xscale, yscale);
			scaleWidth = scale * width;
			scaleHeight = scale * height;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createScaledBitmap(BitmapOrg, (int) scaleWidth,
				(int) scaleHeight, true);
	}
	/**
	 * Delete the modified time is more than fifteen days in SDCard
	 *
	 */
	public void clearTempFiles(){
		if (FeatureFunction.checkSDCard()){
			File file = new File(Environment.getExternalStorageDirectory() + SDCARD_PICTURE_CACHE_PATH);

			if (file != null && file.exists()){
				File[] filelist = file.listFiles();

				File tempfile = null;
				for(int i = 0; i < filelist.length; i++){
					tempfile = filelist[i];
					// Delete the modified time is more than fifteen days
					if (System.currentTimeMillis() - tempfile.lastModified() > FIFTEEN_DAYS){
						tempfile.delete();
					}
				}
			}
		}
	}
}
