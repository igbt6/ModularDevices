package com.app.modulardevice.modulardeviceapp.utils;

/**
 * Created by igbt6 on 27.10.2015.
 */
public class Crc16 {

    private static final int CRC16_POLYNOMIAL =0x9EB2;
    private static final int CRC16_INITIAL_VALUE  =  0xFFFF;
    private int mCrcValue;

    public Crc16() {
        mCrcValue = CRC16_INITIAL_VALUE ;
    }

    /** update CRC
     * @param bytes**/
    /* //Previous not used version
    private void updateCrcValue(byte aByte) {
        int a, b;

        a = (int) aByte;
        for (int count = 7; count >=0; count--) {
            a = a << 1;
            b = (a >>> 8) & 1;
            if ((mCrcValue & 0x8000) != 0) {
                mCrcValue = ((mCrcValue<< 1) + b) ^ 0x1021;
            } else {
                mCrcValue = (mCrcValue << 1) + b;
            }
        }
        mCrcValue = mCrcValue & 0xffff;
        return;
    }
    */
    /*//Previous not used version
    public int getChecksum(Byte[] bytes) {
        resetCrcVal();
        for (byte b : bytes) {
            updateCrcValue(b);
        }
        return mCrcValue;
    }
   */

    public int getChecksum(byte[] bytes) {
        resetCrcVal();
        int data;
        for (byte b : bytes) {
            data=(b&0xFF);
            for (int i = 0;i<8;i++,data>>=1) {
                if (((mCrcValue & 0x0001) ^ (data & 0x0001))>0)
                    mCrcValue = ((mCrcValue >> 1) ^ CRC16_POLYNOMIAL);
                else
                    mCrcValue >>= 1;
            }
        }
        mCrcValue =  ~mCrcValue;
        data = mCrcValue;
        mCrcValue =  ((mCrcValue << 8) | (data >> 8 & 0xff));
        return mCrcValue&0xFFFF;
    }

    public byte[] convertChecksumToBytes() {
        byte[] crcByte = new byte[2];
        crcByte[0]= (byte)((mCrcValue>>8)&0xFF);
        crcByte[1]= (byte)((mCrcValue)&0xFF);
        //System.out.println("byte[]: " + String.valueOf(String.format("%X",crcByte[0])) +String.valueOf(String.format("%X",crcByte[1])));
        return crcByte;
    }




    /** reset CRC value to 0 */
    public void resetCrcVal() {
        mCrcValue = CRC16_INITIAL_VALUE;
    }


}
