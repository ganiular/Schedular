package com.example.schedular.recycleview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedular.MainActivity;
import com.example.schedular.NotificationReceiver;
import com.example.schedular.R;
import com.example.schedular.dialog.AlertDialog;
import com.example.schedular.dialog.Result;
import com.example.schedular.provider.EventProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<EventProvider.EventModel> dataItems;
    final EventProvider provider;
    final static String EVENT_ID = "com.example.scheduler.EVENT_ID";
    private MainActivity itemClickListener;


    public EventAdapter(Context context){
        provider = new EventProvider(context);
        setHasStableIds(true);
        fetchDataItems();
    }

    @Override
    public long getItemId(int position) {
        return dataItems.get(position).id;
    }

    public void fetchDataItems() {
        // TODO: populate dataItems in background
        dataItems = new ArrayList<EventProvider.EventModel>();
        dataItems = provider.getAllEvents();
    }

    public void setEventAlarms(Context context){
        for (EventProvider.EventModel e: dataItems) {
            long time = e.getEventRemainingTime();
            setAlarm(context, e, time);
        }
    }

    private void setAlarm(Context context, EventProvider.EventModel event, long timeRemaining) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create a BroadcastReceiver to handle the notification
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        if(event.reminder)
            notificationIntent.setAction(NotificationReceiver.ACTION_REMINDER);
        else
            notificationIntent.setAction(NotificationReceiver.ACTION_START_SESSION);
        notificationIntent.setData(Uri.parse("custom:" + event.id));
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, event.id); // Unique ID for the notification (optional)

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) event.id, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (timeRemaining > 0) {
            // Calculate the alarm time based on the remaining time
//            long alarmTime = SystemClock.elapsedRealtime() + timeRemaining;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MINUTE, (int) timeRemaining);

            // Set the alarm to trigger at the calculated time
//            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }


    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        EventProvider.EventModel item = dataItems.get(position);
        holder.setData(item);
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public void setItemClickListener(MainActivity mainActivity) {
        itemClickListener = mainActivity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time =  itemView.findViewById(R.id.time);
            itemView.findViewById(R.id.deleteBtn).setOnClickListener(this::confirmDelete);
            itemView.setOnClickListener(view -> {
                itemClickListener.gotoEditEventActivity(ViewHolder.this.getItemId());
            });
        }

        private void confirmDelete(View view) {
            new AlertDialog(view.getContext(), "Delete Event", null, new Result() {
                @Override
                protected void onSubmit(int value) {
                    removeItem(ViewHolder.this.getAdapterPosition());
                }
            });
        }

        public void removeItem(int position) {
            EventProvider.EventModel data = dataItems.remove(position);
            provider.deleteEvent(data.id);
            notifyItemRemoved(position);
        }

        public void setData(EventProvider.EventModel item) {
            title.setText(item.title);
            time.setText(item.getTimeString());

            GradientDrawable background = (GradientDrawable) itemView.getBackground();
            if (item.isCurrent )
                background.setColor(itemView.getResources().getColor(R.color.event_item_current_bg));
            else
                background.setColor(itemView.getResources().getColor(R.color.event_item_bg));
            itemView.setBackground(background);
        }
    }
}
