package com.pact41.lastnight.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pact41.lastnight.R;

public class QRCodeScanActivity extends MenuActivity {

    private Button scan;
    private EditText partyIdField;
    String partyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_scan);

        scan = (Button) findViewById(R.id.qrcode_scan_button);
        final Activity activity = this;
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }

        });
        final Button saisie;
        saisie = (Button)findViewById(R.id.qrcode_saisie_button);
        saisie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To be completed with an external QR code scan activity !
                partyId = partyIdField.getText().toString().trim();
                if(!partyId.matches(""))
                    startPartyProfileActivity(true, partyId);
                else
                    Toast.makeText(QRCodeScanActivity.this,
                            getResources().getString(R.string.qrcode_scan_party_not_found_message),
                            Toast.LENGTH_SHORT).show();
            }
        });
        partyIdField = (EditText)findViewById(R.id.qrcode_scan_party_id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Le scan a échoué", Toast.LENGTH_LONG).show();
            } else {
                partyId = result.getContents();
                if (!partyId.matches("")) {
                    startPartyProfileActivity(true, partyId.replace("\n", ""));
                } else {
                    Toast.makeText(QRCodeScanActivity.this,
                            getResources().getString(R.string.qrcode_scan_party_not_found_message),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    protected int getId() {
        return R.id.menu_item_current_party;
    }
}




