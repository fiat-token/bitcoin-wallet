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
import com.eternitywall.regtest.eternitywall.Utils;
import com.eternitywall.regtest.ui.AbstractBindServiceActivity;
import com.eternitywall.regtest.ui.CountryCodesAdapter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

import static com.eternitywall.regtest.eternitywall.BitcoinEW.EW_API_KEY;
import static com.eternitywall.regtest.eternitywall.BitcoinEW.EW_URL;


public class PhoneActivity extends AbstractBindServiceActivity {

    EditText etPhone;
    Button btnPhoneVerify, btnPhoneSkip;
    TextView tvTerms;
    CheckBox cbTerms;

    Wallet wallet;
    WalletApplication application;
    Configuration config;
    String number;

    Boolean numberIsDefined = false;
    private Spinner mCountryCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_activity);

        mCountryCode = (Spinner) findViewById(R.id.phone_cc);

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


        etPhone = (EditText) findViewById(R.id.etPhone);
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

                final CountryCodesAdapter.CountryCode selected = ccList.getSelected();

                number = "00" + selected.countryCode + etPhone.getText().toString();
                number = number.replace(" ","").replace("_","").replace("+","").replace(".","");
                log.info("Number is " + number);
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

    /**
     * Compatibility method for {@link PhoneNumberUtil#getSupportedRegions()}.
     * This was introduced because crappy Honeycomb has an old version of
     * libphonenumber, therefore Dalvik will insist on we using it.
     * In case getSupportedRegions doesn't exist, getSupportedCountries will be
     * used.
     */
    @SuppressWarnings("unchecked")
    private Set<String> getSupportedRegions(PhoneNumberUtil util) {

        try {
            return (Set<String>) util.getClass()
                    .getMethod("getSupportedRegions")
                    .invoke(util);
        }
        catch (NoSuchMethodException e) {
            try {
                return (Set<String>) util.getClass()
                        .getMethod("getSupportedCountries")
                        .invoke(util);
            }
            catch (Exception helpme) {
                // ignored
            }
        }
        catch (Exception e) {
            // ignored
        }
        return new HashSet<>();
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

    private void phoneVerify(final String phone, String secret) {
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
                    SharedPreferences prefs = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("phone_verification", true)
                            .putString("phone_number",phone)
                            .apply();

                    RegisterAddress registerTask = new RegisterAddress(PhoneActivity.this, phone);
                    registerTask.startLoading();

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
