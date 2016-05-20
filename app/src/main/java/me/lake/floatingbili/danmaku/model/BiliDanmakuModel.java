package me.lake.floatingbili.danmaku.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lakeinchina(lakeinchina@hotmail.com) on 2015/11/26.
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
public class BiliDanmakuModel {
    String roomid;
    String cmd;
    DanmakuInfo danmakuinfo;
    UserInfo userInfo;
    String chat;

    static public BiliDanmakuModel fromJson(String jsonString) {
        BiliDanmakuModel result = null;
        try {
            JSONObject wholeJsonObject = new JSONObject(jsonString);
            result = new BiliDanmakuModel();
            result.setRoomid(wholeJsonObject.optString("roomid"));
            result.setCmd(wholeJsonObject.getString("cmd"));
            JSONArray infoJsonArray = wholeJsonObject.getJSONArray("info");
            JSONArray firstInfoJsonArray = infoJsonArray.getJSONArray(0);
            DanmakuInfo danmakuInfo = new DanmakuInfo();
            danmakuInfo.setAppearTime(firstInfoJsonArray.getLong(0));
            danmakuInfo.setMode(firstInfoJsonArray.getInt(1));
            danmakuInfo.setTextSize(firstInfoJsonArray.getInt(2));
            danmakuInfo.setColor(firstInfoJsonArray.getInt(3));
            danmakuInfo.setTimestamp(firstInfoJsonArray.getLong(4));
            danmakuInfo.setDanmakuPool(firstInfoJsonArray.getInt(6));
            danmakuInfo.setSenderId(firstInfoJsonArray.getString(7));
            result.setDanmakuinfo(danmakuInfo);
            result.setChat(infoJsonArray.getString(1));
            JSONArray thirdInfoJsonArray = infoJsonArray.getJSONArray(2);
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(thirdInfoJsonArray.getInt(0));
            userInfo.setUname(thirdInfoJsonArray.getString(1));
            result.setUserInfo(userInfo);
        } catch (JSONException e) {
            return null;
        }
        return result;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public DanmakuInfo getDanmakuinfo() {
        return danmakuinfo;
    }

    public void setDanmakuinfo(DanmakuInfo danmakuinfo) {
        this.danmakuinfo = danmakuinfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    static public class UserInfo {
        int uid;
        String uname;

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }
    }

    static public class DanmakuInfo {
        long appearTime;
        int mode;
        int textSize;
        int color;
        long timestamp;
        int danmakuPool;
        String senderId;

        public long getAppearTime() {
            return appearTime;
        }

        public void setAppearTime(long appearTime) {
            this.appearTime = appearTime;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public int getTextSize() {
            return textSize;
        }

        public void setTextSize(int textSize) {
            this.textSize = textSize;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getDanmakuPool() {
            return danmakuPool;
        }

        public void setDanmakuPool(int danmakuPool) {
            this.danmakuPool = danmakuPool;
        }

        public String getSenderId() {
            return senderId;
        }

        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
    }
}
/*
这个字段里面的内容：
0,1,25,16777215,1312863760,0,eff85771,42759017
中几个逗号分割的数据
第一个参数是弹幕出现的时间 以秒数为单位.
第二个参数是弹幕的模式1..3 滚动弹幕 4底端弹幕 5顶端弹幕 6.逆向弹幕 7精准定位 8高级弹幕
第三个参数是字号, 12非常小,16特小,18小,25中,36大,45很大,64特别大
第四个参数是字体的颜色 以HTML颜色的十位数为准
第五个参数是Unix格式的时间戳.基准时间为 1970-1-1 08:00:00
第六个参数是弹幕池 0普通池 1字幕池 2特殊池 【目前特殊池为高级弹幕专用】
第七个参数是发送者的ID,用于“屏蔽此弹幕的发送者”功能
第八个参数是弹幕在弹幕数据库中rowID 用于“历史弹幕”功能.
 */
