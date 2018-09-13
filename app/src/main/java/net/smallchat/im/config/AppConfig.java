package net.smallchat.im.config;

public class AppConfig {
    //配置APP的运行环境 默认是开发环境配置
    public static RunEnvironmental runEnvironmentalConfig=RunEnvironmental.DEVELOPMENT;
    //环境类型
    public enum RunEnvironmental{
        PRODUCT,
        DEVELOPMENT,
        TESTING
    }
}
