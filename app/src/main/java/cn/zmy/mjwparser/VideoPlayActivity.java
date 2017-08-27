package cn.zmy.mjwparser;

import android.app.Activity;
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
    private String mVideoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mVideoUrl = getIntent().getStringExtra(IntentKeys.KEY_VIDEO_URL);
        if (TextUtils.isEmpty(mVideoUrl))
        {
            return;
        }
        setContentView(R.layout.activity_video_play);
        SuperVideoView videoView = (SuperVideoView) findViewById(R.id.videoView);
        SuperVideoController videoController = new SuperVideoController(this);
        videoView.setVideoController(videoController);
        videoController.play(Uri.parse(mVideoUrl), null);
    }
}
