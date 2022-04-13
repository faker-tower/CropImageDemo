package com.axiang.cropimagedemo.editimg.magic;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.axiang.cropimagedemo.MainApplication;
import com.axiang.cropimagedemo.util.BitmapUtil;
import com.axiang.cropimagedemo.util.CollectionUtil;
import com.axiang.cropimagedemo.util.FileUtil;
import com.axiang.cropimagedemo.util.GsonUtil;
import com.axiang.cropimagedemo.util.ThreadPoolUtil;
import com.axiang.cropimagedemo.util.ZipUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MagicFragment 辅助类，抽一些逻辑过来避免 MagicFragment 过于臃肿
 * Created by 邱翔威 on 2022/4/12
 */
public class MagicHelper {

    public static final String TAG = "MagicHelper";

    /**
     * 填充 assets 文件夹里的素材到 magicDataList
     */
    public static void fillAssetsData(@NonNull List<MagicData> magicDataList) {
        // 添加 assets -> magic -> face 文件夹里的素材
        MagicData faceData = new MagicData(false);
        faceData.setAssetsThumb("magic/face/1.png");
        List<String> faceMagicList = new ArrayList<>();
        Collections.addAll(faceMagicList, "magic/face/1.png", "magic/face/2.png", "magic/face/3.png",
                "magic/face/4.png", "magic/face/5.png", "magic/face/6.png",
                "magic/face/7.png", "magic/face/8.png", "magic/face/9.png");
        faceData.setAssetsMagicList(faceMagicList);
        magicDataList.add(faceData);

        // 添加 assets -> magic -> love 文件夹里的素材
        MagicData loveData = new MagicData(false);
        loveData.setAssetsThumb("magic/love/1.png");
        List<String> loveMagicList = new ArrayList<>();
        Collections.addAll(loveMagicList, "magic/love/1.png", "magic/love/2.png", "magic/love/3.png",
                "magic/love/4.png", "magic/love/5.png", "magic/love/6.png",
                "magic/love/7.png", "magic/love/8.png", "magic/love/9.png");
        loveData.setAssetsMagicList(loveMagicList);
        magicDataList.add(loveData);

        // 添加 assets -> magic -> space 文件夹里的素材
        MagicData spaceData = new MagicData(false);
        spaceData.setAssetsThumb("magic/space/1.png");
        List<String> spaceMagicList = new ArrayList<>();
        Collections.addAll(spaceMagicList, "magic/space/1.png", "magic/space/2.png", "magic/space/3.png",
                "magic/space/4.png", "magic/space/5.png", "magic/space/6.png",
                "magic/space/7.png", "magic/space/8.png", "magic/space/9.png");
        spaceData.setAssetsMagicList(spaceMagicList);
        magicDataList.add(spaceData);
    }

    /**
     * 读取 assets 文件夹里所有的素材 zip 资源
     *
     * @param zipAssetsFileNames assets 文件夹中素材 zip 文件名字合集
     */
    public static void loadAssetsAllZipData(String[] zipAssetsFileNames, OnLoadAssetsZipListener listener) {
        if (CollectionUtil.isEmpty(zipAssetsFileNames)) {
            return;
        }

        ThreadPoolUtil.execute(() -> {
            List<MagicData> magicDataList = new ArrayList<>();
            // 填充数组
            for (String zipAssetsFileName : zipAssetsFileNames) {
                loadAssetsZipData(magicDataList, zipAssetsFileName);
            }

            if (listener != null) {
                listener.onLoadComplete(magicDataList);
            }
        });
    }

