package com.psykar.cookiemanager;

import com.facebook.react.modules.network.ForwardingCookieHandler;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.webkit.CookieSyncManager;
import android.webkit.CookieManager;
import android.os.Build;

public class CookieManagerModule extends ReactContextBaseJavaModule {

    private ForwardingCookieHandler cookieHandler;
    private ReactApplicationContext context;

    public CookieManagerModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
        this.cookieHandler = new ForwardingCookieHandler(context);
    }

    public String getName() {
        return "RNCookieManagerAndroid";
    }

    @ReactMethod
    public void set(ReadableMap cookie, final Callback callback) throws Exception {
        String cookieString = "";

        if (cookie.getString("name") != null && cookie.getString("value") != null) {
            cookieString += cookie.getString("name") + "=" + cookie.getString("value");
        }

        if (cookie.getString("domain") != null) {
            cookieString += "; domain=" + cookie.getString("domain");
        }

        if (cookie.getString("path") != null) {
            cookieString += "; path=" + cookie.getString("path");
        }

        if (cookie.getString("expiration") != null) {
            cookieString += "; expires=Fri, 31 Mar 2035 00:00:00 -0000;";
        }

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();

        cookieManager.setCookie(cookie.getString("url"), cookieString);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager.getInstance().sync();
        }

        callback.invoke(null, null);
    }

    @ReactMethod
    public void setFromResponse(String url, String value, final Callback callback) throws URISyntaxException, IOException {
        Map headers = new HashMap<String, List<String>>();
        // Pretend this is a header

        headers.put("Set-cookie", Collections.singletonList(value));
        URI uri = new URI(url);
        this.cookieHandler.put(uri, headers);
        callback.invoke(null, null);
    }

    @ReactMethod
    public void getAll(Callback callback) throws Exception {
        throw new Exception("Cannot get all cookies on android, try getCookieHeader(url)");
    }

    @ReactMethod
    public void get(String url, Callback callback) throws URISyntaxException, IOException {
        URI uri = new URI(url);

        Map<String, List<String>> cookieMap = this.cookieHandler.get(uri, new HashMap());
        // If only the variables were public
        List<String> cookieList = cookieMap.get("Cookie");
        WritableMap map = Arguments.createMap();
        if (cookieList != null) {
            String[] cookies = cookieList.get(0).split(";");
            for (int i = 0; i < cookies.length; i++) {
                String[] cookie = cookies[i].split("=", 2);
                if(cookie.length > 1) {
                  map.putString(cookie[0].trim(), cookie[1]);
                }
            }
        }
        callback.invoke(null, map);
    }

    @ReactMethod
    public void clearAll(final Callback callback) {
        this.cookieHandler.clearCookies(new Callback() {
            public void invoke(Object... args) {
                callback.invoke(null, null);
            }
        });
    }
}
