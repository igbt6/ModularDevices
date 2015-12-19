package com.app.modulardevice.modulardeviceapp.model;


import com.app.modulardevice.modulardeviceapp.utils.Crc16;
import com.app.modulardevice.modulardeviceapp.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igbt6 on 27.10.2015.
 */
public class DataReceiverParser implements DataParser {


    /* received Frame looks like the following: */
    /*  | + data.length + data + 2 bytes of CRC  */
    /* MSB of data length field describes type of frame: 0- Broadcast Fram , 1- data frame*/

    private static final int MAX_ANDROID_BLE_BUFFER_SIZE =20; //length of data frame
    private static final int MAX_FRAME_LENGTH =128; //length of data frame
    private static final int MODULE_HEADER_FIELD_LENGTH = 4 ; //nrOfBytes
    private static final int MODULE_ID_FIELD_LENGTH = 3;
    private static final int MODULE_ADDR_FIELD_LENGTH = 1;

    private static final byte START_BYTE = '|' ;
    private static final int NR_OF_START_BYTES= 1;
    private static final int NR_OF_CRC_BYTES = 2;
    private static final int NR_OF_DATA_LENGTH_BYTES= 1;

    private static final int START_BYTE_SHIFT =0;
    private static final int DATA_LENGTH_BYTE_SHIFT =1;
    private static final int RAW_DATA_SHIFT =DATA_LENGTH_BYTE_SHIFT+NR_OF_DATA_LENGTH_BYTES;
    public enum TYPE_OF_FRAME{

        MSG_BROADCAST,
        MSG_DATA,
        MSG_UNKNOWN
    }


    public enum TYPE_OF_PARSING_ERROR{
        OK,
        NO_START_BYTE,
        INCORRECT_FRAME,
        LONG_FRAME,
        MAX_FRAME_LENGTH_EXCEEDED,
        CRC_INVALID
    }



    private List<String> mAllModulesIds; //modules received from xml earlier

    private byte[] mParsedRawData;  //removed length, crc, type of frame
    private TYPE_OF_FRAME mTypeOfFrame=TYPE_OF_FRAME.MSG_UNKNOWN;
    private int mLengthOfFrame;

    public DataReceiverParser(List<String> modulesIds){   //For future use

        mAllModulesIds= modulesIds;

    }

    public DataReceiverParser(){


    }

    /**
     * Parses raw data stream checking if frame was correct or not
     * @param receivedData
     * @return  true when frame correct , false when not
     * NOTE: DO NOT USE FOR BUFFERS LONGER THAN 20 BYTES , You can use this of course just remember to check  if frame Length> 20 after getting false response
     */
    @Override
    public boolean parseData(byte[] receivedData){

        boolean resp =false;
        if(receivedData.length>DATA_LENGTH_BYTE_SHIFT+1)
            mLengthOfFrame = (int)receivedData[DATA_LENGTH_BYTE_SHIFT]&0x7F;
        if(receivedData[START_BYTE_SHIFT]!=START_BYTE)
            return  false;
        if ((mLengthOfFrame > MAX_ANDROID_BLE_BUFFER_SIZE - NR_OF_CRC_BYTES - NR_OF_DATA_LENGTH_BYTES - NR_OF_START_BYTES)&&(getFullFrameLength()!=receivedData.length)) {
            return false;
        }
        if((receivedData.length-mLengthOfFrame - NR_OF_CRC_BYTES-NR_OF_DATA_LENGTH_BYTES-1)!=0)
            return  false;
        if(receivedData.length>MAX_FRAME_LENGTH)
            return false;
        int crc16= extractCrc16FromByteArray(new byte[]{receivedData[receivedData.length - 2], receivedData[receivedData.length - 1]});

        mParsedRawData = new byte[mLengthOfFrame];
        for(int i =0;i<mLengthOfFrame;i++){
            mParsedRawData[i]=receivedData[NR_OF_START_BYTES+NR_OF_DATA_LENGTH_BYTES+i];
        }
        if(isFrameValid(mParsedRawData,crc16)){
            if((receivedData[DATA_LENGTH_BYTE_SHIFT]&0x80)>0) //MSB =1- dataMSG
                mTypeOfFrame= TYPE_OF_FRAME.MSG_DATA;
            else
                mTypeOfFrame= TYPE_OF_FRAME.MSG_BROADCAST;
            resp = true;
        }
        return resp;
    }

    /**
     * Parses raw data stream and returns type of parsing error
     * @param receivedData
     * @return type pf error that occured while parsing process
     */

