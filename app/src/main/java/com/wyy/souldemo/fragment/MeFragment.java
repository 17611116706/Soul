package com.wyy.souldemo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.wyy.framework.base.BaseFragment;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.helper.GlideHelper;
import com.wyy.souldemo.R;
import com.wyy.souldemo.ui.MeInfoActivity;
import com.wyy.souldemo.ui.NewFriendActivity;
import com.wyy.souldemo.ui.PrivateSetActivity;
import com.wyy.souldemo.ui.SettingActivity;
import com.wyy.souldemo.ui.ShareImgActivity;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * 我的
 */
public class MeFragment extends BaseFragment implements View.OnClickListener {


    private CircleImageView ivMePhoto;
    private TextView tvNickname;
    private TextView tvServerStatus;
    private LinearLayout llMeInfo;
    private LinearLayout llNewFriend;
    private LinearLayout llPrivateSet;
    private LinearLayout llShare;
    private LinearLayout llNotice;
    private LinearLayout llSetting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initView(view);
        initListener();
        return view;
    }

    private void initListener() {
        llMeInfo.setOnClickListener(this);
        llNewFriend.setOnClickListener(this);
        llPrivateSet.setOnClickListener(this);
        llShare.setOnClickListener(this);
        llNotice.setOnClickListener(this);
        llSetting.setOnClickListener(this);

    }

    private void initView(View view) {
        ivMePhoto = (CircleImageView)view. findViewById(R.id.iv_me_photo);
        tvNickname = (TextView)view.  findViewById(R.id.tv_nickname);
        tvServerStatus = (TextView)view.  findViewById(R.id.tv_server_status);
        llMeInfo = (LinearLayout)view.  findViewById(R.id.ll_me_info);
        llNewFriend = (LinearLayout)view.  findViewById(R.id.ll_new_friend);
        llPrivateSet = (LinearLayout)view.  findViewById(R.id.ll_private_set);
        llShare = (LinearLayout)view.  findViewById(R.id.ll_share);
        llNotice = (LinearLayout) view. findViewById(R.id.ll_notice);
        llSetting = (LinearLayout)view.  findViewById(R.id.ll_setting);

        loadMeInfo();
    }

    /**
     * 加载我的个人信息
     */
    private void loadMeInfo() {
        IMUser imUser = BmobManager.getInstance().getUser();
        GlideHelper.loadUrl(getActivity(),imUser.getPhoto(),ivMePhoto);
        tvNickname.setText(imUser.getNickName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_me_info:
                //个人信息
                startActivity(new Intent(getActivity(), MeInfoActivity.class));
                break;
            case R.id.ll_new_friend:
                //新朋友
                startActivity(new Intent(getActivity(), NewFriendActivity.class));
                break;
            case R.id.ll_private_set:
                //隐私设置
                startActivity(new Intent(getActivity(), PrivateSetActivity.class));
                break;
            case R.id.ll_share:
                //分享
                startActivity(new Intent(getActivity(),ShareImgActivity.class));
                break;
            case R.id.ll_notice:
                //通知

                break;
            case R.id.ll_setting:
                //设置
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }
}
