package com.example.schedular.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.schedular.R;

public class InputNumberDialog {
    public InputNumberDialog(Context context, CharSequence title, @Nullable CharSequence description, Result result){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input_number, null, false);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView titleView = dialogView.findViewById(R.id.title);
        TextView descriptionView = dialogView.findViewById(R.id.description);
        EditText textEdt = dialogView.findViewById(R.id.textEdt);
        titleView.setText(title);
        descriptionView.setText(description);

        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(view -> result.onCancel(dialog));
        dialogView.findViewById(R.id.okBtn).setOnClickListener(view -> {
            dialog.dismiss();
            try{
                int value = Integer.parseInt(textEdt.getText().toString());
                result.onSubmit(Math.abs(value));
            } catch (NumberFormatException e){}
        });

        dialog.show();
    }

    public abstract static class Result{
        protected abstract void onSubmit(int value);

        public void onCancel(AlertDialog dialog) {
            dialog.dismiss();
        }
    }
}
