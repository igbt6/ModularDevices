package com.app.modulardevice.modulardeviceapp.service;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.ble.BleDevice;
import com.app.modulardevice.modulardeviceapp.ble.BleDeviceGattAttributes;
import com.app.modulardevice.modulardeviceapp.model.DataReceiverParser;
import com.app.modulardevice.modulardeviceapp.utils.CircularBuffer;
import com.app.modulardevice.modulardeviceapp.utils.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGD;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGI;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGW;
import static com.app.modulardevice.modulardeviceapp.utils.Util.byteArrayToPrimitives;
import static com.app.modulardevice.modulardeviceapp.utils.Util.convertByteArrayToString;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BleService extends Service {
	private final static String DEBUG_TAG  = BleService.class.getSimpleName();
	private final static boolean DEBUG_ENABLE = true;
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;



	// These constant members are used to sending and receiving broadcasts
	// between BluetoothLeService and rest parts of application
	public static final String ACTION_START_SCAN = "com.app.modulardevice.ACTION_START_SCAN";
	public static final String ACTION_STOP_SCAN = "com.app.modulardevice.ACTION_STOP_SCAN";
	public static final String ACTION_DEVICE_DISCOVERED = "com.app.modulardevice.ACTION_DEVICE_DISCOVERED";
	public static final String ACTION_GATT_CONNECTED = "com.app.modulardevice.ACTION_GATT_CONNECTED";
	public static final String ACTION_GATT_DISCONNECTED = "com.app.modulardevice.ACTION_GATT_DISCONNECTED";
	public static final String ACTION_GATT_CONNECTION_STATE_ERROR = "com.app.modulardevice.ACTION_GATT_CONNECTION_STATE_ERROR";
	public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.app.modulardevice.ACTION_GATT_SERVICES_DISCOVERED";
	public static final String ACTION_DATA_AVAILABLE = "com.app.modulardevice.ACTION_DATA_AVAILABLE";
	public static final String ACTION_DATA_WRITE = "com.app.modulardevice.ACTION_DATA_WRITE";
	public static final String ACTION_READ_REMOTE_RSSI = "com.app.modulardevice.ACTION_READ_REMOTE_RSSI";
	public static final String ACTION_DESCRIPTOR_WRITE = "com.app.modulardevice.ACTION_DESCRIPTOR_WRITE";

	// These constant members are used to sending and receiving extras from
	// broadcast intents
	public static final String SCAN_PERIOD = "scanPeriod";
	public static final String DISCOVERED_DEVICE = "discoveredDevice";
	public static final String DEVICE_NAME = "deviceName";
	public static final String DEVICE_ADDRESS = "deviceAddress";
	public static final String RSSI = "rssi";
	public static final String UUID_CHARACTERISTIC = "uuidCharacteristic";
	public static final String UUID_DESCRIPTOR = "uuidDescriptor";
	public static final String GATT_STATUS = "gattStatus";
	public static final String READ_DATA = "readData";
	public static final String SCAN_RECORD = "scanRecord";

	//broadcast intents concerning received data frames from device
	public static final String READ_MSG_DATA = DataReceiverParser.TYPE_OF_FRAME.MSG_DATA.toString();
    public static final String READ_MSG_DATA_MOD_ID = READ_MSG_DATA+"mod_id";
    public static final String READ_MSG_DATA_RAW_DATA= READ_MSG_DATA+"raw_data";
	public static final String READ_MSG_BROADCAST_DATA = DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST.toString();
	public static final String READ_MSG_UNKNOWN_DATA = DataReceiverParser.TYPE_OF_FRAME.MSG_UNKNOWN.toString();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private Handler mHandler;
	private boolean mScanning;
	private int mLongFrameLength;
    CircularBuffer mFrameBuffer = new CircularBuffer(128);

	private final AppEngine app =AppEngine.getInstance();
	private final IBinder mBinder = new LocalBinder();



	private BluetoothAdapter.LeScanCallback mLeScanCallback= new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Intent broadcastIntent = new Intent(ACTION_DEVICE_DISCOVERED );
			broadcastIntent.putExtra(DISCOVERED_DEVICE, device);
			broadcastIntent.putExtra(RSSI, rssi);
			broadcastIntent.putExtra(SCAN_RECORD, scanRecord);
			sendBroadcast(broadcastIntent);
            LOGI(DEBUG_ENABLE, DEBUG_TAG, "onLeScan - device: " + device.getAddress() + " - rssi: " + rssi);
		}
	};


	// Implements callback methods for GATT events that the app cares about.
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		//called when device has changed its status - connected or disconnected
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if(status==BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED){
					AppEngine app = AppEngine.getInstance();
					app.setBleDevice(new BleDevice(gatt));
					app.getBleDevice().setConnected(true);
                    LOGI(DEBUG_ENABLE, DEBUG_TAG, "Connected to GATT server.");
					broadcastUpdate(ACTION_GATT_CONNECTED);
					// Attempts to discover services after successful connection.
                    LOGI(DEBUG_ENABLE, DEBUG_TAG, "Attempting to start service discovery:");
					mBluetoothGatt.discoverServices();

				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    LOGI(DEBUG_ENABLE, DEBUG_TAG,  "Disconnected from GATT server.");
					AppEngine.getInstance().getBleDevice().setConnected(false);
					broadcastUpdate( ACTION_GATT_DISCONNECTED);
				}
			} else{
				broadcastUpdate(ACTION_GATT_CONNECTION_STATE_ERROR);
			}
		}


		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			}
            LOGI(DEBUG_ENABLE, DEBUG_TAG,  "onServicesDiscovered received: " + status);

		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic,
										 int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
            LOGI(DEBUG_ENABLE, DEBUG_TAG, "onCharacteristicRead - status: " + status + "  - UUID: " + characteristic.getUuid());
		}

		// Called when characteristic was written
		// Broadcast with characteristic uuid is sent
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			broadcastUpdate(ACTION_DATA_WRITE, characteristic);
            LOGI(DEBUG_ENABLE, DEBUG_TAG, "onCharacteristicWrite - status: " + status + "  - UUID: " + characteristic.getUuid());
		}

		// Called when descriptor was written
		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(UUID_DESCRIPTOR, descriptor.getUuid().toString());
			broadcastUpdate(ACTION_DESCRIPTOR_WRITE, map);
            LOGI(DEBUG_ENABLE, DEBUG_TAG, "onDescriptorWrite - status: " + status + "  - UUID: " + descriptor.getUuid().toString());
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            LOGI(DEBUG_ENABLE, DEBUG_TAG, "onCharacteristicWrite - status: " + "  - UUID: " + characteristic.getUuid());
		}
	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
								 final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		intent.putExtra(UUID_CHARACTERISTIC, characteristic.getUuid().toString());
		if (BleDeviceGattAttributes.UUID_HM11_DEVICE_DATA_CHARACTERISTIC.equals(characteristic.getUuid())) {

			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				DataReceiverParser rParser = new DataReceiverParser();
                for(byte b:data){
                    mFrameBuffer.insert(b);
                }
                //LOGI(DEBUG_ENABLE, DEBUG_TAG, "RAW_DATA: " + convertByteArrayToString(mFrameBuffer.peekBuf()));
                int idxOfStart= mFrameBuffer.getIndexOfFirstAppearance(rParser.getStartByte());
                if( idxOfStart!=-1&&mLongFrameLength==0){
                    mFrameBuffer.removeBuf(idxOfStart);
                }
                if(mLongFrameLength==0||mLongFrameLength<=mFrameBuffer.getSize()) {
                    byte[] frame = mLongFrameLength>0?byteArrayToPrimitives(mFrameBuffer.peekBuf(mLongFrameLength)):byteArrayToPrimitives(mFrameBuffer.peekBuf());
                   // LOGI(DEBUG_ENABLE, DEBUG_TAG, "RAW_DATA_FRAME: " + convertByteArrayToString(frame));
                    DataReceiverParser.TYPE_OF_PARSING_ERROR response= rParser.parseDataError(frame);
                    if (response == DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME) {
                        mLongFrameLength = rParser.getFullFrameLength();
                    }
                    else if (response == DataReceiverParser.TYPE_OF_PARSING_ERROR.OK) {
                        if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_DATA) {
                            int modId= rParser.getModuleInfo(rParser.getParsedData()).getFirst();
                            intent.putExtra(READ_MSG_DATA_RAW_DATA, rParser.getParsedData());
                            intent.putExtra(READ_MSG_DATA_MOD_ID, modId);
                            LOGI(DEBUG_ENABLE, DEBUG_TAG, "-------MSG_DATA_RECEIVED------- ");
                        } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST) {
                            LOGI(DEBUG_ENABLE, DEBUG_TAG, "-------MSG_BROADCAST_RECEIVED------- ");
                            intent.putExtra(READ_MSG_BROADCAST_DATA, (Serializable) rParser.getAvailableModules(rParser.getParsedData()));
                        } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_UNKNOWN) {
                            LOGI(DEBUG_ENABLE, DEBUG_TAG, "-------MSG_UNKNOWN_RECEIVED------- ");
                        }
                        if(DEBUG_ENABLE){
                            intent.putExtra(READ_MSG_DATA, frame);  //FOR USE MODULE_TEST_ACTIVITY
                        }
                        mLongFrameLength = 0;
                        mFrameBuffer.removeBuf(rParser.getFullFrameLength());
                    } else {
                        LOGI(DEBUG_ENABLE, DEBUG_TAG, "PARSER_RESP_NOT_OK: " + rParser.parseDataError(frame).toString());
                        if(mFrameBuffer.isFull())
                            mFrameBuffer.clear();
                        else {
                            mFrameBuffer.removeBuf(mLongFrameLength);
                        }
                        mLongFrameLength = 0;
                    }
                }
			}
		}
		else if(BleDeviceGattAttributes.UUID_HM11_DEVICE_NAME_CHARACTERISTIC.equals(characteristic.getUuid())) {
			final String data = characteristic.getStringValue(0);
			intent.putExtra(DEVICE_NAME , data+ "\n" );
		}
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
								 final HashMap<String,String> extrasMap) {
		final Intent intent = new Intent(action);
		if(!extrasMap.isEmpty()){
			Iterator it = extrasMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String,String> me= (Map.Entry<String,String>)it.next();
				intent.putExtra(me.getKey(),me.getValue());
			}
		}
		sendBroadcast(intent);
	}


	private void fillBuffer(ArrayList<Byte> dest, byte[] src, int nrOfBytes){
		if(src!=null && src.length>0&&dest!=null){
			if(nrOfBytes==-1) {
				for (byte b : src) {
					dest.add(b);
				}
			}
			else{
				for (int i=0;i<nrOfBytes&&i<src.length;i++) {
					dest.add(src[i]);
				}
			}
		}
	}


	private byte[] ByteListToPrimitives(ArrayList<Byte> arr){
			byte[] resArr= new byte[arr.size()];
		for (int i=0;i<resArr.length;i++) {
			resArr[i]=arr.get(i);
		}
		return resArr;
	}


	// Starts scanning for new BLE devices
	public void startScanning(final int scanPeriod) {
		mHandler.postDelayed(new Runnable() {
			// Called after scanPeriod milliseconds elapsed
			// It stops scanning and sends broadcast
			@Override
			public void run() {
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);

				Intent broadcastIntent = new Intent(ACTION_STOP_SCAN);
				sendBroadcast(broadcastIntent);
			}
		}, scanPeriod);
		mScanning = true;
		mBluetoothAdapter.startLeScan(mLeScanCallback);

	}
	public class LocalBinder extends Binder {
		public BleService getService() {
			return BleService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that BluetoothGatt.close() is called
		// such that resources are cleaned up properly.  In this particular example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}


	//Initializes a reference to the local Bluetooth adapter.

	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter through
		// BluetoothManager.
		mHandler=new Handler();
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				LOGE(DEBUG_ENABLE, DEBUG_TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			LOGE(DEBUG_ENABLE, DEBUG_TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	//connects to gatt server (hm11 device)
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			LOGW(DEBUG_ENABLE, DEBUG_TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device.  Try to reconnect.
		if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			LOGD(DEBUG_ENABLE, DEBUG_TAG, "Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				LOGD(DEBUG_ENABLE,DEBUG_TAG, "CONNECTING.......");
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			LOGW(DEBUG_ENABLE,DEBUG_TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		LOGD(DEBUG_ENABLE, DEBUG_TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		return true;
	}

	// Disconnects an existing connection or cancel a pending connection

	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LOGW(DEBUG_ENABLE, DEBUG_TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}


	//After using a given BLE device, the app must call this method to ensure resources arereleased properly.
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
	 * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LOGW(DEBUG_ENABLE,DEBUG_TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	public void writeCharacteristic(String data) {
		BluetoothGattCharacteristic gattCharacteristic;
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LOGE(DEBUG_ENABLE, DEBUG_TAG, "ERROR-  ble gatt not initialized");
			return;
		}
		gattCharacteristic = mBluetoothGatt.getService(
				BleDeviceGattAttributes.UUID_HM11_DEVICE_SERVICE).getCharacteristic(
				BleDeviceGattAttributes.UUID_HM11_DEVICE_DATA_CHARACTERISTIC);
		gattCharacteristic.setValue(data);
		mBluetoothGatt.writeCharacteristic(gattCharacteristic);
	}

	public void writeCharacteristic(byte[] data) {
		BluetoothGattCharacteristic gattCharacteristic;
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LOGE(DEBUG_ENABLE, DEBUG_TAG, "ERROR-  ble gatt not initialized");
			return;
		}
		gattCharacteristic = mBluetoothGatt.getService(
				BleDeviceGattAttributes.UUID_HM11_DEVICE_SERVICE).getCharacteristic(
				BleDeviceGattAttributes.UUID_HM11_DEVICE_DATA_CHARACTERISTIC);
		gattCharacteristic.setValue(data);
		mBluetoothGatt.writeCharacteristic(gattCharacteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic Characteristic to act on.
	 * @param enabled If true, enable notification.  False otherwise.
	 */
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
											  boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LOGW(DEBUG_ENABLE,DEBUG_TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);


	}
	// Checks if service is currently scanning for new BLE devices
	public boolean isScanning() {
		return mScanning;
	}
	/**
	 * Retrieves a list of supported GATT services on the connected device. This should be
	 * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null) return null;

		return mBluetoothGatt.getServices();
	}
}
