package me.lake.floatingbili.player.floatingplay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import me.lake.floatingbili.R;
import me.lake.floatingbili.player.WeakHandler;
import me.lake.floatingbili.player.playerSurfaceView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by Lakeinchina(lakeinchina@hotmail.com) on 2015/10/25.
 * FloatingBili Project
 *
 * Copyright (C) 2015 Po Hu <lakeinchina@hotmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
public class BiliFloatingView implements OnVideoSizeChangedListener, OnInfoListener, OnBufferingUpdateListener, OnErrorListener, OnCompletionListener {
    private FrameLayout mSurfaceFrame;
    private playerSurfaceView psv_player;
    // size of the video
    private int navigationBarHeight = 0;
    private int mVideoHeight;
    private int mVideoWidth;
    private int mSarNum;
    private int mSarDen;
    private boolean mHasVout = false;
    // ****************************************end*****************************************
    private AudioManager mAudioManager;
    private int mAudioMax;
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int FADE_OUT = 1;
    private static final int SURFACE_SIZE = 3;
    private static final int FIX_SIZE = 4;
    private static float TOLERATION = 0.1f;
    private static final int PLAYER_ERROR = 103;
    private static final int REFRESH_PROGRESS = 105;
    private float lastX;
    private float lastY;
    Display currentDisplay;
    public final static String TAG = "BiliFloatingView";
    ViewGroup mlayoutView;
    private BiliFloatingPlayService mService;
    // size of the video
    private int mWindowHeight;
    private int mWindowWidth;
    private View btn_close;
    private View btn_back;
    private ScaleGestureDetector mScaleGestureDetector = null;
    private WindowManager wm = null;
    private WindowManager.LayoutParams wmParams = null;
    private int ScreenWidth = 0;
    private int ScreenHeight = 0;
    private int MinWindowWidth = 0;
    private TextView tv_channel_name;
    private TextView tv_channel_program;
    private View rl_overlay;
    private View pb_progress;
    private boolean isShowingOverlay = false;
    private float ratio = 0;
    private float default_ratio = 9 / 16;
    private int stateBarHeight = 50;

    private String playUrl;
    private IjkMediaPlayer ijkMediaPlayer;

