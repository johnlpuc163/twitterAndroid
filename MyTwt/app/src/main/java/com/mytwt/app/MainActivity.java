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
    //formal
    private StatusListAdapter adapter;
    User user;
    View headerView;
    View footerView;
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

    //to be checked
    public static Twitter twitter;
    public static AccessToken acsTkn;
    private List<Status> statii;
    Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TestURL","enter oncreate");
        if( acsTkn == null){
            Log.d("TestURL","null acstkn");
        }
        if( twitter == null){
            Log.d("TestURL","null twitter");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set post button
        setPostButton();

        //login to twitter, called when the application is initialized for the first time
        if(twitter == null){
            configureTwitter();
        }


        /*
        * String token = "702351582-n3I0cwhEfwJaBegW2F2pumkd9o4FCj131Dxmf0MR";
            String tokenSecret = "1bJribAvZdNk88iMOjF6VudDgI1Z5MsECRauqdrYTJi6E";
        * */
        if(false){
         String token = "702351582-NiH0sSLURX9ePzckfU8jGl6Wz3HlY0Iu84Fsbeev";
        String tokenSecret = "UZE2i7hIdUAI72qwklDsUxqH4VmFoUhj9pbaseAr52gEN";
             Log.d("TestURL", "init acsTKN");
            acsTkn = new AccessToken(token, tokenSecret);
            Log.d("TestURL","init twitter oauth");
            twitter = TwitterFactory.getSingleton();
            twitter.setOAuthConsumer(
                    getString(R.string.TWITTER_CONSUMER_KEY),
                    getString(R.string.TWITTER_CONSUMER_SECRET));
            Log.d("TestURL","init twitter access");
            twitter.setOAuthAccessToken(acsTkn);
        }
        //loadTimeline();
        //    getUsrName getAccessTokenTask = new getUsrName();
          //  getAccessTokenTask.execute();

        if (twitter == null) {
            loginToTwitter();
        } else {
            Log.d("TestURL","begin to loadTimeline");
            loadTimeline();
        }


    }

    /*
    @Override
    protected void onResume() {
        Log.d("TestURL","enter onresume");
        super.onResume();
        if (acsTkn == null) {
            loginToTwitter();
        } else {
            Log.d("TestURL","begin to loadTimeline");
            loadTimeline();
        }
    }
*/
    private void loadTimeline(){
        LoadTimelineAsyncTast loadtimelineTask = new LoadTimelineAsyncTast();
        loadtimelineTask.execute();
    }

    private class LoadTimelineAsyncTast extends AsyncTask<Void, Void, List<Status>> {

        @Override
        protected List<twitter4j.Status> doInBackground(Void... voids) {
            Log.d("TestURL","enter asyncTask for usrName");
            String username = "";
            List<twitter4j.Status> statii = null;
            try {
                long userID = acsTkn.getUserId();
                Log.d("TestURL","retrieving usrName");
                user = twitter.showUser(userID);
                Log.d("TestURL","retrieving usrname finish");
                username = user.getName();
                Log.d("TestURL","username got");
                Log.d("TestURL", username);
                statii = twitter.getHomeTimeline();
                Log.d("TestURL", "gotHomeLine");

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
            // Watch out! Doesn't account for header/footer! -> Status status = adapter.getItem(position);
        }
    } 
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
                Log.d("TestURL","start to get access token");
                AccessToken accessToken = twitter.getOAuthAccessToken(verifier);
                acsTkn = accessToken;
                Log.d("TestURL","got access token");
                Log.d("TestURL",accessToken.getToken());
                Log.d("TestURL",accessToken.getTokenSecret());
                twitter.setOAuthAccessToken(accessToken);
                long userID = accessToken.getUserId();
                User user = twitter.showUser(userID);
                String username = user.getName();
                Log.d("TestURL","username got");
                Log.d("TestURL",username);
                //Log.d(MainActivity.class.getSimpleName(), accessToken.getToken());
            } catch (Exception e) {

            }
            return null;
        }

        protected void onPostExecute(Void result) {
            storeAccessToken();
            Log.d("TestURL","enter post execu");
            loadTimeline();
        }
    }

    private void storeAccessToken(){
        prefs = getSharedPreferences(APPLICATION_PREFERENCES, MODE_PRIVATE);
        Log.d("TestURL","store acs tkn");
        Editor editor = prefs.edit();
        Log.d("TestURL","got editor");
        editor.putString(AUTH_KEY, acsTkn.getToken());
        editor.putString(AUTH_SEKRET_KEY, acsTkn.getTokenSecret());
        Log.d("TestURL","ready to commit edit");
        editor.commit();
        Log.d("TestURL","finish store acs tkn");
    }

    private boolean configureTwitter() {
        prefs = getSharedPreferences(APPLICATION_PREFERENCES, MODE_PRIVATE);
        String token = prefs.getString(AUTH_KEY, null);
        String tokenSecret = prefs.getString(AUTH_SEKRET_KEY, null);
        if (null != token && null != tokenSecret) {
            Log.d("TestURL","ready to configureTwitter");
            acsTkn = new AccessToken(token, tokenSecret);
            twitter = TwitterFactory.getSingleton();
            twitter.setOAuthConsumer(
                    getString(R.string.TWITTER_CONSUMER_KEY),
                    getString(R.string.TWITTER_CONSUMER_SECRET));
            twitter.setOAuthAccessToken(acsTkn);
            Log.d("TestURL","finish configureTwitter");
            return true;
        } else {
            return false;
        }
    }


}
