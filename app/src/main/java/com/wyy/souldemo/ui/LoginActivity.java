package com.wyy.souldemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wyy.framework.base.BaseUIActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.entity.Constants;
import com.wyy.framework.manager.DialogManager;
import com.wyy.framework.utils.SpUtils;
import com.wyy.framework.view.DialogView;
import com.wyy.framework.view.LodingView;
import com.wyy.framework.view.TouchPictureV;
import com.wyy.souldemo.R;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * FileName: LoginActivity
 * Founder: LiuGuiLin
 * Profile: 登录页
 */
public class LoginActivity extends BaseUIActivity implements View.OnClickListener {

    private EditText etPhone;
    private EditText etCode;
    private Button btnSendCode;
    private Button btnLogin;
    private TextView tvTestLogin;
    private TextView tvUserAgreement;

    private DialogView mCodeView;
    private TouchPictureV mPictureV;
    private LodingView mLodingView;

    private static final int H_TIME=1001;
    //60s倒计时
    private static int TIME = 60;

    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case H_TIME:
                    TIME--;
                    btnSendCode.setText(TIME+"s");
                    if(TIME>0){
                        mHandler.sendEmptyMessageDelayed(H_TIME,1000);
                    }else{
                        btnSendCode.setEnabled(true);
                        btnSendCode.setText(getString(R.string.text_login_send));
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        btnSendCode.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        tvTestLogin.setOnClickListener(this);
    }

    private void initData() {
        //手机号显示在输入框
        String phone = SpUtils.getInstance().getString(Constants.SP_PHONE, "");
        if (!TextUtils.isEmpty(phone)) {
            etPhone.setText(phone);
        }

    }

    private void initView() {

        initDialogView();

        etPhone = (EditText) findViewById(R.id.et_phone);
        etCode = (EditText) findViewById(R.id.et_code);
        btnSendCode = (Button) findViewById(R.id.btn_send_code);
        btnLogin = (Button) findViewById(R.id.btn_login);
        tvTestLogin = (TextView) findViewById(R.id.tv_test_login);
        tvUserAgreement = (TextView) findViewById(R.id.tv_user_agreement);

    }

    private void initDialogView() {

        mLodingView = new LodingView(this);
        mCodeView = DialogManager.getInstance().initView(this, R.layout.dialog_code_view);
        mPictureV = mCodeView.findViewById(R.id.mPictureV);
        mPictureV.setViewResultListener(new TouchPictureV.OnViewResultListener() {
            @Override
            public void onResult() {
                DialogManager.getInstance().hide(mCodeView);
                sendSMS();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send_code:
                DialogManager.getInstance().show(mCodeView);
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.tv_test_login:
                startActivity(new Intent(this, TestLoginActivity.class));
                break;
        }
    }

    /**
     * 登录
     */
    private void login() {
        //1.判断手机号码和验证码不为空
        String phone = etPhone.getText().toString().trim();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, getString(R.string.text_login_phone_null),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String code = etCode.getText().toString().trim();
        if(TextUtils.isEmpty(code)){
            Toast.makeText(this, getString(R.string.text_login_code_null),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //显示LodingView
        mLodingView.show(getString(R.string.text_login_now_login_text));

        BmobManager.getInstance().signOrLoginByMobilePhone(phone, code, new LogInListener<IMUser>() {
            @Override
            public void done(IMUser imUesr, BmobException e) {
                mLodingView.hide();
                if(e==null){
                    //登陆成功
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    //把手机号码保存下来
                    SpUtils.getInstance().putString(Constants.SP_PHONE, phone);
                    finish();
                }else {
                    if (e.getErrorCode() == 207) {
                        Toast.makeText(LoginActivity.this, getString(R.string.text_login_code_error), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "ERROR:" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * 发送短信验证码
     */
    private void sendSMS() {
        //1.获取手机号码
        String phone = etPhone.getText().toString().trim();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, getString(R.string.text_login_phone_null),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //2.请求短信验证码
        BmobManager.getInstance().requestSMSI(phone, new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                if(e==null){
                    btnSendCode.setEnabled(false);
                    mHandler.sendEmptyMessage(H_TIME);
                    Toast.makeText(LoginActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoginActivity.this, "验证码发送失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
