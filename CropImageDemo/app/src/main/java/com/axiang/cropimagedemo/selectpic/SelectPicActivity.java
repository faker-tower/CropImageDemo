package com.axiang.cropimagedemo.selectpic;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class SelectPicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        Fragment newFragment = new BucketsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, newFragment);
        transaction.commit();
    }

    public void showBucket(final int bucketId) {
        Bundle bundle = new Bundle();
        bundle.putInt("bucketId", bucketId);
        Fragment imageFragment = new ImageFragment();
        imageFragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, imageFragment)
                .addToBackStack(null)
                .commit();
    }

    public void picSelected(final String imgPath, final String imgTaken, final long imageSize) {
        returnResult(imgPath, imgTaken, imageSize);
    }

    private void returnResult(final String imgPath, final String imageTaken, final long imageSize) {
        Intent result = new Intent();
        result.putExtra("imgPath", imgPath);
        result.putExtra("dateTaken", imageTaken);
        result.putExtra("imageSize", imageSize);
        setResult(RESULT_OK, result);
        finish();
    }
}