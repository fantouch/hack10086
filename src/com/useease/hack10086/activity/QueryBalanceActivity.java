
package com.useease.hack10086.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.useease.hack10086.application.MyApp;
import com.useease.hack10086.R;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class QueryBalanceActivity extends Activity {
    private final String TAG = QueryBalanceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_balance_activity);
    }

    public void onClick_QueryBalance(View v) {
        MyApp.getInstance().getFinalHttp("gbk")
                .post(MyApp.URL_QUERY_BALANCE,
                        getQueryBalanceHeaders(),
                        new AjaxParams("isReRequest", "false"),
                        "application/x-www-form-urlencoded; charset=UTF-8",
                        new AjaxCallBack<String>() {
                            @Override
                            public void onStart() {
                                MyApp.getInstance().showProgressDialog(QueryBalanceActivity.this,
                                        "正在查询...");
                            }

                            @Override
                            public void onSuccess(String t) {
                                Log.i(TAG, "onSuccess : " + t);

                                TextView responseTxt = (TextView) findViewById(R.id.responseTxt);

                                if (t.contains("notlogin")) {
                                    responseTxt.setText("未登录,或登录信息无效");
                                } else {
                                    responseTxt.setText(Html.fromHtml(t));
                                }

                                MyApp.getInstance().killDialog();
                            }

                            @Override
                            public void onFailure(Throwable t, String strMsg) {
                                StringBuffer errMsg = new StringBuffer("QUERY_BALANCE_FAIL\n\n");

                                if (t != null) {
                                    t.printStackTrace();
                                    errMsg.append("Throwable:\n\n");
                                    errMsg.append(t.toString());
                                }
                                errMsg.append("\n\nstrMsg:\n\n");
                                errMsg.append(strMsg);

                                MyApp.getInstance().killDialog();

                                Toast.makeText(QueryBalanceActivity.this, errMsg, Toast.LENGTH_LONG)
                                        .show();

                                Log.i(TAG, "strMsg = " + strMsg);
                            }
                        });
    }

    private Header[] getQueryBalanceHeaders() {
        Header[] headers = new BasicHeader[8];

        headers[0] = new BasicHeader("Referer", MyApp.URL_QUERY_BALANCE_REF);
        headers[1] = new BasicHeader("Origin", "http://gd.10086.cn");
        headers[2] = new BasicHeader("Accept",
                "text/javascript, text/html, application/xml, text/xml, */*");
        headers[3] = new BasicHeader("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7");
        headers[4] = new BasicHeader("Accept-Encoding", "gzip");
        headers[5] = new BasicHeader("X-Requested-With", "XMLHttpRequest");
        headers[6] = new BasicHeader("X-Prototype-Version", "1.6.1");
        headers[7] = new BasicHeader("Accept-Language", "en-US,en;q=0.5");

        return headers;
    }
}
