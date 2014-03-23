package com.mytwt.app;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URL;
import java.util.List;
import java.util.zip.Inflater;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MainActivity extends ListActivity implements LoadMoreAsyncTask.LoadMoreStatusesResponder {

    private StatusListAdapter adapter;
    private User user;
    private View headerView;
    private View footerView;
    public static Twitter twitter;
    public static AccessToken acsTkn;
    private final int LOAD_NEWER = 1;
    private final int LOAD_TIMELINE = 0;
    private final int LOAD_OLDER = -1;

    //for oauth
    protected static final String AUTHENTICATION_URL_KEY = "AUTHENTICATION_URL_KEY";
    protected static final int LOGIN_TO_TWITTER_REQUEST= 0;
    private SharedPreferences prefs;
    private static final String APPLICATION_PREFERENCES = "MyTwt";
    private static final String AUTH_KEY = "auth_key";
    private static final String AUTH_SEKRET_KEY = "auth_secret_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TestURL","enter oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set post button
        setPostButton();

        //login to twitter, called when the application is initialized for the first time
        if(twitter == null){
            configureTwitter();
        }

        if (twitter == null) {
            loginToTwitter();
        } else {
            loadTimeline();
        }
    }

    private void loadTimeline(){
        LoadTimelineAsyncTast loadtimelineTask = new LoadTimelineAsyncTast();
        loadtimelineTask.execute();
    }

    private class LoadTimelineAsyncTast extends AsyncTask<Void, Void, List<Status>> {

        @Override
        protected List<twitter4j.Status> doInBackground(Void... voids) {
            List<twitter4j.Status> statii = null;
            try {
                long userID = acsTkn.getUserId();
                user = twitter.showUser(userID);
                statii = twitter.getHomeTimeline();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return statii;
        }

        protected void onPostExecute(List<twitter4j.Status> result) {
            setTimeLine(result);
        }

    }
    private void setTimeLine(List<twitter4j.Status> statii){

        //set usr name
        String username = user.getName();
        TextView usrName = (TextView) findViewById(R.id.usrName);
        usrName.setText(username + "'s home");

        //get homeLine
        Log.d("TestURL","got statii");
        adapter = new StatusListAdapter(MainActivity.this, statii);
        Log.d("TestURL","adapter init finished");

        // set headerView & footerView
        headerView = getWindow().getLayoutInflater().inflate(R.layout.load_more, null);
        getListView().addHeaderView(headerView);
        footerView = getWindow().getLayoutInflater().inflate(R.layout.load_more, null);
        getListView().addFooterView(footerView);

        //set listAdapter
        setListAdapter(adapter);
        getListView().setSelection(1);


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (v.equals(headerView)) {
            setTextToLoading(headerView, true);
            new LoadMoreAsyncTask(this, twitter, adapter.getFirstId(), LOAD_NEWER).execute();
        } else if (v.equals(footerView)) {
            setTextToLoading(footerView,true);
            new LoadMoreAsyncTask(this, twitter, adapter.getLastId()-1, LOAD_OLDER).execute();
        } else {
            // item detail page
        }
    }

    //set header, footer text
    private void setTextToLoading(View view, boolean flag){
        TextView loadMoreText = (TextView) view.findViewById(R.id.load_more_text);
        if (flag == true){
            loadMoreText.setText(getString(R.string.loading));
        }else{
            loadMoreText.setText(getString(R.string.load_more));
        }
    }

    public void statusesLoaded(LoadMoreAsyncTask.LoadMoreStatusesResult result) {
        switch (result.loadType){
            case LOAD_TIMELINE:
                break;
            case LOAD_NEWER:
                adapter.appendNewer(result.statuses);
                setTextToLoading(headerView, false);
                break;
            case LOAD_OLDER:
                adapter.appendOlder(result.statuses);
                setTextToLoading(footerView,false);
                break;
            default:
                break;
        }
        getListView().setSelection(1);
    }

    private void setPostButton(){
        ImageView imageView = (ImageView) findViewById(R.id.post_button);
        imageView.setImageResource(R.drawable.post_twitter);
        findViewById(R.id.post_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, PostActivity.class);
                        startActivity(intent);
                    }
                });
    }

    private void loginToTwitter() {
        GetRequestTokenTask getRequestTokenTask = new GetRequestTokenTask();
        getRequestTokenTask.execute();
    }

    private class GetRequestTokenTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("TestURL", "init twitter");
            twitter = TwitterFactory.getSingleton();
            twitter.setOAuthConsumer(
                    getString(R.string.TWITTER_CONSUMER_KEY),
                    getString(R.string.TWITTER_CONSUMER_SECRET));
            try {
                RequestToken requestToken = twitter.getOAuthRequestToken(
                        getString(R.string.TWITTER_CALLBACK_URL));
                launchLoginWebView(requestToken);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void launchLoginWebView(RequestToken requestToken) {
        Intent intent = new Intent(this, LoginToTwitterActivity.class);
        intent.putExtra(AUTHENTICATION_URL_KEY, requestToken.getAuthenticationURL());
        startActivityForResult(intent, LOGIN_TO_TWITTER_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TestURL","return from webView");
        if (requestCode == LOGIN_TO_TWITTER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("TestURL","ready to get access token");
                getAccessToken(data.getStringExtra(LoginToTwitterActivity.CALLBACK_URL_KEY));
            }
        }
    }


    private void getAccessToken(String callbackUrl) {
        Uri uri = Uri.parse(callbackUrl);
        String verifier = uri.getQueryParameter("oauth_verifier");

        GetAccessTokenTask getAccessTokenTask = new GetAccessTokenTask();
        getAccessTokenTask.execute(verifier);
    }


    private class GetAccessTokenTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String verifier = strings[0];
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(verifier);
                acsTkn = accessToken;
                twitter.setOAuthAccessToken(accessToken);
            } catch (Exception e) {

            }
            return null;
        }

        protected void onPostExecute(Void result) {
            storeAccessToken();
            loadTimeline();
        }
    }

    private void storeAccessToken(){
        prefs = getSharedPreferences(APPLICATION_PREFERENCES, MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(AUTH_KEY, acsTkn.getToken());
        editor.putString(AUTH_SEKRET_KEY, acsTkn.getTokenSecret());
        editor.commit();
    }

    private boolean configureTwitter() {
        prefs = getSharedPreferences(APPLICATION_PREFERENCES, MODE_PRIVATE);
        String token = prefs.getString(AUTH_KEY, null);
        String tokenSecret = prefs.getString(AUTH_SEKRET_KEY, null);
        if (null != token && null != tokenSecret) {
            acsTkn = new AccessToken(token, tokenSecret);
            twitter = TwitterFactory.getSingleton();
            twitter.setOAuthConsumer(
                    getString(R.string.TWITTER_CONSUMER_KEY),
                    getString(R.string.TWITTER_CONSUMER_SECRET));
            twitter.setOAuthAccessToken(acsTkn);
            return true;
        } else {
            return false;
        }
    }


}
