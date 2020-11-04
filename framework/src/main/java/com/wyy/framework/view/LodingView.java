package com.wyy.framework.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.wyy.framework.R;
import com.wyy.framework.manager.DialogManager;
import com.wyy.framework.utils.AnimUtils;

/**
 * FileName: LodingView
 * Founder: LiuGuiLin
 * Profile: 加载提示框
 */
public class LodingView {
    private DialogView mLodingView;
    private ImageView ivLoding;
    private TextView tvLodingText;
    private ObjectAnimator animator;



    public LodingView(Context mContext){
        mLodingView= DialogManager.getInstance().initView(mContext, R.layout.dialog_loding);
        ivLoding =mLodingView. findViewById(R.id.iv_loding);
        tvLodingText = mLodingView. findViewById(R.id.tv_loding_text);
        animator= AnimUtils.rotation(ivLoding);
    }

    /**
     * 设置加载的提示文本
     *
     * @param text
     */
    public void setLodingText(String text){
        if(!TextUtils.isEmpty(text)){
            tvLodingText.setText(text);
        }
    }

    public void show(){
        animator.start();
        DialogManager.getInstance().show(mLodingView);
    }

    public void show(String text){
        animator.start();
        setLodingText(text);
        DialogManager.getInstance().show(mLodingView);
    }

    public void hide(){
        animator.pause();
        DialogManager.getInstance().hide(mLodingView);
    }

    /**
     * 外部是否可以点击消失
     * @param flag
     */
    public void setCancelable(boolean flag){
        mLodingView.setCancelable(false);
    }


}
