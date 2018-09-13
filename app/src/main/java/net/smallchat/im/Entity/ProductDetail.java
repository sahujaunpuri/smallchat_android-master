package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductDetail implements Serializable{

	private static final long serialVersionUID = -14564561512112L;

	/**
	 * data": {
        "productID": "123",
        "productIMG": [
            "http://img3.winxuancdn.com/6139/1200726139_10.jpg?1379037340409",
            "http://img2.winxuancdn.com/3626/1200723626_10.jpg?1376383481871",
            "http://img3.winxuancdn.com/2763/1200722763_10.jpg?1375752792250"
        ],
        "productName": "usb接口",
        "productBrand": "HTC",
        "productQuantity": "NODETDAa32",
        "salePrice": "$13333333333",
        "costPrice": "$1000000",
        "favor": "这是什么?给出值",
        "carriageAmount": "$122.22",
        "productDetail": "this is a product detail",
        "specification": "this is a specification info",
        "productStatus": 0
    },
	 */
	
	public String productID;
	public List<String> mImageList;
	public String productName;
	public String productBrand;
	public String productQuantity;
	public String salePrice;
	public String costPrice;
	public String favor;
	public String carriageAmount;
	public String productDetail;
	public String specification;
	public String productStatus;
	public String productKey;
	
	public ProductDetail(){
		
	}
	
	public ProductDetail(JSONObject json){
		try {
			productID = json.getString("productID");
			if(!json.isNull("productIMG")){
				JSONArray array = json.getJSONArray("productIMG");
				if(array != null && array.length() != 0){
					mImageList = new ArrayList<String>();
					for (int i = 0; i < array.length(); i++) {
						mImageList.add(array.getString(i));
					}
				}
			}
			
			/*if(mImageList == null){
				mImageList = new ArrayList<String>();
			}
			
			if(mImageList.size() == 0){
				mImageList.add("http://img3.winxuancdn.com/6139/1200726139_10.jpg?1379037340409");
				mImageList.add("http://img2.winxuancdn.com/3626/1200723626_10.jpg?1376383481871");
				mImageList.add("http://img3.winxuancdn.com/2763/1200722763_10.jpg?1375752792250");
			}*/
			
			if(!json.isNull("productName")){
				productName = json.getString("productName");
			}
			
			if(!json.isNull("productBrand")){
				productBrand = json.getString("productBrand");
			}
			
			if(!json.isNull("productQuantity")){
				productQuantity = json.getString("productQuantity");
			}
			
			if(!json.isNull("salePrice")){
				salePrice = json.getString("salePrice");
			}
			
			if(!json.isNull("estimatePrice")){
				costPrice = json.getString("estimatePrice");
			}
			
			if(!json.isNull("favor")){
				favor = json.getString("favor");
			}
			
			if(!json.isNull("carriageAmount")){
				carriageAmount = json.getString("carriageAmount");
			}
			
			if(!json.isNull("productDetail")){
				productDetail = json.getString("productDetail");
			}
			
			if(!json.isNull("specification")){
				specification = json.getString("specification");
			}
			
			if(!json.isNull("productStatus")){
				productStatus = json.getString("productStatus");
			}
			
			if(!json.isNull("productKey")){
				productKey = json.getString("productKey");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
