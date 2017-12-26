package com.yruili.animelist.Activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yruili.animelist.Fragments.Search.SearchAnimeFragment;
import com.yruili.animelist.Loaders.AnimeSearchLoader;
import com.yruili.animelist.Loaders.AnimeSortLoad;
import com.yruili.animelist.Loaders.LogoutLoader;
import com.yruili.animelist.Network.HttpClient;
import com.yruili.animelist.R;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Model.AnimeList;
import com.yruili.animelist.Utils.ColumnUtil;
import com.yruili.animelist.Utils.ListContent;
import com.yruili.animelist.Utils.ListOptions;
import com.yruili.animelist.Utils.SearchCallback;

public class SearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SearchAnimeFragment.OnSearchAnimeFragmentInteractionListener, SearchCallback{

    //TODO: clean up this shit up and test for bugs

    boolean loaded = true;
    String query;
    int page = 1;
    boolean noInternet = false;
    boolean running = false;
    boolean atEnd = false;

    boolean grid;
    int sort = 0;
    int asc = -1;

    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_search);

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
                    queryLoadDataForList(query, page);
                }
            }
        });

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        sort = sharedPref.getInt(getString(R.string.season_list_sorting), 0);
        asc = sharedPref.getInt(getString(R.string.season_list_ad), -1);
        grid = sharedPref.getBoolean(getString(R.string.season_list_grid), false);
        if(grid){
            ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);
        }
        setupFragment();

        if(ListContent.getList() != null)ListContent.setList(new AnimeList());

        if(savedInstanceState == null) {

            //Do some stuff here wtf

        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /*** The searching method ***/
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if(loaded && !query.equals(this.query) && !query.isEmpty()) {
                if(page != 1)page = 1;
                atEnd = false;
                this.query = query;
                final View progress = findViewById(R.id.progress_bar);
                progress.setVisibility(View.VISIBLE);
                if(findViewById(R.id.no_results_text).getVisibility() == View.VISIBLE)findViewById(R.id.no_results_text).setVisibility(View.GONE);
                loaded = false;
                queryLoadDataForList(query, page);
                Log.w("QUERING", query);
            }
        }
    }

    @Override
    public void loadNextBatch(int page) {
        if(loaded && !atEnd){
            this.page = page;
            Log.w("LOADING NEXT", String.valueOf(page));
            final View progress = findViewById(R.id.progress_bar);
            progress.setVisibility(View.VISIBLE);
            if(findViewById(R.id.no_results_text).getVisibility() == View.VISIBLE)findViewById(R.id.no_results_text).setVisibility(View.GONE);
            loaded = false;
            queryLoadDataForList(query, page);
        }
    }
    @Override
    public int getPage() {
        return page;
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

    public void setupFragment(){
        if(getSupportFragmentManager().findFragmentByTag("search") != null)getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("search")).commitAllowingStateLoss();;
        getSupportFragmentManager().beginTransaction().add(R.id.search_list, SearchAnimeFragment.newInstance(ListOptions.COLUMN_COUNT), "search").commitAllowingStateLoss();
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
        if(grid && o != ListOptions.SCREEN_ORIENTATION){
            ListOptions.COLUMN_COUNT = ColumnUtil.calculateNoOfColumns(this);
            setupFragment();
            ListOptions.SCREEN_ORIENTATION = o;
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.season_list_sorting), sort);
        editor.putInt(getString(R.string.season_list_ad), asc);
        editor.putBoolean(getString(R.string.season_list_grid), grid);
        editor.apply();
        Log.w("WROTE", "SETTINGS");
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
            setupFragment();
            ListOptions.SCREEN_ORIENTATION = newConfig.orientation;
        } else {
            menu.findItem(R.id.action_grid_toggle).setIcon(R.drawable.ic_view_module_white_24px);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        if(!grid){
            menu.findItem(R.id.action_grid_toggle).setIcon(R.drawable.ic_view_module_white_24px);
        }
        this.menu = menu;

        return super.onCreateOptionsMenu(menu);
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
            Log.w("REINIT", "FOR GRID");
            setupFragment();
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
                if(ListContent.getList() != null)reloadDataForQueryListSorted();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_search) {
        } else if (id == R.id.nav_season_list) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_schedule) {
            Intent intent = new Intent(this, ScheduleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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

    /**All Anime fragment interface**/
    @Override
    public void onSearchAnimeFragmentInteraction(Anime item) {
        Intent intent = new Intent(this, AnimePageActivity.class);
        intent.putExtra("ID", String.valueOf(item.getId()));
        startActivity(intent);
    }

    /**Initial loading in data and adding progress animation**/
    public void queryLoadDataForList(String query, int page){
        if(getSupportLoaderManager().getLoader(5) == null)getSupportLoaderManager().initLoader(5, null, new QueryLoad(this, query, page));
        else getSupportLoaderManager().restartLoader(5, null, new QueryLoad(this, query, page));
    }

    public void reloadDataForQueryListSorted(){
        if(loaded) {
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            loaded = false;
            if (getSupportLoaderManager().getLoader(6) == null)
                getSupportLoaderManager().initLoader(6, null, new SortList(this)).forceLoad();
            else
                getSupportLoaderManager().restartLoader(6, null, new SortList(this)).forceLoad();
        }
    }

    /**Initialization loading**/
    private class QueryLoad implements LoaderManager.LoaderCallbacks<AnimeList> {
        private Context context;
        private String query;
        private int page;
        public QueryLoad(Context context, String query, int page){
            this.context = context;
            this.query = query;
            this.page = page;
        }
        @Override
        public Loader<AnimeList> onCreateLoader(int id, Bundle args) {
            return new AnimeSearchLoader(context, query, page, sort, asc);
        }

        @Override
        public void onLoadFinished(Loader<AnimeList> loader, AnimeList data) {
            LinearLayout view = (LinearLayout) findViewById(R.id.search_list);
            final View progress = findViewById(R.id.progress_bar);
            TextView connect = (TextView) findViewById(R.id.connect_text);
            TextView retry = (TextView) findViewById(R.id.retry_text);
            TextView noResults = (TextView) findViewById(R.id.no_results_text);

            running = false; //Retrying for network

            if (data == null && !isNetworkAvailable(context)) {
                noInternet = true;
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
                view.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
                retry.setVisibility(View.VISIBLE);
            } else if (data == null && page == 1) {
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
                view.setVisibility(View.GONE);
                noResults.setVisibility(View.VISIBLE);
            } else if(data == null){
                atEnd = true;
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
            } else {
                if (noInternet) {
                    noInternet = false;
                    Log.w("HI THERE", "WTF");
                    connect.setVisibility(View.GONE);
                    retry.setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                }
                if(view.getVisibility() == View.GONE)view.setVisibility(View.VISIBLE);
                ListContent.setList(data);
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
                SearchAnimeFragment frag = (SearchAnimeFragment)getSupportFragmentManager().findFragmentByTag("search");
                if(page == 1)frag.updateList();
                else frag.reloadList();
            }
            if(ListContent.getList() != null)ListContent.getList().setQuery(query);
            loaded = true;
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
                }
                ListContent.setList(data);
                ListContent.getList().setQuery(query);
                SearchAnimeFragment frag = (SearchAnimeFragment)getSupportFragmentManager().findFragmentByTag("search");
                frag.reloadList();
                final View progress = findViewById(R.id.progress_bar);
                if (progress != null) {
                    progress.setVisibility(View.GONE);
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
    /**
     *
     * @param context
     * @return true if connected to internet
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            return (networkInfo != null && networkInfo.isConnected());
        } catch (Exception ex) {
            return false;
        }
    }
}
