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

import com.yruili.animelist.Adapters.SchedulePagerStateAdapter;
import com.yruili.animelist.Fragments.Schedule.ScheduleAnimeFragment;
import com.yruili.animelist.Loaders.AnimeScheduleLoader;
import com.yruili.animelist.Loaders.AnimeSortLoad;
import com.yruili.animelist.Loaders.LogoutLoader;
import com.yruili.animelist.Network.HttpClient;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Model.AnimeList;
import com.yruili.animelist.Utils.ColumnUtil;
import com.yruili.animelist.Utils.CountDownCallback;
import com.yruili.animelist.Utils.ListContent;
import com.yruili.animelist.Utils.ListOptions;
import com.yruili.animelist.Utils.ScheduleUtil;
import com.yruili.animelist.Utils.SeasonUtil;

import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ScheduleAnimeFragment.OnScheduleAnimeFragmentInteractionListener, CountDownCallback{

    //TODO: Searching and schedule activities
    //TODO: provide way to mark anime + notifications

    String[] titles = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    int[] dayValues = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};
    ViewPager viewPager;
    TabLayout tabLayout;
    String season;
    String year;
    boolean loaded = false;
    SchedulePagerStateAdapter adapter;
    boolean noInternet = false;
    boolean running = false;
    boolean initRan = false;

    boolean grid;
    int sort = 2;
    int asc = 1;
    int currentSelectedTab = 0;

    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_schedule);

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
                    initLoadDataForList("Currently Airing", season, year, true);
                }
            }
        });

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        sort = sharedPref.getInt(getString(R.string.season_list_sorting), 2);
        asc = sharedPref.getInt(getString(R.string.season_list_ad), 1);
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

        String season = SeasonUtil.checkMonth(month);
        String year = String.valueOf(y);
        ListContent.setCurrentSeason(season);
        this.season = season;
        ListContent.setCurrentYear(year);
        this.year = year;

        /** Java implementation starts with sunday as day 1 **/
        int day = c.get(Calendar.DAY_OF_WEEK)-1;

        Log.w("DAYS", Calendar.SUNDAY + " " + Calendar.SATURDAY);

        tabLayout.getTabAt(day).select();
        currentSelectedTab = day;

        boolean reinit = (savedInstanceState != null);

        initLoadDataForList("Currently Airing", season, year, reinit);
    }

    private void setUpViewPager(ViewPager viewPager) {
        adapter = new SchedulePagerStateAdapter(getSupportFragmentManager(), dayValues, titles);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setOnPageChangeListener(new ViewPagerSwiperListener());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                        "Login failed...", Snackbar.LENGTH_SHORT).show();
                Log.e("LOGIN ERROR", "An error has occurred : " + error);
            } else {
                String code = uri.getQueryParameter("code");
                Snackbar.make(findViewById(R.id.main),
                        "Logging in...", Snackbar.LENGTH_SHORT).show();
                Log.w("CODE FOR LOGIN BOYS", code);
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
            menu.findItem(R.id.action_grid_toggle).setIcon(R.drawable.ic_list_white_24px);
            ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);
            setUpViewPager(viewPager);
            ListOptions.SCREEN_ORIENTATION = newConfig.orientation;
            if(tabLayout.getSelectedTabPosition() != currentSelectedTab)tabLayout.getTabAt(currentSelectedTab).select();
        } else {
            menu.findItem(R.id.action_grid_toggle).setIcon(R.drawable.ic_view_module_white_24px);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schedule, menu);

        this.menu = menu;
        if(!grid)menu.findItem(R.id.action_grid_toggle).setIcon(R.drawable.ic_view_module_white_24px);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_grid_toggle){
            //Toggle grid/list viewing ------------------- ListOptions.grid is flag for which type to use (list or grid), and column count is dynamically calculated for grids
            if(!grid){
                ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);
                item.setIcon(R.drawable.ic_list_white_24px);
                grid = true;
            } else {
                ListOptions.COLUMN_COUNT = 1;
                item.setIcon(R.drawable.ic_view_module_white_24px);
                grid = false;
            }
            currentSelectedTab = tabLayout.getSelectedTabPosition();
            setUpViewPager(viewPager);
            tabLayout.getTabAt(currentSelectedTab).select();
        }

        return super.onOptionsItemSelected(item);
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
                reloadDataForScheduleListSorted();
            }
        }
    }

    private class ViewPagerSwiperListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        @Override
        public void onPageSelected(int position) {
            if(initRan)
                changeTitle(position);
            currentSelectedTab = position;
        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private void changeTitle(int position){
        Calendar date = ScheduleUtil.nextDayOfWeek(dayValues[position]);
        Calendar nextWeek = null;
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == dayValues[position])
            nextWeek = ScheduleUtil.nextDayOfWeekOverride(dayValues[position]);
        getSupportActionBar().setTitle(SeasonUtil.getMonth(date.get(Calendar.MONTH) + 1) + " " + date.get(Calendar.DATE) + ", " + date.get(Calendar.YEAR));
        if(nextWeek != null)getSupportActionBar().setSubtitle(Html.fromHtml("<font color='#00BFA5'>"+SeasonUtil.getMonth(nextWeek.get(Calendar.MONTH) + 1) + " " + nextWeek.get(Calendar.DATE) + ", " + nextWeek.get(Calendar.YEAR)+"</font>"));
        else getSupportActionBar().setSubtitle(null);
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_schedule) {
        } else if (id == R.id.nav_marked) {
            Toast.makeText(this, "Selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_login) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.anilist.co/api/auth/authorize?" +
                    "grant_type=authorization_code" +
                    "&client_id="+ HttpClient.getClientId()+
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

    @Override
    public void onScheduleAnimeFragmentInteraction(Anime item) {
        Intent intent = new Intent(this, AnimePageActivity.class);
        intent.putExtra("ID", String.valueOf(item.getId()));
        startActivity(intent);
    }

    /**Initial loading in data and adding progress animation**/
    public void initLoadDataForList(String status, String season, String year, boolean reinit){
        if(getSupportLoaderManager().getLoader(7) == null)getSupportLoaderManager().initLoader(7, null, new InitLoad(this, status, season, year, reinit));
        else getSupportLoaderManager().restartLoader(7, null, new InitLoad(this, status, season, year, reinit));
    }

    /**Reload data when anime countdown is over**/
    public void reloadDataForScheduleList(String status, String season, String year){
        if(loaded) {
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            loaded = false;
            if (getSupportLoaderManager().getLoader(8) == null)
                getSupportLoaderManager().initLoader(8, null, new ReloadList(this, status, season, year)).forceLoad();
            else
                getSupportLoaderManager().restartLoader(8, null, new ReloadList(this, status, season, year)).forceLoad();
        }
    }

    /**Reload data when anime countdown is over**/
    public void reloadDataForScheduleListSorted(){
        if(loaded) {
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            loaded = false;
            if (getSupportLoaderManager().getLoader(9) == null)
                getSupportLoaderManager().initLoader(9, null, new SortList(this)).forceLoad();
            else
                getSupportLoaderManager().restartLoader(9, null, new SortList(this)).forceLoad();
        }
    }


    /**Countdown timer callback to reload list**/
    @Override
    public void call() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reloadDataForScheduleList("Currently Airing", season, year);
                Log.w("CALLBACK BRO", "BRUH");
                for(int i = 0; i < 7; i++){
                    if(adapter.getRegisteredFragment(i) != null){
                        ScheduleAnimeFragment frag = (ScheduleAnimeFragment) adapter.getRegisteredFragment(i);
                        frag.setCalled(false);
                    }
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
        private String status;
        private String season;
        private String year;
        private boolean reinit;
        public InitLoad(Context context, String status, String season, String year, boolean reinit){
            this.context = context;
            this.status = status;
            this.reinit = reinit;
            this.year = year;
            this.season = season;
        }
        @Override
        public Loader<AnimeList> onCreateLoader(int id, Bundle args) {
            return new AnimeScheduleLoader(context, status, season, year, sort, asc);
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
                for(int i = 0; i < 7; i++){
                    if(adapter.getRegisteredFragment(i) != null){
                        ScheduleAnimeFragment frag = (ScheduleAnimeFragment) adapter.getRegisteredFragment(i);
                        frag.reloadList();
                    }
                }
                /** Java implementation starts with sunday as day 1 **/
                int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;

                tabLayout.getTabAt(day).select();
                currentSelectedTab = day;
                changeTitle(day);

                loaded = true;
                initRan = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<AnimeList> loader) {
        }
    }
    /**Reloading list when countdown is zero**/
    private class ReloadList implements LoaderManager.LoaderCallbacks<AnimeList> {
        private Context context;
        private String status;
        private String season;
        private String year;
        public ReloadList(Context context, String status, String season, String year){
            this.context = context;
            this.status = status;
            this.season = season;
            this.year = year;
        }
        @Override
        public Loader<AnimeList> onCreateLoader(int id, Bundle args) {
            return new AnimeScheduleLoader(context, status, season, year, sort, asc);
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
                ListContent.setList(data);
                for(int i = 0; i < 7; i++){
                    if(adapter.getRegisteredFragment(i) != null){
                        ScheduleAnimeFragment frag = (ScheduleAnimeFragment) adapter.getRegisteredFragment(i);
                        frag.reloadList();
                    }
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
                /** Java implementation starts with sunday as day 1 **/
                int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;

                if(tabLayout.getSelectedTabPosition() == day) {
                    currentSelectedTab = day;
                    changeTitle(day);
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
                ListContent.setList(data);
                for(int i = 0; i < 7; i++){
                    if(adapter.getRegisteredFragment(i) != null){
                        ScheduleAnimeFragment frag = (ScheduleAnimeFragment) adapter.getRegisteredFragment(i);
                        frag.reloadList();
                    }
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
                loaded = true;
            }
        }

        @Override
        public void onLoaderReset(Loader<AnimeList> loader) {
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
