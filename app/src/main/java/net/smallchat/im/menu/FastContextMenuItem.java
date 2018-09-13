package net.smallchat.im.menu;

/**
 * 上下文菜单项对象
 * Created by it on 2017/9/1.
 */

public class FastContextMenuItem {
    private int itemId;
    private int imageId;
    private String title;
    private boolean visible;
    private String colorString;

    public FastContextMenuItem(int itemId,int imageId, String title, boolean visible, String colorString) {
        this.itemId=itemId;
        this.imageId = imageId;
        this.title = title;
        this.visible = visible;
        this.colorString = colorString;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getColorString() {
        return colorString;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}