/* Copyright (c) 2013
 *
 * author: Daniel Furini - dna.furini[at].gmail.com
 *
 */
package org.obd2.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothConnection extends CordovaPlugin {

	// Debug messages
	private static final String TAG = "BluetoothPlugin";
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Actions called from the UI
	public static final String ACTION_ENABLE_BT = "enableBT";
	public static final String ACTION_DISABLE_BT = "disableBT";
	public static final String ACTION_IS_BT_ENABLED = "isBTEnabled";
	public static final String ACTION_PAIR_BT = "pairBT";
	public static final String ACTION_UNPAIR_BT = "unPairBT";
	public static final String ACTION_CONNECT = "connect";
	public static final String ACTION_DISCOVER_DEVICES = "discoverDevices";
	public static final String ACTION_STOP_DISCOVER = "stopDiscover";
	public static final String ACTION_LIST_BOUND_DEVICES = "listBoundDevices";
	public static final String ACTION_IS_BOUND_BT = "isBound";
	public static final String ACTION_WRITE_MESSAGE = "writeMessage";
	public static final String ACTION_GET_OBD2_VALUES = "getOBD2Values";
	public static final String ACTION_OBD2_CONNECTION_STATUS = "getOBD2ConnectionStatus";
	public static final String ACTION_GET_LOCATION = "getLocation";
	public static final String Action_GET_LOCATION_STATUS = "getLocationStatus";

	// MemberVariables
	private static BluetoothAdapter mBtAdapter;
	private boolean mIsDiscovering = false;
	private Context context;
	private ConnectionHandler mConnectionHandler = null;
	private ArrayList<BluetoothDevice> mDiscoveredDevices;
	private JSONArray mJSONDiscoveredDevices;

	private List<String> mResultsOfOBD2Values;
	private int mMsgCnt;
	private JSONArray mMsgList;
	private JSONArray mResultArrayForOBD2Values;
	
	String mStringBuffer;
	//error handling 
	private List<String> mSentValues; 
	private List<String> mReceivedValues;

	private GPSTracker mGps;
	private CallbackContext mCallbackContext;

	// Messages
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		context = this.cordova.getActivity().getBaseContext();

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery starts
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		context.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		context.registerReceiver(mReceiver, filter);

		// Register for broadcasts when connectivity state changes
		filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(mReceiver, filter);

		mConnectionHandler = new ConnectionHandler(context, mHandler);
		OBD2Library.initializeLib();
		
		mSentValues = new ArrayList<String>();
		mReceivedValues = new ArrayList<String>();
		mStringBuffer = new String();

		mGps = new GPSTracker(context);
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {

		Log.d(TAG, "Plugin Called");
		this.mCallbackContext = callbackContext;
		PluginResult result = null;

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mDiscoveredDevices = new ArrayList<BluetoothDevice>();

		if (ACTION_DISCOVER_DEVICES.equals(action)) {
			discoverDevices(callbackContext);

		} else if (ACTION_IS_BT_ENABLED.equals(action)) {
			result = isEnabled();

		} else if (ACTION_ENABLE_BT.equals(action)) {
			result = enableBluetooth();

		} else if (ACTION_DISABLE_BT.equals(action)) {
			result = disableBluetooth();

		} else if (ACTION_PAIR_BT.equals(action)) {
			result = pair(args);

		} else if (ACTION_UNPAIR_BT.equals(action)) {
			result = unpair(args);

		} else if (ACTION_LIST_BOUND_DEVICES.equals(action)) {
			result = listBoundDevices(callbackContext);

		} else if (ACTION_STOP_DISCOVER.equals(action)) {
			result = stopDiscover();

		} else if (ACTION_IS_BOUND_BT.equals(action)) {
			result = isBound(args);

		} else if (ACTION_WRITE_MESSAGE.equals(action)) {
			result = writeMessage(args);

		} else if (ACTION_CONNECT.equals(action)) {
			result = connect(args);
			
		} else if (ACTION_GET_OBD2_VALUES.equals(action)) {
		    result = getOBD2Values(args);
		    
		} else if (ACTION_OBD2_CONNECTION_STATUS.equals(action)) {
			result = getOBD2ConnectionStatus(callbackContext);
		
			
		} else if (action.equals(ACTION_GET_LOCATION)) {
			result = getLocation(callbackContext);
		}

		else if (action.equals(Action_GET_LOCATION_STATUS)) {
			result = getLocationStatus(callbackContext);
		}
		else {
			result = new PluginResult(PluginResult.Status.INVALID_ACTION);
			Log.d(TAG, "Invalid action : " + action + " passed");
		}
			
		this.mCallbackContext.sendPluginResult(result);
		return true;
		
	}

	public PluginResult enableBluetooth() {
		PluginResult result = null;
		try {
			mBtAdapter.enable();
			Log.d("BluetoothPlugin - " + ACTION_ENABLE_BT, "Returning "
					+ "Result: " + mBtAdapter.isEnabled());
			result = new PluginResult(PluginResult.Status.OK,
					mBtAdapter.isEnabled());
		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_ENABLE_BT, "Got Exception "
					+ Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;
	}

	public PluginResult disableBluetooth() {
		PluginResult result = null;
		try {
			if (mBtAdapter.isEnabled())
				mBtAdapter.disable();
			Log.d("BluetoothPlugin - " + ACTION_DISABLE_BT, "Returning "
					+ "Result: " + mBtAdapter.isEnabled());
			result = new PluginResult(PluginResult.Status.OK,
					mBtAdapter.isEnabled());
		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_DISABLE_BT, "Got Exception "
					+ Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;
	}

	public PluginResult isEnabled() {
		PluginResult result = null;
		try {
			Log.d("BluetoothPlugin - " + ACTION_DISABLE_BT, "Returning "
					+ "Result: " + mBtAdapter.isEnabled());
			result = new PluginResult(PluginResult.Status.OK,
					mBtAdapter.isEnabled());
		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_DISABLE_BT, "Got Exception "
					+ Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;
	}

	public void discoverDevices(final CallbackContext callbackContext) {

		this.cordova.getThreadPool().execute(new Runnable() {

			@Override
			public void run() {
				PluginResult result = null;
				try {
					mDiscoveredDevices.clear();
					setDiscovering(true);

					if (mBtAdapter.isDiscovering()) {
						mBtAdapter.cancelDiscovery();
					}
					mBtAdapter.startDiscovery();

					while (mIsDiscovering) {
					}

					mJSONDiscoveredDevices = createJSONArrayOfDiscoveredDevices();

					result = new PluginResult(PluginResult.Status.OK,
							mJSONDiscoveredDevices);
					result.setKeepCallback(true);
					callbackContext.sendPluginResult(result);
				}

				catch (Exception Ex) {
					Log.d("BluetoothPlugin - " + ACTION_DISCOVER_DEVICES,
							"Got Exception " + Ex.getMessage());
					result = new PluginResult(PluginResult.Status.ERROR);
				}

			}
		});

	}

	public PluginResult stopDiscover() {
		PluginResult result = null;

		try {

			setDiscovering(true);

			Log.i(TAG, "BluetoothPlugin - " + ACTION_STOP_DISCOVER);
			result = new PluginResult(PluginResult.Status.OK);

		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_STOP_DISCOVER, "Got Exception "
					+ Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;

	}

	public PluginResult pair(JSONArray args) {
		PluginResult result = null;
		try {
			String addressDevice = args.getString(0);

			if (mBtAdapter.isDiscovering()) {
				mBtAdapter.cancelDiscovery();
			}
			BluetoothDevice device = mBtAdapter.getRemoteDevice(addressDevice);
			boolean paired = false;
			Log.d(TAG,
					"Pairing with Bluetooth device with name "
							+ device.getName() + " and address "
							+ device.getAddress());

			try {
				Method m = device.getClass().getMethod("createBond");
				paired = (Boolean) m.invoke(device);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Log.d("BluetoothPlugin - " + ACTION_PAIR_BT, "Returning "
					+ "Result: " + paired);
			result = new PluginResult(PluginResult.Status.OK, paired);
		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_PAIR_BT,
					"Got Exception " + Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;
	}

	public PluginResult unpair(JSONArray args) {
		PluginResult result = null;
		try {
			String addressDevice = args.getString(0);

			if (mBtAdapter.isDiscovering()) {
				mBtAdapter.cancelDiscovery();
			}

			BluetoothDevice device = mBtAdapter.getRemoteDevice(addressDevice);
			boolean unpaired = false;

			Log.d(TAG, "Unpairing Bluetooth device with " + device.getName()
					+ " and address " + device.getAddress());

			try {
				Method m = device.getClass().getMethod("removeBond");
				unpaired = (Boolean) m.invoke(device);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d("BluetoothPlugin - " + ACTION_UNPAIR_BT, "Returning "
					+ "Result: " + unpaired);
			result = new PluginResult(PluginResult.Status.OK, unpaired);
		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_UNPAIR_BT, "Got Exception "
					+ Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;
	}

	public PluginResult listBoundDevices(CallbackContext callbackContext) {
		PluginResult result = null;
		
		JSONArray resultArray = new JSONArray();
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.i(TAG, device.getName() + " " + device.getAddress()+ " " + device.getBondState());
				JSONObject jDevice = new JSONObject();
				
				try {
					jDevice.put("name", device.getName());
					jDevice.put("address", device.getAddress());
					resultArray.put(jDevice);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		result = new PluginResult(PluginResult.Status.OK, resultArray);
		result.setKeepCallback(true);
		callbackContext.sendPluginResult(result);
		return result;
	}

	public PluginResult isBound(JSONArray args) {
		PluginResult result = null;
		try {

			String addressDevice = args.getString(0);
			BluetoothDevice device = mBtAdapter.getRemoteDevice(addressDevice);
			Log.i(TAG, "BT Device in state " + device.getBondState());

			boolean state = false;

			if (device != null && device.getBondState() == 12)
				state = true;
			else
				state = false;

			Log.d(TAG, "Is Bound with " + device.getName() + " - address "
					+ device.getAddress());
			Log.d("BluetoothPlugin - " + ACTION_IS_BOUND_BT, "Returning "
					+ "Result: " + state);
			result = new PluginResult(PluginResult.Status.OK, state);

		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_IS_BOUND_BT, "Got Exception "
					+ Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;
	}

	public PluginResult connect(JSONArray args) {

		PluginResult result = null;
		String macAddress;

		try {

			macAddress = args.getString(0);
			Log.i(TAG, "connect to ..." + macAddress);
			BluetoothDevice device = mBtAdapter.getRemoteDevice(macAddress);
			mConnectionHandler.connect(device, false);

			result = new PluginResult(PluginResult.Status.OK, true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public PluginResult writeMessage(JSONArray args) {
		PluginResult result = null;
		try {
			String msg = args.getString(0);
			msg = msg + "1\r";
			if (mConnectionHandler.getState() != ConnectionHandler.STATE_CONNECTED) {
				Log.d(TAG, "Can't send, not connected");
				result = new PluginResult(PluginResult.Status.ERROR);
			}

			if (msg.length() > 0) {
				byte[] send = msg.getBytes();
				mConnectionHandler.write(send);
				//Log.d("BluetoothPlugin - " + ACTION_WRITE_MESSAGE, "Returning "+ "Result: " + send);
				result = new PluginResult(PluginResult.Status.OK, send);
			} else {
				Log.d(TAG, "Nothing to send here.");
				result = new PluginResult(PluginResult.Status.ERROR);
			}
		} catch (Exception Ex) {
			Log.d("BluetoothPlugin - " + ACTION_WRITE_MESSAGE, "Got Exception "
					+ Ex.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR);
		}
		return result;
	}

	private void writeMessage(String msg) {
		msg = msg + '\r';

		if (mConnectionHandler.getState() != ConnectionHandler.STATE_CONNECTED) {
			Log.d(TAG, "Can't send, not connected");
		}

		if (msg.length() > 0) {
			byte[] send = msg.getBytes();
			mConnectionHandler.write(send);
			Log.d(TAG, "Returning " + "Result: " + send);
		} else {
			Log.d(TAG, "Nothind to send here.");
		}

	}

	public PluginResult getOBD2Values(JSONArray args) {
		mReceivedValues.clear();
		mSentValues.clear();	
		mResultArrayForOBD2Values = null;
		mResultsOfOBD2Values = null;
		
		PluginResult result = null;
		mResultsOfOBD2Values = new ArrayList<String>();		
		mMsgList = args;
		mMsgCnt = 0;

		try {
			parseAndWriteMsg();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = new PluginResult(PluginResult.Status.OK);
		return result;
	}

	public void parseAndWriteMsg() throws JSONException {
		if (mMsgList.length() > mMsgCnt) {
			JSONObject item = mMsgList.getJSONObject(mMsgCnt);
			String tmpVal = item.getString("value");
			String val = OBD2Library.getCodeForName(tmpVal);
			mSentValues.add(val);
			
			Log.d(TAG, "WRITE "+val);
			writeMessage("01" + val);
			mMsgCnt++;
		}	
		else {
			processMessage();
		}
	}
	
	public void processMessage(){
		mResultArrayForOBD2Values = new JSONArray();
		for(String s :mResultsOfOBD2Values){ 
			if(s.length() > 6 && !s.equals("")){
				s = s.replaceAll("[^0-9a-fA-F]", "");
				//Log.d(TAG, "Processed Message "+s);
				JSONObject obj = OBD2Library.returnResultObject(s); 
				mReceivedValues.add("");//OBD2Library.getLabelForAnswer(s));
				mResultArrayForOBD2Values.put(obj); 
			}
		} 
		
		
		
		/*if(!mSentValues.equals(mReceivedValues)){
			this.webView.sendJavascript("alert('Some values are missing.');");	
		} */
		
		this.webView.sendJavascript("fetchOBD2Values("+ mResultArrayForOBD2Values+");");
	}
	
	
	public PluginResult getOBD2ConnectionStatus(CallbackContext callbackContext) {
		PluginResult result = null;
		boolean obd2connected = false;

		if (mConnectionHandler.getState() == ConnectionHandler.STATE_CONNECTED) {
			obd2connected = true;
		} else {
			obd2connected = false;
		}

		result = new PluginResult(PluginResult.Status.OK, obd2connected);
		result.setKeepCallback(true);
		callbackContext.sendPluginResult(result);
		return result;
	}
	


	public PluginResult getLocation(CallbackContext callbackContext) {
		PluginResult result = null;
		Location loc = mGps.getLocation();
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();

		JSONObject obj = new JSONObject();
		try {
			obj.put("lat", lat);
			obj.put("lon", lon);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		result = new PluginResult(PluginResult.Status.OK, obj);
		result.setKeepCallback(true);
		callbackContext.sendPluginResult(result);
		return result;
	}

	public PluginResult getLocationStatus(CallbackContext callbackContext) {
		PluginResult result = null;
		boolean res = false;

		if (mGps.canGetLocation()) {
			res = true;
		}
		result = new PluginResult(PluginResult.Status.OK, res);
		result.setKeepCallback(true);
		callbackContext.sendPluginResult(result);
		return result;
	}
	
	
	public PluginResult getInternetStatus(CallbackContext callbackContext){
		PluginResult result = null;
		
		
		return result;
	}

	public void setDiscovering(boolean state) {
		mIsDiscovering = state;
	}

	public void addDevice(BluetoothDevice device) {
		if (!mDiscoveredDevices.contains(device)) {
			mDiscoveredDevices.add(device);
		}
	}

	public JSONArray createJSONArrayOfDiscoveredDevices() {
		JSONArray result = new JSONArray();

		for (BluetoothDevice device : mDiscoveredDevices) {
			JSONObject jDevice = new JSONObject();
			try {
				jDevice.put("name", device.getName());
				jDevice.put("address", device.getAddress());
				result.put(jDevice);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return result;
	}

	@Override
	public void onDestroy() {
		context.unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.i(TAG,
						"Device found " + device.getName() + " "
								+ device.getBondState() + " "
								+ device.getAddress());
				addDevice(device);

			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Log.i(TAG, "Discovery started");
				setDiscovering(true);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Log.i(TAG, "Discovery was finished");
				setDiscovering(false);
			}
		}
	};

	public void updateConnectionStatus(String statement) {
		this.webView
				.sendJavascript("document.getElementById('btConnectionStatus').innerHTML='"
						+ statement + "';");
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case ConnectionHandler.STATE_CONNECTED:
					Log.d(TAG, "Handler - BluetoothChatService.STATE_CONNECTED");
					updateConnectionStatus("Connected.");
					
					/*
					//reset 
					writeMessage("ATZ");
					//linefeed off
					writeMessage("ATL0");
					//timeout
					writeMessage("ATST" + Integer.toHexString(0xFF & 62));
					//setprotocol 
					writeMessage("ATSP00");
					*/
					
					break;
				case ConnectionHandler.STATE_CONNECTING:
					Log.d(TAG,
							"Handler - BluetoothChatService.STATE_CONNECTING");
					updateConnectionStatus("Connecting ... ");
					break;
				case ConnectionHandler.STATE_LISTEN:
					Log.d(TAG, "Handler - BluetoothChatService.STATE_LISTEN");
					updateConnectionStatus("Listen ... ");
					break;
				case ConnectionHandler.STATE_NONE:
					Log.d(TAG, "Handler - BluetoothChatService.STATE_NONE");
					updateConnectionStatus("Nothing to do / Connection Error");
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				break;
			case MESSAGE_READ:
				
				
				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);
				Log.d(TAG, "MESSAGE READ :"+readMessage);
				mStringBuffer += readMessage;
				
				if (mStringBuffer.contains("41") && mStringBuffer.contains(">")) {
						Log.e("StringBuffer","Before "+mStringBuffer);
						String result = mStringBuffer.substring(mStringBuffer.indexOf("41"), mStringBuffer.indexOf(">")-1);
						mResultsOfOBD2Values.add(result);
						Log.d(TAG, "WAS ADDED TO LIST "+result);
						mStringBuffer = mStringBuffer.substring(mStringBuffer.indexOf(">")+1, mStringBuffer.length());
						Log.e("StringBuffer", "After" +mStringBuffer);
						try {
							parseAndWriteMsg();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					
					
				} else {
					//Log.d(TAG, "NOT IN IF :MESSAGE_READ: " + readMessage);
				}

				break;
			case MESSAGE_DEVICE_NAME:
				Log.d(TAG, "MESSAGE_DEVICE_NAME");
				break;
			case MESSAGE_TOAST:
				Log.d(TAG, "MESSAGE_TOAST");
				break;

			}
		}
	};
}