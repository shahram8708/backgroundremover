package com.example.backgroundremover.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.backgroundremover.R;
import java.util.List;

public class BackgroundAdapter extends RecyclerView.Adapter<BackgroundAdapter.BackgroundViewHolder> {

    public interface OnBackgroundClickListener {
        void onBackgroundClick(int backgroundResId);
    }

    private List<Integer> backgroundList;
    private OnBackgroundClickListener listener;

    public BackgroundAdapter(List<Integer> backgroundList, OnBackgroundClickListener listener) {
        this.backgroundList = backgroundList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BackgroundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_background, parent, false);
        return new BackgroundViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BackgroundViewHolder holder, int position) {
        int bgResId = backgroundList.get(position);
        holder.imageView.setImageResource(bgResId);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBackgroundClick(bgResId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return backgroundList.size();
    }

    public static class BackgroundViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public BackgroundViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.bgImageView);
        }
    }
}
