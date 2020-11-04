package com.wyy.souldemo.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.wyy.framework.base.BasePageAdapter;
import com.wyy.framework.base.BaseUIActivity;
import com.wyy.framework.manager.MediaPlayerManager;
import com.wyy.framework.utils.AnimUtils;
import com.wyy.souldemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * FileName: GuideActivity
 * Founder: LiuGuiLin
 * Profile: 引导页
 */
public class GuideActivity extends BaseUIActivity implements View.OnClickListener {
    /**
     * 1.ViewPager : 适配器|帧动画播放
     * 2.小圆点的逻辑
     * 3.歌曲的播放
     * 4.属性动画旋转
     * 5.跳转
     */

    private ViewPager mViewPager;
    private ImageView ivMusicSwitch;
    private TextView tvGuideSkip;
    private ImageView ivGuidePoint1;
    private ImageView ivGuidePoint2;
    private ImageView ivGuidePoint3;

    private View view1;
    private View view2;
    private View view3;
    private List<View> mPageList = new ArrayList<>();
    private BasePageAdapter mPageAdapter;

    private ImageView iv_guide_star;
    private ImageView iv_guide_night;
    private ImageView iv_guide_smile;

    //音乐播放
    private MediaPlayerManager mGuideMusic;

    //旋转动画
    private ObjectAnimator mAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        ivMusicSwitch.setOnClickListener(this);
        tvGuideSkip.setOnClickListener(this);

        //小圆点逻辑
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectPoint(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    private void initData() {
    }

    /**
     * 动态选择小圆点
     * @param position
     */
    private void selectPoint(int position) {
        switch (position){
            case 0:
                ivGuidePoint1.setImageResource(R.drawable.img_guide_point_p);
                ivGuidePoint2.setImageResource(R.drawable.img_guide_point);
                ivGuidePoint3.setImageResource(R.drawable.img_guide_point);
                break;
            case 1:
                ivGuidePoint1.setImageResource(R.drawable.img_guide_point);
                ivGuidePoint2.setImageResource(R.drawable.img_guide_point_p);
                ivGuidePoint3.setImageResource(R.drawable.img_guide_point);
                break;
            case 2:
                ivGuidePoint1.setImageResource(R.drawable.img_guide_point);
                ivGuidePoint2.setImageResource(R.drawable.img_guide_point);
                ivGuidePoint3.setImageResource(R.drawable.img_guide_point_p);
                break;
        }
    }

    /**
     * 播放音乐
     */
    private void startMusic() {
        mGuideMusic=new MediaPlayerManager();
        mGuideMusic.setLooping(true);
        AssetFileDescriptor fileDescriptor=getResources().openRawResourceFd(R.raw.guide);
        mGuideMusic.startPlay(fileDescriptor);

        mGuideMusic.setOnComplteionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mGuideMusic.startPlay(fileDescriptor);
            }
        });

        //旋转动画
        mAnim= AnimUtils.rotation(ivMusicSwitch);
        mAnim.start();
    }


    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        ivMusicSwitch = (ImageView) findViewById(R.id.iv_music_switch);
        tvGuideSkip = (TextView) findViewById(R.id.tv_guide_skip);
        ivGuidePoint1 = (ImageView) findViewById(R.id.iv_guide_point_1);
        ivGuidePoint2 = (ImageView) findViewById(R.id.iv_guide_point_2);
        ivGuidePoint3 = (ImageView) findViewById(R.id.iv_guide_point_3);

        view1 = View.inflate(this, R.layout.layout_pager_guide_1, null);
        view2 = View.inflate(this, R.layout.layout_pager_guide_2, null);
        view3 = View.inflate(this, R.layout.layout_pager_guide_3, null);

        mPageList.add(view1);
        mPageList.add(view2);
        mPageList.add(view3);

        //预加载
        mViewPager.setOffscreenPageLimit(mPageList.size());
        mPageAdapter = new BasePageAdapter(mPageList);
        mViewPager.setAdapter(mPageAdapter);

        //帧动画
        iv_guide_star = view1.findViewById(R.id.iv_guide_star);
        iv_guide_night = view2.findViewById(R.id.iv_guide_night);
        iv_guide_smile = view3.findViewById(R.id.iv_guide_smile);

        //播放帧动画
        AnimationDrawable animStar = (AnimationDrawable) iv_guide_star.getBackground();
        animStar.start();

        AnimationDrawable animNight = (AnimationDrawable) iv_guide_night.getBackground();
        animNight.start();

        AnimationDrawable animSmile= (AnimationDrawable) iv_guide_smile.getBackground();
        animSmile.start();

        //歌曲的逻辑
        startMusic();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_music_switch:
                if(mGuideMusic.MEDIA_STATUS==MediaPlayerManager.MEDIA_STATUS_PAUSE){
                    mAnim.start();
                    mGuideMusic.continuePlay();
                    ivMusicSwitch.setImageResource(R.drawable.img_guide_music);
                }else if(mGuideMusic.MEDIA_STATUS==MediaPlayerManager.MEDIA_STATUS_PLAY){
                    mAnim.pause();
                    mGuideMusic.pausePlay();
                    ivMusicSwitch.setImageResource(R.drawable.img_guide_music_off);
                }
                break;

            case R.id.tv_guide_skip:
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGuideMusic.stopPlay();
    }
}
