package net.smallchat.im.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.smallchat.im.Entity.Card;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.SmallVideo;
import net.smallchat.im.chat.ChatViewHolder;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.dialog.MMAlert.OnAlertSelectId;

import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.RoomTable;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MessageSendState;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.FilePreviewActivity;
import net.smallchat.im.MiniBrowserActivity;
import net.smallchat.im.R;
import net.smallchat.im.action.AudioPlayListener;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.components.LocationActivity;
import net.smallchat.im.components.ShowImageActivity;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.fragment.MiniBrowserFragment;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.FileTool;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.api.Utility;
import net.smallchat.im.smallvideo.PlayVideoActivity;
import net.smallchat.im.utils.ImageUtils;

import java.lang.ref.SoftReference;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

/*
 * 聊天消息适配器
 */
public class ChatMessageAdapter extends BaseAdapter {

    private static final String TAG = "chat_adapter";
    public static final String EMOJIREX = "emoji_[\\d]{0,3}";
    private final static int MAX_SECOND = 10;
    private final static int MIN_SECOND = 2;
    private  ImageLoader mImageLoader;
    public static final int MY_MESSAGE=0;//我自己到消息
    public static final int FRIEND_MESSAGE=1;//别人的消息
    private final LayoutInflater mInflater;
    HashMap<String, View> hashMap;
    private boolean mIsShowNickName;
    private ChatMainActivity mContext;
    private Login mLogin;
    private Login mFCustomerVo;
    private int mType;

    private HashMap<String, SoftReference<Bitmap>> mBitmapCache;
    private AudioPlayListener mPlayListener;
    private List<ChatMessage> chatMessages;
    public ChatMessageAdapter(ChatMainActivity context, AudioPlayListener playListener, HashMap<String, SoftReference<Bitmap>> bitmapCache, ImageLoader imageLoader, Login login, Login fCustomerVo, List<ChatMessage> messageList, int chatType){

        this.mType=chatType;
        this.mContext=context;
        this.mLogin=login;
        this.chatMessages =messageList;
        this.mFCustomerVo=fCustomerVo;
        this.mImageLoader=imageLoader;
        this.mBitmapCache=bitmapCache;

        this.mPlayListener=playListener;

        mInflater = (LayoutInflater)mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        hashMap= new HashMap<String, View>();
        if(mType == ChatType.GroupMessage){//群聊模式
            SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase();
            RoomTable roomTable = new RoomTable(db);
            Room room = roomTable.query(mFCustomerVo.uid);
            if(room!=null){
                mIsShowNickName = room.isShowNickname == 1?true:false;
            }
        }


    }



    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public ChatMessage getItem(int arg0) {
        return chatMessages.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setIsShowNickName(boolean isShow){
        this.mIsShowNickName =isShow;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {



        int type = 0;
        final ChatMessage chatMessage = chatMessages.get(position);
        if(chatMessage.getFromId().equals(mLogin.uid)){
            type = MY_MESSAGE;
        }else{
            type = FRIEND_MESSAGE;
        }

        ChatViewHolder viewHolder = null;
        convertView = hashMap.get(chatMessage.tag);

        if(convertView == null){
            if(FRIEND_MESSAGE == type){
                //朋友的消息。
                convertView = mInflater.inflate(R.layout.chat_talk_left, null);
            }else{
                //自己的消息
                convertView = mInflater.inflate(R.layout.chat_talk_right, null);
            }
            //聊天记录创建
            viewHolder = ChatViewHolder.getInstance(convertView);
            convertView.setTag(viewHolder);
            //存入聊天列表 hashMAP  聊天ID+UI
            hashMap.put(chatMessage.tag, convertView);
        }else{
            viewHolder = (ChatViewHolder) convertView.getTag();
        }
        //判断多选框是否显示
        if(viewHolder.checkSelect!=null){
            if(type==MY_MESSAGE) {
                if (chatMessage.getShowSelect() == 1) {
                    //显示选择框
                    viewHolder.checkSelect.setVisibility(View.VISIBLE);
                    //修改头像对齐样式
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewHolder.imgHead.getLayoutParams();
                    lp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    viewHolder.imgHead.setLayoutParams(lp);
                } else {
                    viewHolder.checkSelect.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewHolder.imgHead.getLayoutParams();
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    viewHolder.imgHead.setLayoutParams(lp);
                }
            }else if(type==FRIEND_MESSAGE){
                if (chatMessage.getShowSelect() == 1) {
                    //显示选择框
                    viewHolder.checkSelect.setVisibility(View.VISIBLE);

                } else {
                    viewHolder.checkSelect.setVisibility(View.INVISIBLE);
                }
            }
        }

        viewHolder.imgMsgPhoto.setImageBitmap(null);

        viewHolder.imgMsgPhoto.setImageResource(R.drawable.default_image);
        //单机聊天消息
        viewHolder.txtMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果消息中含有网址，点击则直接打开网址
                if(chatMessage.getContent().contains("http://")|| chatMessage.getContent().contains("https://"))
                {
                    String	url= Utility.getCompleteUrl(chatMessage.getContent());
                    Intent intent = new Intent(v.getContext(), MiniBrowserActivity.class);
                    intent.putExtra(MiniBrowserFragment.EXTRA_URL, url);
                    mContext.startActivity(intent);
                }
            }
        });
        if(chatMessage.messageType == MessageType.AUDIO && MessageSendState.SEND_STATE_SUCCESS == chatMessage.getSendState()) {
            //声音
            setOnLongClick(viewHolder.mRootLayout, position, 0, null, chatMessage);
        }else {
            if( MessageSendState.SEND_STATE_SUCCESS == chatMessage.getSendState()){
                //长按文本
                setOnLongClick(viewHolder.txtMsg, position, 1, chatMessage.getContent(), chatMessage);
                //长按图片
                setOnLongClick(viewHolder.imgMsgPhoto, position,0,null, chatMessage);
                //长按地图
                setOnLongClick(viewHolder.mapLayout,position,0,null, chatMessage);
                //长按名片
                setOnLongClick(viewHolder.cardLayout,position,0,null, chatMessage);
                //长按文件
                setOnLongClick(viewHolder.fileLayout,position,0,null, chatMessage);
            }

        }



