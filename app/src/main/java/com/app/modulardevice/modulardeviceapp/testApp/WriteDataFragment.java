package com.app.modulardevice.modulardeviceapp.testApp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.activity.ModuleActivity;
import com.app.modulardevice.modulardeviceapp.model.DataSenderParser;
import com.app.modulardevice.modulardeviceapp.service.BleService;
import com.app.modulardevice.modulardeviceapp.utils.CustomKeyboard;

/**
 * Created by igbt6 on 24.11.2015.
 */
public class WriteDataFragment extends Fragment {
    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mNameField;
    private TextView mDevAddrField;
    private EditText mWriteDataEditText;
    private Button mWriteDataButton;
    private String mDeviceAddress;
    private Activity mHostActivity;
    private Context mContext;

    private String mDeviceName;
    private CheckBox mReadOnOff;
    private BleService mBleService ;
    private CustomKeyboard mHexKeyboard;

    public static WriteDataFragment newInstance() {
        WriteDataFragment writeDataFragment = new WriteDataFragment();
        return writeDataFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mHostActivity=activity;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext =context;
    }

    @Override
    public void onStart() {
        super.onStart();
        mHexKeyboard= new CustomKeyboard(this.getActivity() , R.id.keyboardview, R.xml.hexkeyboard);
        mHexKeyboard.registerEditText(R.id.write_data_edit_text);
        mWriteDataButton = (Button) getActivity().findViewById(R.id.write_data_button);
        mBleService= ModuleActivityTest.mBleService;
        mWriteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHexKeyboard.storeLastUsedValue(mWriteDataEditText.getText().toString());
                DataSenderParser parser = new DataSenderParser();
                if (parser.parseData(parser.convertStringToRawData(mWriteDataEditText.getText().toString()))) {
                    try {
                        mBleService=ModuleActivityTest.mBleService;
                        mBleService.writeCharacteristic(parser.getParsedData());
                    }
                    catch(Exception e){

                    }
                }
            }
        });
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_module_test_write_fragment, container, false);
        mConnectionState = (TextView) view.findViewById(R.id.connection_state);
        mDataField = (TextView) view.findViewById(R.id.data_value);
        mNameField = (TextView) view.findViewById(R.id.device_name);
        mDevAddrField= (TextView) view.findViewById(R.id.device_address);
        mWriteDataEditText = (EditText) view.findViewById(R.id.write_data_edit_text);
        mReadOnOff= (CheckBox)view.findViewById(R.id.read_data_check_box);
        mWriteDataEditText.setFocusableInTouchMode(true);
        return view;
    }


    public void updateConnectionState(final int resourceId) {
      /*  getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
*/
        mConnectionState.setText(resourceId);
    }

    public void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    public void displayDeviceNameAndAddress( String deviceAddress,String deviceName) {
        if (deviceName != null) {
            mNameField.setText(deviceName);
        }
        if (deviceAddress != null) {
            mDevAddrField.setText(deviceAddress);
        }
    }


    public void clearUI() {
        mDevAddrField.setText("--------");
        mDataField.setText(R.string.no_data);
        mNameField.setText(R.string.unknown_device);
    }


    public boolean isReadOnOffChecked() {
        return mReadOnOff.isChecked();
    }

}
