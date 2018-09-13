package net.smallchat.im.utils.device;

import android.os.Build;

import java.util.Locale;

public class RomUtils {
    public static final int BRAND_UNKNOWN = 0;
    public static final int BRAND_XIAOMI = 1;
    public static final int BRAND_HUAWEI = 2;
    public static final int BRAND_HONOR = 3;
    public static final int BRAND_VIVO = 4;
    public static final int BRAND_OPPO = 5;
    public static final int BRAND_SMARTISAN = 6;
    public static final int BRAND_MEIZU = 7;

    public static final int BRAND;

    static {
        final String brand = Build.BRAND.toLowerCase(Locale.getDefault());
        if (brand.equals("xiaomi"))
            BRAND = BRAND_XIAOMI;
        else if (brand.equals("huawei"))
            BRAND = BRAND_HUAWEI;
        else if (brand.equals("honor"))
            BRAND = BRAND_HONOR;
        else if (brand.equals("vivo"))
            BRAND = BRAND_VIVO;
        else if (brand.equals("oppo"))
            BRAND = BRAND_OPPO;
        else if (brand.equals("smartisan"))
            BRAND = BRAND_SMARTISAN;
        else if (brand.equals("meizu"))
            BRAND = BRAND_MEIZU;
        else
            BRAND = BRAND_UNKNOWN;
    }
}