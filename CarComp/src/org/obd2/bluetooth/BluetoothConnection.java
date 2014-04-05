/* Copyright (c) 2013
 *
 * author: Daniel Furini - dna.furini[at].gmail.com
 *
 */
package org.obd2.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;



public class BluetoothConnection extends CordovaPlugin  {

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
	ConnectionHandler mConnectionHandler;
	private ArrayList<BluetoothDevice> mDiscoveredDevices;
	private JSONArray mJSONDiscoveredDevices;
	
	private CallbackContext mCallbackContext;

	// Messages
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Service Connection
	private boolean isServiceBound;
	private ExecutorService mThreadPool;
	

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
		mThreadPool = this.cordova.getThreadPool();
		
		doBindService();
		//mConnectionHandler.setMemberVariables(context, mHandler);
		
		
		
		OBD2Library.initializeLib();

	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) {

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

		} else if (ACTION_OBD2_CONNECTION_STATUS.equals(action)) {
			result = getOBD2ConnectionStatus(callbackContext);
		} else {
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
				Log.i(TAG, device.getName() + " " + device.getAddress() + " "
						+ device.getBondState());
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
			mConnectionHandler.connect(device);

			result = new PluginResult(PluginResult.Status.OK, true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public PluginResult writeMessage(JSONArray args) {
		PluginResult result = null;		
		final ArrayList<OBDCommand> commands = new ArrayList<OBDCommand>();
		
		Log.e("ARGS", args.toString());
		
		for(int i = 0; i < args.length(); i++){
			JSONObject item;
			try {
				item = args.getJSONObject(i);
				String tmpVal = item.getString("value");
				String val = OBD2Library.getCodeForName(tmpVal);
				OBDCommand cmd = new OBDCommand("01"+val);
				commands.add(cmd);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		mThreadPool.execute(new Runnable(){

			@Override
			public void run() {
				mConnectionHandler.writeMessage(commands);
				
				JSONArray resultArray = new JSONArray();
				
				for (OBDCommand command : commands) {
					String result = command.getResult().substring(4);
					resultArray.put(OBD2Library.returnResultObject(result));
				}
				Log.e("TAG", "Result "+resultArray.toString());
				callJavaScriptfunction("fetchOBD2Values", resultArray);				
			}
		});
		
		result = new PluginResult(PluginResult.Status.OK);
		return result;
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



	public PluginResult getInternetStatus(CallbackContext callbackContext) {
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

	public void callJavaScriptfunction(String functionName, Object object){
		
		this.webView.sendJavascript(functionName+"("+ object+");");
	}
	
	public void updateConnectionStatus(String statement) {
		this.webView
				.sendJavascript("document.getElementById('btConnectionStatus').innerHTML='"
						+ statement + "';");
	}

	public void doBindService() {
		Intent serviceIntent = new Intent(context, ConnectionHandler.class);
		context.bindService(serviceIntent, serviceConn,
				Context.BIND_AUTO_CREATE);
	}

	public void doUnbindService() {
		if (isServiceBound) {
			if (mConnectionHandler.getState() == ConnectionHandler.STATE_CONNECTED)
				mConnectionHandler.stopService();
			Log.d(TAG, "Unbinding OBD service..");
			context.unbindService(serviceConn);
		}
	}


	
	
	private ServiceConnection serviceConn = new ServiceConnection() {
		
		
        public void onServiceConnected(ComponentName className,  IBinder service) {
        	ConnectionHandler.ConnectionHandlerBinder binder = (ConnectionHandler.ConnectionHandlerBinder) service;
            mConnectionHandler = binder.getService();
            isServiceBound = true;
            mConnectionHandler.setMemberVariables(context, mHandler, mThreadPool);
        }

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "Test service is unbound");
			isServiceBound = false;
		}
	};
	
	
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.e("TAG", "MESSAGE "+msg);
		}
	};
	
	
	
	
}