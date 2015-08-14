package com.rpatil.twitrr.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.rpatil.twitrr.R;

import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;

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
        private AQuery aQuery;

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

        public AQuery getaQuery() {
            return aQuery;
        }

        public void setaQuery(AQuery aQuery) {
            this.aQuery = aQuery;
        }

        public String getUserHtmlText() {
            User user = null;
            if (data instanceof Status) {
                user = ((Status) data).getUser();
            } else if (data instanceof DirectMessage) {
                user = ((DirectMessage) data).getSender();
            }
            return user != null ? String.format("<h3>%s</h3><p><small>%s</small></p>", user.getName(), user.getDescription()) : "Unknown";
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
        AQuery aq;
        TweetRowViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row, parent, false);

            viewHolder = new TweetRowViewHolder();
            aq = new AQuery(convertView);
            viewHolder.setaQuery(aq);
            viewHolder.setUserName((TextView) convertView.findViewById(R.id.userNameView));
            viewHolder.setTweetText((TextView) convertView.findViewById(R.id.tweetTextView));
            viewHolder.setTimeText((TextView) convertView.findViewById(R.id.timeView));
            viewHolder.setImage((ImageView) convertView.findViewById(R.id.imageView));
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (TweetRowViewHolder) convertView.getTag();
            aq = viewHolder.getaQuery();
        }

        viewHolder.setData(object);

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
        aq.id(viewHolder.getImage()).image(profileImageUrl, true, false);

        return convertView;
    }

    private String timeToString(long time) {
        return DateUtils.getRelativeTimeSpanString(context, time).toString();
    }

}

