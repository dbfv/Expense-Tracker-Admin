package com.example.expensetrackeradmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.expensetrackeradmin.R;

import java.util.ArrayList;
import java.util.List;

public class ExpenseImageAdapter extends RecyclerView.Adapter<ExpenseImageAdapter.ImageViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    private final List<String> imageSources = new ArrayList<>();
    private final boolean removable;
    private final OnRemoveClickListener onRemoveClickListener;

    public ExpenseImageAdapter(List<String> initialItems, boolean removable, OnRemoveClickListener onRemoveClickListener) {
        if (initialItems != null) {
            imageSources.addAll(initialItems);
        }
        this.removable = removable;
        this.onRemoveClickListener = onRemoveClickListener;
    }

    public void updateData(List<String> items) {
        imageSources.clear();
        if (items != null) {
            imageSources.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String source = imageSources.get(position);
        Glide.with(holder.itemView.getContext())
                .load(source)
                .centerCrop()
                .into(holder.ivExpenseImage);

        if (removable) {
            holder.btnRemoveImage.setVisibility(View.VISIBLE);
            holder.btnRemoveImage.setOnClickListener(v -> {
                if (onRemoveClickListener != null) {
                    onRemoveClickListener.onRemove(holder.getBindingAdapterPosition());
                }
            });
        } else {
            holder.btnRemoveImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return imageSources.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivExpenseImage;
        ImageButton btnRemoveImage;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExpenseImage = itemView.findViewById(R.id.ivExpenseImage);
            btnRemoveImage = itemView.findViewById(R.id.btnRemoveImage);
        }
    }
}
