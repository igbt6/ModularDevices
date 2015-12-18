package com.app.modulardevice.modulardeviceapp.adapter;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.app.modulardevice.modulardeviceapp.fragment.ModulePageFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igbt6 on 07.11.2015.
 */
public class MFragPagerAdapter extends ModuleFragmentStatePagerAdapter{

    private final List<ModulePageFragment> mModuleFragmentList= new ArrayList<>();
    private final List<Integer> mModuleFragmentIdsList =new ArrayList<>();
    private final List<String> mModuleFragmentNameList =new ArrayList<>();


    public MFragPagerAdapter(FragmentManager fm) {
        super(fm);
        clearModuleFragments();
    }

    public void addModuleFragment(ModulePageFragment modFragment,Integer modId, String modLabel){
        mModuleFragmentList.add(modFragment);
        mModuleFragmentIdsList.add(modId);
        mModuleFragmentNameList.add(modLabel);
    }


    public void clearModuleFragments(){
        mModuleFragmentList.clear();
        mModuleFragmentIdsList.clear();
        mModuleFragmentNameList.clear();
    }

    public Integer getModuleId(int position) {
        return mModuleFragmentIdsList.get(position);
    }

    @Override
    public ModulePageFragment getItem(int position) {
        return mModuleFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mModuleFragmentList.size();
    }

    @Override
    public int getItemPosition(Object object){
        return FragmentStatePagerAdapter.POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mModuleFragmentNameList.get(position);
    }
}
