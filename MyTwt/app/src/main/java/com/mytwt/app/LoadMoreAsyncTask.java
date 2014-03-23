package com.mytwt.app;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;


public class LoadMoreAsyncTask extends AsyncTask<Void, Void, LoadMoreAsyncTask.LoadMoreStatusesResult> {

    private long targetId;
    private int loadType;
    private Twitter twitter;
    private LoadMoreStatusesResponder responder;
    private final int LOAD_NEWER = 1;
    private final int LOAD_TIMELINE = 0;
    private final int LOAD_OLDER = -1;

    public interface LoadMoreStatusesResponder {
        public void statusesLoaded(LoadMoreStatusesResult result);
    }

    public class LoadMoreStatusesResult {
        public List<twitter4j.Status> statuses;
        public int loadType;
        public LoadMoreStatusesResult(List<twitter4j.Status> statuses, int loadType) {
            super();
            this.statuses = statuses;
            this.loadType = loadType;
        }
    }

    //constructor
    public LoadMoreAsyncTask(LoadMoreStatusesResponder responder, Twitter twitter, long targetId, int loadType) {
        super();
        this.responder = responder;
        this.targetId = targetId;
        this.twitter = twitter;
        this.loadType = loadType;
    }

    @Override
    protected LoadMoreAsyncTask.LoadMoreStatusesResult doInBackground(Void...params) {
        List<twitter4j.Status> statii = null;
        try {
            switch (loadType){
                case LOAD_TIMELINE:
                    statii = twitter.getHomeTimeline();
                    break;
                case LOAD_NEWER:
                    statii = twitter.getHomeTimeline(new Paging(1).sinceId(targetId));
                    break;
                case LOAD_OLDER:
                    statii = twitter.getHomeTimeline(new Paging(1).maxId(targetId));
                default:
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load timeline", e);
        }

        return new LoadMoreStatusesResult(statii, loadType);
    }

    @Override
    public void onPostExecute(LoadMoreStatusesResult result) {
        responder.statusesLoaded(result);
    }
}
