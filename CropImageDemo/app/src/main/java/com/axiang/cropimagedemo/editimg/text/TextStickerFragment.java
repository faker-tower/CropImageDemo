package com.axiang.cropimagedemo.editimg.text;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.axiang.cropimagedemo.ModuleConfig;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.editimg.BaseEditImageFragment;
import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.util.DialogUtil;

public class TextStickerFragment extends BaseEditImageFragment implements TextWatcher {

    public static final int INDEX = ModuleConfig.INDEX_TEXT;

    private ImageView mIvBackToMain;
    private AppCompatEditText mEtInputText;
    private ImageView mIvColorSelector;

    private int mRed, mGreen, mBlue;
    private InputMethodManager mInputMethodManager;

    private String mEtHint = "请输入文字";

    public static TextStickerFragment newInstance() {
        return new TextStickerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_text_sticker, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mIvBackToMain = rootView.findViewById(R.id.iv_back_to_main);
        mEtInputText = rootView.findViewById(R.id.et_input_text);
        mIvColorSelector = rootView.findViewById(R.id.iv_color_selector);

        mEtInputText.addTextChangedListener(this);
        mActivity.mTextStickerView.setEditText(mEtInputText);

        mInputMethodManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        changeTextColor(45, 215, 215);

        mIvBackToMain.setOnClickListener(view -> backToMain());
        mIvColorSelector.setOnClickListener(view -> openColorSelector());
    }

    /**
     * 打开文字颜色选择器
     */
    private void openColorSelector() {
        DialogUtil.showColorPickerDialog(mActivity.getSupportFragmentManager(),
                mRed,
                mGreen,
                mBlue,
                this::changeTextColor);
    }

    /**
     * 修改字体颜色
     */
    private void changeTextColor(int red, int green, int blue) {
        mRed = red;
        mGreen = green;
        mBlue = blue;
        int color = Color.rgb(mRed, mGreen, mBlue);
        mIvColorSelector.setBackgroundColor(color);
        mActivity.mTextStickerView.setTextColor(color);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        mActivity.mTextStickerView.setText(editable.toString().trim());
    }

    @Override
    public void onShow() {
        mActivity.mMode = EditImageActivity.Mode.TEXT;
        mActivity.mTextStickerView.setVisibility(View.VISIBLE);
        mActivity.mViewFlipperSave.showNext();
    }

    @Override
    public void backToMain() {
        hideInput();
        mActivity.mMode = EditImageActivity.Mode.NONE;
        mActivity.mBottomOperateBar.setCurrentItem(0);
        mActivity.mStickerView.setVisibility(View.GONE);
        mActivity.mViewFlipperSave.showPrevious();
    }

    public void hideInput() {
        if (mActivity == null || mActivity.isFinishing() || !isAdded()) {
            return;
        }
        if (mActivity.getCurrentFocus() == null || !isInputMethodShow()) {
            return;
        }
        mInputMethodManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public boolean isInputMethodShow() {
        return mInputMethodManager.isActive();
    }

    /**
     * 保存文字贴图图片
     */
    public void applyTextStickers() {
        
    }
}
