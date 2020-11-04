package com.wyy.souldemo.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.cloud.CloudManager;
import com.wyy.framework.db.CallRecord;
import com.wyy.framework.db.LitePalHelper;
import com.wyy.framework.db.NewFriend;
import com.wyy.framework.entity.Constants;
import com.wyy.framework.event.EventManager;
import com.wyy.framework.event.MessageEvent;
import com.wyy.framework.gson.TextBean;
import com.wyy.framework.helper.GlideHelper;
import com.wyy.framework.helper.WindowHelper;
import com.wyy.framework.manager.MediaPlayerManager;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.framework.utils.LanguaueUtils;
import com.wyy.framework.utils.LogUtils;
import com.wyy.framework.utils.SpUtils;
import com.wyy.framework.utils.TimeUtils;
import com.wyy.souldemo.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

/**
 * Profile: 云服务
 */
public class CloudService extends Service implements View.OnClickListener {
    //计时
    private static final int H_TIME_WHAT = 1000;
    //通话时间
    private int callTimer = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull android.os.Message msg) {
            switch (msg.what){
                case H_TIME_WHAT:
                    callTimer++;
                    String time = TimeUtils.formatDuring(callTimer * 1000);
                    audioTvStatus.setText(time);
                    video_tv_time.setText(time);
                    mSmallTime.setText(time);
                    mHandler.sendEmptyMessageDelayed(H_TIME_WHAT,1000);
                    break;
            }
            return false;
        }
    });


    private Disposable disposable;

    //音频窗口
    private View mFullAudioView;
    private CircleImageView audioIvPhoto;//头像
    private TextView audioTvStatus;//状态
    private LinearLayout audioLlRecording; //录音按钮
    private ImageView audioIvRecording;//录音图片
    private LinearLayout audioLlAnswer;//接听按钮
    private ImageView audioIvAnswer;//接听图片
    private LinearLayout audioLlHangup;//挂断按钮
    private ImageView audioIvHangup;//挂断图片
    private LinearLayout audioLlHf;//免提按钮
    private ImageView audioIvHf;//免提图片
    private ImageView audioIvSmall;//最小化

    //视频窗口
    private View mFullVideoView;
    //大窗口
    private RelativeLayout video_big_video;
    //小窗口
    private RelativeLayout video_small_video;
    //头像
    private CircleImageView video_iv_photo;
    //昵称
    private TextView video_tv_name;
    //状态
    private TextView video_tv_status;
    //个人信息窗口
    private LinearLayout video_ll_info;
    //时间
    private TextView video_tv_time;
    //接听
    private LinearLayout video_ll_answer;
    //挂断
    private LinearLayout video_ll_hangup;
    //最小化的音频View
    private WindowManager.LayoutParams lpSmallView;
    private View mSmallAudioView;
    //时间
    private TextView mSmallTime;

    //媒体类
    private MediaPlayerManager mAudioCallMedia;
    private MediaPlayerManager mAudioHangupMedia;

    //摄像类
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    //是否小窗口显示本地视频
    private boolean isSmallShowLocal = false;

    //拨打状态
    private int isCallTo = 0;
    //接听状态
    private int isReceiverTo = 0;
    //拨打还是接听
    private boolean isCallOrReceiver = true;

    //通话Id
    private String callId="";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initService();
        initWindow();
        linkCloudServer();
    }

    /**
     * 初始化服务
     */
    private void initService() {
        EventManager.register(this);
        //来电铃声
        mAudioCallMedia = new MediaPlayerManager();
        //挂断铃声
        mAudioHangupMedia = new MediaPlayerManager();
        //无限循环
        mAudioCallMedia.setOnComplteionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAudioCallMedia.startPlay(CloudManager.callAudioPath);
            }
        });
    }

    /**
     * 初始化窗口
     */
    private void initWindow() {
        //音频
        mFullAudioView=WindowHelper.getInstance().getView(R.layout.layout_chat_audio);
        audioIvPhoto = (CircleImageView)mFullAudioView. findViewById(R.id.audio_iv_photo);
        audioTvStatus = (TextView)mFullAudioView. findViewById(R.id.audio_tv_status);
        audioLlRecording = (LinearLayout)mFullAudioView. findViewById(R.id.audio_ll_recording);
        audioIvRecording = (ImageView)mFullAudioView. findViewById(R.id.audio_iv_recording);
        audioLlAnswer = (LinearLayout) mFullAudioView.findViewById(R.id.audio_ll_answer);
        audioIvAnswer = (ImageView)mFullAudioView. findViewById(R.id.audio_iv_answer);
        audioLlHangup = (LinearLayout)mFullAudioView. findViewById(R.id.audio_ll_hangup);
        audioIvHangup = (ImageView)mFullAudioView. findViewById(R.id.audio_iv_hangup);
        audioLlHf = (LinearLayout) mFullAudioView.findViewById(R.id.audio_ll_hf);
        audioIvHf = (ImageView)mFullAudioView. findViewById(R.id.audio_iv_hf);
        audioIvSmall = (ImageView)mFullAudioView. findViewById(R.id.audio_iv_small);

        audioLlRecording.setOnClickListener(this);
        audioLlAnswer.setOnClickListener(this);
        audioLlHangup.setOnClickListener(this);
        audioLlHf.setOnClickListener(this);
        audioIvSmall.setOnClickListener(this);

        //视频
        mFullVideoView=WindowHelper.getInstance().getView(R.layout.layout_chat_video);
        video_big_video = mFullVideoView.findViewById(R.id.video_big_video);
        video_small_video = mFullVideoView.findViewById(R.id.video_small_video);
        video_iv_photo = mFullVideoView.findViewById(R.id.video_iv_photo);
        video_tv_name = mFullVideoView.findViewById(R.id.video_tv_name);
        video_tv_status = mFullVideoView.findViewById(R.id.video_tv_status);
        video_ll_info = mFullVideoView.findViewById(R.id.video_ll_info);
        video_tv_time = mFullVideoView.findViewById(R.id.video_tv_time);
        video_ll_answer = mFullVideoView.findViewById(R.id.video_ll_answer);
        video_ll_hangup = mFullVideoView.findViewById(R.id.video_ll_hangup);

        video_ll_answer.setOnClickListener(this);
        video_ll_hangup.setOnClickListener(this);
        video_small_video.setOnClickListener(this);

        createSmallAudioView();
    }

    //是否移动
    private boolean isMove = false;
    //是否拖拽
    private boolean isDrag = false;
    private int mLastX;
    private int mLastY;

    /**
     * 创建最小化的音频窗口
     */
    private void createSmallAudioView() {
        lpSmallView=WindowHelper.getInstance().createLayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT);
        mSmallAudioView=WindowHelper.getInstance().getView(R.layout.layout_chat_small_audio);
        mSmallTime=mSmallAudioView.findViewById(R.id.mSmallTime);

        mSmallAudioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //最大化
                WindowHelper.getInstance().hideView(mSmallAudioView);
                WindowHelper.getInstance().showView(mFullAudioView);
            }
        });

        mSmallAudioView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int mStartX=(int) event.getRawX();
                int mStartY=(int) event.getRawY();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isMove=false;
                        isDrag=false;
                        mLastX =(int) event.getRawX();
                        mLastY =(int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //偏移量
                        int mX=mStartX-mLastX;
                        int mY=mStartY-mLastY;
                        if(isMove){
                            isDrag=true;
                        }else{
                            if(mX == 0 && mY == 0){
                                isMove=false;
                            }else{
                                isMove=true;
                                isDrag=true;
                            }
                        }
                        //移动
                        lpSmallView.x+=mX;
                        lpSmallView.y+=mY;
                        //重置坐标
                        mLastX=mStartX;
                        mLastY=mStartY;

                        WindowHelper.getInstance().updateView(mSmallAudioView,lpSmallView);
                        break;
                }
                return isDrag;
            }
        });
    }

    /**
     * 连接云服务
     */
    private void linkCloudServer() {
        //获取Token
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        LogUtils.e("token:" + token);
        //链接服务
        CloudManager.getInstance().connect(token);
        //接受消息
        CloudManager.getInstance().setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int i) {
                LogUtils.i("message: " + message);
                String objectName = message.getObjectName();
                //文本消息
                if (objectName.equals(CloudManager.MSG_TEXT_NAME)) {
                    //获取消息主题
                    TextMessage textMessage = (TextMessage) message.getContent();
                    String content = textMessage.getContent();
                    LogUtils.i("content: " + content);

                    //Gson
                    TextBean textBean = new Gson().fromJson(content, TextBean.class);
                    //普通消息
                    if (textBean.getType().equals(CloudManager.TYPE_TEXT)) {
                        MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_TEXT);
                        event.setText(textBean.getMsg());
                        event.setUserId(message.getSenderUserId());
                        EventManager.post(event);

                        //添加好友消息
                    } else if (textBean.getType().equals(CloudManager.TYPE_ADD_FRIEND)) {
                        //存入本地数据库
                        LogUtils.i("添加好友消息");
                        LitePalHelper.getInstance().saveNewFriend(
                                textBean.getMsg(), message.getSenderUserId());


                        //查询数据库如果有重复的则不添加
                        disposable = Observable.create(new ObservableOnSubscribe<List<NewFriend>>() {
                            @Override
                            public void subscribe(ObservableEmitter<List<NewFriend>> emitter) throws Exception {
                                emitter.onNext(LitePalHelper.getInstance().queryNewFriend());
                                emitter.onComplete();
                            }
                        }).subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<List<NewFriend>>() {
                                    @Override
                                    public void accept(List<NewFriend> newFriends) throws Exception {
                                        if (CommonUtils.isEmpty(newFriends)) {
                                            boolean isHave = false;
                                            for (int j = 0; j < newFriends.size(); j++) {
                                                NewFriend newFriend = newFriends.get(j);
                                                if (message.getSenderUserId().equals(newFriend.getId())) {
                                                    isHave = true;
                                                    break;
                                                }
                                            }
                                            //防止重复添加
                                            if (!isHave) {
                                                LitePalHelper.getInstance().saveNewFriend(
                                                        textBean.getMsg(),
                                                        message.getSenderUserId());
                                            }
                                        }

                                    }
                                });


                        //同意添加好友消息
                    } else if (textBean.getType().equals(CloudManager.TYPE_ARGEED_FRIEND)) {
                        //1.添加好友到列表
                        BmobManager.getInstance().addFriend(message.getSenderUserId(), new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if (e == null) {
                                    //2.刷新好友列表
                                    EventManager.post(EventManager.FLAG_UPDATE_FRIEND_LIST);

                                }

                            }
                        });

                    }
                } else if (objectName.equals(CloudManager.MSG_IMAGE_NAME)) {//图片消息
                    ImageMessage imageMessage = (ImageMessage) message.getContent();
                    String url = imageMessage.getRemoteUri().toString();
                    if (!TextUtils.isEmpty(url)) {
                        MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                        event.setImgUrl(url);
                        event.setUserId(message.getSenderUserId());
                        EventManager.post(event);
                    }
                }
                return false;
            }
        });

        //监听通话
        CloudManager.getInstance().setReceivedCallListener(new IRongReceivedCallListener() {
            @Override
            public void onReceivedCall(RongCallSession rongCallSession) {
                //检查设备可用
                if (!CloudManager.getInstance().isVoIPEnabled(CloudService.this)) {
                    return;
                }

                //呼叫端的Id
                String callerUserId = rongCallSession.getCallerUserId();

                //通话Id
                callId = rongCallSession.getCallId();

                //播放来电铃声
                mAudioCallMedia.startPlay(CloudManager.callAudioPath);
                //更新个人信息
                updateWindowInfo(0,rongCallSession.getMediaType(),callerUserId);

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    LogUtils.i("音频通话");
                    WindowHelper.getInstance().showView(mFullAudioView);
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    LogUtils.i("视频通话");
                    WindowHelper.getInstance().showView(mFullVideoView);
                }

                isReceiverTo=1;
                isCallOrReceiver=false;
            }
            @Override
            public void onCheckPermission(RongCallSession rongCallSession) {
                LogUtils.i("onCheckPermission:" + rongCallSession.toString());
            }
        });

        //监听通话状态
        CloudManager.getInstance().setVoIPCallListener(new IRongCallListener() {

            //电话拨出
            @Override
            public void onCallOutgoing(RongCallSession rongCallSession, SurfaceView surfaceView) {
                isCallOrReceiver=true;
                isCallTo=1;

                //更新信息
                String targetId = rongCallSession.getTargetId();
                //更新信息
                updateWindowInfo(1,rongCallSession.getMediaType(),targetId);
                //通话Id
                callId = rongCallSession.getCallId();

                if(rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)){
                    WindowHelper.getInstance().showView(mFullAudioView);
                }else if(rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)){
                    WindowHelper.getInstance().showView(mFullVideoView);
                    //显示摄像头
                    mLocalView=surfaceView;
                    video_big_video.addView(mLocalView);
                }
            }

            //已建立通话
            @Override
            public void onCallConnected(RongCallSession rongCallSession, SurfaceView surfaceView) {
                isCallTo=2;
                isReceiverTo=2;

                //关闭铃声
                if(mAudioCallMedia.isPlaying()){
                    mAudioCallMedia.stopPlay();
                }
                //开始计时
                mHandler.sendEmptyMessage(H_TIME_WHAT);

                if(rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)){
                    goneAudioView(true,false,true,true,true);
                }else if(rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)){
                    goneVideoView(false,true,true,false,true,true);
                    mLocalView=surfaceView;
                }
            }

            //通话结束
            @Override
            public void onCallDisconnected(RongCallSession rongCallSession, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {
                String callerUserId = rongCallSession.getCallerUserId();
                String recevierId = rongCallSession.getTargetId();

                //关闭计时
                mHandler.removeMessages(H_TIME_WHAT);
                //铃声挂断
                mAudioCallMedia.pausePlay();
                //播放挂断铃声
                mAudioHangupMedia.startPlay(CloudManager.callAudioHangup);
                //重置计时器
                callTimer=0;

                if(rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)){
                    if(isCallOrReceiver){
                        if(isCallTo==1){
                            saveAudioRecord(recevierId,CallRecord.CALL_STATUS_DIAL);
                        }else if(isCallTo==2){
                            saveAudioRecord(recevierId,CallRecord.CALL_STATUS_ANSWER);
                        }
                    }else{
                        if(isReceiverTo==1){
                            saveAudioRecord(callerUserId,CallRecord.CALL_STATUS_UN_ANSWER);
                        }else if(isReceiverTo==2){
                            saveAudioRecord(callerUserId,CallRecord.CALL_STATUS_ANSWER);
                        }
                    }
                }else if(rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)){
                    if(isCallOrReceiver){
                        if(isCallTo==1){
                            saveVideoRecord(recevierId,CallRecord.CALL_STATUS_DIAL);
                        }else if(isCallTo==2){
                            saveVideoRecord(recevierId,CallRecord.CALL_STATUS_ANSWER);
                        }
                    }else{
                        if(isReceiverTo==1){
                            saveVideoRecord(callerUserId,CallRecord.CALL_STATUS_UN_ANSWER);
                        }else if(isReceiverTo==2){
                            saveVideoRecord(callerUserId,CallRecord.CALL_STATUS_ANSWER);
                        }
                    }
                }
                //如果出现异常,可能无法退出
                WindowHelper.getInstance().hideView(mFullAudioView);
                WindowHelper.getInstance().hideView(mSmallAudioView);
                WindowHelper.getInstance().hideView(mFullVideoView);
                isCallTo=0;
                isReceiverTo=0;
            }

            //被叫端正在振铃
            @Override
            public void onRemoteUserRinging(String s) { }

            //被叫端加入通话
            @Override
            public void onRemoteUserJoined(String s, RongCallCommon.CallMediaType callMediaType, int i, SurfaceView surfaceView) {
                MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_CAMERA_VIEW);
                event.setmSurfaceView(surfaceView);
                EventManager.post(event);
            }

            //通话中的某一个参与者，邀请好友加入通话
            @Override
            public void onRemoteUserInvited(String s, RongCallCommon.CallMediaType callMediaType) { }

            //通话中的远端参与者离开。
            @Override
            public void onRemoteUserLeft(String s, RongCallCommon.CallDisconnectedReason callDisconnectedReason) { }

            // 当通话中的某一个参与者切换通话类型
            @Override
            public void onMediaTypeChanged(String s, RongCallCommon.CallMediaType callMediaType, SurfaceView surfaceView) { }

            // 通话过程中，发生异常。
            @Override
            public void onError(RongCallCommon.CallErrorCode callErrorCode) { }

            //远端参与者 camera 状态发生变化
            @Override
            public void onRemoteCameraDisabled(String s, boolean b) {

            }

            @Override
            public void onRemoteMicrophoneDisabled(String s, boolean b) {

            }

            @Override
            public void onNetworkReceiveLost(String s, int i) { }

            @Override
            public void onNetworkSendLost(int i, int i1) { }

            @Override
            public void onFirstRemoteVideoFrame(String s, int i, int i1) { }

            @Override
            public void onAudioLevelSend(String s) { }

            @Override
            public void onAudioLevelReceive(HashMap<String, String> hashMap) { }

            @Override
            public void onRemoteUserPublishVideoStream(String s, String s1, String s2, SurfaceView surfaceView) { }

            @Override
            public void onRemoteUserUnpublishVideoStream(String s, String s1, String s2) { }
        });

    }

    /**
     * 更新窗口上的用户信息
     * type 媒体类型
     * index 0:接收 1：拨打
     * @param id
     */
    private void updateWindowInfo(int index, RongCallCommon.CallMediaType type, String id) {
        //音频
        if(type.equals(RongCallCommon.CallMediaType.AUDIO)){
            if(index==0){
                goneAudioView(false,true,true,false,false);
            }else if(index==1){
                goneAudioView(false,false,true,false,false);
            }
            //视频
        }else if(type.equals(RongCallCommon.CallMediaType.VIDEO)){
            if(index==0){
                goneVideoView(true,false,false,true,true,false);
            }else if(index==1){
                goneVideoView(true,false,true,false,true,false);
            }
        }

        //加载信息
        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if(e==null){
                    if(CommonUtils.isEmpty(list)){
                        IMUser imUser = list.get(0);
                        //音频
                        if(type.equals(RongCallCommon.CallMediaType.AUDIO)){
                            //直接设置音频属性
                            GlideHelper.loadUrl(CloudService.this,imUser.getPhoto(),audioIvPhoto);
                            audioTvStatus.setText(imUser.getNickName()+"来电了...");
                            if(index==0){
                                audioTvStatus.setText(imUser.getNickName()+"来电了...");
                            }else if(index==1){
                                audioTvStatus.setText("正在呼叫"+imUser.getNickName()+"...");
                            }
                        //视频
                        }else if(type.equals(RongCallCommon.CallMediaType.VIDEO)){
                            GlideHelper.loadUrl(CloudService.this,imUser.getPhoto(),video_iv_photo);
                            audioTvStatus.setText(imUser.getNickName()+"来电了...");
                            if(index==0){
                                video_tv_status.setText(imUser.getNickName()+"视频来电了...");
                            }else if(index==1){
                                video_tv_status.setText("正在视频呼叫"+imUser.getNickName()+"...");
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 控制音频窗口的控件显示或者隐藏
     *
     * @param recording 录音
     * @param answer 接听
     * @param hangup 挂断
     * @param hf 免提
     * @param small 最小化
     */
    private void goneAudioView(boolean recording, boolean answer,
                               boolean hangup, boolean hf, boolean small){
        audioLlRecording.setVisibility(recording ? View.VISIBLE : View.GONE);
        audioLlAnswer.setVisibility(answer ? View.VISIBLE : View.GONE);
        audioLlHangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        audioLlHf.setVisibility(hf ? View.VISIBLE : View.GONE);
        audioIvSmall.setVisibility(small ? View.VISIBLE : View.GONE);
    }

    /**
     * 控制视频窗口的控件显示或者隐藏
     * @param info 个人信息
     * @param small 小窗口
     * @param big
     * @param answer 接听
     * @param hangup 挂断
     * @param time 时间
     */
    private void goneVideoView(boolean info, boolean small, boolean big,
                               boolean answer, boolean hangup, boolean time){

        video_ll_info.setVisibility(info ? View.VISIBLE : View.GONE);
        video_small_video.setVisibility(small ? View.VISIBLE : View.GONE);
        video_big_video.setVisibility(big ? View.VISIBLE : View.GONE);
        video_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        video_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        video_tv_time.setVisibility(time ? View.VISIBLE : View.GONE);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        EventManager.unregister(this);
    }

    private boolean isRecording = false;
    private boolean isHF = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.audio_ll_recording:
                //录音
                break;
            case R.id.audio_ll_answer:
            case R.id.video_ll_answer:
                //接听
                CloudManager.getInstance().acceptCall(callId);
                break;
            case R.id.audio_ll_hangup:
            case R.id.video_ll_hangup:
                //挂断
                CloudManager.getInstance().hangUpCall(callId);
                break;
            case R.id.audio_ll_hf:
                //免提
                isHF=!isHF;
                CloudManager.getInstance().setEnableSpeakerphone(isHF);
                audioIvHf.setImageResource(isHF ? R.drawable.img_hf_p : R.drawable.img_hf);
                break;
            case R.id.audio_iv_small:
                //最小化
                WindowHelper.getInstance().hideView(mFullAudioView);
                WindowHelper.getInstance().showView(mSmallAudioView,lpSmallView);

                break;
            case R.id.video_small_video:
                isSmallShowLocal = !isSmallShowLocal;
                //小窗切换
                updateVideoView();
                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.FLAG_SEND_CAMERA_VIEW:
                SurfaceView surfaceView = event.getmSurfaceView();
                if(surfaceView!=null){
                    mRemoteView=surfaceView;
                }
                updateVideoView();
                break;
        }
    }

    /**
     * 更新视频流
     */
    private void updateVideoView() {
        video_big_video.removeAllViews();
        video_small_video.removeAllViews();

        if(isSmallShowLocal){
            if(mLocalView!=null){
                video_small_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(true);
            }
            if (mRemoteView != null) {
                video_big_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(false);
            }
        }else{
            if(mLocalView!=null){
                video_big_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(false);
            }
            if (mRemoteView != null) {
                video_small_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(true);
            }
        }
    }

    /**
     *保存音频记录
     *
     * @param id
     * @param callStatus
     */
    private void saveAudioRecord(String id,int callStatus){
        LitePalHelper.getInstance()
                .saveCallRecord(id, CallRecord.MEDIA_TYPE_AUDIO,callStatus);
    }

    /**
     * 保存视频记录
     * @param id
     * @param callStatus
     */
    private void saveVideoRecord(String id, int callStatus) {
        LitePalHelper.getInstance()
                .saveCallRecord(id,CallRecord.MEDIA_TYPE_VIDEO,callStatus);
    }






}