			/*
			viewHolder.mCommentBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListView.setSelection(position);
					showMenuDialog(chatMessage);
				}
			});*/

        if(position >0){
            bindView(viewHolder, chatMessage, chatMessages.get(position-1).time);
        }else{
            bindView(viewHolder, chatMessage,0);
        }

        return convertView;
    }

    private void setOnLongClick(View v, final int position,final int type,
                                final String content,final ChatMessage msg){
        v.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                mContext.showContextMenu(v,position, type, content, msg);
                return true;
            }
        });
    }

    private void bindView(final ChatViewHolder viewHolder, final ChatMessage chatMessage,
                          final long lasttime){
        final int type = chatMessage.getFromId().equals(mLogin.uid) ? MY_MESSAGE : FRIEND_MESSAGE;
        if(MY_MESSAGE == type){
            viewHolder.nickName .setVisibility(View.GONE);



            if( MessageSendState.SEND_STATE_FAIL == chatMessage.getSendState()){
                viewHolder.imgSendState.setVisibility(View.VISIBLE);
            }else{
                viewHolder.imgSendState.setVisibility(View.GONE);
            }

            viewHolder.imgSendState.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showResendDialog(chatMessage);
                }
            });
            if( MessageSendState.SEND_STATE_REVOKING == chatMessage.getSendState()) {

//					viewHolder.txtRevokeState.setVisibility(View.VISIBLE);
//					viewHolder.mRootLayout.setVisibility(View.GONE);
//					viewHolder.txtRevokeState.setText("正在撤回消息...");

            }else if( MessageSendState.SEND_STATE_REVOKE_FAIL == chatMessage.getSendState()) {
                //撤回失败
                viewHolder.txtRevokeState.setVisibility(View.VISIBLE);
                viewHolder.mRootLayout.setVisibility(View.GONE);
                viewHolder.txtRevokeState.setText("消息撤回失败.");
            }

        }else{
            viewHolder.imgVoiceReadState.setVisibility(View.GONE);
            if(mIsShowNickName){
                viewHolder.nickName .setVisibility(View.VISIBLE);
                viewHolder.nickName.setText(chatMessage.fromName);
            }else{
                viewHolder.nickName .setVisibility(View.GONE);
            }

        }
        if(viewHolder.checkSelect!=null) {
            //选择框点击事件
            viewHolder.checkSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //选中消息
                    if(chatMessage.getSelected()==1) {
                        chatMessage.setSelected(0);
                    }else {
                        chatMessage.setSelected(1);
                    }
                }
            });
        }

        viewHolder.imgHead.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("uid", chatMessage.fromId);
                if(type == MY_MESSAGE){
                    intent.putExtra("isLogin", 1);
                }
                intent.putExtra("type", 2);
                mContext.startActivity(intent);
            }
        });

        if(!TextUtils.isEmpty(chatMessage.fromUrl)){
            viewHolder.imgHead.setTag(chatMessage.fromUrl);
            //viewHolder.imgHead.setOnClickListener(showInfo);
            mImageLoader.getBitmap(mContext, viewHolder.imgHead, null, chatMessage.fromUrl, 0, false, false);
        }

        //viewHolder.txtTime.setVisibility(View.GONE);
        String time = FeatureFunction.calculaterReleasedTime(mContext,
                new Date(chatMessage.time), chatMessage.time,lasttime);
        if(time == null || time.equals("")){
            viewHolder.txtTime.setVisibility(View.GONE);
        }else{
            viewHolder.txtTime.setVisibility(View.VISIBLE);
            viewHolder.txtTime.setText(time);
        }

        if(chatMessage.messageType == MessageType.AUDIO) {//语音消息的消息长度显示
            int length = chatMessage.audioData.time;
            float max = mContext.getResources().getDimension(R.dimen.voice_max_length);
            float min = mContext.getResources().getDimension(R.dimen.voice_min_length);
            int width = (int) min;
            if (length >= MIN_SECOND && length <= MAX_SECOND) {
                width += (length - MIN_SECOND) * (int) ((max - min) / (MAX_SECOND - MIN_SECOND));
            } else if (length > MAX_SECOND) {
                width = (int) max;
            }

            RelativeLayout.LayoutParams timeParams = (RelativeLayout.LayoutParams) viewHolder.txtVoiceNum.getLayoutParams();
            //timeParams.addRule(RelativeLayout.CENTER_VERTICAL);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(width, FeatureFunction.dip2px(mContext, 48));
            if (type == MY_MESSAGE) {
                //timeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                //timeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, viewHolder.mRootLayout.getId());
                param.addRule(RelativeLayout.LEFT_OF, viewHolder.imgHead.getId());
                param.setMargins(0, FeatureFunction.dip2px(mContext, 5), FeatureFunction.dip2px(mContext, 5), FeatureFunction.dip2px(mContext, 5));
            } else {
                //timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                //timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, viewHolder.mRootLayout.getId());
                param.addRule(RelativeLayout.RIGHT_OF, viewHolder.imgHead.getId());
                if (mIsShowNickName) {
                    param.setMargins(FeatureFunction.dip2px(mContext, 5), FeatureFunction.dip2px(mContext, 25), 0, FeatureFunction.dip2px(mContext, 5));
                } else {
                    param.setMargins(FeatureFunction.dip2px(mContext, 5), FeatureFunction.dip2px(mContext, 5), 0, FeatureFunction.dip2px(mContext, 5));
                }

            }
            param.addRule(RelativeLayout.BELOW, viewHolder.txtTime.getId());
            //viewHolder.txtVoiceNum.setLayoutParams(timeParams);
            viewHolder.mRootLayout.setLayoutParams(param);
        } else {//其他消息
            int padding = FeatureFunction.dip2px(mContext, 5);
            if(type == MY_MESSAGE){
                viewHolder.mParams.setMargins(0, padding, FeatureFunction.dip2px(mContext, 5), FeatureFunction.dip2px(mContext, 5));
            }else {
                viewHolder.mParams.setMargins(FeatureFunction.dip2px(mContext, 5), padding, 0, FeatureFunction.dip2px(mContext, 5));
            }
            viewHolder.mRootLayout.setLayoutParams(viewHolder.mParams);
        }
        //显示不同消息类型
        switch (chatMessage.messageType) {
            case MessageType.TEXT://文本消息
                viewHolder.mRootLayout.setVisibility(View.VISIBLE);
                viewHolder.mapLayout.setVisibility(View.GONE);
                viewHolder.cardLayout.setVisibility(View.GONE);
                viewHolder.fileLayout.setVisibility(View.GONE);
                viewHolder.txtMsgMap.setVisibility(View.GONE);
                viewHolder.txtVoiceNum.setVisibility(View.GONE);
                viewHolder.smallVideoLayout.setVisibility(View.GONE);

                if(viewHolder.wiatProgressBar != null){
                    viewHolder.wiatProgressBar.setVisibility(View.GONE);
                }
                viewHolder.imgMsgVoice.setVisibility(View.GONE);
                viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                viewHolder.txtMsg.setVisibility(View.VISIBLE);
                viewHolder.txtMsg.setText(EmojiUtil.getExpressionString(this.mContext.getBaseContext(), chatMessage.getContent(), EMOJIREX));

                break;
            case MessageType.IMAGE://图片消息
                Log.d(TAG," IMAGE  MESSAGE");
                viewHolder.mRootLayout.setVisibility(View.GONE);
                viewHolder.mapLayout.setVisibility(View.GONE);
                viewHolder.cardLayout.setVisibility(View.GONE);
                viewHolder.fileLayout.setVisibility(View.GONE);
                viewHolder.txtMsgMap.setVisibility(View.GONE);
                viewHolder.txtVoiceNum.setVisibility(View.GONE);
                viewHolder.imgMsgPhoto.setVisibility(View.VISIBLE);
                viewHolder.smallVideoLayout.setVisibility(View.GONE);

                String urlpic = chatMessage.imageData.smallUrl;
                if(urlpic == null || urlpic.equals("")){
                    urlpic = chatMessage.imageData.largeUrl;
                }
                final String path = urlpic;


                viewHolder.imgMsgPhoto.setTag(path);
                Log.d(TAG,"IMAGE path="+path);
                if(path.startsWith("http://") &&  MessageSendState.SEND_STATE_SUCCESS == chatMessage.getSendState()){
                    Log.d(TAG,"显示远程图片");
                    Log.d(TAG,"path=="+path);
                    if(viewHolder.wiatProgressBar != null){
                        viewHolder.wiatProgressBar.setVisibility(View.VISIBLE);
                    }
                    viewHolder.imgMsgVoice.setVisibility(View.GONE);
                    viewHolder.txtMsg.setVisibility(View.GONE);

                    viewHolder.imgMsgPhoto.setTag(path);
                    Log.d(TAG,"IMAGE===="+path);
                    if(!mImageLoader.getImageBuffer().containsKey(path)){
                        viewHolder.imgMsgPhoto.setImageBitmap(null);
                        viewHolder.imgMsgPhoto.setImageResource(R.drawable.default_image);
                        mImageLoader.getBitmap(mContext, viewHolder.imgMsgPhoto, viewHolder.wiatProgressBar, path, 0, false, false);
                    }else {
                        viewHolder.wiatProgressBar.setVisibility(View.GONE);
                        viewHolder.imgMsgPhoto.setImageBitmap(mImageLoader.getImageBuffer().get(path));
                    }

                    viewHolder.imgMsgPhoto.setVisibility(View.VISIBLE);

                    viewHolder.imgMsgPhoto.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Log.d(TAG,"Image Message click url="+ chatMessage.imageData.largeUrl);
                            Intent intent = new Intent(mContext, ShowImageActivity.class);
                            intent.putExtra("imageurl", chatMessage.imageData.largeUrl);
                            intent.putExtra("message", chatMessage);
                            String ownerId ="";
                            String groupId = "";

                            if(chatMessage.getFromId().equals(mLogin.uid)){
                                if(chatMessage.chatType == ChatType.GroupMessage){
                                    groupId = chatMessage.fromId;
                                }else if(chatMessage.chatType == ChatType.PrivateMessage){
                                    ownerId = chatMessage.fromId;
                                }
                            }else{
                                if(chatMessage.chatType == ChatType.GroupMessage){
                                    groupId = chatMessage.toId;
                                }else if(chatMessage.chatType == ChatType.PrivateMessage){
                                    ownerId = chatMessage.toId;
                                }

                            }
                            intent.putExtra("fuid", ownerId);
                            intent.putExtra("groupid",groupId);
                            mContext.startActivity(intent);
                        }
                    });

                }else{
                    //显示本地图片
                    Log.d(TAG,"显示本地图片");
                    Log.d(TAG,"path=="+path);
                    Bitmap bitmap = null;
                    if(!mBitmapCache.containsKey(path)){

                        bitmap= ImageUtils.getSmallBitmap(path);
                        mBitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
                    }else {
                        bitmap = mBitmapCache.get(path).get();
                    }
                    if(bitmap!=null && !bitmap.isRecycled()){
                        viewHolder.imgMsgPhoto.setImageBitmap(bitmap);
                    }else{
						bitmap = ImageUtils.getSmallBitmap(path);
						if(bitmap!=null) {
                            viewHolder.imgMsgPhoto.setImageBitmap(bitmap);
                        }
					}

                    viewHolder.imgMsgPhoto.setVisibility(View.VISIBLE);
                    if(viewHolder.wiatProgressBar != null){
                        viewHolder.imgMsgVoice.setVisibility(View.GONE);
                        //viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                        viewHolder.txtMsg.setVisibility(View.GONE);

                        if( MessageSendState.SEND_STATE_SENDING == chatMessage.getSendState()){
                            viewHolder.wiatProgressBar.setVisibility(View.VISIBLE);
                        }else {
                            viewHolder.wiatProgressBar.setVisibility(View.GONE);
                        }
                    }
                }
                break;
            case MessageType.AUDIO://语言消息
                viewHolder.mRootLayout.setVisibility(View.VISIBLE);
                viewHolder.mapLayout.setVisibility(View.GONE);
                viewHolder.cardLayout.setVisibility(View.GONE);
                viewHolder.smallVideoLayout.setVisibility(View.GONE);
                if( MessageSendState.SEND_STATE_SENDING == chatMessage.getSendState()){
                    if(viewHolder.wiatProgressBar != null){
                        viewHolder.imgMsgVoice.setVisibility(View.GONE);
                        viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                        viewHolder.txtMsg.setVisibility(View.GONE);
                        viewHolder.wiatProgressBar.setVisibility(View.VISIBLE);
                    }
                }else{
                    if(viewHolder.wiatProgressBar != null){
                        viewHolder.wiatProgressBar.setVisibility(View.GONE);
                    }
					/*if(4 == chatMessage.getSendState()){
						downVoice(chatMessage);
					}*/
                    viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                    viewHolder.imgMsgVoice.setVisibility(View.VISIBLE);
                    viewHolder.txtMsg.setVisibility(View.GONE);
                    viewHolder.mRootLayout.setTag(chatMessage);
                    viewHolder.mRootLayout.setOnClickListener(mPlayListener);
                    viewHolder.txtVoiceNum.setVisibility(View.VISIBLE);
                    viewHolder.txtVoiceNum.setText(chatMessage.audioData.time + "''");
                    //viewHolder.imgMsgVoice.setLayoutParams(new Rela)
                    try {
                        AnimationDrawable drawable = (AnimationDrawable) viewHolder.imgMsgVoice.getDrawable();
                        if (mPlayListener.getMessageTag().equals(chatMessage.tag)) {
                            drawable.start();
                        }else {
                            drawable.stop();
                            drawable.selectDrawable(0);
                        }

                    } catch (Exception e) {

                    }
                }
                break;

            case MessageType.LOCATION://地图消息
                viewHolder.mRootLayout.setVisibility(View.VISIBLE);
                viewHolder.mapLayout.setVisibility(View.VISIBLE);
                viewHolder.cardLayout.setVisibility(View.GONE);
                viewHolder.txtVoiceNum.setVisibility(View.GONE);
                if(viewHolder.wiatProgressBar != null){
                    viewHolder.wiatProgressBar.setVisibility(View.GONE);
                }
                viewHolder.imgMsgVoice.setVisibility(View.GONE);
                viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                viewHolder.txtMsg.setVisibility(View.GONE);
                try {
                    if(!TextUtils.isEmpty(chatMessage.locationData.address)){
                        viewHolder.txtMsgMap.setText(chatMessage.locationData.address);
                    }

                    //http://api.map.baidu.com/staticimage?center=103.978683,30.739114&width=300&height=200&zoom=16&&markerStyles=s
                    //http://api.map.baidu.com/staticimage?center=103.978683,30.739114&width=200&height=120&zoom=16&markers=103.978683,30.739114&markerStyles=s
                    //显示地图所列图示例
					/*String ImageURL = "http://api.map.baidu.com/staticimage?center="+chatMessage.mLng+
							","+chatMessage.mLat+"&width=300&height=200&zoom=16";*/
                    String ImageURL = "http://api.map.baidu.com/staticimage?center="+ chatMessage.locationData.lng+","+ chatMessage.locationData.lat+
                            "&width=200&height=140&zoom=16&markers="+ chatMessage.locationData.lng+","+ chatMessage.locationData.lat+"&markerStyles=s";
                    mImageLoader.getBitmap(mContext, viewHolder.mapIcon, null,ImageURL,0,false,true);

                    viewHolder.mapLayout.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent  = new Intent(mContext, LocationActivity.class);
                            intent.putExtra("show", true);
                            intent.putExtra("lat", chatMessage.locationData.lat);
                            intent.putExtra("lng", chatMessage.locationData.lng);
                            intent.putExtra("addr", chatMessage.locationData.address);
                            String ownerId ="";
                            String groupId = "";

                            if(chatMessage.getFromId().equals(mLogin.uid)){
                                if(chatMessage.chatType == ChatType.GroupMessage){
                                    groupId = chatMessage.fromId;
                                }else if(chatMessage.chatType == ChatType.PrivateMessage){
                                    ownerId = chatMessage.fromId;
                                }
                            }else{
                                if(chatMessage.chatType == ChatType.GroupMessage){
                                    groupId = chatMessage.toId;
                                }else if(chatMessage.chatType == ChatType.PrivateMessage){
                                    ownerId = chatMessage.toId;
                                }

                            }
                            intent.putExtra("fuid", ownerId);
                            intent.putExtra("groupid", groupId);
                            mContext.startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                }

                viewHolder.txtMsgMap.setTag(chatMessage);
                break;
            case MessageType.CARD://名片消息
                viewHolder.mRootLayout.setVisibility(View.VISIBLE);
                viewHolder.mapLayout.setVisibility(View.GONE);
                viewHolder.txtVoiceNum.setVisibility(View.GONE);
                if(viewHolder.wiatProgressBar != null){
                    viewHolder.wiatProgressBar.setVisibility(View.GONE);
                }
                viewHolder.imgMsgVoice.setVisibility(View.GONE);
                viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                viewHolder.txtMsg.setVisibility(View.GONE);
                viewHolder.txtMsgMap.setVisibility(View.GONE);
                viewHolder.cardLayout.setVisibility(View.VISIBLE);
                viewHolder.smallVideoLayout.setVisibility(View.GONE);

                final Card card = Card.getInfo(chatMessage.getContent());
                if(card!=null){
                    final String userId = card.uid;
                    if(card.headSmall!=null && !card.headSmall.equals("")){
                        mImageLoader.getBitmap(mContext, viewHolder.cardHeader, null, card.headSmall,0,false,false);
                    }
                    viewHolder.cardLayout.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if(userId != null && !userId.equals("")){
								/*SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getReadableDatabase();
								UserTable userTable = new UserTable(dbDatabase);
								Login user = userTable.query(userId);*/

                                Intent intent = new Intent(mContext, UserInfoActivity.class);
                                intent.putExtra("uid",card.uid);
                                intent.putExtra("type",2);
                                mContext.startActivity(intent);
                            }

                        }
                    });

                    viewHolder.cardName.setText(card.nickname);
                    viewHolder.cardEM.setText(card.content);
                }
                break;
            case MessageType.FILE://文件消息
                viewHolder.mRootLayout.setVisibility(View.VISIBLE);
                viewHolder.mapLayout.setVisibility(View.GONE);
                viewHolder.txtVoiceNum.setVisibility(View.GONE);
                if(viewHolder.wiatProgressBar != null){
                    viewHolder.wiatProgressBar.setVisibility(View.GONE);
                }
                viewHolder.imgMsgVoice.setVisibility(View.GONE);
                viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                viewHolder.txtMsg.setVisibility(View.GONE);
                viewHolder.txtMsgMap.setVisibility(View.GONE);
                viewHolder.cardLayout.setVisibility(View.GONE);
                viewHolder.fileLayout.setVisibility(View.VISIBLE);
                viewHolder.smallVideoLayout.setVisibility(View.GONE);
                final net.smallchat.im.Entity.File file = net.smallchat.im.Entity.File.getInfo(chatMessage.content);
                Log.d(TAG,"show chat message file type msg start:");
                if(file!=null){
                    Log.d(TAG,"show chat message file type msg:process file ");
                    final String fileUrl = file.url;
                    final String fileType = file.type;
                    Log.d(TAG,"show chat message file type msg:show file name");
                    if(file.filename !=null && !file.filename.equals("")){
                        Log.d(TAG,"show chat message file type msg:file name= "+file.filename);
                        viewHolder.fileName.setText(file.filename);
                        //mImageLoader.getBitmap(mContext, viewHolder.cardHeader, null, file.headSmall,0,false,false);
                    }
                    Log.d(TAG,"show chat message file type msg:show file ext ");
                    if(file.ext !=null && !file.ext.equals("")){
                        Log.d(TAG,"show chat message file type msg:file ext= "+file.ext);
                        int fileIcon=FileTool.getFileIcon(file.ext);
                        viewHolder.fileIcon.setImageResource(fileIcon);

                    }
                    viewHolder.fileLayout.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if(fileUrl != null && !fileUrl.equals("")){
                                //点击文件，打开文件预览界面
                                Intent intent = new Intent(mContext, FilePreviewActivity.class);
                                intent.putExtra("url",file.url);
                                intent.putExtra("filePath",file.url);
                                intent.putExtra("filename",file.filename);
                                intent.putExtra("ext",file.ext);
                                intent.putExtra("type",fileType);
                                mContext.startActivity(intent);
                            }

                        }
                    });
                    Log.d(TAG,"show chat message file type msg:show file size ="+ FileTool.getFileSizeString(file.size));
                    viewHolder.fileSize.setText(""+FileTool.getFileSizeString(file.size));
                }
                break;
            case MessageType.VIDEO://小视频
                Log.d(TAG,"显示小视频消息");
                viewHolder.mRootLayout.setVisibility(View.GONE);
                viewHolder.mapLayout.setVisibility(View.GONE);
                viewHolder.txtVoiceNum.setVisibility(View.GONE);
                if(viewHolder.wiatProgressBar != null){
                    viewHolder.wiatProgressBar.setVisibility(View.GONE);
                }
                viewHolder.imgMsgVoice.setVisibility(View.GONE);
                viewHolder.imgMsgPhoto.setVisibility(View.GONE);
                viewHolder.txtMsg.setVisibility(View.GONE);
                viewHolder.txtMsgMap.setVisibility(View.GONE);
                viewHolder.smallVideoLayout.setVisibility(View.VISIBLE);
                final SmallVideo smallVideo = SmallVideo.getInfo(chatMessage.getContent());
                if(smallVideo!=null){
                    if(smallVideo.thumb!=null && !smallVideo.thumb.equals("")){
                        mImageLoader.getBitmap(mContext, viewHolder.smallVideoThumb, null, smallVideo.thumb,0,false,false);
                    }
                    //小视频下载地址
                    final String videoUrl=smallVideo.url;
                    viewHolder.smallVideoThumb.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Log.d(TAG,"Click Video videoUrl = "+videoUrl);
                            if(videoUrl != null && !videoUrl.equals("")){
                                //打开小视频播放界面
                                Intent intent = new Intent(mContext, PlayVideoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("videoPath", videoUrl);
                                intent.putExtras(bundle);
                                mContext.startActivity(intent);
                            }else
                            {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.small_video_not_found), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }else {
                    Log.d(TAG,"smallVideo is null");
                }
                break;
            default:
                break;
        }
    }

    private void showResendDialog(final ChatMessage chatMessage){


        MMAlert.showAlert(mContext, "", mContext.getResources().
                        getStringArray(R.array.resend_item),
                null, new OnAlertSelectId() {

                    @Override
                    public void onClick(int whichButton) {
                        switch (whichButton) {
                            case 0:
                                chatMessage.sendState = MessageSendState.SEND_STATE_SENDING;
                                ChatMessageAdapter.this.notifyDataSetChanged();
                                mContext.btnResendAction(chatMessage);
                                break;
                            default:
                                break;
                        }
                    }
                });


    }

}
