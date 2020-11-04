package com.wyy.framework.bmob;

import android.content.Context;

import com.wyy.framework.utils.CommonUtils;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import io.rong.calllib.RongCallCommon;
import io.rong.imlib.model.Conversation;

/**
 * FileName: BmobManager
 * Founder: LiuGuiLin
 * Profile: Bmob 管理类
 */
public class BmobManager {

    //f8efae5debf319071b44339cf51153fc
    //46db3352a737d64d8aaaa7082a9fa101
    //8378d6e92be3719b9fcc29cbafba80cf
    //e34c6158d06cfea9a028dbc6997a84b9
    //d148f9638f8ed07b312a248ff1c6578c

//    private static final String BMOB_SDK_ID = "46db3352a737d64d8aaaa7082a9fa101";
    private static final String BMOB_SDK_ID = "287fdef7b0c7ebb710f2102b3f919384";
    private static final String BMOB_NEW_DOMAIN = "http://sdk.cilc.cloud/8/";

    private volatile static BmobManager mInstance = null;

    private BmobManager() { }

    public static BmobManager getInstance() {
        if (mInstance == null) {
            synchronized (BmobManager.class) {
                if (mInstance == null) {
                    mInstance = new BmobManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化Bmob
     *
     * @param mContext
     */
    public void initBmob(Context mContext) {
        //如果Bmob绑定独立域名，则需要在初始化之前重置
        Bmob.resetDomain(BMOB_NEW_DOMAIN);
        Bmob.initialize(mContext, BMOB_SDK_ID);
    }

    /**
     * 是否登录
     * @return
     */
    public boolean isLogin() {
        return BmobUser.isLogin();
    }

    /**
     * 获取本地对象
     * @return
     */
    public IMUser getUser(){
        return BmobUser.getCurrentUser(IMUser.class);
    }

    /**
     * 发送短信验证码
     *
     * @param phone    手机号码
     * @param listener 回调
     */
    public void requestSMSI(String phone, QueryListener<Integer> listener){
        BmobSMS.requestSMSCode(phone,"",listener);
    }

    /**
     * 通过手机号码注册或者登陆
     *
     * @param phone    手机号码
     * @param code     短信验证码
     * @param listener 回调
     */
    public void signOrLoginByMobilePhone(String phone, String code, LogInListener<IMUser> listener){
        BmobUser.signOrLoginByMobilePhone(phone,code,listener);
    }

    /**
     * 第一次上传头像
     * @param nickName
     * @param file
     */
    public void uploadFirstPhoto(final String nickName, File file, final OnUploadPhotoListener listener){
        final IMUser imUser = getUser();
        final BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    //上传成功
                    imUser.setNickName(nickName);
                    imUser.setPhoto(bmobFile.getFileUrl());

                    imUser.setTokenNickName(nickName);
                    imUser.setTokenPhoto(bmobFile.getFileUrl());
                    //更新用户信息
                    imUser.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                listener.OnUpdateDone();
                            }else{
                                listener.OnUpdateFail(e);
                            }
                        }
                    });
                }else{
                  listener.OnUpdateFail(e);
                }
            }
        });
    }

    public interface OnUploadPhotoListener{
        void OnUpdateDone();
        void OnUpdateFail(BmobException e);
    }

    /**
     *根据手机号码查询用户
     * @param phone
     */
    public void queryPhoneUser(String phone, FindListener<IMUser> listener){
        baseQuery("mobilePhoneNumber",phone,listener);
    }

    /**
     * 根据objectId查询用户
     * @param objectId
     * @param listener
     */
    public void queryObjectIdUser(String objectId,FindListener<IMUser> listener){
        baseQuery("objectId",objectId,listener);
    }


    /**
     * 查询我的好友
     *
     * @param listener
     */
    public void queryMyFriends(FindListener<Friend> listener) {
        BmobQuery<Friend> query = new BmobQuery<>();
        query.addWhereEqualTo("user",getUser());
        query.findObjects(listener);
    }


    /**
     * 查询所有的用户
     * @param listener
     */
    public void queryAllUser(FindListener<IMUser> listener){
        BmobQuery<IMUser> query=new BmobQuery<>();
        query.findObjects(listener);
    }


    /**
     * 查询基类
     * @param key
     * @param value
     * @param listener
     */
    public void baseQuery(String key,String value,FindListener<IMUser> listener){
        BmobQuery<IMUser> query=new BmobQuery<>();
        query.addWhereEqualTo(key,value);
        query.findObjects(listener);
    }

    /**
     * 添加好友
     *
     * @param imUser
     * @param listener
     */
    public void addFriend(IMUser imUser, SaveListener<String> listener){
        Friend friend = new Friend();
        friend.setUser(getUser());
        friend.setFriendUser(imUser);
        friend.save(listener);
    }

    /**
     * 通过ID添加好友
     *
     * @param id
     * @param listener
     */
    public void addFriend(String id, final SaveListener<String> listener) {
        queryObjectIdUser(id, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if(e==null){
                    if(CommonUtils.isEmpty(list)){
                        IMUser imUser = list.get(0);
                        addFriend(imUser,listener);
                    }
                }
            }
        });
    }


    /**
     * 查询私有库
     *
     * @param listener
     */
    public void queryPrivateSet(FindListener<PrivateSet> listener) {
        BmobQuery<PrivateSet> query = new BmobQuery<>();
        query.findObjects(listener);
    }

    /**
     * 添加私有库
     *
     * @param listener
     */
    public void addPrivateSet(SaveListener<String> listener) {
        PrivateSet set = new PrivateSet();
        set.setUserId(getUser().getObjectId());
        set.setPhone(getUser().getMobilePhoneNumber());
        set.save(listener);
    }

    /**
     * 删除私有库
     *
     * @param id
     * @param listener
     */
    public void delPrivateSet(String id, UpdateListener listener) {
        PrivateSet set = new PrivateSet();
        set.setObjectId(id);
        set.delete(listener);
    }




    /**
     * 查询更新
     *
     * @param listener
     */
    public void queryUpdateSet(FindListener<UpdateSet> listener) {
        BmobQuery<UpdateSet> bmobQuery = new BmobQuery<>();
        bmobQuery.findObjects(listener);
    }

    /**
     * 账号密码登录
     *
     * @param userName
     * @param pw
     * @param listener
     */
    public void loginByAccount(String userName, String pw, SaveListener<IMUser> listener) {
        IMUser imUser = new IMUser();
        imUser.setUsername(userName);
        imUser.setPassword(pw);
        imUser.login(listener);
    }

    /**
     * 添加到缘分池中
     *
     * @param listener
     */
    public void addFateSet(SaveListener<String> listener) {
        FateSet set = new FateSet();
        set.setUserId(getUser().getObjectId());
        set.save(listener);
    }

    /**
     * 查询缘分池
     *
     * @param listener
     */
    public void queryFateSet(FindListener<FateSet> listener) {
        BmobQuery<FateSet> query = new BmobQuery<>();
        query.findObjects(listener);
    }

    /**
     * 删除缘分池
     *
     * @param id
     * @param listener
     */
    public void delFateSet(String id, UpdateListener listener) {
        FateSet set = new FateSet();
        set.setObjectId(id);
        set.delete(listener);
    }

    /**
     * 同步控制台信息至本地缓存
     */
    public void fetchUserInfo(FetchUserInfoListener<BmobUser> listener) {
        BmobUser.fetchUserInfo(listener);
    }



    /**
     * 发布广场
     *
     * @param mediaType 媒体类型
     * @param text      文本
     * @param path      路径
     */
    public void pushSquare(int mediaType, String text, String path, SaveListener<String> listener) {
        SquareSet squareSet = new SquareSet();
        squareSet.setUserId(getUser().getObjectId());
        squareSet.setPushTime(System.currentTimeMillis());

        squareSet.setText(text);
        squareSet.setMediaUrl(path);
        squareSet.setPushType(mediaType);
        squareSet.save(listener);
    }

    /**
     * 查询所有的广场的数据
     *
     * @param listener
     */
    public void queryAllSquare(FindListener<SquareSet> listener) {
        BmobQuery<SquareSet> query = new BmobQuery<>();
        query.findObjects(listener);
    }


}
