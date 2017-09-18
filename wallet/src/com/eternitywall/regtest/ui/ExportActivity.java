package com.eternitywall.regtest.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;

import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by luca on 07/08/2017.
 */

public class ExportActivity extends  AbstractBindServiceActivity{

    Button btnRecharge;
    Button btnCancel;
    static String LAST_RECHARGE = "LAST_RECHARGE";

    Wallet wallet;
    WalletApplication application;
    Configuration config;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.export_content);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        List<String> strings = application.exportSeed();
        ListAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, strings);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);

        /* polar tomorrow industry fuel harsh obvious embrace devote merry win notice recipe */
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
