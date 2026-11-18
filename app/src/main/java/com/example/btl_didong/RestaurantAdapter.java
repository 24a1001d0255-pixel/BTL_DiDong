package com.example.btl_didong;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    private List<Restaurant> restaurantList;
    private OnRestaurantClickListener listener;

    public RestaurantAdapter(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public RestaurantAdapter(List<Restaurant> restaurantList, OnRestaurantClickListener listener) {
        this.restaurantList = restaurantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_card, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        if (holder.tvName != null) holder.tvName.setText(restaurant.getName());
        if (holder.tvRating != null) holder.tvRating.setText("★ " + restaurant.getRating());
        if (holder.tvSub != null) holder.tvSub.setText(restaurant.getDistance() + " • " + restaurant.getPriceRange());
        if (holder.tag1 != null) holder.tag1.setText(restaurant.getTag1());
        if (holder.tag2 != null) holder.tag2.setText("Hoàng Mai");
        if (holder.tagMatch != null) holder.tagMatch.setText("Khớp " + restaurant.getTag1());

        if (holder.imgRestaurant != null) {
            Glide.with(holder.itemView.getContext())
                    .load(restaurant.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgRestaurant);
        }

        if (holder.btnSaveBookmark != null) {
            Context ctx = holder.itemView.getContext();
            SharedPreferences prefs = ctx.getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
            String savedStr = prefs.getString("saved_restaurants", "");
            String savedFoodsStr = prefs.getString("saved_foods", "");
            boolean isSaved = isRestaurantSaved(savedStr, restaurant.getName()) || isRestaurantSaved(savedFoodsStr, restaurant.getName());

            updateBookmarkUI(holder.btnSaveBookmark, isSaved, ctx);

            holder.btnSaveBookmark.setOnClickListener(v -> {
                SharedPreferences sp = v.getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                String currentStr = sp.getString("saved_restaurants", "");
                String currentFoodsStr = sp.getString("saved_foods", "");
                boolean currentlySaved = isRestaurantSaved(currentStr, restaurant.getName()) || isRestaurantSaved(currentFoodsStr, restaurant.getName());
                boolean nowSaved;

                if (!currentlySaved) {
                    currentStr = restaurant.getName() + ";" + currentStr;
                    currentFoodsStr = restaurant.getName() + ";" + currentFoodsStr;
                    nowSaved = true;
                    Toast.makeText(v.getContext(), "⭐ Đã lưu: " + restaurant.getName() + " vào Bộ sưu tập!", Toast.LENGTH_SHORT).show();
                } else {
                    // Ấn lần 2 ➔ Bỏ lưu khỏi Bộ sưu tập
                    currentStr = removeRestaurantFromSaved(currentStr, restaurant.getName());
                    currentFoodsStr = removeRestaurantFromSaved(currentFoodsStr, restaurant.getName());
                    nowSaved = false;
                    Toast.makeText(v.getContext(), "Đã bỏ lưu: " + restaurant.getName(), Toast.LENGTH_SHORT).show();

                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && restaurantList != null && restaurantList.size() > pos) {
                        restaurantList.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, restaurantList.size());
                    }
                }

                sp.edit()
                        .putString("saved_restaurants", currentStr)
                        .putString("saved_foods", currentFoodsStr)
                        .apply();

                updateBookmarkUI(holder.btnSaveBookmark, nowSaved, v.getContext());
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestaurantClick(restaurant);
            }
        });
    }

    private boolean isRestaurantSaved(String savedString, String name) {
        if (savedString == null || name == null) return false;
        String[] parts = savedString.split(";");
        for (String part : parts) {
            if (part.trim().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private String removeRestaurantFromSaved(String savedString, String name) {
        if (savedString == null || name == null) return "";
        String[] parts = savedString.split(";");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.trim().isEmpty() && !part.trim().equalsIgnoreCase(name.trim())) {
                sb.append(part.trim()).append(";");
            }
        }
        return sb.toString();
    }

    private void updateBookmarkUI(ImageView btn, boolean isSaved, Context context) {
        if (btn == null || context == null) return;
        if (isSaved) {
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

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRestaurant;
        ImageView btnSaveBookmark;
        TextView tvName, tvRating, tvSub, tag1, tag2, tagMatch;

        public RestaurantViewHolder(@NonNull View itemView) {
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
