package net.smallchat.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import net.smallchat.im.Entity.Menu;
import net.smallchat.im.R;

import java.util.List;

/**
 * Author: matt
 * Date 16-4-21.
 */
public class MenuAdapter extends ArrayAdapter<Menu>{

    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;

    private boolean hideIcon = false;
    public MenuAdapter(Context context) {
        super(context, R.layout.menu_item_layout);

        init(context);
    }
    public MenuAdapter(Context context, List<Menu> list) {
        super(context, R.layout.menu_item_layout, list);
        init(context);
    }

    private void init(Context context) {
        mInflater = LayoutInflater.from(context);
        try {
            mImageLoader = ImageLoader.getInstance();
            //创建默认的ImageLoader配置参数
            ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                    .createDefault(this.getContext());

            //Initialize ImageLoader with configuration.
            ImageLoader.getInstance().init(configuration);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void setHideIcon(boolean hideIcon) {
        this.hideIcon = hideIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.menu_item_layout, parent, false);
            holder = new ViewHolder();
            holder.mMenuTitleView = (TextView) view.findViewById(R.id.menu_title);
            holder.mMenuIconView = (ImageView)view.findViewById(R.id.menu_icon);
            view.setTag(holder);

        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        Menu menu = getItem(position);
        holder.bindView(menu);

        return view;
    }

    private class ViewHolder {

        private ImageView mMenuIconView;

        private TextView mMenuTitleView;


        public void bindView(Menu menu) {
            if(!hideIcon) {
                mImageLoader.displayImage(menu.icon, mMenuIconView);
            } else {
                mMenuIconView.setVisibility(View.GONE);
            }
            mMenuTitleView.setText(menu.name);
        }
    }
}
