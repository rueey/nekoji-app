package com.yruili.animelist.Network;

import android.util.Log;

import com.yruili.animelist.App;

import java.util.HashMap;
import java.util.Map;

import ca.mimic.oauth2library.OAuth2Client;
import ca.mimic.oauth2library.OAuthResponse;

/**
 * Created by rui on 03/09/17.
 */

public class LoginUtil {
    public static boolean login(String code){
        Map<String, String> params = new HashMap<>();
        params.put("redirect_uri", "https://www.google.com/ANIME_CHART_LOGIN_REDIRECT");
        params.put("code", code);
        OAuth2Client client = new OAuth2Client.Builder(App.getClient().CLIENT_ID, App.getClient().CLIENT_SECRET, "https://anilist.co/api/auth/access_token").grantType("authorization_code").parameters(params).build();
        try {
            OAuthResponse resp = client.requestAccessToken();
            if (resp.isSuccessful()) {
                Log.w("SUCCESSFUL LOGIN", resp.getBody());
                Log.w("GETTING TOKENS", resp.getAccessToken() + " " + resp.getRefreshToken());
                HttpClient.setAccessToken(resp.getAccessToken());
                HttpClient.setRefreshToken(resp.getRefreshToken());
                App.getClient().setOAuthClient(new OAuth2Client.Builder(App.getClient().CLIENT_ID, App.getClient().CLIENT_SECRET, "https://anilist.co/api/auth/access_token").grantType("refresh_token").okHttpClient(App.getClient().getOkHttpClient()).build());
                App.getClient().writeRefreshToken();
                return true;
            } else {
                Log.w("UNSUCCESSFUL LOGIN", String.valueOf(resp.getHttpResponse().request().url()));
            }
        } catch (Exception e){
            Log.w("ERROR LOGGING IN", e.toString());
        }
        return false;
    }
    public static boolean logout(){
        try {
            Log.w("LOGGING OUT", "DELETING REFRESH TOKEN");
            App.getClient().setOAuthClient(new OAuth2Client.Builder(HttpClient.CLIENT_ID, HttpClient.CLIENT_SECRET, "https://anilist.co/api/auth/access_token").grantType("client_credentials").okHttpClient(App.getClient().getOkHttpClient()).build());
            App.getClient().deleteRefreshToken();
            return true;
        } catch (Exception e){}
        return false;
    }
}
