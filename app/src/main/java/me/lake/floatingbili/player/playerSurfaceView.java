package me.lake.floatingbili.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
public class playerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    IjkMediaPlayer mediaPlayer;

    public playerSurfaceView(Context context) {
        super(context);
        init();
    }

    public playerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public playerSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPlayer(IjkMediaPlayer ijkMediaPlayer) {
        mediaPlayer = ijkMediaPlayer;
        mediaPlayer.setDisplay(getHolder());
    }


    private void init() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mediaPlayer.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
