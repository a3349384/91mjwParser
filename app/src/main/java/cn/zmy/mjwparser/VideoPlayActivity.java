package cn.zmy.mjwparser;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import cn.zmy.mjwparser.constant.IntentKeys;
import cn.zmy.mjwparser.util.Md5Util;
import cn.zmy.mjwparser.widget.video.SuperVideoController;
import cn.zmy.mjwparser.widget.video.SuperVideoView;

/**
 * Created by zmy on 2017/8/27.
 */

public class VideoPlayActivity extends Activity
{
    public static void start(Context context, String videoUrl, String videoTitle, long seekDuration)
    {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(IntentKeys.KEY_VIDEO_URL, videoUrl);
        intent.putExtra(IntentKeys.KEY_VIDEO_TITLE, videoTitle);
        intent.putExtra(IntentKeys.KEY_SEEK_DURATION, seekDuration);
        context.startActivity(intent);
    }

    private SuperVideoController mVideoController;
    private String mVideoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mVideoUrl = getIntent().getStringExtra(IntentKeys.KEY_VIDEO_URL);
        String videoTitle = getIntent().getStringExtra(IntentKeys.KEY_VIDEO_TITLE);
        long seekDuration = getPlayedDuration(mVideoUrl);
        if (TextUtils.isEmpty(mVideoUrl))
        {
            return;
        }
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_video_play);
        SuperVideoView videoView = (SuperVideoView) findViewById(R.id.videoView);
        mVideoController = new SuperVideoController(this);
        videoView.setVideoController(mVideoController);
        mVideoController.play(Uri.parse(mVideoUrl), null, seekDuration);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mVideoController == null)
        {
            return;
        }
        if (!TextUtils.isEmpty(mVideoUrl))
        {
            long playDuration = mVideoController.getPlayer().getCurrentPosition();
            if (playDuration > 0)
            {
                setPlayedDuration(mVideoUrl, playDuration);
            }
            mVideoController.stop();
            mVideoController.release();
        }
    }

    private long getPlayedDuration(String videoUrl)
    {
        return getSharedPreferences("VIDEO_PLAYED_DURATION", Context.MODE_PRIVATE).getLong(Md5Util.md5(videoUrl), 0);
    }

    private void setPlayedDuration(String videoUrl, long duration)
    {
        getSharedPreferences("VIDEO_PLAYED_DURATION", Context.MODE_PRIVATE)
                .edit()
                .putLong(Md5Util.md5(videoUrl), duration)
                .apply();
    }
}
