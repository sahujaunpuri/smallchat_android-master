package net.smallchat.im.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ImageView;

public class MyImageViewTouchBase extends ImageView{

	protected static final float SCALE_RATE = 1.5F;
	protected static final float MAX_ZOOM = 2.0F;
	
	protected Bitmap mBitmap = null;
	
	private Recycler mRecycler;
	
	private Runnable mOnLayoutRunnable = null;
	protected Handler mHandler = new Handler();
	
	protected Matrix mBaseMatrix = new Matrix();
	protected Matrix mSuppMatrix = new Matrix();
	protected Matrix mDisplayMatrix = new Matrix();
	protected float[] mMatrixValues = new float[9];
	
	protected int mThisWidth = -1;
	protected int mThisHeight = -1;
	
	protected float mMaxZoom;
	protected int mRotation;
	
	
	public MyImageViewTouchBase(Context context) {
		super(context);
		init();
	}
	
	
	public MyImageViewTouchBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	
	protected void init() {
		setScaleType(ImageView.ScaleType.MATRIX);
	}
	
	
	public void clear() {
		setImageRotateBitmapReset(null, 0, true);
	}
	
	
	public interface Recycler {
		public void recycler(Bitmap bitmap);
	}
	
	
	public void setRecycler(Recycler recycler) {
		mRecycler = recycler;
	}
	
	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		mThisWidth = right - left;
		mThisHeight = bottom - top;
		
		Runnable r = mOnLayoutRunnable;
		if(r != null) {
			mOnLayoutRunnable = null;
			r.run();
		}
		
