package com.axiang.cropimagedemo.editimg.crop;

public class RatioItem {

    private String mText;
    private float mRatio;
    private int mIndex;

    public RatioItem(String text, float ratio) {
        super();
        mText = text;
        mRatio = ratio;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public float getRatio() {
        return mRatio;
    }

    public void setRatio(float ratio) {
        mRatio = ratio;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }
}
