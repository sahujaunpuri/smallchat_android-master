package net.smallchat.im.Entity;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage extends IMMessage {
    private static final long serialVersionUID = -4274108350647182194L;
    public String id; 			//消息ID
    public String tag = "";				//消息标识符
    public String fromId;  // 发送者id
    public String fromName; //发送者name
    public String fromUrl; //发送者头像
    public String toId; //接收者，可以是某人，也可以是某个群id
    public String toName;//接收者name
    public String toUrl;//接收者头像
    public String content; //消息的文字内容
    public int chatType = ChatType.PrivateMessage;//PrivateMessage-单聊 GroupMessage-群聊 MeetingMessage-会议 默认为PrivateMessage
    public int messageType=MessageType.TEXT;//1-文字 2-图片 3-声音 4-位置 5-通讯录名片 6-文件 7-小视频 默认 文字消息
    public long time;//发送消息的时间,毫秒（服务器生成）
    public Login mUser;
    public int sendState; // 消息发送成功与否的状态  0 失败 1 成功, 2 正在发送， 4， 正在下载，5 正在撤回 6 撤回失败 7 撤回成功
    public int readState; // 读取消息的状态.
    public long sendTime;   // 对方发送的时间
    public long pullTime;	// 得到消息的时间
    public int auto_id;
    public int sampleRate = 8000;		//播放音频采样率
    public int systemMessage = 0; //1-系统消息 0-聊天消息 2-会议消息
    public String cardOwerName;//拥有名片者姓名
    public int position;
    public int showSelect = 0; //0-不显示选择框 1-显示选择框
    public int selected =0; //0 -未选中 1 - 选中
    public int privacyMode =0; //0 -普通消息 1 隐私消息/阅后即焚
    public int encryptMode =0;//0 -普通消息 1 加密消息

    public MessageImage imageData =new MessageImage();//图片消息
    public MessageVideo videoData =new MessageVideo();//小视频消息
    public MessageLocation locationData =new MessageLocation();//地图消息
    public MessageAudio audioData =new MessageAudio();//语音留言
    public MessageCard cardData=new MessageCard();//名片消息
    public MessageTransfer transferData=new MessageTransfer();//转账消息
    public MessageFile  fileData=new MessageFile();//文件消息
    public MessageFavorite favoriteData=new MessageFavorite();//收藏消息
    public MessageRedPacket redpacketData=new MessageRedPacket();//红包消息




    public ChatMessage(){

    }


    public ChatMessage(JSONObject json){
        try {
            if(!json.isNull("messageType")){
                messageType = json.getInt("messageType");
            }
            if(!json.isNull("id")){
                id = json.getString("id");
            }

            if(!json.isNull("tag")){
                tag = json.getString("tag");
            }

            if(!json.isNull("chatType")){
                chatType = json.getInt("chatType");
            }

            if(!json.isNull("content")){
                content = json.getString("content");
            }

            if(!json.isNull("encryptMode")){
                encryptMode = json.getInt("encryptMode");
            }

            if(!json.isNull("privacyMode")){
                privacyMode = json.getInt("privacyMode");
            }

            if(!json.isNull("from")){
                JSONObject from = json.getJSONObject("from");
                fromId = from.getString("id");
                fromName= from.getString("name");
                fromUrl = from.getString("url");
            }

            if(!json.isNull("time")){
                time = json.getLong("time");
            }

            if(!json.isNull("to")){
                JSONObject to = json.getJSONObject("to");
                toId = to.getString("id");
                toName = to.getString("name");
                toUrl = to.getString("url");
            }
            mUser = new Login();
            mUser.nickname = fromName;
            mUser.headSmall = fromUrl;
            //将content转换成java对象
            this.convertContentToObject();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    public long getSendTime() {
        return sendTime;
    }

    public long getPullTime() {
        return pullTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public void setPullTime(long pullTime) {
        this.pullTime = pullTime;
    }


    public String getToId() {
        return toId;
    }

    public String getFromId() {
        return fromId;
    }

    public int getType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public int getSendState() {
        return sendState;
    }

    public int getReadState() {
        return readState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    public void setReadState(int readState) {
        this.readState = readState;
    }

    public void setType(int type) {
        this.messageType = type;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public int getShowSelect() {
        return showSelect;
    }

    public void setShowSelect(int showSelect) {
        this.showSelect = showSelect;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }




    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + tag.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChatMessage other = (ChatMessage) obj;
        if (tag != other.tag)
            return false;
        return true;
    }
    //将Java对象转换成CONTENT
    public void convertObjectToContent() {
        if(MessageType.AUDIO ==messageType){//语音消息处理
            this.content=JSON.toJSONString(this.audioData);
        }
        if(MessageType.IMAGE ==messageType){//图片消息处理
            this.content=JSON.toJSONString(this.imageData);
        }
        if(MessageType.FILE==messageType){//文件消息处理
            this.content=JSON.toJSONString(this.fileData);
        }
        if(MessageType.VIDEO ==messageType){//小视频处理
            this.content=JSON.toJSONString(this.videoData);
        }
        if(MessageType.LOCATION ==messageType){//地图位置消息处理
            this.content=JSON.toJSONString(this.locationData);
        }
        if(MessageType.TRANSFER==messageType){//转账消息处理
            this.content=JSON.toJSONString(this.transferData);
        }
        if(MessageType.REDPACKET==messageType){//红包消息处理
            this.content=JSON.toJSONString(this.redpacketData);
        }
    }


    //将content字段JSON内容转换成Java对象
    public void convertContentToObject() {
        Log.d("MESSAGE_INFO","convertContentToObject type="+messageType+"content="+this.content);
        if(MessageType.AUDIO ==messageType){//语音消息处理
            if(this.content!=null && !this.content.equals("") && this.content.startsWith("{")){
                MessageAudio obj =  JSON.parseObject(this.content, MessageAudio.class);
                this.audioData =obj;
            }
        }
        if(MessageType.IMAGE ==messageType){//图片消息处理
            if(this.content!=null && !this.content.equals("") && this.content.startsWith("{")){
                MessageImage obj =  JSON.parseObject(this.content, MessageImage.class);
                this.imageData =obj;
            }

        }
        if(MessageType.FILE==messageType){//文件消息处理
            if(this.content!=null && !this.content.equals("") && this.content.startsWith("{")){

                MessageFile obj =  JSON.parseObject(this.content, MessageFile.class);
                this.fileData=obj;

            }
        }
        if(MessageType.VIDEO ==messageType){//小视频处理
            if(this.content!=null && !this.content.equals("") && this.content.startsWith("{")){
                MessageVideo obj =  JSON.parseObject(this.content, MessageVideo.class);
                this.videoData =obj;
            }

        }

        if(MessageType.LOCATION ==messageType){//地图位置消息处理
            if(this.content!=null && !this.content.equals("") && this.content.startsWith("{")){
                MessageLocation obj =  JSON.parseObject(this.content, MessageLocation.class);
                this.locationData =obj;
            }

        }
        if(MessageType.TRANSFER==messageType){//转账消息处理
            if(this.content!=null && !this.content.equals("") && this.content.startsWith("{")){
                MessageTransfer obj =  JSON.parseObject(this.content, MessageTransfer.class);
                this.transferData=obj;
            }

        }
        if(MessageType.REDPACKET==messageType){//红包消息处理
            if(this.content!=null && !this.content.equals("") && this.content.startsWith("{")){
                MessageRedPacket obj =  JSON.parseObject(this.content, MessageRedPacket.class);
                this.redpacketData=obj;
            }
        }

    }


}
