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
        if (holder.tvRating != null) holder.tvRating.setText(String.valueOf(restaurant.getRating()));
        if (holder.tvSub != null) holder.tvSub.setText(restaurant.getDistance() + " • " + restaurant.getPriceRange());
        if (holder.tag1 != null) holder.tag1.setText(" " + restaurant.getTag1() + " ");
        if (holder.tag2 != null) holder.tag2.setText(" " + restaurant.getTag2() + " ");
        if (holder.tagMatch != null) holder.tagMatch.setText(" Khớp món ");

        if (holder.imgRestaurant != null) {
            Glide.with(holder.itemView.getContext())
                    .load(restaurant.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgRestaurant);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestaurantClick(restaurant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList != null ? restaurantList.size() : 0;
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRestaurant;
        TextView tvName, tvRating, tvSub, tag1, tag2, tagMatch;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRestaurant = itemView.findViewById(R.id.imgRestaurant);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvRating = itemView.findViewById(R.id.tvRestaurantRating);
            tvSub = itemView.findViewById(R.id.tvRestaurantSub);
            tag1 = itemView.findViewById(R.id.tag1);
            tag2 = itemView.findViewById(R.id.tag2);
            tagMatch = itemView.findViewById(R.id.tagMatch);
        }
    }
}
