package com.axiang.cropimagedemo.editimg.paint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.bumptech.glide.Glide;

/**
 * Created by 邱翔威 on 2022/4/1
 */
public class ImagePaintAdapter extends RecyclerView.Adapter<ImagePaintAdapter.ViewHolder> {

    public static final String TAG = "ImagePaintAdapter";

    private final Context mContext;
    private final int[] mMaterials;

    private OnItemClickListener mListener;

    public ImagePaintAdapter(Context context, int[] materials) {
        mContext = context;
        mMaterials = materials;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_color_paint, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).load(mMaterials[position]).centerCrop().into(holder.mIvColor);
        holder.mIvColor.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onImageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMaterials == null ? 0 : mMaterials.length;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIvColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvColor = itemView.findViewById(R.id.iv_color);
        }
    }

    public interface OnItemClickListener {

        void onImageClick(int position);
    }
}
