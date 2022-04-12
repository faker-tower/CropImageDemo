package com.axiang.cropimagedemo.editimg.magic;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.BitmapUtil;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicAdapter extends RecyclerView.Adapter<MagicAdapter.ViewHolder> {

    private final Context mContext;
    private List<MagicData> mMagicDataList;

    private OnItemClickListener mListener;

    public MagicAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_magic, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MagicData data = mMagicDataList.get(position);
        if (data.isFromZip()) {
            MagicData.FrameMeta meta = data.getFrameMetaList().get(0);
            Glide.with(mContext).load(new File(meta.getFrameImagePath())).into(holder.mIvMagic);
        } else {
            holder.mIvMagic.setImageBitmap(BitmapUtil.getBitmapFromAssetsFile(mContext, data.getAssetsThumb()));
        }

        holder.mIvMagic.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onImageClick(mMagicDataList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMagicDataList == null || mMagicDataList.isEmpty() ? 0 : mMagicDataList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setMagicDataList(List<MagicData> magicDataList) {
        mMagicDataList = magicDataList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIvMagic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvMagic = itemView.findViewById(R.id.iv_magic);
        }
    }

    public interface OnItemClickListener {

        void onImageClick(MagicData data);
    }
}
