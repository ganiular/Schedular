package com.example.schedular.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


import com.example.schedular.R;

public class InputRangeDialog {
    public InputRangeDialog(Context context, CharSequence title, InputNumberDialog.Result result){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input_range, null, false);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView titleView = dialogView.findViewById(R.id.title);
        titleView.setText(title);
        SeekBar seekBar = dialogView.findViewById(R.id.seekBar);

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(view -> result.onCancel(dialog));
        dialogView.findViewById(R.id.okBtn).setOnClickListener(view -> {
            dialog.dismiss();
            try{
                int value = seekBar.getProgress();
                result.onSubmit(Math.abs(value));
            } catch (NumberFormatException e){}
        });

        dialog.show();
    }
}
