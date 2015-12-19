package com.app.modulardevice.modulardeviceapp.activity;

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
import android.view.View;
import android.widget.Toast;

import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.adapter.MFragPagerAdapter;
import com.app.modulardevice.modulardeviceapp.ble.BleDeviceGattAttributes;
import com.app.modulardevice.modulardeviceapp.fragment.ModulePageFragment;
import com.app.modulardevice.modulardeviceapp.model.ModuleVariable;
import com.app.modulardevice.modulardeviceapp.model.ModuleDataModel;
import com.app.modulardevice.modulardeviceapp.service.BleService;
import com.app.modulardevice.modulardeviceapp.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGD;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGI;

/**
 * Created by igbt6 on 07.11.2015.
 */
public class ModuleActivity extends AppCompatActivity {

    private final static String TAG  = ModuleActivity.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;
    private static String POSITION = "POSITION";
    private ViewPager mViewPager;
    private MFragPagerAdapter mVPageAdapter;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private BleService mBleService;
    private String mDeviceAddress;
    private int mClickedModuleId;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BleService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                LOGE(LOGGER_ENABLE, TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBleService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                LOGD(LOGGER_ENABLE, TAG, BleService.ACTION_GATT_CONNECTED);
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                LOGD(LOGGER_ENABLE,TAG, BleService.ACTION_GATT_DISCONNECTED);
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                setUsedGattServices(mBleService.getSupportedGattServices());
                LOGD(LOGGER_ENABLE,TAG, BleService.ACTION_GATT_SERVICES_DISCOVERED);
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                ArrayList<Integer> availableModuleIds=intent.getIntegerArrayListExtra(BleService.READ_MSG_BROADCAST_DATA);

                int moduleId= intent.getIntExtra(BleService.READ_MSG_DATA_MOD_ID, -1);
                byte[] receivedData = intent.getByteArrayExtra(BleService.READ_MSG_DATA_RAW_DATA);

                if (receivedData != null&&moduleId!=-1) {
                    try {
                        int currentModuleId = mVPageAdapter.getModuleId(mViewPager.getCurrentItem());
                       // ArrayList<ModuleVariable> mVars = mVPageAdapter.getItem(mViewPager.getCurrentItem()).getAllModuleVariables();
                        ArrayList<ModuleVariable> mVars = ((ModulePageFragment)mVPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem())).getAllModuleVariables();
                    if(currentModuleId==moduleId) {
                        for (ModuleVariable mVar : mVars) {
                                Double varValue = mVar.computeEquation(Util.byteArrayToObjects(receivedData));
                                LOGD(LOGGER_ENABLE, TAG, "EQUATION_DONE " + String.valueOf(currentModuleId) + "--- " + mViewPager.getCurrentItem());
                                ((ModulePageFragment)mVPageAdapter.getRegisteredFragment(mViewPager.getCurrentItem())).updateModulevariableValue(mVar.getName(), String.valueOf(String.format("%.1f", varValue)));

                            }
                        }
                    }
                    catch(ArrayIndexOutOfBoundsException aibe){
                        LOGE(LOGGER_ENABLE, TAG, "Problem while computing Equation - index out of array", aibe);
                    }
                    catch(Exception e){
                        LOGE(LOGGER_ENABLE, TAG, "Problem while computing Equation",e);
                    }
                }
                else if((availableModuleIds)!=null){
                    AppEngine app = AppEngine.getInstance();
                    ArrayList<ModuleDataModel> mods =app.getModulesDescription();

                    for(ModuleDataModel m:  mods){
                        LOGD(LOGGER_ENABLE,TAG,"Module ID "+  m.getId());
                        for(Integer i : availableModuleIds) {
                            LOGD(LOGGER_ENABLE,TAG,"Module IDstr "+ String.valueOf(i));
                            if (m.getId().equals(i)){
                                LOGD(LOGGER_ENABLE, TAG, "FOUND SOMETHING WHO KNOW WHAT IT IS");
                               // updateModulesView(m.getId(),m.getName());
                            }
                        }
                    }
                }
                LOGD(LOGGER_ENABLE,TAG, BleService.ACTION_DATA_AVAILABLE);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(BleService.DEVICE_ADDRESS);
        mClickedModuleId = intent.getIntExtra(AppEngine.MODULE_ID, mClickedModuleId);



        mViewPager = (ViewPager) findViewById(R.id.modules_viewpager);
        LOGD(LOGGER_ENABLE, TAG, "^^^^^^^^^^^^^mClickedModuleId"+String.valueOf(mClickedModuleId));
        setupViewPager(mViewPager, mClickedModuleId);

        mTabLayout = (TabLayout) findViewById(R.id.modules_sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        /*
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                //Toast.makeText(getApplicationContext(),"On Tab Selected-"+tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.setCurrentItem(mTabLayout.getSelectedTabPosition());
        */
        mViewPager.setCurrentItem(mViewPager.getCurrentItem());
        mVPageAdapter.notifyDataSetChanged();
        connectService();
    }

    private void setupViewPager(ViewPager vPg,int firstClickedModId){
        AppEngine app = AppEngine.getInstance();
        mVPageAdapter=   new MFragPagerAdapter(getSupportFragmentManager());
        try {
            ArrayList<Integer> modIds = app.getAvailableModulesIds();
            mVPageAdapter.addModuleFragment(ModulePageFragment.newInstance(firstClickedModId), firstClickedModId, app.getModuleById(firstClickedModId).getName());
            for (Integer i : modIds) {
                if (i != mClickedModuleId)
                    mVPageAdapter.addModuleFragment(ModulePageFragment.newInstance(i), i, app.getModuleById(i).getName());
            }

        }
        catch (Exception e){
            Toast.makeText(this,"You are not connected or XML is not correct - yu gotta download a newer version from the INTERNET!", Toast.LENGTH_SHORT).show();
            finish();
        }
        vPg.setAdapter(mVPageAdapter);

    }

    private void connectService() {
        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBleService != null) {
            final boolean result = mBleService.connect(mDeviceAddress);
            LOGD(LOGGER_ENABLE, TAG, "Connect request result=" + result);
        }
        LOGI(LOGGER_ENABLE, TAG, "---------On_Resume invoked---------------");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        LOGI(LOGGER_ENABLE, TAG, "---------On_Pause invoked---------------");
    }
    @Override
    protected void onStop() {
        super.onStop();
        LOGI(LOGGER_ENABLE, TAG, "---------On_Stop invoked---------------");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBleService = null;
        LOGI(LOGGER_ENABLE, TAG, "---------On_Destroy invoked---------------");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, mTabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mViewPager.setCurrentItem(savedInstanceState.getInt(POSITION));
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
