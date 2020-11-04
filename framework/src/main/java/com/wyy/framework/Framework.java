package com.wyy.framework;

import android.content.Context;

import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.cloud.CloudManager;
import com.wyy.framework.helper.WindowHelper;
import com.wyy.framework.manager.MapManager;
import com.wyy.framework.utils.LogUtils;
import com.wyy.framework.utils.SpUtils;

import org.litepal.LitePal;

public class Framework {
    private volatile static Framework mFramework;
    private Framework(){}

    public static Framework getFramework(){
        if(mFramework==null){
            synchronized (Framework.class){
                if(mFramework==null){
                    mFramework=new Framework();
                }
            }
        }
        return mFramework;
    }


    /**
     * 初始化框架 Model
     *
     * @param mContext
     */
    public void initFramework(Context mContext) {
        LogUtils.i("initFramework");
        SpUtils.getInstance().initSp(mContext);
        BmobManager.getInstance().initBmob(mContext);
        CloudManager.getInstance().initCloud(mContext);
        LitePal.initialize(mContext);
        MapManager.getInstance().initMap(mContext);
        WindowHelper.getInstance().initWindow(mContext);

    }

}
