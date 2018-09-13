package net.smallchat.im;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import net.smallchat.im.fragment.MiniBrowserFragment;

public class MiniBrowserActivity extends BaseActivity {

    private Fragment webViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_browser);

        mContext = this;

        String url = getIntent().getStringExtra(MiniBrowserFragment.EXTRA_URL);
        Bundle bundle = new Bundle();
        bundle.putString(MiniBrowserFragment.EXTRA_URL, url);
        bundle.putBoolean(MiniBrowserFragment.EXTRA_IS_ROOT, false);
        //后退按钮+更多按钮
        setTitleContent(R.drawable.back_btn, R.drawable.more_btn, 0);
        mLeftBtn.setOnClickListener(this);


        webViewFragment = Fragment.instantiate(this, MiniBrowserFragment.class.getName(), bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, webViewFragment);
        ft.commit();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        switch (id) {

            case R.id.left_btn://点击后退按钮
                ((MiniBrowserFragment) webViewFragment).goBack();
                break;
            case R.id.more_btn://点击更多按钮

                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ((MiniBrowserFragment) webViewFragment).goBack();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void updateTitle(String title) {
        setMiniBrowserTitle(title);
    }
}
