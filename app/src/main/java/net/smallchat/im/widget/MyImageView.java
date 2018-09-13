package net.smallchat.im.widget;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyImageView extends MyImageViewTouchBase{

	public float MIN_ZOOM = 1.0F;
	protected static final float MAX_ZOOM = 3.0F;
	protected GestureListener mGestureListener;
	protected GestureDetector mGestureDetector;
	protected ScaleListener mScaleListener;
	protected ScaleGestureDetector mScaleGestureDetector;
	protected int mTouchSlop;
	protected int mDoubleTapDirection;
	protected float mScaleFactor = 1.5f;
	protected float mCurrentScaleFactor;
	public boolean bRotate90 = false;	// whether it is rotated 90
	
	public boolean mIsFrame = false;
	public static boolean mIsRotate = false;
	
	public MyImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	@SuppressWarnings("deprecation")
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
		mTouchSlop = ViewConfiguration.getTouchSlop();
		
		mGestureListener = new GestureListener();
		mGestureDetector = new GestureDetector(getContext(), mGestureListener, null, true);
		mScaleListener = new ScaleListener();
		mScaleGestureDetector = new ScaleGestureDetector(getContext(), mScaleListener);

		mCurrentScaleFactor = 1F;
		mDoubleTapDirection = 1;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(!mIsRotate){
			mScaleGestureDetector.onTouchEvent(event);
		}
		
		if(!mScaleGestureDetector.isInProgress()) {
			mGestureDetector.onTouchEvent(event);
		}
		
		int action = event.getAction();
		switch(action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:		
//			int myrotate = (int)getRotation();
//			if (myrotate == 90 || myrotate == 270 || myrotate == -90 || myrotate ==-270)
//				centerWithRomote();
//			else
//				Log.e("MultiTouchImageView", "ACTION_UP");
//				//centerWithoutRomote();
			break;
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE: 
			break;
	  }
		return true;
	}	

    private void centerWithoutRomote(){
    	/*boolean bXCenter = false;
    	boolean bYCenter = false;
    	*/
    	RectF rectF = getBitmapRect();
    	if(rectF == null){
    		return;
    	}
		float picwidth = rectF.width();
		float picheight = rectF.height();		
		
		float dx = 0;
		float dy = 0;
		
		float screenwidth = getWidth();
		float screenheight = getHeight();
					
		if (picwidth < screenwidth){
			float newx = screenwidth/2 - picwidth/2;
			dx = newx - rectF.left;
			//bXCenter = true;
			
		}
		else{
			// handle left side
			if ( rectF.left > 0 && rectF.right > screenwidth)
				dx = -rectF.left;
			// handle right side
			if ( rectF.right < screenwidth && rectF.left < 0)
				dx = screenwidth - rectF.right;
		}
		
		if ( picheight < screenheight){
			float newy = screenheight/2 - picheight/2;
				dy = newy - rectF.top;
			//bYCenter = true;
		}
		else
		{
			// handle top side
			if ( rectF.top > 0 && rectF.bottom > screenheight)
				dy = -rectF.top;
			// handle bottom side
			if ( rectF.bottom < screenheight && rectF.top < 0 )
				dy = screenheight - rectF.bottom;
		}
		
	
			if (dx != 0 || dy != 0)
				//panBy(dx, dy);
				scrollByV3(dx, dy, 15);
		
    }
    
    private void centerWithRomote(){
    	Matrix matrix = getImageViewMatrix();
		RectF rectF = new RectF(0.0F, 0.0F, mBitmap.getWidth(), mBitmap.getHeight());
		
		matrix.mapRect(rectF);
		float dx = 0;
		float dy = 0;
		
		float picwidth = rectF.width();
		float picheight = rectF.height();
		
		float screenwidth = getWidth();
		float screenheight = getHeight();
					
		if (picwidth < screenwidth){
			float newx = screenwidth/2 - picwidth/2;
			dx = newx - rectF.left;
			
		}
		else{
			// handle left side
			if ( rectF.left > 0 && rectF.right > screenwidth)
				dx = -rectF.left;
			// handle right side
			if ( rectF.right < screenwidth && rectF.left < 0)
				dx = screenwidth - rectF.right;
		}
		
		if ( picheight < screenheight){
			float newy = screenheight/2 - picheight/2;
				dy = newy - rectF.top;
		}
		else
		{
			// handle top side
			if ( rectF.top > 0 && rectF.bottom > screenheight)
				dy = -rectF.top;
			// handle bottom side
			if ( rectF.bottom < screenheight && rectF.top < 0 )
				dy = screenheight - rectF.bottom;
		}
		
		
		if (dx != 0 || dy != 0)
			//panBy(dx, dy);
			scrollByV3(dx, dy, 15);
    }
    
	protected float onDoubleTapPost(float scale, float maxZoom) {
		if(mDoubleTapDirection == 1) {
			//mScaleFactor = maxZoom;
			if((scale + ( mScaleFactor * 2)) <=0.5 * maxZoom) {
				return scale + mScaleFactor;
			} else {
				mDoubleTapDirection = -1;
				return 0.5f * maxZoom;
			}
		} else {
			mDoubleTapDirection = 1;
			return 1F;
		}
	}

	public void setIsFrame(boolean isframe){
		mIsFrame = isframe;
	}

	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			
			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return super.onSingleTapUp(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// TODO Auto-generated method stub
			float scale = getScale();
			float targetScale = scale;
			targetScale = onDoubleTapPost(scale, getMaxZoom());
			targetScale = Math.min(maxZoom(), Math.max(targetScale, MIN_ZOOM));
			mCurrentScaleFactor = targetScale;
			zoomTo(targetScale, e.getX(), e.getY(), 400);
			invalidate();
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			if(e1 == null || e2 == null) {
				return false;
			}
			if(e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
				return false;
			}
			if(mScaleGestureDetector.isInProgress()) {
				return false;
			}
			//Log.e("getScale()", "getScale = " + getScale());
			if((getScale() == 1F || getScale() == 1.0F) && !mIsFrame) {
				//Log.e("getScale()", "getScale = " + getScale());
				return false;
			}
			scrollBy( -distanceX, -distanceY);
			invalidate();
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			int myrotate = (int)getRotation();
			if (myrotate == 90 || myrotate == 270 || myrotate == -90 || myrotate ==-270){
				centerWithRomote();
			}else{
				//centerWithoutRomote();
				if(!mIsFrame){
					centerWithoutRomote();
				}
			}
			
			// TODO Auto-generated method stub
			/*if(e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
				return false;
			}
			if(mScaleGestureDetector.isInProgress()) {
				return false;
			}
			
			float diffX = e2.getX() - e1.getX();
			float diffY = e2.getY() - e1.getY();
			
			if(Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
				scrollBy(diffX / 2, diffY / 2, 500);
				invalidate();
			}
			return super.onFling(e1, e2, velocityX, velocityY);*/
			return true;
		}
	}
	
	
	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			float targetScale = mCurrentScaleFactor * detector.getScaleFactor();
			targetScale = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
			
			zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
			mCurrentScaleFactor = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
			mDoubleTapDirection = 1;
			
			invalidate();
			return true;
		}
	}
	
	public void setMinZoom(float minZoom){
		MIN_ZOOM = minZoom;
	}
}