    /**
     * 读取 assets 文件夹里单个素材 zip 资源 到 magicDataList
     *
     * @param zipAssetsFileName assets 文件夹中单个素材 zip 文件名字
     */
    private static void loadAssetsZipData(List<MagicData> magicDataList, String zipAssetsFileName) {
        ZipUtil.unzip(zipAssetsFileName, new ZipUtil.OnUnzipFinishListener() {
            @Override
            public void onSuccess(String successPath) {
                // 取 data.json 文件中的内容
                String dataJson = FileUtil.getJsonFileContent(FileUtil.addSeparator(successPath) + "data.json");
                if (TextUtils.isEmpty(dataJson)) {
                    return;
                }

                // data.json 的 Gson 转实体类
                MagicDataJson magicDataJson = GsonUtil.JsonToObject(dataJson, MagicDataJson.class);
                // 取 data.json 文件中的 files 字段
                String[] dataJsonFiles = getDataJsonFiles(magicDataJson);
                if (CollectionUtil.isEmpty(dataJsonFiles)) {
                    return;
                }

                // 循环取出所有 a1.json、a2.json、a3.json...axx.json 的数据
                String frameJson;
                MagicFrameData frameData;
                List<MagicData.FrameMeta> frameMetaList = new ArrayList<>();
                for (String jsonFile : dataJsonFiles) {
                    frameJson = FileUtil.getJsonFileContent(FileUtil.addSeparator(successPath) + jsonFile);
                    frameData = GsonUtil.JsonToObject(frameJson, MagicFrameData.class);
                    if (frameData == null) {
                        continue;
                    }

                    // 构建数据转化
                    String image = frameData.getMeta().getImage();
                    Log.d(TAG, String.format("%s 的 image = %s", jsonFile, image));
                    MagicData.FrameMeta meta = new MagicData.FrameMeta();
                    meta.setFrameImagePath(FileUtil.addSeparator(successPath) + image);
                    meta.setFrameData(frameData);
                    frameMetaList.add(meta);
                }

                if (!frameMetaList.isEmpty()) {
                    MagicData data = new MagicData(frameMetaList);
                    if (data.checkValidity()) { // 校验合法性
                        magicDataList.add(data);
                    }
                }
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "解压 Assets 目录下的 指尖魔法的 zip 资源失败");
            }
        });
    }

    /**
     * 按照 frame 数组切割 zip 缩略图
     */
    public static List<Bitmap> splitZipBitmap(MagicData.FrameMeta magicFrameData,
                                              int mainBitmapWidth, int mainBitmapHeight) {
        if (!magicFrameData.checkValidity()) {
            return null;
        }

        List<Bitmap> result = new ArrayList<>();

        String srcImagePath = magicFrameData.getFrameImagePath();
        MagicFrameData frameData = magicFrameData.getFrameData();

        MagicFrameData.Meta.Size size = frameData.getMeta().getSize();
        List<MagicFrameData.Frames> frames = frameData.getFrames();

        for (MagicFrameData.Frames frame : frames) {
            // 每一个子 frame 所占原图比例
            MagicFrameData.Frames.Frame childFrame = frame.getFrame();
            int scaleW = size.getW() / childFrame.getW();
            int scaleH = size.getH() / childFrame.getH();

            // 按比例缩放目标进行压缩
            Pair<Integer, Integer> pair = getFrameReqWidth(size.getW(), size.getH(),
                    mainBitmapWidth, mainBitmapHeight);
            int reqW = pair.first * scaleW;
            int reqH = pair.second * scaleH;
            Bitmap bitmap = BitmapUtil.getSampledBitmap(srcImagePath, reqW, reqH);

            // 原图和缩放图的比例
            int sW = size.getW() / bitmap.getWidth();
            int sH = size.getH() / bitmap.getHeight();

            // 裁剪 frame 比例区域
            Bitmap childBitmap = Bitmap.createBitmap(bitmap,
                    childFrame.getX() / sW, childFrame.getY() / sH,
                    childFrame.getW() / sW, childFrame.getH() / sH);

            bitmap.recycle();   // 直接释放

            result.add(childBitmap);
        }
        return result;
    }

    /**
     * 获取一个 frame 的图最后显示所需要的尺寸，以宽度为基准
     */
    private static Pair<Integer, Integer> getFrameReqWidth(int frameSizeWidth, int frameSizeHeight,
                                                           int mainBitmapWidth, int mainBitmapHeight) {
        int resultWidth = frameSizeWidth;
        int resultHeight;
        DisplayMetrics metrics = MainApplication.getContext().getResources().getDisplayMetrics();
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

        resultWidth = Math.min(resultWidth, widthPixels);
        resultWidth = Math.min(resultWidth, mainBitmapWidth);

        if (resultWidth == frameSizeWidth) {
            resultHeight = frameSizeHeight;
        } else if (resultWidth == mainBitmapWidth) {
            resultHeight = mainBitmapHeight;
        } else {
            resultHeight = heightPixels;
        }

        return new Pair<>(resultWidth, resultHeight);
    }

    /**
     * 取 data.json 文件中的 files 字段
     */
    private static String[] getDataJsonFiles(MagicDataJson magicDataJson) {
        List<MagicDataJson.Frame> frame = magicDataJson.getFrame();
        if (CollectionUtil.isEmpty(frame)) {
            return null;
        }

        List<MagicDataJson.Res> res = frame.get(0).getRes();
        if (CollectionUtil.isEmpty(res)) {
            return null;
        }

        String files = res.get(0).getFiles();
        Log.d(TAG, "getMagicFiles(): " + files);
        return files.split(",");
    }

    public interface OnLoadAssetsZipListener {

        void onLoadComplete(List<MagicData> magicDataList);
    }
}
