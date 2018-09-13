package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Delivery implements Serializable{

	private static final long serialVersionUID = -14564545454L;
	
	/**
	 * 订单ID [orderID]
,订单号 [orderNO]
,货运方式 [deliveryType]
,货运单号 [deliveryNO]
,收货人 [consignee]
,联系电话 [contact]
,送货地址 [deliveryAddress]
,送货进度 [deliveryDetailInfo]
时间 [deliveryInfoTime]
,内容 [deliveryInfoContent]
,类型 [deliveryInfoType] —公司内，第3方）

	 */
	
	public String orderID;				//订单ID
	public String orderNO;				//订单号
	public String deliveryType;			//货运方式
	public String deliveryNO;			//货运单号
	public String consignee;			//联系人
	public String contact;				//联系方式
	public String deliveryAddress;		//送货地址
	public List<DeliveryItem> mDeliveryList;	//送货进度
	
	public Delivery(){
		
	}
	
	public Delivery(JSONObject json){
		try {
			if(!json.isNull("orderID")){
				orderID = json.getString("orderID");
			}
			
			if(!json.isNull("orderNO")){
				orderNO = json.getString("orderNO");
			}
			
			if(!json.isNull("deliveryType")){
				deliveryType = json.getString("deliveryType");
			}
			
			if(!json.isNull("deliveryNO")){
				deliveryNO = json.getString("deliveryNO");
			}
			
			if(!json.isNull("consignee")){
				consignee = json.getString("consignee");
			}
			
			if(!json.isNull("contact")){
				contact = json.getString("contact");
			}
			
			if(!json.isNull("deliveryAddress")){
				deliveryAddress = json.getString("deliveryAddress");
			}
			
			if(!json.isNull("deliveryDetailInfo")){
				JSONArray array = json.getJSONArray("deliveryDetailInfo");
				if(array != null && array.length() != 0){
					mDeliveryList = new ArrayList<DeliveryItem>();
					for (int i = 0; i < array.length(); i++) {
						mDeliveryList.add(new DeliveryItem(array.getJSONObject(i)));
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
