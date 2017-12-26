package com.yruili.animelist.Network;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ca.mimic.oauth2library.OAuth2Client;
import ca.mimic.oauth2library.OAuthResponse;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * HTTP Client for querying data from Anilist api
 *
 * TODO: ENCRYPT CLIENT_ID AND CLIENT_SECRET AND ENCRYPT THE STORAGE FILE
 *
 * Created by rui on 15/07/17.
 */
public class HttpClient {
    private OkHttpClient client;
    private OAuth2Client cl;
    public static final String CLIENT_ID = "cynicalpillow-w9hsv";
    public static final String CLIENT_SECRET = "C22DO04rTcSy6P2xvLNNZB0";
    private static String ACCESS_TOKEN;
    private static String REFRESH_TOKEN;
    private Context context;
    //public static final String API_PREFIX = "https://anilist.co/api/";

    public HttpClient(Context co){
        context = co;
        client = new OkHttpClient.Builder()
                .authenticator(new Authenticator() {
                    @Override public Request authenticate(Route route, Response response) throws IOException {
                        if (responseCount(response) >= 3) {
                            return null;
                        }
                        if(REFRESH_TOKEN != null){
                            OAuthResponse resp = cl.refreshAccessToken(REFRESH_TOKEN);
                            if (resp.isSuccessful()) {
                                ACCESS_TOKEN = resp.getAccessToken();
                                Log.w("REFRESHING TOKEN", REFRESH_TOKEN + " || " + ACCESS_TOKEN);
                                client.cache().evictAll();
                                try {
                                    writeStoredAccess();
                                } catch (Exception e) {
                                }
                            }
                        } else {
                            OAuthResponse resp = cl.requestAccessToken();
                            if (resp.isSuccessful()) {
                                ACCESS_TOKEN = resp.getAccessToken();
                                client.cache().evictAll();
                                try {
                                    writeStoredAccess();
                                } catch (Exception e) {
                                }
                            }
                        }
                        Log.w("HI", "HI");
                        HttpUrl mod = response.request().url().newBuilder().setQueryParameter("access_token", ACCESS_TOKEN).build();
                        return response.request().newBuilder()
                                .url(mod)
                                .build();
                    }
                }).retryOnConnectionFailure(false).connectTimeout(2, TimeUnit.SECONDS).cache(new Cache(new File(context.getCacheDir(), "HttpResponseCache"), (long)(100 * 1024 * 1024))).build();
        Log.w("RETRY POSSIBLE?", String.valueOf(client.retryOnConnectionFailure()));
        String x = "";
        try {
            x = getStoredRefresh();
        } catch (Exception e){
            Log.w("ACCESS TOKEN ERROR", e.toString());
        }
        if(!x.equals("")){
            Log.w("LOGGED IN", "HI");
            REFRESH_TOKEN = x;
            cl = new OAuth2Client.Builder(CLIENT_ID, CLIENT_SECRET, "https://anilist.co/api/auth/access_token").grantType("refresh_token").okHttpClient(client).build();
            String c = "";
            try {
                 c = getStoredAccess();
            } catch (Exception e){}
            if (c.equals("")) {
                try {
                    OAuthResponse resp = cl.refreshAccessToken(REFRESH_TOKEN);
                    Log.w("REFRESHING ACCESS TOKEN", ACCESS_TOKEN + " || " + REFRESH_TOKEN);
                    if (resp.isSuccessful()) {
                        ACCESS_TOKEN = resp.getAccessToken();
                        writeStoredAccess();
                    }
                } catch (Exception e){}
            } else {
                ACCESS_TOKEN = c;
            }
        } else {
            Log.w("NOT LOGGED IN", "HI");
            OAuth2Client.Builder builder = new OAuth2Client.Builder(CLIENT_ID, CLIENT_SECRET, "https://anilist.co/api/auth/access_token").grantType("client_credentials").okHttpClient(client);
            cl = builder.build();
            String c = "";
            try {
                c = getStoredAccess();
            } catch (Exception e){}
            if (c.equals("")) {
                try {
                    OAuthResponse resp = cl.requestAccessToken();
                    Log.w("REQUESTING", ACCESS_TOKEN);
                    if (resp.isSuccessful()) {
                        ACCESS_TOKEN = resp.getAccessToken();
                        writeStoredAccess();
                    }
                } catch (Exception e){}
            } else {
                ACCESS_TOKEN = c;
            }
        }
    }

    private String getStoredRefresh() throws  Exception {
        FileInputStream fin = context.openFileInput("REFRESH");
        int c;
        String code = "";
        while( (c = fin.read()) != -1){
            String x = Character.toString((char)c);
            if(x != null)code += x;
        }
        //string code contains all the data of the file.
        fin.close();
        Log.w("Get refresh token: ", code);
        return code;
    }
    public void writeRefreshToken() throws Exception{
        FileOutputStream fout = context.openFileOutput("REFRESH", Context.MODE_PRIVATE);
        fout.write(REFRESH_TOKEN.getBytes());
        fout.close();
        Log.w("Wrote refresh token: ", REFRESH_TOKEN);
    }
    private String getStoredAccess() throws Exception{
        FileInputStream fin = context.openFileInput("ACCESS");
        int c;
        String code = "";
        while( (c = fin.read()) != -1){
            String x = Character.toString((char)c);
            if(x != null)code += x;
        }
        //string code contains all the data of the file.
        fin.close();
        Log.w("Get access token: ", code);
        return code;
    }
    private void writeStoredAccess() throws Exception{
        FileOutputStream fout = context.openFileOutput("ACCESS", Context.MODE_PRIVATE);
        fout.write(ACCESS_TOKEN.getBytes());
        fout.close();
        Log.w("Wrote access token: ", ACCESS_TOKEN);
    }
    public void deleteRefreshToken() throws Exception{
        FileOutputStream fout = context.openFileOutput("REFRESH", Context.MODE_PRIVATE);
        fout.write("".getBytes());
        fout.close();
    }
    private HttpClient(){}
    public OkHttpClient getOkHttpClient(){
        return client;
    }
    public OAuth2Client getOAuthClient(){
        return cl;
    }
    public void setOAuthClient(OAuth2Client cl){
        this.cl = cl;
    }
    public static String getAccessToken(){
        return ACCESS_TOKEN;
    }
    public static void setAccessToken(String token){
        ACCESS_TOKEN = token;
    }
    public static String getClientId(){
        return CLIENT_ID;
    }
    public static void setRefreshToken(String refreshToken) {
        REFRESH_TOKEN = refreshToken;
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
