package net.smallchat.im.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义View，显示加载进度
 */
public class ProgressView extends View {

    /**
     * 进度最大值，默认为100
     */
    private int maxProgress = 100;

    /**
     * 当前进度值
     */
    private int currentProgress = 0;

    /**
     * 中心点x
     */
    private int cx;

    /**
     * 中心点y
     */
    private int cy;

    /**
     * 圆的半径
     */
    private int radiusCircle;
    /**
     * 弧的半径
     */
    private int ovalCircle;

    /**
     * 画笔
     */
    Paint p;

    /**
     * 画扇形的矩形区域
     */
    RectF rf;

    /**
     * 扇形与圆之间的距离
     */
    private int gap = 5;

    public ProgressView(Context context) {
        super(context);
        init(context);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        p = new Paint();
        //设置抗锯齿
        p.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取宽高，圆半径为较短的
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        radiusCircle = w > h ? h/2 : w/2-dip2px(getContext(),1);
        //弧的半径比圆半径小一些
        ovalCircle = radiusCircle - gap;
        //中心点
        cx = w / 2;
        cy = h / 2;
        //初始化画扇形区域
        rf = new RectF(cx-ovalCircle, cy-ovalCircle, cx + ovalCircle, cy + ovalCircle);
    }

    /**
     * 设置当前进度
     *
     * @param progress
     */
    public void setProgress(int progress) {
        currentProgress = progress;
        invalidate();
    }

    /**
     * 设置最大进度值
     *
     * @param max
     */
    public void setMaxProgress(int max) {
        maxProgress = max;
        invalidate();
    }

    /**
     * 获取当前进度值
     *
     * @return
     */
    public int getProgress() {
        return currentProgress;
    }

    /**
     * 获取最大进度值
     *
     * @return
     */
    public int getMax() {
        return maxProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p.reset();
        //设置画笔
        p.setColor(Color.WHITE);
        //空心
        p.setStyle(Paint.Style.STROKE);
        //边框宽高为1dp
        p.setStrokeWidth(dip2px(getContext(), 1));
        //１.先画外面一个圆
        canvas.drawCircle(cx, cy, radiusCircle, p);
        //２.再根据当前进度百分比画弧
        //实心
        p.setStyle(Paint.Style.FILL);
        //半透明
        p.setAlpha(0x88);
        //画扇形矩形区域
        canvas.drawArc(rf, 0, currentProgress * 360f / maxProgress, true, p);
    }


    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
