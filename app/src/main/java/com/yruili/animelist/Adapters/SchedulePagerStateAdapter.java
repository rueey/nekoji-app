package com.yruili.animelist.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.yruili.animelist.Fragments.Schedule.ScheduleAnimeFragment;
import com.yruili.animelist.Utils.ListOptions;

/**
 * Created by rui on 31/08/17.
 */

public class SchedulePagerStateAdapter extends SmartFragmentStatePagerAdapter {
    private static int NUM_ITEMS = 7;
    private int[] dayValues;
    private String[] titles;

    public SchedulePagerStateAdapter(FragmentManager fragmentManager, int[] dayValues, String[] titles) {
        super(fragmentManager);
        this.dayValues = dayValues;
        this.titles = titles;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        int pos = position%NUM_ITEMS;
        return ScheduleAnimeFragment.newInstance(ListOptions.COLUMN_COUNT, dayValues[pos]);
    }
    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        int pos = position%NUM_ITEMS;
        return titles[pos];
    }
}

