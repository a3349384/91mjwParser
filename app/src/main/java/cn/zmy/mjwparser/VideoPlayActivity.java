package cn.zmy.mjwparser;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import cn.zmy.mjwparser.constant.IntentKeys;
import cn.zmy.mjwparser.model.Video;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public class VideoPlayActivity extends Activity
{
    private GetVideoAddressTask mGetVideoAddressTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        Video video = getIntent().getParcelableExtra(IntentKeys.KEY_VIDEO);
        if (video == null)
        {
            return;
        }
        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(video.getUrl()));
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener()
        {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra)
            {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        progressBar.setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra)
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(VideoPlayActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //从网页解析视频地址
        mGetVideoAddressTask = new GetVideoAddressTask(videoView, progressBar);
        mGetVideoAddressTask.execute(video.getUrl());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mGetVideoAddressTask != null && mGetVideoAddressTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            mGetVideoAddressTask.cancel(true);
        }
    }

    private static class GetVideoAddressTask extends AsyncTask<String, Void, String>
    {
        private VideoView mVideoView;
        private ProgressBar mProgressBar;

        public GetVideoAddressTask(VideoView videoView, ProgressBar progressBar)
        {
            this.mVideoView = videoView;
            this.mProgressBar = progressBar;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                String url = params[0];
                Document document = Jsoup.connect(url).timeout(10000).get();
                Element elementVideo = document.select("#my-video_html5_api > source").get(0);
                return elementVideo.attr("src");
            }
            catch (Exception ignored)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String videoUrl)
        {
            super.onPostExecute(videoUrl);
            mVideoView.setVideoURI(Uri.parse(videoUrl));
            mVideoView.start();
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            mProgressBar = null;
            mVideoView = null;
        }
    }
}
