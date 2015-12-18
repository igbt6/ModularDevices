package com.app.modulardevice.modulardeviceapp;

import com.app.modulardevice.modulardeviceapp.ble.BleDevice;
import com.app.modulardevice.modulardeviceapp.model.ModuleDataModel;

import java.util.ArrayList;

/**
 * Created by igbt6 on 18.10.2015.
 */
public class AppEngine {
    private final static String TAG  = AppEngine.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;

    public final static String MODULE_ID  = "MODULE_ID";
    private BleDevice mBleDevice;
    private ArrayList<ModuleDataModel> mModulesDescription;
    final private ArrayList<Integer> mAvailableModulesIds = new ArrayList<>() ;

    private static Object locker = new Object();
    private static AppEngine instance =null;

    //singleton
    public static AppEngine getInstance() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new AppEngine();
                }
            }
        }
        return instance;
    }


    public void setBleDevice(BleDevice device){
        mBleDevice= device;
    }

    public BleDevice getBleDevice(){
        if(mBleDevice!=null)
            return mBleDevice;
        else return null;
    }

    public boolean isAppConnectedToDevice(){

        return mBleDevice.isConnected();
    }


    public void setAllModulesDescription(ArrayList<ModuleDataModel> modulesDescription){
        mModulesDescription= modulesDescription;
    }

    public  ArrayList<ModuleDataModel> getModulesDescription(){

        return mModulesDescription;
    }


    public void addAvailableModuleId(Integer id){
        if(!mAvailableModulesIds.contains(id)) {
            mAvailableModulesIds.add(id);
        }
    };

    public ArrayList<Integer> getAvailableModulesIds(){

        return mAvailableModulesIds;
    }


    public ArrayList<Integer> extractModuleIdsFromDataModuleDescription() {
        ArrayList<Integer> idsArr = new ArrayList<>();
        for(ModuleDataModel m :mModulesDescription){
            idsArr.add(m.getId());
        }
        return idsArr;
    }


    public boolean checkIfGivenModuleIsSupportedById(Integer moduleId){
        for(ModuleDataModel m :mModulesDescription){
            if(m.getId().equals(moduleId) )
                return true;
        }
        return false;
    }

    public ModuleDataModel getModuleById(Integer moduleId){
        for(ModuleDataModel m :mModulesDescription){
            if(m.getId().equals(moduleId) )
                return m;
        }
        return null;
    }


    public boolean checkIfGivenModuleIsSupportedByName(String moduleName){
        for(ModuleDataModel m :mModulesDescription){
            if(m.getName().equals(moduleName) )
                return true;
        }
        return false;
    }


}
