//TODO : onConnectionLost;
//TODO : onConnectionFailed;
//TODO : permanent Statusbar icon while service is running 



package org.obd2.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class ConnectionHandler extends Service {

	public final String TAG = "ConnectionHandler";

	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private Context mContext;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mRemoteDevice;
	private BluetoothSocket mSocket;
	private ExecutorService mThreadPool;
	
	
	private Handler mHandler;
	private int mState;
	public final static int STATE_CONNECTION_ERROR = -4;
	public final static int STATE_DISCONNECTING_ERROR = -3;
	public final static int STATE_CONNECTING_ERROR = -2;
	public final static int STATE_SERVICE_STOPPED = -1;
	public final static int STATE_NONE = 0;
	public final static int STATE_CONNECTING = 1;
	public final static int STATE_CONNECTED = 2;
	public final static int STATE_DISCONNECTING = 3;
	public final static int STATE_DISCONNECTED = 4;
	public final static int STATE_SERVICE_RUNNING = 5;
	
	public final IBinder mBinder = new ConnectionHandlerBinder();
	
	public class ConnectionHandlerBinder extends Binder {
        public ConnectionHandler getService() {
            return ConnectionHandler.this;
        }
    }

	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void startService(Context ctx, Handler handler, ExecutorService threadPool) {
		mContext = ctx;
		mHandler = handler;
		mThreadPool = threadPool;
		setState(STATE_SERVICE_RUNNING);
	}
	
	public void connect(final BluetoothDevice remoteDevice){
		
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				mRemoteDevice = remoteDevice;
				mBluetoothAdapter.cancelDiscovery();
				
				
				
				Bundle bundle = new Bundle();
				bundle.putString(BluetoothConnection.DEVICE_NAME, mRemoteDevice.getName());
				
				setState(STATE_CONNECTING, bundle);

				try {
					mSocket = mRemoteDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
					mSocket.connect();
				} catch (IOException e) {
					setState(STATE_CONNECTING_ERROR);
					e.printStackTrace();
					stopService();
				}

				
				setState(STATE_CONNECTED, bundle);
			}
			
		});
		
	}

	public ArrayList<OBDCommand> writeMessage(final ArrayList<OBDCommand> commands) {
		final CountDownLatch latch = new CountDownLatch(commands.size());
		
		mThreadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				for(OBDCommand command : commands){
					try {
						command.run(mSocket.getInputStream(), mSocket.getOutputStream());
						latch.countDown();
					} catch (IOException e) {
						e.printStackTrace();
						setState(STATE_CONNECTION_ERROR);
					}
				}
				
			}
			
		});
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return commands;
	}

	public void disconnect(){
		setState(STATE_DISCONNECTING);
		if(mSocket != null){
			try {
				mSocket.close();
				setState(STATE_NONE);
			} catch (IOException e) {
				setState(STATE_DISCONNECTING_ERROR);
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void stopService() {
		Log.d(TAG, "Was stopped.");
		if (mSocket != null)
			// close socket
			try {
				mSocket.close();
				setState(STATE_NONE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		setState(STATE_SERVICE_STOPPED);
		stopSelf();
	}

	public void setState(int state, Bundle bundle) {
		mState = state;
		Message message = mHandler.obtainMessage(state);
		message.setData(bundle);
		mHandler.sendMessage(message);
		Log.e("STATE ", "CHANGED STATE "+state);
	}
	
	public void setState(int state){
		Log.e("STATE ", "CHANGED STATE "+state);
		mState = state;
		mHandler.obtainMessage(mState);
	}
	
	public int getState() {
		return mState;
	}
}
