package com.app.modulardevice.modulardeviceapp.fragment;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.adapter.ModuleFragmentRecItemsAdapter;
import com.app.modulardevice.modulardeviceapp.model.ModuleVariable;

import java.util.ArrayList;
import java.util.List;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGD;

/**
 * Created by igbt6 on 07.11.2015.
 */
public class ModulePageFragment extends Fragment {
    private final static String TAG  = ModulePageFragment.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;
    private static final String ARG_MOD_ID = "ARG_MOD_ID";
    RecyclerView mRecyclerView;
    ModuleFragmentRecItemsAdapter mAdapter;
    private int mModuleId;
    public static ModulePageFragment newInstance(int modId) {
        Bundle args = new Bundle();
        args.putInt(ARG_MOD_ID, modId);
        ModulePageFragment fragment = new ModulePageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void updateModulevariableValue(String modVarName, String modVarValue){

        mAdapter.updateModuleVariableValue(modVarName, modVarValue);
    }

    public ArrayList<ModuleVariable> getAllModuleVariables(){
        return mAdapter.getAllVariables();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModuleId = getArguments().getInt(ARG_MOD_ID,0);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_module_page, container, false);
        final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.fragment_module_page_frame_layout);
        frameLayout.setBackgroundColor(getResources().getColor(R.color.colorGreenDark));

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_module_page_scrollable_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        return view;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ModuleFragmentRecItemsAdapter(getActivity().getBaseContext(), AppEngine.getInstance().getModuleById(mModuleId).getModuleVariablesList());
        mRecyclerView.setAdapter(mAdapter);
    }
    /*
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (ListView) getActivity().findViewById(R.id.fragment_module_page_module_list);
        AppEngine app = AppEngine.getInstance();
        System.out.println("+++++++++++++++++++++++++++ON_ACTIVITY_CREATED" + String.valueOf(mModuleId));
        final ModuleFragmentItemAdapter adapter= new ModuleFragmentItemAdapter(getActivity().getApplicationContext(),
                R.layout.fragment_module_page_item,
                R.id.module_variable_name,
                app.getModuleById(mModuleId).getModuleVariablesList());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println("########################_CLICKED_ON: " + adapter.getItem(position).getName() + " POS: " + position);
                    }
                }
        );
    }
    */
    @Override
    public void onPause() {
        super.onPause();
        System.out.println("----------------------onPause$$$$$$$$$$$ "+ String.valueOf(mModuleId));
    }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(ARG_MOD_ID, mModuleId);
        }



///


    private  class ModuleFragmentItemAdapter extends ArrayAdapter<ModuleVariable>{


        private final ArrayList<ModuleVariable> mModuleVars = new ArrayList<>();
        private final ArrayList<Integer> mModuleVarsIcon = new ArrayList<>();

        public ModuleFragmentItemAdapter(Context context, int resource, int textViewResourceId, List<ModuleVariable> objects) {
            super(context, resource, textViewResourceId, objects);

            if(objects.size()>0&&objects!=null){
                for(ModuleVariable obj:objects){
                    addModuleVariable(obj,R.drawable.m_ic_mod_env_comp_x); //TODO ICONS!
                }
            }
        }

        public void addModuleVariable(ModuleVariable modVar, int modVarIcon){
            if(!mModuleVars.contains(modVar)){
                mModuleVars.add(modVar);
                mModuleVarsIcon.add(modVarIcon);
            }
        }

        public void clear(){
            mModuleVars.clear();
            mModuleVarsIcon.clear();
        }
        @Override
        public int getCount() {
            return mModuleVars.size();
        }
        @Override
        public ModuleVariable getItem(int position) {
            return mModuleVars.get(position);
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if(row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.fragment_module_page_item, parent, false);
            }

            ImageView img = (ImageView)row.findViewById(R.id.module_variable_image);
            TextView text = (TextView)row.findViewById(R.id.module_variable_name);

            img.setImageResource(mModuleVarsIcon.get(position));
            text.setText(mModuleVars.get(position).getName());

            return row;
        }
    }






}
