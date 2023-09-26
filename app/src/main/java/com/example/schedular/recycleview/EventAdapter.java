package com.example.schedular.recycleview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedular.R;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<ViewDataModel> dataItems;

    public EventAdapter(){
        // TODO: populate dataItems in background
        dataItems = new ArrayList<>();
        dataItems.add(new ViewDataModel("Morning Class", 23, 28));
        dataItems.add(new ViewDataModel("Morning Class", 23, 28));
        dataItems.add(new ViewDataModel("Morning Class", 23, 28));
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        ViewDataModel item = dataItems.get(position);
        holder.setData(item);
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time =  itemView.findViewById(R.id.time);
            itemView.findViewById(R.id.deleteBtn).setOnClickListener(this::removeItem);
        }

        public void removeItem(View view) {
            int index = this.getAdapterPosition();
            dataItems.remove(index);
            notifyItemRemoved(index);
        }

        public void setData(ViewDataModel item) {
            title.setText(item.title);
            time.setText(String.format("%d - %d", item.time, item.duration));
        }
    }

    public class ViewDataModel {
        String title;
        long time;
        long duration;

        ViewDataModel(String title, long time, long duration){
            this.title = title;
            this.time = time;
            this.duration = duration;
        }
    }
}
