package net.smallchat.im.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;

import net.smallchat.im.R;


public class EmojiUtil {
	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替 规则[...]
	 */
    public static void dealExpression(Context context,SpannableString spannableString, Pattern patten, int start) throws Exception {
    	// 开始匹配真正则
    	Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) { // 如果找到
            String key = matcher.group();
            // 从指定的位置开始匹配
            if (matcher.start() < start) {
                continue;
            }
            
            // 得到图片在R 文件中的属性
            //Field field = R.drawable.class.getDeclaredField(key);
            // 得到该属性的值
			//int resId = field.getInt(null);
            String filename="emoji/base/emoji_"+key+".png";
            Bitmap bitmap =getImageFromAssetsFile(context,key);
            if (bitmap != null) {
                        //BitmapFactory.decodeResource(context.getResources(), resId);
                Drawable mDrawable = new BitmapDrawable(context.getResources(), bitmap);
		        int width = context.getResources().getDimensionPixelSize(R.dimen.pl_emoji);
		        int height = width;
		        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0); 
				ImageSpan span = new ImageSpan(mDrawable);				            
                int end = matcher.start() + key.length();	
                // 使用[]进行约束
                spannableString.setSpan(span, matcher.start()-1, end +1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);	
                if (end < spannableString.length()) {						
                    dealExpression(context,spannableString,  patten, end);
                }
                break;
            }
        }
    }
    /*
    *
    *
    * 从Assets中读取图片
    *
    */
    public static Bitmap getImageFromAssetsFile(Context context,String fileName)
    {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try
        {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;

    }
    public static SpannableString getExpressionString(Context context,String str,String zhengze){
        if(str==null){str="";}
    	// 封装文本消息
    	SpannableString spannableString = new SpannableString(str);
    	// 封装正则表达式
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);		//通过传入的正则表达式来生成一个pattern
        try {
            dealExpression(context,spannableString, sinaPatten, 0);
        } catch (Exception e) {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }
}