package net.smallchat.im.components.multi_image_selector.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/** An image view which always remains square with respect to its width. */
@SuppressLint("AppCompatCustomView")
public  class SquaredImageView extends ImageView {
  public SquaredImageView(Context context) {
    super(context);
  }
  @SuppressLint("AppCompatCustomView")
  public SquaredImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
  }
}
