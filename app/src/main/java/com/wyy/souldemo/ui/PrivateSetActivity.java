package com.wyy.souldemo.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.wyy.framework.base.BaseBackActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.PrivateSet;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.framework.utils.LogUtils;
import com.wyy.framework.view.LodingView;
import com.wyy.souldemo.R;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class PrivateSetActivity extends BaseBackActivity implements View.OnClickListener {

    private Switch sw_kill_contact;

    private LodingView mLodingView;

    //是否选中
    private boolean isCheck = false;

    //当前ID
    private String currentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_set);
        initView();
    }

    private void initView() {

        mLodingView = new LodingView(this);

        sw_kill_contact = (Switch) findViewById(R.id.sw_kill_contact);
        sw_kill_contact.setOnClickListener(this);

        queryPrivateSet();
    }

    /**
     * 查询私有库
     */
    private void queryPrivateSet() {
        BmobManager.getInstance().queryPrivateSet(new FindListener<PrivateSet>() {
            @Override
            public void done(List<PrivateSet> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        for (int i = 0; i < list.size(); i++) {
                            PrivateSet set = list.get(i);
                            if (set.getUserId().equals(BmobManager.getInstance().getUser().getObjectId())) {
                                currentId = set.getObjectId();
                                //我存在表中
                                isCheck = true;
                                break;
                            }
                        }
                        LogUtils.i("currentId:" + currentId);
                        sw_kill_contact.setChecked(isCheck);
                    }
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sw_kill_contact:
                isCheck = !isCheck;
                sw_kill_contact.setChecked(isCheck);
                if (isCheck) {
                    //添加
                    addPrivateSet();
                } else {
                    //删除
                    delPrivateSet();
                }
                break;
        }
    }
    /**
     * 添加
     */
    private void addPrivateSet() {
        mLodingView.show(getString(R.string.text_private_set_open_ing));
        BmobManager.getInstance().addPrivateSet(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                mLodingView.hide();
                if (e == null) {
                    currentId = s;
                    Toast.makeText(PrivateSetActivity.this, getString(R.string.text_private_set_succeess), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 删除
     */
    private void delPrivateSet() {
        LogUtils.i("delPrivateSet:" + currentId);
        mLodingView.show(getString(R.string.text_private_set_close_ing));
        BmobManager.getInstance().delPrivateSet(currentId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                mLodingView.hide();
                if (e == null) {
                    Toast.makeText(PrivateSetActivity.this, getString(R.string.text_private_set_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
