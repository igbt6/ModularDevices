package com.app.modulardevice.modulardeviceapp.model;

import android.content.Context;
import android.os.AsyncTask;

import com.app.modulardevice.modulardeviceapp.OnTaskCompleted;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;

/**
 * Created by igbt6 on 13.10.2015.
 */
public class ModuleXmlNewParser {
    private final static String TAG  = ModuleXmlNewParser.class.getSimpleName();
    private final static boolean LOG_ENABLE = true;

    private final static String MODULE_XML_TAG_MODULES= "Modules";
    private final static String MODULE_XML_TAG_MODULE= "Module";
    private final static String MODULE_XML_TAG_MODULE_ID= "id";
    private final static String MODULE_XML_TAG_MODULE_NAME= "name";
    private final static String MODULE_XML_TAG_MODULE_ICON= "icon";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE= "Variable";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_NAME= "name";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_EQUATION= "equation";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_UNIT= "unit";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_ICON= "icon";

    private final static String MODULE_XML_TAG_MODULE_VARIABLE_READ= "VarRead";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_READ_EQUATION= "equation";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_WRITE= "VarWrite";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_WRITE_TYPE= "type";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_WRITE_DEFAULT= "default";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_WRITE_STEP= "step";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_WRITE_BYTE_NR= "byte_nr";
    private final static String MODULE_XML_TAG_MODULE_VARIABLE_WRITE_BITS= "bits";

    public static final String mUrlString = "http://gluonium.com/xml/moduleDescriptionNew.xml";
    public static final String mFileNameString = "moduleDescription.xml";
    private XmlPullParser xmlParser;

