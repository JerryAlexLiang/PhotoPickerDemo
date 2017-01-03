package com.foamtrace.photopicker.intent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.foamtrace.photopicker.PhotoPreviewActivity;

import java.util.ArrayList;

/**
 * 预览照片
 * @author JerryAlexLiang
 */
public class PhotoPreviewIntent extends Intent{

    public PhotoPreviewIntent(Context packageContext) {
        super(packageContext, PhotoPreviewActivity.class);
    }

    /**
     * 照片地址
     * @param paths
     */
    public void setPhotoPaths(ArrayList<String> paths){
        this.putStringArrayListExtra(PhotoPreviewActivity.EXTRA_PHOTOS, paths);
    }

    /**
     * 当前照片的下标
     * @param currentItem
     */
    public void setCurrentItem(int currentItem){
        this.putExtra(PhotoPreviewActivity.EXTRA_CURRENT_ITEM, currentItem);
    }
}
