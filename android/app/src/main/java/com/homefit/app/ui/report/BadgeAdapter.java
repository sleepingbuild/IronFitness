package com.homefit.app.ui.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.homefit.app.R;

import java.util.ArrayList;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    public static class BadgeItem {
        public String icon;
        public String title;
        public String description;
        public boolean isUnlocked;

        public BadgeItem(String icon, String title, String description, boolean isUnlocked) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.isUnlocked = isUnlocked;
        }
    }

    private final List<BadgeItem> items = new ArrayList<>();

    public void submitList(List<BadgeItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBadgeIcon;
        private final TextView tvBadgeTitle;
        private final TextView tvBadgeDesc;
        private final TextView tvBadgeStatus;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadgeIcon = itemView.findViewById(R.id.tvBadgeIcon);
            tvBadgeTitle = itemView.findViewById(R.id.tvBadgeTitle);
            tvBadgeDesc = itemView.findViewById(R.id.tvBadgeDesc);
            tvBadgeStatus = itemView.findViewById(R.id.tvBadgeStatus);
        }

        public void bind(BadgeItem item) {
            tvBadgeIcon.setText(item.icon);
            tvBadgeTitle.setText(item.title);
            tvBadgeDesc.setText(item.description);

            if (item.isUnlocked) {
                tvBadgeStatus.setText("Đã mở khóa ✨");
                tvBadgeStatus.setBackgroundResource(R.drawable.bg_chip_active);
                tvBadgeStatus.setTextColor(itemView.getContext().getColor(R.color.hf_primary));
                itemView.setAlpha(1.0f);
            } else {
                tvBadgeStatus.setText("Chưa đạt 🔒");
                tvBadgeStatus.setBackgroundResource(R.drawable.bg_chip_inactive);
                tvBadgeStatus.setTextColor(itemView.getContext().getColor(R.color.hf_on_surface_variant));
                itemView.setAlpha(0.6f);
            }
        }
    }
}
