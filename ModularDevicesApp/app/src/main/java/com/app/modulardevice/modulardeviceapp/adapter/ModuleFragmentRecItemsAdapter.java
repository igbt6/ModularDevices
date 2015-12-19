package com.app.modulardevice.modulardeviceapp.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.model.ModuleVariable;
import com.app.modulardevice.modulardeviceapp.utils.Util;

import java.util.ArrayList;

import static com.app.modulardevice.modulardeviceapp.utils.Util.createSpaces;

/**
 * Created by igbt6 on 09.11.2015.
 */
public class ModuleFragmentRecItemsAdapter extends RecyclerView.Adapter<ModuleFragmentRecItemsAdapter.ModuleFragmentViewHolder> {


    private ArrayList<ModuleVariable> mModuleVars = new ArrayList<>();
    private ArrayList<String> mModuleVarsValue;
    Context mContext;
    OnItemClickListener clickListener;

    public ModuleFragmentRecItemsAdapter(Context context,ArrayList<ModuleVariable> modVars) {
        mContext = context;
        mModuleVars = modVars;
        mModuleVarsValue=new ArrayList<>(mModuleVars.size());
        for(int i=0;i<mModuleVars.size();i++){
            mModuleVarsValue.add("---");
        }
    }


    public void updateModuleVariableValue(String modVarName, String modVarValue){
        for(int i=0;i<mModuleVars.size();i++){
            if(mModuleVars.get(i).getName().equals(modVarName)){
                mModuleVarsValue.set(i, modVarValue);
                notifyDataSetChanged();
                break;
            }

        }
    }
    public ArrayList<ModuleVariable> getAllVariables(){
        return mModuleVars;
    }
    @Override
    public ModuleFragmentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_module_recycler_list_item, viewGroup, false);
        ModuleFragmentViewHolder viewHolder = new ModuleFragmentViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ModuleFragmentViewHolder modFragViewHolder, int i) {
        modFragViewHolder.varImg.setImageResource(Util.getModuleIconPathByName(mContext, mModuleVars.get(i).getIcon()));
        modFragViewHolder.varName.setText(mModuleVars.get(i).getName());
        modFragViewHolder.varValue.setText(mModuleVarsValue.get(i));
        String unit =mModuleVars.get(i).getUnit();
        if(unit.length()==0){
            unit="  ";
        }
        else {
            unit = "[" + unit + "]";
        }

        modFragViewHolder.varUnit.setText(createSpaces(12-unit.length())+unit);
    }

    @Override
    public int getItemCount() {
        return (mModuleVars == null ? 0 : mModuleVars.size());
    }


    class ModuleFragmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItemLayout;
        ImageView varImg;
        TextView varName;
        TextView varValue;
        TextView varUnit;
        public ModuleFragmentViewHolder(View itemView) {
            super(itemView);
            cardItemLayout = (CardView) itemView.findViewById(R.id.module_var_cardlist_item);
            varImg = (ImageView)itemView.findViewById(R.id.module_variable_image);
            varName = (TextView)itemView.findViewById(R.id.module_variable_name);
            varValue= (TextView)itemView.findViewById(R.id.module_variable_value);
            varUnit = (TextView)itemView.findViewById(R.id.module_variable_value_unit);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

}



