package com.yruili.animelist.Fragments.Season;

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

import com.yruili.animelist.Adapters.TVAnimeItemRecyclerViewAdapter;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Utils.CountDownCallback;
import com.yruili.animelist.Utils.ListContent;
import com.yruili.animelist.Utils.ListOptions;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnTVAnimeFragmentInteractionListener}
 * interface.
 */
public class TVAnimeFragment extends Fragment {

    private int mColumnCount = 1;
    private OnTVAnimeFragmentInteractionListener mListener;
    private TVAnimeItemRecyclerViewAdapter adapter;
    public CountDownCallback a;
    private boolean loading = false;
    private LinearLayoutManager manager;
    private GridLayoutManager gridManager;
    private boolean scrollBack = false;
    private boolean grid = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TVAnimeFragment() {}

    public static TVAnimeFragment newInstance(int columnCount) {
        TVAnimeFragment fragment = new TVAnimeFragment();
        Bundle args = new Bundle();
        args.putInt(ListOptions.ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ListOptions.ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_anime_fragment_item_list, container, false);
        final RecyclerView list = (RecyclerView)view;
        list.setHasFixedSize(true);
        list.setItemViewCacheSize(20);
        list.setDrawingCacheEnabled(true);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setHasFixedSize(true);
            if (!a.getGrid()) {
                manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
            } else {
                gridManager = new GridLayoutManager(context, mColumnCount);
                recyclerView.setLayoutManager(gridManager);
                grid = true;
            }
            adapter = new TVAnimeItemRecyclerViewAdapter(ListContent.getList().getTV(), mListener, a, grid);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if(dy > 0) {
                        int totalItemCount = adapter.getItemCount();
                        int loadedItems = ListContent.getList().getTV().size();
                        int visibleItemCount;
                        if(a.getGrid()){
                            visibleItemCount = gridManager.getChildCount();
                        } else {
                            visibleItemCount = manager.getChildCount();
                        }
                        int lastVisibleItem;
                        if(a.getGrid()){
                            lastVisibleItem = gridManager.findFirstVisibleItemPosition();
                        } else {
                            lastVisibleItem = manager.findFirstVisibleItemPosition();
                        }
                        if (!loading && totalItemCount < loadedItems
                                && (lastVisibleItem + visibleItemCount) >= totalItemCount) {
                            // End has been reached
                            // Do something
                            loading = true;
                            endlessScrollUpdate();
                        }
                    }
                }
            });
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTVAnimeFragmentInteractionListener) {
            mListener = (OnTVAnimeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTVAnimeFragmentInteractionListener");
        }
        if (context instanceof CountDownCallback) {
            a = (CountDownCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CountDownCallback");
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
        if(scrollBack && getView() != null){
            RecyclerView list = (RecyclerView) getView().findViewById(R.id.list);
            list.getLayoutManager().scrollToPosition(0);
            scrollBack = false;
            updateList();
            Log.w("SCROLLING", "TOP");
            Log.w("SIZE OF DATA", String.valueOf(adapter.getItemCount()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.resetTimers();
        adapter.clearBitmapCache(this.getContext()); //clear bitmap cache to free memory
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.resetTimers();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to TVow an interaction in this fragment to be communicated
     * to the activity and potentiTVy other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTVAnimeFragmentInteractionListener {
        void onTVAnimeFragmentInteraction(Anime item);
    }

    public void setCalled(boolean val){
        if(adapter != null)adapter.setCalled(val);
    }

    /**
     * Endless scrolling update
     * Used to alleviate loads on initial loading
     */

    public void endlessScrollUpdate(){
        if(adapter != null) {
            adapter.endlessScrollReload(ListContent.getList());
            adapter.clearBitmapCache(this.getContext());
            if(getView() != null) {
                RecyclerView list = (RecyclerView) getView().findViewById(R.id.list);
                list.post(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
            Log.w("SIZE OF DATA", String.valueOf(adapter.getItemCount()));
        } else {
            adapter = new TVAnimeItemRecyclerViewAdapter(ListContent.getList().getTV(), mListener, a, grid);
        }
        if(loading)loading = false;
    }

    /**
     * Reloading list update
     * Updates the entire list and is mainly used for refreshing when countdowns reach zero
     */

    public void reloadList(){
        if(adapter != null) {
            adapter.reloadDataSource(ListContent.getList());
            adapter.clearBitmapCache(this.getContext());
            adapter.notifyDataSetChanged();
            Log.w("SIZE OF DATA", String.valueOf(adapter.getItemCount()));
        } else {
            adapter = new TVAnimeItemRecyclerViewAdapter(ListContent.getList().getTV(), mListener, a, grid);
        }
    }

    /**
     * Update List
     * Changing from different seasons
     */

    public void updateList(){
        if(adapter != null) {
            Log.w("TV SIZE BEFORE", String.valueOf(ListContent.getList().getTV().size()));
            adapter.changeDataSource(ListContent.getList());
            adapter.clearBitmapCache(this.getContext());
            adapter.notifyDataSetChanged();
            if(getView() != null){
                RecyclerView list = (RecyclerView) getView().findViewById(R.id.list);
                list.getLayoutManager().scrollToPosition(0);
            } else {
                scrollBack = true;
            }
            Log.w("SIZE OF DATA", String.valueOf(adapter.getItemCount()));
        } else {
            adapter = new TVAnimeItemRecyclerViewAdapter(ListContent.getList().getTV(), mListener, a, grid);
        }
    }
}
