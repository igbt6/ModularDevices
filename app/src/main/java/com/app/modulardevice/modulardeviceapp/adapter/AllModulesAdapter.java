package com.app.modulardevice.modulardeviceapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.modulardevice.modulardeviceapp.R;
import com.squareup.picasso.Picasso;
import java.util.Vector;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGI;
import static com.app.modulardevice.modulardeviceapp.utils.Util.getModuleIconPathByModId;

/**
 * Created by igbt6 on 19.10.2015.
 */
public class AllModulesAdapter extends BaseAdapter {
    private final static String TAG  = AllModulesAdapter.class.getSimpleName();
    private final static boolean LOG_ENABLE = true;

    private Context mContext;
    private LayoutInflater mInflater;
    private Vector<Integer> mModuleIds;
    private Vector<String> mModuleLabels;
    public  AllModulesAdapter (Context c) {
        mModuleIds= new Vector<>();
        mModuleLabels= new Vector<>();
        mContext = c;
        mInflater= LayoutInflater.from(mContext);
    }


    public void addModule(int modId, String modLabel){
        if(!mModuleIds.contains(modId)){
            mModuleIds.add(modId);
            mModuleLabels.add(modLabel);
        }
    }

    public void clear(){
        mModuleLabels.clear();
        mModuleIds.clear();
    }
    @Override
    public int getCount() {
        return mModuleIds.size();
    }
    @Override
    public Object getItem(int position) {
        return mModuleIds.get(position);
    }
    @Override
    public long getItemId(int position) {
        return mModuleIds.get(position);
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder viewHolder;
        if (view == null) {
            view = mInflater.inflate(R.layout.all_modules_item, null);


            viewHolder= new ViewHolder();
            viewHolder.moduleLabel= (TextView)view.findViewById(R.id.all_modules_grid_item_label);

            viewHolder.moduleImage = (ImageView) view.findViewById(R.id.all_modules_grid_item_image);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String modLabel = null;
        if(getCount()>0){
            modLabel=mModuleLabels.get(position);
        }
        if(modLabel!=null&&modLabel.length()>0){
            viewHolder.moduleLabel.setText(modLabel);
            Picasso.with(mContext)
                    .load(getModuleIconPathByModId(mContext, mModuleIds.get(position))) //TODO, i'd prefer getting from the internet database?
                    //.resize(100, 200)
                    .rotate(0)
                    .into(viewHolder.moduleImage);
        }
        else{
            viewHolder.moduleLabel.setText(R.string.unknown_device);
            Picasso.with(mContext)
                    .load(getModuleIconPathByModId(mContext, 0)) // get default
                    .rotate(0)
                    .into(viewHolder.moduleImage);
        }
        return view;
    }


    static class ViewHolder {
        ImageView moduleImage;
        TextView moduleLabel;
    }

}

