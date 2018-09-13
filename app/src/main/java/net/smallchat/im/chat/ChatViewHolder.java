package net.smallchat.im.chat;


import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.components.ProgressView;
import net.smallchat.im.components.XCRoundRectImageView;

public  class ChatViewHolder {
    public TextView txtTime, txtMsg, txtVoiceNum, mShideView,txtRevokeState;
    public ImageView imgHead, imgMsgVoice, imgSendState, imgVoiceReadState;
    public XCRoundRectImageView imgMsgPhoto;
    public CheckBox checkSelect;
    public  ProgressBar wiatProgressBar;
    public RelativeLayout mRootLayout, mDisplayLayout;
    public RelativeLayout.LayoutParams mParams;
    public RelativeLayout.LayoutParams mVoiceTimeParams;


    //名片
    public  LinearLayout cardLayout;
    public  ImageView cardHeader;
    public TextView cardName,cardEM;


    //文件
    public  LinearLayout fileLayout;
    public  TextView fileName;//文件名
    public TextView fileSize;//文件大小
    public ImageView fileIcon;//文件图标


    public  LinearLayout smallVideoLayout;//小视频布局
    public ImageView smallVideoThumb;//视频截图
    public ProgressView smallVideoProgress;//视频进度条
    //地图
    public  RelativeLayout mapLayout;
    public TextView txtMsgMap;
    public ImageView mapIcon;

    //昵称
    public TextView nickName;

    public static ChatViewHolder getInstance(View view){
        ChatViewHolder holder = new ChatViewHolder();


        holder.mRootLayout = (RelativeLayout) view.findViewById(R.id.chat_talk_msg_info);
        holder.mParams = (RelativeLayout.LayoutParams)holder.mRootLayout.getLayoutParams();
        holder.txtTime = (TextView) view.findViewById(R.id.chat_talk_txt_time);
        holder.txtMsg = (TextView) view.findViewById(R.id.chat_talk_msg_info_text);

        holder.checkSelect=(CheckBox) view.findViewById(R.id.chat_talk_select);
        holder.imgHead = (ImageView) view.findViewById(R.id.chat_talk_img_head);
        holder.imgMsgPhoto = (XCRoundRectImageView) view.findViewById(R.id.chat_talk_msg_info_msg_photo);
        holder.imgMsgVoice = (ImageView) view.findViewById(R.id.chat_talk_msg_info_msg_voice);

        holder.txtRevokeState = (TextView) view.findViewById(R.id.chat_talk_msg_revoke_sate);
        holder.imgSendState = (ImageView) view.findViewById(R.id.chat_talk_msg_sendsate);
        holder.wiatProgressBar = (ProgressBar) view.findViewById(R.id.chat_talk_msg_progressBar);
        holder.txtVoiceNum = (TextView) view.findViewById(R.id.chat_talk_voice_num);
        holder.mVoiceTimeParams = (RelativeLayout.LayoutParams)holder.txtVoiceNum.getLayoutParams();
        holder.imgVoiceReadState = (ImageView) view.findViewById(R.id.unread_voice_icon);

        //+++通讯录名片++//
        holder.cardLayout = (LinearLayout)view.findViewById(R.id.card_layout);
        holder.cardHeader = (ImageView)view.findViewById(R.id.card_header);
        holder.cardName = (TextView)view.findViewById(R.id.card_name);
        holder.cardEM = (TextView)view.findViewById(R.id.card_emal);
        //--通讯录名片--//


        //+++文件发送++//
        holder.fileLayout = (LinearLayout)view.findViewById(R.id.file_layout);
        holder.fileIcon = (ImageView)view.findViewById(R.id.file_icon);
        holder.fileName = (TextView)view.findViewById(R.id.file_name);
        holder.fileSize= (TextView)view.findViewById(R.id.file_size);

        //--文件发送--//

        //++地图++//
        holder.mapLayout = (RelativeLayout)view.findViewById(R.id.map_layout);
        holder.txtMsgMap = (TextView) view.findViewById(R.id.chat_talk_msg_map);
        holder.mapIcon = (ImageView)view.findViewById(R.id.map_icon);
        //--地图--//

        holder.nickName = (TextView)view.findViewById(R.id.from_message_nickname);

        // 小视频
        holder.smallVideoLayout = (LinearLayout) view.findViewById(R.id.chat_small_video_layout);//小视频布局
        holder.smallVideoThumb = (ImageView) view.findViewById(R.id.chat_small_video_thumb);//小视频缩略图
        holder.smallVideoProgress = (ProgressView) view.findViewById(R.id.chat_small_video_progress);//小视频缩略图

        return holder;
    }
}
