package com.app.modulardevice.modulardeviceapp.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
/**
 * Created by igbt6 on 04.10.2015.
 */

//wrapper for ble device object
public class BleDevice {

    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private boolean mConnected;
    private int mRssi;


    public static int MAX_EXTRA_DATA = 3;

    public BleDevice(BluetoothGatt bluetoothGatt) {
        mBluetoothDevice = bluetoothGatt.getDevice();
        mBluetoothGatt= bluetoothGatt;
        mConnected = false;
    }

    public BleDevice(BluetoothDevice bleDev,int rssi) {
        mBluetoothDevice = bleDev;
        mRssi = rssi;
        mConnected = false;
    }

    public BluetoothDevice BluetoothDevice() {
        return mBluetoothDevice;
    }


    public BluetoothGatt getBluetoothGatt() {

        return mBluetoothGatt;
    }


    public boolean isConnected() {
        return mConnected;
    }

    public void setConnected(boolean connected) {

        this.mConnected = connected;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public String getName() {
        return mBluetoothDevice.getName();
    }

    public String getAddress() {
        return mBluetoothDevice.getAddress();
    }



}
