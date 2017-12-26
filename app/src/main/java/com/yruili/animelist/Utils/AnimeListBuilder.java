package com.yruili.animelist.Utils;

import android.util.Log;

import com.yruili.animelist.App;
import com.yruili.animelist.Model.Anime;
import com.yruili.animelist.Model.AnimeList;
import com.yruili.animelist.Network.HttpClient;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rui on 18/07/17.
 *
 * ANIMELIST FACTORY CLASS THAT CREATES ANIMELISTS FROM AniList.co API
 *
 * ONLY FOR NETWORK QUERIES
 * READING FROM STORED DATA MUST BE IMPLEMENTED SEPARATELY
 *
 */
public class AnimeListBuilder {
    /*
     * Implementation of networking and JSON parsing in these methods to obtain anime lists/pages
     */
    private static ArrayList<Anime> getSeasonList(String season, int sort, int asc){
        String s = season.substring(0, season.indexOf(" ")).toLowerCase();
        String y = season.substring(season.indexOf(" ") + 1);
        try {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("www.anilist.co")
                    .addPathSegment("api")
                    .addPathSegment("browse")
                    .addPathSegment("anime")
                    .addQueryParameter("access_token", HttpClient.getAccessToken())
                    .addQueryParameter("season", s)
                    .addQueryParameter("year", y)
                    .addQueryParameter("airing_data", "true")
                    .addQueryParameter("full_page", "true")
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Cache-Control", "max-stale=3600000")
                    .tag("season")
                    .build();
            Response response = App.getClient().getOkHttpClient().newCall(request).execute();
            ArrayList<Anime> a = JSONParse.parseJsonForList(new InputStreamReader(response.body().byteStream()));
            response.body().close();
            Log.d("Network: ", "Getting season list");
            String cr = "null";
            if (response.cacheResponse() != null) cr = response.cacheResponse().toString();
            Log.w("Cache response:", cr);
            Log.w("Network response:", response.networkResponse().toString());
            Collections.sort(a, new AnimeComparator(sort, asc));
            return a;
        } catch (Exception e) {
            Log.d("Request Season List: ", e.toString());
        }
        return null;
    }
    private static ArrayList<Anime> getStatusList(String status, int sort, int asc){
        try {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("www.anilist.co")
                    .addPathSegment("api")
                    .addPathSegment("browse")
                    .addPathSegment("anime")
                    .addQueryParameter("access_token", HttpClient.getAccessToken())
                    .addQueryParameter("status", status)
                    .addQueryParameter("airing_data", "true")
                    .addQueryParameter("full_page", "true")
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Cache-Control", "max-stale=3600000")
                    .tag("status")
                    .build();
            Response response = App.getClient().getOkHttpClient().newCall(request).execute();
            ArrayList<Anime> a = JSONParse.parseJsonForList(new InputStreamReader(response.body().byteStream()));
            response.body().close();
            Log.d("Network: ", "Getting status list");
            String cr = "null";
            if (response.cacheResponse() != null) cr = response.cacheResponse().toString();
            Log.w("Cache response:", cr);
            Log.w("Network response:", response.networkResponse().toString());
            Collections.sort(a, new AnimeComparator(sort, asc));
            return a;
        } catch (Exception e) {
            Log.d("Request Status List: ", e.toString());
        }
        return null;
    }
    private static Anime getAnimePage(String id){
        try {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("www.anilist.co")
                    .addPathSegment("api")
                    .addPathSegment("anime")
                    .addPathSegment(id)
                    .addPathSegment("page")
                    .addQueryParameter("access_token", HttpClient.getAccessToken())
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Cache-Control", "max-stale=3600000")
                    .tag("page")
                    .build();
            Response response = App.getClient().getOkHttpClient().newCall(request).execute();
            Anime a = JSONParse.parseJsonForAnimePage(new InputStreamReader(response.body().byteStream()));
            response.body().close();
            return a;
        } catch (Exception e){
            Log.d("Request Anime page: ", e.toString());
        }
        return null;
    }
    private static ArrayList<Anime> getSearchList(String query, int page){
        try {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("www.anilist.co")
                    .addPathSegment("api")
                    .addPathSegment("anime")
                    .addPathSegment("search")
                    .addPathSegment(query)
                    .addQueryParameter("access_token", HttpClient.getAccessToken())
                    .addQueryParameter("page", String.valueOf(page))
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Cache-Control", "max-stale=3600000")
                    .build();
            Response response = App.getClient().getOkHttpClient().newCall(request).execute();
            ArrayList<Anime> a = JSONParse.parseJsonForList(new InputStreamReader(response.body().byteStream()));
            response.body().close();
            Log.d("Network: ", "Getting search list");
            String cr = "null";
            if (response.cacheResponse() != null) cr = response.cacheResponse().toString();
            Log.w("Cache response:", cr);
            Log.w("Network response:", response.networkResponse().toString());
            return a;
        } catch (Exception e) {
            Log.d("Request search List: ", e.toString());
        }
        return null;
    }
    /*
     * Create AnimeList object to store in ListContent
     */
    public static AnimeList buildSeasonList(String season, int sort, int asc){
        if(checkSeason(season)){
            return ListContent.getList();
        }
        ArrayList<Anime> all = getSeasonList(season, sort, asc);
        if(all == null)return null;
        AnimeList result = new AnimeList();
        result.setAll(all);
        ArrayList<Anime> tv = new ArrayList<>();
        ArrayList<Anime> ova = new ArrayList<>();
        ArrayList<Anime> movie = new ArrayList<>();
        for(Anime a : all){
            if(a.getType().toLowerCase().equals("tv") || a.getType().toLowerCase().equals("tv short")){
                tv.add(a);
            } else if(a.getType().toLowerCase().equals("movie")){
                movie.add(a);
            } else if(a.getType().toLowerCase().equals("special") || a.getType().toLowerCase().equals("ova") || a.getType().toLowerCase().equals("ona")){
                ova.add(a);
            }
        }
        result.setTV(tv);
        result.setMovie(movie);
        result.setOVAONASpecial(ova);
        result.setSeason(season);
        //Calendar c = Calendar.getInstance();
        //int seconds = c.get(Calendar.SECOND);
        //TODO: Implement last updated for animelist
        //result.setLastUpdated();
        return result;
    }
    public static AnimeList buildStatusList(String status, int sort, int asc){
        ArrayList<Anime> all = getStatusList(status, sort, asc);
        if(all == null)return null;
        AnimeList result = new AnimeList();
        result.setAll(all);
        ArrayList<Anime> tv = new ArrayList<>();
        ArrayList<Anime> ova = new ArrayList<>();
        ArrayList<Anime> movie = new ArrayList<>();
        for(Anime a : all){
            if(a.getType().toLowerCase().equals("tv") || a.getType().toLowerCase().equals("tv short")){
                tv.add(a);
            } else if(a.getType().toLowerCase().equals("movie")){
                movie.add(a);
            } else if(a.getType().toLowerCase().equals("special") || a.getType().toLowerCase().equals("ova") || a.getType().toLowerCase().equals("ona")){
                ova.add(a);
            }
        }
        result.setTV(tv);
        result.setMovie(movie);
        result.setOVAONASpecial(ova);
        return result;
    }
    public static AnimeList buildScheduleList(String season, String status, int sort, int asc){
        ArrayList<Anime> all = getStatusList(status, sort, asc);
        ArrayList<Anime> seasonPart = getSeasonList(season, sort, asc);
        if(all == null || seasonPart == null)return null;
        for (Anime a : seasonPart) {
            if (a.getAiring_status() != null) {
                if (!all.contains(a) && (a.getAiring_status().equalsIgnoreCase("currently airing") || a.getAiring_status().equals("not yet aired"))) {
                    all.add(a);
                }
            }
        }
        AnimeList result = new AnimeList();
        result.setAll(all);
        ArrayList<Anime> tv = new ArrayList<>();
        ArrayList<Anime> ova = new ArrayList<>();
        ArrayList<Anime> movie = new ArrayList<>();
        for(Anime a : all){
            if(a.getType().toLowerCase().equals("tv") || a.getType().toLowerCase().equals("tv short")){
                tv.add(a);
            } else if(a.getType().toLowerCase().equals("movie")){
                movie.add(a);
            } else if(a.getType().toLowerCase().equals("special") || a.getType().toLowerCase().equals("ova") || a.getType().toLowerCase().equals("ona")){
                ova.add(a);
            }
        }
        result.setTV(tv);
        result.setMovie(movie);
        result.setOVAONASpecial(ova);
        return result;
    }
    public static AnimeList buildSearchList(String query, int page, int sort, int asc){
        if(checkQuery(query)) {
            ArrayList<Anime> r = getSearchList(query, page);
            if (r == null) return null;
            ArrayList<Anime> result = new ArrayList<>();
            result.addAll(ListContent.getList().getAll());
            result.addAll(r);
            Log.w("LOADING", "NEXT BATCH AT " + String.valueOf(page));
            AnimeList ret = new AnimeList();
            Collections.sort(result, new AnimeComparator(sort, asc));
            ret.setAll(result);
            return ret;
        } else {
            ArrayList<Anime> r = getSearchList(query, page);
            if (r == null) return null;
            ArrayList<Anime> result = new ArrayList<>();
            result.addAll(r);
            AnimeList ret = new AnimeList();
            Collections.sort(result, new AnimeComparator(sort, asc));
            ret.setAll(result);
            return ret;
        }
    }
    public static Anime buildAnimePage(String id){
        Anime x = getAnimePage(id);
        return x;
    }

