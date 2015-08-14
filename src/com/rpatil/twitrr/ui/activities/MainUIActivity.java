package com.rpatil.twitrr.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rpatil.twitrr.Constants;
import com.rpatil.twitrr.R;
import com.rpatil.twitrr.adapters.TweetViewListAdapter;
import com.rpatil.twitrr.adapters.TwitterResponseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.AsyncTwitter;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;

public class MainUIActivity extends Activity implements ActionBar.TabListener {

    public static final int TAB_HOME = 0;
    public static final int TAB_MENTIONS = 1;
    public static final int TAB_MESSAGES = 2;
    public static final int TAB_COMPOSE = 3;

    private int listViewIndex;
    private int composeViewIndex;
    private int detailedViewIndex;

    private List<Status> tweets = new ArrayList<Status>();
    private List<Status> mentions = new ArrayList<Status>();
    private List<DirectMessage> messages = new ArrayList<DirectMessage>();

    private Object currentTweet; //Status or DM

    private AsyncTwitter twitter;
    private int currentTabIndex = 0;

    private ViewFlipper flipper;

    private Menu mMenu = null;

    private ActionBar mActionBar = null;

    private void initViewFlipperIndexes() {
        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        listViewIndex = flipper.indexOfChild(findViewById(R.id.listView));
        composeViewIndex = flipper.indexOfChild(findViewById(R.id.composeView));
        detailedViewIndex = flipper.indexOfChild(findViewById(R.id.detailedView));
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getActionBar();
        if (mActionBar == null) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            mActionBar = getActionBar();
        }

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        setContentView(R.layout.main);
        setTitle(Constants.MY_APP_NAME);

        initViewFlipperIndexes();

        /*flipper.setInAnimation(this, R.anim.push_left_in);
        flipper.setOutAnimation(this, R.anim.push_left_out);*/

