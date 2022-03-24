package com.axiang.cropimagedemo.editimg.sticker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;

/**
 * 贴图分类列表 Adapter
 */
public class StickerTypeAdapter extends RecyclerView.Adapter<StickerTypeAdapter.ViewHolder> {

    public static final String[] sStickerPath = {"sticker/type1", "sticker/type2", "sticker/type3", "sticker/type4", "sticker/type5", "sticker/type6"};
    public static final String[] sStickerPathName = {"表情1", "表情2", "表情3", "表情4", "表情5", "表情6"};
    private final Context mContext;

    private OnItemClickListener mOnItemClickListener;

    public StickerTypeAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_sticker_type, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTypeName.setText(sStickerPathName[position]);
        holder.mTypeName.setTag(position);
        holder.mTypeName.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onClick(sStickerPath[(int) view.getTag()]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sStickerPathName.length;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTypeName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTypeName = itemView.findViewById(R.id.type_name);
        }
    }

    interface OnItemClickListener {

        void onClick(String stickerPath);
    }
}
