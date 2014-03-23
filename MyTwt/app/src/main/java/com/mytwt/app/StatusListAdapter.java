package com.mytwt.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import twitter4j.Status;
import twitter4j.User;

public class StatusListAdapter extends ArrayAdapter<Status> {
    private LayoutInflater inflater;

    //declear row components
    private View row;
    private ImageView avatarView;
    private TextView usrName;
    private TextView screenName;
    private TextView statusText;

    public StatusListAdapter(Activity activity, List<Status> statii) {
        super(activity, android.R.layout.simple_list_item_1, statii);
        inflater = activity.getWindow().getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        row = inflater.inflate(R.layout.status_list_item, parent, false);
        findViews();
        Status status = (Status) getItem(position);
        setStatus(status);
        return row;
    }

    private void findViews() {
        usrName = (TextView) row.findViewById(R.id.status_user_name_text);
        screenName = (TextView) row.findViewById(R.id.status_usr_screenname);
        statusText = (TextView) row.findViewById(R.id.status_text);
        avatarView = (ImageView) row.findViewById(R.id.user_avatar);
    }

    public void setStatus(Status status) {
        final User user = status.getUser();
        user.getProfileImageURL();
        usrName.setText(user.getName());
        screenName.setText(" @"+user.getScreenName());
        statusText.setText(status.getText());
        LoadImageAsyncTask loadImageAsyncTask = new LoadImageAsyncTask(avatarView);
        loadImageAsyncTask.execute(user.getProfileImageURL());
    }

    public void appendNewer(List<Status> statii) {
        for (Status status : statii) {
            insert(status, 0);
        }
    }

    public void appendOlder(List<Status> statii) {
        for (Status status : statii) {
            add(status);
        }
    }

    public long getFirstId() {
        Status firstStatus = getItem(0);
        if (null == firstStatus) {
            return 0;
        } else {
            return firstStatus.getId();
        }
    }

    public long getLastId() {
        Status lastStatus = getItem(getCount()-1);
        if (null == lastStatus) {
            return 0;
        } else {
            return lastStatus.getId();
        }
    }
}
