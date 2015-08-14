package com.rpatil.twitrr.ui;

import com.rpatil.twitrr.ui.activities.AuthActivity;

import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class AuthTwitterAdapter extends TwitterAdapter {

    private final AuthActivity authActivity;

    public AuthTwitterAdapter(AuthActivity authActivity) {
        this.authActivity = authActivity;
    }

    @Override
    public void gotOAuthAccessToken(AccessToken token) {
        authActivity.onOAuthAccessToken(token);
    }

    @Override
    public void gotOAuthRequestToken(RequestToken token) {
        authActivity.onOAuthRequestToken(token);
    }

    @Override
    public void gotUserDetail(User user) {
        authActivity.onUserDetail(user);
    }

    @Override
    public void onException(TwitterException te, TwitterMethod method) {
        super.onException(te, method);
        te.printStackTrace();
    }


}
