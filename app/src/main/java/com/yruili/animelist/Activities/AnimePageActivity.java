package com.yruili.animelist.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yruili.animelist.GlideApp;
import com.yruili.animelist.Loaders.AnimePageLoader;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Utils.SeasonUtil;
import com.yruili.animelist.Model.Studio;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AnimePageActivity extends AppCompatActivity {

    ArrayList<CountDownTimer> timers = new ArrayList<>();
    int episodeNum;
    int dataId;
    long millis;
    boolean noInternet;
    boolean running;
    String animeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.w("STARTING", "INIT");

        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");
        animeId = id;
        Log.w("INTENT", String.valueOf(intent.getExtras().size()));
        loadAnimePage(id);

        TextView retry = (TextView) findViewById(R.id.page_retry_text);
        retry.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!running){
                    running = true;
                    findViewById(R.id.page_retry_text).setVisibility(View.GONE);
                    findViewById(R.id.page_connect_text).setVisibility(View.GONE);
                    findViewById(R.id.anime_page_loading).setVisibility(View.VISIBLE);
                    loadAnimePage(animeId);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent() != null && getIntent().hasExtra("EPISODE") && getIntent().hasExtra("TIME") && getIntent().hasExtra("ID RESTART")){
            Log.w("RESTARTING", "TIMER");
            Log.w("EPISODE", String.valueOf(episodeNum));
            episodeNum = getIntent().getIntExtra("EPISODE", 0);
            dataId = getIntent().getIntExtra("ID RESTART", 0);
            millis = getIntent().getLongExtra("TIME", 0);
            getIntent().removeExtra("EPISODE");
            getIntent().removeExtra("ID RESTART");
            getIntent().removeExtra("TIME");
            findViewById(R.id.anime_countdown_page).setVisibility(View.VISIBLE);
            final TextView countdown = (TextView) findViewById(R.id.anime_countdown_page);
            CountDownTimer timer = new CountDownTimer(millis - System.currentTimeMillis(), 500) {
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
                    countdown.setText(result);
                }

                @Override
                public void onFinish() {
                    loadAnimePage(String.valueOf(dataId));
                }
            }.start();
            if(timers == null)timers = new ArrayList<>();
            timers.add(timer);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(timers != null && timers.size() > 0) {
            for(CountDownTimer timer : timers) {
                timer.cancel();
                timer = null;
            }
            Log.w("STOPPING", "REMOVING TIMER");
            getIntent().putExtra("TIME", millis);
            getIntent().putExtra("ID RESTART", dataId);
            getIntent().putExtra("EPISODE", episodeNum);
        }
        if(getSupportLoaderManager().getLoader(2) != null)getSupportLoaderManager().destroyLoader(2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadAnimePage(String id){
        if(getSupportLoaderManager().getLoader(2) == null)getSupportLoaderManager().initLoader(2, null, new PageLoad(this, id));
        else getSupportLoaderManager().restartLoader(2, null, new PageLoad(this, id));
    }
    private class PageLoad implements LoaderManager.LoaderCallbacks<Anime> {
        Context context;
        String id;
        public PageLoad(Context c, String id){
            context = c;
            this.id = id;
        }
        @Override
        public Loader<Anime> onCreateLoader(int id, Bundle args) {
            return new AnimePageLoader(context, this.id);
        }
        @Override
        public void onLoadFinished(Loader<Anime> loader, Anime data) {
            final View progress = findViewById(R.id.anime_page_loading);
            if(data == null){
                noInternet = true;
                //Animate progress bar out and display the data
                progress.setVisibility(View.GONE);
                findViewById(R.id.page).setVisibility(View.GONE);
                findViewById(R.id.page_connect_text).setVisibility(View.VISIBLE);
                findViewById(R.id.page_retry_text).setVisibility(View.VISIBLE);
            } else {
                //TODO: Relationship with other anime in data for prequels/sequels/ova etc.
                //TODO: Display stats
                if(noInternet){
                    noInternet = false;
                    findViewById(R.id.page).setVisibility(View.VISIBLE);
                    findViewById(R.id.page_connect_text).setVisibility(View.GONE);
                    findViewById(R.id.page_retry_text).setVisibility(View.GONE);
                }
                getSupportActionBar().setTitle(data.getTitle_romaji());

                //Set the title of the appbar
                TextView title = (TextView) findViewById(R.id.anime_name_page);
                title.setText(data.getTitle_romaji()); //TODO: Setting to choose what title to use (romaji, english or japanese)

                //Anime image thumbnail
                ImageView image = (ImageView) findViewById(R.id.anime_page_image);
                String url = data.getImage_url_lge();
                if (url == null) url = data.getImage_url_med();
                if (url == null) url = data.getImage_url_sml();
                GlideApp.with(image.getContext())
                        .load(url)
                        .centerCrop()
                        .placeholder(R.color.cardview_dark_background)
                        .into(image);

                //Synonyms
                TextView synonyms = (TextView) findViewById(R.id.anime_synonyms_page);
                ArrayList<String> filtered = new ArrayList<>();
                for (String s : data.getSynonyms()) {
                    if (s != null && s.length() != 0 && !s.equals(" ")) filtered.add(s);
                }
                if (filtered.size() > 0) {
                    String result = "";
                    int c = 0;
                    for (String s : filtered) {
                        if (c < filtered.size() - 1) result += (s + ", ");
                        else result += s;
                        c++;
                    }
                    synonyms.setText(result);
                } else {
                    synonyms.setVisibility(View.GONE);
                }

                //Updated
                TextView updatedAt = (TextView) findViewById(R.id.anime_updated_at_page);
                if (data.getUpdated_at() != 0) {
                   String result = new SimpleDateFormat("M/d/y, h:mm a").format(new Date(data.getUpdated_at() * 1000L));
                    updatedAt.setText("Last updated: " + result);
                } else {
                    updatedAt.setText(data.getAiring_status());
                }

                //Type
                TextView type = (TextView) findViewById(R.id.anime_type_page);
                if(data.getType() != null)type.setText(data.getType());
                else type.setText("?");

                //Episodes
                TextView episodes = (TextView) findViewById(R.id.anime_episodes_page);
                if (data.getTotal_episodes() != 0)
                    episodes.setText(String.valueOf(data.getTotal_episodes()));
                else episodes.setText("?");

                //Duration
                TextView duration = (TextView) findViewById(R.id.anime_duration_page);
                if (data.getDuration() != 0)
                    duration.setText(String.valueOf(data.getDuration()) + " mins");
                else duration.setText("?");

                //Airing status
                TextView status = (TextView) findViewById(R.id.anime_status_page);
                if(data.getAiring_status() != null) {
                    String firstLetter = data.getAiring_status().substring(0, 1).toUpperCase();
                    status.setText(firstLetter + data.getAiring_status().substring(1));
                } else {
                    status.setText("Unavailable");
                }

                //Aired from-to
                TextView aired = (TextView) findViewById(R.id.anime_aired_to_page);
                String result = "";
                if (String.valueOf(data.getStart_date_fuzzy()).length() == 8 && !String.valueOf(data.getStart_date_fuzzy()).substring(6).equals("00")) {
                    String value = String.valueOf(data.getStart_date_fuzzy());
                    String year = value.substring(0, 4);
                    String month = SeasonUtil.getMonth(Integer.valueOf(value.substring(4, 6)));
                    String day = value.substring(6);
                    if (day.charAt(0) == '0') day = day.substring(1);
                    if (day.equals("00")) result += "? to ";
                    else result += month + " " + day + ", " + year + " to";
                } else {
                    result += "? to";
                }
                if (String.valueOf(data.getEnd_date_fuzzy()).length() == 8 && !String.valueOf(data.getEnd_date_fuzzy()).substring(6).equals("00")) {
                    String value = String.valueOf(data.getEnd_date_fuzzy());
                    String year = value.substring(0, 4);
                    String month = SeasonUtil.getMonth(Integer.valueOf(value.substring(4, 6)));
                    String day = value.substring(6);
                    if (day.charAt(0) == '0') day = day.substring(1);
                    if (day.equals("00")) result += " ?";
                    else result += "\n" + month + " " + day + ", " + year;
                } else {
                    result += " ?";
                }
                aired.setText(result);

                //Studios
                TextView studios = (TextView) findViewById(R.id.anime_studios_page);
                if(data.getStudio() != null && data.getStudio().size() > 0){
                    String f = "";
                    int c = 0;
                    for(Studio s : data.getStudio()) {
                        String r = s.getStudio_name();
                        r = r.replace(" ", "\u00A0");
                        if (data.getStudio().size() > 1 && s.getMain_studio() == 1 && c != data.getStudio().size() - 1)
                            r += " (Main), ";
                        else if(data.getStudio().size() > 1 && s.getMain_studio() == 1 && c == data.getStudio().size() - 1)
                            r += " (Main)";
                        else if(data.getStudio().size() > 1 && c != data.getStudio().size()-1)
                            r += ", ";
                        f += r;
                        c++;
                    }
                    studios.setText(f);
                } else {
                    studios.setText("No studios added");
                }

                //Genres
                TextView genres = (TextView) findViewById(R.id.anime_genres_page);
                ArrayList<String> filteredGenres = new ArrayList<>();
                for (String s : data.getGenres()) {
                    if (s != null && s.length() != 0 && !s.equals(" ")) filteredGenres.add(s);
                }
                if (filteredGenres.size() > 0) {
                    String r = "";
                    int c = 0;
                    for (String s : filteredGenres) {
                        String t = s;
                        t = t.replace(" ", "\u00A0");
                        if (c != filteredGenres.size() - 1)t += ", ";
                        c++;
                        r += t;
                    }
                    genres.setText(r);
                } else {
                    genres.setText("No genres listed");
                }

                //TODO: FINISH ADDING ALL THE ANIME ATTRIBUTES (External links, youtube, tags maybe)

                //Description
                TextView desc = (TextView) findViewById(R.id.anime_description_page);
                if(data.getDescription() != null) {
                    String descEdited = data.getDescription();
                    descEdited = descEdited.replace("<br>", "\n");
                    descEdited = descEdited.replaceAll("<.*?>", "");
                    int count = 0;
                    String r = "";
                    for (int i = 0; i < descEdited.length(); i++) {
                        if (descEdited.substring(i, i + 1).equals("\n") && count >= 3) continue;
                        if (descEdited.substring(i, i + 1).equals("\n") && count < 3) count++;
                        else count = 0;
                        r += descEdited.substring(i, i + 1);
                    }
                    descEdited = r;
                    desc.setText(descEdited);
                } else {
                    desc.setText("No description added");
                }

                //Source
                TextView source = (TextView) findViewById(R.id.anime_source_page);
                if(data.getSource() != null)source.setText(data.getSource());
                else source.setText("Other");

                //Premiered
                TextView premiered = (TextView) findViewById(R.id.anime_premiered_page);
                if(String.valueOf(data.getSeason()).length() >= 3){
                    premiered.setText(SeasonUtil.getSeasonText(data.getSeason() % 10) + " 20"+String.valueOf(data.getSeason()).substring(0, 2));
                } else {
                    String year = String.valueOf(data.getStart_date_fuzzy()).length() >= 4 ? String.valueOf(data.getStart_date_fuzzy()).substring(0, 4) : "?";
                    String season = String.valueOf(data.getStart_date_fuzzy()).length() >= 6 ? SeasonUtil.checkMonth(Integer.parseInt(String.valueOf(data.getStart_date_fuzzy()).substring(4, 6))-1) : "?";
                    premiered.setText(season + " " + year);
                }

                if(data.getAiring() != null) {
                    findViewById(R.id.anime_countdown_page).setVisibility(View.VISIBLE);
                    final TextView countdown = (TextView) findViewById(R.id.anime_countdown_page);
                    DateTime time = new DateTime(data.getAiring().getTime());
                    episodeNum = data.getAiring().getNext_episode();
                    dataId = data.getId();
                    millis = time.getMillis();
                    CountDownTimer timer = new CountDownTimer(millis - System.currentTimeMillis(), 500) {
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
                            countdown.setText(result);
                        }

                        @Override
                        public void onFinish() {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loadAnimePage(String.valueOf(dataId));
                                }
                            }, 5000);
                        }
                    }.start();
                    timers.add(timer);
                }

                //Animate progress bar out and display the data
                if (progress != null) {
                    progress.animate()
                            .alpha(0.0f)
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    progress.setVisibility(View.GONE);
                                }
                            });
                }
                findViewById(R.id.page).setVisibility(View.VISIBLE);
            }
            running = false;
        }
        @Override
        public void onLoaderReset(Loader<Anime> loader) {

        }
    }
}
