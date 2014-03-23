package com.mytwt.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;

import java.util.List;

import twitter4j.Status;

public class PostActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //set cancel button
        findViewById(R.id.cancel_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

        //set tweet button
        findViewById(R.id.tweet_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String postText = ((EditText)findViewById(R.id.tweet_content)).getText().toString();
                        int postLength = postText.length();
                        if (postLength > 0 && postLength < 140){
                            new PostTweetAsycTask().execute(postText);
                        }
                    }
                });
    }


    private void postFinish(){
        finish();
    }
    private class PostTweetAsycTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... tweets) {
            String tweet = tweets[0];
            try {
                MainActivity.twitter.updateStatus(tweet);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            postFinish();
        }

    }
}
