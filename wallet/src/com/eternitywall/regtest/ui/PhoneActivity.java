package com.eternitywall.regtest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.bitcoinj.core.Address;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.eternitywall.regtest.Constants.EW_API_KEY;
import static com.eternitywall.regtest.Constants.EW_URL;

public class PhoneActivity extends AbstractBindServiceActivity {

    EditText etPhone;
    Button btnPhoneVerify, btnPhoneSkip;


    Wallet wallet;
    WalletApplication application;
    Configuration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        etPhone = (EditText) findViewById(R.id.etPhone);
        btnPhoneVerify = (Button) findViewById(R.id.btnPhoneVerify);
        btnPhoneSkip = (Button) findViewById(R.id.btnPhoneSkip);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        btnPhoneVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number = etPhone.getText().toString();
                if (number != null && number.length() > 8) {
                    // pass checking
                    phoneSendSms(number);

                } else {
                    // phone invalid
                    new AlertDialog.Builder(PhoneActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.phone_verification_invalid))
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ;
                                }
                            })
                            .show();
                }

            }
        });

        btnPhoneSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneActivity.this.finish();
            }
        });
    }

    private void phoneSendSms(String phone) {
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", EW_API_KEY);
        client.post(EW_URL + "/sendsms/" + phone, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);
                phoneInsertPin();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progress(false);
                Toast.makeText(PhoneActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void phoneInsertPin() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PhoneActivity.this);
        LayoutInflater inflater = PhoneActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.popup_phone, null);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        final EditText etPin = (EditText) dialogView.findViewById(R.id.etPin);
        Button btnConfirm = (Button) dialogView.findViewById(R.id.btnPhoneConfirm);
        Button btnSkip = (Button) dialogView.findViewById(R.id.btnPhoneSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // PIN verify : remote call
                phoneVerify(etPhone.getText().toString(), etPin.getText().toString(), wallet.currentReceiveAddress());

            }
        });
    }

    private void phoneVerify(String phone, String secret, Address address) {
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", EW_API_KEY);
        RequestParams params = new RequestParams();
        params.add("secret_code", secret);
        params.add("genesis_address", address.toBase58().toString());
        client.post(EW_URL + "/verify/" + phone, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);

                SharedPreferences prefs = getSharedPreferences("com.eternitywall.regtest", MODE_PRIVATE);
                prefs.edit().putBoolean("phone_verification", true).apply();

                new AlertDialog.Builder(PhoneActivity.this)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.verification_success))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PhoneActivity.this.finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progress(false);
                Toast.makeText(PhoneActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ProgressDialog progressDialog = null;

    private void progress(boolean visible) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        }
        if (visible == true) {
            progressDialog.show();
        } else
            progressDialog.dismiss();
    }

}
