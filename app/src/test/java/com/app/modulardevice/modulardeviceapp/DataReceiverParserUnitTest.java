package com.app.modulardevice.modulardeviceapp;

import com.app.modulardevice.modulardeviceapp.model.DataReceiverParser;
import com.app.modulardevice.modulardeviceapp.utils.CircularBuffer;
import com.app.modulardevice.modulardeviceapp.utils.Crc16;
import com.app.modulardevice.modulardeviceapp.utils.Pair;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGI;
import static com.app.modulardevice.modulardeviceapp.utils.Util.byteArrayToPrimitives;
import static com.app.modulardevice.modulardeviceapp.utils.Util.convertByteArrayToString;
import static org.junit.Assert.assertEquals;

/**
 * Created by igbt6 on 28.10.2015.
 */
public class DataReceiverParserUnitTest {



/*


    @Test //TODO
    public void checkModuleIds() throws Exception {

        DataReceiverParser parser = new DataReceiverParser(modIdsSupported);
        byte[] dataTab = new byte[]{0x42, 0x4C, 0x45, 0x01, 0x45, 0x53, 0x45, 0x06, 0x50, 0x6C, 0x64, 0x1E, 0x4C, 0x42, 0x43, 0x05
                ,0x4C, 0x68, 0x74, 0x02}; //broadcast frame

        Map<Integer,String> modIdsAvailable =  parser.getAvailableModules(dataTab);


        System.out.println(Arrays.toString(modIdsAvailable.entrySet().toArray()));
        assertEquals(10, 9);
    }

*/
   // @Test
    public void parseMsgBroadcastDataFrame() throws Exception {
        System.out.println("\n-----------------parseMsgBroadcastDataFrame------------------------");
        DataReceiverParser parser = new DataReceiverParser();
        byte[] rawDataArr = new byte[]{0x42, 0x4C, 0x45, 0x01, 0x45, 0x53, 0x45, 0x06, 0x50, 0x6C, 0x64, 0x1E, 0x4C, 0x42, 0x43, 0x05, 0x4C, 0x68, 0x74, 0x02}; //data frame

        Crc16 crc = new Crc16();
        crc.getChecksum(rawDataArr);
        byte[] crcValBytes = crc.convertChecksumToBytes();
        byte[] dataArr = new byte[]{'|', (byte)(rawDataArr.length & 0x7F),0x42, 0x4C, 0x45, 0x01, 0x45, 0x53, 0x45, 0x06, 0x50, 0x6C, 0x64, 0x1E, 0x4C, 0x42, 0x43, 0x05, 0x4C, 0x68, 0x74, 0x02, crcValBytes[0], crcValBytes[1]}; //data frame
        System.out.println("PARSER_RESP: " + parser.parseDataError(dataArr).toString());
        if (parser.parseData(dataArr)) {

            if (parser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_DATA) {
                System.out.println("MSG_DATA: " + parser.getParsedData());
            } else if (parser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST) {
                System.out.println("-------MSG_BROADCAST-------: ");
                ArrayList<Integer> availableModules = parser.getAvailableModules(parser.getParsedData());
                for(Integer modIds: availableModules){
                    System.out.println("MODULES: -moduleID: " + modIds);
                }
                for(int i = 0; i<availableModules.size();i++){
                    assertEquals((int)availableModules.get(i), (convertByteArrayToInt(parser.getParsedData(), 4*i, 3)));

                }
            } else {

            }
        } else
            assertEquals(true, parser.parseData(dataArr));
    }


