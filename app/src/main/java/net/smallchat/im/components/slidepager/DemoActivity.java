package net.smallchat.im.components.slidepager;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;

import net.smallchat.im.R;
import net.smallchat.im.components.slidepager.indicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends AppCompatActivity {
//    public static int item_grid_num = 12;//每一页中GridView中item的数量
//    public static int number_columns = 4;//gridview一行展示的数目
//    private ViewPager view_pager;
//    private SlidePagerViewPagerAdapter mAdapter;
//    private List<DataBean> dataList;
//    private List<GridView> gridList = new ArrayList<>();
//    private CirclePageIndicator indicator;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.chat_box_expra);
//        initViews();
//        initDatas();
//    }
//
//    private void initViews() {
//        //初始化ViewPager
//        view_pager = (ViewPager) findViewById(R.tid.view_pager);
//        mAdapter = new SlidePagerViewPagerAdapter(new );
//        view_pager.setAdapter(mAdapter);
//        dataList = new ArrayList<>();
//        //圆点指示器
//        indicator = (CirclePageIndicator) findViewById(R.tid.indicator);
//        indicator.setVisibility(View.VISIBLE);
//        indicator.setViewPager(view_pager);
//    }
//
//    private void initDatas() {
//        if (dataList.size() > 0) {
//            dataList.clear();
//        }
//        if (gridList.size() > 0) {
//            gridList.clear();
//        }
//        //初始化数据
//        for (int i = 0; i < 60; i++) {
//            DataBean bean = new DataBean();
//            bean.setIcon(R.drawable.icon_pay_logo);
//            bean.setName("第" + (i + 1) + "条数据");
//            dataList.add(bean);
//        }
//        //计算viewpager一共显示几页
//        int pageSize = dataList.size() % item_grid_num == 0
//                ? dataList.size() / item_grid_num
//                : dataList.size() / item_grid_num + 1;
//        for (int i = 0; i < pageSize; i++) {
//            GridView gridView = new GridView(this);
//            SlidePagerGridViewAdapter adapter = new SlidePagerGridViewAdapter(dataList, i,item_grid_num);
//            gridView.setNumColumns(number_columns);
//            gridView.setAdapter(adapter);
//            gridList.add(gridView);
//        }
//        mAdapter.add(gridList);
//    }

}
