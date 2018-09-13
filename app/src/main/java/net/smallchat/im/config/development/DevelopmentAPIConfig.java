package net.smallchat.im.config.development;

import net.smallchat.im.config.AppConfig;

public class DevelopmentAPIConfig {

    //APP接口配置信息    
    //预留APPKEY 接口安全验证.目前已经加入到每一个接口请求中  这个目前后台没有验证
    public static final String APPKEY ="0e93f53b5b02e29ca3eb6f37da3b05b9";
    //存储服务器
    public static final String API_SERVER_STORAGE = "http://im.storage.wqdsoft.com";
    //普通接口服务器,API  通讯核心
    public static final String API_SERVER_PREFIX = "http://im.test.wqdsoft.com/index.php";
    //二维码服务器配置   这个可以不是真实的
    public static final String QRCODE_URL ="http://qrcode.wqdsoft.com/";

    //头像服务器配置   用户量大，这个可以单独设置
    public static final String HEAD_URL = "http://im.test.wqdsoft.com/index.php";
}
