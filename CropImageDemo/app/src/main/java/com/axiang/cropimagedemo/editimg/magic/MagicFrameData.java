package com.axiang.cropimagedemo.editimg.magic;

import java.util.List;

/**
 * a1.json、a2.json、a3.json...axx.json 的数据
 * Created by 邱翔威 on 2022/4/11
 */
public class MagicFrameData {

    private List<Frames> frames;
    private Meta meta;

    public void setFrames(List<Frames> frames) {
        this.frames = frames;
    }

    public List<Frames> getFrames() {
        return this.frames;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Meta getMeta() {
        return this.meta;
    }

    public class Frames {

        private String filename;
        private double d;
        private Frame frame;

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return this.filename;
        }

        public void setD(double d) {
            this.d = d;
        }

        public double getD() {
            return this.d;
        }

        public void setFrame(Frame frame) {
            this.frame = frame;
        }

        public Frame getFrame() {
            return this.frame;
        }
    }

    public class Frame {

        private int x;
        private int y;
        private int w;
        private int h;

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return this.x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return this.y;
        }

        public void setW(int w) {
            this.w = w;
        }

        public int getW() {
            return this.w;
        }

        public void setH(int h) {
            this.h = h;
        }

        public int getH() {
            return this.h;
        }
    }

    public class Meta {

        private String image;
        private Size size;

        public void setImage(String image) {
            this.image = image;
        }

        public String getImage() {
            return this.image;
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public Size getSize() {
            return this.size;
        }
    }

    public class Size {

        private int w;
        private int h;

        public void setW(int w) {
            this.w = w;
        }

        public int getW() {
            return this.w;
        }

        public void setH(int h) {
            this.h = h;
        }

        public int getH() {
            return this.h;
        }
    }
}
