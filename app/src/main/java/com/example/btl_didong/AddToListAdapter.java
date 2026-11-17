package com.example.btl_didong;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddToListAdapter extends RecyclerView.Adapter<AddToListAdapter.AddViewHolder> {

    public interface OnCardClickListener {
        void onCardClick(Restaurant restaurant);
    }

    private List<Restaurant> restaurantList;
    private Set<String> selectedNames;
    private OnCardClickListener cardClickListener;

    public AddToListAdapter(List<Restaurant> restaurantList, Set<String> initialSelected, OnCardClickListener listener) {
        this.restaurantList = restaurantList;
        this.selectedNames = new HashSet<>(initialSelected);
        this.cardClickListener = listener;
    }

    public Set<String> getSelectedNames() {
        return selectedNames;
    }

    private boolean isNameSelected(String name) {
        if (name == null || selectedNames == null) return false;
        for (String s : selectedNames) {
            if (s.trim().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private void removeSelectedName(String name) {
        if (name == null || selectedNames == null) return;
        selectedNames.removeIf(s -> s.trim().equalsIgnoreCase(name.trim()));
    }

    @NonNull
    @Override
    public AddViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_card, parent, false);
        return new AddViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        Context context = holder.itemView.getContext();

        if (holder.tvName != null) holder.tvName.setText(restaurant.getName());
        if (holder.tvRating != null) holder.tvRating.setText("★ " + restaurant.getRating());
        if (holder.tvSub != null) holder.tvSub.setText(restaurant.getDistance() + " • " + restaurant.getPriceRange());
        if (holder.tag1 != null) holder.tag1.setText(restaurant.getTag1());
        if (holder.tag2 != null) holder.tag2.setText("Hoàng Mai");
        if (holder.tagMatch != null) holder.tagMatch.setText("Khớp " + restaurant.getTag1());

        if (holder.imgRestaurant != null) {
            Glide.with(context)
                    .load(restaurant.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgRestaurant);
        }

        boolean isSelected = isNameSelected(restaurant.getName());
        updateBookmarkFlagState(holder.btnSaveBookmark, isSelected, context);

        if (holder.btnSaveBookmark != null) {
            holder.btnSaveBookmark.setOnClickListener(v -> {
                if (isNameSelected(restaurant.getName())) {
                    // Bấm lần 2 ➔ Bỏ lưu khỏi danh sách, trả biểu tượng lá cờ về dạng khung rỗng
                    removeSelectedName(restaurant.getName());
                    updateBookmarkFlagState(holder.btnSaveBookmark, false, context);
                } else {
                    // Bấm lần 1 ➔ Lưu vào danh sách, tô đậm lá cờ màu xanh
                    selectedNames.add(restaurant.getName());
                    updateBookmarkFlagState(holder.btnSaveBookmark, true, context);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (cardClickListener != null) {
                cardClickListener.onCardClick(restaurant);
            }
        });
    }

    private void updateBookmarkFlagState(ImageView btn, boolean isSelected, Context context) {
        if (btn == null || context == null) return;
        if (isSelected) {
            btn.setBackgroundResource(R.drawable.bg_bookmark_saved); // Tô đậm lá cờ màu xanh
            btn.setColorFilter(context.getResources().getColor(R.color.white));
        } else {
            btn.setBackgroundResource(R.drawable.bg_bookmark_unsaved); // Khung rỗng viền xanh
            btn.setColorFilter(context.getResources().getColor(R.color.forest_green));
        }
    }

    @Override
    public int getItemCount() {
        return restaurantList != null ? restaurantList.size() : 0;
    }

    public static class AddViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRestaurant;
        ImageView btnSaveBookmark;
        TextView tvName, tvRating, tvSub, tag1, tag2, tagMatch;

        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRestaurant = itemView.findViewById(R.id.imgRestaurant);
            btnSaveBookmark = itemView.findViewById(R.id.btnSaveBookmark);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvRating = itemView.findViewById(R.id.tvRestaurantRating);
            tvSub = itemView.findViewById(R.id.tvRestaurantSub);
            tag1 = itemView.findViewById(R.id.tag1);
            tag2 = itemView.findViewById(R.id.tag2);
            tagMatch = itemView.findViewById(R.id.tagMatch);
        }
    }
}