   // @Test
    public void parseLongMsgBroadcastDataFrame() throws Exception {
        System.out.println("\n-----------------parseLongMsgBroadcastDataFrame------------------------");
        DataReceiverParser rParser = new DataReceiverParser();
        int mLongFrameLength=0;
        ArrayList<Byte> mLongFrameBuffer = new ArrayList<>();
        byte[] rawDataArr = new byte[]{0x42, 0x4C, 0x45, 0x01, 0x45, 0x53, 0x45, 0x06, 0x50, 0x6C, 0x64, 0x1E, 0x4C, 0x42, 0x43, 0x05, 0x4C, 0x68, 0x74, 0x02}; //data frame
        Crc16 crc = new Crc16();
        crc.getChecksum(rawDataArr);
        byte[] crcValBytes = crc.convertChecksumToBytes();
        byte[] dataArr = new byte[]{'|', (byte)(rawDataArr.length & 0x7F),0x42, 0x4C, 0x45, 0x01, 0x45, 0x53, 0x45, 0x06, 0x50, 0x6C, 0x64, 0x1E, 0x4C, 0x42, 0x43, 0x05, 0x4C, 0x68, 0x74, 0x02, crcValBytes[0], crcValBytes[1]}; //data frame
        System.out.println("NEGATIVE_PARSER_RESP: " + rParser.parseDataError(dataArr).toString());


        int nrOfFrames= dataArr.length/20;
        int lengthIdx= 0;
        //creates subFrames not bigger than 20 bytes - simulates Android BLE Buffer
        for(int x=0;x<nrOfFrames+1;x++) {
            int z=0;
            int i =0;
            for (i=lengthIdx,z=0;z<20&&i<dataArr.length;i++,z++) {
            }
            lengthIdx+=z;
            byte[] data= new byte[z];

            for (int j=0;j<data.length;j++) {
                data[j] =dataArr[lengthIdx-z+j];
            }
/*
            for(byte b:dataArr){
                System.out.println("ByteArr: "+ String.valueOf(b));
            }
            for(byte b:data){
                System.out.println("Byte: "+ String.valueOf(b));
            }
*/
            if (data != null && data.length > 0) {
                if (mLongFrameLength == 0) {
                    if (rParser.parseDataError(data) == DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME) {
                        System.out.println("DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME");
                        mLongFrameLength = rParser.getFullFrameLength();
                        fillBuffer(mLongFrameBuffer, data, -1);
                    }
                } else {
                    fillBuffer(mLongFrameBuffer, data, mLongFrameLength - mLongFrameBuffer.size());
                    if ((mLongFrameLength) == mLongFrameBuffer.size()) {
                        System.out.println("LONG_FRAME: " + mLongFrameBuffer.toString());
                        if (rParser.parseDataError(byteListToPrimitives(mLongFrameBuffer)/*,true*/) == DataReceiverParser.TYPE_OF_PARSING_ERROR.OK) {
                            if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_DATA) {
                                System.out.println("MSG_LONG_DATA: " + rParser.getParsedData());
                            } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST) {
                                System.out.println("-------MSG_LONG_BROADCAST-------: ");
                                ArrayList<Integer> availableModules = rParser.getAvailableModules(rParser.getParsedData());
                                for (Integer modIds : availableModules) {
                                    System.out.println("MODULES: -moduleID: " + modIds);
                                }
                                for (int j = 0; i < availableModules.size(); j++) {
                                    assertEquals((int) availableModules.get(j), (convertByteArrayToInt(rParser.getParsedData(), 4 * j, 3)));
                                }
                            } else if(rParser.getTypeOfFrame()== DataReceiverParser.TYPE_OF_FRAME.MSG_UNKNOWN){
                                System.out.println("-------MSG_LONG_UNKNOWN-------");
                            }
                        } else {
                            System.out.println("PARSER_RESP_NOT_OK: " + rParser.parseDataError(byteListToPrimitives(mLongFrameBuffer)).toString());
                        }
                        mLongFrameLength = 0;
                        mLongFrameBuffer.clear();
                    }
                }
            }
        }
        assertEquals(true, rParser.parseData(dataArr));
    }

  //  @Test
    public void parseMsgDataFrame() throws Exception {
        System.out.println("\n-----------------parseMsgDataFrame------------------------");
        DataReceiverParser parser = new DataReceiverParser();
        byte[] rawDataArr = new byte[]{0x45, 0x53, 0x45, 0x06, 0x24, 0x23}; //data frame

        Crc16 crc = new Crc16();
        crc.getChecksum(rawDataArr);
        byte[] crcValBytes= crc.convertChecksumToBytes();
        byte[] dataArr = new byte[]{'|',(byte)(rawDataArr.length|0x80),0x45, 0x53, 0x45, 0x06, 0x24, 0x23,crcValBytes[0],crcValBytes[1]}; //data frame
        System.out.println("PARSER_RESP: " + parser.parseDataError(dataArr).toString());
        if(parser.parseData(dataArr)){

            if(parser.getTypeOfFrame()== DataReceiverParser.TYPE_OF_FRAME.MSG_DATA){
                System.out.println("-------MSG_DATA-------: "+parser.getParsedData());
                Pair<Integer,Integer> modInfo = parser.getModuleInfo(parser.getParsedData());
                System.out.println("MODULE_INFO: -moduleID: "+ modInfo.getFirst() +" -moduleADDR: "+ modInfo.getSecond());
                assertEquals((int)modInfo.getFirst(), (convertByteArrayToInt(parser.getParsedData(),0,3)));
            }
            else if(parser.getTypeOfFrame()== DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST){
                System.out.println("MSG_BROADCAST: "+parser.getParsedData());
            }
            else{

            }
        }
        else
            assertEquals( true, parser.parseData(dataArr));
    }


   // @Test
    public void parseLongMsgDataFrame() throws Exception {
        System.out.println("\n-----------------parseLongMsgDataFrame------------------------");
        DataReceiverParser rParser = new DataReceiverParser();
        int mLongFrameLength=0;
        ArrayList<Byte> mLongFrameBuffer = new ArrayList<>();
        byte[] rawDataArr = new byte[]{ 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA, 0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00, 0x00, 0x00, 0x00}; //data frame
        Crc16 crc = new Crc16();
        crc.getChecksum(rawDataArr);
        byte[] crcValBytes = crc.convertChecksumToBytes();
        byte[] dataArr = new byte[]{'|', (byte)(rawDataArr.length | 0x80), 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA, 0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00, 0x00, 0x00, 0x00, crcValBytes[0], crcValBytes[1]}; //data frame


        int nrOfFrames= dataArr.length/20;
        int lengthIdx= 0;
        //creates subFrames not bigger than 20 bytes - simulates Android BLE Buffer
        for(int x=0;x<nrOfFrames+1;x++) {
            int z=0;
            int i =0;
            for (i=lengthIdx,z=0;z<20&&i<dataArr.length;i++,z++) {
            }
            lengthIdx+=z;
            byte[] data= new byte[z];

            for (int j=0;j<data.length;j++) {
                data[j] =dataArr[lengthIdx-z+j];
            }
            if (data != null && data.length > 0) {
                if (mLongFrameLength == 0) {
                    if (rParser.parseDataError(data) == DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME) {
                        System.out.println("DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME");
                        mLongFrameLength = rParser.getFullFrameLength();

                        fillBuffer(mLongFrameBuffer, data, -1);

                    }
                } else {
                    int leftDataLength;
                    if((data.length+mLongFrameBuffer.size())< mLongFrameLength)
                        leftDataLength=data.length;
                    else{
                        leftDataLength=mLongFrameLength-mLongFrameBuffer.size();
                    }
                    fillBuffer(mLongFrameBuffer,data,leftDataLength);
                    System.out.println(mLongFrameBuffer.toString());
                    if ((mLongFrameLength) <= mLongFrameBuffer.size()) {
                        if (rParser.parseDataError(byteListToPrimitives(mLongFrameBuffer)) == DataReceiverParser.TYPE_OF_PARSING_ERROR.OK) {
                            if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_DATA) {
                                System.out.println("-------MSG_LONG_DATA-------");
                                assertEquals(true,true);

                            } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST) {
                                System.out.println("-------MSG_LONG_BROADCAST-------: ");
                                assertEquals(true, false);
                            } else if(rParser.getTypeOfFrame()== DataReceiverParser.TYPE_OF_FRAME.MSG_UNKNOWN){
                                System.out.println("-------MSG_LONG_UNKNOWN-------");
                                assertEquals(true, false);
                            }
                        } else {
                            System.out.println("PARSER_RESP_NOT_OK: " + rParser.parseDataError(byteListToPrimitives(mLongFrameBuffer)).toString());
                        }
                        mLongFrameLength = 0;
                        mLongFrameBuffer.clear();
                    }
                }
            }
        }
    }



   // @Test
    public void parseLongMsgDataFrameCircularBuffer() throws Exception {
        System.out.println("\n-----------------parseLongMsgDataFrameCircularBuffer------------------------");
        DataReceiverParser rParser = new DataReceiverParser();
        int mLongFrameLength=0;
        CircularBuffer mFrameBuffer = new CircularBuffer(128);
        byte[] rawDataArr = new byte[]{ 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA, 0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00, 0x00, 0x00, 0x00}; //data frame
        Crc16 crc = new Crc16();
        crc.getChecksum(rawDataArr);
        byte[] crcValBytes = crc.convertChecksumToBytes();
        byte[] dataArr = new byte[]{
                                    0x01,0x01,0x04,'|', (byte)(rawDataArr.length | 0x80), 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //1st frame
                                    0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA,
                                    0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00,
                                    0x00, 0x00, 0x00, crcValBytes[0], crcValBytes[1],
                                    0x01,0x01,0x04,'|', (byte)(rawDataArr.length | 0x80), 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //2nd frame
                                    0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA,
                                    0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00,
                                    0x00, 0x00, 0x00, crcValBytes[0], crcValBytes[1],
                                    0x01,0x01,'|','|', (byte)(rawDataArr.length | 0x80), 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //3rd frame
                                    0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA,
                                    0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00,
                                    0x00, 0x00, 0x00, crcValBytes[0], crcValBytes[1],}; //data frame



        int nrOfFrames= dataArr.length/20;
        int lengthIdx= 0;
        //creates subFrames not bigger than 20 bytes - simulates Android BLE Buffer
        for(int x=0;x<nrOfFrames+1;x++) {
            int z;
            int i;
            for (i=lengthIdx,z=0;z<20&&i<dataArr.length;i++,z++) {
            }
            lengthIdx+=z;
            byte[] data= new byte[z];

            for (int j=0;j<data.length;j++) {
                data[j] =dataArr[lengthIdx-z+j];
            }
            if (data != null && data.length > 0) {
                for(byte b:data){
                    mFrameBuffer.insert(b);
                }
                System.out.println("RAW_DATA: " + convertByteArrayToString(mFrameBuffer.peekBuf()));
                int idxOfStart= mFrameBuffer.getIndexOfFirstAppearance(rParser.getStartByte());
                System.out.println("\nIDX_OF_START: " + idxOfStart);
                if( idxOfStart!=-1&&mLongFrameLength==0){

                    System.out.println("PART_OF_BUF_REMOVED: " + convertByteArrayToString(mFrameBuffer.removeBuf(idxOfStart)));
                }
                if(mLongFrameLength==0||mLongFrameLength<=mFrameBuffer.getSize()) {

                    byte[] frame = mLongFrameLength>0?byteArrayToPrimitives(mFrameBuffer.peekBuf(mLongFrameLength)):byteArrayToPrimitives(mFrameBuffer.peekBuf());
                    System.out.println("RAW_DATA_FRAME: "+convertByteArrayToString(frame));
                    if (rParser.parseDataError(frame) == DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME) {
                        System.out.println("DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME");
                        mLongFrameLength = rParser.getFullFrameLength();
                    }
                    else if (rParser.parseDataError(frame) == DataReceiverParser.TYPE_OF_PARSING_ERROR.OK) {
                        System.out.println("DataReceiverParser.TYPE_OF_PARSING_ERROR.OK");
                        if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_DATA) {
                            System.out.println("-------MSG_DATA-------");
                            assertEquals(true, true);

                        } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST) {
                            System.out.println("-------MSG_BROADCAST-------: ");
                            assertEquals(true, false);
                        } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_UNKNOWN) {
                            System.out.println("-------MSG_UNKNOWN-------");
                            assertEquals(true, false);
                        }
                        mLongFrameLength = 0;
                        mFrameBuffer.removeBuf(rParser.getFullFrameLength());
                    } else {
                        System.out.println("PARSER_RESP_NOT_OK: " + rParser.parseDataError(frame).toString());
                        mFrameBuffer.removeBuf(mLongFrameLength);
                        mLongFrameLength = 0;
                    }
                }
            }
        }
    }


    @Test
    public void parseMixedLengthMsgDataFrameCircularBuffer() throws Exception {
        System.out.println("\n-----------------parseMixedLengthMsgDataFrameCircularBuffer------------------------");
        DataReceiverParser rParser = new DataReceiverParser();
        int mLongFrameLength=0;
        Crc16 crc = new Crc16();
        CircularBuffer mFrameBuffer = new CircularBuffer(128);

        byte[] rawDataArrShort = new byte[]{0x45, 0x53, 0x45, 0x06, 0x24, 0x23}; //data frame
        crc.getChecksum(rawDataArrShort);
        byte[] crcValBytesShort= crc.convertChecksumToBytes();
        byte[] dataArrShort = new byte[]{'|',(byte)(rawDataArrShort.length|0x80),0x45, 0x53, 0x45, 0x06, 0x24, 0x23,crcValBytesShort[0],crcValBytesShort[1]}; //data frame


        byte[] rawDataArr = new byte[]{ 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA, 0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00, 0x00, 0x00, 0x00}; //data frame
        crc.getChecksum(rawDataArr);
        byte[] crcValBytes = crc.convertChecksumToBytes();
        byte[] dataArr = new byte[]{
                0x01,0x04,'|',(byte)(rawDataArrShort.length|0x80),0x45, 0x53, 0x45, 0x06, 0x24, 0x23,crcValBytesShort[0],crcValBytesShort[1],     //1st frame
                0x01,0x01,0x04,'|', (byte)(rawDataArr.length | 0x80), 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //2nd frame
                0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA,
                0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00,
                0x00, 0x00, 0x00, crcValBytes[0], crcValBytes[1],
                0x01,0x01,0x04,'|', (byte)(rawDataArr.length | 0x80), 0x45, 0x53, 0x4E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //3rd frame
                0x00, 0x16, 0x02, 0x63, 0x13, (byte)0xEF, (byte)0xFF, (byte)0xA5, 0x00, (byte)0x84, (byte)0xFE, 0x5F, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, (byte)0xDA,
                0x00, (byte)0xDD, 0x00, (byte)0xD0, 0x00, 0x22, 0x04, (byte)0x86, 0x01, 0x2B, 0x00, 0x00, 0x00, 0x00 ,0x00 ,0x00, 0x00,
                0x00, 0x00, 0x00, crcValBytes[0], crcValBytes[1],
                }; //data frame



        int nrOfFrames= dataArr.length/20;
        int lengthIdx= 0;
        //creates subFrames not bigger than 20 bytes - simulates Android BLE Buffer
        for(int x=0;x<nrOfFrames+1;x++) {
            int z;
            int i;
            for (i=lengthIdx,z=0;z<20&&i<dataArr.length;i++,z++) {
            }
            lengthIdx+=z;
            byte[] data= new byte[z];

            for (int j=0;j<data.length;j++) {
                data[j] =dataArr[lengthIdx-z+j];
            }
            if (data != null && data.length > 0) {
                for(byte b:data){
                    mFrameBuffer.insert(b);
                }
                System.out.println("RAW_DATA: " + convertByteArrayToString(mFrameBuffer.peekBuf()));
                int idxOfStart= mFrameBuffer.getIndexOfFirstAppearance(rParser.getStartByte());
                System.out.println("\nIDX_OF_START: " + idxOfStart);
                if( idxOfStart!=-1&&mLongFrameLength==0){

                    System.out.println("PART_OF_BUF_REMOVED: " + convertByteArrayToString(mFrameBuffer.removeBuf(idxOfStart)));
                }
                if(mLongFrameLength==0||mLongFrameLength<=mFrameBuffer.getSize()) {

                    byte[] frame = mLongFrameLength>0?byteArrayToPrimitives(mFrameBuffer.peekBuf(mLongFrameLength)):byteArrayToPrimitives(mFrameBuffer.peekBuf());
                    System.out.println("RAW_DATA_FRAME: "+convertByteArrayToString(frame));
                    if (rParser.parseDataError(frame) == DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME) {
                        System.out.println("DataReceiverParser.TYPE_OF_PARSING_ERROR.LONG_FRAME");
                        mLongFrameLength = rParser.getFullFrameLength();
                    }
                    else if (rParser.parseDataError(frame) == DataReceiverParser.TYPE_OF_PARSING_ERROR.OK) {
                        System.out.println("DataReceiverParser.TYPE_OF_PARSING_ERROR.OK");
                        if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_DATA) {
                            System.out.println("-------MSG_DATA-------");
                            assertEquals(true, true);

                        } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_BROADCAST) {
                            System.out.println("-------MSG_BROADCAST-------: ");
                            assertEquals(true, false);
                        } else if (rParser.getTypeOfFrame() == DataReceiverParser.TYPE_OF_FRAME.MSG_UNKNOWN) {
                            System.out.println("-------MSG_UNKNOWN-------");
                            assertEquals(true, false);
                        }
                        mLongFrameLength = 0;
                        mFrameBuffer.removeBuf(rParser.getFullFrameLength());
                    } else {
                        System.out.println("PARSER_RESP_NOT_OK: " + rParser.parseDataError(frame).toString());
                        mFrameBuffer.removeBuf(mLongFrameLength);
                        mLongFrameLength = 0;
                    }
                }
            }
        }
    }

    private static List<String> modIdsSupported = new ArrayList<String>();
    static {
        modIdsSupported.add("19779");
        modIdsSupported.add("4543310");
        modIdsSupported.add("4997699");
        modIdsSupported.add("5270628");
    }

    private int convertByteArrayToInt(byte[] byteArray,int dataShift, int lengthOfData) {
        final StringBuilder strB = new StringBuilder(byteArray.length);
        for(int i=0;i<lengthOfData&&i<byteArray.length;i++){
            strB.append(String.format("%02X", byteArray[i+dataShift]));
        }
        String str= strB.toString();
        if(str.length()>0)
            return Integer.parseInt(str, 16);
        else
            return -1;

    }

    private void fillBuffer(ArrayList<Byte> dest, byte[] src, int nrOfBytes){
        if(src!=null && src.length>0&&dest!=null){
            if(nrOfBytes==-1) {
                for (byte b : src) {
                    dest.add(b);
                }
            }
            else{
                for (int i=0;i<nrOfBytes&&i<src.length;i++) {
                    dest.add(src[i]);
                }
            }
        }
    }


    private byte[] byteListToPrimitives(ArrayList<Byte> arr){
        byte[] resArr= new byte[arr.size()];
        for (int i=0;i<resArr.length;i++) {
            resArr[i]=arr.get(i);
        }
        return resArr;
    }

}
