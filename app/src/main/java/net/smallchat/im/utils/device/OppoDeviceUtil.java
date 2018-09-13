package net.smallchat.im.utils.device;

import android.content.Context;
import android.content.Intent;

public final class OppoDeviceUtil {

    /**
     * 跳转到神隐模式-应用配置列表
     * @param context {@link Context}
     * @return {@code true} 跳转成功，{@code false} 跳转失败
     */
    public static boolean toHiddenAppList(Context context) {
        try {
            Intent intent = new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST");
            context.startActivity(intent);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     *  跳转到神隐模式-应用配置-给定包名应用详细配置界面
     *  本方法不适合用{@link android.app.Activity#startActivityForResult(Intent, int)}方法，因为无论
     *  用户如何选择，返回结果均为-1。
     *  测试机型红米4X，MIUI 9 7.11.2开发板
     * @param context {@link Context}
     * @param packageName 包名
     * @param label 应用名
     * @return {@code true} 跳转成功，{@code false} 跳转失败
     */
    public static boolean toConfigApp(Context context, String packageName, String label) {
        try {
            Intent intent = new Intent("miui.intent.action.HIDDEN_APPS_CONFIG_ACTIVITY");
//            intent.setComponent(new ComponentName("com.miui.powerkeeper",
//                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
            intent.putExtra("package_name", packageName);
            intent.putExtra("package_label", label);
            context.startActivity(intent);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static class AppBackgroundRunConfig {
    }
}