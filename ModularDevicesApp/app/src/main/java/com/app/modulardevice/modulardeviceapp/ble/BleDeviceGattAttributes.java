package com.app.modulardevice.modulardeviceapp.ble;
/**
 * Created by igbt6 on 03.10.2015.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

public abstract class BleDeviceGattAttributes {

	private static final String DEBUG_TAG = BleDeviceGattAttributes .class
			.getSimpleName();
	private static final boolean DEBUG_ENABLE = true;

	//HM 11 settings
	public static final UUID UUID_HM11_DEVICE_SERVICE = UUID
			.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	public static final UUID UUID_HM11_DEVICE_DATA_CHARACTERISTIC = UUID
			.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

	public static final UUID  UUID_HM11_DEVICE_NAME_CHARACTERISTIC = UUID
			.fromString("00002a00-0000-1000-8000-00805f9b34fb");

	public static final UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");



	private static HashMap<String, String> attributes = new HashMap();
	static {
		// Sample Services.
		attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "Hm11_Device_Service");
		attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Hm11_Device_Info_Service");
		// Sample Characteristics.
		attributes.put("0000ffe1-0000-1000-8000-00805f9b34fb", "Hm11_Device_Data_Characteristic");
		attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Hm11_Device_Name_Characteristic");
	}

	public static String lookup(String uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}

	public static String lookup(String uuid) {
		String name = attributes.get(uuid);
		return name;
	}

	public BleDeviceGattAttributes(BluetoothGatt gatt, BluetoothAdapter btAdapter) {
		mBluetoothGatt = gatt;
		mBluetoothAdapter = btAdapter;

	}

	private BluetoothGatt mBluetoothGatt;
	private BluetoothAdapter mBluetoothAdapter;


	private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	private void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic) {
		mBluetoothGatt.setCharacteristicNotification(characteristic, true);
		BluetoothGattDescriptor descriptor = characteristic
				.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);

	}

	public void sendSomeDataToServer(byte[] data) {
		BluetoothGattCharacteristic gattCharacteristic;
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			if (DEBUG_ENABLE)
				Log.e(DEBUG_TAG, "ERROR-  ble gatt not initialized");
			return;
		}
		gattCharacteristic = mBluetoothGatt.getService(
				UUID_HM11_DEVICE_SERVICE ).getCharacteristic(
				UUID_HM11_DEVICE_DATA_CHARACTERISTIC);
		gattCharacteristic.setValue(data);
		mBluetoothGatt.writeCharacteristic(gattCharacteristic);
	}

	public void readCharacteristicData() {
		BluetoothGattCharacteristic gattCharacteristic;
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			if (DEBUG_ENABLE)
				Log.e(DEBUG_TAG, "ERROR-  ble gatt not initialized");
			return;
		}
		gattCharacteristic = mBluetoothGatt.getService(UUID_HM11_DEVICE_SERVICE )
				.getCharacteristic(UUID_HM11_DEVICE_DATA_CHARACTERISTIC);
		readCharacteristic(gattCharacteristic);
	}

	public void enableCharacteristicNotification() {
		BluetoothGattCharacteristic gattCharacteristic;
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			if (DEBUG_ENABLE)
				Log.e(DEBUG_TAG, "ERROR-  ble gatt not initialized");
			return;
		}
		gattCharacteristic = mBluetoothGatt.getService(
				UUID_HM11_DEVICE_SERVICE).getCharacteristic(
				UUID_HM11_DEVICE_DATA_CHARACTERISTIC);
		setCharacteristicNotification(gattCharacteristic);
	}





}
