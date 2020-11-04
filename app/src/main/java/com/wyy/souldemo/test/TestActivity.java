package com.wyy.souldemo.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wyy.framework.bmob.MyData;
import com.wyy.framework.utils.LogUtils;
import com.wyy.framework.view.TouchPictureV;
import com.wyy.souldemo.R;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private com.wyy.framework.view.TouchPictureV TouchPictureV;
    private Button btnAdd;
    private Button btnDel;
    private Button btnQuery;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {


        btnAdd = (Button) findViewById(R.id.btn_add);
        btnDel = (Button) findViewById(R.id.btn_del);
        btnQuery = (Button) findViewById(R.id.btn_query);
        btnUpdate = (Button) findViewById(R.id.btn_update);
        btnAdd.setOnClickListener(this);
        btnDel.setOnClickListener(this);
        btnQuery.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add:
                MyData myData=new MyData();
                myData.setName("张三");
                myData.setSex(1);
                myData.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if(e==null){
                            //94a535679e
                            LogUtils.i("新增成功"+s);
                        }
                    }
                });
                break;
            case R.id.btn_del:
                MyData data=new MyData();
                data.setObjectId("94a535679e");
                data.delete(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e==null){
                            Toast.makeText(TestActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(TestActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.btn_query:
                BmobQuery<MyData> bmobQuery=new BmobQuery<>();
//                bmobQuery.getObject("16e3f73ee1", new QueryListener<MyData>() {
//                @Override
//                public void done(MyData myData, BmobException e) {
//                    if(e==null){
//                        LogUtils.i("myData:"+myData.getName());
//                    }else{
//                        LogUtils.e("myData:"+e.toString());
//                    }
//
//                }
//            });

                //条件查新
                bmobQuery.addWhereEqualTo("name","张三");
                bmobQuery.findObjects(new FindListener<MyData>() {
                    @Override
                    public void done(List<MyData> list, BmobException e) {
                        if(e==null){
                            if(list!=null&&list.size()>0){
                                for(int i = 0; i < list.size(); i++){
                                    LogUtils.i(list.get(i).getName()+"++"+list.get(i).getSex());
                                }

                            }

                        }

                    }
                });



                break;
            case R.id.btn_update:

                MyData p2 = new MyData();
                p2.setName("北京朝阳");
                p2.update("a6e0cb9c12", new UpdateListener() {

                    @Override
                    public void done(BmobException e) {
                        if(e==null){
                            LogUtils.i("更新成功:"+p2.getUpdatedAt());
                        }else{
                            LogUtils.e("更新失败：" + e.getMessage());
                        }
                    }

                });

                break;


        }


    }
}
