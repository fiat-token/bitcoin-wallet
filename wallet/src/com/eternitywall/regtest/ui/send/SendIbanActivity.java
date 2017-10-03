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

package com.eternitywall.regtest.ui.send;

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

import com.eternitywall.regtest.Constants;
import com.eternitywall.regtest.R;
import com.eternitywall.regtest.data.PaymentIntent;
import com.eternitywall.regtest.ui.AbstractBindServiceActivity;
import com.eternitywall.regtest.ui.HelpDialogFragment;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.bitcoinj.core.Coin;
import org.json.JSONObject;

import java.util.Locale;

import javax.annotation.Nullable;

import cz.msebera.android.httpclient.Header;


/**
 * @author Andreas Schildbach
 */
public final class SendIbanActivity extends AbstractBindServiceActivity {
    public static final String INTENT_EXTRA_PAYMENT_INTENT = "payment_intent";
    public static final String INTENT_EXTRA_FEE_CATEGORY = "fee_category";
    public static final int PICK_CONTACT = 101;


    public static void start(final Context context, final PaymentIntent paymentIntent,
            final @Nullable FeeCategory feeCategory, final int intentFlags) {
        final Intent intent = new Intent(context, SendIbanActivity.class);
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

        setContentView(R.layout.send_iban_content);

        getWalletApplication().startBlockchainService(false);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        return super.onCreateOptionsMenu(menu);
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
