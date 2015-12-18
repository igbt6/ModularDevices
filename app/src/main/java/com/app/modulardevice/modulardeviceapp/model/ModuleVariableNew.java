package com.app.modulardevice.modulardeviceapp.model;


/**
 * Created by igbt6 on 14.12.2015.
 */
public class ModuleVariableNew {

    private String mName;
    private String mUnit;
    private String mIcon;


    private ModuleVariableRead mVarRead;
    private ModuleVariableWrite mVarWrite;
    public  ModuleVariableNew(ModuleVariableRead varRead, ModuleVariableWrite varWrite, String name, String icon, String unit){

        mName =name;
        mUnit =unit;
        mIcon =icon;
        mVarRead= varRead;
        mVarWrite =varWrite;
    }
    //setters
    public void setName(String name){

        mName =name;
    }

    public void setUnit(String unit){

        mUnit =unit;
    }

    public void setIcon(String icon){

        mIcon =icon;
    }


    public void setVarRead(ModuleVariableRead mVarRead) {
        this.mVarRead = mVarRead;
    }


    public void setVarWrite(ModuleVariableWrite mVarWrite) {
        this.mVarWrite = mVarWrite;
    }


    //getters
    public String getName( ){

        return mName;
    }

    public String getUnit(){

        return mUnit;
    }

    public String getIcon(){
        return mIcon;
    }

    public ModuleVariableWrite getVarWrite() {
        return mVarWrite;
    }

    public ModuleVariableRead getVarRead() {
        return mVarRead;
    }

    @Override
    public String toString() {
        String retVal="VARIABLE: "+ mName + " "+mUnit+" "+mIcon+ "\n";
        try{ // just in case w/r null
            retVal+="  "+getVarRead().toString();
            retVal+="  "+String.valueOf(getVarWrite().toString());
        }
        catch(Exception e){

        }
        return retVal;
    }
}



