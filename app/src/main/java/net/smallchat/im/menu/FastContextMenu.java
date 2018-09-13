package net.smallchat.im.menu;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义上下文菜单
 * Created by MaJian on 16/4/28.
 */
public class FastContextMenu {

    private Context mContext;
    private List<FastContextMenuItem> itemList;
    private PopupWindow popupWindow;
    private View contentView;
    private ListView mLvMenuList;
    private MenuAdapter menuAdapter;
    private OnItemSelectListener onItemSelectListener;

    public interface OnItemSelectListener{
        void onItemSelect(int itemFlag,int position);
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener){
        this.onItemSelectListener = onItemSelectListener;
    }

    public FastContextMenu(Context mContext){
        this.mContext = mContext;
        itemList = new ArrayList<>();
        initPopWindow();
    }

    /**
     * 初始化popwindow菜单
     */
    private void initPopWindow(){
        contentView = LayoutInflater.from(mContext).inflate(R.layout.fast_popwindow_menu, null);


        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        mLvMenuList = (ListView) contentView.findViewById(R.id.fast_context_menu_lv_menu);
        menuAdapter = new MenuAdapter();
        mLvMenuList.setAdapter(menuAdapter);
        mLvMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onItemSelectListener != null){
                    onItemSelectListener.onItemSelect(itemList.get(position).getItemId(),position);
                }
                popupWindow.dismiss();
            }
        });
    }

    /**
     * 设置菜单列表数据源
     * @param itemList
     */
    public void setItemList(List<FastContextMenuItem> itemList){
        this.itemList = itemList;
        menuAdapter.notifyDataSetChanged();
    }

    public void showMenu(View view){
        if (popupWindow == null)
            return;
        try {
            View windowContentViewRoot =view; //我们要设置给PopupWindow进行显示的View
            int windowPos[] = calculatePopWindowPos(view, windowContentViewRoot);
            int xOff = 50;// 可以自己调整偏移
            windowPos[0] -= xOff;
            popupWindow.showAtLocation(view, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);
            // windowContentViewRoot是根布局View

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



    /**
     * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
     * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
     * @param anchorView  呼出window的view
     * @param contentView   window的内容布局
     * @return window显示的左上角的xOff,yOff坐标
     */
    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // 获取屏幕的高宽
        final int screenHeight = ScreenUtils.getScreenHeight(anchorView.getContext());
        final int screenWidth = ScreenUtils.getScreenWidth(anchorView.getContext());
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }

    /**
     * 上下文菜单列表适配器
     */
    class MenuAdapter extends BaseAdapter{

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return itemList == null ? 0 : itemList.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        /**
         * Get the row tid associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row tid we want.
         * @return The tid of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.fast_context_menu_item, null);

                viewHolder.mIvIcon = (ImageView) convertView.findViewById(R.id.fast_context_menu_item_icon);
                viewHolder.mTvTitle = (TextView) convertView.findViewById(R.id.fast_context_menu_item_title);
                viewHolder.mViewDivider = convertView.findViewById(R.id.fast_context_menu_item_divider);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.mTvTitle.setText(itemList.get(position).getTitle());
            viewHolder.mIvIcon.setImageResource(itemList.get(position).getImageId());
            viewHolder.mItemId=itemList.get(position).getItemId();
//            convertView.setBackgroundColor(Color.parseColor(itemList.get(position).getColorString()));
            if (position == itemList.size() - 1){
                viewHolder.mViewDivider.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.mViewDivider.setVisibility(View.VISIBLE);
            }
            //如果没有图标，则隐藏
            if(itemList.get(position).getImageId()==0){
                viewHolder.mIvIcon.setVisibility(View.INVISIBLE);
            }else{
                viewHolder.mIvIcon.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        class ViewHolder{
            int mItemId;
            TextView mTvTitle;
            ImageView mIvIcon;
            View mViewDivider;
        }
    }
}