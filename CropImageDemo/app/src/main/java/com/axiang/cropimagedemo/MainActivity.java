package com.axiang.cropimagedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.selectpic.SelectPicActivity;
import com.axiang.cropimagedemo.util.FileUtil;
import com.bumptech.glide.Glide;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    public static final int CODE_REQUEST_PERMISSION = 123;
    public static final int CODE_SELECT_ALBUM_IMAGE = 124;
    public static final int CODE_EDIT_IMAGE = 125;

    private ImageView mIvImage;
    private Button mBtnToAlbum;
    private Button mBtnEditPic;

    private int mDisplayWidth;
    private int mDisplayHeight;

    private String mImgPath;
    private Bitmap mMainBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mIvImage = findViewById(R.id.iv_image);
        mBtnToAlbum = findViewById(R.id.btn_to_album);
        mBtnEditPic = findViewById(R.id.btn_edit_pic);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDisplayWidth = metrics.widthPixels;
        mDisplayHeight = metrics.heightPixels;

        mBtnToAlbum.setOnClickListener(view -> toAlbum());
        mBtnEditPic.setOnClickListener(view -> toEditPic());
    }

    private void toAlbum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openAlbumWithCheckPermission();
        } else {
            openAlbum();
        }
    }

    private void openAlbumWithCheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_REQUEST_PERMISSION);
        } else {
            openAlbum();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CODE_REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
                return;
            }
            Toast.makeText(this, "需要权限才能打开相册", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void openAlbum() {
        startActivityForResult(new Intent(this, SelectPicActivity.class),
                CODE_SELECT_ALBUM_IMAGE);
    }

    private void toEditPic() {
        if (TextUtils.isEmpty(mImgPath)) {
            Toast.makeText(this, "需要先选择一张图片才能去编辑", Toast.LENGTH_SHORT).show();
            return;
        }

        File outputFile = FileUtil.genEditFile();
        Intent it = new Intent(this, EditImageActivity.class);
        it.putExtra("imgPath", mImgPath);
        it.putExtra("outputFilePath", outputFile.getAbsolutePath());
        startActivityForResult(it, CODE_EDIT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODE_SELECT_ALBUM_IMAGE:
                    if (data != null) {
                        handleSelectPic(data);
                    }
                    break;
                case CODE_EDIT_IMAGE:

            }
        }
    }

    private void handleSelectPic(Intent data) {
        mImgPath = data.getStringExtra("imgPath");
        Log.d(TAG, "select picture path: " + mImgPath);
        Glide.with(this)
                .load(Uri.parse("file://" + mImgPath))
                .override(mDisplayWidth / 2, mDisplayHeight / 2)
                .into(mIvImage);
    }
}