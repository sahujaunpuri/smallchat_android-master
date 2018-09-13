package net.smallchat.im.components.slidepager;

import java.io.Serializable;

/**
 * Created by MQ on 2016/11/11.
 */

public class DataBean implements Serializable {
    public DataBean(){}

    public DataBean(String name,int icon){
        this.icon=icon;
        this.name=name;
    }
    private String name;
    private int icon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
