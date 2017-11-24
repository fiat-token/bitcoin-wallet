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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eternitywall.regtest.BuildConfig;
import com.eternitywall.regtest.Configuration;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.WalletApplication;
import com.eternitywall.regtest.eternitywall.BitcoinEW;
import com.eternitywall.regtest.eternitywall.Utils;
import com.eternitywall.regtest.ui.AbstractBindServiceActivity;
import com.eternitywall.regtest.ui.CountryCodesAdapter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import nl.garvelink.iban.IBAN;

public class IbanValidationActivity extends AbstractBindServiceActivity {

    EditText etPhone, etIban;
    Button btnVerify, btnSkip;
    TextView tvTerms;
    CheckBox cbTerms;
    Spinner mCountryCode;

    Wallet wallet;
    WalletApplication application;
    Configuration config;
    String number, iban;
    DeterministicKey deterministicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iban_validation_activity);

        etPhone = (EditText) findViewById(R.id.etPhone);
        etIban = (EditText) findViewById(R.id.etIban);
        tvTerms = (TextView) findViewById(R.id.tvTerms);
        cbTerms = (CheckBox) findViewById(R.id.cbTerms);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        btnSkip = (Button) findViewById(R.id.btnSkip);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();
        deterministicKey = Utils.getDeterministicKey(wallet);


        mCountryCode = (Spinner) findViewById(R.id.phone_cc_iban);

// populate country codes
        final CountryCodesAdapter ccList = new CountryCodesAdapter(this,
                android.R.layout.simple_list_item_1,
                android.R.layout.simple_spinner_dropdown_item);
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        Set<String> ccSet = util.getSupportedRegions();

        for (String cc : ccSet) {
            ccList.add(cc);
        }

        ccList.sort(new Comparator<CountryCodesAdapter.CountryCode>() {
            public int compare(CountryCodesAdapter.CountryCode lhs, CountryCodesAdapter.CountryCode rhs) {
                return lhs.regionName.compareTo(rhs.regionName);
            }
        });

        List<String> ordered = new LinkedList<>(ccSet);
        Collections.sort(ordered);
        mCountryCode.setAdapter(ccList);
        mCountryCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ccList.setSelected(position);
                log.info("Selected " + ccList.getSelected());
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        CountryCodesAdapter.CountryCode cc = new CountryCodesAdapter.CountryCode();
        cc.regionCode = "IT";
        cc.countryCode = 39;
        cc.regionName = "Italy";
        mCountryCode.setSelection(ccList.getPositionForId(cc));


        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cbTerms.isChecked() == false) {
                    new AlertDialog.Builder(IbanValidationActivity.this)
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

                final CountryCodesAdapter.CountryCode selected = ccList.getSelected();
                number = "00" + selected.countryCode + etPhone.getText().toString();
                number = number.replace(" ","").replace("_","").replace("+","").replace(".","");
                log.info("Number is " + number);
                iban = etIban.getText().toString();
                if (number == null || number.length() < 8){
                    // phone invalid
                    new AlertDialog.Builder(IbanValidationActivity.this)
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
                } else if (!validateIban()) {
                    // iban invalid
                    return;
                } else {
                    // pass checking
                    phoneSendSms(number);
                    return;
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IbanValidationActivity.this.finish();
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

        etIban.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b == false){
                    validateIban();
                }
            }
        });

        checkExistIban(deterministicKey.getPubKeyHash());
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


    private void checkExistIban(byte[] pubkey) {
        progress(true);


        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", BitcoinEW.EW_API_KEY);
        client.get(BitcoinEW.EW_URL + "/iban/get/" + Hex.encodeHex(pubkey).toString(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);


                // no association
                try {
                    if(response.getString("status").equals("ko")){
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                // retrieve local iban
                SharedPreferences prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
                String iban = prefs.getString("iban", null);
                String msg = getString(R.string.iban_verification_just_registered);
                if(iban != null ) {
                    msg += " : " + iban;
                }

                // popup confirmation
                new AlertDialog.Builder(IbanValidationActivity.this)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(msg)
                        .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                IbanValidationActivity.this.finish();
                            }
                        })
                        .setPositiveButton(getString(R.string.next),new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ;
                            }
                        })
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progress(false);
            }
        });
    }


    private boolean validateIban(){
        try {
            IBAN iban = IBAN.valueOf(etIban.getText().toString());
            if(iban.isSEPA()){
                Toast.makeText(this,getString(R.string.iban_sepa),Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(this,getString(R.string.iban_not_sepa),Toast.LENGTH_LONG).show();
                return false;
            }
        }catch (Exception e){
            Toast.makeText(this,getString(R.string.invalid_iban),Toast.LENGTH_LONG).show();
            return false;
        }
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
                Toast.makeText(IbanValidationActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void phoneInsertPin() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IbanValidationActivity.this);
        LayoutInflater inflater = IbanValidationActivity.this.getLayoutInflater();
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
                verify(etIban.getText().toString(), number, etPin.getText().toString(), deterministicKey.getPubKeyHash());


            }
        });
    }

    private void verify(final String iban, final String phone, final String secret, final byte[] pubKeyHash) {
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", BitcoinEW.EW_API_KEY);
        RequestParams params = new RequestParams();
        params.add("phone_number_hash", String.valueOf(Hex.encodeHex(Sha256Hash.hash(phone.getBytes()))));
        params.add("secret_code", secret);
        params.add("iban_hash", String.valueOf(Hex.encodeHex( Sha256Hash.hash(iban.getBytes()))));
        params.add("public_key_hash", String.valueOf(Hex.encodeHex(pubKeyHash)));

        client.post(BitcoinEW.EW_URL + "/iban/verify", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);

                try {
                    if(response.getString("status").equals("ko")){
                        Toast.makeText(IbanValidationActivity.this, getString(R.string.phone_verification_invalid), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // set preferences for UI
                    SharedPreferences prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
                    prefs.edit().putString("iban", iban).apply();

                    // popup confirmation
                    new AlertDialog.Builder(IbanValidationActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.iban_verification_success))
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    IbanValidationActivity.this.finish();
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
                Toast.makeText(IbanValidationActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
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
