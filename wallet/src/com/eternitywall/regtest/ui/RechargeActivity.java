package com.eternitywall.regtest.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;

import org.bitcoinj.wallet.Wallet;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by luca on 07/08/2017.
 */

public class RechargeActivity extends  AbstractBindServiceActivity{

    Button btnRecharge;
    Button btnCancel;
    static String LAST_RECHARGE = "LAST_RECHARGE";

    Wallet wallet;
    WalletApplication application;
    Configuration config;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.recharge_content);


        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        btnRecharge = (Button) findViewById(R.id.recharge_go);
        btnCancel = (Button) findViewById(R.id.recharge_cancel);

        btnRecharge.setText(getString(R.string.recharge_activity_title));
        btnCancel.setText(getString(R.string.button_cancel));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RechargeActivity.this.finish();
            }
        });
        btnRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("com.eternitywall.regtest", MODE_PRIVATE);
                Long lastTimestamp = prefs.getLong(LAST_RECHARGE,0);
                Date lastDate = new Date(lastTimestamp);
                Date nowDate = new Date();
                if (lastDate.getYear() == nowDate.getYear() && lastDate.getMonth() == nowDate.getMonth() && lastDate.getDay() == nowDate.getDay()){
                    new AlertDialog.Builder(RechargeActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(R.string.invalid_recharge)
                            .setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                    return;
                }
                registerAddress();
                prefs.edit().putLong(LAST_RECHARGE, System.currentTimeMillis()).commit();
            }
        });

    }


    private void registerAddress(){
        RegisterAddress registerTask = new RegisterAddress(this, wallet.currentReceiveAddress());
        registerTask.startLoading();

        new AlertDialog.Builder(RechargeActivity.this)
                .setTitle(getString(R.string.app_name))
                .setMessage(R.string.sent_recharge)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
