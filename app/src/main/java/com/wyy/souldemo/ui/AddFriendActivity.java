package com.wyy.souldemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyy.framework.adapter.CommonAdapter;
import com.wyy.framework.adapter.CommonViewHolder;
import com.wyy.framework.base.BaseBackActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.helper.GlideHelper;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.framework.utils.LogUtils;
import com.wyy.souldemo.R;
import com.wyy.souldemo.adapter.AddFriendAdapter;
import com.wyy.souldemo.model.AddFriendModel;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * 添加好友
 */
public class AddFriendActivity extends BaseBackActivity implements View.OnClickListener {
    //标题
    public static final int TYPE_TITLE=0;
    //内容
    public static final int TYPE_CONTENT=1;
    private LinearLayout llToContact;
    private EditText etPhone;
    private ImageView ivSearch;
    private RecyclerView mSearchResultView;
    private View include_empty_view;
    private CommonAdapter<AddFriendModel> mAddFriendAdapter;
    private List<AddFriendModel> mList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        initView();
        initListener();
    }

    private void initListener() {
        llToContact.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
    }

    private void initView() {
        llToContact = (LinearLayout) findViewById(R.id.ll_to_contact);
        etPhone = (EditText) findViewById(R.id.et_phone);
        ivSearch = (ImageView) findViewById(R.id.iv_search);
        mSearchResultView = (RecyclerView) findViewById(R.id.mSearchResultView);
        include_empty_view=findViewById(R.id.include_empty_view);

        //列表的数据
        mSearchResultView.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

       mAddFriendAdapter=new CommonAdapter<AddFriendModel>(mList, new CommonAdapter.OnMoreBindDataListener<AddFriendModel>() {
           @Override
           public int getItemType(int position) {
               return mList.get(position).getType();
           }

           @Override
           public void onBindViewHolder(AddFriendModel model, CommonViewHolder viewHolder, int type, int position) {
                if(type==TYPE_TITLE){
                    viewHolder.setText(R.id.tv_title,model.getTitle());
                }else if(type==TYPE_CONTENT){
                    //设置头像
                    viewHolder.setImageUrl(AddFriendActivity.this,R.id.iv_photo,model.getPhoto());
                    //设置性别
                    viewHolder.setImageResource(R.id.iv_sex,model.isSex()
                            ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                    //设置昵称
                    viewHolder.setText(R.id.tv_nickname,model.getNickName());
                    //设置年龄
                    viewHolder.setText(R.id.tv_age,model.getAge()+getString(R.string.text_search_age));
                    //设置描述
                    viewHolder.setText(R.id.tv_desc,model.getDesc());

                    //点击事件
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserInfoActivity.startActivity(AddFriendActivity.this,
                                    model.getUserId());
                        }
                    });

                }
           }
           @Override
           public int getLayoutId(int type) {
               if(type==TYPE_TITLE){
                   return R.layout.layout_search_title_item;
               }else if(type==TYPE_CONTENT){
                   return R.layout.layout_search_user_item;
               }
               return 0;
           }
       });

        mSearchResultView.setAdapter(mAddFriendAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_to_contact: //从通讯录导入
                //处理权限
                startActivity(new Intent(this,ContactFirendActivity.class));
//                if(checkPermissiones(Manifest.permission.READ_CONTACTS)){
//                    startActivity(new Intent(this,ContactFirendActivity.class));
//                }else{
//                    requestPermission(new String[]{Manifest.permission.READ_CONTACTS});
//                }
                break;
            case R.id.iv_search:
                //搜索手机号码
                queryPhoneUser();
                break;
        }
    }

    /**
     * 查询手机号码
     */
    private void queryPhoneUser() {
        //1.获取号码
        String phone = etPhone.getText().toString().trim();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, getString(R.string.text_login_phone_null), Toast.LENGTH_SHORT).show();
            return;
        }
        //2.过滤自己
        String phoneNumber = BmobManager.getInstance().getUser().getMobilePhoneNumber();
        if(phone.equals(phoneNumber)){
            Toast.makeText(this, "不能查询自己", Toast.LENGTH_SHORT).show();
            return;
        }


        //3.查询
        BmobManager.getInstance().queryPhoneUser(phone, new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if(e!=null){
                    return;
                }
                if(CommonUtils.isEmpty(list)){
                    IMUser imUser = list.get(0);
                    LogUtils.i("imUser+++++: "+imUser.toString());
                    include_empty_view.setVisibility(View.GONE);
                    mSearchResultView.setVisibility(View.VISIBLE);
                    //每次查询有数据清空
                    mList.clear();
                    addTitle("查询结果");
                    addContent(imUser);
                    mAddFriendAdapter.notifyDataSetChanged();
                    //推荐
                    pushUser();

                }else{
                    //显示空数据
                    include_empty_view.setVisibility(View.VISIBLE);
                    mSearchResultView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 查询所有的好友
     */
    private void pushUser() {
        BmobManager.getInstance().queryAllUser(new FindListener<IMUser>() {
            @Override
            public void done(List<IMUser> list, BmobException e) {
                if(e==null){
                    if(CommonUtils.isEmpty(list)){
                        addTitle("推荐好友");
                        int num=(list.size()<=100?list.size():100);
                        for(int i = 0; i < num; i++){
                            //不能推荐自己
                            String phoneNumber = BmobManager.getInstance().getUser().getMobilePhoneNumber();
                            if(list.get(i).getMobilePhoneNumber().equals(phoneNumber)){
                                //跳过本次循环
                                continue;
                            }
                            addContent(list.get(i));
                        }
                        mAddFriendAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * 添加头部
     * @param title
     */
    private void addTitle(String title){
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_TITLE);
        model.setTitle(title);
        mList.add(model);
    }

    /**
     * 添加内容
     * @param imUser
     */
    private void addContent(IMUser imUser){
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_CONTENT);
        model.setUserId(imUser.getObjectId());
        model.setPhoto(imUser.getPhoto());
        model.setSex(imUser.isSex());
        model.setAge(imUser.getAge());
        model.setNickName(imUser.getNickName());
        model.setDesc(imUser.getDesc());
        mList.add(model);
    }






}
