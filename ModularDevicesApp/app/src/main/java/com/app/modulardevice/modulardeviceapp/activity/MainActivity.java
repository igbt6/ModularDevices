package com.app.modulardevice.modulardeviceapp.activity;
/**
 * Created by igbt6 on 03.10.2015.
 */

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.model.ModuleDataModelNew;
import com.app.modulardevice.modulardeviceapp.model.ModuleXmlNewParser;
import com.app.modulardevice.modulardeviceapp.testApp.ModuleActivityTest;
import com.app.modulardevice.modulardeviceapp.utils.Dialogs;
import com.app.modulardevice.modulardeviceapp.OnTaskCompleted;
import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.adapter.BleDeviceAdapter;
import com.app.modulardevice.modulardeviceapp.ble.BleDevice;
import com.app.modulardevice.modulardeviceapp.model.ModuleDataModel;
import com.app.modulardevice.modulardeviceapp.model.ModuleXmlParser;
import com.app.modulardevice.modulardeviceapp.service.BleService;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGI;
import static com.app.modulardevice.modulardeviceapp.utils.Util.checkInternetConnection;


public class MainActivity extends AppCompatActivity  {
    private final static String TAG  = MainActivity.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;
    private static final String DATA_PREF_NAME = "BLE_SHARED_PREF_DATA";
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=1;
    public static final int SCAN_PERIOD = 10000;
    private GridView mDevicesGrid;
    private DrawerLayout mDrawerLayout;
    private IntentFilter mBleIntentFilter;
    private BleService mBleService;
    private Dialog mDialog;
    private Toolbar mToolbar;
    private BleDeviceAdapter adapter;
    private ProgressDialog mProgressDialog;
    private Dialog mAutoConnectDialog;
    private SharedPreferences mSharedData;
    private boolean bleIsSupported = true;
    private ProgressBar scanProgress;
    private TextView noDevicesFoundView;
    private String  mBleDeviceAddress=null;
    private String  mBleDeviceName=null;
    private ArrayList<BleDevice> discoveredBleDevices= new ArrayList<>();
    private static final int REQUEST_ENABLE_BT =1;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BleService.LocalBinder) service).getService();

            if (!mBleService.initialize()) {
                LOGE(LOGGER_ENABLE, TAG, "Unable to initialize Bluetooth");
                finish();
            }
            LOGI(LOGGER_ENABLE, TAG, "ServiceConnection -onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    // Implements receive methods that handle a specific intent actions from
    // mBleService
    private final BroadcastReceiver mBluetoothLeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BleService.ACTION_DEVICE_DISCOVERED.equals(action)) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent
                        .getParcelableExtra(BleService.DISCOVERED_DEVICE);
                int rssi = intent.getIntExtra(BleService.RSSI, 0);
                //byte[] scanRecord = intent.getByteArrayExtra(BleService.SCAN_RECORD); FOR FUTURE USE
                BleDevice newDiscoveredDevice= new BleDevice(bluetoothDevice, rssi);


                boolean isDevice =true;
                for( BleDevice bleDev : discoveredBleDevices){
                    if(bleDev.getAddress().equals(newDiscoveredDevice.getAddress())){
                        isDevice=false;
                    }
                }
                if(isDevice){
                        discoveredBleDevices.add(newDiscoveredDevice);
                        ((BleDeviceAdapter) mDevicesGrid.getAdapter()).notifyDataSetChanged();
                }
            } else if (intent.getAction().equals(BleService.ACTION_STOP_SCAN)) {
                setScanningStatus(discoveredBleDevices.size() > 0);
                setScanningProgress(false);
            } else if (intent.getAction().equals(BleService.ACTION_GATT_CONNECTED)) {
                refreshViewOnUiThread();
                Toast.makeText(MainActivity.this, "CONNECTED to ", Toast.LENGTH_SHORT).show();

            } else if (intent.getAction().equals(BleService.ACTION_GATT_DISCONNECTED)
                    || intent.getAction().equals(BleService.ACTION_READ_REMOTE_RSSI)
                    || intent.getAction().equals(BleService.ACTION_GATT_CONNECTION_STATE_ERROR)) {
                refreshViewOnUiThread();
            }
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if Bluetooth Low Energy technology is supported on device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bleIsSupported = false;

            mDialog = Dialogs.showAlert(this.getText(R.string.app_name), this.getText(R.string.ble_not_supported),
                    this, getText(android.R.string.ok), null, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            mDialog.dismiss();
                            finish();
                        }
                    }, null,R.drawable.ic_alert_not_ok);
            return;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        }
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(
                this,  mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(mToggle);
        mToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Done by Kalicki&Uszko", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        noDevicesFoundView = (TextView) findViewById(R.id.noDevicesFoundLabel);

        scanProgress = (ProgressBar) findViewById(R.id.scanProgress);


        adapter = new BleDeviceAdapter(this, discoveredBleDevices);
        checkBluetoothAdapter();
        configureDeviceGrid();
        loadModulesDescriptionXml(); //TODO check if it was succesful - add DB
        loadModulesDescriptionXmlNew();
        mSharedData= getSharedPreferences(DATA_PREF_NAME,Activity.MODE_PRIVATE);
        mBleDeviceAddress = mSharedData.getString(BleService.DEVICE_ADDRESS, null);
        mBleDeviceName = mSharedData.getString(BleService.DEVICE_NAME, null);
        getAutoConnectPrompt();

    }



    // If Bluetooth is not supported on device, the application is closed
    // in other case method enable Bluetooth
    private void checkBluetoothAdapter() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            bluetoothNotSupported();
        } else if (!bluetoothAdapter.isEnabled()) {
            bluetoothEnable();
        } else {
            connectService();
        }
    }


    private void connectService() {
        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    // Displays dialog with information that phone doesn't support Bluetooth
    private void bluetoothNotSupported() {
        mDialog = Dialogs.showAlert(getText(R.string.app_name), getText(R.string.bluetooth_not_supported), this,
                getText(android.R.string.ok), null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, null,R.drawable.ic_alert_not_ok);
    }
    // Displays dialog and request user to enable Bluetooth
    private void bluetoothEnable() {
        mDialog = Dialogs.showAlert(this.getText(R.string.no_bluetooth_dialog_title_text), this
                        .getText(R.string.no_bluetooth_dialog_text), this, getText(android.R.string.ok),
                getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Intent enableBtIntent = new Intent();
                        enableBtIntent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        MainActivity.this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        MainActivity.this.finish();
                    }
                },R.drawable.ic_alert_alert);
    }

    // Configures grid view for showing discovered devices list
    private void configureDeviceGrid() {
        mDevicesGrid = (GridView) findViewById(R.id.deviceGrid);

        mDevicesGrid.setAdapter(adapter);

        mDevicesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //AppEngine app = AppEngine.getInstance();
                //app.setBledevice(discoveredBleDevices.get(position));
                BleDevice bleDev= discoveredBleDevices.get(position);
                /*mProgressDialog = Dialogs.showProgress(getText(R.string.connecting_title),
                        getText(R.string.connecting), MainActivity.this, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mProgressDialog.dismiss();
                                // TODO restartBluetooth();
                            }
                        });
                */
                startAllAvailableModulesActivity(bleDev.getAddress());
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled() && mDialog != null) {
                mDialog.show(); //still BLE not enabled
            } else {
                connectService();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bleIsSupported) {
            registerReceiver(mBluetoothLeReceiver, getGattUpdateIntentFilter());
            if (mBleService != null) {
                setScanningProgress(mBleService.isScanning());
            }
            ((BleDeviceAdapter) mDevicesGrid.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bleIsSupported) {
            unregisterReceiver(mBluetoothLeReceiver);
        }
        savePreferenceData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBleService != null) {
            mBleService.close();
        }
        if (bleIsSupported && mBleService != null) {
            unbindService(mServiceConnection);
        }
        mBleService = null;
    }

    // Displays scanning status in UI and starts scanning for new BLE devices
    private void startScanning() {
        if(mBleService.isScanning())
            return;
        setScanningProgress(true);
        setScanningStatus(true);
        // Connected devices are not deleted from list
        Iterator<BleDevice> device = discoveredBleDevices.iterator();
        while (device.hasNext()) {
            BleDevice bleDevice= device.next();
            if(!bleDevice.isConnected())
                discoveredBleDevices.remove(bleDevice);
        }
        // Starts a scan for Bluetooth LE devices for SCAN_PERIOD miliseconds
        mBleService.startScanning(SCAN_PERIOD);
        registerReceiver(mBluetoothLeReceiver, getGattUpdateIntentFilter());

    }

    private void setScanningStatus(boolean foundDevices) {
        if (foundDevices) {
            noDevicesFoundView.setVisibility(View.GONE);
        } else {
            noDevicesFoundView.setVisibility(View.VISIBLE);
        }
    }

    private void setScanningProgress(boolean isScanning) {
        if (isScanning) {
            scanProgress.setVisibility(View.VISIBLE);
            mToolbar.setSubtitle(getText(R.string.scanning));
        } else {
            scanProgress.setVisibility(View.GONE);
            mToolbar.setSubtitle(getText(R.string.not_scanning));
        }
    }
    private void startAllAvailableModulesActivity(String bleAddr){

        Intent intent = new Intent(MainActivity.this, AllModulesActivity.class);
        intent.putExtra(BleService.DEVICE_ADDRESS,bleAddr);
        startActivity(intent);
    }


    private void storeDescriptionFile(String modulesDescriptionFile) {
        FileOutputStream fOut = null;
        try {
            fOut = openFileOutput(ModuleXmlParser.mFileNameString,MODE_PRIVATE);
            if (fOut != null) {
                try {
                    fOut.write(modulesDescriptionFile.getBytes());
                    fOut.close();
                } catch (IOException e) {
                    LOGE(LOGGER_ENABLE, TAG, "storeDescriptionFile file write EXCEPTION", e);
                }
            }

        } catch (FileNotFoundException e) {
            LOGE(LOGGER_ENABLE, TAG, "storeDescriptionFile EXCEPTION", e);
        }
    }

    private boolean loadModulesDescriptionXml(){
        final ModuleXmlParser modParser = new ModuleXmlParser();
            if (checkInternetConnection(MainActivity.this)) {
                try {
                    modParser.loadXmlFileFromNetworkAsync(ModuleXmlParser.mUrlString, new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(Object obj) {
                            storeDescriptionFile(modParser.getModuleDescriptionXmlAsString());
                            ArrayList<ModuleDataModel> xmlDataParsed = (ArrayList<ModuleDataModel>) obj;
                            for (ModuleDataModel mod : xmlDataParsed)
                                LOGI(LOGGER_ENABLE, TAG+"---FROM_INTERNET", mod.toString());
                            AppEngine.getInstance().setAllModulesDescription(xmlDataParsed);
                        }
                    });
                } catch (IOException e) {
                    LOGE(LOGGER_ENABLE, TAG, "loadModulesDescriptionFromNetwork- XmlEXCEPTION", e);
                    return false;
                }
            }
            else {
                try {
                    ArrayList<ModuleDataModel> xmlDataParsed= modParser.loadXmlFileFromMemory(ModuleXmlParser.mFileNameString,this);
                    if(xmlDataParsed!=null) {
                        for (ModuleDataModel mod : xmlDataParsed)
                            LOGI(LOGGER_ENABLE, TAG+"---FROM_MEMORY ", mod.toString());
                        AppEngine.getInstance().setAllModulesDescription(xmlDataParsed);
                    }
                }
                catch (IOException e) {
                    LOGE(LOGGER_ENABLE, TAG, "loadModulesDescriptionFromFile- XmlEXCEPTION", e);
                    return false;
                }
            }
            return true;
        }

    private boolean loadModulesDescriptionXmlNew(){
        final ModuleXmlNewParser modParser = new ModuleXmlNewParser();
        if (checkInternetConnection(MainActivity.this)) {
            try {
                modParser.loadXmlFileFromNetworkAsync(ModuleXmlNewParser.mUrlString, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(Object obj) {
                        storeDescriptionFile(modParser.getModuleDescriptionXmlAsString());
                        ArrayList<ModuleDataModelNew> xmlDataParsed = (ArrayList<ModuleDataModelNew>) obj;
                        LOGI(LOGGER_ENABLE, TAG,"--------++NEW_XML_DATA_MODEL++------------------");
                        for (ModuleDataModelNew mod : xmlDataParsed)
                            LOGI(LOGGER_ENABLE, TAG+"---FROM_INTERNET", mod.toString());
                        //AppEngine.getInstance().setAllModulesDescription(xmlDataParsed);
                    }
                });
            } catch (IOException e) {
                LOGE(LOGGER_ENABLE, TAG, "loadModulesDescriptionNewFromNetwork- XmlEXCEPTION", e);
                return false;
            }
        }
        return true;
    }

    private void savePreferenceData() {
        String bleAddr = null;
        String bleName = null;
        try {
            bleAddr= AppEngine.getInstance().getBleDevice().getAddress();
            bleName= AppEngine.getInstance().getBleDevice().getName();
            if(bleAddr!=null&&bleAddr.length()>0) {
                SharedPreferences.Editor editor= mSharedData.edit();
                editor.putString(BleService.DEVICE_ADDRESS,bleAddr);
                editor.putString(BleService.DEVICE_NAME,bleName);
                editor.commit();
                LOGI(LOGGER_ENABLE, TAG, "!!!!!!!!!!!! savePreferenceData!!!!!!!!!!!! " + bleAddr + " "+bleName);
            }
        }
        catch ( Exception e){

        }
    }
    private void getAutoConnectPrompt() {

        if (mBleDeviceAddress != null && mBleDeviceName != null) {
            LOGI(LOGGER_ENABLE, TAG, "Connect to the last used device");
            mAutoConnectDialog=Dialogs.showAlert("Connect to last used device", "Wanna connect to last used device: " + mBleDeviceName+  "?",
                    this, "YES", "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startAllAvailableModulesActivity(mBleDeviceAddress);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing
                        }
                    },0);
            mAutoConnectDialog.show();
        }
    }


    // Returns intent filter for receiving specific action from
    // mBleService that are handled by mBluetoothLeReceiver
    private IntentFilter getGattUpdateIntentFilter() {
        if (mBleIntentFilter == null) {
            mBleIntentFilter = new IntentFilter();
            mBleIntentFilter.addAction(BleService.ACTION_DEVICE_DISCOVERED);
            mBleIntentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
            mBleIntentFilter.addAction(BleService.ACTION_STOP_SCAN);
            mBleIntentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
            mBleIntentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
            mBleIntentFilter.addAction(BleService.ACTION_READ_REMOTE_RSSI);
            mBleIntentFilter.addAction(BleService.ACTION_GATT_CONNECTION_STATE_ERROR);
        }
        return mBleIntentFilter;
    }

    private void refreshViewOnUiThread() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                ((BleDeviceAdapter) mDevicesGrid.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Handle navigation view item clicks here.
                        int id = menuItem.getItemId();

                        if (id == R.id.nav_home) {
                        } else if (id == R.id.nav_control) {

                        }
                        //so on TODO
                        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        //drawer.closeDrawer(GravityCompat.START);
                        // return true;

                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {

        }
        else if (id == R.id.action_test_activity) {
            Intent intent = new Intent(this,ModuleActivityTest.class);
            intent.putExtra(BleService.DEVICE_ADDRESS, mBleDeviceAddress);
            startActivity(intent);
        }
        else if (id ==R.id.action_search) {
            startScanning();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }


}