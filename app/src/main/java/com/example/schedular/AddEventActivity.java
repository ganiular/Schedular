package com.example.schedular;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedular.dialog.InputNumberDialog;
import com.example.schedular.dialog.InputRangeDialog;
import com.example.schedular.utils.Converter;
import com.google.android.material.chip.Chip;

import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity{
    ContentValues settingsValue = new ContentValues();
    String[] selectedDays = new String[7];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        findViewById(R.id.timeBox).setOnClickListener(this::openTimeDialog);
        findViewById(R.id.durationBox).setOnClickListener(this::openDurationDialog);
        findViewById(R.id.eventNameTxt).setOnClickListener(this::openEventNameDialog);
        ((Switch) findViewById(R.id.reminderSwt)).setOnCheckedChangeListener(this::switchReminder);
        ((Switch) findViewById(R.id.muteSwt)).setOnCheckedChangeListener(this::switchMuteDevice);
        ((Switch) findViewById(R.id.vibrationSwt)).setOnCheckedChangeListener(this::switchEnableVibration);
        ((Switch) findViewById(R.id.dataSwt)).setOnCheckedChangeListener(this::switchDataDevice);
        findViewById(R.id.ringingVolumeLyt).setOnClickListener(this::openRingVolumeDialog);
        ((CheckBox) findViewById(R.id.repeatChb)).setOnCheckedChangeListener(this::switchRepeat);


        // set default time and day
        setNow();

        findViewById(R.id.submitBtn).setOnClickListener(view -> {
            try {
                createNewEvent(view);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchDataDevice(CompoundButton compoundButton, boolean on) {
        settingsValue.put("offData", on);
    }

    private void switchEnableVibration(CompoundButton compoundButton, boolean on) {
        settingsValue.put("enableVibration", on);
    }

    private void switchRepeat(CompoundButton compoundButton, boolean on) {
        settingsValue.put("repeat", on);
    }

    private void openRingVolumeDialog(View view) {
        new InputRangeDialog(this, "Set Volume", new InputNumberDialog.Result() {
            @Override
            protected void onSubmit(int value) {
                settingsValue.put("ringingVolume", value);
                TextView txt = view.findViewById(R.id.ringingVolumeTxt);
                txt.setText(value + "%");
            }
        });
    }

    private void createNewEvent(View view) throws Exception {
        Log.i("createNewEvent", settingsValue.toString());

        // Verify if event title and time has been set, if not raise error
        if(!settingsValue.containsKey("title") || settingsValue.getAsString("title").trim().length() < 1){
            throw new Exception("Event name not set");
        }
        if(!(settingsValue.containsKey("hour") || settingsValue.containsKey("minute"))
            || (settingsValue.getAsInteger("hour") + settingsValue.getAsInteger("minute") < 0)){
            throw new Exception("Event time not set");
        }
        // if(settingsValue.containsKey("reminder") )

        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }

    private void switchMuteDevice(CompoundButton compoundButton, boolean on) {
        LinearLayout layout = findViewById(R.id.ringingVolumeLyt);
        layout.setEnabled(!on);
        layout.setClickable(!on);

        for(int i=0; i<layout.getChildCount(); i++){
            View child = layout.getChildAt(i);
            child.setEnabled(!on);
        }

        settingsValue.put("muteDevice", on);
    }

    private void switchReminder(CompoundButton compoundButton, boolean on) {
        LinearLayout[] layouts = new LinearLayout[3];
        layouts[0] = findViewById(R.id.remindLyt);
        layouts[1] = findViewById(R.id.snoozeLyt);
        layouts[2] = findViewById(R.id.soundLyt);

        for (LinearLayout l: layouts){
            for(int i=0; i<l.getChildCount(); i++){
                View child = l.getChildAt(i);
                child.setEnabled(on);
            }
            l.setClickable(on);
            l.setEnabled(on);
        }

        settingsValue.put("reminder", on);
    }

    private void openEventNameDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event_name, null, false);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText eventNameEdt = dialogView.findViewById(R.id.eventNameEdt);
        eventNameEdt.setText(((TextView) view).getText());
        dialogView.findViewById(R.id.okBtn).setOnClickListener(view1 -> {
            String eventName = eventNameEdt.getText().toString();
            ((TextView) view).setText(eventName);
            settingsValue.put("title", eventName);
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(view1 -> dialog.dismiss());
    }

    private void openDurationDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_duration_input, null, false);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText hourEdt = dialogView.findViewById(R.id.hourEdt);
        EditText minuteEdt = dialogView.findViewById(R.id.minuteEdt);
        dialogView.findViewById(R.id.okBtn).setOnClickListener(view1 -> {
            setDuration(view1, hourEdt, minuteEdt);
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.cancelBtn).setOnClickListener(view1 -> dialog.dismiss());
    }

    private void setNow() {
        final Calendar calendar = Calendar.getInstance();

        // get current hour, minute and day
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int d = calendar.get(Calendar.DAY_OF_WEEK);

        TextView timeTxt = findViewById(R.id.timeTxt);
        timeTxt.setText(Converter.timeFormatToAM_PM(currentHour, currentMinute));

        int chipId = 0;
        switch (d){
            case 1: chipId = R.id.sunday; break;
            case 2: chipId = R.id.monday; break;
            case 3: chipId = R.id.tuesday; break;
            case 4: chipId = R.id.wednesday; break;
            case 5: chipId = R.id.thursday; break;
            case 6: chipId = R.id.friday; break;
            case 7: chipId = R.id.saturday; break;
        }
        Chip chip = findViewById(chipId);
        chip.setSelected(true);
        chip.setChecked(true);
    }

    private void setDuration(View view, EditText hourEdt, EditText minuteEdt) {
        int hour = 0, minute = 0;
        try {
            hour = Integer.parseInt(hourEdt.getText().toString());
            minute = Integer.parseInt(minuteEdt.getText().toString());
        } catch (NumberFormatException e){
            try {
                minute = Integer.parseInt(minuteEdt.getText().toString());
            } catch (NumberFormatException ignored){}
        }

        TextView durationTxt = findViewById(R.id.durationTxt);
        durationTxt.setText(Converter.durationToTimeFormat(hour, minute));

        settingsValue.put("duration", hour * 60 + minute);
    }

    private void openTimeDialog(View view){
        final Calendar calendar = Calendar.getInstance();

        // get current hour and minute
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (timePicker, hour, minute) -> {
                    TextView timeView = view.findViewById(R.id.timeTxt);
                    timeView.setText(Converter.timeFormatToAM_PM(hour, minute));
                    settingsValue.put("hour", hour);
                    settingsValue.put("minute", minute);
                },
                currentHour,    // initial hour
                currentMinute,  // initial minute
                false           // use AM PM
        );

        timePickerDialog.show();
    }

    public void dayClick(View view){
        Chip chip = (Chip) view;
        if (chip.isSelected()){
            chip.setSelected(false);
            chip.setChecked(false);
            int id = chip.getId();
            if (id == R.id.sunday) {
                selectedDays[0] = null;
            } else if (id == R.id.monday) {
                selectedDays[1] = null;
            } else if (id == R.id.tuesday) {
                selectedDays[2] = null;
            } else if (id == R.id.wednesday) {
                selectedDays[3] = null;
            } else if (id == R.id.thursday) {
                selectedDays[4] = null;
            } else if (id == R.id.friday) {
                selectedDays[5] = null;
            } else if (id == R.id.saturday) {
                selectedDays[6] = null;
            }
        }else {
            chip.setSelected(true);
            chip.setChecked(true);
            int id = chip.getId();
            if (id == R.id.sunday) {
                selectedDays[0] = "sunday";
            } else if (id == R.id.monday) {
                selectedDays[1] = "monday";
            } else if (id == R.id.tuesday) {
                selectedDays[2] = "tuesday";
            } else if (id == R.id.wednesday) {
                selectedDays[3] = "wednesday";
            } else if (id == R.id.thursday) {
                selectedDays[4] = "thursday";
            } else if (id == R.id.friday) {
                selectedDays[5] = "friday";
            } else if (id == R.id.saturday) {
                selectedDays[6] = "saturday";
            }
        }

        // disable repeat if no day is selected
        boolean repeatable = false;
        for(String day: selectedDays){
            if (day != null){
                repeatable = true;
                break;
            }
        }

        findViewById(R.id.repeatChb).setEnabled(repeatable);

        String days = Converter.arrayToCommaSeparatedString(selectedDays);
        settingsValue.put("days", days);
    }

    public void openSettingsDialog(View view){
        int viewId = view.getId();

        if(viewId == R.id.remindLyt){
            new InputNumberDialog(this, "Remind Time", "(minutes)", new InputNumberDialog.Result() {
                @Override
                protected void onSubmit(int value) {
                    TextView textView = view.findViewById(R.id.remindTxt);
                    textView.setText(value + " min");
                    settingsValue.put("remind", value);
                }
            });
        }
        else if(viewId == R.id.snoozeLyt){
            new InputNumberDialog(this, "Snooze Interval", "(minutes)", new InputNumberDialog.Result() {
                @Override
                protected void onSubmit(int value) {
                    TextView textView = view.findViewById(R.id.snoozeTxt);
                    textView.setText(value + " min");
                    settingsValue.put("snooze", value);
                }
            });
        }
        else if(viewId == R.id.soundLyt){
            Toast.makeText(this, "Not yet available", Toast.LENGTH_SHORT).show();
        }
    }
}