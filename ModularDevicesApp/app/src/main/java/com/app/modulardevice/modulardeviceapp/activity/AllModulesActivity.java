package com.app.modulardevice.modulardeviceapp.activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.adapter.AllModulesAdapter;
import com.app.modulardevice.modulardeviceapp.ble.BleDeviceGattAttributes;
import com.app.modulardevice.modulardeviceapp.model.ModuleDataModel;
import com.app.modulardevice.modulardeviceapp.service.BleService;
import com.app.modulardevice.modulardeviceapp.utils.Dialogs;

import java.util.ArrayList;
import java.util.List;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGD;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGI;

/**
 * Created by igbt6 on 18.10.2015.
 */
public class AllModulesActivity extends AppCompatActivity{

    private static final String TAG  = AllModulesActivity.class.getSimpleName();
    private static final boolean LOGGER_ENABLE = true;
    private static final long WAIT_FOR_ALL_DEVICES_PERIOD = 15000;
    private Toolbar mToolbar;
    private BleService mBleService;
    private AllModulesAdapter mAllModulesAdapter;
    private GridView mGridview;
    private String mDeviceAddress;
    private TextView mConnectionState;
    private TextView mWaitForAllModulesTextView;
    private ProgressBar mWaitForAllModulesProgressBar;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private Handler mHandler;
    private Context mActivityContext;


    public AllModulesActivity() {
        super();
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BleService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                LOGE(LOGGER_ENABLE, TAG, "Unable to initialize Bluetooth");
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
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                LOGD(LOGGER_ENABLE, TAG, BleService.ACTION_GATT_CONNECTED);
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                LOGD(LOGGER_ENABLE, TAG, BleService.ACTION_GATT_DISCONNECTED);
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                setUsedGattServices(mBleService.getSupportedGattServices());
                LOGD(LOGGER_ENABLE,TAG, BleService.ACTION_GATT_SERVICES_DISCOVERED);
            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                ArrayList<Integer> availableModuleIds=intent.getIntegerArrayListExtra(BleService.READ_MSG_BROADCAST_DATA);

                if((availableModuleIds)!=null){
                    AppEngine app = AppEngine.getInstance();
                    ArrayList<ModuleDataModel> mods =app.getModulesDescription();
                    for(Integer i : availableModuleIds) {
                        app.addAvailableModuleId(i);
                    }
                    for(ModuleDataModel m:  mods){
                        LOGD(LOGGER_ENABLE,TAG,"Module ID "+  m.getId());
                        if (app.getAvailableModulesIds().contains(m.getId())){
                            updateModulesView(m.getId(),m.getName());
                            setWaitForAllModulesStatus(true);
                        }
                    }
                }
                LOGD(LOGGER_ENABLE,TAG, BleService.ACTION_DATA_AVAILABLE);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_modules);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mActivityContext=this;
        setGridView(mActivityContext);
        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(BleService.DEVICE_ADDRESS);
        connectService();
        mWaitForAllModulesTextView= (TextView)findViewById(R.id.wait_for_all_modules_text_view);
        mWaitForAllModulesProgressBar= (ProgressBar)findViewById(R.id.wait_for_all_modules_progress_bar);
        setWaitForAllModulesStatus(false);
    }

    private void connectService() {
        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    private void setGridView(Context c){
        mAllModulesAdapter= new AllModulesAdapter(c);
        mGridview = (GridView) findViewById(R.id.all_devices_gridview);
        mGridview.setAdapter(mAllModulesAdapter);

        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                LOGI(LOGGER_ENABLE, TAG, "Clicked on position result=" + String.valueOf(position) + "ID= " + String.valueOf(id));
                startModuleAtivity((int) id);  //starts same like before
            }
        });
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
        if(mHandler!=null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        LOGI(LOGGER_ENABLE, TAG, "---------On_Destroy invoked---------------");
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

    private void setWaitForAllModulesStatus(final boolean devicesObtained) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( devicesObtained) {
                    mWaitForAllModulesTextView.setVisibility(View.GONE);
                    mWaitForAllModulesProgressBar.setVisibility(View.GONE);
                    mHandler.removeCallbacksAndMessages(null); //remove all
                } else {
                    mWaitForAllModulesTextView.setVisibility(View.VISIBLE);
                    mWaitForAllModulesTextView.setText(R.string.wait_for_all_devices_discovered);
                    mWaitForAllModulesProgressBar.setVisibility(View.VISIBLE);
                    mHandler=new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.removeCallbacksAndMessages(null);
                            noModulesObtained();

                        }
                    },WAIT_FOR_ALL_DEVICES_PERIOD);
                }
            }
        });
    }

    private void noModulesObtained() {
        mHandler.removeCallbacksAndMessages(null); //remove all
        Dialogs.showAlert("No modules have been found", "Check your hardware and come back later",
                this, "OK", "", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, null, 0).show();

    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId); TODO
            }
        });
    }

    private void updateModulesView(final Integer modId, final String modLabelName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAllModulesAdapter.addModule(modId,modLabelName);
                mAllModulesAdapter.notifyDataSetChanged();
            }
        });
    }

    private void startModuleAtivity(int clickedModuleId){

        final Intent intent = new Intent(AllModulesActivity.this, ModuleActivity.class);
        intent.putExtra(BleService.DEVICE_ADDRESS,mDeviceAddress);
        intent.putExtra(AppEngine.MODULE_ID,clickedModuleId);
        startActivity(intent);
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
