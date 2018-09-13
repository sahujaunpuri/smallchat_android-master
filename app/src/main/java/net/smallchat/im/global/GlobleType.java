package net.smallchat.im.global;

public class GlobleType {
	public final static int COMPLETE_ADDR = 00001;//地址
	public final static int COMPLETE_EMAIL = 00002;//邮箱
	public final static int COMPLETE_COMPANY = 00003;//公司
	public final static int COMPLETE_SIGN = 00004;//个性签名
	public final static int COMPLETE_HANGYUE = 00005;//行业
	public final static int COMPLETE_SUBJECT = 00006;//课程期数
	public final static int COMPLETE_SEX = 00007;//地址
	public final static int COMPLETE_NICKNAME = 00014;//昵称
	public final static int MODIFY_GROUP_INFO = 00012;//修改分组信息
	public final static int MODIFY_GROUP_NICKNAME = 00013;//修改用户所在分组的昵称
	public final static int COMPLETE_MET_TITLE = 00015;//填写会议主题
	
	//sessicon表数据类型
	public final static int Session_Sub = 1;//session表-订阅号
	public final static int Session_Server = 2;//session表-服务号
	public final static int Session_OutLander = 3;//session表-陌生人
	public final static int Session_Normal = 4;//session表-普通人

	//通讯录
	public final static int CONTACT_PINLI = 00010;//通讯录-频率
	public final static int CONTACT_ADDTIME = 00011;//通讯录-添加时间
	
	//服务号和订阅号类型
	public final static int SERVICE_TYPE = 3;//通讯录-服务号
	public final static int ORDER_TYPE = 2;//通讯录-订阅号
	
	//举报订阅号用户 1-用户 2-订阅号
	public final static int REPORTED_USER_TYPE = 1;//举报用户
	public final static int REPORTED_SUB_TYPE = 2;//举报订阅号

	//黑名单、及关注、粉丝列表、订阅号和服务号列表类型
	public final static int BLOCKLISTACTIVITY_BLOCK_TYPE = 0;//黑名单
	public final static int BLOCKLISTACTIVITY_SUB_SERVER_TYPE = 1;//订阅号和服务号粉丝列表
	public final static int BLOCKLISTACTIVITY_USER_FOCUS_TYPE = 2;//用户关注列表
	public final static int BLOCKLISTACTIVITY_USER_FANS_TYPE = 3;//用户粉丝列表
	public final static int BLOCKLISTACTIVITY_RECOMMEND_TYPE = 4;//推荐人员列表
	public final static int BLOCKLISTACTIVITY_APPLY_MEETING_USER_TYPE = 5;//申请会议人员列表
	public final static int BLOCKLISTACTIVITY_TOP_MEETING_USER_TYPE = 6;//参会用户活跃度排行

	//隐私设置和新消息通知设置
	public final static int PrivateSetActivity_Normal_TYPE = 0;//隐私设置
	public final static int PrivateSetActivity_New_Msg_Notify_TYPE = 1;//新消息通知设置
	
	//树形结构菜单类型
	public final static int TreeViewActivity_City_TYPE = 0;//城市列表
	public final static int TreeViewActivity_Project_TYPE = 1;//课程列表
	public final static int TreeViewActivity_Subject_TYPE = 2;//行业列表

}
