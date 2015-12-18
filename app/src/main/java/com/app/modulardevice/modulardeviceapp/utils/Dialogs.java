
package com.app.modulardevice.modulardeviceapp.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

// Dialogs - is used to showing dialogs
public class Dialogs {

    // Displays dialog with title, message, positive and negative button
    public static Dialog showAlert(CharSequence title, CharSequence message, Context context,
            CharSequence positiveBtnText, CharSequence negativeBtnText, OnClickListener positiveListener,
            OnClickListener negativeListener, int iconDrawableId) {

        final AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        if (positiveListener != null && positiveBtnText != null) {
            alert.setButton(AlertDialog.BUTTON_POSITIVE, positiveBtnText, positiveListener);
        }
        if (negativeListener != null && negativeBtnText != null) {
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, negativeBtnText, negativeListener);
        }
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setIcon(iconDrawableId);
        alert.show();
        return alert;
    }

    // Displays progress dialog with title and message
    public static ProgressDialog showProgress(CharSequence title, CharSequence message, Context context,
            OnCancelListener listener,int iconDrawableId) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.setOnCancelListener(listener);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setIcon(iconDrawableId);
        progressDialog.show();
        return progressDialog;
    }
}
