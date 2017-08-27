package cn.zmy.mjwparser.widget.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.zmy.mjwparser.R;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;

/**
 * Created by zmy on 2016/8/20.
 * 视频播放控制器
 */
public class SuperVideoController extends FrameLayout
    implements OnVideoEventsListener,SeekBar.OnSeekBarChangeListener
{
    protected IVideoPlayer player;
    protected long videoDuration;
    protected SubscriptionList subscriptionList;

    protected View viewControl;
    protected ImageView imageViewPlay;
    protected ImageView imageViewFullScreen;
    protected TextView textViewDuration;
    protected TextView textViewCurrentPosition;
    protected SeekBar seekBarProgress;

    protected View viewTop;

    protected ResizeVideoTextureView textureView;
    protected ProgressBar progressBarLoading;

    public SuperVideoController(Context context)
    {
        this(context,null);
    }

    public SuperVideoController(Context context, AttributeSet attrs)
    {
        this(context, attrs,0);
    }

    public SuperVideoController(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        int controlLayoutID = getControlLayout();
        int topLayoutID = getTopLayout();

        if (controlLayoutID > 0)
        {
            viewControl = LayoutInflater.from(context).inflate(controlLayoutID,this,false);
            initControlItem();
            setControlItemEvents();

            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(viewControl,params);
        }

        if (topLayoutID > 0)
        {
            viewTop = LayoutInflater.from(context).inflate(topLayoutID,this,false);
            initTopItem();
            setTopItemEvents();

            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP;
            this.addView(viewTop,params);
        }
    }

    public IVideoPlayer getPlayer()
    {
        return player;
    }

    public void setPlayer(IVideoPlayer player)
    {
        this.player = player;
    }

    protected int getControlLayout()
    {
        return R.layout.widget_video_controller;
    }

    protected int getTopLayout()
    {
        return 0;
    }

    protected void initControlItem()
    {
        imageViewPlay = (ImageView) this.viewControl.findViewById(R.id.imageViewPlay);
        imageViewFullScreen = (ImageView) this.viewControl.findViewById(R.id.imageViewFullScreen);
        textViewDuration = (TextView) this.viewControl.findViewById(R.id.textViewDuration);
        textViewCurrentPosition = (TextView) this.viewControl.findViewById(R.id.textViewCurrentPosition);
        seekBarProgress = (SeekBar) this.viewControl.findViewById(R.id.seekBarProgress);
    }

    protected void setControlItemEvents()
    {
        imageViewPlay.setOnClickListener(this::controlPlay);
        imageViewFullScreen.setOnClickListener(this::controlFullScreen);
        seekBarProgress.setOnSeekBarChangeListener(this);
    }

    protected void initTopItem()
    {

    }

    protected void setTopItemEvents()
    {

    }

    protected void controlPlay(View view)
    {
        if (this.player.isPlaying())
        {
            this.player.pause();
        }
        else
        {
            this.player.play();
        }
    }

    protected void controlFullScreen(View view)
    {
        Activity activity = (Activity) getContext();
        if (activity == null)
        {
            return;
        }
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            this.imageViewFullScreen.setImageResource(R.drawable.ic_normal_screen_white);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else
        {
            this.imageViewFullScreen.setImageResource(R.drawable.ic_full_screen_white);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    protected String toTimeString(long duration)
    {
        if (duration <= 0 || duration >= 24 * 60 * 60 * 1000)
        {
            return "00:00";
        }
        int totalSeconds = (int) (duration / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0)
        {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        }
        else
        {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    //region OnVideoEventsListener接口实现

    @Override
    public void onPrepared()
    {
        this.videoDuration = this.player.getDuration();
        this.textViewDuration.setText(toTimeString(videoDuration));
        this.player.play();
    }

    @Override
    public void onPlay()
    {
        this.imageViewPlay.setImageResource(R.drawable.ic_pause_white);
        this.startTimerToHide();
    }

    @Override
    public void onPause()
    {
        this.imageViewPlay.setImageResource(R.drawable.ic_play_white);
    }

    @Override
    public void onCompeted()
    {
        this.imageViewPlay.setImageResource(R.drawable.ic_play_white);
        //取消可能的对Controller的显示或隐藏的操作
        this.releaseSubscriptionList();
        //强制显示Controller
        this.performShow();
    }

    @Override
    public void onPlayProgressUpdated(long currentPosition)
    {
        currentPosition = Math.min(currentPosition,videoDuration);
        this.textViewCurrentPosition.setText(toTimeString(currentPosition));
        this.seekBarProgress.setProgress((int) ((float)currentPosition/(float) videoDuration * 100));
    }

    @Override
    public void onBufferingStart()
    {
        this.progressBarLoading.setVisibility(VISIBLE);
    }

    @Override
    public void onBufferingEnd()
    {
        this.progressBarLoading.setVisibility(GONE);
    }

    @Override
    public void onBufferingUpdated(int percent)
    {
        percent = Math.min(percent,100);
        this.seekBarProgress.setSecondaryProgress(percent);
    }

    @Override
    public void onVideoSizeChanged(int width, int height)
    {
        this.textureView.setVideoWH(width,height);
    }

    //endregion

    //region SeekBar.OnSeekBarChangeListener接口实现
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        //如果用户拖动了进度条，当前点的时间也应该同步变化
        if (fromUser)
        {
            this.textViewCurrentPosition.setText(toTimeString((long) (videoDuration * (progress/100f))));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        //取消隐藏controller的操作
        releaseSubscriptionList();
        //处于拖动状态，视频设置为暂停状态
        this.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        int seekTo = (int) (seekBar.getProgress()/100f * videoDuration);
        this.player.seekTo(seekTo);
        if (!this.player.isPlaying())
        {
            this.player.play();
        }
    }
    //endregion

    public void play(Uri uri, Map<String,String> header)
    {
        this.player.reset();
        this.player.setUri(uri,header);
        this.player.setVideoEventsListener(this);
        this.player.prepare();
    }

    public void pause()
    {
        this.player.pause();
    }

    public void stop()
    {
        this.player.stop();
    }

    public void release()
    {
        this.player.release();
        this.releaseSubscriptionList();
    }

    public void setTextureView(ResizeVideoTextureView textureView)
    {
        this.textureView = textureView;
    }

    public void setProgressBarLoading(ProgressBar progressBarLoading)
    {
        this.progressBarLoading = progressBarLoading;
    }

    /**
     * 显示Controller
     * @param autoHide 是否自动隐藏
     * */
    public void show(boolean autoHide)
    {
        if (isShowing())
        {
            return;
        }

        performShow();
        if (autoHide)
        {
            startTimerToHide();
        }
    }

    public boolean isShowing()
    {
        return this.viewControl.getTranslationY() != this.viewControl.getHeight();
    }

    private void performShow()
    {
        if (this.viewControl != null)
        {
            this.viewControl.animate().translationY(0).setDuration(300).start();
        }

        if (this.viewTop != null)
        {
            this.viewTop.animate().translationY(0).setDuration(300).start();
        }
    }

    private void performHide()
    {
        if (this.viewControl != null)
        {
            this.viewControl.animate().translationY(viewControl.getHeight()).setDuration(300).start();
        }

        if (this.viewTop != null)
        {
            this.viewTop.animate().translationY(-viewTop.getHeight()).setDuration(300).start();
        }
    }

    private void startTimerToHide()
    {
        Subscription subscription = Observable.interval(3000, TimeUnit.MILLISECONDS)
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(l -> performHide());
        this.releaseSubscriptionList();
        this.subscriptionList = new SubscriptionList();
        this.subscriptionList.add(subscription);
    }

    private void releaseSubscriptionList()
    {
        if (subscriptionList == null)
        {
            return;
        }
        this.subscriptionList.unsubscribe();
        this.subscriptionList.clear();
        this.subscriptionList = null;
    }
}
