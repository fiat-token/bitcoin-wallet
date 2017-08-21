package com.eternitywall.regtest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.eternitywall.regtest.R;

public class PhoneActivity extends Activity {

    EditText etPhone;
    Button btnPhoneVerify, btnPhoneSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        etPhone = (EditText) findViewById(R.id.etPhone);
        btnPhoneVerify = (Button) findViewById(R.id.btnPhoneVerify);
        btnPhoneSkip = (Button) findViewById(R.id.btnPhoneSkip);

        btnPhoneVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number = etPhone.getText().toString();
                if (number!=null && number.length()>8){
                    // pass checking

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PhoneActivity.this);
                    LayoutInflater inflater = PhoneActivity.this.getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.popup_phone, null);
                    dialogBuilder.setView(dialogView);

                    final AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();

                    EditText etPin = (EditText) dialogView.findViewById(R.id.etPin);
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


                        }
                    });






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

}
