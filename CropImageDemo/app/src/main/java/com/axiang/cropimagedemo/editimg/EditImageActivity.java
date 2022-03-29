package com.axiang.cropimagedemo.editimg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.axiang.cropimagedemo.editimg.crop.CropFragment;
import com.axiang.cropimagedemo.editimg.sticker.StickerFragment;
import com.axiang.cropimagedemo.util.FileUtil;
import com.axiang.cropimagedemo.view.ScrollableViewPager;
import com.axiang.cropimagedemo.view.crop.CropImageView;
import com.axiang.cropimagedemo.view.imagezoom.ImageViewTouch;
import com.axiang.cropimagedemo.view.imagezoom.ImageViewTouchBase;
import com.axiang.cropimagedemo.view.sticker.StickerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EditImageActivity extends AppCompatActivity {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Mode.NONE, Mode.STICKERS, Mode.CROP})
    public @interface Mode {
        int NONE = 0;
        int STICKERS = 1;   // 贴图模式
        int CROP = 2;   // 裁剪模式
    }

    public static final String INTENT_IMAGE_PATH = "image_path";
    public static final String INTENT_SAVE_FILE_PATH = "save_file_path";

    private ImageView mIvBack;
    private TextView mTvSave;
    private TextView mTvApply;

    public ViewFlipper mViewFlipperSave;
    public ImageViewTouch mMainImageView;
    public ScrollableViewPager mBottomOperateBar;  // 底部操作栏

    public Bitmap mMainBitmap;  // 底层显示 Bitmap

    private String mImagePath;    // 需要编辑的图片路径
    private String mSaveFilePath; // 保存新图片的路径
    private SaveImageTask mSaveImageTask;

    public MainMenuFragment mMainMenuFragment;  // 底部操作栏 Fragment

    public StickerFragment mStickerFragment;    // 贴图 Fragment
    public StickerView mStickerView;

    public CropFragment mCropFragment; // 裁剪 Fragment
    public CropImageView mCropImageView;

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
        mCropImageView = findViewById(R.id.crop_image_view);

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
        mCropFragment = CropFragment.newInstance();
    }

    private void registerObserver() {
        getLifecycle().addObserver(mMainMenuFragment);
        getLifecycle().addObserver(mStickerFragment);
        getLifecycle().addObserver(mCropFragment);
    }

    private void loadImage() {
        Glide.with(this)
                .asBitmap()
                .load(Uri.parse("file://" + mImagePath))
                .fitCenter()
                .into(new BitmapImageViewTarget(mMainImageView) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mMainBitmap = resource;
                        super.onResourceReady(resource, transition);
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
            case Mode.CROP:
                mCropFragment.applyCropImage();
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
        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }

        mSaveImageTask = new SaveImageTask(this, mSaveFilePath, this::backToMain);
        mSaveImageTask.execute(mMainBitmap);
    }

    public void changeMainBitmap(Bitmap newBitmap) {
        if (newBitmap == null || mMainBitmap == newBitmap) {
            return;
        }

        mMainBitmap = newBitmap;
        // Glide是异步的，这里不适用，改用 setImageBitmap
        mMainImageView.setImageBitmap(mMainBitmap);
        mMainImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
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
                case CropFragment.INDEX:
                    return mCropFragment;
                case StickerFragment.INDEX:
                    return mStickerFragment;
                case MainMenuFragment.INDEX:
                default:
                    return mMainMenuFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
