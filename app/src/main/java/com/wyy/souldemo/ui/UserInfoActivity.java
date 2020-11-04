package com.wyy.souldemo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyy.framework.adapter.CommonAdapter;
import com.wyy.framework.adapter.CommonViewHolder;
import com.wyy.framework.base.BaseUIActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.Friend;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.cloud.CloudManager;
import com.wyy.framework.entity.Constants;
import com.wyy.framework.helper.GlideHelper;
import com.wyy.framework.manager.DialogManager;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.framework.view.DialogView;
import com.wyy.souldemo.R;
import com.wyy.souldemo.model.UserInfoModel;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;
/**
 * FileName: UserInfoActivity
 * Founder: LiuGuiLin
 * Profile: 用户信息
 */
public class UserInfoActivity extends BaseUIActivity implements View.OnClickListener {

    /**
     * 跳转
     * @param mContext
     * @param userId
     */
    public static void startActivity(Context mContext,String userId){
        Intent intent = new Intent(mContext,UserInfoActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID,userId);
        mContext.startActivity(intent);
    }

    private RelativeLayout llBack;
    private CircleImageView ivUserPhoto;
    private TextView tvNickname;
    private TextView tvDesc;
    private RecyclerView mUserInfoView;
    private Button btnAddFriend;
    private LinearLayout llIsFriend;
    private Button btnChat;
    private Button btnAudioChat;
    private Button btnVideoChat;

    //提示框控件
    private DialogView mAddFriendDialogView;
    private EditText et_msg;
    private TextView tv_cancel;
    private TextView tv_add_friend;

    private CommonAdapter<UserInfoModel> mUserInfoAdapter;
    private List<UserInfoModel> mUserInfoList = new ArrayList<>();



    //个人信息颜色
    private int[] mColor = {0x881E90FF, 0x8800FF7F, 0x88FFD700, 0x88FF6347, 0x88F08080, 0x8840E0D0};

    //用户ID
    private String userId = "";

