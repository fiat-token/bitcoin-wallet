/*
 * Copyright 2011-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.eternitywall.regtest.ui.eternitywall;

import javax.annotation.Nullable;

import org.bitcoinj.core.Coin;
import org.json.JSONObject;

import com.eternitywall.regtest.Constants;
import com.eternitywall.regtest.data.PaymentIntent;
import com.eternitywall.regtest.ui.AbstractBindServiceActivity;
import com.eternitywall.regtest.ui.HelpDialogFragment;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.ui.send.FeeCategory;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

import cz.msebera.android.httpclient.Header;

import static com.eternitywall.regtest.eternitywall.BitcoinEW.EW_API_KEY;
import static com.eternitywall.regtest.eternitywall.BitcoinEW.EW_URL;


/**
 * @author Andreas Schildbach
 */
public final class SendCoinsActivity extends AbstractBindServiceActivity {
    public static final String INTENT_EXTRA_PAYMENT_INTENT = "payment_intent";
    public static final String INTENT_EXTRA_FEE_CATEGORY = "fee_category";
    public static final int PICK_CONTACT = 101;


    public static void start(final Context context, final PaymentIntent paymentIntent,
                             final @Nullable FeeCategory feeCategory, final int intentFlags) {
        final Intent intent = new Intent(context, SendCoinsActivity.class);
        intent.putExtra(INTENT_EXTRA_PAYMENT_INTENT, paymentIntent);
        if (feeCategory != null)
            intent.putExtra(INTENT_EXTRA_FEE_CATEGORY, feeCategory);
        if (intentFlags != 0)
            intent.setFlags(intentFlags);
        context.startActivity(intent);
    }

    public static void start(final Context context, final PaymentIntent paymentIntent) {
        start(context, paymentIntent, null, 0);
    }

    public static void startDonate(final Context context, final Coin amount, final @Nullable FeeCategory feeCategory,
            final int intentFlags) {
        start(context, PaymentIntent.from(Constants.DONATION_ADDRESS,
                context.getString(R.string.wallet_donate_address_label), amount), feeCategory, intentFlags);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.send_coins_content);

        getWalletApplication().startBlockchainService(false);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.send_coins_activity_options, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.send_coins_options_address_book:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, PICK_CONTACT);
                return true;
            case R.id.send_coins_options_help:
                HelpDialogFragment.page(getFragmentManager(), R.string.help_send_coins);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data){
        super.onActivityResult(reqCode, resultCode, data);

        switch(reqCode){
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK){
                    Uri contactData = data.getData();
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor c = getContentResolver().query(contactData, projection, null, null, null);

                    if (c.moveToFirst()){
                        //String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                        String phone = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Toast.makeText(getApplicationContext(), phone, Toast.LENGTH_SHORT).show();

                        try {
                            phone = normalizePhone(phone);
                            resolveToAddress(phone);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        }
    }


    private String address = null;
    public String getAddress(){
        return this.address;
    }

    private String normalizePhone(String phone) throws NumberParseException {
        final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber phoneNumber = null;
        phone = phone.replace("+","00");
        phone = phone.replace(" ","");
        boolean internationalFormat = false;
        for (Locale locale : Locale.getAvailableLocales()){
            int prefix = phoneNumberUtil.getCountryCodeForRegion(locale.getCountry());
            if(phone.startsWith("00"+String.valueOf(prefix))){
                internationalFormat = true;
                phoneNumber = phoneNumberUtil.parse(phone, locale.getCountry());
            }
        }
        if(internationalFormat==false){
            Locale locale = Locale.ITALY;
            int prefix = phoneNumberUtil.getCountryCodeForRegion(locale.getCountry());
            phoneNumber = phoneNumberUtil.parse(String.valueOf(prefix)+phone, locale.getCountry());
        }

        String number = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        number = number.replace("+","00");
        number = number.replace(" ","");
        return number;
    }

    private boolean isMobileNumber(final String phone) {
        if(phone.startsWith("0039") && phone.getBytes()[4]=='0'){
            return false;
        }else if(phone.startsWith("+39") && phone.getBytes()[3]=='0'){
            return false;
        }else if(phone.startsWith("39") && phone.getBytes()[2]=='0'){
            return false;
        }else if(phone.getBytes()[0]=='0'){
            return false;
        }
        return true;

    }
    private void resolveToAddress(final String phone) {
        address = null;
        progress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("api_key", EW_API_KEY);
        client.get(EW_URL + "/address/" + phone, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progress(false);
                try {
                    address = response.getJSONObject("value").getString("address");
                    Toast.makeText(SendCoinsActivity.this, address, Toast.LENGTH_SHORT).show();

                    SendCoinsFragment coinsFragment = (SendCoinsFragment) getFragmentManager().findFragmentById(R.id.send_coins_fragment);
                    coinsFragment.setAddress(phone, address);


                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(SendCoinsActivity.this, getString(R.string.phone_verification_user_not_found), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                progress(false);

                boolean mobileNumber = isMobileNumber(phone);
                if(mobileNumber==false){
                    Toast.makeText(SendCoinsActivity.this, getString(R.string.phone_verification_user_not_found), Toast.LENGTH_SHORT).show();
                    return;
                }
                // popup confirmation
                new AlertDialog.Builder(SendCoinsActivity.this)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.phone_verification_sendcoins_popup))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri uri = Uri.parse("smsto:"+phone);
                                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                                it.putExtra("sms_body", getString(R.string.phone_verification_sendcoins_sms));
                                startActivity(it);
                            }
                        })
                        .setCancelable(false)
                        .show();

            }
        });
    }

    ProgressDialog progressDialog = null;
    private void progress(boolean visible) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, R.style.CustomAlertDialog);
            progressDialog.setCancelable(false);
        }
        if (visible == true) {
            progressDialog.show();
        } else
            progressDialog.dismiss();
    }

}
