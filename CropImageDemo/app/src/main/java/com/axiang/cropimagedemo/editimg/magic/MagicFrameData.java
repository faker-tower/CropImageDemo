package com.axiang.cropimagedemo.editimg.magic;

import java.util.List;

/**
 * a1.json、a2.json、a3.json...axx.json 的数据
 * Created by 邱翔威 on 2022/4/11
 */
public class MagicFrameData {

    private List<Frames> frames;
    private Meta meta;

    public List<Frames> getFrames() {
        return frames;
    }

    public void setFrames(List<Frames> frames) {
        this.frames = frames;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public static class Meta {

        private String image;
        private Size size;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public Size getSize() {
            return size;
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public static class Size {

            private int w;
            private int h;

            public int getW() {
                return w;
            }

            public void setW(int w) {
                this.w = w;
            }

            public int getH() {
                return h;
            }

            public void setH(int h) {
                this.h = h;
            }
        }
    }

    public static class Frames {

        private String filename;
        private double d;
        private Frame frame;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }

        public Frame getFrame() {
            return frame;
        }

        public void setFrame(Frame frame) {
            this.frame = frame;
        }

        public static class Frame {

            private int x;
            private int y;
            private int w;
            private int h;

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }

            public int getW() {
                return w;
            }

            public void setW(int w) {
                this.w = w;
            }

            public int getH() {
                return h;
            }

            public void setH(int h) {
                this.h = h;
            }
        }
    }
}
