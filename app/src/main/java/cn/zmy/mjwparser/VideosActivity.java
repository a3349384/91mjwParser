package cn.zmy.mjwparser;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import cn.zmy.mjwparser.base.SimpleTextAdapter;
import cn.zmy.mjwparser.constant.IntentKeys;
import cn.zmy.mjwparser.model.Video;
import cn.zmy.mjwparser.model.VideoGroup;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public class VideosActivity extends Activity
{
    private GetVideoAsyncTask mGetVideoAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        VideoGroup mVideoGroup = getIntent().getParcelableExtra(IntentKeys.KEY_VIDEO_GROUP);
        if (mVideoGroup == null)
        {
            return;
        }
        setContentView(R.layout.activity_videos);
        setTitle(mVideoGroup.getName());
        ListView listViewVideos = (ListView) findViewById(R.id.listViewVideos);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        VideosAdapter adapter = new VideosAdapter();
        listViewVideos.setAdapter(adapter);
        listViewVideos.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(VideosActivity.this, VideoPlayActivity.class);
                intent.putExtra(IntentKeys.KEY_VIDEO, (Video)parent.getAdapter().getItem(position));
                VideosActivity.this.startActivity(intent);
            }
        });

        //解析网页获取视频数据
        mGetVideoAsyncTask = new GetVideoAsyncTask(progressBar, adapter);
        mGetVideoAsyncTask.execute(mVideoGroup.getUrl());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mGetVideoAsyncTask != null && mGetVideoAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            mGetVideoAsyncTask.cancel(true);
        }
    }

    private static class GetVideoAsyncTask extends AsyncTask<String, Void, List<Video>>
    {
        private ProgressBar mProgressBar;
        private VideosAdapter mAdapter;

        public GetVideoAsyncTask(ProgressBar mProgressBar, VideosAdapter mAdapter)
        {
            this.mProgressBar = mProgressBar;
            this.mAdapter = mAdapter;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Video> doInBackground(String... params)
        {
            try
            {
                String url = params[0];
                Document document = Jsoup.connect(url).timeout(10000).get();
                Elements elementsVideo = document.select("#video_list_li a");
                List<Video> videos = new ArrayList<>(elementsVideo.size());
                for (Element elementVideo : elementsVideo)
                {
                    Video video = new Video();
                    video.setName(elementVideo.text().trim());
                    video.setUrl(url + elementVideo.attr("href").trim());

                    videos.add(video);
                }

                return videos;
            }
            catch (Exception ignored)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Video> videos)
        {
            super.onPostExecute(videos);
            mProgressBar.setVisibility(View.GONE);
            mAdapter.refresh(videos);
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            mProgressBar = null;
            mAdapter = null;
        }
    }

    private static class VideosAdapter extends SimpleTextAdapter<Video>
    {
        @Override
        protected String getText(Video video)
        {
            return video.getName();
        }
    }
}
