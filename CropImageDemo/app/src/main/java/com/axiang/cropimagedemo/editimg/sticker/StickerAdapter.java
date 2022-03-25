package com.axiang.cropimagedemo.editimg.sticker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.BitmapUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {

    public static final String TAG = "StickerAdapter";

    private final Context mContext;
    private final List<String> mStickerPathList = new ArrayList<>();  // 图片路径列表

    private OnItemClickListener mOnItemClickListener;

    public StickerAdapter(Context context) {
        super();
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_sticker, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String path = mStickerPathList.get(position);
        holder.mIvSticker.setImageBitmap(BitmapUtil.getBitmapFromAssetsFile(mContext, path));

        holder.mIvSticker.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onClick(path);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStickerPathList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addStickerImages(String folderPath) {
        mStickerPathList.clear();
        try {
            String[] files = mContext.getAssets().list(folderPath);
            for (String name : files) {
                mStickerPathList.add(folderPath + File.separator + name);
            }
        } catch (IOException e) {
            Log.e(TAG, "addStickerImages 方法抛出了异常：" + e);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIvSticker;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIvSticker = itemView.findViewById(R.id.iv_sticker);
        }
    }

    interface OnItemClickListener {

        void onClick(String stickerPath);
    }
}
