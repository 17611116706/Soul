package com.wyy.framework.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wyy.framework.utils.SystemUI;


/**
 * FileName: BaseUIActivity
 * Founder: LiuGuiLin
 * Profile: UI 基类
 */
public class BaseUIActivity extends BaseActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUI.fixSystemUI(this);
    }
}
