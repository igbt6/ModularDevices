package com.app.modulardevice.modulardeviceapp.model;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGD;
import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;

/**
 * Created by igbt6 on 17.10.2015.
 */

public class ModuleVariable{
    private final static String TAG  = ModuleVariable.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;

    //private static final String mRegexEquationDataPattern= "(d\\[\\s*(?<nrOfByte>\\d*)\\s*\\]*)"; unfortunately named group is not supported in android
    private static final String mRegexEquationDataPattern= "(d\\[\\s*(\\d*)\\s*\\]*)";
    private String mName;
    private String mEquation;
    private String mUnit;
    private String mIcon;
    private Double mComputedValue;
    public  ModuleVariable(String name, String equation,String unit, String icon){

        mName =name;
        mEquation =equation;
        mUnit =unit;
        mIcon =icon;
        mComputedValue=0.0;

    }
    //setters
    public void setName(String name){

        mName =name;
    }

    public void setEquation(String equation){

        mEquation =equation;
    }


    public void setUnit(String unit){

        mUnit =unit;
    }

    public void setIcon(String icon){

        mIcon =icon;
    }

    //getters
    public String getName( ){

        return mName;
    }

    public String getEquation( ){

        return mEquation;
    }

    public String getUnit(){

        return mUnit;
    }

    public String getIcon(){
        return mIcon;
    }


    public void setComputedValue(Double value){
        mComputedValue= value;
    }

    public Double getComputedValue(){
        return mComputedValue;
    }

    public Double computeEquation(Byte[] receivedFrame){
        Double val;
        if(receivedFrame==null) {
            throw new IllegalArgumentException(
                    "Received data frame cannot be empty");
        }
        Pattern pattern = Pattern.compile(mRegexEquationDataPattern);
        Matcher matcher = pattern.matcher(mEquation);

        Set<String> varNames= new TreeSet<>();
        List<Integer> nrOfBytes= new ArrayList<>();
        while (matcher.find()) {
            varNames.add(convertParenthessisToUnderscores(matcher.group(1))); //d[..]
            nrOfBytes.add(Integer.parseInt(matcher.group(2)));
        }
        /*
        for( String s: varNames )
            LOGD(LOGGER_ENABLE, TAG,s);
        for( Integer s: nrOfBytes )
             LOGD(LOGGER_ENABLE, TAG,String.valueOf(s));
        */
        Map<String,Double> varMap = new TreeMap<>();
        for (int i = 0; i < varNames.size() ; i++) {
            try {
                varMap.put((String) varNames.toArray()[i], (Double.valueOf((int)(receivedFrame[nrOfBytes.get(i)])&0xFF)));
            }
            catch (Exception e){
                LOGE(LOGGER_ENABLE, TAG, "Cannot create Double value", e);
            }
        }
        //LOGD(LOGGER_ENABLE, TAG,String.valueOf(varMap.get("d_3_")));
        Expression expr= new ExpressionBuilder(convertParenthessisToUnderscores(mEquation)).variables(varNames).build().setVariables(varMap);
        val = expr.evaluate();
        return val;
    }

    private String convertParenthessisToUnderscores(String varName) {

        if (varName == null) {
            throw new IllegalArgumentException(
                    "Empty var name is a big bug buddy ;]");
        }
        return varName.replace("[", "_").replace("]", "_");
    }

    @Override
    public String toString() {
        return "VARIABLE: "+ mName + " "+mEquation +" "+mUnit+" "+mIcon+ '\n';
    }
}
