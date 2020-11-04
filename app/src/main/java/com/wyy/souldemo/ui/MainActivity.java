package com.wyy.souldemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.wyy.framework.base.BaseUIActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.entity.Constants;
import com.wyy.framework.gson.TokenBean;
import com.wyy.framework.java.SimulationData;
import com.wyy.framework.manager.DialogManager;
import com.wyy.framework.manager.HttpManager;
import com.wyy.framework.view.DialogView;
import com.wyy.souldemo.service.CloudService;
import com.wyy.framework.utils.LogUtils;
import com.wyy.framework.utils.SpUtils;
import com.wyy.souldemo.R;
import com.wyy.souldemo.fragment.ChatFragment;
import com.wyy.souldemo.fragment.MeFragment;
import com.wyy.souldemo.fragment.SquareFragment;
import com.wyy.souldemo.fragment.StarFragment;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseUIActivity implements View.OnClickListener {

    //星球
    private ImageView ivStar;
    private TextView tvStar;
    private LinearLayout llStar;
    private StarFragment mStarFragment=null;
    private FragmentTransaction mStarTransaction=null;

    //广场
    private ImageView ivSquare;
    private TextView tvSquare;
    private LinearLayout llSquare;
    private SquareFragment mSquareFragment=null;
    private FragmentTransaction mSquareTransaction=null;

    //聊天
    private ImageView ivChat;
    private LinearLayout llChat;
    private TextView tvChat;
    private ChatFragment mChatFragment=null;
    private FragmentTransaction mChatTransaction=null;

    //我的
    private ImageView ivMe;
    private LinearLayout llMe;
    private TextView tvMe;
    private MeFragment mMeFragment=null;
    private FragmentTransaction mMeTransaction=null;

    private DialogView mUploadView;

    //跳转上传头像的回调
    public static final int UPLOAD_REQUEST_CODE=1002;

    private Disposable disposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();
    }

    private void initListener() {
        llStar.setOnClickListener(this);
        llSquare.setOnClickListener(this);
        llChat.setOnClickListener(this);
        llMe.setOnClickListener(this);
    }

    private void initData() { }

    private void initView() {
        requestPermiss();

        llStar = (LinearLayout) findViewById(R.id.ll_star);
        ivStar = (ImageView) findViewById(R.id.iv_star);
        tvStar = (TextView) findViewById(R.id.tv_star);
        llSquare = (LinearLayout) findViewById(R.id.ll_square);
        ivSquare = (ImageView) findViewById(R.id.iv_square);
        tvSquare = (TextView) findViewById(R.id.tv_square);
        llChat = (LinearLayout) findViewById(R.id.ll_chat);
        ivChat = (ImageView) findViewById(R.id.iv_chat);
        tvChat = (TextView) findViewById(R.id.tv_chat);
        llMe = (LinearLayout) findViewById(R.id.ll_me);
        ivMe = (ImageView) findViewById(R.id.iv_me);
        tvMe = (TextView) findViewById(R.id.tv_me);

        initFragment();
        //切换选项卡
        checkMainTab(0);

        //检查token
        checkToken();
        //模拟数据
        //SimulationData.testData();

    }

    /**
     * 检查token
     */
    private void checkToken() {
        //获取toke需要3个参数 1.用户id 2.头像地址 3.昵称
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        if (!TextUtils.isEmpty(token)) {
            //启动云服务去连接融云服务
            startCloudService();
        }else{
            //1.有三个参数
            String tokenPhoto= BmobManager.getInstance().getUser().getTokenPhoto();
            String tokenName=BmobManager.getInstance().getUser().getTokenNickName();
            if(!TextUtils.isEmpty(tokenPhoto)&&!TextUtils.isEmpty(tokenName)){
                //创建token
                createToken();
            }else{
                //创建上传头像提示框
                createUploadDialog();
            }
        }
    }


    //创建上传头像提示框
    private void createUploadDialog() {
        mUploadView = DialogManager.getInstance().initView(this,R.layout.dialog_first_upload);
        DialogManager.getInstance().show(mUploadView);
        ImageView iv_go_upload = mUploadView.findViewById(R.id.iv_go_upload);
        iv_go_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hide(mUploadView);
                FirstUploadActivity.startActivity(MainActivity.this,UPLOAD_REQUEST_CODE);
            }
        });

    }

    /**
     * 启动云服务去连接融云服务
     */
    private void startCloudService() {
        LogUtils.i("startCloudService");
        startService(new Intent(this, CloudService.class));
        //检查更新
      //  new UpdateHelper(this).updateApp(null);
    }

    //创建token
    private void createToken() {
        LogUtils.e("createToken++++++++++");
        /**
         * 1.去融云后台获取Token
         * 2.连接融云
         */
        final HashMap<String, String> map = new HashMap<>();
        map.put("userId", BmobManager.getInstance().getUser().getObjectId());
        map.put("name", BmobManager.getInstance().getUser().getTokenNickName());
        map.put("portraitUri", BmobManager.getInstance().getUser().getTokenPhoto());

        //通过OkHttp请求Token
       disposable=Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                //执行请求过程
                String json = HttpManager.getInstance().postCloudToken(map);
                LogUtils.i("json:" + json);
                emitter.onNext(json);
                emitter.onComplete();
            }
            //线程调度
        }).subscribeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        parsingCloudToken(s);
                    }
                });
    }

    /**
     * 解析Token
     *
     * @param s
     */
    private void parsingCloudToken(String s) {
        try {
            LogUtils.i("parsingCloudToken:" + s);
            TokenBean tokenBean = new Gson().fromJson(s, TokenBean.class);
            if (tokenBean.getCode() == 200) {
                if (!TextUtils.isEmpty(tokenBean.getToken())) {
                    //保存Token
                    SpUtils.getInstance().putString(Constants.SP_TOKEN, tokenBean.getToken());
                    startCloudService();
                }
            } else if (tokenBean.getCode() == 2007) {
                Toast.makeText(this, "注册人数已达上限，请替换成自己的Key", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LogUtils.i("parsingCloudToken:" + e.toString());
        }
    }


    //初始化fragment
    private void initFragment() {
        //星球
        if(mStarFragment==null){
            mStarFragment=new StarFragment();
            mStarTransaction=getSupportFragmentManager().beginTransaction();
            mStarTransaction.add(R.id.mMainLayout,mStarFragment);
            mStarTransaction.commit();
        }

        //广场
        if(mSquareFragment==null){
            mSquareFragment=new SquareFragment();
            mSquareTransaction=getSupportFragmentManager().beginTransaction();
            mSquareTransaction.add(R.id.mMainLayout,mSquareFragment);
            mSquareTransaction.commit();
        }

        //聊天
        if(mChatFragment==null){
            mChatFragment=new ChatFragment();
            mChatTransaction=getSupportFragmentManager().beginTransaction();
            mChatTransaction.add(R.id.mMainLayout,mChatFragment);
            mChatTransaction.commit();
        }

        //我的
        if(mMeFragment==null){
            mMeFragment=new MeFragment();
            mMeTransaction=getSupportFragmentManager().beginTransaction();
            mMeTransaction.add(R.id.mMainLayout,mMeFragment);
            mMeTransaction.commit();
        }
    }

    /**
     * 显示Fragment
     * @param fragment
     */
    private void showFragment(Fragment fragment){
        if(fragment!=null){
            FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 隐藏所有的Fragment
     *
     * @param transaction
     */
    private void hideAllFragment(FragmentTransaction transaction){
        if(mStarFragment!=null){
            transaction.hide(mStarFragment);
        }
        if(mSquareFragment!=null){
            transaction.hide(mSquareFragment);
        }
        if(mChatFragment!=null){
            transaction.hide(mChatFragment);
        }
        if(mMeFragment!=null){
            transaction.hide(mMeFragment);
        }
    }

    /**
     * 切换主页选项卡
     */
    private void checkMainTab(int index){
        switch (index){
            case 0:
                showFragment(mStarFragment);
                ivStar.setImageResource(R.mipmap.img_star_p);
                ivSquare.setImageResource(R.mipmap.img_square);
                ivChat.setImageResource(R.mipmap.img_chat);
                ivMe.setImageResource(R.mipmap.img_me);

                tvStar.setTextColor(getResources().getColor(R.color.colorAccent));
                tvSquare.setTextColor(Color.BLACK);
                tvChat.setTextColor(Color.BLACK);
                tvMe.setTextColor(Color.BLACK);

                llStar.setBackgroundResource(R.color.colorTab);
                llSquare.setBackgroundColor(Color.WHITE);
                llChat.setBackgroundColor(Color.WHITE);
                llMe.setBackgroundColor(Color.WHITE);

                break;
            case 1:
                showFragment(mSquareFragment);
                ivStar.setImageResource(R.mipmap.img_star);
                ivSquare.setImageResource(R.mipmap.img_square_p);
                ivChat.setImageResource(R.mipmap.img_chat);
                ivMe.setImageResource(R.mipmap.img_me);

                tvStar.setTextColor(Color.BLACK);
                tvSquare.setTextColor(getResources().getColor(R.color.colorAccent));
                tvChat.setTextColor(Color.BLACK);
                tvMe.setTextColor(Color.BLACK);

                llStar.setBackgroundColor(Color.WHITE);
                llSquare.setBackgroundResource(R.color.colorTab);
                llChat.setBackgroundColor(Color.WHITE);
                llMe.setBackgroundColor(Color.WHITE);

                break;
            case 2:
                showFragment(mChatFragment);
                ivStar.setImageResource(R.mipmap.img_star);
                ivSquare.setImageResource(R.mipmap.img_square);
                ivChat.setImageResource(R.mipmap.img_chat_p);
                ivMe.setImageResource(R.mipmap.img_me);

                tvStar.setTextColor(Color.BLACK);
                tvSquare.setTextColor(Color.BLACK);
                tvChat.setTextColor(getResources().getColor(R.color.colorAccent));
                tvMe.setTextColor(Color.BLACK);

                llStar.setBackgroundColor(Color.WHITE);
                llSquare.setBackgroundColor(Color.WHITE);
                llChat.setBackgroundResource(R.color.colorTab);
                llMe.setBackgroundColor(Color.WHITE);
                break;
            case 3:
                showFragment(mMeFragment);
                ivStar.setImageResource(R.mipmap.img_star);
                ivSquare.setImageResource(R.mipmap.img_square);
                ivChat.setImageResource(R.mipmap.img_chat);
                ivMe.setImageResource(R.mipmap.img_me_p);

                tvStar.setTextColor(Color.BLACK);
                tvSquare.setTextColor(Color.BLACK);
                tvChat.setTextColor(Color.BLACK);
                tvMe.setTextColor(getResources().getColor(R.color.colorAccent));

                llStar.setBackgroundColor(Color.WHITE);
                llSquare.setBackgroundColor(Color.WHITE);
                llChat.setBackgroundColor(Color.WHITE);
                llMe.setBackgroundResource(R.color.colorTab);
                break;
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if(mStarFragment!=null&&fragment instanceof StarFragment){
            mStarFragment=(StarFragment) fragment;
        }
        if(mSquareFragment!=null&&fragment instanceof SquareFragment){
            mSquareFragment=(SquareFragment) fragment;
        }
        if(mChatFragment!=null&&fragment instanceof ChatFragment){
            mChatFragment=(ChatFragment) fragment;
        }
        if(mMeFragment!=null&&fragment instanceof MeFragment){
            mMeFragment=(MeFragment) fragment;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_star:
                checkMainTab(0);
                break;
            case R.id.ll_square:
                checkMainTab(1);
                break;
            case R.id.ll_chat:
                checkMainTab(2);
                break;
            case R.id.ll_me:
                checkMainTab(3);
                break;
        }
    }

    /**
     * 请求权限
     */
    private void requestPermiss() {
        //危险权限
        request(new OnPermissionsResult() {
            @Override
            public void OnSuccess() {

            }

            @Override
            public void OnFail(List<String> noPermissions) {
                LogUtils.i("noPermissions:" + noPermissions.toString());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode==UPLOAD_REQUEST_CODE){
                checkToken();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    //第一次按下时间
    private long firstClick;

    /**
     * 再按一次退出
     */
    public void AppExit() {
        if (System.currentTimeMillis() - this.firstClick > 2000L) {
            this.firstClick = System.currentTimeMillis();
            Toast.makeText(this, getString(R.string.text_main_exit), Toast.LENGTH_LONG).show();
            return;
        }
        finish();
    }


}
