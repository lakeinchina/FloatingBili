package me.lake.floatingbili.WebView;

import android.webkit.JavascriptInterface;

public class InJavaScriptLocalObj {
    private LocalJavaScriptCallback callback = null;

    public void setCallback(LocalJavaScriptCallback callBack) {
        this.callback = callBack;
    }

    @JavascriptInterface
    public void resolveResult(String html) {
        if (null != callback) {
            callback.resolveResult(html);
        }
    }
}
