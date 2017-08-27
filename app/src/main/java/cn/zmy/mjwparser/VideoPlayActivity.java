package cn.zmy.mjwparser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import cn.zmy.mjwparser.constant.IntentKeys;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String videoUrl = getIntent().getStringExtra(IntentKeys.KEY_VIDEO_URL);
        String videoTitle = getIntent().getStringExtra(IntentKeys.KEY_VIDEO_TITLE);
        long seekDuration = getIntent().getLongExtra(IntentKeys.KEY_SEEK_DURATION, 0);
        if (TextUtils.isEmpty(videoUrl))
        {
            return;
        }
        setContentView(R.layout.activity_video_play);
        SuperVideoView videoView = (SuperVideoView) findViewById(R.id.videoView);
        SuperVideoController videoController = new SuperVideoController(this);
        videoView.setVideoController(videoController);
        videoController.play(Uri.parse(videoUrl), null);
    }
}
