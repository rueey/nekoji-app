package com.yruili.animelist.Adapters;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
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
import com.yruili.animelist.Fragments.Season.MovieAnimeFragment.OnMovieAnimeFragmentInteractionListener;
import com.yruili.animelist.GlideApp;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Model.AnimeList;
import com.yruili.animelist.Utils.CountDownCallback;
import com.yruili.animelist.Utils.ListOptions;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display an Anime and makes a call to the
 * specified {@link OnMovieAnimeFragmentInteractionListener}.
 */
public class MovieAnimeItemRecyclerViewAdapter extends RecyclerView.Adapter<MovieAnimeItemRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Anime> mValues = new ArrayList<>();
    private final OnMovieAnimeFragmentInteractionListener mListener;
    private ArrayList<CountDownTimer> timers = new ArrayList<>();
    private boolean called = false;
    private CountDownCallback act;
    private boolean grid;

    public MovieAnimeItemRecyclerViewAdapter(ArrayList<Anime> items, OnMovieAnimeFragmentInteractionListener listener, CountDownCallback act, boolean grid) {
        mValues.addAll(items);
        mListener = listener;
        this.act = act;
        this.grid = grid;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(!grid){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.all_anime_fragment_item, parent, false);
            return new ViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_anime_fragment_grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(holder.mItem.getTitle_romaji());
        holder.mStatusView.setText(holder.mItem.getAiring_status());
        holder.mTypeView.setText(holder.mItem.getType());

        if(holder.mItem.getAiring() != null && holder.mItem.getAiring().getTime() != null) {
            holder.countDown.setVisibility(View.VISIBLE);
            if (holder.timer != null) {
                holder.timer.cancel();
                timers.remove(holder.timer);
                holder.timer = null;
            }
            holder.createTimer(holder.mItem);
        } else {
            holder.countDown.setVisibility(View.GONE);
        }

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
                    mListener.onMovieAnimeFragmentInteraction(holder.mItem);
                }
            }
        });
    }
    private synchronized void update(ArrayList<Anime> temp){
        mValues.clear();
        mValues.addAll(temp);
    }
    public void changeDataSource(AnimeList newUserList) {
        ArrayList<Anime> temp = new ArrayList<>();
        for(int i = 0; i < Math.min(newUserList.getMovie().size(), 20); i++)
            temp.add(newUserList.getMovie().get(i));
        update(temp);
        Log.w("MOVIE SIZE CHANGE", String.valueOf(newUserList.getMovie().size()));
        resetTimers();
    }
    public void reloadDataSource(AnimeList newUserList){
        update(newUserList.getMovie());
        resetTimers();
    }
    public void endlessScrollReload(AnimeList newUserList) {
        ArrayList<Anime> temp = new ArrayList<>();
        temp.addAll(mValues);
        int updatedSize = temp.size() + 20;
        Log.w("MOVIE SIZE", String.valueOf(newUserList.getMovie().size()));
        for (int i = Math.min(newUserList.getMovie().size(), temp.size()); i < Math.min(newUserList.getMovie().size(), updatedSize); i++){
            temp.add(newUserList.getMovie().get(i));
        }
        Log.w("LOADING IN", "MORE DATA");
        update(temp);
        resetTimers();
    }
    public void resetTimers(){
        for(CountDownTimer t : timers){
            t.cancel();
            t = null;
        }
        timers = null;
        timers = new ArrayList<>();
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

    public void setCalled(boolean called){
        this.called = called;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mTypeView;
        public final TextView mStatusView;
        public final TextView mEpisodeView;
        public final ImageView mThumbNail;
        public final TextView countDown;
        public CountDownTimer timer;
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
            if (grid) {
                CardView card = (CardView) view.findViewById(R.id.grid_card);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
                mThumbNail.requestLayout();
                mThumbNail.getLayoutParams().width = (int) (view.getContext().getResources()
                        .getDisplayMetrics().widthPixels * (1.0 / (double) ListOptions.COLUMN_COUNT) - (lp.leftMargin + lp.rightMargin));
                mThumbNail.getLayoutParams().height = (int) (mThumbNail.getLayoutParams().width * 1.44);
            }
        }

        public void createTimer(Anime a) {
            //Get the correct date difference
            final DateTime time = new DateTime(a.getAiring().getTime());
            final int episodeNum = a.getAiring().getNext_episode();
            final WeakReference<TextView> count = new WeakReference<>(countDown);
            timer = new CountDownTimer(time.getMillis() - System.currentTimeMillis(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long days = (millisUntilFinished / (1000 * 60 * 60 * 24));
                    millisUntilFinished %= (1000 * 60 * 60 * 24);
                    long hours = (millisUntilFinished / (1000 * 60 * 60));
                    millisUntilFinished %= (1000 * 60 * 60);
                    long mins = (millisUntilFinished / (1000 * 60));
                    millisUntilFinished %= (1000 * 60);
                    long secs = (millisUntilFinished / 1000);
                    String result = "Ep " + episodeNum + ": ";
                    if (days != 0) result += days + "d ";
                    if (hours != 0) result += hours + "h ";
                    if (mins != 0) result += mins + "m ";
                    result += secs + "s";
                    count.get().setText(result);
                }

                @Override
                public void onFinish() {
                    if (!called) {
                        called = true;
                        if (act != null) act.call();
                    }
                }
            }.start();
            timers.add(timer);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
