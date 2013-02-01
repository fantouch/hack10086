
package com.useease.hack10086.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.useease.hack10086.application.MyApp;
import com.useease.hack10086.R;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.File;

/**
 * @author Fantouch
 */
/**
 * @author Fantouch
 */
public class SimulateLoginActivity extends Activity {
    private final String TAG = SimulateLoginActivity.class.getSimpleName();
    private EditText etTel, etPsw, etVerificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        initCookie();
    }

    private void initUI() {
        setContentView(R.layout.simulate_login_activity);
        etTel = (EditText) findViewById(R.id.etTel);
        etPsw = (EditText) findViewById(R.id.etPsw);
        etVerificationCode = (EditText) findViewById(R.id.etVerificationCode);
    }

    private void initCookie() {
        // 排除旧的Cookies干扰,否则有可能提示系统繁忙
        MyApp.getInstance().clearCookies();

        MyApp.getInstance().getFinalHttp("gbk")
                .get(MyApp.URL_LOGIN_PAGE, new AjaxCallBack<String>() {
                    @Override
                    public void onStart() {
                        MyApp.getInstance().showProgressDialog(SimulateLoginActivity.this,
                                "初始化Cookie...");
                    }

                    @Override
                    public void onSuccess(String s) {
                        Toast.makeText(SimulateLoginActivity.this, s.trim(), Toast.LENGTH_LONG)
                                .show();
                        refreshVerificationCode(null);
                    }

                    @Override
                    public void onFailure(Throwable t, String strMsg) {
                        MyApp.getInstance().hideProgressDialog();
                        Log.e(TAG, strMsg, t);
                    }
                });

    }

    /**
     * 刷新验证码按钮点击
     * 
     * @param btn
     */
    public void refreshVerificationCode(View btn) {
        String imgPath = getCacheDir().getAbsolutePath() + File.separator
                + "VerificationCode.jpg";
        MyApp.getInstance()
                .getFinalHttp("utf-8")
                .download(MyApp.URL_VRFCODE + Math.random(), imgPath,
                        getVerificationCodeHeaders(),
                        new AjaxCallBack<File>() {
                            @Override
                            public void onStart() {
                                MyApp.getInstance().showProgressDialog(SimulateLoginActivity.this,
                                        "获取验证码...");
                            }

                            @Override
                            public void onSuccess(File file) {
                                MyApp.getInstance().hideProgressDialog();
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                ImageView imageView = (ImageView) SimulateLoginActivity.this
                                        .findViewById(R.id.ivVerificationCode);
                                imageView.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onFailure(Throwable t, String strMsg) {
                                MyApp.getInstance().hideProgressDialog();
                                Log.e(TAG, strMsg, t);
                            }
                        });

    }

    private Header[] getVerificationCodeHeaders() {
        Header[] headers = new BasicHeader[3];
        headers[0] = new BasicHeader("Referer", MyApp.URL_LOGIN_PAGE);
        headers[1] = new BasicHeader("Host", "gd.10086.cn");
        headers[2] = new BasicHeader("Accept", "image/png,image/*;q=0.8,*/*;q=0.5");
        return headers;
    }

    /**
     * 提交按钮点击
     * 
     * @param v
     */
    public void loginPost(View v) {
        if (verifyInput()) {

            String name = etTel.getText().toString().trim();
            String psw = etPsw.getText().toString().trim();
            String vCode = etVerificationCode.getText().toString().trim();

            LoginPostBean bean = new LoginPostBean();
            bean.get_loginInfo().set_logonName(name);
            bean.get_loginInfo().set_password(psw);
            bean.get_loginInfo().set_imageCode(vCode);

            String postString = new Gson().toJson(bean, LoginPostBean.class);

            MyApp.getInstance()
                    .getFinalHttp("utf-8")
                    .post(MyApp.URL_LOGIN, getLoginHeaders(),
                            new AjaxParams("_request_json", postString),
                            "application/x-www-form-urlencoded; charset=UTF-8",
                            new AjaxCallBack<String>() {
                                @Override
                                public void onStart() {
                                    MyApp.getInstance().showProgressDialog(
                                            SimulateLoginActivity.this,
                                            "正在登录...");
                                }

                                @Override
                                public void onSuccess(String t) {
                                    MyApp.getInstance().hideProgressDialog();

                                    LoginResultBean resultBean = new Gson().fromJson(t,
                                            LoginResultBean.class);

                                    Toast.makeText(SimulateLoginActivity.this,
                                            resultBean.getMessage(),
                                            Toast.LENGTH_SHORT).show();

                                    if (resultBean.isSucc()) {
                                        loginCallNoticePost(resultBean);
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t, String strMsg) {
                                    MyApp.getInstance().hideProgressDialog();
                                    Log.e(TAG, strMsg, t);
                                }
                            });
        }

    }

    /**
     * 模仿浏览器行为,登录后紧接着的操作(1/4)
     */
    private void loginCallNoticePost(final LoginResultBean resultBean) {

        LoginCallNoticeBean callNoticeBean = new LoginCallNoticeBean();
        callNoticeBean.setToken(resultBean.get_token());
        callNoticeBean.set_login_backurl_really(resultBean.get_login_backurl());
        callNoticeBean.setMobileType(resultBean.get_mobileType());
        callNoticeBean.setOperation(resultBean.get_operation());

        String postString = new Gson().toJson(callNoticeBean, LoginCallNoticeBean.class);

        MyApp.getInstance()
                .getFinalHttp("gbk")
                .post(MyApp.URL_LOGIN_CALL_NOTICE, getLoginCallNoticeHeaders(),
                        new AjaxParams("_request_json", postString),
                        "application/x-www-form-urlencoded",
                        new AjaxCallBack<String>() {
                            @Override
                            public void onStart() {
                                MyApp.getInstance().showProgressDialog(
                                        SimulateLoginActivity.this,
                                        "LoginCallNotice...");
                            }

                            @Override
                            public void onSuccess(String t) {
                                MyApp.getInstance().hideProgressDialog();

                                Toast.makeText(SimulateLoginActivity.this, t,
                                        Toast.LENGTH_SHORT).show();

                                ssoNgcrm(resultBean);
                            }

                            @Override
                            public void onFailure(Throwable t, String strMsg) {
                                MyApp.getInstance().hideProgressDialog();
                                Log.e(TAG, strMsg, t);
                            }
                        });

    }

    /**
     * 模仿浏览器行为,登录后紧接着的操作(2/4)
     */
    private void ssoNgcrm(final LoginResultBean resultBean) {

        SsoBean ssoBean = new SsoBean();
        ssoBean._id = resultBean.get_token();
        ssoBean._operation = resultBean.get_operation();
        ssoBean._portalCode = "bascNB";

        MyApp.getInstance()
                .getFinalHttp("gbk").get(MyApp.URL_SSO_NGCRM + ssoBean.toString(),
                        getSsoHeaders(), null,
                        new AjaxCallBack<String>() {
                            @Override
                            public void onStart() {
                                MyApp.getInstance().showProgressDialog(
                                        SimulateLoginActivity.this,
                                        "ssoNgcrm...");
                            }

                            @Override
                            public void onSuccess(String t) {
                                MyApp.getInstance().hideProgressDialog();

                                Toast.makeText(SimulateLoginActivity.this, t,
                                        Toast.LENGTH_SHORT).show();

                                Log.i(TAG, "ssoNgcrm result = " + t);

                                ssoCommodity(resultBean);
                            }

                            @Override
                            public void onFailure(Throwable t, String strMsg) {
                                MyApp.getInstance().hideProgressDialog();
                                Log.e(TAG, strMsg, t);
                            }
                        });
    }

    /**
     * 模仿浏览器行为,登录后紧接着的操作(3/4)
     */
    private void ssoCommodity(final LoginResultBean resultBean) {

        SsoBean ssoBean = new SsoBean();
        ssoBean._id = resultBean.get_token();
        ssoBean._operation = resultBean.get_operation();
        ssoBean._portalCode = "1200";

        MyApp.getInstance()
                .getFinalHttp("gbk").get(MyApp.URL_SSO_COMMODITY + ssoBean.toString(),
                        getSsoHeaders(), null,
                        new AjaxCallBack<String>() {
                            @Override
                            public void onStart() {
                                MyApp.getInstance().showProgressDialog(
                                        SimulateLoginActivity.this,
                                        "ssoCommodity...");
                            }

                            @Override
                            public void onSuccess(String t) {
                                MyApp.getInstance().hideProgressDialog();

                                Toast.makeText(SimulateLoginActivity.this, t,
                                        Toast.LENGTH_SHORT).show();

                                Log.i(TAG, "ssoCommodity result = " + t);

                                ssoPmarketing(resultBean);
                            }

                            @Override
                            public void onFailure(Throwable t, String strMsg) {
                                MyApp.getInstance().hideProgressDialog();
                                Log.e(TAG, strMsg, t);
                            }
                        });
    }

    /**
     * 模仿浏览器行为,登录后紧接着的操作(4/4)
     */
    private void ssoPmarketing(LoginResultBean resultBean) {

        SsoBean ssoBean = new SsoBean();
        ssoBean._id = resultBean.get_token();
        ssoBean._operation = resultBean.get_operation();
        ssoBean._portalCode = "discount";

        MyApp.getInstance()
                .getFinalHttp("gbk").get(MyApp.URL_SSO_PMARKETING + ssoBean.toString(),
                        getSsoHeaders(), null,
                        new AjaxCallBack<String>() {
                            @Override
                            public void onStart() {
                                MyApp.getInstance().showProgressDialog(
                                        SimulateLoginActivity.this,
                                        "ssoPmarketing...");
                            }

                            @Override
                            public void onSuccess(String t) {
                                Log.i(TAG, "ssoPmarketing result = " + t);
                                Toast.makeText(SimulateLoginActivity.this, t,
                                        Toast.LENGTH_SHORT).show();

                                MyApp.getInstance().hideProgressDialog();
                                MyApp.getInstance().saveCookies();

                                SimulateLoginActivity.this.finish();
                                startActivity(new Intent(SimulateLoginActivity.this,
                                        QueryBalanceActivity.class));
                            }

                            @Override
                            public void onFailure(Throwable t, String strMsg) {
                                MyApp.getInstance().hideProgressDialog();
                                Log.e(TAG, strMsg, t);
                            }
                        });
    }

    private Header[] getSsoHeaders() {
        Header[] headers = new BasicHeader[5];
        headers[0] = new BasicHeader("Referer", MyApp.URL_LOGIN_CALL_NOTICE);
        headers[1] = new BasicHeader("Host", "gd.10086.cn");
        headers[2] = new BasicHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers[3] = new BasicHeader("Accept-Language", "en-US,en;q=0.5");
        headers[4] = new BasicHeader("Accept-Encoding", "gzip, deflate");
        return headers;
    }

    private Header[] getLoginHeaders() {
        Header[] headers = new BasicHeader[3];
        headers[0] = new BasicHeader("Referer", MyApp.URL_LOGIN_PAGE);
        headers[1] = new BasicHeader("Host", "gd.10086.cn");
        headers[2] = new BasicHeader("Accept", "*/*");
        return headers;
    }

    private Header[] getLoginCallNoticeHeaders() {
        Header[] headers = new BasicHeader[3];
        headers[0] = new BasicHeader("Referer", MyApp.URL_LOGIN_CALL_NOTICE_REF);
        headers[1] = new BasicHeader("Host", "gd.10086.cn");
        headers[2] = new BasicHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return headers;
    }

    private boolean verifyInput() {
        if (etTel.getText().toString().trim().length() != 11) {
            Toast.makeText(this, "电话号码不足11位", Toast.LENGTH_SHORT).show();
            return false;
        } else if (etPsw.getText().toString().trim().length() == 0) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (etVerificationCode.getText().toString().trim().length() != 4) {
            Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }

    /**
     * 服务密码登录方式所需提交的内容
     * 
     * @author Fantouch
     */
    private class LoginPostBean {
        private String operation = "login";
        private boolean isProtocol = true;
        private String _dl100Mobile = "";
        private boolean _isfrompop = false;
        private LoginInfo _loginInfo = new LoginInfo();

        public LoginInfo get_loginInfo() {
            return _loginInfo;
        }

        private class LoginInfo {
            private String _loginType = "2";
            private String _logonName;
            private String _password;
            private String _smsRND = "";
            private String _imageCode;
            private String mobileip = null;
            private String isSso = null;
            private int _channel = 0;
            private String _login_backurl = "/personal/index.jsp";
            private String exp = "";

            public void set_logonName(String _logonName) {
                this._logonName = _logonName;
            }

            public void set_password(String _password) {
                this._password = _password;
            }

            public void set_imageCode(String _imageCode) {
                this._imageCode = _imageCode;
            }
        }
    }

    private class LoginResultBean {
        private String message = "";
        private String _operation = "";
        private String _token = "";
        private String _mobileType = "";
        private String _login_backurl = "";
        private boolean result = false;

        public String get_login_backurl() {
            return _login_backurl;
        }

        public String get_operation() {
            return _operation;
        }

        public String get_token() {
            return _token;
        }

        public String get_mobileType() {
            return _mobileType;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSucc() {
            return result;
        }

    }

    private class LoginCallNoticeBean {
        private String operation;
        private String token;
        private String logintype = "undefined";
        private String portalCode = "bsacNB";
        private String mobileType;
        private String _login_backurl_really;

        public void setMobileType(String mobileType) {
            this.mobileType = mobileType;
        }

        public void set_login_backurl_really(String _login_backurl_really) {
            this._login_backurl_really = _login_backurl_really;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    private class SsoBean {
        String _id;
        String _operation;
        String _portalCode;
        String _time = System.currentTimeMillis() + "";
        String _backURL = "http://gd.10086.cn/ngcrm/hall/servicearea/Balance/index.jsp";
        String _needJump = "0;";

        /**
         * 转换成httpGet的Url参数格式
         */
        @Override
        public String toString() {
            return "_id=" + _id + "&_time=" + _time + "&_operation=" + _operation + "&_portalCode="
                    + _portalCode + "&_backURL=" + _backURL + "&_needJump=" + _needJump;
        }
    }
}
