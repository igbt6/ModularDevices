package com.app.modulardevice.modulardeviceapp.utils;

/**
 * Created by igbt6 on 11.11.2015.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;

import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.R;

import java.nio.CharBuffer;
import java.util.ArrayList;


/**
 * Util class provides convenient methods for common operations
 */
public class Util {


    //UTILS
    public static Byte[] byteArrayToObjects(byte[] bytesData) {
        Byte[] bytes = new Byte[bytesData.length];
        int i = 0;
        for (byte b : bytesData) bytes[i++] = b;

        return bytes;
    }


    public static byte[] byteArrayToPrimitives(Byte[] bytesData) {
        byte[] bytes = new byte[bytesData.length];
        int i = 0;
        for (Byte b : bytesData){
            bytes[i] = b.byteValue();
            i++;
        }

        return bytes;


    }
    public static byte[] byteListToPrimitives(ArrayList<Byte> arr){
        byte[] resArr= new byte[arr.size()];
        for (int i=0;i<resArr.length;i++) {
            resArr[i]=arr.get(i);
        }
        return resArr;
    }


    public static int getModuleIconPathByModId(Context context, int moduleId){
        String iconUri= "drawable/"+ AppEngine.getInstance().getModuleById(moduleId).getIcon();
        int iconResource= context.getResources().getIdentifier(iconUri, null, context.getPackageName());
        try{
            Drawable icon= ContextCompat.getDrawable(context, iconResource);
        }
        catch(Exception e){
            // icon= ContextCompat.getDrawable(mContext,R.drawable.app_logo);
            iconResource= R.drawable.app_logo;
        }
        return iconResource;
    }

    public static int getModuleIconPathByName(Context context, String iconName){
        String iconUri= "drawable/"+ iconName;

        int iconResource= context.getResources().getIdentifier(iconUri, null, context.getPackageName());
        try{
            Drawable icon= ContextCompat.getDrawable(context, iconResource);
        }
        catch(Exception e){
            // icon= ContextCompat.getDrawable(mContext,R.drawable.app_logo);
            iconResource= R.drawable.app_logo;
        }
        return iconResource;
    }


    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static String convertByteArrayToString(Byte[] data) {
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (Byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    public static String convertByteArrayToString(byte[] data) {
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            return stringBuilder.toString();
        } else {
            return null;
        }
    }


    public static String createSpaces( int nrOfSpaces ) {
        if(nrOfSpaces>0)
            return CharBuffer.allocate(nrOfSpaces ).toString().replace( '\0', ' ' );
        else return "";
    }
}
