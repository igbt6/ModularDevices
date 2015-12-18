package com.app.modulardevice.modulardeviceapp.model;

/**
 * Created by igbt6 on 30.10.2015.
 */
public interface DataParser {
    /**
     * Parses Data
     * @param receivedData
     * @return result of checked frame if success or not
     */

    boolean parseData(byte[] receivedData);


}
