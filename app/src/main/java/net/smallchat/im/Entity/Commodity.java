package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Commodity implements Serializable{

	private static final long serialVersionUID = -1145345454545L;
	
	/**
	 * "productID": "P01N5GM0NP70ZL",
      "productIMG": "http://p.lefux.com/61/20090803/A0980000DX/Nasty-Stink-Smelly-Fart-1-small.jpg",
      "productName": "Nasty Stink Smelly  Fart Bomb Bag Trick Toy",
      "productQuantity": "2",
      "estimatePrice": "0.10",
      "salePrice": "0.10",
      "productStatus": "true"
	 */
	
	public String productID;
	public String productIMG;
	public String productName;
	public int productQuantity;
	public String estimatePrice;
	public String salePrice;
	public boolean productStatus;
	
	public Commodity(){}
	
	public Commodity(JSONObject json){
		try {
			productID = json.getString("productID");
			if(!json.isNull("productIMG")){
				productIMG = json.getString("productIMG");
			}
			
			if(!json.isNull("productName")){
				productName = json.getString("productName");
			}
			
			if(!json.isNull("productQuantity")){
				productQuantity = json.getInt("productQuantity");
			}
			
			if(!json.isNull("estimatePrice")){
				estimatePrice = json.getString("estimatePrice");
			}
			
			if(!json.isNull("salePrice")){
				salePrice = json.getString("salePrice");
			}
			if(!json.isNull("productStatus")){
				productStatus = json.getBoolean("productStatus");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Commodity> constructCommodityList(JSONArray array){
		try {
			List<Commodity> commodityList = new ArrayList<Commodity>();
			int size = array.length();

			for (int i = 0; i < size; i++) {
				commodityList.add(new Commodity(array.getJSONObject(i)));
			}
			return commodityList;
		} catch (JSONException jsone) {
			jsone.printStackTrace();
		}
		
		return null;
	}

}
