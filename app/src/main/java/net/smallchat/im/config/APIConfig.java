package net.smallchat.im.config;

import net.smallchat.im.config.development.DevelopmentAPIConfig;
import net.smallchat.im.config.product.ProductAPIConfig;
import net.smallchat.im.config.testing.TestingAPIConfig;

public class APIConfig {


    public static String  getAppKey(){
        if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.DEVELOPMENT) {//开发环境配置
            return DevelopmentAPIConfig.APPKEY;
        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.TESTING) {//测试环境配置
            return TestingAPIConfig.APPKEY;

        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.PRODUCT) {//生产环境配置
            return ProductAPIConfig.APPKEY;

        }
        return "";
    }
    public static String  getApiServer(){
        if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.DEVELOPMENT) {//开发环境配置
            return DevelopmentAPIConfig.API_SERVER_PREFIX;
        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.TESTING) {//测试环境配置
            return TestingAPIConfig.API_SERVER_PREFIX;

        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.PRODUCT) {//生产环境配置
            return ProductAPIConfig.API_SERVER_PREFIX;

        }
        return "";
    }

    public static String  getStorageServer(){
        if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.DEVELOPMENT) {//开发环境配置
            return DevelopmentAPIConfig.API_SERVER_STORAGE;
        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.TESTING) {//测试环境配置
            return TestingAPIConfig.API_SERVER_STORAGE;

        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.PRODUCT) {//生产环境配置
            return ProductAPIConfig.API_SERVER_STORAGE;

        }
        return "";
    }

    public static String  getQrcodeUrl(){
        if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.DEVELOPMENT) {//开发环境配置
            return DevelopmentAPIConfig.QRCODE_URL;
        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.TESTING) {//测试环境配置
            return TestingAPIConfig.QRCODE_URL;

        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.PRODUCT) {//生产环境配置
            return ProductAPIConfig.QRCODE_URL;

        }
        return "";
    }
    public static String  getHeadUrl(){
        if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.DEVELOPMENT) {//开发环境配置
            return DevelopmentAPIConfig.HEAD_URL;
        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.TESTING) {//测试环境配置
            return TestingAPIConfig.HEAD_URL;

        }else if(AppConfig.runEnvironmentalConfig== AppConfig.RunEnvironmental.PRODUCT) {//生产环境配置
            return ProductAPIConfig.HEAD_URL;
        }
        return "";
    }
}
