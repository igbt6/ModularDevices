package com.app.modulardevice.modulardeviceapp.model;

import java.util.ArrayList;

/**
 * Created by igbt6 on 13.12.2015.
 */
// File describes dataModel of a given module in xml format

public class ModuleDataModelNew {

    private Integer mId;
    private String mName;
    private String mIcon;
    private ArrayList<ModuleVariableNew> mModuleVariablesList;


   public ModuleDataModelNew(Integer id, String name, String icon){

        mName =name;
        mId =id;
        mIcon= icon;
        mModuleVariablesList = new ArrayList<>();

    }

    public void setName(String name){

        mName =name;
    }

    public void setId(Integer id){

        mId =id;
    }

    public void setIcon(String icon){

        mIcon =icon;
    }

    public String getName(){

        return mName;
    }


    public Integer getId(){

        return mId;
    }

    public String getIcon(){

        return mIcon;
    }
        
    public ArrayList<ModuleVariableNew> getModuleVariablesList(){
        return mModuleVariablesList;
    }


    public void addModuleVariable(ModuleVariableNew mVar){
        mModuleVariablesList.add(mVar);
    }

    public int getNrOfVariables(){
        return mModuleVariablesList.size();
    }

    public ModuleVariableNew getModuleVariablebyName(String name){
        for (ModuleVariableNew var: mModuleVariablesList){
            if(var.getName().equals(name))
                return var;
        }
        return null;
    }


    @Override
    public String toString() {
        String moduleStr= new String();
        moduleStr+= "MODULE: "+ String.valueOf(mId)+ " "+ mName +" "+mIcon+ " NRofVARS: "+ String.valueOf(getNrOfVariables())+ '\n';
        for(ModuleVariableNew var : mModuleVariablesList){
            moduleStr+=var.toString();
        }
        return moduleStr;
    }


}



