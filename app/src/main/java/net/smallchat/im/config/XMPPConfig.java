package net.smallchat.im.config;

import net.smallchat.im.config.development.DevelopmentXMPPConfig;
import net.smallchat.im.config.product.ProductXMPPConfig;
import net.smallchat.im.config.testing.TestingXMPPConfig;

public class XMPPConfig {
    public static String  getXmppHost(){
        if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.DEVELOPMENT) {//开发环境配置
            return DevelopmentXMPPConfig.XMPP_HOST;
        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.TESTING) {//测试环境配置
            return TestingXMPPConfig.XMPP_HOST;

        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.PRODUCT) {//生产环境配置
            return ProductXMPPConfig.XMPP_HOST;
        }
        return "";
    }

    public static int  getXmppPort(){
        if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.DEVELOPMENT) {//开发环境配置
            return DevelopmentXMPPConfig.XMPP_PORT;
        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.TESTING) {//测试环境配置
            return TestingXMPPConfig.XMPP_PORT;

        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.PRODUCT) {//生产环境配置
            return ProductXMPPConfig.XMPP_PORT;
        }
        return 5222;
    }
}
