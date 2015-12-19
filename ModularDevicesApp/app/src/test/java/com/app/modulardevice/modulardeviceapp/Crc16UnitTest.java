package com.app.modulardevice.modulardeviceapp;

import com.app.modulardevice.modulardeviceapp.utils.Crc16;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 * Created by igbt6 on 28.10.2015.
 */
public class Crc16UnitTest {



    @Test
    public void crc16RawCompute() throws Exception {
        System.out.println("\n-----------------crc16RawCompute------------------------");
        Crc16 crc = new Crc16();
        byte[] data2 = new byte[]{0x45, 0x53, 0x45, 0x06, 0x24, 0x23};
        byte[] data = new byte[]{(byte)0xCC,0x0F,(byte)0x97,(byte)0xFF,0x00,0x60,0x34};
        int crcVal= crc.getChecksum(data);
        System.out.println("crc16RawCompute - int: " + crcVal);
        byte[] crcByte = new byte[2];
        crcByte[0]= (byte)((crcVal>>8)&0xFF);
        crcByte[1]= (byte)((crcVal)&0xFF);
        System.out.println("crc16RawCompute - byte: " + String.valueOf(String.format("%X",crcByte[0])) +String.valueOf(String.format("%X",crcByte[1])));

        assertEquals(1, 1); //dummy call
    }


    @Test
     public void crc16Compute() throws Exception {
        System.out.println("\n-----------------crc16Compute------------------------");
        Crc16 crc = new Crc16();
        byte[] dataTab = new byte[]{0x42, 0x4C, 0x45};
        System.out.println("crc16Compute - convertByteArrayToInt: "+ convertByteArrayToInt(dataTab));
        System.out.println("crc16Compute - crc.getChecksum: " +crc.getChecksum(dataTab));
        assertEquals(34788, crc.getChecksum(dataTab));
    }


    @Test
    public void crc16Compare() throws Exception {
        System.out.println("\n-----------------crc16Compare------------------------");
        Crc16 crc = new Crc16();
        byte[] dataTab = new byte[]{0x11, 0x55};
        byte[] checksum = new byte[]{ 0x2B,0x47 };
        crc.getChecksum(dataTab);

        int crcVal = convertByteArrayToInt(checksum);
        //System.out.println(crcVal);
        assertEquals(11866, crc.getChecksum(dataTab));
    }



    private int convertByteArrayToInt(byte[] byteArray) {
        final StringBuilder strB = new StringBuilder(byteArray.length);
        for (byte aByte : byteArray)
            strB.append(String.format("%02X",aByte));
        return Integer.parseInt(strB.toString(), 16);

    }

}
