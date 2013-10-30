package obd2bluetooth;

import java.util.ArrayList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.megster.cordova.BluetoothSerialService;

public class BTUtility extends CordovaPlugin{

	//actions
	private static final String ACTIVATE_BLUETOOTH = "activateBluetooth";
	private static final String DEACTIVATE_BLUETOOTH = "deactivateBluetooth";
	private static final String DISCOVER_DEVICES = "discoverDevices";
	private static final String STOP_DISCOVER_DEVICES = "stopDiscoverDevices";
	private static final String CONNECT = "connect";
	private static final String WRITE = "writeToDevice";
	private static final String READ = "readFromDevice";
	private static final String DISCONNECT ="disconnectConnection";
	
	//callbacks
	private CallbackContext mConnectCallback;
	private CallbackContext mDataAvailableCallback;
	
	//member fields
	private BluetoothAdapter mBluetoothAdapter;
	private BTConnectionHandler mBTConnectionHandler;
	
	//memory variables
	private JSONArray listOfDiscoveredDevices;
	
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	StringBuffer buffer = new StringBuffer();
	private String delimiter;
	private static final String TAG = "BTUtility";
	private static final boolean D = true;
	
	
	
	@Override
	public boolean execute(String action, CordovaArgs args,CallbackContext callbackContext) throws JSONException {

		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		if (mBTConnectionHandler == null) {
			mBTConnectionHandler = new BTConnectionHandler(mHandler);
		}

		boolean validAction = true;

		if (action.equals(ACTIVATE_BLUETOOTH)) {
			enableBluetooth();
		}
		else if (action.equals(DEACTIVATE_BLUETOOTH)) {
			disableBluetooth();
		}
		else if (action.equals(DISCOVER_DEVICES)) {
			startDiscovering();
		}
		else if (action.equals(STOP_DISCOVER_DEVICES)) {
			stopDiscovering(callbackContext);		
		}
		else if (action.equals(CONNECT)) {
			connect(args,callbackContext);		
		}
		else if(action.equals(WRITE)){
			write(args, callbackContext);
		}
		else if(action.equals(READ)){
			callbackContext.success(read());
		}
		else if(action.equals(DISCONNECT)){
			disconnect();
		}
		else {
			validAction = false;
		}
		return validAction;
	}
	
	
	

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_READ:
				buffer.append((String) msg.obj);

				if (mDataAvailableCallback != null) {
					sendDataToSubscriber();
				}
				break;
			case MESSAGE_STATE_CHANGE:

				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothSerialService.STATE_CONNECTED:
					Log.i(TAG, "BTConnectionHandler.STATE_CONNECTED");
					notifyConnectionSuccess();
					break;
				case BluetoothSerialService.STATE_CONNECTING:
					Log.i(TAG, "BTConnectionHandler.STATE_CONNECTING");
					break;
				case BluetoothSerialService.STATE_LISTEN:
					Log.i(TAG, "BTConnectionHandler.STATE_LISTEN");
					break;
				case BluetoothSerialService.STATE_NONE:
					Log.i(TAG, "BTConnectionHandler.STATE_NONE");
					break;
				}
				break;
			case MESSAGE_WRITE:
				 byte[] writeBuf = (byte[]) msg.obj;
				 String writeMessage = new String(writeBuf);
				 Log.i(TAG, "Wrote: " + writeMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				Log.i(TAG, msg.getData().getString(DEVICE_NAME));
				break;
			case MESSAGE_TOAST:
				String message = msg.getData().getString(TOAST);
				notifyConnectionLost(message);
				break;
			}
		}
	};
	
	private String read() {
		int length = buffer.length();
		String data = buffer.substring(0, length);
		buffer.delete(0, length);
		return data;
	}
	
	private void notifyConnectionLost(String error) {
		if (mConnectCallback != null) {
			mConnectCallback.error(error);
			mConnectCallback = null;
		}
	}
	
	private void notifyConnectionSuccess() {
		if (mConnectCallback != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK);
			result.setKeepCallback(true);
			mConnectCallback.sendPluginResult(result);
		}
	}
	
	
	private void sendDataToSubscriber() {
		String data = readUntil(delimiter);
		if (data != null && data.length() > 0) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, data);
			result.setKeepCallback(true);
			mDataAvailableCallback.sendPluginResult(result);
		}
	}
	
	private String readUntil(String c) {
		String data = "";
		int index = buffer.indexOf(c, 0);
		if (index > -1) {
			data = buffer.substring(0, index + c.length());
			buffer.delete(0, index + c.length());
		}
		return data;
	}
	
	
	
	
	
	
	
	
	
	
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.i("FOUND","Name " + device.getName() + "-" + device.getAddress());
				JSONObject discoveredDevice = new JSONObject();
				try {
					discoveredDevice.put("name", device.getName());
					discoveredDevice.put("adress", device.getAddress());
					if(!isJSONInArray(discoveredDevice)){
						listOfDiscoveredDevices.put(discoveredDevice);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		}
	};
	
//native functions 
	public void enableBluetooth(){
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				mBluetoothAdapter.enable();
			}
		});
	}
	
	public void disableBluetooth(){
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				mBluetoothAdapter.disable();
			}
		});
	}
	
	private void startDiscovering() {
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		cordova.getActivity().registerReceiver(mReceiver, intentFilter);
		mBluetoothAdapter.startDiscovery();
	}

	
	private void stopDiscovering(CallbackContext callbackContext) {
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		
		PluginResult res = new PluginResult(PluginResult.Status.OK, listOfDiscoveredDevices);
		res.setKeepCallback(true);
		callbackContext.sendPluginResult(res);
		Log.i("Info", "Stopped discovering Devices !");
	}
	
	private void connect(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String macAddress = args.getString(0);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
		
		if (device != null) {
			mConnectCallback = callbackContext;
			mBTConnectionHandler.connect(device, false);
			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);
		} else {
			callbackContext.error("Could not connect to " + macAddress);
		}
	}
	
	public void write(CordovaArgs args, CallbackContext callbackContext){
		String data;
		try {
			data = args.getString(0);
			mBTConnectionHandler.write(data.getBytes());
			callbackContext.success();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect(){
		mBTConnectionHandler.stop();
	}
	
	
	
//utility functions 
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
			for (int cnt = 0; cnt < listOfDiscoveredDevices.length(); cnt++) {
				String listElement;

				listElement = listOfDiscoveredDevices.get(cnt).toString();

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


	

