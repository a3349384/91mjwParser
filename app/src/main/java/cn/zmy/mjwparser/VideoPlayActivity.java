package cn.zmy.mjwparser;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import cn.zmy.mjwparser.constant.IntentKeys;
import cn.zmy.mjwparser.model.Video;
import cn.zmy.mjwparser.util.IOUtil;

/**
 * Created by zmy on 2017/8/25 0025.
 */

public class VideoPlayActivity extends Activity
{
    private GetVideoAddressTask mGetVideoAddressTask;
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_play);

        Video video = getIntent().getParcelableExtra(IntentKeys.KEY_VIDEO);
        if (video == null)
        {
            return;
        }
        mVideoView = (VideoView) findViewById(R.id.videoView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final MediaController mediaController = new MediaController(this);
        mVideoView.setMediaController(mediaController);
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener()
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
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
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
        mGetVideoAddressTask = new GetVideoAddressTask(mVideoView, progressBar);
        mGetVideoAddressTask.execute(video.getUrl());
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mVideoView != null && mVideoView.isPlaying())
        {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mGetVideoAddressTask != null && mGetVideoAddressTask.getStatus() != AsyncTask.Status.FINISHED)
        {
            mGetVideoAddressTask.cancel(true);
        }
        //todo 记录播放时间

        mVideoView.getCurrentPosition();
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
                String videoWebUrl = params[0];
                Document document = Jsoup.connect(videoWebUrl).timeout(10000).get();
                String typeAndTvIdString = document.select(".video_list").get(0).parent().select("p script").get(0).data().trim();
                String[] variablesString = typeAndTvIdString.split(";");
                ArrayMap<String,String> variablesMap = new ArrayMap<>();
                for (String variableString : variablesString)
                {
                    String[] keyValue = variableString.replace("var", "").trim().replace("\"", "").split("=");
                    variablesMap.put(keyValue[0].trim(), keyValue[1].trim());
                }
                if (!variablesMap.containsKey("type") || !variablesMap.containsKey("tvid"))
                {
                    return null;
                }
                String type = variablesMap.get("type");
                String tvid = variablesMap.get("tvid");
                //发起Http请求获取视频播放地址
                String postString = String.format("type=%s&data=%s&refres=1&my_url=%s", type, tvid, videoWebUrl);
                String postUrl = "https://vod.lujiahb.com/1SuPlayer/vod/Api.php";
                URL url = new URL(postUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(10000);
                connection.setUseCaches(false);
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

                if (connection.getResponseCode() != 200)
                {
                    return null;
                }
                String responseString = IOUtil.toString(connection.getInputStream());
                return new JSONObject(responseString).getString("url");
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
            if (TextUtils.isEmpty(videoUrl))
            {
                Toast.makeText(mVideoView.getContext(), "视频地址获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            mVideoView.setVideoURI(Uri.parse(videoUrl));
            mVideoView.start();

            try
            {
                Field fieldController = VideoView.class.getDeclaredField("mMediaController");
                fieldController.setAccessible(true);
                MediaController mediaController = (MediaController) fieldController.get(mVideoView);
                Field fieldId = Class.forName("com.android.internal.R$id").getDeclaredField("mediacontroller_progress");
                fieldId.setAccessible(true);
                SeekBar seekBar = (SeekBar) mediaController.findViewById((Integer) fieldId.get(null));
                seekBar.getThumb().setTint(Color.WHITE);
            }
            catch (Exception ignored) {}
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