    public BiliFloatingView(BiliFloatingPlayService c, String playUrl) {
        this.playUrl = playUrl;
        mService = c;
        int notificationBarResources[] = {android.R.drawable.stat_sys_phone_call, android.R.drawable.stat_notify_call_mute,
                android.R.drawable.stat_notify_sdcard, android.R.drawable.stat_notify_sync, android.R.drawable.stat_notify_missed_call,
                android.R.drawable.stat_sys_headset, android.R.drawable.stat_sys_warning};
        for (int i = 0; i < notificationBarResources.length; i++) {
            try {
                Drawable phoneCallIcon = mService.getResources().getDrawable(notificationBarResources[i]);
                if ((stateBarHeight = phoneCallIcon.getIntrinsicHeight()) != -1) {
                    break;
                }
            } catch (Resources.NotFoundException e) {
            }
        }
        mlayoutView = (ViewGroup) View.inflate(mService, R.layout.layout_floatview, null);
        mScaleGestureDetector = new ScaleGestureDetector(mService, new ScaleGestureListener());
        tv_channel_name = (TextView) mlayoutView.findViewById(R.id.tv_channel_name);
        tv_channel_program = (TextView) mlayoutView.findViewById(R.id.tv_channel_program);
        rl_overlay = mlayoutView.findViewById(R.id.rl_overlay);
        pb_progress = mlayoutView.findViewById(R.id.pb_progress);
        btn_close = mlayoutView.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });
        btn_back = mlayoutView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mlayoutView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                mScaleGestureDetector.onTouchEvent(paramMotionEvent);
                onTouchEvent(paramMotionEvent);
                return false;
            }
        });
        try {
            updateTextInfo();
            initWindow();
            initPlay();
            showOverlay();
            mHasVout = false;
        } catch (SecurityException se) {
            Toast.makeText(mService, "opps!,好像没有权限,no permission", Toast.LENGTH_LONG).show();
            quit();
        } catch (Exception e) {
            Toast.makeText(mService, "opps!,出错了" + e.getMessage(), Toast.LENGTH_LONG).show();
            quit();
        }
    }

    public void changeUrl(String path) {
        try {
            ijkMediaPlayer.stop();
            ijkMediaPlayer.setDataSource(path);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ijkMediaPlayer.start();
    }

    private void initWindow() {
        wm = (WindowManager) mService.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.TRANSLUCENT;
        wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | LayoutParams.FLAG_KEEP_SCREEN_ON;

        wmParams.gravity = Gravity.CENTER;
        currentDisplay = wm.getDefaultDisplay();
        ScreenWidth = currentDisplay.getWidth();
        ScreenHeight = currentDisplay.getHeight() - stateBarHeight;
        mWindowWidth = ScreenWidth;
        mWindowHeight = mWindowWidth * 9 / 16;
        MinWindowWidth = (ScreenWidth < ScreenHeight ? ScreenWidth : ScreenHeight) / 2;
        wmParams.width = mWindowWidth;
        wmParams.height = mWindowHeight;
        wmParams.x = 0;
        wmParams.y = (wmParams.height - ScreenHeight) / 2;

        wm.addView(mlayoutView, wmParams);
    }

    @SuppressWarnings("deprecation")
    private void initPlay() {
        mSurfaceFrame = (FrameLayout) mlayoutView.findViewById(R.id.player_surface_frame);
        psv_player = (playerSurfaceView) mlayoutView.findViewById(R.id.psv_player);
        ijkMediaPlayer = new IjkMediaPlayer();
        psv_player.setPlayer(ijkMediaPlayer);

        ijkMediaPlayer.setOnVideoSizeChangedListener(this);
        ijkMediaPlayer.setOnBufferingUpdateListener(this);
        ijkMediaPlayer.setOnInfoListener(this);
        ijkMediaPlayer.setOnErrorListener(this);
        ijkMediaPlayer.setOnCompletionListener(this);
        try {
            ijkMediaPlayer.setDataSource(playUrl);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ijkMediaPlayer.start();
        showLoading();
    }


    private void showLoading() {
        pb_progress.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        pb_progress.setVisibility(View.GONE);
    }

    private void showOverlay() {
        rl_overlay.setVisibility(View.VISIBLE);
        isShowingOverlay = true;
        mHandler.removeMessages(FADE_OUT);
        Message msg = mHandler.obtainMessage(FADE_OUT);
        mHandler.sendMessageDelayed(msg, OVERLAY_TIMEOUT);
    }

    private void hideOverlay() {
        rl_overlay.setVisibility(View.GONE);
        isShowingOverlay = false;
    }

    private void updateTextInfo() {
//            tv_channel_name.setText(channelString);
//            tv_channel_program.setText(channelString);
    }

    private final Handler mHandler = new VideoPlayerHandler(this);


    private static class VideoPlayerHandler extends WeakHandler<BiliFloatingView> {
        public VideoPlayerHandler(BiliFloatingView owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            BiliFloatingView activity = getOwner();
            if (activity == null) // WeakReference could be GC'ed early
                return;

            switch (msg.what) {
                case FADE_OUT:
                    activity.hideOverlay();
                    break;
                case SURFACE_SIZE:
                    activity.changeSurfaceSize();
                    break;
                case FIX_SIZE:
                    activity.fixViewSize();
                    break;
            }
        }
    }

    ;

    private boolean IsDouble = false;

    private boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    IsDouble = false;
                }
                if (IsDouble == false) {
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                }
                showOverlay();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1) {
                    IsDouble = true;
                }
                if (IsDouble == false) {
                    int deltaX = (int) (event.getRawX() - lastX);
                    lastX = event.getRawX();
                    int deltaY = (int) (event.getRawY() - lastY);
                    lastY = event.getRawY();
                    updateViewPosition(deltaX, deltaY);
                }
                break;

            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }

    private void changeSurfaceSize() {
        // force surface buffer size
        psv_player.getHolder().setSizeFromLayout();
    }

    private void updateViewPosition(int deltaX, int deltaY) {// 待优化
        int newX = wmParams.x + deltaX;
        int newY = wmParams.y + deltaY;
        int leftX = newX + (ScreenWidth - wmParams.width) / 2;
        int rightX = newX + (ScreenWidth + wmParams.width) / 2;
        int topY = newY + (ScreenHeight - wmParams.height) / 2;
        int bottomY = newY + (ScreenHeight + wmParams.height) / 2;
        if (leftX < 0) {
            newX = (wmParams.width - ScreenWidth) / 2;
            leftX = newX + (ScreenWidth - wmParams.width) / 2;
            rightX = newX + (ScreenWidth + wmParams.width) / 2;
        }
        if (rightX > ScreenWidth + 2) {
            newX = (ScreenWidth - wmParams.width) / 2;
            leftX = newX + (ScreenWidth - wmParams.width) / 2;
            rightX = newX + (ScreenWidth + wmParams.width) / 2;
        }
        if (topY < 0) {
            newY = (wmParams.height - ScreenHeight) / 2;
            topY = newY + (ScreenHeight - wmParams.height) / 2;
            bottomY = newY + (ScreenHeight + wmParams.height) / 2;
        }
        if (bottomY > ScreenHeight) {
            newY = (ScreenHeight - wmParams.height) / 2;
            topY = newY + (ScreenHeight - wmParams.height) / 2;
            bottomY = newY + (ScreenHeight + wmParams.height) / 2;
        }
        boolean xmove = false;
        boolean ymove = false;
        if ((leftX >= 0 || deltaX > 0) && (rightX <= ScreenWidth || deltaX < 0)) {
            xmove = true;
        }
        if ((topY >= 0 || deltaY > 0) && (bottomY <= ScreenHeight || deltaY < 0)) {
            ymove = true;
        }
        if (!xmove && !ymove) {// 不移动
            return;
        }
        // 移动
        if (xmove) {
            wmParams.x = newX;
        }
        if (ymove) {
            wmParams.y = newY;
        }
        wm.updateViewLayout(mlayoutView, wmParams);
    }

    private void updateViewSize(int w) {// 待优化
        float r = ratio == 0 ? default_ratio : ratio;
        int newW = w;
        int newH = 0;
        if (newW < MinWindowWidth) {
            if (newW != MinWindowWidth) {
                newW = MinWindowWidth;
            } else {
                return;
            }
        }
        newH = (int) (newW * r);
        if (newW >= ScreenWidth)// 宽度大于屏幕宽，直接置中，设为屏幕宽
        {
            if (wmParams.width != ScreenWidth) {
                wmParams.width = ScreenWidth;
                wmParams.height = (int) (ScreenWidth * r);
                mWindowWidth = wmParams.width;
                mWindowHeight = wmParams.height;
                wmParams.x = 0;
                wm.updateViewLayout(mlayoutView, wmParams);
                psv_player.getHolder().setSizeFromLayout();
            }
            return;
        }
        // 宽度小于屏幕宽
        int leftX = wmParams.x + (ScreenWidth - newW) / 2;
        int rightX = wmParams.x + (ScreenWidth + newW) / 2;
        int topY = wmParams.y + (ScreenHeight - newH) / 2;
        int bottomY = wmParams.y + (ScreenHeight + newH) / 2;
        wmParams.width = newW;
        wmParams.height = newH;
        mWindowWidth = newW;
        mWindowHeight = newH;
        if (leftX < 0) {
            wmParams.x = (newW - ScreenWidth) / 2;
        } else if (rightX > ScreenWidth) {
            wmParams.x = (ScreenWidth - newW) / 2;
        }
        if (topY < 0) {
            wmParams.y = (newH - ScreenHeight) / 2;
        } else if (bottomY > ScreenHeight) {
            wmParams.y = (ScreenHeight - newH) / 2;
        }
        wm.updateViewLayout(mlayoutView, wmParams);
        psv_player.getHolder().setSizeFromLayout();
    }

    public void fixViewPostion() {
        ScreenWidth = currentDisplay.getWidth();
        ScreenHeight = currentDisplay.getHeight() - stateBarHeight;
        wmParams.x = 0;
        wmParams.y = (wmParams.height - ScreenHeight) / 2;
        wm.updateViewLayout(mlayoutView, wmParams);
    }

    private void fixViewSize() {
        float r = ratio == 0 ? default_ratio : ratio;
        int newW = wmParams.width;
        int newH = (int) (newW * r);

        int leftX = wmParams.x + (ScreenWidth - newW) / 2;
        int rightX = wmParams.x + (ScreenWidth + newW) / 2;
        int topY = wmParams.y + (ScreenHeight - newH) / 2;
        int bottomY = wmParams.y + (ScreenHeight + newH) / 2;
        wmParams.width = newW;
        wmParams.height = newH;
        mWindowWidth = newW;
        mWindowHeight = newH;
        if (leftX < 0) {
            wmParams.x = (newW - ScreenWidth) / 2;
        } else if (rightX > ScreenWidth) {
            wmParams.x = (ScreenWidth - newW) / 2;
        }
        if (topY < 0) {
            wmParams.y = (newH - ScreenHeight) / 2;
        } else if (bottomY > ScreenHeight) {
            wmParams.y = (ScreenHeight - newH) / 2;
        }
        wm.updateViewLayout(mlayoutView, wmParams);
        psv_player.getHolder().setSizeFromLayout();
    }

    private void quit() {
        try {
            wm.removeView(mlayoutView);
        } catch (Exception e) {

        }
        try {
            ijkMediaPlayer.stop();
            ijkMediaPlayer.release();
        } catch (Exception e) {

        }
        mService.stopSelf();
    }

    class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        int lastw;
        float lastScale = 1;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            if (Math.abs(scale - lastScale) > TOLERATION) {
                updateViewSize((int) (lastw * scale));
                lastScale = scale;
            }
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastScale = 1;
            lastw = wmParams.width;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        Toast.makeText(mService, "播放完毕,退出", Toast.LENGTH_LONG).show();
        quit();
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        quit();
        Toast.makeText(mService, "播放器出错,退出", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        if (!mHasVout) {
            mHasVout = true;
            setBeginPlaying(true);
            hideLoading();
        }
    }


    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        mVideoWidth = width;
        mVideoHeight = height;
        mSarDen = sar_den;
        mSarNum = sar_num;
        if (width * height == 0)
            return;
        mVideoHeight = height;
        mVideoWidth = width;
        if (ratio == 0) {
            ratio = mVideoHeight / (float) mVideoWidth;
            Message msg = mHandler.obtainMessage(FIX_SIZE);
            mHandler.sendMessage(msg);
        }
        Message msg = mHandler.obtainMessage(SURFACE_SIZE);
        mHandler.sendMessage(msg);
    }

    private boolean isBeginPlaying = false;

    private void setBeginPlaying(boolean status) {
        isBeginPlaying = status;
    }
}