		if(mBitmap != null) {
			getProperBaseMatrix(mBaseMatrix);
			setImageMatrix(getImageViewMatrix());
			mMaxZoom = maxZoom();
			//center(true, true);
		}
	}
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if((keyCode == KeyEvent.KEYCODE_BACK) && (getScale() > 1.0F)) {
			zoomTo(1.0F);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	public void setImageBitmap(Bitmap bitmap) {
		setImageBitmap(bitmap, mRotation);
	}
	
	
	private void setImageBitmap(Bitmap bitmap, int rotation) {
		super.setImageBitmap(bitmap);
		
		Drawable drawable = getDrawable();
		if(drawable != null) {
			drawable.setDither(true);
		}
		
		Bitmap old = mBitmap;
		mBitmap = bitmap;
		mRotation = rotation % 360;
		
		if( (old != null) && (old != bitmap) && (mRecycler != null) ) {
			mRecycler.recycler(old);
		}
	}
	
	
	public void setImageRotateBitmapReset(final Bitmap bitmap, final int rotation, final boolean reset) {
		final int viewWidth = getWidth();
		
		if(viewWidth <= 0) {
			mOnLayoutRunnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					setImageRotateBitmapReset(bitmap, mRotation, reset);
				}
			};
			return;
		}
		
		if(bitmap != null) {
			setImageBitmap(bitmap, rotation);
			getProperBaseMatrix(mBaseMatrix);
		} else {
			mBaseMatrix.reset();
			setImageBitmap(null, 0);
		}
		
		if(reset) {
			mSuppMatrix.reset();
		}
		
		setImageMatrix(getImageViewMatrix());
		mMaxZoom = maxZoom();
		
	}
	

	
	protected Matrix getImageViewMatrix() {
		mDisplayMatrix.set(mBaseMatrix);
		mDisplayMatrix.postConcat(mSuppMatrix);		
		return mDisplayMatrix;
	}

	
	protected void getProperBaseMatrix(Matrix matrix) {
		
		float viewWidth = getWidth();
		float viewHeight = getHeight();	
		
		float bitmapWidth = getBitmapWidth();
		float bitmapHeight = getBitmapHeight();

		matrix.reset();
		
		float widthScale = Math.min(viewWidth / bitmapWidth, 2.0F);
		float heightScale = Math.min(viewHeight / bitmapHeight, 2.0F);
		float scale = Math.min(widthScale, heightScale);
		
		matrix.postConcat(getRotateMatrix());
		matrix.postScale(scale, scale);
		matrix.postTranslate((viewWidth - bitmapWidth * scale) / MAX_ZOOM, (viewHeight - bitmapHeight * scale) / MAX_ZOOM);
	}
		

	protected float getValue(Matrix matrix, int whichValue) {
		matrix.getValues(mMatrixValues);
		return mMatrixValues[whichValue];
	}
	
	
	protected RectF getBitmapRect() {
		if(mBitmap == null) {
			return null;
		}
		Matrix matrix = getImageViewMatrix();
		RectF rectF = new RectF(0, 0, getBitmapWidth(), getBitmapHeight());
		matrix.mapRect(rectF);
		return rectF;
	}
	
	
	public float getScale() {
		return getScale(mSuppMatrix);
	}
	
	
	protected float getScale(Matrix matrix) {
		return getValue(matrix, 0);
	}
	

	protected void center(boolean horizontal, boolean vertical) {
		if(mBitmap == null) {
			return;
		}
		
		Matrix matrix = getImageViewMatrix();
		RectF rectF = new RectF(0.0F, 0.0F, mBitmap.getWidth(), mBitmap.getHeight());
		
		matrix.mapRect(rectF);
		
		float height = rectF.height();
		//Log.e("height", "height = " + height);
		float width = rectF.width();
		float deltaX = 0.0F;
		float deltaY = 0.0F;
		
		if(vertical) {
			int viewHeight = mThisHeight;
			//Log.e("viewHeight", "viewHeight = " + viewHeight);
			if(height < viewHeight) {
				//Log.e("rectF.top", "rectF.top = " + rectF.top);
				deltaY = (viewHeight - height) / 2.0F - rectF.top;
				//Log.e("deltaY", "deltaY = " + deltaY);
			} else if(rectF.top > 0.0F) {
				deltaY = -rectF.top;
			} else if(rectF.bottom < viewHeight) {
				deltaY = getHeight() - rectF.bottom;
			}
		}
		
		if(horizontal) {
			int viewWidth = mThisWidth;
			if(width < viewWidth) {
				deltaX = (viewWidth - width) / 2.0F - rectF.left;
			} else if(rectF.left > 0.0F) {
				deltaX = -rectF.left;
			} else if(rectF.right < viewWidth) {
				deltaX = viewWidth - rectF.right;
			}
		}
		
		postTranslate(deltaX, deltaY);

		RectF rectF2;
		{
			
			
			rectF2 = new RectF(0.0F, 0.0F, mBitmap.getWidth(), mBitmap.getHeight());
			
			mDisplayMatrix.mapRect(rectF2);
		}
		
		//postTranslate(240, 400);
		//scrollBy(deltaX, deltaY, 2000);
		setImageMatrix(getImageViewMatrix());
		//int a = 2 + 3;
		//int b = a;
	}
	protected void center(float dx, boolean vertical) {
		if(mBitmap == null) {
			return;
		}
		
		Matrix matrix = getImageViewMatrix();
		RectF rectF = new RectF(0.0F, 0.0F, mBitmap.getWidth(), mBitmap.getHeight());
		
		matrix.mapRect(rectF);
		
		float height = rectF.height();
		//float width = rectF.width();
		float deltaX = dx;
		float deltaY = 0.0F;
		
		if(vertical) {
			int viewHeight = mThisHeight;
			if(height < viewHeight) {
				deltaY = (viewHeight - height) / 2.0F - rectF.top;
			} else if(rectF.top > 0.0F) {
				deltaY = -rectF.top;
			} else if(rectF.bottom < viewHeight) {
				deltaY = getHeight() - rectF.bottom;
			}
		}
		
		
		postTranslate(deltaX, deltaY);
		//scrollBy(deltaX, deltaY, 2000);
		setImageMatrix(getImageViewMatrix());
	}
	protected void center(boolean horizontal, float dy) {
		if(mBitmap == null) {
			return;
		}
		
		Matrix matrix = getImageViewMatrix();
		RectF rectF = new RectF(0.0F, 0.0F, mBitmap.getWidth(), mBitmap.getHeight());
		
		matrix.mapRect(rectF);
		
		//float height = rectF.height();
		float width = rectF.width();
		float deltaX = 0.0F;
		float deltaY = dy;	
		
		
		if(horizontal) {
			int viewWidth = mThisWidth;
			if(width < viewWidth) {
				deltaX = (viewWidth - width) / 2.0F - rectF.left;
			} else if(rectF.left > 0.0F) {
				deltaX = -rectF.left;
			} else if(rectF.right < viewWidth) {
				deltaX = viewWidth - rectF.right;
			}
		}
		
		postTranslate(deltaX, deltaY);
		//scrollBy(deltaX, deltaY, 2000);
		setImageMatrix(getImageViewMatrix());
	}

	
	protected RectF getCenter(boolean horizontal, boolean vertical) {
		if(mBitmap == null) {
			return new RectF(0, 0, 0, 0);
		}
		
		RectF rectF = getBitmapRect();
		
		float height = rectF.height();
		float width = rectF.width();
		float deltaX = 0;
		float deltaY = 0;
		
		if(vertical) {
			int viewHeight = getHeight();
			if(height < viewHeight) {
				deltaY = (viewHeight - height) / 2.0F - rectF.top;
			} else if(rectF.top > 0.0F) {
				deltaY = -rectF.top;
			} else if(rectF.bottom < viewHeight) {
				deltaY = getHeight() - rectF.bottom;
			}
		}
		
		if(horizontal) {
			int viewWidth = getWidth();
			if(width < viewWidth) {
				deltaX = (viewWidth - width) / 2.0F - rectF.left;
			} else if(rectF.left > 0.0F) {
				deltaX = -rectF.left;
			} else if(rectF.right < viewWidth) {
				deltaX = viewWidth - rectF.right;
			}
		}
		
		return new RectF(deltaX, deltaY, 0, 0);
	}
	
	
	protected void postTranslate(float deltaX, float deltaY) {
		mSuppMatrix.postTranslate(deltaX, deltaY);
		setImageMatrix(getImageViewMatrix());
	}
	
	
	protected void postScale(float scale, float centerX, float centerY) {
		mSuppMatrix.postScale(scale, scale, centerX, centerY);
		setImageMatrix(getImageViewMatrix());
	}
	
	
	protected void zoomTo(float scale) {
		float cx = getWidth() / 2.0F;
		float cy = getHeight() / 2.0F;
		zoomTo(scale, cx, cy);
	}
	
	
	public void zoomTo(float scale, float durationMs) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;
		zoomTo(scale, cx, cy, durationMs);
	}
	
	protected void zoomTo(float scale, float centerX, float centerY) {
		if(scale > mMaxZoom) {
			scale = mMaxZoom;
		}
		
		float oldScale = getScale();
		float deltaScale = scale / oldScale;
		mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
		setImageMatrix(getImageViewMatrix());
		center(true, true);
	}
	
	protected void zoomTo(final float scale, final float centerX, final float centerY, final float durationMs) {
		final long startTime = System.currentTimeMillis();
		final float incrementPerMs = (scale - getScale()) / durationMs;
		final float oldScale = getScale();

		mHandler.post(new Runnable(){
			
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				float target = oldScale + (incrementPerMs * currentMs);
				
				zoomTo(target, centerX, centerY);
				
				if(currentMs < durationMs) {
					mHandler.post(this);
				}
			}
		});
	}
	
	
	protected void onZoom(float scale) {
		
	}
	
	
	protected void scrollBy(float x, float y) {
		if ( x != 0F && y != 0F)
			panBy(x, y);
	}
	
	protected void scrollBy(float distanceX, float distanceY, final float durationMs) {
		final float dx = distanceX;
		final float dy = distanceY;
		final long startTime = System.currentTimeMillis();
		
		mHandler.post(new Runnable() {
			float oldX = 0;
			float oldY = 0;
			
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				float x = dx * ( (currentMs = currentMs / durationMs - 1) * currentMs * currentMs + 1);
				float y = dy * (( currentMs = currentMs / durationMs - 1) * currentMs * currentMs +1);
				
				
				
				panBy((x - oldX), (y - oldY));
				oldX = x;
				oldY = y;
				
				
				if( (currentMs < durationMs)/* && newx != 0F && newy != 0F*/) {
					mHandler.post(this);
				}
			}
			
		});
	}
	
	
	
	private static float preTotalTime = 0;
	protected void scrollByV2(float distanceX, float distanceY, final float durationMs) {
		final float dx = distanceX;
		final float dy = distanceY;
		final long startTime = System.currentTimeMillis();
		
		
		mHandler.post(new Runnable() {
			float oldX = 0;
			float oldY = 0;
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				float x = dx * (currentMs - preTotalTime) / durationMs;
				float y = dy * (currentMs - preTotalTime) / durationMs;
				preTotalTime = currentMs;
				
				panBy((x - oldX), (y - oldY));
				oldX = x;
				oldY = y;
				
				
				if( (currentMs < durationMs)/* && newx != 0F && newy != 0F*/) {
					mHandler.post(this);
				}
			}
			
		});
	}
	
	protected void scrollByV3(float distanceX, float distanceY, final float durationMs) {
		final float dx = distanceX;
		final float dy = distanceY;
		
		mHandler.post(new Runnable() {
			
			int i = 0;
			@Override
			public void run() {
				float x = dx / durationMs;
				float y = dy / durationMs;
//				for(int i = 0; i < (int)durationMs; i++ ){
//					panBy(x, y);
//					Log.e("ImageViewTouchBaseView", "panBy--517");
//					try {
//						Thread.sleep(1);
//						mHandler.postDelayed(this, 100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				
				if (i < (int)durationMs) {
					panBy(x, y);
					i++;
					mHandler.postDelayed(this, 1);
					return;
				}
				x = dx %  durationMs;
				y = dy % durationMs;
				if (x != 0.0F || y != 0.0F ){					
					panBy(x, y);
				}				
			}
			
		});
	}
	
	
	protected void panBy(float dx, float dy) {
		RectF rectF = getBitmapRect();
		RectF sRectF = new RectF(dx, dy, 0, 0);
		
		updateRect(rectF, sRectF);
		
		postTranslate(dx, dy);
		//center(true, true);
	}
	
	
	protected void updateRect(RectF bitmapRect, RectF scrollRect) {
		
		float width = getWidth();
		float height = getHeight();
//		FaceDetector.Face f[] = new FaceDetector.Face[1];
//		
//		//scrollRect.inset(-r, -r);
//         if (scrollRect.left < 0) {
//        	 scrollRect.inset(-scrollRect.left, -scrollRect.left);
//         }
//
//         if (scrollRect.top < 0) {
//        	 scrollRect.inset(-scrollRect.top, -scrollRect.top);
//         }
//
//         if (scrollRect.right > bitmapRect.right) {
//        	 scrollRect.inset(scrollRect.right - bitmapRect.right, scrollRect.right - bitmapRect.right);
//         }
//
//         if (scrollRect.bottom > bitmapRect.bottom) {
//        	 scrollRect.inset(scrollRect.bottom - bitmapRect.bottom, scrollRect.bottom - bitmapRect.bottom);
//         }
		
		if(bitmapRect.top >= 0 && bitmapRect.top <= height) {
			scrollRect.top = 0;
		}
		if(bitmapRect.left >= 0 && bitmapRect.left <= width) {
			scrollRect.left = 0;
		}
		if(bitmapRect.bottom >= height ) {
			scrollRect.bottom = height;
		}
		if(bitmapRect.right >= width) {
			scrollRect.right = width;
		}
		if(bitmapRect.top + scrollRect.top >= 0 && bitmapRect.bottom > height) {
			scrollRect.top = (int)(0 - bitmapRect.top);
		}
		if(bitmapRect.bottom + scrollRect.top <= (height - 0) && bitmapRect.top < 0) {
			scrollRect.top = (int)((height - 0) - bitmapRect.bottom);
		}
		if(bitmapRect.left + scrollRect.left >= 0) {
			scrollRect.left = (int)(0 - bitmapRect.left);
		}
		if(bitmapRect.right + scrollRect.left <= (width - 0)) {
			scrollRect.left = (int)((width - 0) - bitmapRect.right);
		}
		
	}

	
	protected float maxZoom() {	
		if(mBitmap == null) {
			return 1F;
		}
		
		float fw = (float)mBitmap.getWidth() / (float)mThisWidth;
		float fh = (float)mBitmap.getHeight() / (float)mThisHeight;
		float max = Math.max( Math.max(fw, fh), 1.0F ) * 4.0F;
		return max;
	}
	
	
	public float getMaxZoom() {
		return mMaxZoom;
	}
	
	
	public Bitmap getDisplayBitmap() {
		return mBitmap;
	}
	
	
	public void dispose() {
		if(mBitmap != null) {
			if( !mBitmap.isRecycled() ) {
				mBitmap.recycle();
			}
		}
		clear();
	}

	
	public void zoomIn() {
		zoomIn(SCALE_RATE);
	}
	
	
	protected void zoomIn(float rate) {
		if(getScale() >= mMaxZoom) {
			return;
		}
		if(mBitmap == null) {
			return;
		}
		
		float cx = getWidth() / 2.0F;
		float cy = getHeight() / 2.0F;
		
		mSuppMatrix.postScale(rate, rate, cx, cy);
		setImageMatrix(getImageViewMatrix());
	}
	
	
	public void zoomOut() {
		zoomOut(SCALE_RATE);
	}
	
	
	protected void zoomOut(float rate) {
		if(mBitmap == null) {
			return;
		}
		
		float cx = getWidth() / 2.0F;
		float cy = getHeight() / 2.0F;
		
		Matrix tmp = new Matrix(this.mSuppMatrix);
		tmp.postScale(1.0F / rate, 1.0F / rate, cx, cy);
		
		if(getScale(tmp) < 1.0F) {
			mSuppMatrix.setScale(1.0F, 1.0F, cx, cy);
		} else {
			mSuppMatrix.postScale(1.0F / rate, 1.0F / rate, cx, cy);
		}
		
		setImageMatrix(getImageViewMatrix());
		//center(true, true);	
	}
	
	
	private boolean isOrientationChanged() {
		return (mRotation / 90) % 2 != 0;
	}
	
	
	private int getBitmapWidth() {
		if(isOrientationChanged()) {
			return mBitmap.getHeight();
		} else {
			return mBitmap.getWidth();
		}
	}
	
	
	private int getBitmapHeight() {
		if(isOrientationChanged()) {
			return mBitmap.getWidth();
		} else {
			return mBitmap.getHeight();
		}
		
	}
	
	
	private Matrix getRotateMatrix() {
		Matrix localMatrix = new Matrix();
		
		if( (mRotation != 0) && (mBitmap != null)) {
			int x = mBitmap.getWidth() / 2;
			int y = mBitmap.getHeight() / 2;
			
			localMatrix.preTranslate(-x, -y);
			localMatrix.postRotate(mRotation);
			localMatrix.postTranslate(x, y);
		}
		
		return localMatrix;
	}			
	
	
	public float getRotation() {
		return mRotation;
	}
	
	
	public void rotate(int rotation) {
		rotateTo(rotation + mRotation);
	}
	
	
	public void rotateTo(int rotation) {
		mRotation = rotation % 360;
		
		if(mBitmap == null) {
			return;
		}
		
		getProperBaseMatrix(mBaseMatrix);
		setImageMatrix(getImageViewMatrix());
		mMaxZoom = maxZoom();
		//center(true, true);
	}
	
}
