package com.axiang.cropimagedemo.selectpic;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class BucketsAdapter extends RecyclerView.Adapter<BucketsAdapter.ViewHolder> {

    private final Context mContext;
    private List<BucketsItem> mBucketsItems;

    private OnClickListener mOnClickListener;

    public BucketsAdapter(Context context) {
        super();
        mContext = context;
    }

    public void setBucketsItems(List<BucketsItem> bucketsItems) {
        mBucketsItems = bucketsItems;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public BucketsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_buckets, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BucketsAdapter.ViewHolder holder, int position) {
        BucketsItem bi = (BucketsItem) mBucketsItems.get(position);
        holder.text.setText(bi.images > 1 ?
                bi.name + " - " + String.format("%s 张图片", bi.images) : bi.name);
        Glide.with(mContext).load(Uri.parse("file://" + bi.path)).into(holder.icon);
        holder.icon.setOnClickListener(view -> {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBucketsItems == null || mBucketsItems.isEmpty() ? 0 : mBucketsItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            text = itemView.findViewById(R.id.text);
        }
    }

    interface OnClickListener {

        void onClick(int position);
    }
}
