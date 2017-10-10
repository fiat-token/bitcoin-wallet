package com.eternitywall.regtest.ui.eternitywall;

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
import com.eternitywall.regtest.eternitywall.Utils;
import com.eternitywall.regtest.ui.AbstractBindServiceActivity;
import com.eternitywall.regtest.ui.eternitywall.RegisterAddress;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.eternitywall.regtest.eternitywall.BitcoinEW.EW_API_KEY;
import static com.eternitywall.regtest.eternitywall.BitcoinEW.EW_URL;


public class PhoneActivity extends AbstractBindServiceActivity {

    EditText etPhone, etPrefix;
    Button btnPhoneVerify, btnPhoneSkip;
    TextView tvTerms;
    CheckBox cbTerms;

    Wallet wallet;
    WalletApplication application;
    Configuration config;
    String number;

    Boolean numberIsDefined = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_activity);

        etPhone = (EditText) findViewById(R.id.etPhone);
        etPrefix = (EditText) findViewById(R.id.etPrefix);
        tvTerms = (TextView) findViewById(R.id.tvTerms);
        cbTerms = (CheckBox) findViewById(R.id.cbTerms);
        btnPhoneVerify = (Button) findViewById(R.id.btnPhoneVerify);
        btnPhoneSkip = (Button) findViewById(R.id.btnPhoneSkip);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        btnPhoneVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cbTerms.isChecked() == false) {
                    new AlertDialog.Builder(PhoneActivity.this)
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
                if (number != null && number.length() > 8) {
                    // pass checking
                    phoneValidNumber(number);
                    return;

                }


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
        });

        btnPhoneSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneActivity.this.finish();
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



    private void phoneValidNumber(String phone) {
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", EW_API_KEY);
        client.get(EW_URL + "/address/" + phone, new JsonHttpResponseHandler() {

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
                phoneVerify(number, etPin.getText().toString());


            }
        });
    }

    private void phoneVerify(String phone, String secret) {
        progress(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", EW_API_KEY);
        RequestParams params = new RequestParams();
        params.add("secret_code", secret);
        params.add("genesis_address", wallet.currentReceiveAddress().toBase58().toString());
        params.add("xpub", String.valueOf(Hex.encodeHex(Utils.getDeterministicKey(wallet).getPubKey())));
        client.post(EW_URL + "/verify/" + phone, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);

                try {
                    if(response.getString("status").equals("ko")){
                        Toast.makeText(PhoneActivity.this, getString(R.string.phone_verification_invalid), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // set preferences for UI
                    SharedPreferences prefs = getSharedPreferences("com.eternitywall.regtest", MODE_PRIVATE);
                    prefs.edit().putBoolean("phone_verification", true).apply();

                    // send coupon only if the phone number was not just registered
                    if(numberIsDefined == false){
                        RegisterAddress registerTask = new RegisterAddress(PhoneActivity.this, wallet.currentReceiveAddress());
                        registerTask.startLoading();
                    }

                    // popup confirmation
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
