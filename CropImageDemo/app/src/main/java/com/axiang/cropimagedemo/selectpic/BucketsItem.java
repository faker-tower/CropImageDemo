package com.axiang.cropimagedemo.selectpic;

public class BucketsItem {

    final int bucketsId;
    final String name;
    final String path;
    int images = 1;

    public BucketsItem(int bucketsId, String name, String path) {
        this.bucketsId = bucketsId;
        this.name = name;
        this.path = path;
    }
}
