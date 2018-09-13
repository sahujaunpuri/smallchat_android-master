package net.smallchat.im.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;

import net.smallchat.im.R;
import net.smallchat.im.global.IMCommon;

import java.util.List;

public class SearchActivity extends Activity
        implements OnClickListener, OnGetPoiSearchResultListener,
        OnItemClickListener, OnGetSuggestionResultListener {
    
    private Context mContext;
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    
    private EditText mContentEdit;
    private ListView mListView;
    private Button mClearBtn;
    private ImageView mBackView;
    private LocationAdapter mLocationAdapter;
    
    private LatLng mSearchLocation;
    private int load_Index = 0;

    private TextWatcher mTextWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s)) {
                mClearBtn.setClickable(true);
            } else {
                mClearBtn.setClickable(false);
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = this;
        
        Intent i = getIntent();
        double lng = i.getDoubleExtra("lng", IMCommon.getCurrentLng(this));
        double lat = i.getDoubleExtra("lat", IMCommon.getCurrentLat(this));
        mSearchLocation = new LatLng(lat, lng);
                
        mListView = (ListView)findViewById(R.id.search_result);
        mListView.setCacheColorHint(0);
        mListView.setOnItemClickListener(this);
        
        mClearBtn = (Button)findViewById(R.id.search_location_btn);
        mClearBtn.setOnClickListener(this);
        
        mBackView = (ImageView)findViewById(R.id.left_icon);
        mBackView.setOnClickListener(this);
        
        mContentEdit = (EditText)findViewById(R.id.search_content);
        mContentEdit.addTextChangedListener(mTextWatcher);
        
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
    }
    

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.left_btn:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.search_btn:
                String key = mContentEdit.getText().toString();
                doSearch(key);
                hideSofrInput();
                break;
        }
    }

    private void doSearch(String keyword) {
        mPoiSearch.searchNearby(new PoiNearbySearchOption()
                .location(mSearchLocation)
                .keyword(keyword)
                .pageNum(load_Index));
    }
    
    private void hideSofrInput(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mContentEdit.getWindowToken(), 0) ;  
    }
    
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PoiInfo info = (PoiInfo) mLocationAdapter.getItem(position);
        Intent i = new Intent();
        i.putExtra("lng", info.location.longitude);
        i.putExtra("lat", info.location.latitude);
        i.putExtra("name", info.name);
        i.putExtra("address", info.address);
        setResult(RESULT_OK, i);
        finish();
    }
    
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
    }
    
    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "未找到结果", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            List<PoiInfo> list = result.getAllPoi();
            mLocationAdapter = new  LocationAdapter();
            mLocationAdapter.setList(list);
            mListView.setAdapter(mLocationAdapter);
            mLocationAdapter.notifyDataSetChanged();
            return;
        }
    }
    
    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        super.onDestroy();
    }
    
    private class SearchViewHolder{
        
        public TextView mLocTextView;
        public TextView mLocAddView;
        public CheckBox mLocStateView;
    }
    
    public class LocationAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        
        private List<PoiInfo> mResultList;
        public LocationAdapter() {
            super();
            mInflater = LayoutInflater.from(mContext);
        }

        public void setList(List<PoiInfo> list){
            mResultList = list;
        }
        
        @Override
        public int getCount() {
            return mResultList == null ? 0 : mResultList.size();
        }

        @Override
        public Object getItem(int position) {
            return mResultList == null ? null : mResultList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchViewHolder holder;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.neary_location_item2, parent, false);
                holder = new SearchViewHolder();
                holder.mLocTextView = (TextView)convertView.findViewById(R.id.location_text);
                holder.mLocAddView = (TextView)convertView.findViewById(R.id.location_addr);
                holder.mLocStateView = (CheckBox)convertView.findViewById(R.id.checklocation);
                holder.mLocStateView .setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
                holder = (SearchViewHolder) convertView.getTag();
            }
  
            holder.mLocTextView .setText(mResultList.get(position).name);
            holder.mLocAddView .setText(mResultList.get(position).address);
            return convertView;
        }
    }
 

}
