package org.obd2.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.*;

public class BluetoothConnection extends CordovaPlugin {

	// Member-Variables
	public BluetoothAdapter mBluetoothAdapter;
	JSONArray mListOfDiscoveredDevices;
	
	
	// Phonegap-specific actions, which call the function
	public String ACTION_ENABLEBLUETOOTH = "enableBluetooth";
	public String ACTION_DISABLEBLUETOOTH = "disableBluetooth";
	public String ACTION_DISCOVERDECIVES = "discoverDevices";
	public String ACTION_STOPDISCOVERDEVICES = "stopDiscoverDevices";
	public String ACTION_CREATEBOUND = "createBound";
	
	
	// not usable, this moment 
	public String ACTION_CONNECT = "connect";
	public String ACTION_DISCONNECT = "disconnect";

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter.equals(null)) {
			Log.i("obd2.bluetooth", "no adapter was found");
		}
		
		
		if (action.equals(ACTION_ENABLEBLUETOOTH)) {
			enableBluetooth();
		}
		else if (action.equals(ACTION_DISABLEBLUETOOTH)) {
			disableBluetooth();
		}
		else if (action.equals(ACTION_DISCOVERDECIVES)) {
			discoverDevices();
		}
		else if (action.equals(ACTION_STOPDISCOVERDEVICES)) {
			stopDiscovering(callbackContext);
		}
		else if (action.equals(ACTION_CREATEBOUND)) {
			try {
				createBound(args, callbackContext);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (action.equals(ACTION_CONNECT)) {
			//connect();
		}
		else if (action.equals(ACTION_DISCONNECT)) {
			disconnect();
		}
		
		return false;

	}

	
	
	
	
	public void enableBluetooth() {
		if (!mBluetoothAdapter.equals(null)) {
			mBluetoothAdapter.enable();
			Log.i("obd2.bluetooth", "bluetooth on");
		}

	}

	public void disableBluetooth() {
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable();
			Log.i("obd2.bluetooth", "bluetooth off");
		}
	}

	public void discoverDevices() {
		mListOfDiscoveredDevices = new JSONArray();
		Log.i("Log", "in the start searching method");
		IntentFilter intentFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		cordova.getActivity().registerReceiver(mFoundDevices, intentFilter);
		mBluetoothAdapter.startDiscovery();
	}
	
	private void stopDiscovering(CallbackContext callbackContext) {
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		PluginResult res = new PluginResult(PluginResult.Status.OK,
				mListOfDiscoveredDevices);
		res.setKeepCallback(true);
		callbackContext.sendPluginResult(res);

		Log.i("Info", "Stopped discovering Devices !");

	}
	
	


	public boolean connect(BluetoothDevice btDevice) {
		Boolean bool = false;
        try {
            Log.i("Log", "service method is called ");
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(btDevice);//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool.booleanValue();
    }
	
	
	public void disconnect(){
		
	}

	public void createBound(JSONArray args, CallbackContext callbackContext) throws Exception {
		String macAddress = args.getString(0);
		Log.i("obd2.bluetooth", "Connect to MacAddress "+macAddress);
		BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
		Log.i("Device","Device "+btDevice);
		
		Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");  
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
        
        
	}       
	
	
        
	

	public boolean removeBound(BluetoothDevice btDevice) throws Exception {
		  Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
	      Method removeBondMethod = btClass.getMethod("removeBond");  
	      Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);  
	      return returnValue.booleanValue();  
	}

	private BroadcastReceiver mFoundDevices = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Message msg = Message.obtain();
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Toast.makeText(context, "found Device !", Toast.LENGTH_SHORT).show();

				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				
				Log.i("FOUND", "Name " + device.getName() + "-" + device.getAddress());
				JSONObject discoveredDevice = new JSONObject();
				try {
					discoveredDevice.put("name", device.getName());
					discoveredDevice.put("adress", device.getAddress());
					if (!isJSONInArray(discoveredDevice)) {
						mListOfDiscoveredDevices.put(discoveredDevice);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		}
	};
	
	/**
	 * This function checks, whether a discovered Bluetooth-Device is already in the 
	 * result-list. If a device is already in list, the function returns true, otherwise
	 * false.
	 * 
	 * @param discoveredDevice - device which should be checked
	 * @return
	 */
	public boolean isJSONInArray(JSONObject discoveredDevice) {
		boolean result = false;

		try {
			for (int cnt = 0; cnt < mListOfDiscoveredDevices.length(); cnt++) {
				String listElement;

				listElement = mListOfDiscoveredDevices.get(cnt).toString();

				String checkElement = discoveredDevice.toString();

				if (listElement.equals(checkElement)) {
					result = true;
					System.out.println(listElement + "-" + checkElement);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	

}
