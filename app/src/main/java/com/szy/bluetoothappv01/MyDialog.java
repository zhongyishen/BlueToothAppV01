package com.szy.bluetoothappv01;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

public class MyDialog extends Dialog {
    public MyDialog(@NonNull Context context){
        super(context);
        setContentView(R.layout.mydialog);

    }
}