package com.app.modulardevice.modulardeviceapp.model;
import com.app.modulardevice.modulardeviceapp.utils.Crc16;

import java.util.ArrayList;


/**
 * Created by igbt6 on 15.11.2015.
 */
public class DataSenderParser implements DataParser  {
    private final static String TAG  = DataSenderParser.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;
    private static final String mRegexHexDataPattern= "^([A-Fa-f0-9]{2})";

     /* sender Frame looks like the following: */
    /*  ADDR of Module + data + 2 bytes of CRC + 4 stop bytes: 0x7c 0x7c 0x7c 0x00. */

    private static final int MAX_ANDROID_BLE_BUFFER_SIZE =20; //length of data frame
    private static final int MAX_FRAME_LENGTH =128; //length of data frame
    private static final byte[] StopBytes= {0x7c, 0x7c, 0x7c, 0x00};
    private static final int NR_OF_STOP_BYTES= StopBytes.length;
    private static final int NR_OF_CRC_BYTES = 2;
    private static final int NR_OF_MODULE_ADDR_LENGTH_BYTES= 1;


    private static final int START_BYTE_SHIFT =0;
    private static final int RAW_DATA_SHIFT =1;

    private byte[] mParsedFullFrame;  //ADDR of Module + data + 2 bytes of CRC + 4 stop bytes: 0x7c 0x7c 0x7c 0x00.

    public DataSenderParser(){
    }

    /**
     * Converts data from String type to int
     * @param strData - String
     * @return converted Data - byte[]
     */
    public byte[] convertStringToRawData(String strData){
        String data = strData.replaceAll("\\s","");
        byte[] byteData=new byte[data.length()/2];
        ArrayList<Integer> decimals= new ArrayList<>();
        if(data!=null && data.length()>0){

            for(int i=1;i<data.length();i=i+2){
                String s= String.valueOf(data.charAt(i-1));
                s+= data.charAt(i);
                if(s.matches(mRegexHexDataPattern)){
                    decimals.add(Integer.parseInt(s, 16));
                }
            }
            byteData=new byte[decimals.size()];
            for(int i=0;i<byteData.length;i++){
                byteData[i]=decimals.get(i).byteValue();
            }
        }
        return byteData;
    }


    /**
     * Adds CRC + stop bytes to frame
     * @param rawData contains AddrOf the device +rawData
     * @return cuccees or not
     */
    @Override
    public boolean parseData(byte[] rawData) {

        if (rawData.length > MAX_FRAME_LENGTH||rawData==null||rawData.length==0) {
            return false;
        }
        mParsedFullFrame= new byte[rawData.length+NR_OF_STOP_BYTES+NR_OF_CRC_BYTES];
        Crc16 crc16 =new Crc16();
        crc16.getChecksum(rawData);
        byte[] crcVal=crc16.convertChecksumToBytes();
        System.arraycopy(rawData,0,mParsedFullFrame,0,rawData.length);
        System.arraycopy(crcVal,0,mParsedFullFrame,rawData.length,crcVal.length);
        System.arraycopy(StopBytes,0,mParsedFullFrame,rawData.length+2,NR_OF_STOP_BYTES);

        return true;
    }


    public byte[] getParsedData(){

        return mParsedFullFrame;
    }

    public int getFullFrameLength(){
        return mParsedFullFrame.length;
    }

}
