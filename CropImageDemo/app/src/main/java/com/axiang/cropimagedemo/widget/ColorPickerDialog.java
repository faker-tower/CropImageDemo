package com.axiang.cropimagedemo.widget;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.axiang.cropimagedemo.R;

/**
 * 文字颜色选择器
 */
public class ColorPickerDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "ColorPickerDialog";
    private static final String KEY_RED = "key_red";
    private static final String KEY_GREEN = "key_green";
    private static final String KEY_BLUE = "key_blue";
    private static final String COLOR_CODE_STRING_FORMAT = "#%02x%02x%02x";
    private static final String COLOR_TEN_STRING_FORMAT = "  %d";
    private static final String COLOR_HUNDREDS_STRING_FORMAT = " %d";

    private View mViewColorPreview;
    private SeekBar mSeekBarRed, mSeekBarGreen, mSeekBarBlue;
    private TextView mTvRedColorTip, mTvGreenColorTip, mTvBlueColorTip;
    private EditText mEtColorCode;
    private AppCompatButton mBtnColorSelect;

    private int mSeekBarLeft;
    private final Rect mThumbRect = new Rect();    // Seekbar的进度条Rect，避免多次创建

    private int mRed = 255, mGreen, mBlue;
    private OnSelectListener mOnSelectListener;

    public static void show(@NonNull FragmentManager fragmentManager,
                            int red,
                            int green,
                            int blue,
                            OnSelectListener onSelectListener) {
        ColorPickerDialog dialog = new ColorPickerDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_RED, red);
        bundle.putInt(KEY_GREEN, green);
        bundle.putInt(KEY_BLUE, blue);
        dialog.setArguments(bundle);
        dialog.setOnSelectListener(onSelectListener);
        dialog.show(fragmentManager, TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundle();
    }

    private void getBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }

        mRed = bundle.getInt(KEY_RED);
        mGreen = bundle.getInt(KEY_GREEN);
        mBlue = bundle.getInt(KEY_BLUE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_color_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(@NonNull View rootView) {
        mViewColorPreview = rootView.findViewById(R.id.view_color_preview);
        mSeekBarRed = rootView.findViewById(R.id.seekbar_red);
        mSeekBarGreen = rootView.findViewById(R.id.seekbar_green);
        mSeekBarBlue = rootView.findViewById(R.id.seekbar_blue);
        mTvRedColorTip = rootView.findViewById(R.id.tv_red_color_tip);
        mTvGreenColorTip = rootView.findViewById(R.id.tv_green_color_tip);
        mTvBlueColorTip = rootView.findViewById(R.id.tv_blue_color_tip);
        mEtColorCode = rootView.findViewById(R.id.et_color_code);
        mBtnColorSelect = rootView.findViewById(R.id.btn_color_select);

        mSeekBarLeft = mSeekBarRed.getPaddingLeft();

        mSeekBarRed.setOnSeekBarChangeListener(this);
        mSeekBarGreen.setOnSeekBarChangeListener(this);
        mSeekBarBlue.setOnSeekBarChangeListener(this);

        mSeekBarRed.setProgress(mRed);
        mSeekBarGreen.setProgress(mGreen);
        mSeekBarBlue.setProgress(mBlue);

        mViewColorPreview.setBackgroundColor(Color.rgb(mRed, mGreen, mBlue));
        mEtColorCode.setText(String.format(COLOR_CODE_STRING_FORMAT, mRed, mGreen, mBlue).toUpperCase());

        mBtnColorSelect.setOnClickListener(view -> {
            if (mOnSelectListener != null) {
                mOnSelectListener.onSelect(mRed, mGreen, mBlue);
            }
            dismissAllowingStateLoss();
        });

        rootView.post(() -> {
            if (!isAdded()) {
                return;
            }

            setColorTip(mSeekBarRed, mTvRedColorTip, mRed);
            setColorTip(mSeekBarGreen, mTvGreenColorTip, mGreen);
            setColorTip(mSeekBarBlue, mTvBlueColorTip, mBlue);
        });
    }

    private void setColorTip(@NonNull SeekBar colorSeekBar, @NonNull TextView colorTipView, int color) {
        mThumbRect.set(colorSeekBar.getThumb().getBounds());
        colorTipView.setX(mSeekBarLeft + mThumbRect.left);
        setColorTipText(colorTipView, color);
    }

    @SuppressLint("DefaultLocale")
    private void setColorTipText(@NonNull TextView colorTipView, int color) {
        if (color < 10) {
            colorTipView.setText(String.format(COLOR_TEN_STRING_FORMAT, color));
        } else if (color < 100) {
            colorTipView.setText(String.format(COLOR_HUNDREDS_STRING_FORMAT, color));
        } else {
            colorTipView.setText(String.valueOf(color));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (seekBar.getId() == R.id.seekbar_red) {
            mRed = progress;
            setColorTip(mSeekBarRed, mTvRedColorTip, mRed);
        } else if (seekBar.getId() == R.id.seekbar_green) {
            mGreen = progress;
            setColorTip(mSeekBarGreen, mTvGreenColorTip, mGreen);
        } else if (seekBar.getId() == R.id.seekbar_blue) {
            mBlue = progress;
            setColorTip(mSeekBarBlue, mTvBlueColorTip, mBlue);
        }
        mViewColorPreview.setBackgroundColor(Color.rgb(mRed, mGreen, mBlue));
        mEtColorCode.setText(String.format(COLOR_CODE_STRING_FORMAT, mRed, mGreen, mBlue).toUpperCase());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
    }

    public interface OnSelectListener {

        void onSelect(int red, int green, int blue);
    }
}
