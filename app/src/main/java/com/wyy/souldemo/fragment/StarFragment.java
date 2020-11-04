package com.wyy.souldemo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.moxun.tagcloudlib.view.TagCloudView;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.cloud.CloudManager;
import com.wyy.framework.event.EventManager;
import com.wyy.framework.event.MessageEvent;
import com.wyy.framework.helper.PairFriendHelper;
import com.wyy.framework.manager.DialogManager;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.framework.utils.LogUtils;
import com.wyy.framework.view.DialogView;
import com.wyy.framework.view.LodingView;
import com.wyy.souldemo.adapter.CloudTagAdapter;
import com.wyy.framework.base.BaseFragment;
import com.wyy.souldemo.R;
import com.wyy.souldemo.model.StarModel;
import com.wyy.souldemo.ui.AddFriendActivity;
import com.wyy.souldemo.ui.UserInfoActivity;
import com.wyy.souldemo.ui.ZxingActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 * 星球
 */
public class StarFragment extends BaseFragment implements View.OnClickListener {
    //二维码结果
    private static final int REQUEST_CODE = 1235;

    private TextView tvStarTitle;
    private ImageView ivCamera;
    private ImageView ivAdd;
    private TextView tvConnectStatus;
    private TagCloudView mCloudView;
    private LinearLayout llRandom;
    private TextView tvRandom;
    private LinearLayout llSoul;
    private TextView tvSoul;
    private LinearLayout llFate;
    private TextView tvFate;
    private LinearLayout llLove;
    private TextView tvLove;

    private CloudTagAdapter mCloudTagAdapter;
    private List<StarModel> mStarList=new ArrayList<>();
    private LodingView mLodingView;
    private DialogView mNullDialogView;
    private TextView tv_null_text;
    private TextView tv_null_cancel;
    //连接状态
    private TextView tv_connect_status;

    //全部好友
    private List<IMUser> mAllUserList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initListener() {
        ivCamera.setOnClickListener(this);
        ivAdd.setOnClickListener(this);
        llRandom.setOnClickListener(this);
        llSoul.setOnClickListener(this);
        llFate.setOnClickListener(this);
        llLove.setOnClickListener(this);
    }

