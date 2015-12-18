
package com.app.modulardevice.modulardeviceapp.adapter;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.ble.BleDevice;


// DeviceAdapter - used to build up device list in MainActivity
public class BleDeviceAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BleDevice> devices;

    public BleDeviceAdapter(Context context, ArrayList<BleDevice> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int arg0) {
        return devices.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item_device, null);
        }

        RelativeLayout itemLayout = (RelativeLayout) convertView.findViewById(R.id.gridDeviceLayout);

        TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
        TextView deviceMacAddress = (TextView) convertView.findViewById(R.id.deviceMacAddress);
        TextView rssi = (TextView) convertView.findViewById(R.id.rssi);

        BleDevice bluetoothDevice = devices.get(position);
        if (bluetoothDevice.isConnected()) {
            itemLayout.setBackgroundResource(R.drawable.round_corner_conn);
        } else {
            itemLayout.setBackgroundResource(R.drawable.round_corner_disc);
        }

        deviceName.setText(bluetoothDevice.getName());
        if (bluetoothDevice.getAddress() == null || bluetoothDevice.getAddress().isEmpty()) {
            deviceMacAddress.setText(context.getText(R.string.unknown_device));
        } else {
            deviceMacAddress.setText(bluetoothDevice.getAddress());
        }
        rssi.setText(Integer.toString(bluetoothDevice.getRssi()));



        return convertView;
    }



}
