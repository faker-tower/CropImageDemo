package com.axiang.cropimagedemo.editimg.magic;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.axiang.cropimagedemo.MainApplication;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.BitmapUtil;
import com.axiang.cropimagedemo.util.FileUtil;
import com.axiang.cropimagedemo.util.GsonUtil;
import com.axiang.cropimagedemo.util.ThreadPoolUtil;
import com.axiang.cropimagedemo.util.ZipUtil;

import java.io.File;
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
    public static void fillAssetsData(List<MagicData> magicDataList) {
        if (magicDataList == null) {
            return;
        }

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
        if (zipAssetsFileNames == null || zipAssetsFileNames.length <= 0) {
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
                String dataJson = FileUtil.getJsonFileContent(successPath + File.separator + "data.json");
                if (TextUtils.isEmpty(dataJson)) {
                    return;
                }

                // data.json 的 Gson 转实体类
                MagicDataJson magicDataJson = GsonUtil.JsonToObject(dataJson, MagicDataJson.class);
                // 取 data.json 文件中的 files 字段
                String[] magicFrameJsonFiles = getMagicFiles(magicDataJson);
                if (magicFrameJsonFiles == null || magicFrameJsonFiles.length <= 0) {
                    return;
                }

                // 循环取出所有 a1.json、a2.json、a3.json...axx.json 的数据
                String frameJson;
                MagicFrameData frameData;
                List<MagicData.FrameMeta> frameMetaList = new ArrayList<>();
                for (String jsonFile : magicFrameJsonFiles) {
                    frameJson = FileUtil.getJsonFileContent(successPath + File.separator + jsonFile);
                    frameData = GsonUtil.JsonToObject(frameJson, MagicFrameData.class);
                    if (frameData == null) {
                        continue;
                    }

                    String image = frameData.getMeta().getImage();
                    Log.d(TAG, String.format("%s 的 image = %s", jsonFile, frameData.getMeta().getImage()));
                    MagicData.FrameMeta meta = new MagicData.FrameMeta();
                    meta.setFrameImagePath(successPath + File.separator + image);
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
     * 构建 zip 缩略图
     * 构建 MagicData 缩略图，取 a1 的 frame 数组中的第一个数据作为缩略图
     */
    public static Bitmap generateZipThumbBitmap(String srcImagePath, MagicFrameData frameData) {
        if (frameData == null) {
            return null;
        }

        MagicFrameData.Meta meta = frameData.getMeta();
        List<MagicFrameData.Frames> frames = frameData.getFrames();
        if (meta == null || frames == null || frames.isEmpty()) {
            return null;
        }

        MagicFrameData.Meta.Size size = meta.getSize();
        MagicFrameData.Frames.Frame frame = frames.get(0).getFrame();
        if (size == null || frame == null) {
            return null;
        }

        int scaleW = size.getW() / frame.getW();    // frame 所占 原图 宽度比例
        int scaleH = size.getH() / frame.getH();    // frame 所占 原图 高度比例
        Log.d(TAG, "size.getW() = " + size.getW() + ", size.getH() = " + size.getH());
        Log.d(TAG, "frame.getW() = " + frame.getW() + ", frame.getH() = " + frame.getH());
        Log.d(TAG, "scaleW = " + scaleW + ", scaleH = " + scaleH);

        int thumbReqSize = MainApplication.getContext().getResources()
                .getDimensionPixelSize(R.dimen.magic_thumb_size);   // 缩略图目标尺寸
        Log.d(TAG, "thumbReqSize = " + thumbReqSize);

        int reqW = thumbReqSize * scaleW;   // 缩放目标宽度
        int reqH = thumbReqSize * scaleH;   // 缩放目标高度
        Log.d(TAG, "reqW = " + reqW + ", reqH = " + reqH);

        Bitmap bitmap = BitmapUtil.getSampledBitmap(srcImagePath, reqW, reqH);
        int sw = size.getW() / bitmap.getWidth();   // 原图和缩放图的宽度比例
        int sH = size.getH() / bitmap.getHeight();  // 原图和缩放图的高度比例
        Log.d(TAG, "bitmap.getWidth = " + bitmap.getWidth() + ", bitmap.getHeight = " + bitmap.getHeight());
        Log.d(TAG, "sw = " + sw + ", sH = " + sH);

        Bitmap result = Bitmap.createBitmap(bitmap,
                frame.getX() / sw, frame.getY() / sH,
                frame.getW() / sw, frame.getH() / sH);  // 裁剪 frame 比例区域
        Log.d(TAG, "frame.getX() / sw = " + (frame.getX() / sw) + ", frame.getY() / sH = " + (frame.getY() / sH));
        Log.d(TAG, "frame.getW() / sw = " + (frame.getW() / sw) + ", frame.getH() / sH = " + (frame.getH() / sH));

//        bitmap.recycle();   // 直接释放
        return bitmap;
    }

    /**
     * 取 data.json 文件中的 files 字段
     */
    public static String[] getMagicFiles(MagicDataJson magicData) {
        List<MagicDataJson.Frame> frame = magicData.getFrame();
        if (frame == null || frame.isEmpty()) {
            return null;
        }

        List<MagicDataJson.Res> res = frame.get(0).getRes();
        if (res == null || res.isEmpty()) {
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
