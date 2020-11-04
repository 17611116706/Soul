package com.wyy.souldemo.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.wyy.framework.base.BaseBackActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.helper.FileHelper;
import com.wyy.framework.helper.GlideHelper;
import com.wyy.framework.view.LodingView;
import com.wyy.souldemo.R;

public class ShareImgActivity extends BaseBackActivity implements View.OnClickListener {

    //头像
    private ImageView iv_photo;
    //昵称
    private TextView tv_name;
    //性别
    private TextView tv_sex;
    //年龄
    private TextView tv_age;
    //电话
    private TextView tv_phone;
    //简介
    private TextView tv_desc;
    //二维码
    private ImageView iv_qrcode;
    //根布局
    private LinearLayout ll_content;
    //下载
    private LinearLayout ll_download;

    private LodingView mLodingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_img);
        initView();
    }
    private void initView() {

        mLodingView = new LodingView(this);
        mLodingView.setLodingText(getString(R.string.text_shar_save_ing));

        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        tv_age = (TextView) findViewById(R.id.tv_age);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_desc = (TextView) findViewById(R.id.tv_desc);
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        ll_download = (LinearLayout) findViewById(R.id.ll_download);

        ll_download.setOnClickListener(this);
        loadInfo();
    }

    /**
     * 加载个人信息
     */
    private void loadInfo() {
        IMUser imUser = BmobManager.getInstance().getUser();
        GlideHelper.loadUrl(this, imUser.getPhoto(), iv_photo);
        tv_name.setText(imUser.getNickName());
        tv_sex.setText(imUser.isSex() ? R.string.text_me_info_boy : R.string.text_me_info_girl);
        tv_age.setText(imUser.getAge() + " " + getString(R.string.text_search_age));
        tv_phone.setText(imUser.getMobilePhoneNumber());
        tv_desc.setText(imUser.getDesc());

        createQRCode(imUser.getObjectId());
    }

    /**
     * 创建二维码
     */
    private void createQRCode(final String userId) {
        /**
         * View的绘制
         */
        iv_qrcode.post(new Runnable() {
            @Override
            public void run() {
                String textContent = "Meet#" + userId;
                Bitmap mBitmap = CodeUtils.createImage(textContent,
                        iv_qrcode.getWidth(), iv_qrcode.getHeight(), null);
                iv_qrcode.setImageBitmap(mBitmap);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_download:
                mLodingView.show();
                ll_content.setDrawingCacheEnabled(true);

                ll_content.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                ll_content.layout(0, 0, ll_content.getMeasuredWidth(),
                        ll_content.getMeasuredHeight());

                Bitmap mBitmap = ll_content.getDrawingCache();

                if (mBitmap != null) {
                    FileHelper.getInstance().saveBitmapToAlbum(this, mBitmap);
                    mLodingView.hide();
                }
                break;
        }
    }






}