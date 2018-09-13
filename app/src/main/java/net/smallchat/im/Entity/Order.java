package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Order implements Serializable{

	private static final long serialVersionUID = 143456464564165L;
	
	/**
	 *       
      "orderID": "201192",
      "orderNo": "N5K4P1JB-GE",
      "orderTotalAmount": "61.63",
      "shopsTotalAmount": "33.63",
      "carriageAmount": "28.00",
      "receiver": "testaa  testaa",
      "recerverPhone": "12313213",
      "recerverAddr": "United States Florida testaa testaa",
      "payMethod": "在线支付",
      "deliverMothed": "Regular Post Service",
      "dispatchTime": "",
      "deliverTime": "",
      "orderStatus": "未完结",
      "belongTo": "OED",
      "orderCreatedTime": "2013_04_01T09:50:38Z",
      "existOrderTrack": "false",
      "shopDetails": [
        {
          "pic": "http://p.lefux.com/61/20130621/X00010GPVT/woman-handbag-solid-small-nylon-zipper-opening-p1324906-5441456-small.jpg",
          "name": "Fashion Longchamp Solid Small Nylon Zipper OPening Woman Handbag",
          "nums": "1",
          "orderPrice": "5.63",
          "salePrice": "5.63",
          "status": ""
        }
      ]
	 */
	
	public String orderID;
	public String orderNo;
	public String orderTotalAmount;
	public String orderStatus = "";
	public String belongTo;
	public String orderCreatedTime;
	public boolean existOrderTrack;
	public String shopsTotalAmount;
	public String deliveryFee;
	public String congisee;
	public String contctNum;
	public String congiseeAddr;
	public String payWay;
	public String deliveryWay;
	public String deliveryTime;
	public String deliveryDate;
	public List<Commodity> mCommodityList;
	public String productQuantity;
	public boolean canCancel = false;

	public Order(){}
	
	public Order(JSONObject json) {
		try {
			orderID = json.getString("orderID");
			if(!json.isNull("orderNo")){
				orderNo = json.getString("orderNo");
			}
			
			if(!json.isNull("orderTotalAmount")){
				orderTotalAmount = json.getString("orderTotalAmount");
			}
			
			if(!json.isNull("orderStatus")){
				orderStatus=  json.getString("orderStatus");
			}
			
			if(!json.isNull("belongTo")){
				belongTo = json.getString("belongTo");
			}
			
			if(!json.isNull("orderCreatedTime")){
				orderCreatedTime = json.getString("orderCreatedTime");
			}
			
			if(!json.isNull("existOrderTrack")){
				existOrderTrack = json.getBoolean("existOrderTrack");
			}
			
			if(!json.isNull("shopsTotalAmount")){
				shopsTotalAmount = json.getString("shopsTotalAmount");
			}
			
			if(!json.isNull("carriageAmount")){
				deliveryFee = json.getString("carriageAmount");
			}
			
			if(!json.isNull("receiver")){
				congisee = json.getString("receiver");
			}
			
			if(!json.isNull("recerverPhone")){
				contctNum = json.getString("recerverPhone");
			}
			
			if(!json.isNull("recerverAddr")){
				congiseeAddr = json.getString("recerverAddr");
			}
			
			if(!json.isNull("payMethod")){
				payWay = json.getString("payMethod");
			}
			
			if(!json.isNull("deliverMothed")){
				deliveryWay = json.getString("deliverMothed");
			}
			
			if(!json.isNull("dispatchTime")){
				deliveryTime = json.getString("dispatchTime");
			}
			
			if(!json.isNull("deliverTime")){
				deliveryDate = json.getString("deliverTime");
			}
			
			if(!json.isNull("productList")){
				JSONArray array = json.getJSONArray("productList");
				if(array != null){
					mCommodityList = new ArrayList<Commodity>();
					List<Commodity> tempList = Commodity.constructCommodityList(array);
					if(tempList != null){
						mCommodityList.addAll(tempList);
					}
				}
			}
			
			if(!json.isNull("productNums")){
				productQuantity = json.getString("productNums");
			}
			
			if(!json.isNull("canCancel")){
				int cancel = json.getInt("canCancel");
				canCancel = cancel == 1 ? true : false;
			}
		} catch ( NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Order> constructOrderList(JSONArray array){
		try {
			List<Order> orderList = new ArrayList<Order>();
			int size = array.length();

			for (int i = 0; i < size; i++) {
				orderList.add(new Order(array.getJSONObject(i)));
			}
			return orderList;
		} catch (JSONException jsone) {
			jsone.printStackTrace();
		}
		
		return null;
	}
}
