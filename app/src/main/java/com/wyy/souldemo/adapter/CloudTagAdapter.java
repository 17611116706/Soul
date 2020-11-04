package com.wyy.souldemo.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moxun.tagcloudlib.view.TagsAdapter;
import com.wyy.framework.R;
import com.wyy.framework.helper.GlideHelper;
import com.wyy.souldemo.model.StarModel;

import java.util.List;

/**
 * 3D球体适配器
 */
public class CloudTagAdapter extends TagsAdapter {

    private Context mContext;
    private List<StarModel> mList;
    private LayoutInflater inflater;

    public CloudTagAdapter(Context mContext, List<StarModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        View view = inflater.inflate(R.layout.layout_star_view_item, null);
        ImageView iv_star_icon=view.findViewById(R.id.iv_star_icon);
        TextView tv_star_name=view.findViewById(R.id.tv_star_name);

        StarModel model = mList.get(position);
        GlideHelper.loadUrl(mContext,model.getPhotoUrl(),iv_star_icon);
        tv_star_name.setText(model.getNickName());

//        switch (position%10){
//            case 0:
//                iv_star_icon.setImageResource(R.drawable.img_guide_star_1);
//                break;
//            case 1:
//                iv_star_icon.setImageResource(R.drawable.img_guide_star_2);
//                break;
//            case 2:
//                iv_star_icon.setImageResource(R.drawable.img_guide_star_3);
//                break;
//            case 3:
//                iv_star_icon.setImageResource(R.drawable.img_guide_star_4);
//                break;
//            case 4:
//                iv_star_icon.setImageResource(R.drawable.img_guide_star_5);
//                break;
//            case 5:
//                iv_star_icon.setImageResource(R.drawable.img_guide_star_6);
//                break;
//            default:
//                iv_star_icon.setImageResource(R.drawable.img_guide_star_7);
//                break;
//        }
//
        return view;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return 7;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }
}
