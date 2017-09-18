package com.eternitywall.regtest.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;

import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luca on 07/08/2017.
 */

public class ImportActivity extends  AbstractBindServiceActivity{

    Button btnRecharge;
    Button btnCancel;
    static String LAST_RECHARGE = "LAST_RECHARGE";

    Wallet wallet;
    WalletApplication application;
    Configuration config;
    EditText editText;
    Button btnImport;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.import_content);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        editText = (EditText) findViewById(R.id.editText);
        String seed = "polar tomorrow industry fuel harsh obvious embrace devote merry win notice recipe";
        editText.setText(seed);

        btnImport = (Button) findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(ImportActivity.this)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(R.string.import_seed_confirm)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                importSeed();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });

    }

    private void importSeed(){
        String seedCode = editText.getText().toString();
        Long creationtime = 1409478661L;
        String passphrase = "";

        try {
            application.importSeed(seedCode, passphrase, creationtime);
            success();
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
            failure();
        }
    }

    private void success(){
        new AlertDialog.Builder(ImportActivity.this)
                .setTitle(getString(R.string.app_name))
                .setMessage(R.string.import_seed_success)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ImportActivity.this.finish();
                    }
                }).show();
    }
    private void failure(){
        new AlertDialog.Builder(ImportActivity.this)
                .setTitle(getString(R.string.app_name))
                .setMessage(R.string.import_seed_failure)
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
