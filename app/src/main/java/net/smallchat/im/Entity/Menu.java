package net.smallchat.im.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Author: Matt
 * Date: 2016/4/20
 */
public class Menu implements Serializable {

    public int id;

    public String name;

    public int sort;

    public String icon;

    public int menu_type;

    public int have_child;

    public String menu_code;

    //public List<Menu> child_menu;


    public Menu(JSONObject json) {

        if(!json.isNull("id")){
            try {
                id = json.getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        if(!json.isNull("name")){
            try {
                name = json.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(!json.isNull("sort")){
            try {
                sort= json.getInt("sort");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(!json.isNull("icon")){
            try {
                icon = json.getString("icon");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(!json.isNull("menu_type")){
            try {
                menu_type = json.getInt("menu_type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(!json.isNull("have_child")){
            try {
                have_child = json.getInt("have_child");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        if(!json.isNull("menu_code")){
            try {
                menu_code = json.getString("menu_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasChildMenu() {
        return have_child != 0;
    }
}
