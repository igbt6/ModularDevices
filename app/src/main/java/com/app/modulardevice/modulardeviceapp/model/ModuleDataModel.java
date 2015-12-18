package com.app.modulardevice.modulardeviceapp.model;

import java.util.ArrayList;

/**
 * Created by igbt6 on 15.10.2015.
 */
// File describes dataModel of a given module in xml format

public class ModuleDataModel {

    private Integer mId;
    private String mName;
    private String mIcon;
    private ArrayList<ModuleVariable> mModuleVariablesList;


   public ModuleDataModel(Integer id, String name, String icon){

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
        
    public ArrayList<ModuleVariable> getModuleVariablesList(){
        return mModuleVariablesList;
    }


    public void addModuleVariable(ModuleVariable mVar){
        mModuleVariablesList.add(mVar);
    }

    public int getNrOfVariables(){
        return mModuleVariablesList.size();
    }

    public ModuleVariable getModuleVariablebyName(String name){
        for (ModuleVariable var: mModuleVariablesList){
            if(var.getName().equals(name))
                return var;
        }
        return null;
    }


    @Override
    public String toString() {
        String moduleStr= new String();
        moduleStr+= "MODULE: "+ String.valueOf(mId)+ " "+ mName +" "+mIcon+ " NRofVARS: "+ String.valueOf(getNrOfVariables())+ '\n';
        for(ModuleVariable var : mModuleVariablesList){
            moduleStr+=" "+var.toString();
        }
        return moduleStr;
    }


}



