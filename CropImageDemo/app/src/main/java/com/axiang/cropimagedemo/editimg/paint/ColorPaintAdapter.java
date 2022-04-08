package com.axiang.cropimagedemo.editimg.paint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.axiang.cropimagedemo.R;

/**
 * Created by 邱翔威 on 2022/4/1
 */
public class ColorPaintAdapter extends RecyclerView.Adapter<ColorPaintAdapter.ViewHolder> {

    private final Context mContext;
    private final int[] mColors;

    private OnItemClickListener mListener;

    public ColorPaintAdapter(Context context, int[] colors) {
        mContext = context;
        mColors = colors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_color_paint, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == mColors.length - 1) {
            Drawable drawable = VectorDrawableCompat.create(mContext.getResources(), mColors[position], mContext.getTheme());
            holder.mIvColor.setBackground(drawable);
        } else {
            holder.mIvColor.setBackgroundColor(mColors[position]);
        }

        holder.mIvColor.setOnClickListener(view -> {
            if (mListener == null) {
                return;
            }

            if (position == mColors.length - 1) {
                mListener.onMoreClick();
            } else {
                mListener.onColorClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColors == null ? 0 : mColors.length;
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

        void onColorClick(int position);

        void onMoreClick();
    }
}
