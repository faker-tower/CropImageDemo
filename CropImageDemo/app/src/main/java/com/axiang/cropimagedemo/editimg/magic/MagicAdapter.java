package com.axiang.cropimagedemo.editimg.magic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.BitmapUtil;

/**
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicAdapter extends RecyclerView.Adapter<MagicAdapter.ViewHolder> {

    private final Context mContext;
    private final String[] mMaterials;

    private OnItemClickListener mListener;

    public MagicAdapter(Context context, String[] materials) {
        mContext = context;
        mMaterials = materials;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_magic, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mIvMagic.setImageBitmap(BitmapUtil.getBitmapFromAssetsFile(mContext, mMaterials[position]));
        holder.mIvMagic.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onImageClick(mMaterials[position]);
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

        private final ImageView mIvMagic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvMagic = itemView.findViewById(R.id.iv_magic);
        }
    }

    public interface OnItemClickListener {

        void onImageClick(String key);
    }
}
