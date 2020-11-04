package com.wyy.souldemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.wyy.framework.base.BaseBackActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.helper.FileHelper;
import com.wyy.framework.manager.DialogManager;
import com.wyy.framework.view.DialogView;
import com.wyy.framework.view.LodingView;
import com.wyy.souldemo.R;

import java.io.File;

import cn.bmob.v3.exception.BmobException;
import de.hdodenhof.circleimageview.CircleImageView;
/**
 * FileName: FirstUploadActivity
 * Founder: LiuGuiLin
 * Profile: 头像上传
 */
public class FirstUploadActivity extends BaseBackActivity implements View.OnClickListener {

    private TextView tvCamera;
    private TextView tvAblum;
    private TextView tvCancel;
    private File uploadFile=null;

    /**
     * 跳转
     *
     * @param mActivity
     * @param requestCode
     */
    public static void startActivity(Activity mActivity, int requestCode) {
        Intent intent = new Intent(mActivity, FirstUploadActivity.class);
        mActivity.startActivityForResult(intent, requestCode);
    }

    //原形头像
    private CircleImageView ivPhoto;
    private EditText etNickname;
    private Button btnUpload;
    private DialogView mPhotoSelectView;
    private LodingView mLodingView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_upload);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        ivPhoto.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        tvCamera.setOnClickListener(this);
        tvAblum.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    private void initData() {
        etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0){
                    btnUpload.setEnabled(uploadFile!=null);
                }else{
                    btnUpload.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

    }

    private void initView() {
        initPhotoView();
        ivPhoto = (CircleImageView) findViewById(R.id.iv_photo);
        etNickname = (EditText) findViewById(R.id.et_nickname);
        btnUpload = (Button) findViewById(R.id.btn_upload);
        btnUpload.setEnabled(false);
    }

    //初始化选择提示框
    private void initPhotoView() {
        mLodingView=new LodingView(this);
        mLodingView.setLodingText("正在上传头像中...");

        mPhotoSelectView = DialogManager.getInstance()
                .initView(this, R.layout.dialog_select_photo, Gravity.BOTTOM);

        tvCamera = (TextView)mPhotoSelectView. findViewById(R.id.tv_camera);
        tvAblum = (TextView)mPhotoSelectView. findViewById(R.id.tv_ablum);
        tvCancel = (TextView)mPhotoSelectView. findViewById(R.id.tv_cancel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo:
                //显示选择提示框
                DialogManager.getInstance().show(mPhotoSelectView);
                break;
            case R.id.btn_upload:
                //上传头像
                uploadPhoto();
                break;
            case R.id.tv_camera:
                DialogManager.getInstance().hide(mPhotoSelectView);
                //跳转相机
                FileHelper.getInstance().toCamera(this);
                break;
            case R.id.tv_ablum:
                DialogManager.getInstance().hide(mPhotoSelectView);
                //跳转相册
                FileHelper.getInstance().toAlbum(this);
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hide(mPhotoSelectView);
                break;
        }
    }

    /**
     * 上传头像
     */
    private void uploadPhoto() {
        String nickName = etNickname.getText().toString().trim();
        mLodingView.show();
        BmobManager.getInstance().uploadFirstPhoto(nickName, uploadFile, new BmobManager.OnUploadPhotoListener() {
            @Override
            public void OnUpdateDone() {
                mLodingView.hide();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void OnUpdateFail(BmobException e) {
                mLodingView.hide();
                Toast.makeText(FirstUploadActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==Activity.RESULT_OK){
            if(requestCode==FileHelper.CAMEAR_REQUEST_CODE){
                uploadFile=FileHelper.getInstance().getTempFile();
            }else if(requestCode==FileHelper.ALBUM_REQUEST_CODE){
                Uri uri = data.getData();
                if(uri!=null){
                    String path = FileHelper.getInstance().getRealPathFromURI(this, uri);
                    if(!TextUtils.isEmpty(path)){
                        uploadFile=new File(path);
                    }
                }
            }
        }
        //设置头像
        if (uploadFile != null) {
            Bitmap mBitmap = BitmapFactory.decodeFile(uploadFile.getPath());
            ivPhoto.setImageBitmap(mBitmap);

            //判断当前输入框
            String nickName = etNickname.getText().toString().trim();
            btnUpload.setEnabled(!TextUtils.isEmpty(nickName));
        }
    }




}
