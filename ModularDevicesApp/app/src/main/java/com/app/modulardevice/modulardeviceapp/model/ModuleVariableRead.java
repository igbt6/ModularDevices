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


/**
 * Created by igbt6 on 13.12.2015.
 */

public class ModuleVariableRead {

    private static final String mRegexEquationDataPattern= "(d\\[\\s*(\\d*)\\s*\\]*)";
    private Double mComputedValue;
    private String mEquation;


    ModuleVariableRead(String equation){
        mEquation =equation;
        mComputedValue=0.0;
    }

    public void setEquation(String equation){

        mEquation =equation;
    }

    public String getEquation(){

        return mEquation;
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
            throw new IllegalArgumentException("Received data frame cannot be empty ");
        }
        Pattern pattern = Pattern.compile(mRegexEquationDataPattern);
        Matcher matcher = pattern.matcher(mEquation);

        Set<String> varNames= new TreeSet<>();
        List<Integer> nrOfBytes= new ArrayList<>();
        while (matcher.find()) {
            varNames.add(convertParenthesisToUnderscores(matcher.group(1)));
            nrOfBytes.add(Integer.parseInt(matcher.group(2)));
        }

        Map<String,Double> varMap = new TreeMap<>();
        for (int i = 0; i < varNames.size() ; i++) {
            try {
                varMap.put((String) varNames.toArray()[i], (Double.valueOf((int)(receivedFrame[nrOfBytes.get(i)])&0xFF)));
            }
            catch (Exception e){
            }
        }
        Expression expr= new ExpressionBuilder(convertParenthesisToUnderscores(mEquation)).variables(varNames).build().setVariables(varMap);
        val = expr.evaluate();
        return val;
    }

    private String convertParenthesisToUnderscores(String varName) {

        if (varName == null) {
            throw new IllegalArgumentException(
                    "Empty var name is a big bug buddy ;]");
        }
        return varName.replace("[", "_").replace("]", "_");
    }

    @Override
    public String toString() {
        return "VAR_R: "+" "+mEquation +" " +"\n";
    }

}