        final EditText tweetComposeEdit = (EditText) findViewById(R.id.tweetComposeEdit);
        tweetComposeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                TextView textCount = (TextView) findViewById(R.id.textCount);
                int charsLeft = 140 - tweetComposeEdit.getText().toString().length();
                Button tweetButton = (Button) findViewById(R.id.sentTweetButton);
                tweetButton.setEnabled(charsLeft >= 0);
                textCount.setText((charsLeft) + "");
            }
        });

        Button tweetButton = (Button) findViewById(R.id.sentTweetButton);
        tweetButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View button) {
                hideKeyboard(tweetComposeEdit);
                if (currentTweet != null && currentTweet instanceof Status) {
                    Status status = (Status) currentTweet;
                    StatusUpdate statusUpdate = new StatusUpdate(tweetComposeEdit.getText().toString());
                    statusUpdate.setInReplyToStatusId(status.getId());
                    twitter.updateStatus(statusUpdate);
                } else {
                    twitter.updateStatus(tweetComposeEdit.getText().toString());
                }
                //Toast.makeText(getApplicationContext(), "Sending tweet...", Toast.LENGTH_SHORT).show();
                //reloadList();
            }
        });

        Button dvCloseButton = (Button) findViewById(R.id.dvCloseButton);
        dvCloseButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View button) {
                currentTweet = null; //reset current tweet
                flipper.setDisplayedChild(listViewIndex);
            }
        });

        Button dvReplyButton = (Button) findViewById(R.id.dvReplyButton);
        dvReplyButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View button) {
                mActionBar.selectTab(mActionBar.getTabAt(TAB_COMPOSE));
            }
        });

        final ListView listView = (ListView) findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long myLong) {
                flipper.setDisplayedChild(detailedViewIndex);
                TweetViewListAdapter.TweetRowViewHolder viewHolder = (TweetViewListAdapter.TweetRowViewHolder) myView.getTag();
                currentTweet = viewHolder.getData();
                fillDetailView(viewHolder);
            }
        });

        twitter = AuthActivity.getTwitterInstance(getApplicationContext().getSharedPreferences(Constants.MY_APP_NAME, MODE_PRIVATE));
        twitter.addListener(new TwitterResponseAdapter(this));

        ActionBar.Tab tab = mActionBar.newTab();
        tab.setText(R.string.timeline);
        tab.setTabListener(this);
        mActionBar.addTab(tab);

        tab = mActionBar.newTab();
        tab.setText(R.string.mentions);
        tab.setTabListener(this);
        loadFromServer(1, false);
        mActionBar.addTab(tab);

        tab = mActionBar.newTab();
        tab.setText(R.string.messages);
        tab.setTabListener(this);
        loadFromServer(2, false);
        mActionBar.addTab(tab);

        tab = mActionBar.newTab();
        tab.setText(R.string.compose);
        tab.setTabListener(this);
        mActionBar.addTab(tab);

    }


    private void fillDetailView(TweetViewListAdapter.TweetRowViewHolder viewHolder) {

        TextView userName2 = (TextView) findViewById(R.id.dvUserNameView);
        TextView tweetText2 = (TextView) findViewById(R.id.dvTweetTextView);
        ImageView image2 = (ImageView) findViewById(R.id.dvImageView);
        TextView timeView2 = (TextView) findViewById(R.id.dvTimeView);

        userName2.setText(Html.fromHtml(viewHolder.getUserHtmlText()));
        tweetText2.setText(viewHolder.getTweetText().getText());
        image2.setImageDrawable(viewHolder.getImage().getDrawable());
        timeView2.setText(viewHolder.getTimeText().getText());

    }

    @Override
    public void onBackPressed() {
        if (flipper.getDisplayedChild() == composeViewIndex) {
            mActionBar.selectTab(mActionBar.getTabAt(this.currentTabIndex));
        } else if (this.currentTweet != null) {
            this.currentTweet = null;
            flipper.setDisplayedChild(listViewIndex);
        } else {
            super.onBackPressed();
        }
    }

    private void loadFromServer(final int type, final boolean force) {
        List list;
        switch (type) {
            case TAB_HOME:
                list = tweets;
                if (force || list == null || list.size() < 1) {
                    showProgressBar(true);
                    Paging paging = new Paging();
                    paging.setCount(20);
                    if (list != null && list.size() > 0) {
                        Status status = (Status) list.get(0);
                        paging.setSinceId(status.getId());
                    }
                    twitter.getHomeTimeline(paging);
                } else {
                    reloadList();
                }
                break;
            case TAB_MENTIONS:
                list = mentions;
                if (force || list == null || list.size() < 1) {
                    showProgressBar(true);
                    twitter.getMentions();
                } else {
                    reloadList();
                }
                break;
            case TAB_MESSAGES:
                list = messages;
                if (force || list == null || list.size() < 1) {
                    showProgressBar(true);
                    twitter.getDirectMessages();
                } else {
                    reloadList();
                }
                break;
        }
    }

    public void hideKeyboard(EditText editText) {

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void showKeyboard(final EditText editText) {
        editText.post(new Runnable() {
            public void run() {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, MODE_PRIVATE);
            }
        });
    }

    public void showProgressBar(boolean show) {
        if (mMenu == null) return;

        MenuItem item = mMenu.findItem(R.id.reloadMenu);

        if (show) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View abprogress = inflater.inflate(R.layout.progressbar, null);
            item.setActionView(abprogress);
        } else {
            item.setActionView(null);
        }
    }

    private void reloadList() {
        ListView listView = (ListView) findViewById(R.id.listView);
        flipper.setDisplayedChild(listViewIndex);
        ArrayAdapter arrayAdapter = (ArrayAdapter) listView.getAdapter();
        List<Object> list = new ArrayList<Object>();
        switch (this.currentTabIndex) {
            case TAB_MENTIONS:
                list.addAll(mentions);
                break;
            case TAB_MESSAGES:
                list.addAll(messages);
                break;
            case TAB_HOME:
                list.addAll(tweets);
                break;
        }
        if (arrayAdapter == null) {
            listView.setAdapter(new TweetViewListAdapter(this, list));
        } else {
            arrayAdapter.clear();
            arrayAdapter.addAll(list);
            arrayAdapter.notifyDataSetChanged();
        }
        showProgressBar(false);
    }

    private String extractUsersFrom(String screenName, String text) {
        StringBuilder builder = new StringBuilder("@" + screenName + " ");
        Matcher m = Pattern.compile("@(\\w){1,15}")
                .matcher(text);
        while (m.find()) {
            String match = m.group();
            if (builder.indexOf(match) < 0) {
                builder.append(match).append(" ");
            }
        }
        return builder.toString();
    }

    private void showCompose(EditText tweetText) {
        tweetText.setText("");
        if (this.currentTweet != null) {
            if (this.currentTweet instanceof Status) {
                Status inReplyTo = (Status) this.currentTweet;
                String text = inReplyTo.getText();
                tweetText.setText(extractUsersFrom(inReplyTo.getUser().getScreenName(), text));
            } else if (this.currentTweet instanceof DirectMessage) {
                DirectMessage inReplyTo = (DirectMessage) this.currentTweet;
                String text = inReplyTo.getText();
                tweetText.setText(extractUsersFrom(inReplyTo.getSenderScreenName(), text));
            }
        }
        flipper.setDisplayedChild(composeViewIndex);
        showKeyboard(tweetText);
        tweetText.setSelection(0, tweetText.getText().length());
    }

    @Override
    public void onTabSelected(final ActionBar.Tab tab, FragmentTransaction ft) {
        if (Constants.isLoggable()) {
            Log.d(Constants.MY_APP_NAME, "View: " + flipper.getDisplayedChild());
        }
        EditText tweetText = (EditText) findViewById(R.id.tweetComposeEdit);
        if (tab.getPosition() == TAB_COMPOSE) {
            this.showCompose(tweetText);
            return;
        } else {
            this.currentTweet = null;
            hideKeyboard(tweetText);
            flipper.setDisplayedChild(listViewIndex);
        }
        this.currentTabIndex = tab.getPosition();
        loadFromServer(tab.getPosition(), false);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (tab.getPosition() == TAB_COMPOSE) {
            return;
        }
        loadFromServer(tab.getPosition(), true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EditText tweetText = (EditText) findViewById(R.id.tweetComposeEdit);
        hideKeyboard(tweetText);
        if (Constants.isLoggable()) {
            Log.d(Constants.MY_APP_NAME, "Destroyed");
        }
    }

  /*@Override
  public void onTweetsResponse(List list) {
    this.reloadList();
  }*/

    public void gotHomeTimeLine(ResponseList<Status> statuses) {
        if (statuses.size() > 0) {
            int i = 0;
            for (Status status : statuses) {
                tweets.add(i++, status);
                if (tweets.size() > 20) {
                    tweets.remove(tweets.size());
                }
            }
            tweets = statuses;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    reloadList();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressBar(false);
                }
            });
        }

    }

    public void updatedStatus(Status status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActionBar.selectTab(mActionBar.getTabAt(TAB_HOME));
            }
        });
    }

    public void gotDirectMessages(ResponseList<DirectMessage> messages) {
        this.messages = messages;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reloadList();
            }
        });
    }

    public void gotMentions(ResponseList<Status> statuses) {
        mentions = statuses;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reloadList();
            }
        });
    }

    public void sentDirectMessage(DirectMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActionBar.selectTab(mActionBar.getTabAt(TAB_HOME));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_actions, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Event Handling for Individual main_activity_actions item selected
     * Identify single main_activity_actions item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.composeMenu:
                mActionBar.selectTab(mActionBar.getTabAt(TAB_COMPOSE));
            case R.id.logoutMenu:
                getSharedPreferences(Constants.MY_APP_NAME, 0).edit().clear().commit();
                return true;
            case R.id.reloadMenu:
                loadFromServer(this.currentTabIndex, true);
                return true;
        }
        return false;
    }

}

