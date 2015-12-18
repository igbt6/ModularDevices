package com.app.modulardevice.modulardeviceapp;

import com.app.modulardevice.modulardeviceapp.model.DataSenderParser;
import org.junit.Test;
import java.lang.String;
import static org.junit.Assert.assertEquals;

/**
 * Created by igbt6 on 15.11.2015.
 */
public class DataSenderParserUnitTest {


    @Test
    public void prepareFrameFromString() throws Exception {
        System.out.println("\n-----------------prepareFrameFromString------------------------");
        DataSenderParser parser = new DataSenderParser();
        String data = "CC0F97FF006034"; //data frame
        byte[] preparedByteDataFrame;
        preparedByteDataFrame = parser.convertStringToRawData(data);
        assertEquals(true, preparedByteDataFrame != null);

        final StringBuilder stringBuilder = new StringBuilder(preparedByteDataFrame.length);
        for (byte byteChar : preparedByteDataFrame)
            stringBuilder.append(String.format("%02X", byteChar));
        System.out.println("RAW_DATA_TO_STR:" + stringBuilder.toString());
        System.out.println(new String(preparedByteDataFrame));
        assertEquals(data, stringBuilder.toString());

    }


    @Test
    public void parseDataToFullFrame() throws Exception {
        System.out.println("\n-----------------parseDataToFullFrame------------------------");
        DataSenderParser parser = new DataSenderParser();
        String data = "CC0F97FF006034"; //data frame

        String fullFrameResult = "CC0F97FF006034AF4B7C7C7C00"; //result data frame , crc computed in Crc16UnitTest
        byte[] preparedRawByteDataFrame;
        preparedRawByteDataFrame = parser.convertStringToRawData(data);
        assertEquals(true, preparedRawByteDataFrame != null);

        final StringBuilder stringBuilder = new StringBuilder(preparedRawByteDataFrame.length);

        parser.parseData(preparedRawByteDataFrame);

        assertEquals(true, parser.getParsedData() != null);

        for (byte byteChar : parser.getParsedData())
            stringBuilder.append(String.format("%02X", byteChar));
        System.out.println("FULL_FRAME:" + stringBuilder.toString());
        assertEquals(stringBuilder.toString(), fullFrameResult);

    }

}
