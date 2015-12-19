package com.app.modulardevice.modulardeviceapp.testApp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * Created by igbt6 on 24.11.2015.
 */
public class ModActivityTestPageAdapter extends FragmentPagerAdapter {

    private static final String[] Titles= new String[]{"WRITING","READING"};
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private Fragment mWriteFragment,mReadFragment;
    ModActivityTestPageAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
    }


    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                mWriteFragment=WriteDataFragment.newInstance();
                return mWriteFragment;
            case 1:
                mReadFragment=ReadDataFragment.newInstance();
                return mReadFragment;

            default:   return null;
        }
    }

    // Register the fragment when the item is instantiated
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    // Unregister when the item is inactive
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    // Returns the fragment for the position (if instantiated)
    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public Fragment getCurrentFragment(int pos){
        if(pos==0)
            return mWriteFragment;
        else
            return mReadFragment;
    }
    @Override
    public int getCount() {
        return Titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position] ;
    }
}
