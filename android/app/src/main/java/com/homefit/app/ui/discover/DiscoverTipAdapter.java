package com.homefit.app.ui.discover;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.homefit.app.R;
import com.homefit.app.data.remote.dto.DiscoverDto;
import com.homefit.app.ui.common.MediaLoader;
import com.homefit.app.util.LocalizationHelper;

import java.util.ArrayList;
import java.util.List;

public class DiscoverTipAdapter extends RecyclerView.Adapter<DiscoverTipAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(DiscoverDto item);
    }

    private final Context context;
    private final List<DiscoverDto> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public DiscoverTipAdapter(@NonNull Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<DiscoverDto> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discover_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiscoverDto item = items.get(position);
        String lang = LocalizationHelper.getLanguage(context);

        holder.tvTitle.setText(item.getTitle(lang));
        holder.tvDescription.setText(item.getDescriptionText());

        if (item.category != null && !item.category.trim().isEmpty()) {
            holder.tvCategory.setText(item.category);
        } else {
            holder.tvCategory.setText(R.string.discover_tips_section);
        }

        String coverUrl = MediaLoader.resolveUrl(item.getCover());
        if (!coverUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(coverUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_media_placeholder)
                    .error(R.drawable.ic_workout_placeholder)
                    .centerCrop()
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.bg_media_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvCategory;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvReadMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReadMore = itemView.findViewById(R.id.tvReadMore);
        }
    }
}
