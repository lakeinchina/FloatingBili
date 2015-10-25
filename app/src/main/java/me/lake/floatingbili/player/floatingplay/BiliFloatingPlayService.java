package me.lake.floatingbili.player.floatingplay;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

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
public class BiliFloatingPlayService extends Service {
    private final static int BiliFloatPlayServiceID = 127001;
    public final static String TAG = "BiliFloatingPlayService";
    private BiliFloatingView sFloatView;
    private String channelId;

    private void createView(String playUrl) {
        sFloatView = new BiliFloatingView(this, playUrl);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String playUrl = null;
        if (null != intent) {
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                playUrl = bundle.getString("playUrl");
            }
        }
        if (sFloatView == null) {
            NotificationCompat.Builder NBuilder = new NotificationCompat.Builder(this).setSubText("BiliFloatingPlay");
            startForeground(BiliFloatPlayServiceID, NBuilder.build());

            createView(playUrl);
        } else {
            sFloatView.changeUrl(playUrl);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (sFloatView != null) {
            sFloatView.fixViewPostion();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
