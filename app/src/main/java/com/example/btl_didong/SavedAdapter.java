package com.example.btl_didong;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.SavedViewHolder> {

    public interface OnSavedItemClickListener {
        void onItemClick(SavedItem item);
    }

    private List<SavedItem> itemList;
    private OnSavedItemClickListener listener;

    public SavedAdapter(List<SavedItem> itemList) {
        this.itemList = itemList;
    }

    public SavedAdapter(List<SavedItem> itemList, OnSavedItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_card, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        SavedItem item = itemList.get(position);

        if (holder.tvTitle != null) holder.tvTitle.setText(item.getTitle());
        if (holder.tvSubtitle != null) holder.tvSubtitle.setText(item.getSubtitle());

        if (holder.imgFood != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.imgFood);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class SavedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvTitle;
        TextView tvSubtitle;

        public SavedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvTitle = itemView.findViewById(R.id.tvSavedTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSavedSubtitle);
        }
    }
}
