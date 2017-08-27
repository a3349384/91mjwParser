package cn.zmy.mjwparser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import cn.zmy.mjwparser.base.SimpleTextAdapter;
import cn.zmy.mjwparser.constant.IntentKeys;
import cn.zmy.mjwparser.model.Video;
import cn.zmy.mjwparser.model.VideoGroup;
import cn.zmy.mjwparser.util.IOUtil;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public class VideosActivity extends Activity
{
    private GetVideoAsyncTask mGetVideoAsyncTask;
    private GetVideoAddressTask mGetVideoAddressTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

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
                onVideoClick((Video) parent.getAdapter().getItem(position));
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
        if (mGetVideoAddressTask != null && mGetVideoAddressTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            mGetVideoAddressTask.cancel(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        if (menuItem.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onVideoClick(Video video)
    {
        Intent intent = new Intent(this, VideoPlayActivity.class);
        intent.putExtra(IntentKeys.KEY_VIDEO, video);
        startActivity(intent);
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

    private static class GetVideoAddressTask extends AsyncTask<Video, Void, String>
    {
        private ProgressDialog mProgressDialog;
        private Video mVideo;

        public GetVideoAddressTask(ProgressDialog progressDialog)
        {
            this.mProgressDialog = progressDialog;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Video... params)
        {
            mVideo = params[0];
            String videoWebUrl = mVideo.getUrl();
            String videoPlayAddress = null;
            int retryTimes = 0;
            while (retryTimes < 3)
            {
                try
                {
                    Document document = Jsoup.connect(videoWebUrl).timeout(10000).get();
                    String typeAndTvIdString = document.select(".video_list").get(0).parent().select("p script").get(0).data().trim();
                    String[] variablesString = typeAndTvIdString.split(";");
                    ArrayMap<String,String> variablesMap = new ArrayMap<>();
                    for (String variableString : variablesString)
                    {
                        String[] keyValue = variableString.replace("var", "").trim().replace("\"", "").split("=");
                        variablesMap.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                    if (variablesMap.containsKey("type") && variablesMap.containsKey("tvid"))
                    {
                        String type = variablesMap.get("type");
                        String tvid = variablesMap.get("tvid");
                        //发起Http请求获取视频播放地址
                        String postString = String.format("type=%s&data=%s&cip=221.237.118.61&refres=1&my_url=%s",
                                type, URLEncoder.encode(tvid, "UTF-8"), URLEncoder.encode(videoWebUrl, "UTF-8"));
                        String postUrl = "https://vod.lujiahb.com/1SuPlayer/vod/Api.php";
                        URL url = new URL(postUrl);
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setReadTimeout(10000);
                        connection.setConnectTimeout(10000);
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                        connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
                        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                        connection.setRequestProperty("Referer", String.format("https://vod.lujiahb.com/1SuPlayer/vod/?type=%s&v=%s", type, tvid));
                        connection.setRequestProperty("Accept-Language", "zh-CN");
                        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
                        connection.setRequestProperty("Host", "vod.lujiahb.com");
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.connect();
                        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                        dos.write(postString.getBytes());
                        dos.flush();
                        dos.close();

                        if (connection.getResponseCode() == 200)
                        {
                            String responseString = IOUtil.toString(connection.getInputStream());
                            videoPlayAddress = new JSONObject(responseString).getString("url");
                            if (!TextUtils.isEmpty(videoPlayAddress))
                            {
                                break;
                            }
                        }
                    }

                    retryTimes ++;
                }
                catch (Exception ex)
                {
                    retryTimes ++;
                }
            }

            return videoPlayAddress;
        }

        @Override
        protected void onPostExecute(String videoUrl)
        {
            super.onPostExecute(videoUrl);
            mProgressDialog.dismiss();
            if (TextUtils.isEmpty(videoUrl))
            {
                Toast.makeText(mProgressDialog.getContext(), "视频地址获取失败,请重试", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intentOpenVideo = new Intent(Intent.ACTION_VIEW);
            intentOpenVideo.setPackage("com.miui.video");
            intentOpenVideo.setDataAndType(Uri.parse(videoUrl), "video/*");
            intentOpenVideo.putExtra("mediaTitle", mVideo.getName());
            mProgressDialog.getContext().startActivity(intentOpenVideo);
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            mProgressDialog = null;
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
