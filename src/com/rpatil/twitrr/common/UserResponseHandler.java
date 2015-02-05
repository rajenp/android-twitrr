package com.rpatil.twitrr.common;

import twitter4j.User;

/**
 *
 */
public interface UserResponseHandler {

    public void onUserDetail(User user);
}
