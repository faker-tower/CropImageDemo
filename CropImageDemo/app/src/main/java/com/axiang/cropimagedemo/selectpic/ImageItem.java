package com.axiang.cropimagedemo.selectpic;

public class ImageItem {

    final String path;
    final String imageTaken;
    final long imageSize;

    public ImageItem(final String path, final String imageTaken, final long imageSize) {
        this.path = path;
        this.imageTaken = imageTaken;
        this.imageSize = imageSize;
    }
}
