package com.wyy.souldemo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.gson.Gson;
import com.wyy.framework.adapter.CommonAdapter;
import com.wyy.framework.adapter.CommonViewHolder;
import com.wyy.framework.base.BaseBackActivity;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.cloud.CloudManager;
import com.wyy.framework.entity.Constants;
import com.wyy.framework.event.EventManager;
import com.wyy.framework.event.MessageEvent;
import com.wyy.framework.gson.TextBean;
import com.wyy.framework.helper.FileHelper;
import com.wyy.framework.manager.MapManager;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.framework.utils.LanguaueUtils;
import com.wyy.framework.utils.LogUtils;
import com.wyy.souldemo.R;
import com.wyy.souldemo.fragment.ChatFragment;
import com.wyy.souldemo.model.ChatModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;

public class ChatActivity extends BaseBackActivity implements View.OnClickListener {

    public static void startActivity(Context mContext,String userId,String userName,String userPhoto){
        Intent intent=new Intent(mContext,ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID,userId);
        intent.putExtra(Constants.INTENT_USER_NAME,userName);
        intent.putExtra(Constants.INTENT_USER_PHOTO,userPhoto);
        mContext.startActivity(intent);
    }



    //左边
    public static final int TYPE_LEFT_TEXT = 0;
    public static final int TYPE_LEFT_IMAGE = 1;
    public static final int TYPE_LEFT_LOCATION = 2;

    //右边
    public static final int TYPE_RIGHT_TEXT = 3;
    public static final int TYPE_RIGHT_IMAGE = 4;
    public static final int TYPE_RIGHT_LOCATION = 5;

    private static final int LOCATION_REQUESTCODE = 1888;


    private LinearLayout llChatBg;
    private RecyclerView mChatView;
    private EditText etInputMsg;
    private Button btnSendMsg;
    private LinearLayout llVoice;
    private LinearLayout llCamera;
    private LinearLayout llPic;
    private LinearLayout llLocation;

    //对方用户信息
    private String yourUserId;
    private String yourUserName;
    private String yourUserPhoto;
    private String Iaddress;
    //自己的信息
    private String meUserPhoto;

    //列表
    private CommonAdapter<ChatModel> mChatAdapter;
    private List<ChatModel> mList=new ArrayList<>();

    //图片文件
    private File uploadFile=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        initListener();
    }

    private void initListener() {
        btnSendMsg.setOnClickListener(this);
        llVoice.setOnClickListener(this);
        llCamera.setOnClickListener(this);
        llPic.setOnClickListener(this);
        llLocation.setOnClickListener(this);
    }

    private void initView() {
        llChatBg = (LinearLayout) findViewById(R.id.ll_chat_bg);
        mChatView = (RecyclerView) findViewById(R.id.mChatView);
        etInputMsg = (EditText) findViewById(R.id.et_input_msg);
        btnSendMsg = (Button) findViewById(R.id.btn_send_msg);
        llVoice = (LinearLayout) findViewById(R.id.ll_voice);
        llCamera = (LinearLayout) findViewById(R.id.ll_camera);
        llPic = (LinearLayout) findViewById(R.id.ll_pic);
        llLocation = (LinearLayout) findViewById(R.id.ll_location);

        mChatView.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter=new CommonAdapter<>(mList, new CommonAdapter.OnMoreBindDataListener<ChatModel>() {
            @Override
            public int getItemType(int position) {
                return mList.get(position).getType();
            }

            @Override
            public void onBindViewHolder(ChatModel model, CommonViewHolder viewHolder, int type, int position) {
                double la = model.getLa();
                double lo = model.getLo();
                String address = model.getAddress();
                String imgUrl = model.getImgUrl();
                switch (model.getType()){
                    case TYPE_LEFT_TEXT://左文本
                        viewHolder.setText(R.id.tv_left_text,model.getText());
                        viewHolder.setImageUrl(ChatActivity.this,R.id.iv_left_photo,yourUserPhoto);
                        break;
                    case TYPE_RIGHT_TEXT://右文本
                        viewHolder.setText(R.id.tv_right_text,model.getText());
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, meUserPhoto);
                        break;
                    case TYPE_LEFT_IMAGE://左图片
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_left_img, model.getImgUrl());
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_left_photo, yourUserPhoto);

                        viewHolder.getView(R.id.iv_left_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImagePreviewActivity.startActivity(ChatActivity.this,
                                        true,model.getImgUrl());
                            }
                        });
                        break;
                    case TYPE_RIGHT_IMAGE://右图片
                        if(TextUtils.isEmpty(model.getImgUrl())){
                            if(model.getLocalFile()!=null){
                                //加载本地文件
                                viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_img, model.getLocalFile());
                                viewHolder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ImagePreviewActivity.startActivity(ChatActivity.this,
                                                false,model.getLocalFile().getPath());
                                    }
                                });
                            }
                        }else {
                            viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_img, model.getImgUrl());
                            viewHolder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImagePreviewActivity.startActivity(ChatActivity.this,
                                            true,model.getImgUrl());
                                }
                            });
                        }
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, meUserPhoto);
                        break;
                    case TYPE_LEFT_LOCATION://左地址
                        viewHolder.setImageUrl(ChatActivity.this,R.id.iv_left_photo,yourUserPhoto);
                        viewHolder.setImageUrl(ChatActivity.this,R.id.iv_left_location_img,model.getMapUrl());
                        viewHolder.setText(R.id.tv_left_address,model.getAddress());
