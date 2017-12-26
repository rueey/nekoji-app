package com.yruili.animelist.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yruili.animelist.Adapters.SeasonPagerStateAdapter;
import com.yruili.animelist.Fragments.Season.AllAnimeFragment;
import com.yruili.animelist.Fragments.Season.MovieAnimeFragment;
import com.yruili.animelist.Fragments.Season.OVAAnimeFragment;
import com.yruili.animelist.Fragments.Season.TVAnimeFragment;
import com.yruili.animelist.Loaders.AnimeSeasonLoader;
import com.yruili.animelist.Loaders.AnimeSeasonReload;
import com.yruili.animelist.Loaders.AnimeSortLoad;
import com.yruili.animelist.Loaders.LoginLoader;
import com.yruili.animelist.Loaders.LogoutLoader;
import com.yruili.animelist.Network.HttpClient;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Model.AnimeList;
import com.yruili.animelist.Utils.ColumnUtil;
import com.yruili.animelist.Utils.CountDownCallback;
import com.yruili.animelist.Utils.ListContent;
import com.yruili.animelist.Utils.ListOptions;
import com.yruili.animelist.Utils.SeasonUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TVAnimeFragment.OnTVAnimeFragmentInteractionListener,
        AllAnimeFragment.OnAllAnimeFragmentInteractionListener,
        MovieAnimeFragment.OnMovieAnimeFragmentInteractionListener,
        OVAAnimeFragment.OnOVAAnimeFragmentInteractionListener, CountDownCallback{

    //TODO: Searching and schedule activities
    //TODO: provide way to mark anime + notifications

    ViewPager viewPager;
    TabLayout tabLayout;
    boolean loaded = false;
    String season;
    String year;
    SeasonPagerStateAdapter adapter;
    boolean noInternet = false;
    ArrayList<String> years = new ArrayList<>();
    boolean running = false;

    boolean grid;
    int sort = 0;
    int asc = -1;
    int currentSelectedTab = 0;

    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_season_list);
        SharedPreferences s = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        boolean loggedIn = s.getBoolean(getString(R.string.login), false);
        if(loggedIn){
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_login).setVisible(false);
            menu.findItem(R.id.nav_register).setVisible(false);
            menu.findItem(R.id.nav_logout).setVisible(true);
        }

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.list_config);
        myFab.setOnClickListener(new SelectSortDialogFabListener(this));

        TextView retry = (TextView) findViewById(R.id.retry_text);
        retry.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!running) {
                    running = true;
                    findViewById(R.id.retry_text).setVisibility(View.GONE);
                    findViewById(R.id.connect_text).setVisibility(View.GONE);
                    findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                    initLoadDataForList(season, year, true);
                }
            }
        });

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        sort = sharedPref.getInt(getString(R.string.season_list_sorting), 0);
        asc = sharedPref.getInt(getString(R.string.season_list_ad), -1);
        grid = sharedPref.getBoolean(getString(R.string.season_list_grid), false);
        if(grid)ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setUpViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);

        if(ListContent.getList() != null)ListContent.setList(new AnimeList());

        if(savedInstanceState == null) {
            String season = SeasonUtil.checkMonth(month);
            String year = String.valueOf(y);
            ListContent.setCurrentSeason(season);
            this.season = season;
            ListContent.setCurrentYear(year);
            this.year = year;
            Log.w("Not saved:", year + " " + season);
        } else {
            this.season = savedInstanceState.getString("SEASON");
            this.year = savedInstanceState.getString("YEAR");
            ListContent.setCurrentSeason(season);
            ListContent.setCurrentYear(year);
            Log.w("saved:", year + " " + season);
        }
        Log.w("Current ym:", year + " " + season);
        for(int i = 1951; i <= y+1; i++){
            years.add(String.valueOf(i));
        }
        //FragmentManager.enableDebugLogging(true);
        getSupportActionBar().setTitle(season + " " + year);
        //noinspection deprecation
        getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#00BFA5'>"+SeasonUtil.getSubtitle(season)+"</font>"));

        boolean reinit = (savedInstanceState != null);

        initLoadDataForList(season, year, reinit);
    }

    private void setUpViewPager(ViewPager viewPager) {
        adapter = new SeasonPagerStateAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPagerSwiperListener());
        viewPager.setOffscreenPageLimit(1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("SEASON", season);
        outState.putString("YEAR", year);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int o = getResources().getConfiguration().orientation;
        if(o != ListOptions.SCREEN_ORIENTATION){
            ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);
            setUpViewPager(viewPager);
            ListOptions.SCREEN_ORIENTATION = o;
            if(tabLayout.getSelectedTabPosition() != currentSelectedTab)tabLayout.getTabAt(currentSelectedTab).select();
        }
        if(getIntent()!=null && getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Snackbar.make(findViewById(R.id.main),
                        "Login failed!", Snackbar.LENGTH_SHORT).show();
                Log.e("LOGIN ERROR", "An error has occurred : " + error);
            } else {
                String code = uri.getQueryParameter("code");
                Snackbar.make(findViewById(R.id.main),
                        "Logging in...", Snackbar.LENGTH_LONG).show();
                Log.w("CODE FOR LOGIN BOYS", code);
                getSupportLoaderManager().initLoader(10, null, new Login(this, code)).forceLoad();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeSettings();
    }

    public void writeSettings(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.season_list_sorting), sort);
        editor.putInt(getString(R.string.season_list_ad), asc);
        editor.putBoolean(getString(R.string.season_list_grid), grid);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.w("CONFIGURATION", "CHANGED");
        // Checks the orientation of the screen
        if(grid){
            ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);
            setUpViewPager(viewPager);
            ListOptions.SCREEN_ORIENTATION = newConfig.orientation;
            menu.findItem(R.id.action_grid_toggle).setTitle("Change to list");
            if(tabLayout.getSelectedTabPosition() != currentSelectedTab)tabLayout.getTabAt(currentSelectedTab).select();
        } else {
            menu.findItem(R.id.action_grid_toggle).setTitle("Change to grid");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        this.menu = menu;
        if(grid)menu.findItem(R.id.action_grid_toggle).setTitle("Change to list");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_previous_season){
            if(loaded && !(season.toLowerCase().equals("winter") && (Integer.valueOf(year) == 1951))){
                //Update for previous season
                String[] a = SeasonUtil.prevSeason(season, year);
                loadDataForSeasonList(a[0], a[1]);
                season = a[0];
                year = a[1];
                ListContent.setCurrentYear(year);
                ListContent.setCurrentSeason(season);
            }
            return true;
        } else if(id == R.id.action_next_season){
            //Update for next season
            if(loaded && !(season.toLowerCase().equals("fall") && (Integer.valueOf(year) == Calendar.getInstance().get(Calendar.YEAR)+1))){
                String[] a = SeasonUtil.nextSeason(season, year);
                loadDataForSeasonList(a[0], a[1]);
                season = a[0];
                year = a[1];
                ListContent.setCurrentYear(year);
                ListContent.setCurrentSeason(season);
            }
            return true;
        } else if(id == R.id.action_selectSeason){
            if(loaded) {
                //Create builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                builder.setTitle("Select Season");
                View dialogView = inflater.inflate(R.layout.season_select_dialog, null);
                builder.setView(dialogView);

                //Instantiate spinners and set the current selection options
                Spinner seasonMenu = (Spinner) dialogView.findViewById(R.id.action_dialog_selectSeason);
                ArrayAdapter<CharSequence> seasonAdapter = ArrayAdapter.createFromResource(this, R.array.season_array, R.layout.spinner_item);
                seasonAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                seasonMenu.setAdapter(seasonAdapter);

                int idx = 0;
                if (season.toLowerCase().equals("spring")) idx = 1;
                else if (season.toLowerCase().equals("summer")) idx = 2;
                else if (season.toLowerCase().equals("fall")) idx = 3;
                seasonMenu.setSelection(idx);

                Spinner yearMenu = (Spinner) dialogView.findViewById(R.id.action_dialog_selectYear);
                ArrayAdapter<CharSequence> yearAdapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_item, years.toArray(new String[years.size()]));
                yearAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                yearMenu.setAdapter(yearAdapter);

                yearMenu.setSelection(years.indexOf(year));

                //Create and show dialog
                builder.setPositiveButton("Apply", new SelectSeasonListener(dialogView)).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                final Dialog d = builder.create();
                d.show();
            }
        } else if(id == R.id.action_grid_toggle){
            //Toggle grid/list viewing ------------------- ListOptions.grid is flag for which type to use (list or grid), and column count is dynamically calculated for grids
            if(!grid){
                ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);
                grid = true;
                item.setTitle("Change to list");
            } else {
                ListOptions.COLUMN_COUNT = 1;
                grid = false;
                item.setTitle("Change to grid");
            }
            currentSelectedTab = tabLayout.getSelectedTabPosition();
            setUpViewPager(viewPager);
            tabLayout.getTabAt(currentSelectedTab).select();
        }

        return super.onOptionsItemSelected(item);
    }

    private class SelectSeasonListener implements DialogInterface.OnClickListener{
        View dialogView;
        public SelectSeasonListener(View v){
            dialogView = v;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Spinner seasonSpinner = (Spinner) dialogView.findViewById(R.id.action_dialog_selectSeason);
            Spinner yearSpinner = (Spinner) dialogView.findViewById(R.id.action_dialog_selectYear);
            if(!seasonSpinner.getSelectedItem().toString().toLowerCase().equals(season.toLowerCase()) || !yearSpinner.getSelectedItem().toString().toLowerCase().equals(year.toLowerCase())) {
                loadDataForSeasonList(seasonSpinner.getSelectedItem().toString(), yearSpinner.getSelectedItem().toString());
                ListContent.setCurrentSeason(seasonSpinner.getSelectedItem().toString());
                year = yearSpinner.getSelectedItem().toString();
                season = seasonSpinner.getSelectedItem().toString();
            }
        }
    }

    private class SelectSortDialogFabListener implements View.OnClickListener {
        Context context;
        public SelectSortDialogFabListener(Context context){
            this.context = context;
        }
        @Override
        public void onClick(View v) {
            if(loaded) {
                //Create builder
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                builder.setTitle("Sort");
                View dialogView = inflater.inflate(R.layout.sort_select_dialog, null);
                builder.setView(dialogView);

                //Instantiate spinners
                Spinner sortMenu = (Spinner) dialogView.findViewById(R.id.action_dialog_selectSort);
                ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(context, R.array.sort_array, R.layout.spinner_item);
                sortAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                sortMenu.setAdapter(sortAdapter);

                Spinner adMenu = (Spinner) dialogView.findViewById(R.id.action_dialog_selectAD);
                ArrayAdapter<CharSequence> adAdapter = ArrayAdapter.createFromResource(context, R.array.ad_array, R.layout.spinner_item);
                adAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                adMenu.setAdapter(adAdapter);

                //Set the current options
                sortMenu.setSelection(sort);
                adMenu.setSelection(asc == -1 ? 0 : 1);

                //Create and show dialog
                builder.setPositiveButton("Apply", new SelectSortListener(dialogView)).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                final Dialog d = builder.create();
                d.show();
            }
        }
    }

    private class SelectSortListener implements DialogInterface.OnClickListener{
        View dialogView;
        public SelectSortListener(View v){
            dialogView = v;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Spinner sortSpinner = (Spinner) dialogView.findViewById(R.id.action_dialog_selectSort);
            Spinner adSpinner = (Spinner) dialogView.findViewById(R.id.action_dialog_selectAD);
            if(sortSpinner.getSelectedItemPosition() != sort || (adSpinner.getSelectedItemPosition() == 0 ? -1 : 1) != asc) {
                sort = sortSpinner.getSelectedItemPosition();
                asc = adSpinner.getSelectedItemPosition() == 0 ? -1 : 1;
                reloadDataForSeasonListSorted();
            }
        }
    }

    private class ViewPagerSwiperListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        @Override
        public void onPageSelected(int position) {
            currentSelectedTab = position;
        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_season_list) {
        } else if (id == R.id.nav_schedule) {
            Intent intent = new Intent(this, ScheduleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_marked) {
            Toast.makeText(this, "Selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_login) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.anilist.co/api/auth/authorize?" +
                    "grant_type=authorization_code" +
                    "&client_id="+HttpClient.getClientId()+
                    "&redirect_uri=https://www.google.com/ANIME_CHART_LOGIN_REDIRECT" +
                    "&response_type=code"));
            startActivity(browserIntent);
        } else if(id == R.id.nav_register){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.anilist.co/register"));
            startActivity(browserIntent);
        } else if(id == R.id.nav_logout){
            getSupportLoaderManager().initLoader(11, null, new Logout(this)).forceLoad();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**All Anime fragment interface**/
    @Override
    public void onAllAnimeFragmentInteraction(Anime item) {
        Intent intent = new Intent(this, AnimePageActivity.class);
        intent.putExtra("ID", String.valueOf(item.getId()));
        startActivity(intent);
    }
    //TV Anime fragment interface
    @Override
    public void onTVAnimeFragmentInteraction(Anime item) {
        Intent intent = new Intent(this, AnimePageActivity.class);
        intent.putExtra("ID", String.valueOf(item.getId()));
        startActivity(intent);
    }
    /**Movie Anime fragment interface**/
    @Override
    public void onMovieAnimeFragmentInteraction(Anime item) {
        Intent intent = new Intent(this, AnimePageActivity.class);
        intent.putExtra("ID", String.valueOf(item.getId()));
        startActivity(intent);
    }
    /**OVA/SPECIALS Anime fragment interface**/
    @Override
    public void onOVAAnimeFragmentInteraction(Anime item) {
        Intent intent = new Intent(this, AnimePageActivity.class);
        intent.putExtra("ID", String.valueOf(item.getId()));
        startActivity(intent);
    }

    /**Initial loading in data and adding progress animation**/
    public void initLoadDataForList(String season, String year, boolean reinit){
        if(getSupportLoaderManager().getLoader(0) == null)getSupportLoaderManager().initLoader(0, null, new InitLoad(this, season, year, reinit));
        else getSupportLoaderManager().restartLoader(0, null, new InitLoad(this, season, year, reinit));
    }
    /**Loading in data and adding progress animation for season list**/
    public void loadDataForSeasonList(String season, String year){
        findViewById(R.id.list).setVisibility(View.GONE);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        loaded = false;
        getSupportActionBar().setTitle(season + " " + year);
        //noinspection deprecation
        getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#00BFA5'>"+SeasonUtil.getSubtitle(season)+"</font>"));
        if(getSupportLoaderManager().getLoader(1) == null)getSupportLoaderManager().initLoader(1, null, new SeasonLoad(this, season, year));
        else getSupportLoaderManager().restartLoader(1, null, new SeasonLoad(this, season, year));
    }

    /**Reload data when anime countdown is over**/
    public void reloadDataForSeasonList(String season, String year){
        if(loaded) {
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            loaded = false;
            if (getSupportLoaderManager().getLoader(3) == null)
                getSupportLoaderManager().initLoader(3, null, new ReloadList(this, season, year)).forceLoad();
            else
                getSupportLoaderManager().restartLoader(3, null, new ReloadList(this, season, year)).forceLoad();
        }
    }

    /**Reload data when anime countdown is over**/
    public void reloadDataForSeasonListSorted(){
        if(loaded) {
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            loaded = false;
            if (getSupportLoaderManager().getLoader(4) == null)
                getSupportLoaderManager().initLoader(4, null, new SortList(this)).forceLoad();
            else
                getSupportLoaderManager().restartLoader(4, null, new SortList(this)).forceLoad();
        }
    }


    /**Countdown timer callback to reload list**/
    @Override
    public void call() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reloadDataForSeasonList(season, year);
                Log.w("CALLBACK BRO", "BRUH");
                if(adapter.getRegisteredFragment(0) != null){
                    AllAnimeFragment all = (AllAnimeFragment) adapter.getRegisteredFragment(0);
                    all.setCalled(false);
                }
                if(adapter.getRegisteredFragment(1) != null){
                    TVAnimeFragment tv = (TVAnimeFragment) adapter.getRegisteredFragment(1);
                    tv.setCalled(false);
                }
                if(adapter.getRegisteredFragment(2) != null){
                    MovieAnimeFragment movie = (MovieAnimeFragment) adapter.getRegisteredFragment(2);
                    movie.setCalled(false);
                }
                if(adapter.getRegisteredFragment(3) != null){
                    OVAAnimeFragment ova = (OVAAnimeFragment) adapter.getRegisteredFragment(3);
                    ova.setCalled(false);
                }
            }
        }, 5000);
    }
    @Override
    public boolean getGrid() {
        return grid;
    }
    @Override
    public int getSort() {
        return sort;
    }
    @Override
    public int getAsc() {
        return asc;
    }

    /**Initialization loading**/
    private class InitLoad implements LoaderManager.LoaderCallbacks<AnimeList> {
        private Context context;
        private String season; //Initial season and year
        private String year;
        private boolean reinit; //Currently not used due to overhaul with viewpager adapter
        public InitLoad(Context context, String season, String year, boolean reinit){
            this.context = context;
            this.season = season;
            this.year = year;
            this.reinit = reinit;
        }
        @Override
        public Loader<AnimeList> onCreateLoader(int id, Bundle args) {
            return new AnimeSeasonLoader(context, season, year, sort, asc);
        }

        @Override
        public void onLoadFinished(Loader<AnimeList> loader, AnimeList data) {
            ViewPager view = (ViewPager) findViewById(R.id.viewpager);
            final View progress = findViewById(R.id.progress_bar);
            TextView connect = (TextView) findViewById(R.id.connect_text);
            TextView retry = (TextView) findViewById(R.id.retry_text);
            running = false;
            if(data == null){
                noInternet = true;
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
                view.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
                retry.setVisibility(View.VISIBLE);
            } else {
                if (noInternet) {
                    noInternet = false;
                    Log.w("HI THERE", "WTF");
                    connect.setVisibility(View.GONE);
                    retry.setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                }
                if ((season.toLowerCase() + " " + year.toLowerCase()).equals(getSupportActionBar().getTitle().toString().toLowerCase())) {
                    ListContent.setList(data);
                    Log.w("Size of data", String.valueOf(data.getAll().size()));
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
                    if(adapter.getRegisteredFragment(0) != null){
                        AllAnimeFragment all = (AllAnimeFragment) adapter.getRegisteredFragment(0);
                        all.updateList();
                    }
                    if(adapter.getRegisteredFragment(1) != null){
                        TVAnimeFragment tv = (TVAnimeFragment) adapter.getRegisteredFragment(1);
                        tv.updateList();
                    }
                    if(adapter.getRegisteredFragment(2) != null){
                        MovieAnimeFragment movie = (MovieAnimeFragment) adapter.getRegisteredFragment(2);
                        movie.updateList();
                    }
                    if(adapter.getRegisteredFragment(3) != null){
                        OVAAnimeFragment ova = (OVAAnimeFragment) adapter.getRegisteredFragment(3);
                        ova.updateList();
                    }
                }
                loaded = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<AnimeList> loader) {
        }
    }

    /**Loading for each season**/
    private class SeasonLoad implements LoaderManager.LoaderCallbacks<AnimeList> {
        private Context context;
        private String season; //Updated season
        private String year; //Updated year
        public SeasonLoad(Context context, String season, String year){
            this.context = context;
            this.season = season;
            this.year = year;
        }
        @Override
        public Loader<AnimeList> onCreateLoader(int id, Bundle args) {
            return new AnimeSeasonLoader(context, season, year, sort, asc);
        }

        @Override
        public void onLoadFinished(Loader<AnimeList> loader, AnimeList data) {
            if(data == null){
                noInternet = true;
                final View progress = findViewById(R.id.progress_bar);
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
                findViewById(R.id.viewpager).setVisibility(View.GONE);
                findViewById(R.id.connect_text).setVisibility(View.VISIBLE);
                findViewById(R.id.retry_text).setVisibility(View.VISIBLE);
            } else {
                if (noInternet) {
                    noInternet = false;
                    Log.w("HI THERE", "WTF");
                    findViewById(R.id.connect_text).setVisibility(View.GONE);
                    findViewById(R.id.retry_text).setVisibility(View.GONE);
                    findViewById(R.id.viewpager).setVisibility(View.VISIBLE);
                }
                if ((season.toLowerCase() + " " + year.toLowerCase()).equals(getSupportActionBar().getTitle().toString().toLowerCase())) {
                    ListContent.setList(data);
                    if(adapter.getRegisteredFragment(0) != null){
                        AllAnimeFragment all = (AllAnimeFragment) adapter.getRegisteredFragment(0);
                        all.updateList();
                    }
                    if(adapter.getRegisteredFragment(1) != null){
                        TVAnimeFragment tv = (TVAnimeFragment) adapter.getRegisteredFragment(1);
                        tv.updateList();
                    }
                    if(adapter.getRegisteredFragment(2) != null){
                        MovieAnimeFragment movie = (MovieAnimeFragment) adapter.getRegisteredFragment(2);
                        movie.updateList();
                    }
                    if(adapter.getRegisteredFragment(3) != null){
                        OVAAnimeFragment ova = (OVAAnimeFragment) adapter.getRegisteredFragment(3);
                        ova.updateList();
                    }
                    final View progress = findViewById(R.id.progress_bar);
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
                    findViewById(R.id.list).setVisibility(View.VISIBLE);
                }
                loaded = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<AnimeList> loader) {
        }
    }
    /**Reloading list when countdown is zero**/
    private class ReloadList implements LoaderManager.LoaderCallbacks<AnimeList> {
        private Context context;
        private String season; //Updated season
        private String year; //Updated year
        public ReloadList(Context context, String season, String year){
            this.context = context;
            this.season = season;
            this.year = year;
        }
        @Override
        public Loader<AnimeList> onCreateLoader(int id, Bundle args) {
            return new AnimeSeasonReload(context, season, year, sort, asc);
        }

        @Override
        public void onLoadFinished(Loader<AnimeList> loader, AnimeList data) {
            if(data == null){
                noInternet = true;
                final View progress = findViewById(R.id.progress_bar);
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
                findViewById(R.id.viewpager).setVisibility(View.GONE);
                findViewById(R.id.connect_text).setVisibility(View.VISIBLE);
                findViewById(R.id.retry_text).setVisibility(View.VISIBLE);
            } else {
                if (noInternet) {
                    noInternet = false;
                    Log.w("HI THERE", "WTF");
                    findViewById(R.id.connect_text).setVisibility(View.GONE);
                    findViewById(R.id.retry_text).setVisibility(View.GONE);
                    findViewById(R.id.viewpager).setVisibility(View.VISIBLE);
                }
                if ((season.toLowerCase() + " " + year.toLowerCase()).equals(getSupportActionBar().getTitle().toString().toLowerCase())) {
                    ListContent.setList(data);
                    if(adapter.getRegisteredFragment(0) != null){
                        AllAnimeFragment all = (AllAnimeFragment) adapter.getRegisteredFragment(0);
                        all.reloadList();
                    }
                    if(adapter.getRegisteredFragment(1) != null){
                        TVAnimeFragment tv = (TVAnimeFragment) adapter.getRegisteredFragment(1);
                        tv.reloadList();
                    }
                    if(adapter.getRegisteredFragment(2) != null){
                        MovieAnimeFragment movie = (MovieAnimeFragment) adapter.getRegisteredFragment(2);
                        movie.reloadList();
                    }
                    if(adapter.getRegisteredFragment(3) != null){
                        OVAAnimeFragment ova = (OVAAnimeFragment) adapter.getRegisteredFragment(3);
                        ova.reloadList();
                    }
                    final View progress = findViewById(R.id.progress_bar);
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
                }
                loaded = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<AnimeList> loader) {
        }
    }

    /**Sorts list and reloads**/
    private class SortList implements LoaderManager.LoaderCallbacks<AnimeList> {
        private Context context;
        public SortList(Context context){
            this.context = context;
        }
        @Override
        public Loader<AnimeList> onCreateLoader(int id, Bundle args) {
            return new AnimeSortLoad(context, sort, asc);
        }

        @Override
        public void onLoadFinished(Loader<AnimeList> loader, AnimeList data) {
            if(data != null){
                if (noInternet) {
                    noInternet = false;
                    findViewById(R.id.connect_text).setVisibility(View.GONE);
                    findViewById(R.id.retry_text).setVisibility(View.GONE);
                    findViewById(R.id.viewpager).setVisibility(View.VISIBLE);
                }
                if ((season.toLowerCase() + " " + year.toLowerCase()).equals(getSupportActionBar().getTitle().toString().toLowerCase())) {
                    ListContent.setList(data);
                    if(adapter.getRegisteredFragment(0) != null){
                        AllAnimeFragment all = (AllAnimeFragment) adapter.getRegisteredFragment(0);
                        all.reloadList();
                    }
                    if(adapter.getRegisteredFragment(1) != null){
                        TVAnimeFragment tv = (TVAnimeFragment) adapter.getRegisteredFragment(1);
                        tv.reloadList();
                    }
                    if(adapter.getRegisteredFragment(2) != null){
                        MovieAnimeFragment movie = (MovieAnimeFragment) adapter.getRegisteredFragment(2);
                        movie.reloadList();
                    }
                    if(adapter.getRegisteredFragment(3) != null){
                        OVAAnimeFragment ova = (OVAAnimeFragment) adapter.getRegisteredFragment(3);
                        ova.reloadList();
                    }
                    final View progress = findViewById(R.id.progress_bar);
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
                }
                loaded = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<AnimeList> loader) {
        }
    }

    /** Login **/
    private class Login implements LoaderManager.LoaderCallbacks<Boolean> {
        private Context context;
        private String code;
        public Login(Context context, String code){
            this.context = context;
            this.code = code;
        }
        @Override
        public Loader<Boolean> onCreateLoader(int id, Bundle args) {
            return new LoginLoader(context, code);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
            if(data != null && data){
                Snackbar.make(findViewById(R.id.main),
                        "Login Successful!", Snackbar.LENGTH_LONG).show();
                SharedPreferences sharedPref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.login), data);
                editor.apply();
                NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
                Menu menu = navigationView.getMenu();
                menu.findItem(R.id.nav_login).setVisible(false);
                menu.findItem(R.id.nav_register).setVisible(false);
                menu.findItem(R.id.nav_logout).setVisible(true);
            } else {
                Snackbar.make(findViewById(R.id.main),
                        "Login Unsuccessful!", Snackbar.LENGTH_LONG).show();
            }
        }
        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    }

    /** Logout **/
    private class Logout implements LoaderManager.LoaderCallbacks<Boolean> {
        private Context context;
        public Logout(Context context){
            this.context = context;
        }
        @Override
        public Loader<Boolean> onCreateLoader(int id, Bundle args) {
            return new LogoutLoader(context);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
            if(data != null && data){
                Snackbar.make(findViewById(R.id.main),
                        "Logout Successful!", Snackbar.LENGTH_LONG).show();
                SharedPreferences sharedPref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.login), !data);
                editor.apply();
                NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
                Menu menu = navigationView.getMenu();
                menu.findItem(R.id.nav_login).setVisible(true);
                menu.findItem(R.id.nav_register).setVisible(true);
                menu.findItem(R.id.nav_logout).setVisible(false);
            } else {
                Snackbar.make(findViewById(R.id.main),
                        "Logout Unsuccessful! An unexpected error occurred...", Snackbar.LENGTH_LONG).show();
            }
        }
        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    }
}
