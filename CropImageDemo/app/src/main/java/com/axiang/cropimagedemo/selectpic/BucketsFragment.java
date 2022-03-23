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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.widget.GridItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class BucketsFragment extends Fragment {

    public static final int BUCKETS_SPAN_COUNT = 3;

    private RecyclerView mRvBuckets;
    private ProgressBar mProgressBarLoading;

    private BucketsAdapter mBucketsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buckets, container, false);
        initView(rootView);
        initData();
        return rootView;
    }

    private void initView(View rootView) {
        mRvBuckets = rootView.findViewById(R.id.rv_buckets);
        mProgressBarLoading = rootView.findViewById(R.id.progress_bar_loading);

        mRvBuckets.setLayoutManager(new GridLayoutManager(getActivity(), BUCKETS_SPAN_COUNT));
        int spacing = getResources().getDimensionPixelOffset(R.dimen.fab_margin) / 2;
        mRvBuckets.addItemDecoration(new GridItemDecoration(BUCKETS_SPAN_COUNT, spacing, spacing));
        mBucketsAdapter = new BucketsAdapter(getActivity());
        mRvBuckets.setAdapter(mBucketsAdapter);
    }

    private void initData() {
        mRvBuckets.setVisibility(View.GONE);
        mProgressBarLoading.setVisibility(View.VISIBLE);

        new BucketsAsyncTask(this).execute();
    }

    private void show(List<BucketsItem> bucketsItems) {
        mProgressBarLoading.setVisibility(View.GONE);
        mRvBuckets.setVisibility(View.VISIBLE);

        if (bucketsItems == null || bucketsItems.isEmpty()) {
            Toast.makeText(getActivity(), "找不到图片", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        mBucketsAdapter.setBucketsItems(bucketsItems);
        mBucketsAdapter.notifyItemRangeInserted(0, bucketsItems.size());
        mBucketsAdapter.setOnClickListener(position -> {
            if (getActivity() != null && getActivity() instanceof SelectPicActivity) {
                ((SelectPicActivity) getActivity()).showBucket(bucketsItems.get(position).bucketsId);
            }
        });
    }

    private static class BucketsAsyncTask extends AsyncTask<Void, Void, List<BucketsItem>> {

        private final WeakReference<BucketsFragment> mWeakReference;

        BucketsAsyncTask(BucketsFragment bucketsFragment) {
            super();
            mWeakReference = new WeakReference<>(bucketsFragment);
        }

        @Override
        protected List<BucketsItem> doInBackground(Void... voids) {
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

            String[] projection = {
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID
            };
            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, " + MediaStore.Images.Media.DATE_MODIFIED + " DESC");
            if (cursor == null) {
                return null;
            }

            List<BucketsItem> result = new ArrayList<>();
            BucketsItem lastBucketsItem = null;
            while (cursor.moveToNext()) {
                if (lastBucketsItem == null || !lastBucketsItem.name.equals(cursor.getString(1))) {
                    lastBucketsItem = new BucketsItem(
                            cursor.getInt(2),
                            cursor.getString(1),
                            cursor.getString(0));
                    result.add(lastBucketsItem);
                } else {
                    lastBucketsItem.images++;
                }
            }
            cursor.close();
            return result;
        }


        @Override
        protected void onPostExecute(List<BucketsItem> bucketsItems) {
            if (mWeakReference == null || mWeakReference.get() == null) {
                return;
            }
            BucketsFragment bucketsFragment = mWeakReference.get();
            if (bucketsFragment.isAdded() && !bucketsFragment.isHidden()) {
                bucketsFragment.show(bucketsItems);
            }
        }
    }
}