//                        MapManager.getInstance().address2poi()
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LocationActivity.startActivity(ChatActivity.this,false,model.getLa(),model.getLo(),model.getAddress(),LOCATION_REQUESTCODE);
                            }
                        });
                        break;
                    case TYPE_RIGHT_LOCATION://右地址
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, meUserPhoto);
                        viewHolder.setImageUrl(ChatActivity.this,R.id.iv_right_location_img,model.getMapUrl());
//                        LogUtils.i("model"+model.getImgUrl());
//                        View viewHolderView = viewHolder.getView(R.id.iv_right_img);
//                        Glide.with(ChatActivity.this).load(model.getImgUrl()).transform(new CenterCrop()).into((ImageView) viewHolderView);
//                        viewHolder.setImageUrl(ChatActivity.this, viewHolder.getView(R.id.iv_right_img),"https://restapi.amap.com/v3/staticmap?location=113.56889401955901,25.99794833058689&zoom=15&size=300*200&markers=mid,,A:113.56889401955901,25.99794833058689&key=496d99bcd2e0ca4e928169d6060856bd\n");
                        viewHolder.setText(R.id.tv_right_address,model.getAddress());
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LocationActivity.startActivity(ChatActivity.this,false,model.getLa(),model.getLo(),model.getAddress(),LOCATION_REQUESTCODE);
                            }
                        });
                        break;
                }
            }

            @Override
            public int getLayoutId(int type) {
                if(type==TYPE_LEFT_TEXT){
                    return R.layout.layout_chat_left_text;
                }else if(type == TYPE_RIGHT_TEXT){
                    return R.layout.layout_chat_right_text;
                }else if (type == TYPE_LEFT_IMAGE) {
                    return R.layout.layout_chat_left_img;
                }else if (type == TYPE_RIGHT_IMAGE) {
                    return R.layout.layout_chat_right_img;
                }else if (type == TYPE_LEFT_LOCATION) {
                    return R.layout.layout_chat_left_location;
                }else if (type == TYPE_RIGHT_LOCATION) {
                    return R.layout.layout_chat_right_location;
                }
                return 0;
            }
        });

        mChatView.setAdapter(mChatAdapter);
        loadMeInfo();

        queryMessage();
    }




    /**
     * 查询聊天记录
     */
    private void queryMessage() {
        CloudManager.getInstance().getHistoryMessage(yourUserId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if(CommonUtils.isEmpty(messages)){
                    try {
                        parsingListMessage(messages);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    queryRemoteMessage();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.i("errorCode: "+errorCode);
            }
        });
    }

    /**
     * 解析历史记录
     * @param messages
     */
    private void parsingListMessage(List<Message> messages)  {
        //倒序
        Collections.reverse(messages);
        //遍历
        for(int i = 0; i < messages.size(); i++){
            Message m = messages.get(i);
            String objectName = m.getObjectName();
            if(objectName.equals(CloudManager.MSG_TEXT_NAME)){
                TextMessage textMessage =(TextMessage) m.getContent();
                String msg = textMessage.getContent();
                try {
                    TextBean textBean = new Gson().fromJson(msg, TextBean.class);
                    if(textBean.getType().equals(CloudManager.TYPE_TEXT)){
                        //添加ui 判断 你 | 我
                        if(m.getSenderUserId().equals(yourUserId)){
                            addText(0,textBean.getMsg());
                        }else{
                            addText(1,textBean.getMsg());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }else if(objectName.equals(CloudManager.MSG_IMAGE_NAME)){
                ImageMessage imageMessage=(ImageMessage) m.getContent();
                String url = imageMessage.getRemoteUri().toString();
                if(!TextUtils.isEmpty(url)){
                    if(m.getSenderUserId().equals(yourUserId)){
                        addImage(0,url);
                    }else{
                        addImage(1,url);
                    }
                }
            }else if(objectName.equals(CloudManager.MSG_LOCATION_NAME)){
                LocationMessage locationMessage = (LocationMessage) m.getContent();
                if(m.getSenderUserId().equals(yourUserId)){
                    addLoaction(0,locationMessage.getLat(),locationMessage.getLng(),locationMessage.getPoi());
                }else{
                    addLoaction(1,locationMessage.getLat(),locationMessage.getLng(),locationMessage.getPoi());
                }
            }
        }


    }

    /**
     * 查询服务器的历史记录
     */
    private void queryRemoteMessage() {
        CloudManager.getInstance().getRemoteHistoryMessage(yourUserId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if(CommonUtils.isEmpty(messages)){
                    try {
                        parsingListMessage(messages);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }


            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.i("errorCode: "+errorCode);
            }
        });

    }



    /**
     * 加载自我信息
     */
    private void loadMeInfo() {
        Intent intent = getIntent();
        yourUserId = intent.getStringExtra(Constants.INTENT_USER_ID);
        yourUserName = intent.getStringExtra(Constants.INTENT_USER_NAME);
        yourUserPhoto = intent.getStringExtra(Constants.INTENT_USER_PHOTO);

        meUserPhoto= BmobManager.getInstance().getUser().getPhoto();

        //设置标题
        if(!TextUtils.isEmpty(yourUserName)){
            getSupportActionBar().setTitle(yourUserName);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send_msg:
                String inputMsg = etInputMsg.getText().toString().trim();
                if(TextUtils.isEmpty(inputMsg)){
                    return;
                }
                CloudManager.getInstance().sendTextMessage(inputMsg,CloudManager.TYPE_TEXT,yourUserId);
                addText(1,inputMsg);
                //文本框清空
                etInputMsg.setText("");
                break;
            case R.id.ll_voice:

                break;
            case R.id.ll_camera:
                FileHelper.getInstance().toCamera(this);
                break;
            case R.id.ll_pic:
                FileHelper.getInstance().toAlbum(this);
                break;
            case R.id.ll_location:
                LocationActivity.startActivity(this,true,0,0,"",LOCATION_REQUESTCODE);
                break;

        }
    }


    /**
     * 添加数据的基类
     * @param model
     */
    private void baseAddItem(ChatModel model){
        mList.add(model);
        mChatAdapter.notifyDataSetChanged();
        //滑动到底部
        mChatView.scrollToPosition(mList.size()-1);
    }

    /**
     * 添加左边的文字
     * index 0:左边 1：右边
     * @param text
     */
    private void addText(int index,String text){
        ChatModel model = new ChatModel();
        if(index == 0){
            model.setType(TYPE_LEFT_TEXT);
        }else{
            model.setType(TYPE_RIGHT_TEXT);
        }
        model.setText(text);
        baseAddItem(model);
    }


    /**
     * 添加图片
     * @param index
     * @param url
     */
    private void addImage(int index,String url){
        ChatModel model = new ChatModel();
        if(index==0){
            model.setType(TYPE_LEFT_IMAGE);
        }else{
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setImgUrl(url);
        baseAddItem(model);
    }

    /**
     *  添加图片
     * @param index
     * @param file
     */
    private void addImage(int index,File file){
        ChatModel model = new ChatModel();
        if(index==0){
            model.setType(TYPE_LEFT_IMAGE);
        }else{
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setLocalFile(file);
        baseAddItem(model);
    }


    private void addLoaction(int index,double la,double lo,String address){
        ChatModel model = new ChatModel();
        LogUtils.i("index"+index);
        LogUtils.i("la"+la);
        LogUtils.i("lo"+lo);
        LogUtils.i("address"+address);
        if(index==0){
            model.setType(TYPE_LEFT_LOCATION);
        }else{
            model.setType(TYPE_RIGHT_LOCATION);
        }
        model.setLa(la);
        model.setLo(lo);
        model.setAddress(address);
        model.setMapUrl(MapManager.getInstance().getMapUrl(la,lo));
        baseAddItem(model);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if(!event.getUserId().equals(yourUserId)){
            return;
        }
        switch (event.getType()) {
            case EventManager.FLAG_SEND_TEXT:
                addText(0,event.getText());
                break;
            case EventManager.FLAG_SEND_IMAGE:
                addImage(0,event.getImgUrl());
                break;
            case EventManager.FLAG_SEND_LOCATION:
                addLoaction(0,event.getLa(),event.getLo(),event.getAddress());
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==FileHelper.CAMEAR_REQUEST_CODE){
                uploadFile=FileHelper.getInstance().getTempFile();
            }else if(requestCode==FileHelper.ALBUM_REQUEST_CODE){
                Uri uri = data.getData();
                if(uri!=null){
                    String path = FileHelper.getInstance().getRealPathFromURI(this, uri);
                    if(!TextUtils.isEmpty(path)){
                        uploadFile=new File(path);
                    }
                }
            }else if (requestCode == LOCATION_REQUESTCODE){
                double la = data.getDoubleExtra("la", 0);
                double lo = data.getDoubleExtra("lo", 0);
//                String address = data.getStringExtra("addres");

                MapManager.getInstance().poi2address(la, lo, new MapManager.OnPoi2AddressGeocodeListener() {
                    @Override
                    public void poi2address(String address) {
                        LogUtils.i("la"+la);
                        LogUtils.i("lo"+lo);
                        LogUtils.i("address9"+address);
                        Iaddress = ""+address;
                        LogUtils.i("9999"+Iaddress);
                        CloudManager.getInstance().sendLoactionManager(yourUserId,la,lo,Iaddress);
                        addLoaction(1,la,lo,Iaddress);
                    }
                });
//                LogUtils.i("la"+la);
//                LogUtils.i("lo"+lo);
//                LogUtils.i("9999"+Iaddress);



            }
            if(uploadFile!=null){
                //发送图片消息
                CloudManager.getInstance().sendImageMessage(yourUserId,uploadFile);
                //更新列表
                addImage(1,uploadFile);
                uploadFile = null;
            }
        }

    }
}
