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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class ConnectionHandler extends Service {

	public final String TAG = "ConnectionHandler";

	public final static int STATE_CONNECTING_ERROR = -2;
	public final static int STATE_STOPPED_SERVICE = -1;
	public final static int STATE_NONE = 0;
	public final static int STATE_CONNECTING = 1;
	public final static int STATE_CONNECTED = 2;
	


	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private Context mContext;
	private Handler mHandler;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mRemoteDevice;
	private int mState;
	private BluetoothSocket mSocket;
	private ExecutorService mThreadPool;

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

	public void setMemberVariables(Context ctx, Handler handler, ExecutorService threadPool) {
		mContext = ctx;
		mHandler = handler;
		mThreadPool = threadPool;
	}
	
	public void connect(final BluetoothDevice remoteDevice){
		mThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				mRemoteDevice = remoteDevice;
				mBluetoothAdapter.cancelDiscovery();

				setState(STATE_CONNECTING);

				try {
					mSocket = mRemoteDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
					mSocket.connect();
				} catch (IOException e) {
					setState(STATE_CONNECTING_ERROR);
					e.printStackTrace();
					stopService();
				}

				setState(STATE_CONNECTED);
				
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
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	public void stopService() {
		if (mSocket != null)
			// close socket
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		setState(STATE_STOPPED_SERVICE);
		stopSelf();
	}

	public void setState(int state) {
		mState = state;
		mHandler.obtainMessage(mState);
		Log.e("STATE ", "CHANGED STATE "+state);
	}

	public int getState() {
		return mState;
	}

	

}
