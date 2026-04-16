package com.snhu.hawkins_cs360;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {

    private List<WeightRecord> weightList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(WeightRecord record);
        void onDeleteClick(WeightRecord record);
    }

    public WeightAdapter(List<WeightRecord> weightList, OnItemClickListener listener) {
        this.weightList = weightList;
        this.listener = listener;
    }

    public void updateList(List<WeightRecord> newList) {
        this.weightList.clear();
        this.weightList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        WeightRecord record = weightList.get(position);
        holder.dateText.setText(record.getDate());
        holder.weightText.setText(String.format("%.1f lbs", record.getWeight()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(record);
            }
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return weightList != null ? weightList.size() : 0;
    }

    public static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, weightText;
        ImageButton deleteBtn;

        public WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.date_text);
            weightText = itemView.findViewById(R.id.weight_text);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}
