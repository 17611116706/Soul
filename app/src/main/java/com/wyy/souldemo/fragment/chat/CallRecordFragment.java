package com.wyy.souldemo.fragment.chat;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wyy.framework.adapter.CommonAdapter;
import com.wyy.framework.adapter.CommonViewHolder;
import com.wyy.framework.base.BaseFragment;
import com.wyy.framework.bmob.BmobManager;
import com.wyy.framework.bmob.IMUser;
import com.wyy.framework.db.CallRecord;
import com.wyy.framework.db.LitePalHelper;
import com.wyy.framework.utils.CommonUtils;
import com.wyy.souldemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * 通话记录
 */
public class CallRecordFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private Disposable disposable;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private View item_empty_view;
    private RecyclerView mCallRecordView;
    private SwipeRefreshLayout mCallRecordRefreshLayout;

    private List<CallRecord> mList = new ArrayList<>();
    private CommonAdapter<CallRecord> mCallRecordAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_call_record, container, false);
        initView(view);
        return view;
    }

    private void initView(final View view) {
        item_empty_view = view.findViewById(R.id.item_empty_view);
        mCallRecordView = view.findViewById(R.id.mCallRecordView);
        mCallRecordRefreshLayout = view.findViewById(R.id.mCallRecordRefreshLayout);
        mCallRecordRefreshLayout.setOnRefreshListener(this);

        mCallRecordView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCallRecordView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mCallRecordAdapter=new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<CallRecord>() {
            @Override
            public void onBindViewHolder(CallRecord model, CommonViewHolder viewHolder, int type, int position) {
                String mediaType="";
                if(model.getMediaType()==CallRecord.MEDIA_TYPE_AUDIO){
                    mediaType = getString(R.string.text_chat_record_audio);
                }else if(model.getMediaType()==CallRecord.MEDIA_TYPE_VIDEO){
                    mediaType = getString(R.string.text_chat_record_video);
                }
                String callStatus="";
                if(model.getCallStatus()==CallRecord.CALL_STATUS_UN_ANSWER){
                    callStatus=getString(R.string.text_call_record_un_answer);
                    viewHolder.setImageResource(R.id.iv_status_icon,R.drawable.img_un_answer_icon);
                    viewHolder.setTextColor(R.id.tv_nickname, Color.RED);
                    viewHolder.setTextColor(R.id.tv_type,Color.RED);
                }else if(model.getCallStatus()==CallRecord.CALL_STATUS_DIAL){
                    callStatus = getString(R.string.text_chat_record_dial);
                }else if(model.getCallStatus()==CallRecord.CALL_STATUS_ANSWER){
                    callStatus = getString(R.string.text_chat_record_answer);
                    viewHolder.setImageResource(R.id.iv_status_icon, R.drawable.img_answer_icon);
                }

                viewHolder.setText(R.id.tv_type, mediaType + " " + callStatus);
                viewHolder.setText(R.id.tv_time,dateFormat.format(model.getCallTime()));

                BmobManager.getInstance().queryObjectIdUser(model.getUserId(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if(e==null){
                            if(CommonUtils.isEmpty(list)){
                                IMUser imUser = list.get(0);
                                viewHolder.setText(R.id.tv_nickname,imUser.getNickName());
                            }
                        }
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_call_record;
            }
        });
        mCallRecordView.setAdapter(mCallRecordAdapter);
    }

    @Override
    public void onRefresh() {
        if(mCallRecordRefreshLayout.isRefreshing()){
            queryCallRecord();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        queryCallRecord();
    }

    /**
     * 查询通话记录
     */
    private void queryCallRecord() {
        mCallRecordRefreshLayout.setRefreshing(true);
        disposable = Observable.create(new ObservableOnSubscribe<List<CallRecord>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CallRecord>> emitter) throws Exception {
                emitter.onNext(LitePalHelper.getInstance().queryCallRecord());
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CallRecord>>() {
                    @Override
                    public void accept(List<CallRecord> callRecords) throws Exception {
                        mCallRecordRefreshLayout.setRefreshing(false);
                        if(CommonUtils.isEmpty(callRecords)){
                            if(mList.size() > 0){
                                mList.clear();
                            }
                            mList.addAll(callRecords);
                            mCallRecordAdapter.notifyDataSetChanged();
                            item_empty_view.setVisibility(View.GONE);
                            mCallRecordView.setVisibility(View.VISIBLE);
                        }else{
                            item_empty_view.setVisibility(View.VISIBLE);
                            mCallRecordView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

}
