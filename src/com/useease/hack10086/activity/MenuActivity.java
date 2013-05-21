
package com.useease.hack10086.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.useease.hack10086.application.MyApp;
import com.useease.hack10086.R;

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            
            case R.id.btnSimulateLogin:
                startActivity(new Intent(this, SimulateLoginActivity.class));
                break;

            case R.id.btnClearCookie:
                MyApp.getInstance().clearCookies();
                break;

            case R.id.btnQueryBalance:
                startActivity(new Intent(this, QueryBalanceActivity.class));
                break;

            default:
                break;
        }
    }

}
