package net.smallchat.im.service;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import static net.smallchat.im.config.XMPPConfig.getXmppHost;
import static net.smallchat.im.config.XMPPConfig.getXmppPort;


public class IMXMPPTCPConnection extends XMPPTCPConnection {
    private static IMXMPPTCPConnection connection;
    public IMXMPPTCPConnection(XMPPTCPConnectionConfiguration config) {

        super(config);
    }
    public static synchronized IMXMPPTCPConnection getInstance(){
        //初始化XMPPTCPConnection相关配置
        if(connection == null){
            XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
            //设置连接超时的最大时间
            builder.setConnectTimeout(5000);
            //设置登录openfire的用户名和密码
            //builder.setUsernameAndPassword("guochen", "guochen");
            //设置安全模式
            builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            builder.setResource("Android");
            //设置服务器名称
            builder.setServiceName(getXmppHost());
            //设置主机地址
            builder.setHost(getXmppHost());
            //设置端口号
            builder.setPort(getXmppPort());
            //是否查看debug日志
            builder.setDebuggerEnabled(true);
            connection = new IMXMPPTCPConnection(builder.build());
        }
        return connection;
    }
}
