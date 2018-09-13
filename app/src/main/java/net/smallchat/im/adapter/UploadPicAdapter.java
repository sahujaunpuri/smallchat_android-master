package net.smallchat.im.adapter;


import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import net.smallchat.im.R;
import net.smallchat.im.Entity.UploadImg;
import net.smallchat.im.global.FeatureFunction;

/**
 * 选择图片adapter
 * @author dongli
 *
 */
public class UploadPicAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<UploadImg> mData;
	private Context mContext;
	private boolean mIsDelete = false;
	private HashMap<String, Bitmap> mImageMap = new HashMap<String, Bitmap>();
	private int mWidth = 0;
	
	public UploadPicAdapter(Context context, List<UploadImg> data, int width){
		mInflater = (LayoutInflater)context.getSystemService(
	            Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = data;
		mWidth = width - FeatureFunction.dip2px(mContext, 20);
	}

	@Override
	public int getCount() {
		int columns = mData.size() / 4;
		if(mData.size() % 4 != 0){
			return (columns + 1) * 4;
		}else {
			mData.size();
		}
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if(position < mData.size()){
			return mData.get(position);
		}else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setIsDelete(boolean isDelete){
		mIsDelete = isDelete;
	}
	
	public boolean getIsDelete(){
		return mIsDelete;
	}

	public HashMap<String, Bitmap> getImageBuffer(){
		return mImageMap;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
        ViewHolder holder;  
        
        if (convertView==null) {  
        	convertView=mInflater.inflate(R.layout.upload_img_item, null);   
            holder=new ViewHolder();  
              
            holder.mHeaderView = (ImageView) convertView.findViewById(R.id.header);
            holder.mDeleteBtn = (ImageView) convertView.findViewById(R.id.deletebtn);
            holder.mPicLayout = (RelativeLayout) convertView.findViewById(R.id.piclayout);
            LinearLayout.LayoutParams params = new LayoutParams(mWidth / 4, mWidth / 4);
            holder.mPicLayout.setLayoutParams(params);
            int paddding = FeatureFunction.dip2px(mContext, 5);
            holder.mPicLayout.setPadding(paddding, paddding, paddding, paddding);
            convertView.setTag(holder);  
        }else {
        	holder=(ViewHolder) convertView.getTag();  
		}
        
        holder.mDeleteBtn.setVisibility(View.GONE);
        
        if(position < mData.size()){
        	if(mData.get(position).mType == 0){
        		holder.mHeaderView.setVisibility(View.VISIBLE);
            	if(mIsDelete){
            		holder.mDeleteBtn.setVisibility(View.VISIBLE);
            	}
            	
            	if(!TextUtils.isEmpty(mData.get(position).mPicPath)){
            		holder.mHeaderView.setTag(mData.get(position).mPicPath);
            		Bitmap bitmap = null;
            		if(mImageMap.get(mData.get(position).mPicPath) != null){
            			bitmap = mImageMap.get(mData.get(position).mPicPath);
            		}else {
            			bitmap = BitmapFactory.decodeFile(mData.get(position).mPicPath);
        				mImageMap.put(mData.get(position).mPicPath, bitmap);
					}
    				holder.mHeaderView.setImageBitmap(bitmap);
    			}
            }else {
            	if(mIsDelete){
            		holder.mHeaderView.setVisibility(View.INVISIBLE);
            	}else {
            		holder.mHeaderView.setVisibility(View.VISIBLE);
            	}
            	if(mData.get(position).mType == 1){
                	holder.mHeaderView.setImageResource(R.drawable.smiley_add_btn);
                }
            }
        }else {
        	holder.mHeaderView.setVisibility(View.INVISIBLE);
		}
        
        
        return convertView;
	}

	
	final static class ViewHolder {  
        ImageView mHeaderView;
        private ImageView mDeleteBtn;
        RelativeLayout  mPicLayout;
        
        @Override
        public int hashCode() {
			return this.mHeaderView.hashCode() + mDeleteBtn.hashCode() + mPicLayout.hashCode();
        }
    } 
	
}
