package com.example.schedular;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.schedular.dialog.Result;
import com.example.schedular.provider.EventProvider;
import com.example.schedular.utils.Converter;
import com.google.android.material.chip.Chip;

import java.util.Calendar;

public class EventSettingsActivity extends AppCompatActivity{
    EventProvider.EventModel settingsValue;
    EventProvider provider;
    String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private long eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_settings);

        provider = new EventProvider(this);
        Intent intent = getIntent();
        eventId = intent.getLongExtra(MainActivity.EVENT_ID, 0);
        if (eventId != 0) {
            settingsValue = provider.getEvent(eventId);
            initializeViewsData(settingsValue);
        } else {
            settingsValue = new EventProvider.EventModel();
            initializeViewsData(settingsValue);
        }

        findViewById(R.id.backBtn).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.timeBox).setOnClickListener(this::openTimeDialog);
        findViewById(R.id.durationBox).setOnClickListener(this::openDurationDialog);
        findViewById(R.id.eventNameTxt).setOnClickListener(this::openEventNameDialog);
        ((Switch) findViewById(R.id.reminderSwt)).setOnCheckedChangeListener(this::switchReminder);
        ((Switch) findViewById(R.id.muteSwt)).setOnCheckedChangeListener(this::switchMuteDevice);
        ((Switch) findViewById(R.id.vibrationSwt)).setOnCheckedChangeListener(this::switchEnableVibration);
        ((Switch) findViewById(R.id.dataSwt)).setOnCheckedChangeListener(this::switchDataDevice);
        findViewById(R.id.ringingVolumeLyt).setOnClickListener(this::openRingVolumeDialog);
        ((CheckBox) findViewById(R.id.repeatChb)).setOnCheckedChangeListener(this::switchRepeat);

        findViewById(R.id.submitBtn).setOnClickListener(view -> {
            try {
                submitEvent();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViewsData(EventProvider.EventModel event) {
        if(eventId == 0){
            ((TextView) findViewById(R.id.headerTxt)).setText("Create New Event");
        }else{
            ((TextView) findViewById(R.id.headerTxt)).setText("Edit Event");
        }
        ((TextView) findViewById(R.id.eventNameTxt)).setText(event.title);
        ((TextView) findViewById(R.id.timeTxt)).setText(Converter.timeFormatToAM_PM(event.hour, event.minute));
        ((TextView) findViewById(R.id.durationTxt)).setText(Converter.durationToTimeFormat(0, event.duration));
        ((CheckBox) findViewById(R.id.repeatChb)).setChecked(event.repeat);
        ((Switch) findViewById(R.id.reminderSwt)).setChecked(event.reminder);
        switchReminder(null, event.reminder);
        ((TextView) findViewById(R.id.remindTxt)).setText(Converter.durationToTimeFormat(0, event.remind));
        ((TextView) findViewById(R.id.snoozeTxt)).setText(Converter.durationToTimeFormat(0, event.snooze));
        ((TextView) findViewById(R.id.soundTxt)).setText(event.notificationSound);
        Switch muteSwt = findViewById(R.id.muteSwt);
        muteSwt.setChecked(event.muteDevice);
        switchMuteDevice(muteSwt, event.muteDevice);
        ((TextView) findViewById(R.id.ringingVolumeTxt)).setText(event.ringingVolume + "%");
        ((Switch) findViewById(R.id.vibrationSwt)).setChecked(event.enableVibration);
        ((Switch) findViewById(R.id.dataSwt)).setChecked(event.offMobileData);
        String[] days = event.days;
        Chip chip = findViewById(R.id.sunday);
        chip.setChecked(days[0].equals("Sunday"));
        chip.setSelected(days[0].equals("Sunday"));
        chip = findViewById(R.id.monday);
        chip.setChecked(days[1].equals("Monday"));
        chip.setSelected(days[1].equals("Monday"));
        chip = findViewById(R.id.tuesday);
        chip.setChecked(days[2].equals("Tuesday"));
        chip.setSelected(days[2].equals("Tuesday"));
        chip = findViewById(R.id.wednesday);
        chip.setChecked(days[3].equals("Wednesday"));
        chip.setSelected(days[3].equals("Wednesday"));
        chip = findViewById(R.id.thursday);
        chip.setChecked(days[4].equals("Thursday"));
        chip.setSelected(days[4].equals("Thursday"));
        chip = findViewById(R.id.friday);
        chip.setChecked(days[5].equals("Friday"));
        chip.setSelected(days[5].equals("Friday"));
        chip = findViewById(R.id.saturday);
        chip.setChecked(days[6].equals("Saturday"));
        chip.setSelected(days[6].equals("Saturday"));
    }

    private void switchDataDevice(CompoundButton compoundButton, boolean on) {
        settingsValue.offMobileData = on;
    }

    private void switchEnableVibration(CompoundButton compoundButton, boolean on) {
        settingsValue.enableVibration = on;
    }

    private void switchRepeat(CompoundButton compoundButton, boolean on) {
        settingsValue.repeat = on;
    }

    private void openRingVolumeDialog(View view) {
        new InputRangeDialog(this, "Set Volume", new Result() {
            @Override
            protected void onSubmit(int value) {
                settingsValue.ringingVolume = value;
                TextView txt = view.findViewById(R.id.ringingVolumeTxt);
                txt.setText(value + "%");
            }
        });
    }

    private void submitEvent() throws IllegalArgumentException {

        // Verify if event title and time has been set, if not raise error
        if(settingsValue.title == null || settingsValue.title.trim().length() < 1){
            throw new IllegalArgumentException("Event name not set");
        }

        // if no day is selected, auto select today
        {
            boolean dayIsSelected = false;
            for(String day: settingsValue.days){
                if (day != null){
                    dayIsSelected = true;
                    break;
                }
            }
            if(!dayIsSelected){ // no day is selected
                // get and select today
                final Calendar calendar = Calendar.getInstance();
                int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                settingsValue.days[todayIndex] = daysOfWeek[todayIndex];
            }
        }

        if(eventId == 0){
            eventId = provider.createEvent(settingsValue);
            Toast.makeText(this, "Event added successful", Toast.LENGTH_SHORT).show();
        }else{
            provider.updateEvent(settingsValue, eventId);
            Toast.makeText(this, "Event updated successful", Toast.LENGTH_SHORT).show();
        }

        // setAlarm();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.EVENT_ID, eventId);
        setResult(RESULT_OK, resultIntent);

        // end this activity and go back
        finish();
    }

    private void switchMuteDevice(CompoundButton compoundButton, boolean on) {
        LinearLayout layout = findViewById(R.id.ringingVolumeLyt);
        layout.setEnabled(!on);
        layout.setClickable(!on);

        for(int i=0; i<layout.getChildCount(); i++){
            View child = layout.getChildAt(i);
            child.setEnabled(!on);
        }

        settingsValue.muteDevice = on;
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

        settingsValue.reminder = on;
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
            settingsValue.title = eventName;
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

        settingsValue.duration = hour * 60 + minute;
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
                    settingsValue.hour = hour;
                    settingsValue.minute = minute;
                },
                currentHour,    // initial hour
                currentMinute,  // initial minute
                false           // use AM PM
        );

        timePickerDialog.show();
    }

    public void dayClick(View view){
        Chip chip = (Chip) view;
        int dayIndex = 0;
        int id = chip.getId();
        if (id == R.id.sunday) {
            dayIndex = 0;
        } else if (id == R.id.monday) {
            dayIndex = 1;
        } else if (id == R.id.tuesday) {
            dayIndex = 2;
        } else if (id == R.id.wednesday) {
            dayIndex = 3;
        } else if (id == R.id.thursday) {
            dayIndex = 4;
        } else if (id == R.id.friday) {
            dayIndex = 5;
        } else if (id == R.id.saturday) {
            dayIndex = 6;
        }

        if (chip.isSelected()){
            chip.setSelected(false);
            chip.setChecked(false);
            settingsValue.days[dayIndex] = null;
        }else {
            chip.setSelected(true);
            chip.setChecked(true);
            settingsValue.days[dayIndex] = daysOfWeek[dayIndex];
        }

        // disable repeat if no day is selected
        boolean repeatable = false;
        for(String day: settingsValue.days){
            if (day != null){
                repeatable = true;
                break;
            }
        }

        findViewById(R.id.repeatChb).setEnabled(repeatable);
    }

    public void openSettingsDialog(View view){
        int viewId = view.getId();

        if(viewId == R.id.remindLyt){
            new InputNumberDialog(this, "Remind Time", "(minutes)", new Result() {
                @Override
                protected void onSubmit(int value) {
                    TextView textView = view.findViewById(R.id.remindTxt);
                    textView.setText(value + " min");
                    settingsValue.remind = value;
                }
            });
        }
        else if(viewId == R.id.snoozeLyt){
            new InputNumberDialog(this, "Snooze Interval", "(minutes)", new Result() {
                @Override
                protected void onSubmit(int value) {
                    TextView textView = view.findViewById(R.id.snoozeTxt);
                    textView.setText(value + " min");
                    settingsValue.snooze = value;
                }
            });
        }
        else if(viewId == R.id.soundLyt){
            Toast.makeText(this, "Not yet available", Toast.LENGTH_SHORT).show();
        }
    }
}