    private ArrayList<ModuleDataModelNew> parseData(InputStream inputData)throws IOException, XmlPullParserException {

        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            xmlParser = factory.newPullParser();
            xmlParser.setInput(inputData, null);
            xmlParser.nextTag();
            return readModulesData(xmlParser);
        }
        catch (IOException ioe){
            LOGE(LOG_ENABLE,TAG,"IOException ",ioe);
            return null;
        }
        catch (XmlPullParserException e){
            LOGE(LOG_ENABLE,TAG,"XmlPullParserException",e);
            return null;
        }
        finally {
            inputData.close();
        }

    }
   private ArrayList<ModuleDataModelNew> readModulesData(XmlPullParser xmlParser) throws IOException, XmlPullParserException {
       ArrayList<ModuleDataModelNew> moduleList = new ArrayList<>();

       xmlParser.require(XmlPullParser.START_TAG,null,MODULE_XML_TAG_MODULES);
       while(xmlParser.next()!= XmlPullParser.END_TAG){
           if(xmlParser.getEventType()!=XmlPullParser.START_TAG){

               continue;
           }
           String name =xmlParser.getName();
           //here im searching for a "Module" tag
           if(name.equals(MODULE_XML_TAG_MODULE)){
               moduleList.add(readModule(xmlParser));
           }
           else{
               skip(xmlParser);
           }

       }
       return moduleList;
   }

    private ModuleDataModelNew readModule(XmlPullParser xmlParser)throws IOException, XmlPullParserException {
        xmlParser.require(XmlPullParser.START_TAG, null, MODULE_XML_TAG_MODULE); //must start with <Module> tag
        ModuleDataModelNew module = null;
        String tag = xmlParser.getName();
        String moduleId= xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_ID);
        String moduleName="";
        String moduleIcon="";
        if(tag.equals(MODULE_XML_TAG_MODULE)){
            moduleName = xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_NAME);
            moduleIcon = xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_ICON);
        }
        module = new ModuleDataModelNew(Integer.valueOf(moduleId),moduleName,moduleIcon);

        while(xmlParser.next() != XmlPullParser.END_TAG){
            if(xmlParser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = xmlParser.getName();

            if (name.equals(MODULE_XML_TAG_MODULE_VARIABLE)) {
                xmlParser.require(XmlPullParser.START_TAG, null, MODULE_XML_TAG_MODULE_VARIABLE);
                module.addModuleVariable(readModuleVariable(xmlParser));
                xmlParser.require(XmlPullParser.END_TAG, null, MODULE_XML_TAG_MODULE_VARIABLE);
            }
            else{
                skip(xmlParser);
            }
        }
        return module;
    }



    private ModuleVariableNew readModuleVariable(XmlPullParser xmlParser)throws XmlPullParserException, IOException{

        ModuleVariableNew modVar = null;
        String tag = xmlParser.getName();
        String modVarName="";
        String modVarUnit="";
        String modVarIcon="";
        if(tag.equals(MODULE_XML_TAG_MODULE_VARIABLE)) {
            modVarName = xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_NAME);
            modVarUnit =xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_UNIT);
            modVarIcon =xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_ICON);
        }

        ModuleVariableRead mRead=null;
        ModuleVariableWrite mWrite=null;
        while(xmlParser.next() != XmlPullParser.END_TAG){
            if(xmlParser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = xmlParser.getName();

            if (name.equals(MODULE_XML_TAG_MODULE_VARIABLE_READ)) {
                xmlParser.require(XmlPullParser.START_TAG, null, MODULE_XML_TAG_MODULE_VARIABLE_READ);
                mRead= readModuleVariableReadTag(xmlParser);
                xmlParser.require(XmlPullParser.END_TAG, null, MODULE_XML_TAG_MODULE_VARIABLE_READ);
            }
            else{
                skip(xmlParser);
            }
        }
        /*while(xmlParser.next() != XmlPullParser.END_TAG){
            if(xmlParser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = xmlParser.getName();
            if(name.equals(MODULE_XML_TAG_MODULE_VARIABLE_WRITE)) {
                xmlParser.require(XmlPullParser.START_TAG, null, MODULE_XML_TAG_MODULE_VARIABLE_WRITE);
                mWrite= readModuleVariableWriteTag(xmlParser);
                xmlParser.require(XmlPullParser.END_TAG, null, MODULE_XML_TAG_MODULE_VARIABLE_WRITE);
            }
            else{
                skip(xmlParser);
            }
        }
        */
        modVar= new ModuleVariableNew(mRead,mWrite,modVarName,modVarUnit, modVarIcon);
        return modVar;
    }


    private ModuleVariableRead readModuleVariableReadTag(XmlPullParser xmlParser)throws XmlPullParserException, IOException{

        ModuleVariableRead modVarRead = null;
        String tag = xmlParser.getName();
        String modVarEquation="";
        if(tag.equals(MODULE_XML_TAG_MODULE_VARIABLE_READ)) {
            modVarEquation = xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_READ_EQUATION);
        }
        modVarRead= new ModuleVariableRead(modVarEquation);
        xmlParser.nextTag();
        return modVarRead;
    }

    private ModuleVariableWrite readModuleVariableWriteTag(XmlPullParser xmlParser)throws XmlPullParserException, IOException{

        ModuleVariableWrite modVarWrite = null;
        String tag = xmlParser.getName();
        String type="";
        String defaultVal="";
        String step="";
        String byteNr="";
        String bit="";
        if(tag.equals(MODULE_XML_TAG_MODULE_VARIABLE_WRITE)) {
            type = xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_WRITE_TYPE);
            defaultVal = xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_WRITE_DEFAULT);
            step =xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_WRITE_STEP);
            byteNr =xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_WRITE_BYTE_NR);
            bit =xmlParser.getAttributeValue(null, MODULE_XML_TAG_MODULE_VARIABLE_WRITE_BITS);
        }
        modVarWrite = new ModuleVariableWrite(type,defaultVal,step, byteNr,bit);
        xmlParser.nextTag();
        return modVarWrite;
    }

    private void skip(XmlPullParser xmlParser) throws XmlPullParserException, IOException {

        if(xmlParser.getEventType()!=XmlPullParser.START_TAG){
            throw new IllegalStateException();
        }

        // for every new tag increase the depth counter, decrease it for each tag's end and
        // return when we have reached an end tag that matches the one we started with.
        int depth =1;
        while(depth!=0){

            switch(xmlParser.next()){
                case  XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public String mModuleDescriptionString = "";

    public String getModuleDescriptionXmlAsString()
    {
        return mModuleDescriptionString;
    }

    private class DownloadXmlDataTask extends AsyncTask<String, Void, ArrayList<ModuleDataModelNew>> {

        DownloadXmlDataTask(OnTaskCompleted onTaskCompleteCallback){

            mOnTaskCompleteCallback= onTaskCompleteCallback;

        }

        private  OnTaskCompleted mOnTaskCompleteCallback;
        @Override
        protected ArrayList<ModuleDataModelNew> doInBackground(String... urls) {
            try {
                InputStream inStr = downloadXmlDataFromUrl(urls[0]);
                mModuleDescriptionString = getStringFromInputStream(downloadXmlDataFromUrl(urls[0]));
                return parseData(inStr);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ModuleDataModelNew> result) {
            mOnTaskCompleteCallback.onTaskCompleted(result);
        }


        private String getStringFromInputStream(InputStream is) {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }

    }

    public void loadXmlFileFromNetworkAsync(String urlString, OnTaskCompleted onCallback) throws IOException {

        new DownloadXmlDataTask(onCallback ).execute(urlString);
    }




    public ArrayList<ModuleDataModelNew> loadXmlFileFromMemory(String fileName,Context activityContext) throws IOException {
        try {
            FileInputStream fin =  activityContext.openFileInput(fileName);
            int c;
            String temp = "";
            while ((c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
            String modulesDescriptionFile = temp;
            InputStream inStr = new ByteArrayInputStream(modulesDescriptionFile.getBytes(StandardCharsets.UTF_8));
            return parseData(inStr);
        } catch (Exception e) {
            return null;
        }
    }


    public ArrayList<ModuleDataModelNew> loadXmlFileFromNetwork(String urlString) throws IOException {

        InputStream inStream= null;
        try {
            inStream = downloadXmlDataFromUrl(urlString);
            return parseData(inStream);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            if(inStream!=null){
                inStream.close();
            }
        }
        return null; //TODO get string representation of module
    }


    private InputStream downloadXmlDataFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();// Starts the query
        return conn.getInputStream();
    }
}
