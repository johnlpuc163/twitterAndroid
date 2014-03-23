package com.mytwt.app;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;

/**
 * Created by John on 14-3-21.
 */
public class LoadImageAsyncTask extends AsyncTask<String, Void, Drawable> {
    ImageView avatarView;
    public LoadImageAsyncTask(ImageView avatarView) {
        this.avatarView = avatarView;
    }

    @Override
    protected Drawable doInBackground(String... args) {
        try {
            URL url = new URL(args[0]);
            return Drawable.createFromStream(url.openStream(), url.toString());
        } catch (IOException e) {
            Log.e(getClass().getName(), "Could not load image.", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Drawable result) {
        super.onPostExecute(result);
        avatarView.setImageDrawable(result);
    }
}
