package me.lake.floatingbili.danmaku;

import java.util.List;

import me.lake.live4danmaku.model.danmaku.BaseDanmaku;

/**
 * Created by lake on 15-11-26.
 */
public interface IIncomingDanmakuCallback {
    public void incomingDanmaku(BaseDanmaku danmaku);
    public void incomingDanmakus(List<BaseDanmaku> danmakus);
}
