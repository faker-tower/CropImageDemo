package com.axiang.cropimagedemo.editimg.magic;

import java.util.List;

/**
 * data.json 的数据
 * Created by 邱翔威 on 2022/4/11
 */
public class MagicDataJson {

    private List<Frame> frame;
    private Filter filter;
    private String anim;
    private List<String> sequence;

    public void setFrame(List<Frame> frame) {
        this.frame = frame;
    }

    public List<Frame> getFrame() {
        return this.frame;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setAnim(String anim) {
        this.anim = anim;
    }

    public String getAnim() {
        return this.anim;
    }

    public void setSequence(List<String> sequence) {
        this.sequence = sequence;
    }

    public List<String> getSequence() {
        return this.sequence;
    }

    public static class Frame {

        private int layer;
        private int blendType;
        private int blendAlpha;
        private int refWidth;
        private int refHeight;
        private String pos;
        private int displayMode;
        private int cycle;
        private int fitTotalAnimation;
        private List<Res> res;

        public void setLayer(int layer) {
            this.layer = layer;
        }

        public int getLayer() {
            return this.layer;
        }

        public void setBlendType(int blendType) {
            this.blendType = blendType;
        }

        public int getBlendType() {
            return this.blendType;
        }

        public void setBlendAlpha(int blendAlpha) {
            this.blendAlpha = blendAlpha;
        }

        public int getBlendAlpha() {
            return this.blendAlpha;
        }

        public void setRefWidth(int refWidth) {
            this.refWidth = refWidth;
        }

        public int getRefWidth() {
            return this.refWidth;
        }

        public void setRefHeight(int refHeight) {
            this.refHeight = refHeight;
        }

        public int getRefHeight() {
            return this.refHeight;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public String getPos() {
            return this.pos;
        }

        public void setDisplayMode(int displayMode) {
            this.displayMode = displayMode;
        }

        public int getDisplayMode() {
            return this.displayMode;
        }

        public void setCycle(int cycle) {
            this.cycle = cycle;
        }

        public int getCycle() {
            return this.cycle;
        }

        public void setFitTotalAnimation(int fitTotalAnimation) {
            this.fitTotalAnimation = fitTotalAnimation;
        }

        public int getFitTotalAnimation() {
            return this.fitTotalAnimation;
        }

        public void setRes(List<Res> res) {
            this.res = res;
        }

        public List<Res> getRes() {
            return this.res;
        }
    }

    public static class Res {

        private String ratio;
        private String files;
        private int x;
        private int y;

        public void setRatio(String ratio) {
            this.ratio = ratio;
        }

        public String getRatio() {
            return this.ratio;
        }

        public void setFiles(String files) {
            this.files = files;
        }

        public String getFiles() {
            return this.files;
        }

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
    }

    public static class Filter {

    }
}
