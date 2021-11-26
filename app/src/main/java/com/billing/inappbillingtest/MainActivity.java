package com.billing.inappbillingtest;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.billing.inappbillingtest.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdView;


import java.util.List;


public class MainActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    View hiddenView;
    ImageView lock_key;
    AdView removeAd;
    ActivityMainBinding binding;
    private BillingProcessor bp;
    TextView textStatus;
    Button btnSubscribe;
    private TransactionDetails purchaseTransactionDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //all the code bellow here
        //ID
        hiddenView = (View)findViewById(R.id.hidden_view);
        lock_key = (ImageView) findViewById(R.id.lock_key);
        removeAd = (AdView) findViewById(R.id.adView);

        //billing id
        textStatus = findViewById(R.id.tv_premium);
        btnSubscribe = findViewById(R.id.btn_subscribe);

        //billing initialization
        bp = new BillingProcessor(this, getResources().getString(R.string.play_console_license), this);
        bp.initialize();



        //button on click method
        binding.visible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hiddenView.setVisibility(View.GONE);
                lock_key.setVisibility(View.GONE);
            }
        });
        binding.inVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hiddenView.setVisibility(View.VISIBLE);
                lock_key.setVisibility(View.VISIBLE);
            }
        });
        binding.adInvisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAd.setVisibility(View.GONE);
            }
        });
        binding.adVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAd.setVisibility(View.VISIBLE);
            }
        });

    }

    private boolean hasSubscription(){
        if (purchaseTransactionDetails != null){
            return purchaseTransactionDetails.purchaseInfo!= null;
        }
        return false;

    }

    @Override
    public void onBillingInitialized() {
        Log.d("MainActivity", "onBillingInitialized: ");

        String productID = getResources().getString(R.string.product_id);
        purchaseTransactionDetails = bp.getSubscriptionTransactionDetails(productID);

        //subscribe button click function
        btnSubscribe.setOnClickListener(v -> {
            if (bp.isSubscriptionUpdateSupported()){
                bp.subscribe(this, productID);
            }else{
                Log.d(TAG, "onBillingInitialized: Subscription update is not supported.");
            }
        });

        //text status changed functions and here the remove locked content or ad
        if (hasSubscription()){
            textStatus.setText("Status: Premium");
        }else{
            textStatus.setText("Status: Free");
        }

    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Log.d(TAG, "onProductPurchased: ");
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Log.d(TAG, "onBillingError: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

}