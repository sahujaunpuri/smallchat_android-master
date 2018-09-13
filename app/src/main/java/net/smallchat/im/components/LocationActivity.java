
package net.smallchat.im.components;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;


import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.MessageLocation;
import net.smallchat.im.contact.ChooseUserActivity;
import net.smallchat.im.Entity.MapInfo;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.MovingLoaction;
import net.smallchat.im.R;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;


import java.util.List;


public class LocationActivity extends BaseActivity
        implements OnClickListener, OnGetGeoCoderResultListener {

    public final static int MSG_LOCATION_ERROR = 0x00015;
    public final static int MSG_SHOW_NEARY_LOCATION = 0x00050;
    public final static int MSG_SHOW_NEARY_MAP = 0x00041;

    public final static int RESULT_FOR_SEARCH = 0x01;

    private LayoutInflater mInflater;

    private String mFuid;
    private String mGroupId;
    private boolean mFullScreen;

    // BaiduMap api
    private GeoCoder mGeoSearch;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner mMyListener;
    private LatLng curLocation;
    private String curLocText;
    private String curLocAdd;
    private float mZoom = 16;

    // View
    private MapView mMapView;
    private ImageView mMarkerView;
    private ListView mSearchResultView;
    private SearchResultAdapter mSearchAdapter;
    // The mapinfo to send
    private MapInfo mMapInfo;

    private BaiduMap.OnMapStatusChangeListener mMapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {

        @Override
        public void onMapStatusChangeStart(MapStatus st) {

        }

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

        }

        @Override
        public void onMapStatusChangeFinish(MapStatus st) {
            mZoom = st.zoom;
            if (!mFullScreen) {
                LatLng loc = st.target;
                SearchNearby(loc);
            }
        }

        @Override
        public void onMapStatusChange(MapStatus st) {

        }
    };


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case GlobalParam.SHOW_PROGRESS_DIALOG:
                    String hintMsg = (String) msg.obj;
                    if (!TextUtils.isEmpty(hintMsg)) {
                        showProgressDialog(hintMsg);
                    }
                    break;
                case GlobalParam.HIDE_PROGRESS_DIALOG:
                    hideProgressDialog();
                    break;
                case MSG_LOCATION_ERROR:
                    Toast.makeText(mContext, R.string.location_error, Toast.LENGTH_LONG).show();

                    if (mLocClient != null) {
                        mLocClient.stop();
                    }

                    hideProgressDialog();
                    break;
                case MSG_SHOW_NEARY_LOCATION:
                    @SuppressWarnings("unchecked")
                    List<PoiInfo> poiInfoList = (List<PoiInfo>) msg.obj;
                    mSearchAdapter.setList(poiInfoList);
                    mSearchAdapter.notifyDataSetChanged();
                    break;
                case GlobalParam.MSG_CHECK_FAVORITE_STATUS:
                    IMResponseState favoriteResult = (IMResponseState) msg.obj;
                    if (favoriteResult == null) {
                        Toast.makeText(mContext, R.string.commit_dataing, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (favoriteResult.code != 0) {
                        Toast.makeText(mContext, favoriteResult.errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    break;

                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_layout);
        mContext = this;
        mInflater = LayoutInflater.from(mContext);

        Intent i = getIntent();
        mFuid = i.getStringExtra("fuid");
        mGroupId = i.getStringExtra("groupid");
        mFullScreen = i.getBooleanExtra("show", false);
        double lng = i.getDoubleExtra("lng", IMCommon.getCurrentLng(mContext));
        double lat = i.getDoubleExtra("lat", IMCommon.getCurrentLat(mContext));
        curLocation = new LatLng(lat, lng);
        curLocAdd = i.getStringExtra("addr");
        mGeoSearch = GeoCoder.newInstance();
        mGeoSearch.setOnGetGeoCodeResultListener(this);
        initComponent();
        updateMapPosition(curLocation);

        if (!mFullScreen) {
            getLocation();
        }
    }

    private void updateMapPosition(LatLng loc) {
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(loc, mZoom);
        mBaiduMap.setMapStatus(update);
    }

    /**
     * Get the current location
     */
    private void getLocation() {
        mLocClient = new LocationClient(getApplicationContext());
        mMyListener = new MyLocationListenner();
        mLocClient.registerLocationListener(mMyListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        mHandler.sendEmptyMessageDelayed(MSG_LOCATION_ERROR, 6000);

        Message message = new Message();
        message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
        message.obj = mContext.getResources().getString(R.string.location_doing);
        mHandler.sendMessage(message);
    }

    private void addItemForMap() {
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(curLocation)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_markf_h)));
    }

    /**
     * Init the widget
     */
    private void initComponent() {
        mSearchResultView = (ListView) findViewById(R.id.search_list);
        mMarkerView = (ImageView)findViewById(R.id.map_mark);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapStatusChangeListener(mMapStatusChangeListener);

        if (!mFullScreen) {
            setTitleContent(R.drawable.back_btn, false, R.drawable.send_map_btn, R.string.message_type_location);
            mRightBtn.setOnClickListener(this);
            mSearchBtn.setOnClickListener(this);
            mLeftBtn.setOnClickListener(this);

            mSearchResultView.setVisibility(View.VISIBLE);
            mSearchAdapter = new SearchResultAdapter();
            mSearchResultView.setAdapter(mSearchAdapter);
            mSearchResultView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object item = mSearchAdapter.getItem(position);
                    LatLng loc;
                    if (position == 0) {
                        loc = (LatLng) item;
                        mMapInfo = new MapInfo(curLocText, curLocAdd,
                                curLocation.latitude + "", curLocation.longitude + "");
                    } else {
                        loc = ((PoiInfo) item).location;
                        mMapInfo = new MapInfo(((PoiInfo) item).name, ((PoiInfo) item).address,
                                loc.latitude + "", loc.longitude + "");
                    }
                    updateMapPosition(loc);

                    mSearchAdapter.setSelected(position);
                    mSearchAdapter.notifyDataSetChanged();
                }
            });

            mMarkerView.setVisibility(View.VISIBLE);
        } else {
            setTitleContent(R.drawable.back_btn, false, false, true, R.string.map);
            mMoreBtn.setOnClickListener(this);
            mLeftBtn.setOnClickListener(this);

            addItemForMap();
            mMarkerView.setVisibility(View.GONE);
        }
    }

    public void SearchNearby(LatLng location) {

        mGeoSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(location));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IMCommon.appOnResume(mContext);
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mGeoSearch.destroy();
        if(mMyListener != null && mLocClient != null)
            mLocClient.unRegisterLocationListener(mMyListener);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.left_btn:
                finish();
                break;
            case R.id.right_btn:
                // send action
                if (mMapInfo == null) {
                    Toast.makeText(mContext, R.string.please_select_map, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("mapInfo", mMapInfo);
                setResult(RESULT_OK, intent);
                finish();
                break;

            case R.id.more_btn:
                showMoreMenu();
                break;

            default:
                break;
        }
    }

    private void showMoreMenu(){

        MMAlert.showAlert(mContext, "", mContext.getResources().
                        getStringArray(R.array.map_more_menu),
                null, new MMAlert.OnAlertSelectId() {

                    @Override
                    public void onClick(int whichButton) {
                        switch (whichButton) {
                            case 0://收藏
                                MovingLoaction movingLocation = new MovingLoaction(curLocation.latitude + "",
                                        curLocation.longitude + "", curLocAdd, MessageType.LOCATION + "");
                                favoriteMoving(MovingLoaction.getInfo(movingLocation));
                                break;
                            case 1://转发给好友
                                ChatMessage msg = new ChatMessage();

                                msg.messageType = MessageType.LOCATION;
                                msg.locationData =new MessageLocation();
                                msg.locationData.lat = curLocation.latitude;
                                msg.locationData.lng = curLocation.longitude;
                                msg.locationData.address = curLocAdd;

                                Intent chooseUserIntent = new Intent();
                                chooseUserIntent.setClass(mContext, ChooseUserActivity.class);
                                chooseUserIntent.putExtra("forward_msg", msg);
                                startActivity(chooseUserIntent);
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    private void favoriteMoving(final String favoriteContent) {
        if (!IMCommon.getNetWorkState()) {
            mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
            return;
        }
        new Thread() {
            public void run() {
                try {
                    IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
                            mContext.getResources().getString(R.string.send_request));
                    IMResponseState status = IMCommon.getIMServerAPI().favoreiteMoving(mFuid + "",
                            mGroupId + "", favoriteContent);
                    IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_FAVORITE_STATUS, status);
                    mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
                } catch (IMException e) {
                    e.printStackTrace();
                    IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
                            mContext.getResources().getString(e.getStatusCode()));
                } catch (Exception e) {
                    e.printStackTrace();
                    mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
                }
            };
        }.start();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult arg0) {
    }



    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        List<PoiInfo> pl = result.getPoiList();
        curLocAdd = result.getAddress();
        curLocText = result.getAddressDetail().city;

        mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
        IMCommon.sendMsg(mHandler, MSG_SHOW_NEARY_LOCATION, pl);

        mMapInfo = new MapInfo(curLocText, curLocAdd,
                curLocation.latitude + "", curLocation.longitude + "");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK)
            return;

        if(RESULT_FOR_SEARCH == requestCode){
            double lng = data.getDoubleExtra("lng", 0);
            double lat = data.getDoubleExtra("lat", 0);
            String name  = data.getStringExtra("name");
            String address = data.getStringExtra("address");
            LatLng ll = new LatLng(lat, lng);
            mMapInfo = new MapInfo(name, address, lat + "", lng + "");
            updateMapPosition(ll);
            SearchNearby(ll);
        }
    }




    private class SearchViewHolder{

        public TextView mLocTextView;
        public TextView mLocAddView;
        public CheckBox mLocStateView;
    }

    public class SearchResultAdapter extends BaseAdapter{

        // The list of Nearby poi
        private List<PoiInfo> mResultList;

        // flag weather check on
        private SparseBooleanArray isSelected;

        public SearchResultAdapter() {
            isSelected =  new SparseBooleanArray();
        }

        public void setList(List<PoiInfo> list){
            mResultList = list;

            isSelected.clear();
            int count = getCount();
            for (int i = 0; i < count; i++) {
                isSelected.put(i, false);
            }
            isSelected.put(0, true);
        }


        public void setSelected(int position){
            int count = getCount();
            for (int i = 0; i < count; i++) {
                isSelected.put(i, false);
            }

            isSelected.put(position, true);
        }
        @Override
        public int getCount() {
            // The first is the current position.
            // So the count should be mResultList.size() + 1
            return mResultList == null ? 0 : mResultList.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            if(position == 0)
                return curLocation;
            else
                return mResultList == null ? null : mResultList.get(position - 1);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.neary_location_item2, parent, false);
                holder = new SearchViewHolder();
                holder.mLocTextView = (TextView)convertView.findViewById(R.id.location_text);
                holder.mLocAddView = (TextView)convertView.findViewById(R.id.location_addr);
                holder.mLocStateView = (CheckBox)convertView.findViewById(R.id.checklocation);
                convertView.setTag(holder);
            } else {
                holder = (SearchViewHolder) convertView.getTag();
            }

            // The first is the current position
            if(position == 0){
                holder.mLocTextView.setText("[" + mContext.getString(R.string.message_type_location) + "]");
                holder.mLocAddView.setText(curLocAdd);
            } else {
                PoiInfo info = mResultList.get(position - 1);
                holder.mLocTextView.setText(info.name);
                holder.mLocAddView.setText(info.address);
            }

            holder.mLocStateView.setChecked(isSelected.get(position));
            return convertView;
        }
    }

    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            mHandler.removeMessages(MSG_LOCATION_ERROR);
            mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);

            if (location != null){

                if (mLocClient != null) {
                    mLocClient.stop();
                }

                double Lat = location.getLatitude();
                double Lng = location.getLongitude();

                IMCommon.setCurrentLat(Lat);
                IMCommon.setCurrentLng(Lng);

                SharedPreferences preferences = mContext.getSharedPreferences(IMCommon.LOCATION_SHARED, 0);
                Editor editor = preferences.edit();
                editor.putString(IMCommon.LAT, String.valueOf(Lat));
                editor.putString(IMCommon.LNG, String.valueOf(Lng));
                editor.commit();

                curLocation = new LatLng(Lat, Lng);
                updateMapPosition(curLocation);
                SearchNearby(curLocation);

            }else {
                if (mLocClient != null) {
                    mLocClient.stop();
                }
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }
}
