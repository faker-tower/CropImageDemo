package com.axiang.cropimagedemo.selectpic;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.ToastUtil;
import com.axiang.cropimagedemo.widget.GridItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ImageFragment extends Fragment {

    public static final int IMAGE_SPAN_COUNT = 3;

    private RecyclerView mRvImage;
    private ProgressBar mProgressBarLoading;

    private ImageAdapter mImageAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);
        initView(rootView);
        initData();
        return rootView;
    }

    private void initView(View rootView) {
        mRvImage = rootView.findViewById(R.id.rv_image);
        mProgressBarLoading = rootView.findViewById(R.id.progress_bar_loading);

        mRvImage.setLayoutManager(new GridLayoutManager(getActivity(), IMAGE_SPAN_COUNT));
        int spacing = getResources().getDimensionPixelOffset(R.dimen.fab_margin) / 2;
        mRvImage.addItemDecoration(new GridItemDecoration(IMAGE_SPAN_COUNT, spacing, spacing));
        mImageAdapter = new ImageAdapter(getActivity());
        mRvImage.setAdapter(mImageAdapter);
    }

    private void initData() {
        mRvImage.setVisibility(View.GONE);
        mProgressBarLoading.setVisibility(View.VISIBLE);

        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        new ImageAsyncTask(this).execute(bundle.getInt("bucketId", 0));
    }

    private void show(List<ImageItem> imageItems) {
        mProgressBarLoading.setVisibility(View.GONE);
        mRvImage.setVisibility(View.VISIBLE);

        if (imageItems == null || imageItems.isEmpty()) {
            ToastUtil.showShort("找不到图片");
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        mImageAdapter.setImageItemList(imageItems);
        mImageAdapter.notifyItemRangeInserted(0, imageItems.size());
        mImageAdapter.setOnClickListener(position -> {
            if (getActivity() != null && getActivity() instanceof SelectPicActivity) {
                ((SelectPicActivity) getActivity()).picSelected(
                        imageItems.get(position).path,
                        imageItems.get(position).imageTaken,
                        imageItems.get(position).imageSize);
            }
        });
    }

    private static class ImageAsyncTask extends AsyncTask<Integer, Void, List<ImageItem>> {

        private final WeakReference<ImageFragment> mWeakReference;

        ImageAsyncTask(ImageFragment imageFragment) {
            super();
            mWeakReference = new WeakReference<>(imageFragment);
        }

        @Override
        protected List<ImageItem> doInBackground(Integer... integers) {
            if (mWeakReference == null || mWeakReference.get() == null) {
                return null;
            }
            Activity activity = mWeakReference.get().getActivity();
            if (activity == null) {
                return null;
            }
            ContentResolver resolver = activity.getContentResolver();
            if (resolver == null) {
                return null;
            }

            if (integers == null || integers.length <= 0) {
                return null;
            }

            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA,
                            MediaStore.Images.Media.DATE_TAKEN,
                            MediaStore.Images.Media.SIZE},
                    MediaStore.Images.Media.BUCKET_ID + " = ?",
                    new String[]{String.valueOf(integers[0])},
                    MediaStore.Images.Media.DATE_MODIFIED + " DESC");

            List<ImageItem> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                result.add(new ImageItem(cursor.getString(0), cursor.getString(1), cursor.getLong(2)));
            }
            cursor.close();
            return result;
        }


        @Override
        protected void onPostExecute(List<ImageItem> imageItems) {
            if (mWeakReference == null || mWeakReference.get() == null) {
                return;
            }
            ImageFragment imageFragment = mWeakReference.get();
            if (imageFragment.isAdded() && !imageFragment.isHidden()) {
                imageFragment.show(imageItems);
            }
        }
    }
}