    private IMUser imUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initView();
        initListener();
    }

    private void initListener() {
        llBack.setOnClickListener(this);
        btnAddFriend.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        btnAudioChat.setOnClickListener(this);
        btnVideoChat.setOnClickListener(this);

    }

    private void initView() {
        initAddFriendDialog();
        //获取用户ID
        userId = getIntent().getStringExtra(Constants.INTENT_USER_ID);

        llBack = (RelativeLayout) findViewById(R.id.ll_back);
        ivUserPhoto = (CircleImageView) findViewById(R.id.iv_user_photo);
        tvNickname = (TextView) findViewById(R.id.tv_nickname);
        tvDesc = (TextView) findViewById(R.id.tv_desc);
        mUserInfoView = (RecyclerView) findViewById(R.id.mUserInfoView);
        mUserInfoView.setLayoutManager(new GridLayoutManager(this, 3));

        btnAddFriend = (Button) findViewById(R.id.btn_add_friend);
        llIsFriend = (LinearLayout) findViewById(R.id.ll_is_friend);
        btnChat = (Button) findViewById(R.id.btn_chat);
        btnAudioChat = (Button) findViewById(R.id.btn_audio_chat);
        btnVideoChat = (Button) findViewById(R.id.btn_video_chat);

        //列表
        mUserInfoAdapter=new CommonAdapter<>(mUserInfoList, new CommonAdapter.OnBindDataListener<UserInfoModel>() {
            @Override
            public void onBindViewHolder(UserInfoModel model, CommonViewHolder viewHolder, int type, int position) {
                viewHolder.getView(R.id.ll_bg).setBackgroundColor(model.getBgColor());
                viewHolder.setText(R.id.tv_type, model.getTitle());
                viewHolder.setText(R.id.tv_content, model.getContent());
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_user_info_item;
            }
        });
        mUserInfoView.setAdapter(mUserInfoAdapter);
        queryUserInfo();
    }

    /**
     * 添加好友的提示框
     */
    private void initAddFriendDialog() {
        mAddFriendDialogView = DialogManager.getInstance().initView(this, R.layout.dialog_send_friend);
        et_msg = (EditText) mAddFriendDialogView.findViewById(R.id.et_msg);
        tv_cancel = (TextView) mAddFriendDialogView.findViewById(R.id.tv_cancel);
        tv_add_friend = (TextView) mAddFriendDialogView.findViewById(R.id.tv_add_friend);

        //输入框显示名称
        et_msg.setText(getString(R.string.text_me_info_tips) + BmobManager.getInstance().getUser().getNickName());
        tv_cancel.setOnClickListener(this);
        tv_add_friend.setOnClickListener(this);
    }

    /**
     * 查询用户信息
     */
    private void queryUserInfo() {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        //查询用户信息
        BmobManager.getInstance().queryObjectIdUser(userId, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if(e==null){
                    if(CommonUtils.isEmpty(list)){
                        imUser=list.get(0);
                        updateUserInfo(imUser);
                    }
                }
            }
        });

        //判断好友关系
        BmobManager.getInstance().queryMyFriends(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if(e==null){
                    if(CommonUtils.isEmpty(list)){
                        //你有一个好友列表
                        for(int i = 0; i < list.size(); i++){
                            Friend friend = list.get(i);
                            //判断这个对象中的id是否跟我目前的userId相同
                            if(friend.getFriendUser().getObjectId().equals(userId)){
                                //你们是好友关系
                                btnAddFriend.setVisibility(View.GONE);
                                llIsFriend.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 更新用户信息
     *
     * @param imUser
     */
    private void updateUserInfo(IMUser imUser) {
        //设置基本属性
        GlideHelper.loadUrl(UserInfoActivity.this, imUser.getPhoto(),
                ivUserPhoto);
        tvNickname.setText(imUser.getNickName());
        tvDesc.setText(imUser.getDesc());
        //性别 年龄 生日 星座 爱好 单身状态
        addUserInfoModel(mColor[0], getString(R.string.text_me_info_sex),
                imUser.isSex() ? getString(R.string.text_me_info_boy)
                        : getString(R.string.text_me_info_girl));

        addUserInfoModel(mColor[1], getString(R.string.text_me_info_age),
                imUser.getAge() + getString(R.string.text_search_age));

        addUserInfoModel(mColor[2], getString(R.string.text_me_info_birthday), imUser.getBirthday());
        addUserInfoModel(mColor[3], getString(R.string.text_me_info_constellation), imUser.getConstellation());
        addUserInfoModel(mColor[4], getString(R.string.text_me_info_hobby), imUser.getHobby());
        addUserInfoModel(mColor[5], getString(R.string.text_me_info_status), imUser.getStatus());
        //刷新数据
        mUserInfoAdapter.notifyDataSetChanged();
    }

    /**
     * 添加数据
     *
     * @param color
     * @param title
     * @param content
     */
    private void addUserInfoModel(int color, String title, String content) {
        UserInfoModel model = new UserInfoModel();
        model.setBgColor(color);
        model.setTitle(title);
        model.setContent(content);
        mUserInfoList.add(model);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_add_friend:
                String msg = et_msg.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    msg = getString(R.string.text_user_info_add_friend);
                    return;
                }
                CloudManager.getInstance().sendTextMessage(msg,
                        CloudManager.TYPE_ADD_FRIEND,userId);
                DialogManager.getInstance().hide(mAddFriendDialogView);
                Toast.makeText(this, getString(R.string.text_user_resuest_succeed), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hide(mAddFriendDialogView);
                break;
            case R.id.ll_back:
                finish();
                break;
            case R.id.btn_add_friend:
                DialogManager.getInstance().show(mAddFriendDialogView);
                break;
            case R.id.btn_chat://聊天
                ChatActivity.startActivity(UserInfoActivity.this,userId,
                        imUser.getNickName(),imUser.getPhoto());
                break;
            case R.id.btn_audio_chat://语音聊天
                if(!checkWindowPermissions()){
                    requestWindowPermissions();
                }else{
                    CloudManager.getInstance().startAudioCall(this,userId);
                }
                break;
            case R.id.btn_video_chat://视频聊天
                if(!checkWindowPermissions()){
                    requestWindowPermissions();
                }else{
                    CloudManager.getInstance().startVideoCall(this,userId);
                }
                break;


        }

    }
}
