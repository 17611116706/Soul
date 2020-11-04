package com.wyy.souldemo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.wyy.framework.base.BaseUIActivity;
import com.wyy.framework.entity.Constants;
import com.wyy.framework.helper.GlideHelper;
import com.wyy.souldemo.R;

import java.io.File;

/**
 * FileName: ImagePreviewActivity
 * Founder: LiuGuiLin
 * Profile: 图片预览
 */
public class ImagePreviewActivity extends BaseUIActivity implements View.OnClickListener {

    /**
     * 跳转
     * @param mContext
     * @param isUrl
     * @param url
     */
    public static void startActivity(Context mContext,boolean isUrl,String url){
        Intent intent = new Intent(mContext,ImagePreviewActivity.class);
        intent.putExtra(Constants.INTENT_IMAGE_TYPE,isUrl);
        intent.putExtra(Constants.INTENT_IMAGE_URL,url);
        mContext.startActivity(intent);
    }


    private PhotoView photoView;
    private ImageView ivBack;
    private TextView tvDownload;

    //图片地址
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        initView();
        initListener();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        boolean isUrl = intent.getBooleanExtra(Constants.INTENT_IMAGE_TYPE, false);
        url=intent.getStringExtra(Constants.INTENT_IMAGE_URL);

        //图片地址才下载，File代表本次已经存在
        tvDownload.setVisibility(isUrl?View.VISIBLE:View.GONE);
        if(isUrl){
            GlideHelper.loadUrl(this,url,photoView);
        }else{
            GlideHelper.loadFile(this,new File(url),photoView);
        }
    }

    private void initListener() {
        ivBack.setOnClickListener(this);
    }

    private void initView() {
        photoView = (PhotoView) findViewById(R.id.photo_view);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvDownload = (TextView) findViewById(R.id.tv_download);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_download:
                Toast.makeText(this, getString(R.string.text_iv_pre_downloading), Toast.LENGTH_SHORT).show();


                break;
        }
    }
}