    public static AnimeList reloadSeasonList(String season, int sort, int asc){
        ArrayList<Anime> all = getSeasonList(season, sort, asc);
        if(all == null)return null;
        AnimeList result = new AnimeList();
        result.setAll(all);
        ArrayList<Anime> tv = new ArrayList<>();
        ArrayList<Anime> ova = new ArrayList<>();
        ArrayList<Anime> movie = new ArrayList<>();
        for(Anime a : all){
            if(a.getType().toLowerCase().equals("tv") || a.getType().toLowerCase().equals("tv short")){
                tv.add(a);
            } else if(a.getType().toLowerCase().equals("movie")){
                movie.add(a);
            } else if(a.getType().toLowerCase().equals("special") || a.getType().toLowerCase().equals("ova") || a.getType().toLowerCase().equals("ona")){
                ova.add(a);
            }
        }
        result.setTV(tv);
        result.setMovie(movie);
        result.setOVAONASpecial(ova);
        result.setSeason(season);
        //Calendar c = Calendar.getInstance();
        //int seconds = c.get(Calendar.SECOND);
        //Implement lastupdated
        //result.setLastUpdated();
        return result;
    }
    public static AnimeList sortAnimeList(int sort, int ad, AnimeList a){
        ArrayList<Anime> all = new ArrayList<>();
        ArrayList<Anime> tv = new ArrayList<>();
        ArrayList<Anime> movie = new ArrayList<>();
        ArrayList<Anime> ova = new ArrayList<>();
        all.addAll(a.getAll());
        tv.addAll(a.getTV());
        movie.addAll(a.getMovie());
        ova.addAll(a.getOVAONASpecial());
        Collections.sort(all, new AnimeComparator(sort, ad));
        Collections.sort(tv, new AnimeComparator(sort, ad));
        Collections.sort(movie, new AnimeComparator(sort, ad));
        Collections.sort(ova, new AnimeComparator(sort, ad));
        AnimeList x = new AnimeList();
        x.setAll(all);
        x.setTV(tv);
        x.setMovie(movie);
        x.setOVAONASpecial(ova);
        return x;
    }
    /*
     * Check with ListContent if the season/query is the same. If so, then just return null
     */
    private static boolean checkSeason(String season){
        if(ListContent.getList().getSeason() == null)return false;
        Log.w("HI", ListContent.getList().getSeason().toLowerCase());
        return (season.equalsIgnoreCase(ListContent.getList().getSeason()));
    }
    private static boolean checkQuery(String query){
        if(ListContent.getList().getQuery() == null)return false;
        Log.w("QUERY STRING", query + " " + ListContent.getList().getQuery());
        return (query.equalsIgnoreCase(ListContent.getList().getQuery()));
    }
    private AnimeListBuilder(){}
}
