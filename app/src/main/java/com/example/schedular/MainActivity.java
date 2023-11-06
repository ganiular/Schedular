package com.example.schedular;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.schedular.recycleview.EventAdapter;

public class MainActivity extends AppCompatActivity {
    private static final int CREATE_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2;
    public static final String EVENT_ID = "event_id";
    private RecyclerView eventRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventRecyclerView = findViewById(R.id.eventRV);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        EventAdapter adapter = new EventAdapter(this);
        adapter.setItemClickListener(this);
        adapter.setEventAlarms(this);
        eventRecyclerView.setAdapter(adapter);

        findViewById(R.id.floatingActionButton).setOnClickListener(this::gotoAddEventActivity);

        // TODO: Remove
        // NOTE: This just for test for notification
        findViewById(R.id.floatingActionButton).setOnLongClickListener(view -> {
            Intent intent = new Intent(NotificationReceiver.ACTION_REMINDER);
            intent.putExtra(NotificationReceiver.NOTIFICATION_ID, 1L);
            sendBroadcast(intent);
            return true;
        });
    }

    private void gotoAddEventActivity(View view) {
        Intent intent = new Intent(this, EventSettingsActivity.class);
        startActivityForResult(intent, MainActivity.CREATE_EVENT_REQUEST);
    }

    public  void gotoEditEventActivity(long eventId) {
        Intent intent = new Intent(this, EventSettingsActivity.class);
        intent.putExtra(EVENT_ID, eventId);
        startActivityForResult(intent, MainActivity.EDIT_EVENT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MainActivity.CREATE_EVENT_REQUEST:
            case MainActivity.EDIT_EVENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    // reload event list
                    // reloadData();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadData();
    }

    private void reloadData() {
        EventAdapter adapter = (EventAdapter) eventRecyclerView.getAdapter();
        adapter.fetchDataItems();
        adapter.notifyDataSetChanged();
        adapter.setEventAlarms(this);
    }
}