    private void initView(View view) {
        mLodingView = new LodingView(getActivity());
        mLodingView.setCancelable(false);

        mNullDialogView = DialogManager.getInstance().initView(getActivity(), R.layout.layout_star_null_item, Gravity.BOTTOM);
        tv_null_text = mNullDialogView.findViewById(R.id.tv_null_text);
        tv_null_cancel = mNullDialogView.findViewById(R.id.tv_cancel);
        tv_null_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hide(mNullDialogView);
            }
        });

        tvStarTitle = view. findViewById(R.id.tv_star_title);
        ivCamera = view. findViewById(R.id.iv_camera);
        ivAdd = view. findViewById(R.id.iv_add);
        tvConnectStatus = view. findViewById(R.id.tv_connect_status);
        mCloudView =view. findViewById(R.id.mCloudView);
        llRandom = view. findViewById(R.id.ll_random);
        tvRandom = view. findViewById(R.id.tv_random);
        llSoul = view. findViewById(R.id.ll_soul);
        tvSoul = view. findViewById(R.id.tv_soul);
        llFate = view. findViewById(R.id.ll_fate);
        tvFate = view. findViewById(R.id.tv_fate);
        llLove = view. findViewById(R.id.ll_love);
        tvLove = view. findViewById(R.id.tv_love);
    }

    private void initData() {

        //数据绑定
        mCloudTagAdapter=new CloudTagAdapter(getContext(),mStarList);
        mCloudView.setAdapter(mCloudTagAdapter);

        //监听点击事件
        mCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                startUserInfo(mStarList.get(position).getUserId());
            }
        });

        //绑定接口
        PairFriendHelper.getInstance().setOnPairResultListener(new PairFriendHelper.OnPairResultListener() {
            @Override
            public void OnPairListener(String userId) {
                startUserInfo(userId);
            }

            @Override
            public void OnPairFailListener() {
                mLodingView.hide();
                Toast.makeText(getActivity(), getString(R.string.text_pair_null), Toast.LENGTH_SHORT).show();
            }
        });

        loadStarUser();
    }

    /**
     * 跳转用户信息
     * @param userId
     */
    private  void startUserInfo(String userId){
        mLodingView.hide();
        UserInfoActivity.startActivity(getActivity(),userId);
    }

    /**
     * 加载星球用户
     */
    private void loadStarUser() {
        //从用户库中取抓取一定的好友进行匹配
        BmobManager.getInstance().queryAllUser(new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        if (mAllUserList.size() > 0) {
                            mAllUserList.clear();
                        }
                        if (mStarList.size() > 0) {
                            mStarList.clear();
                        }
                        mAllUserList = list;
                        //这里是所有的用户
                        int index = 100;
                        if (list.size() <= 100) {
                            index = list.size();
                        }
                        //直接填充
                        for (int i = 0; i < index; i++) {
                            IMUser imUser = list.get(i);
                            saveStarUser(imUser.getObjectId(),
                                    imUser.getNickName(),
                                    imUser.getPhoto());
                        }
                        //当请求数据已经加载出来的时候判断是否连接服务器
//                        if(CloudManager.getInstance().isConnect()){
//                            //已经连接，并且星球加载，则隐藏
//                            tv_connect_status.setVisibility(View.GONE);
//                        }
                        mCloudTagAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * 保存星球用户
     * @param userId
     * @param nickName
     * @param photoUrl
     */
    private void saveStarUser(String userId,String nickName,String photoUrl){
        StarModel starModel = new StarModel();
        starModel.setUserId(userId);
        starModel.setNickName(nickName);
        starModel.setPhotoUrl(photoUrl);
        mStarList.add(starModel);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_camera://扫描
                Intent intent = new Intent(getActivity(), ZxingActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.iv_add://添加好友
                startActivity(new Intent(getContext(), AddFriendActivity.class));
                break;
            case R.id.ll_random://随机
                pairUser(0);
                break;
            case R.id.ll_soul://灵魂
                if(TextUtils.isEmpty(BmobManager.getInstance().getUser().getConstellation())){
                    tv_null_text.setText(getString(R.string.text_star_par_tips_1));
                    DialogManager.getInstance().show(mNullDialogView);
                    return;
                }

                if(BmobManager.getInstance().getUser().getAge() == 0){
                    tv_null_text.setText(getString(R.string.text_star_par_tips_2));
                    DialogManager.getInstance().show(mNullDialogView);
                    return;
                }

                if(TextUtils.isEmpty(BmobManager.getInstance().getUser().getHobby())){
                    tv_null_text.setText(getString(R.string.text_star_par_tips_3));
                    DialogManager.getInstance().show(mNullDialogView);
                    return;
                }

                if(TextUtils.isEmpty(BmobManager.getInstance().getUser().getStatus())){
                    tv_null_text.setText(getString(R.string.text_star_par_tips_4));
                    DialogManager.getInstance().show(mNullDialogView);
                    return;
                }

                pairUser(1);
                break;
            case R.id.ll_fate://缘分
                pairUser(2);
                break;
            case R.id.ll_love://恋爱
                pairUser(3);
                break;
        }
    }

    /**
     * 匹配规则
     *
     * @param index
     */
    private void pairUser(int index) {
        switch (index) {
            case 0:
                mLodingView.show(getString(R.string.text_pair_random));
                break;
            case 1:
                mLodingView.show(getString(R.string.text_pair_soul));
                break;
            case 2:
                mLodingView.show(getString(R.string.text_pair_fate));
                break;
            case 3:
                mLodingView.show(getString(R.string.text_pair_love));
                break;
        }
        if (CommonUtils.isEmpty(mAllUserList)) {
            //计算
            PairFriendHelper.getInstance().pairUser(index, mAllUserList);
        } else {
            mLodingView.hide();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    LogUtils.i("result：" + result);
                    //Meet#c7a9b4794f
                    if (!TextUtils.isEmpty(result)) {
                        //是我们自己的二维码
                        if (result.startsWith("Meet")) {
                            String[] split = result.split("#");
                            LogUtils.i("split:" + split.toString());
                            if (split != null && split.length >= 2) {
                                try {
                                    UserInfoActivity.startActivity(getActivity(), split[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.text_toast_error_qrcode), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.text_toast_error_qrcode), Toast.LENGTH_SHORT).show();
                    }

                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getActivity(), getString(R.string.text_qrcode_fail), Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PairFriendHelper.getInstance().disposable();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.EVENT_SERVER_CONNECT_STATUS:
                if(event.isConnectStatus()){
                    if(CommonUtils.isEmpty(mStarList)){
                        tv_connect_status.setVisibility(View.GONE);
                    }
                }else{
                    tv_connect_status.setText(getString(R.string.text_star_pserver_fail));
                }
                break;
        }
    }
}
