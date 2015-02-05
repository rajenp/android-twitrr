package com.rpatil.twitrr.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.rpatil.twitrr.APIKeys;
import com.rpatil.twitrr.Constants;
import com.rpatil.twitrr.adapters.AuthTwitterAdapter;
import com.rpatil.twitrr.common.UserResponseHandler;
import com.rpatil.twitrr.oauth.OAuthResultHandler;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Rajendra Patil
 */
public class AuthActivity extends Activity implements OAuthResultHandler, UserResponseHandler {

    private AsyncTwitter twitter;

    private SharedPreferences sharedPreferences;

    /**
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.MY_APP_NAME, MODE_PRIVATE);
        twitter = AuthActivity.getTwitterInstance(null);
        twitter.addListener(new AuthTwitterAdapter(this));
        if (this.isAuthenticated()) {

            this.goToTimeLineActivity();

        } else {

            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(Constants.TWITTER_CALLBACK_URL)) {
                Log.d(Constants.MY_APP_NAME, "Received OAuth Callback" + getIntent());

                String token = sharedPreferences.getString(Constants.PREF_KEY_REQ_TOKEN, "");
                String secret = sharedPreferences.getString(Constants.PREF_KEY_REQ_TOKEN_SEC, "");
                RequestToken requestToken = new RequestToken(token, secret);
                String verifier = uri.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);
                twitter.getOAuthAccessTokenAsync(requestToken, verifier);
            } else {
                this.doAuthentication();
            }
        }
    }

    public static AsyncTwitter getTwitterInstance(SharedPreferences sharedPreferences) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(APIKeys.TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(APIKeys.TWITTER_CONSUMER_SECRET);
        //builder.setUseSSL(true);
        Configuration configuration = builder.build();
        AsyncTwitterFactory factory = new AsyncTwitterFactory(configuration);

        if (sharedPreferences != null) {
            String token = sharedPreferences.getString(Constants.PREF_KEY_OAUTH_TOKEN, "");
            String secret = sharedPreferences.getString(Constants.PREF_KEY_OAUTH_SECRET, "");
            //Log.d(Constants.MY_APP_NAME, "AuthTokens: " + "<" + token + " : " + secret + ">");
            if (!token.equals("") && !secret.equals("")) {
                AccessToken accessToken = new AccessToken(token, secret);
                return factory.getInstance(accessToken);
            }
        }
        return factory.getInstance();
    }

    private void doAuthentication() {

        twitter.addListener(new TwitterAdapter() {
            @Override
            public void gotOAuthRequestToken(RequestToken token) {
                onOAuthRequestToken(token);
            }
        });
        twitter.getOAuthRequestTokenAsync(Constants.TWITTER_CALLBACK_URL);
    }

    //Launch TimeLine activity
    public void goToTimeLineActivity() {
        Log.d(Constants.MY_APP_NAME, "Launching TimeLine activity");
        Intent intent = new Intent(this, MainUIActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean isAuthenticated() {
        String userName = sharedPreferences.getString(Constants.PREF_KEY_TWITTER_LOGIN, null);
        boolean ret = userName != null;
        Log.d(Constants.MY_APP_NAME, "isAuthenticated: " + ret);
        return ret;
    }

    @Override
    public void onOAuthRequestToken(RequestToken requestToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_KEY_REQ_TOKEN, requestToken.getToken());
        editor.putString(Constants.PREF_KEY_REQ_TOKEN_SEC, requestToken.getTokenSecret());
        editor.commit();

        Log.d(Constants.MY_APP_NAME, "Request Token: " + requestToken);

        //Launch browser and proceed with authorization
        Intent authIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
        authIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(authIntent);
    }

    @Override
    public void onOAuthAccessToken(AccessToken accessToken) {
        // Shared Preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // After getting access token, access token secret
        // store them in application preferences
        editor.putString(Constants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
        editor.putString(Constants.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
        editor.commit();
        Log.d(Constants.MY_APP_NAME, "Twitter OAuth Success" + "> " + accessToken.getToken());

        long userID = accessToken.getUserId();
        twitter.showUser(userID);
    }

    @Override
    public void onUserDetail(User user) {
        if (user == null) {
            finish();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREF_KEY_TWITTER_LOGIN, user.getName());
        goToTimeLineActivity();
        editor.commit();
    }
}





