package com.yruili.animelist.Fragments.Search;

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

import com.yruili.animelist.Adapters.SearchAnimeItemRecyclerViewAdapter;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Utils.ListContent;
import com.yruili.animelist.Utils.ListOptions;
import com.yruili.animelist.Utils.SearchCallback;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSearchAnimeFragmentInteractionListener}
 * interface.
 */
public class SearchAnimeFragment extends Fragment {

    private int mColumnCount = 1;
    private OnSearchAnimeFragmentInteractionListener mListener;
    private SearchAnimeItemRecyclerViewAdapter adapter;
    private LinearLayoutManager manager;
    private GridLayoutManager gridManager;
    private boolean grid = false;
    private SearchCallback callback;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchAnimeFragment() {}

    public static SearchAnimeFragment newInstance(int columnCount) {
        SearchAnimeFragment fragment = new SearchAnimeFragment();
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
        list.setItemViewCacheSize(40);
        list.setDrawingCacheEnabled(true);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (!callback.getGrid()) {
                manager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(manager);
            } else {
                gridManager = new GridLayoutManager(context, mColumnCount);
                recyclerView.setLayoutManager(gridManager);
                grid = true;
            }
            adapter = new SearchAnimeItemRecyclerViewAdapter(ListContent.getList().getAll(), mListener, grid);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if(dy > 0) {
                        int totalItemCount = adapter.getItemCount();
                        int visibleItemCount;
                        if(callback.getGrid()){
                            visibleItemCount = gridManager.getChildCount();
                        } else {
                            visibleItemCount = manager.getChildCount();
                        }
                        int lastVisibleItem;
                        if(callback.getGrid()){
                            lastVisibleItem = gridManager.findFirstVisibleItemPosition();
                        } else {
                            lastVisibleItem = manager.findFirstVisibleItemPosition();
                        }
                        if ((lastVisibleItem + visibleItemCount) >= totalItemCount) {
                            // End has been reached
                            // Do something
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
        if (context instanceof OnSearchAnimeFragmentInteractionListener) {
            mListener = (OnSearchAnimeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSearchAnimeFragmentInteractionListener");
        }
        if (context instanceof SearchCallback) {
            callback = (SearchCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CountDownCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    }

    @Override
    public void onStop() {
        super.onStop();
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
    public interface OnSearchAnimeFragmentInteractionListener {
        void onSearchAnimeFragmentInteraction(Anime item);
    }

    /**
     * Endless scrolling update
     * Used to query for next batch / page
     */

    public void endlessScrollUpdate(){
        if(adapter != null && callback.getPage() < 10) {
            adapter.clearBitmapCache(this.getContext());
            callback.loadNextBatch(callback.getPage()+1);
        }
    }

    /**
     * Reloading list update
     * Updates the entire list and is mainly used for sorting and endless scroll updates
     */

    public void reloadList(){
        if(adapter != null) {
            adapter.changeDataSource(ListContent.getList());
            adapter.clearBitmapCache(this.getContext());
            adapter.notifyDataSetChanged();
            Log.w("SIZE OF DATA", String.valueOf(adapter.getItemCount()));
        } else {
            adapter = new SearchAnimeItemRecyclerViewAdapter(ListContent.getList().getAll(), mListener, grid);
        }
    }

    /**
     * Update List
     * Called when searching queries
     */

    public void updateList(){
        if(adapter != null) {
            Log.w("ALL SIZE BEFORE", String.valueOf(ListContent.getList().getAll().size()));
            adapter.changeDataSource(ListContent.getList());
            adapter.clearBitmapCache(this.getContext());
            adapter.notifyDataSetChanged();
            if(getView() != null){
                RecyclerView list = (RecyclerView) getView().findViewById(R.id.list);
                list.getLayoutManager().scrollToPosition(0);
            }
            Log.w("SIZE OF DATA", String.valueOf(adapter.getItemCount()));
        } else {
            Log.w("REACHED ONE", "HERE WE GO");
            adapter = new SearchAnimeItemRecyclerViewAdapter(ListContent.getList().getAll(), mListener, grid);
        }
    }
}
