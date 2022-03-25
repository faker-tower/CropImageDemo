package com.axiang.cropimagedemo.editimg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.editimg.sticker.StickerFragment;
import com.axiang.cropimagedemo.util.FileUtil;
import com.axiang.cropimagedemo.view.ScrollableViewPager;
import com.axiang.cropimagedemo.view.imagezoom.ImageViewTouch;
import com.axiang.cropimagedemo.view.sticker.StickerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EditImageActivity extends AppCompatActivity {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Mode.NONE, Mode.STICKERS})
    public @interface Mode {
        int NONE = 0;
        int STICKERS = 1;   // 贴图模式
    }

    public static final String INTENT_IMAGE_PATH = "image_path";
    public static final String INTENT_SAVE_FILE_PATH = "save_file_path";

    private ImageView mIvBack;
    private TextView mTvSave;
    private TextView mTvApply;

    public ViewFlipper mViewFlipperSave;
    public StickerView mStickerView;    // 贴图层 View
    public ImageViewTouch mMainImageView;
    public ScrollableViewPager mBottomOperateBar;  // 底部操作栏

    public Bitmap mMainBitmap;  // 底层显示Bitmap

    private String mImagePath;    // 需要编辑的图片路径
    private String mSaveFilePath; // 保存新图片的路径
    private SavePicTask mSavePicTask;

    public MainMenuFragment mMainMenuFragment;  // 底部操作栏 Fragment
    public StickerFragment mStickerFragment;    // 贴图 Fragment

    public int mMode = Mode.NONE;  // 当前操作模式

    public static void startActivityForResult(@NonNull Activity activity, String imagePath, String saveFilePath, int requestCode) {
        Intent intent = new Intent(activity, EditImageActivity.class);
        intent.putExtra(INTENT_IMAGE_PATH, imagePath);
        intent.putExtra(INTENT_SAVE_FILE_PATH, saveFilePath);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        getIntentData();
        initView();
        loadImage();
        registerObserver();
    }

    private void getIntentData() {
        mImagePath = getIntent().getStringExtra(INTENT_IMAGE_PATH);
        mSaveFilePath = getIntent().getStringExtra(INTENT_SAVE_FILE_PATH);
    }

    private void initView() {
        mIvBack = findViewById(R.id.iv_back);
        mViewFlipperSave = findViewById(R.id.view_flipper_save);
        mTvSave = findViewById(R.id.tv_save);
        mTvApply = findViewById(R.id.tv_apply);
        mMainImageView = findViewById(R.id.view_main_image);
        mStickerView = findViewById(R.id.sticker_view);
        mBottomOperateBar = findViewById(R.id.vp_bottom_operate_bar);

        mViewFlipperSave.setInAnimation(this, R.anim.in_bottom_to_top);
        mViewFlipperSave.setOutAnimation(this, R.anim.out_bottom_to_top);

        initFragments();

        mBottomOperateBar.setAdapter(new BottomOperateBarAdapter(getSupportFragmentManager()));

        mIvBack.setOnClickListener(view -> confirmBack());
        mTvApply.setOnClickListener(view -> onApplyClick());
        mTvSave.setOnClickListener(view -> onSaveClick());
    }

    private void initFragments() {
        mMainMenuFragment = MainMenuFragment.newInstance();
        mStickerFragment = StickerFragment.newInstance();
    }

    private void registerObserver() {
        getLifecycle().addObserver(mMainMenuFragment);
        getLifecycle().addObserver(mStickerFragment);
    }

    private void loadImage() {
        Glide.with(this)
                .asBitmap()
                .load(Uri.parse("file://" + mImagePath))
                .fitCenter()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mMainBitmap = resource;
                        mMainImageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void confirmBack() {
        finish();
    }

    private void onApplyClick() {
        switch (mMode) {
            case Mode.STICKERS:
                mStickerFragment.applyStickers();
                break;
        }
    }

    /**
     * 保存按钮 点击退出
     */
    private void onSaveClick() {
        doSaveImage();
    }

    private void doSaveImage() {
        if (mSavePicTask != null) {
            mSavePicTask.cancel(true);
        }

        mSavePicTask = new SavePicTask(this, mSaveFilePath);
        mSavePicTask.execute(mMainBitmap);
    }

    public void changeMainBitmap(Bitmap newBitmap) {
        if (newBitmap == null || mMainBitmap == newBitmap) {
            return;
        }

        mMainBitmap = newBitmap;
        Glide.with(this).load(mMainBitmap).fitCenter().into(mMainImageView);
    }

    /**
     * 保存图片完成，返回主页加载新的图片
     */
    public void backToMain() {
        FileUtil.addPicToAlbum(mSaveFilePath);
        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_IMAGE_PATH, mImagePath);
        returnIntent.putExtra(INTENT_SAVE_FILE_PATH, mSaveFilePath);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            confirmBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private final class BottomOperateBarAdapter extends FragmentPagerAdapter {

        public BottomOperateBarAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case StickerFragment.INDEX:
                    return mStickerFragment;
                case MainMenuFragment.INDEX:
                default:
                    return mMainMenuFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private static class SavePicTask extends SaveImageTask {

        public SavePicTask(@NonNull EditImageActivity activity, String saveFilePath) {
            super(activity, saveFilePath);
        }

        @Override
        public void onPostResult() {
            EditImageActivity editImageActivity = mEditImageActReference.get();
            if (editImageActivity == null || editImageActivity.isFinishing()) {
                return;
            }
            editImageActivity.backToMain();
        }
    }
}
