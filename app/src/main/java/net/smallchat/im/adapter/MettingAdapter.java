package net.smallchat.im.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.MeetingItem;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;

/**
 * 会议适配器
 * @author dongli
 *
 */
public class MettingAdapter  extends BaseAdapter{

    public List<MeetingItem> mData;
    public Context mContext;
    LayoutInflater inflater;
    private Handler mHandler;
    private ImageLoader mImageLoader = new ImageLoader();
    
    public MettingAdapter(Context context, List<MeetingItem> list,Handler handler) {
        inflater = LayoutInflater.from(context);
        mContext = context;
        this.mData = list;
        this.mHandler = handler;
    }
    
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHoler;
        if (convertView == null || ((ViewHolder) convertView.getTag()).mTag != position) {
            viewHoler = new ViewHolder();
            convertView = inflater.inflate(R.layout.subscriptionnum_item, null);
            
            viewHoler.mHeaderView = (ImageView) convertView.findViewById(R.id.header_icon);
            viewHoler.mNameTextView = (TextView)convertView.findViewById(R.id.meeting_name);
            viewHoler.mTimeTextView = (TextView)convertView.findViewById(R.id.metting_time);
            viewHoler.mEndTiemTextView = (TextView)convertView.findViewById(R.id.metting_end_time);
            viewHoler.mJoinCountTextView = (TextView)convertView.findViewById(R.id.join_meeting_count);
            viewHoler.mUnReadCountTextView = (TextView)convertView.findViewById(R.id.message_count);
            viewHoler.mTag = position;
            convertView.setTag(viewHoler);
        }else{
            viewHoler = (ViewHolder)convertView.getTag();
        }
        
        MeetingItem item = mData.get(position);
        
     
        if(item.unread!=0 ||item.applyCount!=0){
        	if(item.applyCount!=0 && item.uid.equals(IMCommon.getUserId(mContext))){
        		viewHoler.mUnReadCountTextView.setVisibility(View.VISIBLE);
        	}else if(item.unread!=0){
        		viewHoler.mUnReadCountTextView.setVisibility(View.VISIBLE);
        		viewHoler.mUnReadCountTextView.setText(item.unread+"");
        	}else{
        		viewHoler.mUnReadCountTextView.setVisibility(View.GONE);
        	}
        }else{
        	viewHoler.mUnReadCountTextView.setVisibility(View.GONE);
        }
        
        if (item.metSmallLog != null && !item.metSmallLog.equals("")) {
            mImageLoader.getBitmap(mContext, viewHoler.mHeaderView,
                    null, item.metSmallLog, 0, false, true);
        }else{
        	viewHoler.mHeaderView.setImageResource(R.drawable.contact_default_header);
        }
        
        viewHoler.mNameTextView.setText(item.metName);
        if(item.metStartTime!=0){
        	 viewHoler.mTimeTextView .setText(FeatureFunction.formartTime(item.metStartTime, "yyyy-MM-dd HH:mm"));
        }
       
        if(item.metEndTime!=0){
        	viewHoler.mEndTiemTextView.setText(FeatureFunction.formartTime(item.metEndTime, "yyyy-MM-dd HH:mm"));
        }
       
        viewHoler.mJoinCountTextView.setText("已有"+item.memberCount+"人参会");
        
        return convertView;
    }
    
    
     class ViewHolder{
        public int mTag;
        public ImageView mHeaderView;
        public TextView mNameTextView;
        public TextView mTimeTextView;
        public TextView mEndTiemTextView;
        public TextView mJoinCountTextView;
        public TextView mUnReadCountTextView;
        @Override
        public int hashCode() {
            return this.mHeaderView.hashCode() + this.mNameTextView.hashCode()
            		+ mTimeTextView.hashCode() + mJoinCountTextView.hashCode()
            		+ mUnReadCountTextView.hashCode()+ mEndTiemTextView.hashCode();
        }
    }
}
