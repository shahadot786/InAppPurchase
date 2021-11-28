package com.billing.inappbillingtest;


import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.billing.inappbillingtest.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    View hiddenView;
    ImageView lock_key;
    AdView removeAd;
    ActivityMainBinding binding;
    TextView textStatus, productName, productDescription, productPrice;
    Button btnUpgrade;
    private BillingClient billingClient;
    public static final String PREF_FILE = "MyPref";
    public static final String PURCHASE_KEY = "billing_test_two";
    public static final String PRODUCT_ID = "billing_test_two";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getResources().getString(R.string.app_id));
        //binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //all the code bellow here
        //ID
        hiddenView = (View) findViewById(R.id.hidden_view);
        lock_key = (ImageView) findViewById(R.id.lock_key);
        removeAd = (AdView) findViewById(R.id.adView);
        //billing id
        textStatus = findViewById(R.id.tv_premium);
        productName = findViewById(R.id.productName);
        productDescription = findViewById(R.id.productDescription);
        productPrice = findViewById(R.id.productPrice);
        btnUpgrade = findViewById(R.id.btnUpgrade);
        //load ads
        AdRequest adRequest = new AdRequest.Builder().build();
        removeAd.loadAd(adRequest);
        //initialize ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });
        //ad listener
        removeAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
        //billing client listener
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
                        @Override
                        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> myPurchase) {
                            if (!myPurchase.isEmpty()) {
                                handlePurchases(myPurchase);
                            }
                            //if purchase list is empty that means item is not purchased
                            //Or purchase is refunded or canceled
                            else {
                                savePurchaseValueToPref(false);
                            }
                        }
                    });
                    //List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });

        //initiate purchase on button click
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if service is already connected
                if (billingClient.isReady()) {
                    initiatePurchase();
                }
                //else reconnect service
                else {
                    billingClient = BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(MainActivity.this).build();
                    billingClient.startConnection(new BillingClientStateListener() {
                        @Override
                        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                initiatePurchase();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error) + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onBillingServiceDisconnected() {
                        }
                    });
                }
            }
        });

        //item Purchased
        if (getPurchaseValueFromPref()) {
            btnUpgrade.setVisibility(View.GONE);
            textStatus.setText(getResources().getString(R.string.status_purchased));
            removeAd.setVisibility(View.GONE);
            hiddenView.setVisibility(View.GONE);
            lock_key.setVisibility(View.GONE);
        }
        //item not Purchased
        else {
            btnUpgrade.setVisibility(View.VISIBLE);
            textStatus.setText(getResources().getString(R.string.status_not_purchased));
            removeAd.setVisibility(View.VISIBLE);
            hiddenView.setVisibility(View.VISIBLE);
            lock_key.setVisibility(View.VISIBLE);
        }

    }//end of OnCreate

    //other codes bellow here

    private SharedPreferences getPreferenceObject() {
        return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
    }

    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }

    private boolean getPurchaseValueFromPref() {
        return getPreferenceObject().getBoolean(PURCHASE_KEY, false);
    }

    private void savePurchaseValueToPref(boolean value) {
        getPreferenceEditObject().putBoolean(PURCHASE_KEY, value).commit();
    }

    //get product details


    private void initiatePurchase() {
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                /*SkuDetails itemInfo = skuDetailsList.get(0);
                                productName.setText(itemInfo.getTitle());
                                productDescription.setText(itemInfo.getDescription());
                                productPrice.setText(itemInfo.getPrice());*/
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                billingClient.launchBillingFlow(MainActivity.this, flowParams);
                            } else {
                                //try to add item/product id "purchase" inside managed product in google play console
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.purchase_item_not_found), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.error) + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item newly purchased
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
            //successful message
            Toast.makeText(MainActivity.this, getResources().getString(R.string.successful_purchase_message), Toast.LENGTH_SHORT).show();
            btnUpgrade.setVisibility(View.GONE);
            textStatus.setText(getResources().getString(R.string.status_purchased));
            removeAd.setVisibility(View.GONE);
            hiddenView.setVisibility(View.GONE);
            lock_key.setVisibility(View.GONE);
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> myPurchase) {

                }
            });

        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show();
        }
        // Handle any other error msgs
        else {
            Toast.makeText(getApplicationContext(),  getResources().getString(R.string.error) + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void handlePurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            //if item is purchased
            if (PRODUCT_ID.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    // Invalid purchase
                    // show error to user
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_purchased), Toast.LENGTH_SHORT).show();
                    return;
                }
                // else purchase is valid
                //if item is purchased and not acknowledged
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                }
                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if (!getPurchaseValueFromPref()) {
                        savePurchaseValueToPref(true);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.item_purchased), Toast.LENGTH_SHORT).show();
                        this.recreate();
                    }
                }
            }
            //if purchase is pending
            else if (PRODUCT_ID.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.pending_purchased), Toast.LENGTH_SHORT).show();
            }
            //if purchase is unknown
            else if (PRODUCT_ID.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                savePurchaseValueToPref(false);
                textStatus.setText(getResources().getString(R.string.status_not_purchased));
                btnUpgrade.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.purchase_status_unknown), Toast.LENGTH_SHORT).show();
            }
        }
    }

    AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
                savePurchaseValueToPref(true);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.item_purchased), Toast.LENGTH_SHORT).show();
                MainActivity.this.recreate();
            }
        }
    };
    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = getResources().getString(R.string.play_console_license_key);
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

}