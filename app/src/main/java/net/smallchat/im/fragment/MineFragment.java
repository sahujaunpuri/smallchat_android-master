package net.smallchat.im.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import net.smallchat.im.adapter.MenuAdapter;
import net.smallchat.im.album.MyAlbumActivity;
import net.smallchat.im.mine.EditProfileActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Menu;
import net.smallchat.im.Entity.MenuList;
import net.smallchat.im.MiniBrowserActivity;
import net.smallchat.im.mine.MyFavoriteActivity;
import net.smallchat.im.mine.PersonalQRCodeActivity;
import net.smallchat.im.R;
import net.smallchat.im.mine.SettingTab;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.mine.WalletActivity;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MineFragment extends Fragment implements android.view.View.OnClickListener, AdapterView.OnItemClickListener {

    private final static int MSG_GET_MENU_SUCCESS = 0x301;

    private ImageLoader mImageLoader;

    private ImageView mAvatarView;
    private TextView mUserName;
    private TextView mAccountView;
    private Login login;
    private MyHandler mHandler;
    private Context mParentContext;
    private View mFavoriteView;
    private View mAlbumView;
    private View mWalletView;
    private MenuAdapter mAdapter;
    public MineFragment() {
        mHandler = new MyHandler(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mParentContext = (Context)MineFragment.this.getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        if (mImageLoader == null){
            try {
                mImageLoader = ImageLoader.getInstance();
                //创建默认的ImageLoader配置参数
                ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                        .createDefault(mParentContext);

                //Initialize ImageLoader with configuration.
                ImageLoader.getInstance().init(configuration);

            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        ListView listView = (ListView) view.findViewById(R.id.fragment_mine_list);


        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View setting = inflater.inflate(R.layout.mine_footer, listView, false);
        listView.addFooterView(setting);
        View settingView = setting.findViewById(R.id.mine_setting);
        settingView.setOnClickListener(this);
        View header = inflater.inflate(R.layout.mine_header, listView, false);
        listView.addHeaderView(header);

        mAvatarView = (ImageView) header.findViewById(R.id.mine_avatar);
        mUserName = (TextView) header.findViewById(R.id.mine_name);
        mAccountView = (TextView) header.findViewById(R.id.mine_account);
        View profileLayout = header.findViewById(R.id.mine_profile);
        profileLayout.setOnClickListener(this);
        View qrcodeView = header.findViewById(R.id.mine_profile_qrcode);
        qrcodeView.setOnClickListener(this);




        //添加钱包
        View wallet = inflater.inflate(R.layout.mine_wallet, listView, false);
        mWalletView = wallet.findViewById(R.id.mine_wallet);
        mWalletView.setOnClickListener(this);
        listView.addHeaderView(mWalletView);





        //添加收藏
        View favorite = inflater.inflate(R.layout.mine_favorite, listView, false);
        mFavoriteView = favorite.findViewById(R.id.mine_favorite);
        mFavoriteView.setOnClickListener(this);
        listView.addHeaderView(mFavoriteView);

        //添加相册
        View album = inflater.inflate(R.layout.mine_album, listView, false);
        mAlbumView = album.findViewById(R.id.mine_album);
        mAlbumView.setOnClickListener(this);
        listView.addHeaderView(mAlbumView);




        //其他菜单
        mAdapter = new MenuAdapter(getActivity());
        mAdapter.setHideIcon(false);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        updateUIData();
        getMineMenu();
    }


    private  void updateUIData(){

        if (mImageLoader == null) {
            try {
                mImageLoader = ImageLoader.getInstance();
                //创建默认的ImageLoader配置参数
                ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                        .createDefault(this.getActivity());

                //Initialize ImageLoader with configuration.
                ImageLoader.getInstance().init(configuration);
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        login = IMCommon.getLoginResult(getActivity());
        if (login != null && !TextUtils.isEmpty(login.headSmall)) {
            try {
                mImageLoader.displayImage(login.headSmall, mAvatarView);
            }catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (login != null && !TextUtils.isEmpty(login.nickname)) {
            mUserName.setText(login.nickname);
        } else if (login != null && !TextUtils.isEmpty(login.name)) {
            mUserName.setText(login.name);
        }

        if (login != null && !TextUtils.isEmpty(login.phone)) {
            mAccountView.setText("账号："+login.phone);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUIData();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateUIData();
    }

    public void getMineMenu() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MenuList menus = IMCommon.getIMServerAPI().getMenu("0");
                    if(menus!=null&&menus.mMenuList!=null) {
                        Message msg = mHandler.obtainMessage(MSG_GET_MENU_SUCCESS, menus.mMenuList);
                        msg.sendToTarget();
                    }
                } catch (IMException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void updateMenu(List<Menu> menus) {
        if(mAdapter != null && menus != null) {

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            mAdapter.addAll(menus);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.mine_profile_qrcode: {//点击二维码 ，优先判断，点击个人二维码界面，进入二维码分享界面
                Intent codeIntent = new Intent();
                codeIntent.setClass(getActivity(), PersonalQRCodeActivity.class);
                startActivity(codeIntent);
                break;
            }
            case R.id.mine_profile: {//点击个人资料
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivityForResult(intent,0);
                break;
            }
            case R.id.mine_setting: {//点击设置
                Intent intent = new Intent(getActivity(), SettingTab.class);
                startActivityForResult(intent,0);
                break;
            }
            case R.id.mine_wallet: {//点击钱包
                //Toast.makeText(getContext(), "钱包功能等待开放", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getActivity(), WalletActivity.class));
                break;
            }
            case R.id.mine_favorite: { //我的收藏
                Intent intent = new Intent(getActivity(), MyFavoriteActivity.class);
                startActivityForResult(intent,0);
                break;
            }
            case R.id.mine_album: {//我的相册
                Intent intent = new Intent(getActivity(), MyAlbumActivity.class);
                startActivityForResult(intent,0);
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Menu menu = (Menu) parent.getAdapter().getItem(position);
        if (menu.hasChildMenu()) {
//            try {
//                Intent intent = new Intent(getActivity(), MenuActivity.class);
//                intent.putExtra("menuId", ""+menu.tid);
//                intent.putExtra("title", menu.name);
//                startActivity(intent);
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
        } else if(menu.menu_type == 0) { // 打开HTML5网页
            if(!TextUtils.isEmpty(menu.menu_code)) {
                Intent intent = new Intent(getActivity(), MiniBrowserActivity.class);
                intent.putExtra(MiniBrowserFragment.EXTRA_URL, menu.menu_code);
                startActivity(intent);
            }
        } else if(menu.menu_type == 1) { // 打开本地Activity功能

//            Intent intent = new Intent();
//            intent.setClass(getActivity(), FriendsCircleActivity.class);
//            intent.putExtra(FriendsCircleActivity.EXTRA_TYPE, menu.menu_code);
//            intent.putExtra(FriendsCircleActivity.EXTRA_TITLE, menu.name);
//            startActivity(intent);

        }

    }

//    @Override
//    public void onChangeState(MyPullToRefreshListView container, int state) {
//
//    }

    static class MyHandler extends Handler {
        WeakReference<MineFragment> mFragment;

        MyHandler(MineFragment fragment) {
            mFragment = new WeakReference<MineFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            MineFragment fragment = mFragment.get();
            switch (what) {
                case MSG_GET_MENU_SUCCESS:
                    fragment.updateMenu((List<Menu>) msg.obj);
                    break;
            }
        }
    }


}
