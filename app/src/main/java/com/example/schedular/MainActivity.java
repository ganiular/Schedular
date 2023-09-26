package com.example.schedular;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.schedular.recycleview.EventAdapter;

public class MainActivity extends AppCompatActivity {
    private RecyclerView eventRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventRecyclerView = findViewById(R.id.eventRV);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventRecyclerView.setAdapter(new EventAdapter());

        findViewById(R.id.floatingActionButton).setOnClickListener(this::gotoAddEventActivity);
    }

    private void gotoAddEventActivity(View view) {
        Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
        startActivity(intent);
    }
}