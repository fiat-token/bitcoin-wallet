package com.eternitywall.regtest.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;
import com.eternitywall.regtest.eternitywall.BitcoinEW;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.bitcoinj.core.Address;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class IbanActivity extends AbstractBindServiceActivity {

    EditText etPhone, etPrefix, etIban;
    Button btnVerify, btnSkip;
    TextView tvTerms;
    CheckBox cbTerms;

    Wallet wallet;
    WalletApplication application;
    Configuration config;
    String number, iban;
    Boolean numberIsDefined = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iban_activity);

        etPhone = (EditText) findViewById(R.id.etPhone);
        etPrefix = (EditText) findViewById(R.id.etPrefix);
        etIban = (EditText) findViewById(R.id.etIban);
        tvTerms = (TextView) findViewById(R.id.tvTerms);
        cbTerms = (CheckBox) findViewById(R.id.cbTerms);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        btnSkip = (Button) findViewById(R.id.btnSkip);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cbTerms.isChecked() == false) {
                    new AlertDialog.Builder(IbanActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.phone_verification_popup_terms_of_service))
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ;
                                }
                            })
                            .show();
                    return;
                }

                number = etPrefix.getText().toString() + etPhone.getText().toString();
                iban = etIban.getText().toString();
                if (number == null && number.length() < 8){
                    // phone invalid
                    new AlertDialog.Builder(IbanActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.phone_verification_invalid))
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ;
                                }
                            })
                            .show();
                    return ;
                } else if (iban == null && iban.length() < 8) {
                    // phone invalid
                    new AlertDialog.Builder(IbanActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.iban_verification_invalid))
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ;
                                }
                            })
                            .show();
                    return;
                } else {
                    // pass checking
                    ibanValidNumber(number);
                    return;
                }



            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IbanActivity.this.finish();
            }
        });

        tvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://eternitywall.com/pn2a/privacy/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

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



    private void ibanValidNumber(String phone) {
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", BitcoinEW.EW_API_KEY);
        client.get(BitcoinEW.EW_URL + "/address/" + phone, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);

                try {
                    if (response.getString("status").equals("ok")) {
                        numberIsDefined = true;
                    } else {
                        numberIsDefined = false;
                    }
                    phoneSendSms(number);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progress(false);

                numberIsDefined = false;
                phoneSendSms(number);
            }
        });
    }



    private void phoneSendSms(String phone) {
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", BitcoinEW.EW_API_KEY);
        client.post(BitcoinEW.EW_URL + "/sendsms/" + phone, new JsonHttpResponseHandler() {

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
                Toast.makeText(IbanActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void phoneInsertPin() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IbanActivity.this);
        LayoutInflater inflater = IbanActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.phone_popup, null);
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
                alertDialog.dismiss();
                phoneVerify(number, etPin.getText().toString(), wallet.currentReceiveAddress());


            }
        });
    }

    private void phoneVerify(String phone, String secret, Address address) {
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", BitcoinEW.EW_API_KEY);
        RequestParams params = new RequestParams();
        params.add("secret_code", secret);
        params.add("genesis_address", address.toBase58().toString());
        client.post(BitcoinEW.EW_URL + "/verify/" + phone, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);

                try {
                    if(response.getString("status").equals("ko")){
                        Toast.makeText(IbanActivity.this, getString(R.string.phone_verification_invalid), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // set preferences for UI
                    SharedPreferences prefs = getSharedPreferences("com.eternitywall.regtest", MODE_PRIVATE);
                    prefs.edit().putBoolean("phone_verification", true).apply();

                    // send coupon only if the phone number was not just registered
                    if(numberIsDefined == false){
                        RegisterAddress registerTask = new RegisterAddress(IbanActivity.this, wallet.currentReceiveAddress());
                        registerTask.startLoading();
                    }

                    // popup confirmation
                    new AlertDialog.Builder(IbanActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.verification_success))
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    IbanActivity.this.finish();
                                }
                            })
                            .setCancelable(false)
                            .show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progress(false);
                Toast.makeText(IbanActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ProgressDialog progressDialog = null;

    private void progress(boolean visible) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, R.style.CustomAlertDialog);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        }
        if (visible == true) {
            progressDialog.show();
        } else
            progressDialog.dismiss();
    }

}
