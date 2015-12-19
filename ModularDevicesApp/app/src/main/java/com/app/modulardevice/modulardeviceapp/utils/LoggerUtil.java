package com.app.modulardevice.modulardeviceapp.utils;

/**
 * Created by igbt6 on 05.11.2015.
 */


import android.util.Log;


public class LoggerUtil {
    private static final int MAX_LOG_TAG_LENGTH = 100;
    private static final String LOGGER_APP_PREFIX = "GLUONIUM-";
    private static final boolean MAIN_LOGGER_ENABLED = true;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH ) {
            return LOGGER_APP_PREFIX  + str.substring(0, MAX_LOG_TAG_LENGTH-LOGGER_APP_PREFIX.length() );
        }

        return LOGGER_APP_PREFIX+ str;
    }

    public static void LOGD(boolean classScopeEnabled, final String tag, String message) {
        if (MAIN_LOGGER_ENABLED){
            if(classScopeEnabled) {
                if (Log.isLoggable(tag, Log.DEBUG)) {
                    Log.d(tag, message);
                }
            }
        }
    }

    public static void LOGD(boolean classScopeEnabled,final String tag, String message, Throwable cause) {
        if (MAIN_LOGGER_ENABLED){
            if(classScopeEnabled) {
                if (Log.isLoggable(tag, Log.DEBUG)) {
                    Log.d(tag, message, cause);
                }
            }
        }
    }

    public static void LOGV(boolean classScopeEnabled,final String tag, String message) {
        if (MAIN_LOGGER_ENABLED) {
            if(classScopeEnabled) {
                if (Log.isLoggable(tag, Log.VERBOSE)) {
                    Log.v(tag, message);
                }
            }
        }
    }

    public static void LOGV(boolean classScopeEnabled,final String tag, String message, Throwable cause) {
        if (MAIN_LOGGER_ENABLED) {
            if(classScopeEnabled) {
                if (Log.isLoggable(tag, Log.VERBOSE)) {
                    Log.v(tag, message, cause);
                }
            }
        }
    }

    public static void LOGI(boolean classScopeEnabled,final String tag, String message) {
        if (MAIN_LOGGER_ENABLED) {
            if(classScopeEnabled) {
                Log.i(tag, message);
            }
        }
    }

    public static void LOGI(boolean classScopeEnabled,final String tag, String message, Throwable cause) {
        if (MAIN_LOGGER_ENABLED) {
            if(classScopeEnabled) {
                Log.i(tag, message, cause);
            }
        }
    }

    public static void LOGW(boolean classScopeEnabled,final String tag, String message) {
        if (MAIN_LOGGER_ENABLED) {
            if(classScopeEnabled) {
                Log.w(tag, message);
            }
        }
    }

    public static void LOGW(boolean classScopeEnabled,final String tag, String message, Throwable cause) {
        if (MAIN_LOGGER_ENABLED) {
            if(classScopeEnabled) {
                Log.w(tag, message, cause);
            }
        }
    }

    public static void LOGE(boolean classScopeEnabled,final String tag, String message) {
        if (MAIN_LOGGER_ENABLED){
            if(classScopeEnabled) {
                Log.e(tag, message);
            }
        }
    }

    public static void LOGE(boolean classScopeEnabled,final String tag, String message, Throwable cause) {
        if (MAIN_LOGGER_ENABLED) {
            if(classScopeEnabled) {
                Log.e(tag, message, cause);
            }
        }
    }

    private LoggerUtil() {
        //empty private constructor
    }

}
