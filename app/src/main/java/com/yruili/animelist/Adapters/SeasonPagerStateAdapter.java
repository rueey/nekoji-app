package com.yruili.animelist.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.yruili.animelist.Fragments.Season.AllAnimeFragment;
import com.yruili.animelist.Fragments.Season.MovieAnimeFragment;
import com.yruili.animelist.Fragments.Season.OVAAnimeFragment;
import com.yruili.animelist.Fragments.Season.TVAnimeFragment;
import com.yruili.animelist.Utils.ListOptions;

/**
 * Created by rui on 31/08/17.
 */

public class SeasonPagerStateAdapter extends SmartFragmentStatePagerAdapter {
    private static int NUM_ITEMS = 4;
    private static boolean[] scrolling = new boolean[4];

    public SeasonPagerStateAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return AllAnimeFragment.newInstance(ListOptions.COLUMN_COUNT);
            case 1:
                return TVAnimeFragment.newInstance(ListOptions.COLUMN_COUNT);
            case 2:
                return MovieAnimeFragment.newInstance(ListOptions.COLUMN_COUNT);
            case 3:
                return OVAAnimeFragment.newInstance(ListOptions.COLUMN_COUNT);

            default:
                return null;
        }
    }
    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "ALL";
            case 1:
                return "TV";
            case 2:
                return "MOVIE";
            case 3:
                return "OVA";
        }
        return null;
    }
}

