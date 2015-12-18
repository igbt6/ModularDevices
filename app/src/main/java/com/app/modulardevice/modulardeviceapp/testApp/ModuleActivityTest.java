package com.app.modulardevice.modulardeviceapp.testApp;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.ble.BleDevice;
import com.app.modulardevice.modulardeviceapp.ble.BleDeviceGattAttributes;

import com.app.modulardevice.modulardeviceapp.model.ModuleDataModel;
import com.app.modulardevice.modulardeviceapp.service.BleService;


import java.util.ArrayList;
import java.util.List;


import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGD;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;

/**
 * Created by igbt6 on 20.10.2015.
 */
public class ModuleActivityTest extends AppCompatActivity {

    private final static String TAG  = ModuleActivityTest.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;
    private Toolbar mToolbar;
    private ModActivityTestPageAdapter modActivityTestPageAdapter;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    public ModuleActivityTest() {
        super();
    }


    private String mDeviceAddress;
    private String mDeviceName;
    public static BleService mBleService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BleService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBleService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    // Handles the following actions sending by BleService
    // ACTION_GATT_CONNECTED,ACTION_GATT_DISCONNECTED,ACTION_GATT_SERVICES_DISCOVERED,ACTION_DATA_AVAILABLE
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                LOGD(LOGGER_ENABLE, TAG, BleService.ACTION_GATT_CONNECTED);
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                LOGD(LOGGER_ENABLE, TAG, BleService.ACTION_GATT_DISCONNECTED);
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                setUsedGattServices(mBleService.getSupportedGattServices());
                LOGD(LOGGER_ENABLE, TAG, BleService.ACTION_GATT_SERVICES_DISCOVERED);
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] receivedData = intent.getByteArrayExtra(BleService.READ_MSG_DATA);

                if(isReadOnOffChecked()) {
                    if (receivedData != null) {
                        final StringBuilder stringBuilder = new StringBuilder(receivedData.length);
                        for(byte byteChar : receivedData)
                            stringBuilder.append(String.format("%02X ", byteChar));
                        displayData(stringBuilder.toString());
                    }
                }
                LOGD(LOGGER_ENABLE, TAG, BleService.ACTION_DATA_AVAILABLE);

                ArrayList<Integer> availableModuleIds=intent.getIntegerArrayListExtra(BleService.READ_MSG_BROADCAST_DATA);
                if(availableModuleIds!=null)
                    updateModulesList(availableModuleIds);
                int moduleId= intent.getIntExtra(BleService.READ_MSG_DATA_MOD_ID, -1);
                byte[] receivedRawData = intent.getByteArrayExtra(BleService.READ_MSG_DATA_RAW_DATA);
                if(receivedRawData!=null&&moduleId!=-1)
                    updateModuleValues(moduleId, receivedRawData);
                LOGE(LOGGER_ENABLE, TAG, BleService.ACTION_DATA_AVAILABLE);
            }

            BleDevice bleDevice = AppEngine.getInstance().getBleDevice();
            if(bleDevice!=null) {
                mDeviceAddress = bleDevice.getAddress();
                mDeviceName =bleDevice.getName();
                if (bleDevice.isConnected()) {
                    mConnected = true;
                    displayDeviceNameAndAddress(mDeviceAddress, mDeviceName);
                    updateConnectionState(R.string.connected);
                } else {
                    mConnected = false;
                    clearUI();
                    updateConnectionState(R.string.disconnected);
                }
            }
        }
    };




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_test_activity);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mViewPager = (ViewPager) findViewById(R.id.view_test_module_activity_pager);
        modActivityTestPageAdapter = new ModActivityTestPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(modActivityTestPageAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.test_module_activity_sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        modActivityTestPageAdapter.notifyDataSetChanged();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(BleService.DEVICE_ADDRESS);
        LOGE(LOGGER_ENABLE, TAG, "mDeviceAddress_______________  "+(mDeviceAddress));

        try {
            BleDevice bleDevice = AppEngine.getInstance().getBleDevice();
            if(bleDevice!=null) {
                mDeviceAddress = bleDevice.getAddress();
                mDeviceName =bleDevice.getName();
                if (bleDevice.isConnected()) {
                    mConnected = true;
                    //setUsedGattServices(bleDevice.getBluetoothGatt().getServices());
                    displayDeviceNameAndAddress(mDeviceAddress, mDeviceName);
                    updateConnectionState(R.string.connected);
                } else {
                    mConnected = false;
                    clearUI();
                    updateConnectionState(R.string.disconnected);
                }
            }
            else{
                LOGE(LOGGER_ENABLE, TAG, "--------Failed getting last device");
                mConnected = false;
                clearUI();
                updateConnectionState(R.string.disconnected);
            }
        }
        catch(Exception e){
            LOGE(LOGGER_ENABLE, TAG, "Failed getting last device",e);
            clearUI();
        }

        connectService();

    }

    private void connectService() {
        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOGD(LOGGER_ENABLE, TAG, "ON___________RESUME+++++++++++++");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBleService != null) {
            mBleService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleService.disconnect();
        unbindService(mServiceConnection);
        mBleService = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_module_activity, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBleService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBleService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void updateConnectionState(final int resourceId) {
        LOGE(LOGGER_ENABLE, TAG, "^^^^^^^^^^%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + String.valueOf(mViewPager.getCurrentItem()));
        if(mViewPager.getCurrentItem()==0){
            try {
            //if(modActivityTestPageAdapter.getCurrentFragment() instanceof WriteDataFragment ) {  //hardcoded
                WriteDataFragment frag = (WriteDataFragment) modActivityTestPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
                frag.updateConnectionState(resourceId);

            }catch(Exception e){

            }
            //}
        }
        else if(mViewPager.getCurrentItem()==1){
            //else if(modActivityTestPageAdapter.getCurrentFragment() instanceof ReadDataFragment ) { //hardcoded
                LOGD(LOGGER_ENABLE, TAG, "READING UPDATE " + String.valueOf(mViewPager.getCurrentItem()));
            //}

        }
    }

    private void displayData(String data) {
        if(mViewPager.getCurrentItem()==0){
            try {
                WriteDataFragment frag = (WriteDataFragment) modActivityTestPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
                frag.displayData(data);
            }
            catch(Exception e){
                //not alwaysavailable
            }
        }
        else if(mViewPager.getCurrentItem()==1){

        }
    }

    private void displayDeviceNameAndAddress( String deviceAddress,String deviceName) {
        if(mViewPager.getCurrentItem()==0){
            try {
                WriteDataFragment frag = (WriteDataFragment) modActivityTestPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
                frag.displayDeviceNameAndAddress(deviceAddress, deviceName);
            }
            catch(Exception e){
                //not alwaysavailable
            }
        }
    }


    private void clearUI() {
        if(mViewPager.getCurrentItem()==0){
            try {
                WriteDataFragment frag = (WriteDataFragment) modActivityTestPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
                frag.clearUI();
            }
            catch(Exception e){
                //not alwaysavailable
            }
        }
        else if(mViewPager.getCurrentItem()==1){

        }
    }


    private void updateModulesList( ArrayList<Integer> availableModuleIds) {
        if(mViewPager.getCurrentItem()==1){
            try {
                ArrayList<ModuleDataModel> modulesCollection = new ArrayList<>();


                ReadDataFragment frag = (ReadDataFragment) modActivityTestPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem());

                for(Integer modId:availableModuleIds){
                    modulesCollection.add(AppEngine.getInstance().getModuleById(modId));
                }
                frag.getDataAdapter().updateModules(modulesCollection);
            }
            catch(Exception e){
                //not alwaysavailable
            }
        }
    }


    public void updateModuleValues(Integer modId, byte[] data){
        if(mViewPager.getCurrentItem()==1) {
            try {
                ArrayList<ModuleDataModel> modulesCollection = new ArrayList<>();
                ReadDataFragment frag = (ReadDataFragment) modActivityTestPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
                frag.getDataAdapter().updateValues(Integer.valueOf(modId), data);
            } catch (Exception e) {
                //not alwaysavailable
            }
        }
    }


    private  boolean isReadOnOffChecked() {
        if(mViewPager.getCurrentItem()==0){
            try {
                WriteDataFragment frag = (WriteDataFragment) modActivityTestPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
                return frag.isReadOnOffChecked();
            } catch (Exception e) {
                //not alwaysavailable
            }
        }
        return false;
    }


    private void setUsedGattServices(List<BluetoothGattService> gattServices) {

        for (BluetoothGattService gattService : gattServices) {
            if (BleDeviceGattAttributes.lookup(gattService.getUuid().toString()) != null) {

                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    if (BleDeviceGattAttributes.lookup(gattCharacteristic.getUuid().toString()) != null) {
                        final int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBleService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBleService.readCharacteristic(gattCharacteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = gattCharacteristic;
                            mBleService.setCharacteristicNotification(
                                    gattCharacteristic, true);
                        }
                    }

                }

            }
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}





