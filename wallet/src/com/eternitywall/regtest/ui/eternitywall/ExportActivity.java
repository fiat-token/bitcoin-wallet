package com.eternitywall.regtest.ui.eternitywall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;
import com.eternitywall.regtest.ui.AbstractBindServiceActivity;
import com.eternitywall.regtest.util.Toast;

import org.bitcoinj.wallet.Wallet;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by luca on 07/08/2017.
 */

public class ExportActivity extends AbstractBindServiceActivity {

    static String LAST_RECHARGE = "LAST_RECHARGE";

    Wallet wallet;
    WalletApplication application;
    Configuration config;
    ListView mListview;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.export_content);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        mListview = (ListView) findViewById(R.id.listView);

        if(wallet.isEncrypted()==true){
            alertPassword();
            return;
        }

        setTitle(getString(R.string.export_seed_dialog_title));
        showSeed(null);
    }

    private void alertPassword(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Password Required");   //title setted

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alert.setView(input);
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ;
            }
        });
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showSeed(input.getText().toString());
            }
        });
        alert.show();
    }

    private void showSeed(String password){
        try {
            List<String> strings = application.exportSeed(password);
            List<String> numberedString = new LinkedList<>();
            for(int i = 0 ; i<strings.size();i++) {
                numberedString.add((i+1) + ". " + strings.get(i));
            }
            ListAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, numberedString);
            mListview.setAdapter(adapter);
        }catch(Exception e){
            e.printStackTrace();
            android.widget.Toast.makeText(this, getString(R.string.export_seed_error), android.widget.Toast.LENGTH_LONG);
        }
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
