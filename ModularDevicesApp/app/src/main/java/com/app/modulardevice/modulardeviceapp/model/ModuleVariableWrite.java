package com.app.modulardevice.modulardeviceapp.model;

/**
 * Created by igbt6 on 13.12.2015.
 */
public class ModuleVariableWrite {

    private static final String TypeValues[]={"int", "bool"};

    private String mType;
    private String mDefaultVal;
    private String mStep;
    private String mByteNr;
    private String mBit;


    ModuleVariableWrite(String type,String defaultVal,String step,String byteNr,String bit){
        mType=type;
        mDefaultVal=defaultVal;
        mStep=step;
        mByteNr=byteNr;
        mBit=bit;
    }


    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getBit() {
        return mBit;
    }

    public void setBit(String bit) {
        mBit = bit;
    }

    public String getByteNr() {
        return mByteNr;
    }

    public void setByteNr(String byteNr) {
        mByteNr = byteNr;
    }

    public String getStep() {
        return mStep;
    }

    public void setStep(String step) {
        mStep = step;
    }

    public String getDefaultVal() {
        return mDefaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        mDefaultVal = defaultVal;
    }

    @Override
    public String toString() {
        return "VAR_W: "+ mType + " "+mDefaultVal+" "+mStep+" "+mByteNr+" "+mBit+ "\n";
    }
}
