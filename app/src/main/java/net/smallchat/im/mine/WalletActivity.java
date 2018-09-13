package net.smallchat.im.mine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.R;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.widget.MyGridView;

public class WalletActivity extends BaseActivity implements OnClickListener {
    private ImageView iv_more;
    private MyGridView gridView;
    private final static String[] names = new String[] {
            "扩展功能1", "扩展功能2","扩展功能3",
//            "城市服务", "控购申请","货款分期",
//            "我要广告", "我的用呗","爱心公益"


    };
    public final static int[] images = new int[] {
            R.drawable.icon_wallet_recharge,//扩展功能1图标
            R.drawable.icon_wallet_recharge,//扩展功能1图标
            R.drawable.icon_wallet_recharge,//扩展功能1图标
    };
    private Myadapter adapter;
    private ScrollView srollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_wallet);
        initView();

    }

    private void initView() {

        iv_more = (ImageView) this.findViewById(R.id.iv_more);
        iv_more.setOnClickListener(this);
      
        
        gridView = (MyGridView) findViewById(R.id.wallet_grid_view);
        adapter = new Myadapter(WalletActivity.this, images, names);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 1) {
                    startActivity(new Intent(WalletActivity.this,
                            PayActivity.class));
                }
            }

        });

        srollView = (ScrollView) findViewById(R.id.srollView);

        this.findViewById(R.id.re_money).setOnClickListener(this);
        RelativeLayout re_card = (RelativeLayout) this.findViewById(R.id.re_card);
        re_card.setOnClickListener(this);
        TextView tv_money = (TextView) this.findViewById(R.id.tv_money);
        try {

            Login mLogin = IMCommon.getLoginResult(mContext);

            tv_money.setText("￥" + mLogin.money);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * 返回按钮响应事件
     * @param view
     */
    public void onBack(View view){
        this.finish();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.iv_more:
            showPopView();

            break;

        case R.id.re_money:
            startActivity(new Intent(WalletActivity.this, MoneyActivity.class));

            break;
            
        case R.id.re_card:
            startActivity(new Intent(WalletActivity.this, BankCardActivity.class));

            break;
            
        default:
            break;

        }
    }

    private void showPopView() {

//        MorePopWindow addPopWindow = new MorePopWindow(WalletActivity.this);
//        addPopWindow.showPopupWindow(iv_more);

    }

    class Myadapter extends BaseAdapter {

        private LayoutInflater inflater;
        private int[] imageDatas;
        private String[] nameDatas;

        public Myadapter(Context context, int[] imageDatas, String[] nameDatas) {
            inflater = LayoutInflater.from(context);

            this.imageDatas = imageDatas;
            this.nameDatas = nameDatas;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return imageDatas.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return imageDatas[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

                convertView = inflater.inflate(R.layout.item_wallet, null, false);
                ImageView iv_image = (ImageView)(convertView
                        .findViewById(R.id.iv_image));
                TextView tv_name = (TextView) (convertView
                        .findViewById(R.id.tv_name));
                String name = nameDatas[position];
                int imageRes = imageDatas[position];
            try {
                //iv_image.setBackgroundResource(imageRes);
                iv_image.setImageResource(imageRes);
                tv_name.setText(name);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return convertView;
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        srollView.smoothScrollTo(0, 0);
        super.onResume();
        TextView tv_money = (TextView) this.findViewById(R.id.tv_money);
        try {
            Login mLogin = IMCommon.getLoginResult(mContext);

            tv_money.setText("￥" + mLogin.money);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
