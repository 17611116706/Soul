package com.wyy.framework.helper;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wyy.framework.R;

import java.io.File;

/**
 * FileName: GlideHelper
 * Founder: LiuGuiLin
 * Profile: Glide 图片加载
 */
public class GlideHelper {

    /**
     * 加载图片
     * @param mContext
     * @param url
     * @param imageView
     */
    public static void loadUrl(Context mContext, String url, ImageView imageView){
        Glide.with(mContext)
                .load(url)
               // .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

    }

    /**
     * 加载图片
     * @param mContext
     * @param file
     * @param imageView
     */
    public static void loadFile(Context mContext, File file, ImageView imageView){
        Glide.with(mContext)
                .load(file)
                // .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

    }

    /**
     * 加载图片Url
     *
     * @param mContext
     * @param url
     * @param imageView
     */
    public static void loadSmollUrl(Context mContext, String url, int w, int h, ImageView imageView) {
        if (mContext != null) {
            Glide.with(mContext)
                    .load(url)
                    .override(w, h)
                    .placeholder(R.drawable.img_glide_load_ing)
                    .error(R.drawable.img_glide_load_error)
                    .format(DecodeFormat.PREFER_RGB_565)
                    // 取消动画，防止第一次加载不出来
                    .dontAnimate()
                    //加载缩略图
                    .thumbnail(0.3f)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

}
