package com.leafnext.shutterdrop;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by syedehteshamuddin on 2017-09-18.
 */

 public class Util {
    public static void runSnackBar(String status, View coordinatorLayoutView){
        Snackbar mSnackbar = Snackbar.make(coordinatorLayoutView,status,Snackbar.LENGTH_SHORT);
        View mView = mSnackbar.getView();
        TextView mTextView = mView.findViewById(android.support.design.R.id.snackbar_text);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1){
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }else {
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        mSnackbar.show();
    }
}
