package com.yruili.animelist.Adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yruili.animelist.Fragments.Search.SearchAnimeFragment.OnSearchAnimeFragmentInteractionListener;
import com.yruili.animelist.GlideApp;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Model.AnimeList;
import com.yruili.animelist.Utils.ListOptions;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display an Anime and makes a call to the
 * specified {@link OnSearchAnimeFragmentInteractionListener}.
 */
public class SearchAnimeItemRecyclerViewAdapter extends RecyclerView.Adapter<SearchAnimeItemRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Anime> mValues = new ArrayList<>();
    private final OnSearchAnimeFragmentInteractionListener mListener;
    private boolean grid;

    public SearchAnimeItemRecyclerViewAdapter(ArrayList<Anime> items, OnSearchAnimeFragmentInteractionListener listener, boolean grid) {
        mValues.addAll(items);
        mListener = listener;
        this.grid = grid;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(!grid){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.all_anime_fragment_item, parent, false);
            ObjectAnimator.ofFloat(view,"alpha",0,1).setDuration(200).start();
            return new ViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_anime_fragment_grid_item, parent, false);
        ObjectAnimator.ofFloat(view,"alpha",0,1).setDuration(200).start();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(holder.mItem.getTitle_romaji());
        holder.mStatusView.setText(holder.mItem.getAiring_status());
        holder.mTypeView.setText(holder.mItem.getType());

        if(holder.mItem.getTotal_episodes() > 1)holder.mEpisodeView.setText(holder.mItem.getTotal_episodes() + " eps");
        else if(holder.mItem.getTotal_episodes() == 1)holder.mEpisodeView.setText(holder.mItem.getTotal_episodes() + " ep");
        else holder.mEpisodeView.setText("? eps");
        String url = holder.mItem.getImage_url_lge();
        if(url == null)url = holder.mItem.getImage_url_med();
        if(url == null)url = holder.mItem.getImage_url_sml();
        GlideApp.with(holder.mThumbNail.getContext())
                .load(url)
                .centerCrop()
                .placeholder(R.color.cardview_dark_background)
                .into(holder.mThumbNail);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSearchAnimeFragmentInteraction(holder.mItem);
                }
            }
        });
    }
    private synchronized void update(ArrayList<Anime> temp){
        mValues.clear();
        mValues.addAll(temp);
    }
    public void changeDataSource(AnimeList newUserList) {
        update(newUserList.getAll());
    }
    public void clearBitmapCache(Context c){
        Glide.get(c).clearMemory();
        Log.w("Clearing memory", "yay");
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mTypeView;
        public final TextView mStatusView;
        public final TextView mEpisodeView;
        public final ImageView mThumbNail;
        public final TextView countDown;
        public Anime mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.anime_name_list);
            mTypeView = (TextView) view.findViewById(R.id.anime_type_list);
            mStatusView = (TextView) view.findViewById(R.id.anime_airing_list);
            mThumbNail = (ImageView) view.findViewById(R.id.anime_list_image);
            mEpisodeView = (TextView) view.findViewById(R.id.anime_episodes_list);
            countDown = (TextView) view.findViewById(R.id.countdown_list);
            countDown.setVisibility(View.GONE);
            if(grid) {
                CardView card = (CardView) view.findViewById(R.id.grid_card);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
                mThumbNail.requestLayout();
                mThumbNail.getLayoutParams().width = (int) (view.getContext().getResources()
                        .getDisplayMetrics().widthPixels * (1.0/(double)ListOptions.COLUMN_COUNT)-(lp.leftMargin + lp.rightMargin));
                mThumbNail.getLayoutParams().height = (int) (mThumbNail.getLayoutParams().width * 1.44);
            }
        }
        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
