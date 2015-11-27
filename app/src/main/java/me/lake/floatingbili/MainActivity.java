package me.lake.floatingbili;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.lake.floatingbili.WebView.InJavaScriptLocalObj;
import me.lake.floatingbili.WebView.LocalJavaScriptCallback;
import me.lake.floatingbili.player.floatingplay.BiliFloatingPlayService;

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
public class MainActivity extends AppCompatActivity {
    private static String liveSite = "http://live.bilibili.com/";
    private EditText et_roomid;
    private Button btn_parse;
    private FloatingActionButton fab_play;
    private Handler parseHadler;
    private ProgressBar pb_parseing;
    InJavaScriptLocalObj jScriptLocalObj;
    WebView wb_live;
    private TextView tv_url;
    private int roomId;
    private String playurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parseHadler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (wb_live != null) {
                            //wb_live.loadUrl("javascript:window.local_obj.resolveResult('<head>'+document.getElementsByClassName('player-box')[0].innerHTML+'</head>');");
                            wb_live.loadUrl("javascript:window.local_obj.resolveResult('<head>'+document.getElementsByTagName('body')[0].innerHTML+'</head>');");
                            parseHadler.sendEmptyMessageDelayed(1, 5000);
                        }
                        break;
                    case 2:
                        break;
                }
            }
        };
        initWebView();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pb_parseing = (ProgressBar) findViewById(R.id.pb_parseing);
        pb_parseing.setVisibility(View.INVISIBLE);
        tv_url = (TextView) findViewById(R.id.tv_url);
        fab_play = (FloatingActionButton) findViewById(R.id.fab);
        et_roomid = (EditText) findViewById(R.id.et_roomid);
        btn_parse = (Button) findViewById(R.id.btn_parse);
        btn_parse.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomid = et_roomid.getText().toString();
                if (!"".equals(roomid.trim())) {
                    wb_live.loadUrl(roomid);
                    wb_live.requestFocus();
                    pb_parseing.setVisibility(View.VISIBLE);
                }
            }
        });
        fab_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!"".equals(tv_url.getText())) {
                    Intent mIntent = new Intent();
                    mIntent.putExtra("playUrl", tv_url.getText());
                    mIntent.putExtra("roomID", roomId);
                    mIntent.setClass(MainActivity.this, BiliFloatingPlayService.class);
                    MainActivity.this.startService(mIntent);
                    MainActivity.this.finish();
                }
            }
        });
    }

    private void initWebView() {
        jScriptLocalObj = new InJavaScriptLocalObj();
        jScriptLocalObj.setCallback(new LocalJavaScriptCallback() {
            @Override
            public void resolveResult(String html) {
                if (!html.contains("source")) {
                    return;
                } else {
                    Pattern pat = Pattern.compile("<source src=\"\\S+\" type=\"video/mp4\">");
                    Matcher mat = pat.matcher(html);
                    if (mat.find()) {
                        String source = mat.group();
                        playurl = source.substring(13, source.length() - 19);
                    } else {
                        return;
                    }
                    parseHadler.removeCallbacksAndMessages(null);
                    pat = Pattern.compile("var room_id = \\d+;");
                    mat = pat.matcher(html);
                    if (mat.find()) {
                        String res = mat.group();
                        roomId = Integer.parseInt(res.substring(14, res.length() - 1));
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_url.setText(playurl);
                            pb_parseing.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }
        });
        wb_live = (WebView) findViewById(R.id.wb_live);
        WebSettings settings = wb_live.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        wb_live.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (null != view) {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!"".equals(url)) {
                    parseHadler.sendMessage(parseHadler.obtainMessage(1, view));
                }
                super.onPageFinished(view, url);
            }
        });
        wb_live.addJavascriptInterface(jScriptLocalObj, "local_obj");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != wb_live) {
            wb_live.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        parseHadler.removeCallbacksAndMessages(null);
        if (null != wb_live) {
            wb_live.stopLoading();
            wb_live.removeAllViews();
            wb_live.destroy();
            wb_live = null;
        }
    }
}
