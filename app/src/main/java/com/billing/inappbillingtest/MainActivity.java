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
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.buyNow);

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null){
                            for (Purchase purchase: list){
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
                                {
                                    if (purchase.getSkus().equals(list)){
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
        productIds.add("test_buy_product");
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
                            TextView itemName = findViewById(R.id.itemName);
                            Button buyNow = findViewById(R.id.buyNow);
                            SkuDetails itemInfo = list.get(0);
                            itemName.setText(itemInfo.getTitle());
                            buyNow.setText(itemInfo.getPrice());
                            buyNow.setOnClickListener(
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