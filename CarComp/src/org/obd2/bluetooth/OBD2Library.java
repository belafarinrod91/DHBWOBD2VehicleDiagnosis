package org.obd2.bluetooth;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class OBD2Library {
	
	private static Map<String,String> mLib = new HashMap<String,String>();
	private static String TAG = "OBD2Lib";
	
	
	public static void initializeLib(){
		mLib.put("oilTemperature", "5C" );
		mLib.put("engineRPM", "0C");
		mLib.put("speed", "0D");
		mLib.put("runtime", "1F");
		mLib.put("fuelType", "51");
		mLib.put("fuelRate", "5E");
	}
	
	
	
	public static String getCodeForName(String label){
		String result = null;
		
		if(mLib.containsKey(label)){
			result = mLib.get(label);
		}
		else{
			result = "No code for this label";
		}
		
		return result;
	}
	
	private static String getNameForCode(String code){
		String result = null;
		
		if(mLib.containsValue(code)){
			for (Map.Entry entry : mLib.entrySet()) {
				if(entry.getValue().equals(code)){
					result = (String)entry.getKey();
				}
			}
	    }
		else {
			result = "No label for this code";
		}
		
		return result; 
	}
	
	private static String resultValueParser(String label, String resultValue){
		String result = null;
		
		if (label.equals("oilTemperature")){
			result = oilTemperature(resultValue);
		}
		else if(label.equals("engineRPM")){
			result = engineRPM(resultValue);
		}
		else if(label.equals("speed")){
			result = String.valueOf(hexParser(resultValue));
		}
		else if(label.equals("runtime")){
			result = engineRuntime(resultValue);
		}
		else if(label.equals("fuelType")){
			result = fuelType(resultValue);
		}
		else if(label.equals("fuelRate")){
			result = fuelRate(resultValue);
		}
		
		
		
		return result;
	}
	
	
	
	
	
	public static JSONObject returnResultObject(String value){
		JSONObject result = new JSONObject();
		
		value = value.replace(" ", "");
		
		String returnCode = value.substring(0,2);
		String label = value.substring(2, 4);
		String resultValue = value.substring(4, value.length());

		label = getNameForCode(label);
		resultValue = resultValueParser(label, resultValue);
		try {
			result.put(label, resultValue);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result; 
	}
	
	private static int hexParser(String value){
		value = value.replace(" ", "");
		int result = Integer.parseInt(value, 16);
		return result; 
	}
	
	
	//return Values for specific codes : 
	private static String oilTemperature(String value){
		String result = null;
		int iValue = hexParser(value)-40;
		result = String.valueOf(iValue);
		return result; 
	}
	
	private static String engineRPM(String value){
		String result = null;
		int iValue = hexParser(value)/4;
		result = String.valueOf(iValue);
		return result;
	}
	
	private static String engineRuntime(String value){
		String result = null;
		int iValue = hexParser(value);
		int hours = iValue / 3600;
		int minutes = (iValue % 3600) / 60;
		int seconds = iValue % 60;
		result = hours+":"+minutes+":"+seconds;
		return result;
	}
	
	private static String fuelType(String value){
		String result = "";
		int iValue = hexParser(value);
		
		switch(iValue){
			case 0 :
				result = "NA";
				break;
			case 1 :
				result = "Gasoline";
				break;
			case 2 :
				result = "Methanol";
				break;
			case 3 :
				result = "Ethanol";
				break;
			case 4 :
				result = "Diesel";
				break;
			case 5 :
				result = "LPG";
				break;
			case 6 :
				result = "CNG";
				break;
			case 7 :
				result = "Propane";
				break;
			case 8 :
				result = "Electric";
				break;
			case 9 :
				result = "Bifuel running Gasoline";
				break;
			case 10 :
				result = "Bifuel running Methanol";
				break;
			case 11 :
				result = "Bifuel running Ethanol";
				break;
			case 12 :
				result = "Bifuel running LPG";
				break;
			case 13 :
				result = "Bifuel running CNG";
				break;
			case 14 :
				result = "Bifuel running Propane";
				break;
			case 15 :
				result = "Bifuel running Electricity";
				break;
			case 16 :
				result = "Bifuel running electric and combustion engine";
				break;
			case 17 :
				result = "Hybrid gasoline";
				break;
			case 18 :
				result = "Hybrid Ethanol";
				break;
			case 19 :
				result = "Hybrid Diesel";
				break;
			case 20 :
				result = "Hybrid Electric";
				break;
			case 21 :
				result = "Hybrid running electric and combustion engine";
				break;
			case 22 :
				result = "Hybrid Regenerative";
				break;
			case 23 :
				result = "Bifuel running diesel";
				break;
		}
		return result;
	}
	
	private static String fuelRate(String value){
		String result = null;
		double iValue = hexParser(value)*0.05;
		result = String.valueOf(iValue);
		return result;
	}
	
	
}
