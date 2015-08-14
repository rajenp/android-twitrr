package com.rpatil.twitrr.adapters;

import com.rpatil.twitrr.ui.activities.MainUIActivity;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

public class TwitterResponseAdapter extends TwitterAdapter {

    private final MainUIActivity activity;

    public TwitterResponseAdapter(MainUIActivity activity) {
        this.activity = activity;
    }

    @Override
    public void gotHomeTimeline(ResponseList<Status> statuses) {
        activity.gotHomeTimeLine(statuses);
    }

    @Override
    public void updatedStatus(Status status) {
        activity.updatedStatus(status);
    }

    @Override
    public void gotDirectMessages(ResponseList<DirectMessage> messages) {
        activity.gotDirectMessages(messages);
    }

    @Override
    public void sentDirectMessage(DirectMessage message) {
        activity.sentDirectMessage(message);
    }

    @Override
    public void gotMentions(ResponseList<Status> statuses) {
        activity.gotMentions(statuses);
    }

    @Override
    public void onException(TwitterException te, TwitterMethod method) {
        super.onException(te, method);
        te.printStackTrace();
    }
}
