package com.app.modulardevice.modulardeviceapp.testApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.model.ModuleDataModel;
import com.app.modulardevice.modulardeviceapp.model.ModuleVariable;
import com.app.modulardevice.modulardeviceapp.utils.Util;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by igbt6 on 24.11.2015.
 */
public class ReadDataFragment extends Fragment {

    private AllVariablesExpandableListAdapter mAllVariablesAdapter;
    private ExpandableListView mAllVariablesList;
    private Context mContext;

    public static ReadDataFragment newInstance() {
        ReadDataFragment readDataFragment = new ReadDataFragment();
        return readDataFragment;
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext =context;
    }
    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_module_test_read_fragment, container, false);
        mAllVariablesList = (ExpandableListView) view.findViewById(R.id.all_variables_list);
        mAllVariablesAdapter = new AllVariablesExpandableListAdapter(mContext);
        mAllVariablesList.setAdapter(mAllVariablesAdapter);
        mAllVariablesList.setOnChildClickListener(VariablesListOnClickListner);
        return view;
    }

    public AllVariablesExpandableListAdapter getDataAdapter(){
        return mAllVariablesAdapter;
    }

    private final ExpandableListView.OnChildClickListener VariablesListOnClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {

                    final String selectedVariable = ((ModuleVariable)mAllVariablesAdapter.getChild(groupPosition, childPosition)).getName();
                    Toast.makeText(getActivity().getBaseContext(), selectedVariable, Toast.LENGTH_LONG)
                            .show();
                    Intent intent = new Intent(v.getContext(),VariableGraph.class);
                    intent.putExtra("MOD_ID", ((ModuleDataModel)mAllVariablesAdapter.getGroup(groupPosition)).getId());
                    intent.putExtra("MOD_NAME", ((ModuleDataModel)mAllVariablesAdapter.getGroup(groupPosition)).getName());
                    intent.putExtra("MOD_VAR_NAME", ((ModuleVariable)mAllVariablesAdapter.getChild(groupPosition, childPosition)).getName());
                    startActivity(intent);
                    return true;
                }
            };

    class AllVariablesExpandableListAdapter extends BaseExpandableListAdapter {

        private Context mContext;
        private List<ModuleDataModel> mModulesCollection;
        private LayoutInflater mInflater;

        public AllVariablesExpandableListAdapter(Context context) {
            mContext = context;
            mModulesCollection = new ArrayList<>();
            mInflater= LayoutInflater.from( mContext );
        }



        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mModulesCollection.get(groupPosition).getModuleVariablesList().get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String variableName = ((ModuleVariable)getChild(groupPosition, childPosition)).getName();
            final String variableUnit = ((ModuleVariable)getChild(groupPosition, childPosition)).getUnit();
            final String variableValue = String.valueOf(String.format("%.1f",((ModuleVariable)getChild(groupPosition, childPosition)).getComputedValue()));
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_module_test_var_child_item, null);
            }

            TextView name= (TextView) convertView.findViewById(R.id.test_module_variable_name);
            TextView value= (TextView) convertView.findViewById(R.id.test_module_variable_value);
            TextView unit= (TextView) convertView.findViewById(R.id.test_module_variable_value_unit);

            name.setText(variableName);
            unit.setText(variableUnit);
            value.setText(variableValue);
            return convertView;
        }
        @Override
        public int getChildrenCount(int groupPosition) {
            return mModulesCollection.get(groupPosition).getModuleVariablesList().size();
        }
        @Override
        public Object getGroup(int groupPosition) {
            return mModulesCollection.get(groupPosition);
        }
        @Override
        public int getGroupCount() {
            return mModulesCollection.size();
        }
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String moduleName = ((ModuleDataModel)getGroup(groupPosition)).getName();
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_module_test_var_group_item, null);
            }
            TextView item = (TextView) convertView.findViewById(R.id.test_modules);
            item.setTypeface(null, Typeface.BOLD);
            item.setText(moduleName);
            return convertView;
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public void updateModules(ArrayList<ModuleDataModel> modules){
            if(modules==null)
                return;
            mModulesCollection=modules;
            this.notifyDataSetChanged();
        }

        public void updateValues(Integer modId, byte[] data){
            if(data.length==0|| data==null)
                return;
            for(ModuleDataModel mod: mModulesCollection){
                if(mod.getId().equals(modId)){
                    for (ModuleVariable mVar : mod.getModuleVariablesList()) {
                        try {
                            mVar.setComputedValue(mVar.computeEquation(Util.byteArrayToObjects(data)));
                        }
                        catch (NumberFormatException e){
                        }
                    }


                }
            }
            this.notifyDataSetChanged();
        }
    }

}
