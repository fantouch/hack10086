
package com.useease.hack10086.application;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.tsz.afinal.FinalHttp;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.lang.reflect.Type;
import java.util.List;

public class MyApp extends Application {
    private final String TAG = MyApp.class.getSimpleName();
    /* URLS */
    public static final String URL_LOGIN_PAGE = "http://gd.10086.cn/common/include/public/dispatcher.jsp";
    public static final String URL_QUERY_BALANCE_REF = "http://gd.10086.cn/ngcrm/hall/servicearea/Balance/index.jsp";
    public static final String URL_QUERY_BALANCE = "http://gd.10086.cn/ngcrm/hall/servicearea/queryRateInfo.action";
    public static final String URL_VRFCODE = "http://gd.10086.cn/image?sds=";
    public static final String URL_LOGIN = "http://gd.10086.cn/ServicesServlet/LOGIN";
    public static final String URL_LOGIN_CALL_NOTICE = "http://gd.10086.cn/login/LoginCallNotice.jsp";
    public static final String URL_LOGIN_CALL_NOTICE_REF = "http://gd.10086.cn/common/include/public/dispatcher.jsp?_backURL=http://gd.10086.cn/ngcrm/hall/servicearea/Balance/index.jsp&_portalCode=bsacNB";
    public static final String URL_SSO_NGCRM = "http://gd.10086.cn/ngcrm/sso/callback?";
    public static final String URL_SSO_COMMODITY = "http://gd.10086.cn/commodity/sso/uapCallback/invok.jsps?";
    public static final String URL_SSO_PMARKETING = "http://gd.10086.cn/pmarketing/sso/uapCallback/invok.jsps?";

    private final String PRE_COOKIES = "cookies";
    private static MyApp mApp;
    private FinalHttp fh;
    private BasicCookieStore cookieStore;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        mApp = this;
    }

    private ProgressDialog pd;

    private ProgressDialog getProgressDialog(Context ctx, String msg) {
        pd = new ProgressDialog(ctx);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(false);
        pd.setMessage(msg);
        return pd;
    }

    public void showProgressDialog(Context ctx, String msg) {
        if (pd != null && pd.isShowing()) {
            hideProgressDialog();
        }
        getProgressDialog(ctx, msg).show();
    }

    public void hideProgressDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
            pd = null;
        }
    }

    public void saveCookies() {
        if (cookieStore != null) {
            List<Cookie> cookies = cookieStore.getCookies();

            Type type = new TypeToken<List<BasicClientCookie>>() {
            }.getType();

            String cookiesStr = new Gson().toJson(cookies, type);
            Log.i(TAG, "CookiesToBeSaved = " + cookiesStr);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putString(PRE_COOKIES, cookiesStr).commit();

            Log.i(TAG, "saveCookies()");
        }
    }

    private CookieStore getRestoredCookies() {
        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
            String cookiesStr = PreferenceManager.getDefaultSharedPreferences(this).getString(
                    PRE_COOKIES, "");
            Log.i(TAG, "CookiesToBeRestore = " + cookiesStr);

            if (cookiesStr != null && !cookiesStr.equals("")) {
                Type type = new TypeToken<List<BasicClientCookie>>() {
                }.getType();
                List<Cookie> cookies = new Gson().fromJson(cookiesStr, type);
                cookieStore.addCookies(cookies.toArray(new Cookie[cookies.size()]));

                Log.i(TAG, "RestoredCookiesSucc");
            } else {
                Log.i(TAG, "RestoredCookiesFail,will use new CookieStore");
            }
        }
        return cookieStore;
    }

    public void clearCookies() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        cookieStore = null;
        if (fh != null) {
            fh.configCookieStore(getRestoredCookies());
        }

        Toast.makeText(this, "清除Cookie成功", Toast.LENGTH_SHORT).show();
    }

    public static MyApp getInstance() {
        return mApp;
    }

    public FinalHttp getFinalHttp(String charset) {
        if (fh == null) {
            fh = new FinalHttp();
            fh.configTimeout(20 * 1000);// 20s
            fh.configCookieStore(getRestoredCookies());
            fh.addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        }
        fh.setCharset(charset);
        return fh;
    }

}
