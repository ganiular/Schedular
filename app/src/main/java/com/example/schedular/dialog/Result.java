package com.example.schedular.dialog;

import android.content.DialogInterface;

public abstract class Result {
    protected abstract void onSubmit(int value);

    public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
    }
}
