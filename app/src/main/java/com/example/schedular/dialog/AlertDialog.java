package com.example.schedular.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.schedular.R;

public class AlertDialog {
    public AlertDialog(Context context, CharSequence title, CharSequence description, Result result){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(description);
        builder.setPositiveButton("Cancel", (dialogInterface, i) -> {
            result.onCancel(dialogInterface);
        });
        builder.setNegativeButton("Delete", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            result.onSubmit(i);
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
