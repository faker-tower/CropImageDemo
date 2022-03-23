package com.axiang.cropimagedemo.editimg;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.axiang.cropimagedemo.R;

public class EditImageActivity extends AppCompatActivity {

    private String mImgPath;
    private String mOutputFilePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        initView();
        getIntentData();
    }

    private void initView() {

    }

    private void getIntentData() {
        mImgPath = getIntent().getStringExtra("imgPath");
        mOutputFilePath = getIntent().getStringExtra("outputFilePath");
        loadImage();
    }

    private void loadImage() {

    }
}
