package net.smallchat.im.adapter;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import net.smallchat.im.R;
import net.smallchat.im.global.FeatureFunction;


public class EmojiAdapter extends BaseAdapter{
	private List<String> emojiList;
	private LayoutInflater mInflater;
	private Context mContext;
	private int mWidth = 0;
	
	public EmojiAdapter(Context context, List<String> emojiList, int width) {
		super();
		this.emojiList = emojiList;
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		mWidth = width - FeatureFunction.dip2px(mContext, 10);
	}

	@Override
	public int getCount() {
		return emojiList.size();
	}

	@Override
	public String getItem(int position) {
		return emojiList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;  
		if (convertView==null) {  
        	convertView=mInflater.inflate(R.layout.emoji_item, null);
            holder=new ViewHolder();  
            holder.mImageView = (ImageView) convertView.findViewById(R.id.emotion);
            holder.mPicLayout = (LinearLayout) convertView.findViewById(R.id.emolayout);
            LinearLayout.LayoutParams params = new LayoutParams(mWidth / 7, mWidth / 7);
            holder.mPicLayout.setLayoutParams(params);
            int paddding = FeatureFunction.dip2px(mContext, 5);
            holder.mPicLayout.setPadding(paddding, paddding, paddding, paddding);
            convertView.setTag(holder);  
        }else {
        	holder=(ViewHolder) convertView.getTag();  
		}
		
		try {
			String name = getItem(position);
			Field field = R.drawable.class.getDeclaredField(name);
			int resId = field.getInt(null);
			//Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resId);

			String filename="emoji/base/"+name+".png";
			Bitmap bitmap =EmojiUtil.getImageFromAssetsFile(mContext,name);
			if (bitmap != null) {
				//BitmapFactory.decodeResource(context.getResources(), resId);
				Drawable mDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
				holder.mImageView.setImageDrawable(mDrawable);
			}





		} catch (Exception e) {
		}
		
		return convertView;
	}

	
	final static class ViewHolder {  
        ImageView mImageView;
        LinearLayout  mPicLayout;
        
        @Override
        public int hashCode() {
			return this.mImageView.hashCode() + mPicLayout.hashCode();
        }
    } 
}
