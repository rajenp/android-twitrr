package com.rpatil.twitrr.oauth;

import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * OAuth result handler
 */
public interface OAuthResultHandler {

    public void onOAuthRequestToken(RequestToken requestToken);

    public void onOAuthAccessToken(AccessToken accessToken);
}
