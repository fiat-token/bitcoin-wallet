package com.eternitywall.regtest.ui.eternitywall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.eternitywall.regtest.BuildConfig;
import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;
import com.eternitywall.regtest.ui.AbstractBindServiceActivity;
import com.eternitywall.regtest.ui.eternitywall.RegisterAddress;

import org.bitcoinj.wallet.Wallet;

import java.util.Date;

/**
 * Created by luca on 07/08/2017.
 */

public class RechargeActivity extends AbstractBindServiceActivity {

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

        btnRecharge.setText(getString(R.string.button_top_up));
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

                final SharedPreferences prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
                boolean phoneVerification = prefs.getBoolean("phone_verification",false);
                if (phoneVerification == false){
                    new AlertDialog.Builder(RechargeActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(R.string.recharge_noregistration)
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(RechargeActivity.this, PhoneActivity.class));
                                }
                            }).show();
                    return ;
                }

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
        /*
        RegisterAddress registerTask = new RegisterAddress(this, wallet.currentReceiveAddress());
        registerTask.startLoading();
        */

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


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
