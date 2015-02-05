package com.rpatil.twitrr.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.rpatil.twitrr.R;
import twitter4j.DirectMessage;
import twitter4j.Status;

import java.util.Date;
import java.util.List;

/**
 * @author Rajendra Patil
 */
public class TweetViewListAdapter extends ArrayAdapter<Object> {

    public static class TweetRowViewHolder {

        private TextView userName;
        private TextView timeText;
        private ImageView image;
        private TextView tweetText;
        private Object data;

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public TextView getUserName() {
            return userName;
        }

        public void setUserName(TextView userName) {
            this.userName = userName;
        }

        public TextView getTimeText() {
            return timeText;
        }

        public void setTimeText(TextView timeText) {
            this.timeText = timeText;
        }

        public ImageView getImage() {
            return image;
        }

        public void setImage(ImageView image) {
            this.image = image;
        }

        public TextView getTweetText() {
            return tweetText;
        }

        public void setTweetText(TextView tweetText) {
            this.tweetText = tweetText;
        }
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    private final Context context;
    private final List<Object> values;

    public TweetViewListAdapter(Context context, List<Object> values) {
        super(context, R.layout.row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        Object object = values.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row, parent, false);

            TweetRowViewHolder viewHolder = new TweetRowViewHolder();

            viewHolder.setUserName((TextView) convertView.findViewById(R.id.userNameView));
            viewHolder.setTweetText((TextView) convertView.findViewById(R.id.tweetTextView));
            viewHolder.setTimeText((TextView) convertView.findViewById(R.id.timeView));
            viewHolder.setImage((ImageView) convertView.findViewById(R.id.imageView));
            viewHolder.setData(object);
            convertView.setTag(viewHolder);
        }

        TweetRowViewHolder viewHolder = (TweetRowViewHolder) convertView.getTag();

        String userName = "", timeText = "", tweetText = "", profileImageUrl = null;

        if (object instanceof Status) {
            Status status = (Status) object;
            userName = status.getUser().getScreenName();
            tweetText = status.getText();
            timeText = timeToString(status.getCreatedAt().getTime());
            profileImageUrl = status.getUser().getProfileImageURL();

        } else if (object instanceof DirectMessage) {
            DirectMessage directMessage = (DirectMessage) object;
            userName = directMessage.getSenderScreenName();
            tweetText = directMessage.getText();
            timeText = timeToString(directMessage.getCreatedAt().getTime());
            profileImageUrl = directMessage.getSender().getProfileImageURL();
        }
        viewHolder.getUserName().setText(userName);
        viewHolder.getTweetText().setText(tweetText);
        viewHolder.getTimeText().setText(timeText);

        AQuery aq = new AQuery(convertView);
        aq.id(viewHolder.getImage()).image(profileImageUrl, true, false);

        return convertView;
    }

    private String timeToString(long time) {
        //return DateUtils.getRelativeTimeSpanString(context, time).toString();
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = new Date().getTime();
        if (time > now || time <= 0) {
            return null;
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a min ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " mins ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

}

