package com.yruili.animelist.Fragments.Schedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yruili.animelist.Adapters.ScheduleAnimeItemRecyclerViewAdapter;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Utils.AnimeComparator;
import com.yruili.animelist.Utils.CountDownCallback;
import com.yruili.animelist.Utils.ListContent;
import com.yruili.animelist.Utils.ListOptions;
import com.yruili.animelist.Utils.ScheduleUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnScheduleAnimeFragmentInteractionListener}
 * interface.
 */
public class ScheduleAnimeFragment extends Fragment {

    private int mColumnCount = 1;
    private OnScheduleAnimeFragmentInteractionListener mListener;
    private ScheduleAnimeItemRecyclerViewAdapter adapter;
    public CountDownCallback a;
    private LinearLayoutManager manager;
    private GridLayoutManager gridManager;
    private boolean grid = false;
    private int dayOfTheWeek;
    private Calendar date;
    private ArrayList<Anime> result;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScheduleAnimeFragment() {}

    public static ScheduleAnimeFragment newInstance(int columnCount, int dayOfTheWeek) {
        ScheduleAnimeFragment fragment = new ScheduleAnimeFragment();
        Bundle args = new Bundle();
        args.putInt(ListOptions.ARG_COLUMN_COUNT, columnCount);
        args.putInt("DAY OF THE WEEK", dayOfTheWeek);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ListOptions.ARG_COLUMN_COUNT);
            dayOfTheWeek = getArguments().getInt("DAY OF THE WEEK");
            date = ScheduleUtil.nextDayOfWeek(dayOfTheWeek);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_anime_fragment_item_list, container, false);
        final RecyclerView list = (RecyclerView)view;
        list.setHasFixedSize(true);
        list.setItemViewCacheSize(40);
        list.setDrawingCacheEnabled(true);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (!a.getGrid()) {
                manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
            } else {
                gridManager = new GridLayoutManager(context, mColumnCount);
                recyclerView.setLayoutManager(gridManager);
                grid = true;
            }
            updateList();
            adapter = new ScheduleAnimeItemRecyclerViewAdapter(result, mListener, a, grid);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnScheduleAnimeFragmentInteractionListener) {
            mListener = (OnScheduleAnimeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnScheduleAnimeFragmentInteractionListener");
        }
        if (context instanceof CountDownCallback) {
            a = (CountDownCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CountDownCallback");
        }
    }

    private void updateList(){
        result = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        for(Anime a : ListContent.getList().getAll()){
            if(a.getAiring() != null && date != null) {
                DateTime time = new DateTime(a.getAiring().getTime());
                if ((int) date.get(Calendar.MONTH) + 1 == time.getMonthOfYear() && (int) date.get(Calendar.DATE) == time.getDayOfMonth() && (int) date.get(Calendar.YEAR) == time.getYear()) {
                    result.add(a);
                }
                if(c.get(Calendar.DAY_OF_WEEK) == dayOfTheWeek){
                    Calendar z = ScheduleUtil.nextDayOfWeekOverride(dayOfTheWeek);
                    if ((int) z.get(Calendar.MONTH) + 1 == time.getMonthOfYear() && (int) z.get(Calendar.DATE) == time.getDayOfMonth() && (int) z.get(Calendar.YEAR) == time.getYear()) {
                        result.add(a);
                    }
                }
                //Log.w("UPDATING", a.getTitle_romaji() + ": " + String.valueOf(date.get(Calendar.MONTH)) + " " + String.valueOf(time.getMonthOfYear()) + " / " + String.valueOf(date.get(Calendar.DATE)) + " " + String.valueOf(time.getDayOfMonth()) + " / " + String.valueOf(date.get(Calendar.YEAR)) + " " + time.getYear());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        a = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.clearBitmapCache(this.getContext()); //clear bitmap cache to free memory
        adapter.resetTimers();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.resetTimers();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnScheduleAnimeFragmentInteractionListener {
        void onScheduleAnimeFragmentInteraction(Anime item);
    }

    public void setCalled(boolean val){
        if(adapter != null)adapter.setCalled(val);
    }

    /**
     * Reloading list update
     * Updates the entire list and is mainly used for refreshing when countdowns reach zero
     */

    public void reloadList(){
        updateList();
        Log.w("RESULT SIZE", dayOfTheWeek + " " + String.valueOf(result.size()));
        if(adapter != null) {
            adapter.changeDataSource(result);
            adapter.clearBitmapCache(this.getContext());
            adapter.notifyDataSetChanged();
            Log.w("SIZE OF DATA", String.valueOf(adapter.getItemCount()));
        } else {
            adapter = new ScheduleAnimeItemRecyclerViewAdapter(result, mListener, a, grid);
        }
    }
}
