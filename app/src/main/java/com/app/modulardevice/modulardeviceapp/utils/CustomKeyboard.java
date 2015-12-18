package com.app.modulardevice.modulardeviceapp.utils;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by igbt6 on 15.11.2015.
 */
public class CustomKeyboard {


        private KeyboardView mKeyboardView;
        private EditText mRegisteredEditText;
        private Activity mHostActivity;
        private String[] mLastValues;
        private int mLastValueIdx;
        private int mPreviousClickedValue;

        /** The key (code) handler. */
        private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {

            public final static int CodeDelete   = -5; // Keyboard.KEYCODE_DELETE
            public final static int CodeCancel   = -3; // Keyboard.KEYCODE_CANCEL
            public final static int CodeAllLeft  = 55001;
            public final static int CodeLeft     = 55002;
            public final static int CodeRight    = 55003;
            public final static int CodeAllRight = 55004;
            public final static int CodeClear    = 55006;
            public final static int CodeSpace = 44001;
            public final static int CodeLastValue  = 44000;


            @Override public void onKey(int primaryCode, int[] keyCodes) {

                if(mRegisteredEditText==null)
                    return;
                EditText edittext = mRegisteredEditText;
                Editable editable = edittext.getText();


                int start = edittext.getSelectionStart();

                if( primaryCode==CodeCancel ) {
                    hideCustomKeyboard();
                } else if( primaryCode==CodeDelete ) {
                    if( editable!=null && start>0 ) editable.delete(start - 1, start);
                } else if( primaryCode==CodeClear ) {
                    if( editable!=null ) editable.clear();
                } else if( primaryCode==CodeLeft ) {
                    if( start>0 ) edittext.setSelection(start - 1);
                } else if( primaryCode==CodeRight ) {
                    if (start < edittext.length()) edittext.setSelection(start + 1);
                } else if( primaryCode==CodeAllLeft ) {
                    edittext.setSelection(0);
                }else if( primaryCode==CodeSpace ) {
                    editable.insert(edittext.getSelectionStart()," ");
                }else if( primaryCode==CodeAllRight ) {
                    edittext.setSelection(edittext.length());
                    editable.insert(edittext.getSelectionStart()," ");
                } else if( primaryCode==CodeLastValue) {
                    if(mLastValueIdx>0) {
                        if (mPreviousClickedValue < 0) {
                            mPreviousClickedValue = mLastValueIdx-1;
                        }
                        editable.clear();
                        editable.insert(0, mLastValues[mPreviousClickedValue]);
                        mPreviousClickedValue--;
                    }
                } else { // insert character
                    editable.insert(start, Character.toString((char) primaryCode));


                }
            }

            @Override public void onPress(int arg0) {
            }

            @Override public void onRelease(int primaryCode) {
            }

            @Override public void onText(CharSequence text) {
            }

            @Override public void swipeDown() {
            }

            @Override public void swipeLeft() {
            }

            @Override public void swipeRight() {
            }

            @Override public void swipeUp() {
            }
        };

    /**
     *
     * @param host
     * @param viewid
     * @param layoutid
     */
        public CustomKeyboard(Activity host, int viewid, int layoutid) {
            mHostActivity= host;
            mKeyboardView= (KeyboardView)mHostActivity.findViewById(viewid);
            mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
            mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
            mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
            mLastValues = new String[10];
            mLastValueIdx=0;
            mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }



        /**
         * Returns whether the CustomKeyboard is visible.
         */
        public boolean isCustomKeyboardVisible() {
            return mKeyboardView.getVisibility() == View.VISIBLE;
        }


        /**
         * Make the CustomKeyboard visible, and hide the system keyboard for view v.
         */
        public void showCustomKeyboard( View v ) {
            mKeyboardView.setVisibility(View.VISIBLE);
            mKeyboardView.setEnabled(true);
            if( v!=null ) ((InputMethodManager)mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }


        /**
         *  Make the CustomKeyboard invisible.
         */
        public void hideCustomKeyboard() {
            mKeyboardView.setVisibility(View.GONE);
            mKeyboardView.setEnabled(false);
        }

        /**
         *  Saves last used value
         */
        public void storeLastUsedValue(String strVal) {
            if(strVal.length()>=0&&strVal!=null){
                if (mLastValueIdx == mLastValues.length) {
                    mLastValueIdx = mLastValues.length-1;
                    for(int i=1;i<mLastValues.length;i++){
                        mLastValues[i-1] = mLastValues[i];
                    }
                }
                mLastValues[mLastValueIdx] = strVal;
                mPreviousClickedValue=mLastValueIdx;
                mLastValueIdx++;
            }
        }

        /**
         * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
         *
         * @param resid The resource id of the EditText that registers to the custom keyboard.
         */
        public void registerEditText(int resid) {

            mRegisteredEditText= (EditText)mHostActivity.findViewById(resid);
            mRegisteredEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override public void onFocusChange(View v, boolean hasFocus) {
                    if( hasFocus ) showCustomKeyboard(v); else hideCustomKeyboard();
                }
            });
            mRegisteredEditText.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    showCustomKeyboard(v);
                }
            });

            // Disable spell check (hex strings look like words to Android)
            mRegisteredEditText.setInputType(mRegisteredEditText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

    }

