package com.wyy.souldemo.ui;

import android.Manifest;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyy.framework.adapter.CommonAdapter;
import com.wyy.framework.adapter.CommonViewHolder;
import com.wyy.framework.base.BaseBackActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.framework.utils.LogUtils;
import com.wyy.souldemo.R;
import com.wyy.souldemo.adapter.AddFriendAdapter;
import com.wyy.souldemo.model.AddFriendModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 *  从通讯录导入
 */
public class ContactFirendActivity extends BaseBackActivity {
    //标题
    public static final int TYPE_TITLE=0;
    //内容
    public static final int TYPE_CONTENT=1;


    private RecyclerView mContactView;
    private Map<String,String> mContactMap=new HashMap<>();
    private CommonAdapter<AddFriendModel> mAddFriendAdapter;
    private List<AddFriendModel> mList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            requestPermission(new String[]{Manifest.permission.READ_CONTACTS});
        }
        setContentView(R.layout.activity_contact_firend);
        initView();
    }

    private void initView() {
        mContactView = (RecyclerView) findViewById(R.id.mContactView);
        mContactView.setLayoutManager(new LinearLayoutManager(this));
        mContactView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

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
                    viewHolder.setImageUrl(ContactFirendActivity.this,R.id.iv_photo,model.getPhoto());
                    //设置性别
                    viewHolder.setImageResource(R.id.iv_sex,model.isSex()
                            ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                    //设置昵称
                    viewHolder.setText(R.id.tv_nickname,model.getNickName());
                    //设置年龄
                    viewHolder.setText(R.id.tv_age,model.getAge()+getString(R.string.text_search_age));
                    //设置描述
                    viewHolder.setText(R.id.tv_desc,model.getDesc());

                    //通讯录
                    if(model.isContact()){
                        viewHolder.setVisibility(R.id.ll_contact_info,View.VISIBLE);
                        viewHolder.setText(R.id.tv_contact_name,model.getContactName());
                        viewHolder.setText(R.id.tv_contact_phone,model.getContactPhone());
                    }

                    //点击事件
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserInfoActivity.startActivity(ContactFirendActivity.this,
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

        mContactView.setAdapter(mAddFriendAdapter);

        loadContact();
        loadUser();

    }

    /**
     * 加载用户
     */
    private void loadUser() {
        if(mContactMap.size()>0){
            for(final Map.Entry<String,String> entry:mContactMap.entrySet()){
                BmobManager.getInstance().queryPhoneUser(entry.getValue(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if(e==null){
                            if(CommonUtils.isEmpty(list)){
                                IMUser imUser=list.get(0);
                                addContent(imUser,entry.getKey(),entry.getValue());
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 加载联系人
     */
    private void loadContact() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,null,null,null);

        String name;
        String phone;
        while (cursor.moveToNext()){
            name=cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phone =cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));
            LogUtils.i("name :"+name+" phone :"+phone );

            phone=phone.replace("","").replace("-","");
            mContactMap.put(name,phone);
        }
        cursor.close();
    }


    /**
     * 添加内容
     * @param imUser
     */
    private void addContent(IMUser imUser,String name,String phone){
        AddFriendModel model = new AddFriendModel();
        model.setType(AddFriendAdapter.TYPE_CONTENT);
        model.setUserId(imUser.getObjectId());
        model.setPhoto(imUser.getPhoto());
        model.setSex(imUser.isSex());
        model.setAge(imUser.getAge());
        model.setNickName(imUser.getNickName());
        model.setDesc(imUser.getDesc());
        model.setContact(true);
        model.setContactName(name);
        model.setContactPhone(phone);
        mList.add(model);
        mAddFriendAdapter.notifyDataSetChanged();
    }



}
