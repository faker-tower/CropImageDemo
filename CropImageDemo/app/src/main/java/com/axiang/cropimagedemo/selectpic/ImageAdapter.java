package com.axiang.cropimagedemo.selectpic;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final Context mContext;
    private List<ImageItem> mImageItemList;

    private OnClickListener mOnClickListener;

    public ImageAdapter(Context context) {
        mContext = context;
    }

    public void setImageItemList(List<ImageItem> imageItemList) {
        mImageItemList = imageItemList;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).load(Uri.parse("file://" + mImageItemList.get(position).path)).into(holder.icon);
        holder.icon.setOnClickListener(view -> {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageItemList == null || mImageItemList.isEmpty() ? 0 : mImageItemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    interface OnClickListener {

        void onClick(int position);
    }
}