    public TYPE_OF_PARSING_ERROR parseDataError(byte[] receivedData){

        TYPE_OF_PARSING_ERROR resp =TYPE_OF_PARSING_ERROR.OK;
        if(receivedData.length==0 || receivedData==null )
            return  TYPE_OF_PARSING_ERROR.INCORRECT_FRAME;
        if(receivedData.length>DATA_LENGTH_BYTE_SHIFT+1)
            mLengthOfFrame = (int)receivedData[DATA_LENGTH_BYTE_SHIFT]&0x7F;
        if(receivedData[START_BYTE_SHIFT]!=START_BYTE)
            return  TYPE_OF_PARSING_ERROR.NO_START_BYTE;

        if ((mLengthOfFrame > (MAX_ANDROID_BLE_BUFFER_SIZE - NR_OF_CRC_BYTES - NR_OF_DATA_LENGTH_BYTES - NR_OF_START_BYTES))&&(getFullFrameLength()!=receivedData.length)) {
            return TYPE_OF_PARSING_ERROR.LONG_FRAME;
        }
        if(receivedData.length<getFullFrameLength())
            return  TYPE_OF_PARSING_ERROR.INCORRECT_FRAME;
        if(receivedData.length>MAX_FRAME_LENGTH)
            return TYPE_OF_PARSING_ERROR.MAX_FRAME_LENGTH_EXCEEDED;
        int crc16= extractCrc16FromByteArray(new byte[]{receivedData[getFullFrameLength()- 2], receivedData[getFullFrameLength() - 1]});

        mParsedRawData = new byte[mLengthOfFrame];
        for(int i =0;i<mLengthOfFrame;i++){
            mParsedRawData[i]=receivedData[NR_OF_START_BYTES+NR_OF_DATA_LENGTH_BYTES+i];
        }

        if(isFrameValid(mParsedRawData,crc16)){
            if((receivedData[DATA_LENGTH_BYTE_SHIFT]&0x80)>0) //MSB =1- dataMSG
                mTypeOfFrame= TYPE_OF_FRAME.MSG_DATA;
            else
                mTypeOfFrame= TYPE_OF_FRAME.MSG_BROADCAST;
        }
        else{
            resp = TYPE_OF_PARSING_ERROR.CRC_INVALID;
        }
        return resp;
    }

    /**
     * gets start Byte index in frame
     * @param frameData
     * @return index of start byte in frame , -1 lack of its
     */

    public int getStartByteFrameIndex(byte[] frameData){
        int index =-1;
        for(int i=0;i<frameData.length;i++){
            if(frameData[i]==START_BYTE){
                index=i;
                break;
            }
        }
        return index;
    }


    /**
     * returns all available modules IDs received broadcast message
     * @param rawData earlier correctly parsed rawbyte[]
     * @return all available module IDs
     */

    public ArrayList<Integer> getAvailableModules(byte[] rawData){
        ArrayList<Integer> modIds= new ArrayList<>();
        for(int i=0;i<rawData.length;i+=MODULE_HEADER_FIELD_LENGTH){
                int modId= convertByteArrayToInt(rawData, i, MODULE_ID_FIELD_LENGTH);
               // if(isGivenModuleIdSupported(modId)) { //TODO
                    modIds.add(Integer.valueOf(modId));
              //  }
        }
        return modIds;
    }

    public Pair<Integer,Integer> getModuleInfo(byte[] rawData){
        Pair<Integer,Integer> modInfo= new Pair<>();
        int modId= convertByteArrayToInt(rawData, 0, MODULE_ID_FIELD_LENGTH);
        int modAddr = convertByteArrayToInt(rawData, MODULE_ID_FIELD_LENGTH, MODULE_ADDR_FIELD_LENGTH);
        modInfo.setFirst(Integer.valueOf(modId));
        modInfo.setSecond(Integer.valueOf(modAddr));

        return modInfo;
    }

    public byte[] getParsedData(){

        return mParsedRawData;
    }

    public int getFrameLength(){
        return mLengthOfFrame;
    }

    public int getFullFrameLength(){
        return mLengthOfFrame+NR_OF_START_BYTES+NR_OF_CRC_BYTES+NR_OF_DATA_LENGTH_BYTES;
    }


    //TODO removing unused modules
    private boolean isGivenModuleIdSupported(int modId){

        if(mAllModulesIds.contains(String.valueOf(modId)))
            return true;
        else
            return false;
    }

    private int convertByteArrayToInt(byte[] byteArray,int dataShift, int lengthOfData) {
        final StringBuilder strB = new StringBuilder(byteArray.length);
        for(int i=0;i<lengthOfData && i+dataShift < byteArray.length;i++){
            strB.append(String.format("%02X", byteArray[i+dataShift]));
        }
        String str= strB.toString();
        //System.out.println("MODULES_IDS -bytes: " +str);
        if(str.length()>0)
            return Integer.parseInt(str, 16);
        else
            return -1;

    }

    private int extractCrc16FromByteArray(byte[] crc16Array) {
        int crc;
        if(crc16Array==null)
            crc= -1;
        else if(crc16Array.length==2){
            crc=(((crc16Array[0]&0xFF)<<8)|(crc16Array[1]&0xFF));
       } else
            crc= -1;
        return crc&0xFFFF;
    }

    private boolean isFrameValid(byte[] receivedData,int receivedCrc){

        Crc16 crc = new Crc16();
        if(crc.getChecksum(receivedData)==receivedCrc)
            return true;
        return false;
    }

    public TYPE_OF_FRAME getTypeOfFrame(){

        return mTypeOfFrame;
    }

    public byte getStartByte(){

        return START_BYTE;
    }

}
