package com.billing.inappbillingtest;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    BillingClient billingClient;
    private Button btn1,btn6,btn12;
    private TextView text1,text6,text12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = findViewById(R.id.btn1Months);
        btn6 = findViewById(R.id.btn6Months);
        btn12 = findViewById(R.id.btn12Months);
        text1 = findViewById(R.id.item1Month);
        text6 = findViewById(R.id.item6Month);
        text12 = findViewById(R.id.item12Month);


        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null){
                            for (Purchase purchase: list){
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
                                {
                                    if (!purchase.isAcknowledged()){
                                        AcknowledgePurchaseParams acknowledgePurchaseParams
                                                = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                                        billingClient.acknowledgePurchase(
                                                acknowledgePurchaseParams,
                                                new AcknowledgePurchaseResponseListener() {
                                                    @Override
                                                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                                                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                                            Toast.makeText(MainActivity.this, "Purchase Acknowledge", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                        );



                                        //successful message
                                        Toast.makeText(MainActivity.this, "Welcome to Premium Version", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }
                        }else{
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
                                Toast.makeText(MainActivity.this, "Try Purchasing Again", Toast.LENGTH_SHORT).show();
                            }else {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
                                    Toast.makeText(MainActivity.this, "Already Purchased", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                })
                .build();
        connectToGooglePlayBilling();

    }

    @Override
    protected void onResume(){
        super.onResume();
        billingClient.queryPurchasesAsync(
                BillingClient.SkuType.INAPP,
                new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            for (Purchase purchase: list){
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                                    if (!purchase.isAcknowledged()){
                                        AcknowledgePurchaseParams acknowledgePurchaseParams
                                                = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                                        billingClient.acknowledgePurchase(
                                                acknowledgePurchaseParams,
                                                new AcknowledgePurchaseResponseListener() {
                                                    @Override
                                                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                                                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                                            Toast.makeText(MainActivity.this, "Acknowledge", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                        );

                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

    private void connectToGooglePlayBilling(){
        billingClient.startConnection(
                new BillingClientStateListener() {
                    @Override
                    public void onBillingServiceDisconnected() {
                        connectToGooglePlayBilling();
                    }

                    @Override
                    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            getProductDetails();
                        }
                    }
                }
        );
    }

    private void getProductDetails(){
        List<String> productIds = new ArrayList<>();
        productIds.add("one_time_purchase");
        /*productIds.add("js_6_months");
        productIds.add("js_12_months");*/
        SkuDetailsParams getProductDetailsQuery = SkuDetailsParams
                .newBuilder()
                .setSkusList(productIds)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        Activity activity = this;
        billingClient.querySkuDetailsAsync(
                getProductDetailsQuery,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK &&
                        list != null){
                            SkuDetails itemInfo = list.get(0);
                            text1.setText(itemInfo.getTitle());
                            btn1.setText(itemInfo.getPrice());
                            btn1.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            billingClient.launchBillingFlow(
                                                    activity,
                                                    BillingFlowParams.newBuilder().setSkuDetails(itemInfo).build()
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }
    
}