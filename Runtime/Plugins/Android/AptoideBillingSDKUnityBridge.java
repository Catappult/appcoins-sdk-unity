import android.util.Log;
import androidx.annotation.NonNull;
import com.appcoins.sdk.billing.*;
import com.appcoins.sdk.billing.helpers.CatapultBillingAppCoinsFactory;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.unity3d.player.UnityPlayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AptoideBillingSDKUnityBridge {
    private static String unityClassName;
    private static String TAG = "AptoideBillingSDKUnityBridge";
    private static AppcoinsBillingClient billingClient;

    private static Map<String, ProductDetails> fetchedProductDetailsMap = new HashMap<>();

    private static AppCoinsBillingStateListener appCoinsBillingStateListener =
            new AppCoinsBillingStateListener() {
                @Override
                public void onBillingSetupFinished(int responseCode) {
                    Log.d(TAG, "Billing setup finished.");
                    UnityPlayer.UnitySendMessage(unityClassName,
                            "BillingSetupFinishedCallback",
                            "" + responseCode);
                }

                @Override
                public void onBillingServiceDisconnected() {
                    Log.d(TAG, "Billing service disconnected.");
                    UnityPlayer.UnitySendMessage(unityClassName,
                            "BillingServiceDisconnectedCallback",
                            "");
                }
            };

    private static PurchasesUpdatedListener purchasesUpdatedListener =
            (responseCode, purchases) -> {
                Log.d(TAG, "Purchase updated: " + responseCode);
                UnityPlayer.UnitySendMessage(unityClassName, "PurchasesUpdatedCallback",
                        purchasesResultToJson(responseCode, purchases));
            };

    private static PurchasesResponseListener purchasesResponseListener =
            (billingResult, purchases) -> {
                Log.d(TAG,
                        "Purchases received: " + billingResult.getResponseCode() + " debugMessage: "
                                + billingResult.getDebugMessage());
                UnityPlayer.UnitySendMessage(unityClassName, "PurchasesResponseCallback",
                        purchasesResponseResultToJson(billingResult, (List<Purchase>) purchases));
            };

    private static SkuDetailsResponseListener skuDetailsResponseListener =
            (responseCode, skuDetailsList) -> {
                Log.d(TAG, "SKU details received: " + responseCode);
                UnityPlayer.UnitySendMessage(unityClassName, "SkuDetailsResponseCallback",
                        skuDetailsResultToJson(responseCode, skuDetailsList));
            };

    private static ProductDetailsResponseListener productDetailsResponseListener =
            (billingResult, details) -> {
                Log.d(TAG, "SKU details received: " + billingResult.getResponseCode()
                        + " debugMessage: " + billingResult.getDebugMessage());
                if (!details.isEmpty()) {
                    for (ProductDetails productDetail : details) {
                        fetchedProductDetailsMap.put(productDetail.getProductId(), productDetail);
                    }
                }
                UnityPlayer.UnitySendMessage(unityClassName, "ProductDetailsResponseCallback",
                        productDetailsResultToJson(billingResult, details));
            };

    private static ConsumeResponseListener consumeResponseListener =
            (responseCode, purchaseToken) -> {
                Log.d(TAG, "Consume response: " + purchaseToken + ", result: " + responseCode);
                UnityPlayer.UnitySendMessage(unityClassName, "ConsumeResponseCallback",
                        consumeResultToJson(responseCode, purchaseToken));
            };

    public static void initialize(String _unityClassName, String _publicKey) {
        unityClassName = _unityClassName;
        billingClient = CatapultBillingAppCoinsFactory.BuildAppcoinsBilling(
                UnityPlayer.currentActivity,
                _publicKey, purchasesUpdatedListener);
    }

    public static void startConnection() {
        billingClient.startConnection(appCoinsBillingStateListener);
    }

    public static void endConnection() {
        billingClient.endConnection();
        Log.d(TAG, "Billing client connection ended.");
    }

    public static boolean isReady() {
        boolean ready = billingClient.isReady();
        Log.d(TAG, "Billing client is ready: " + ready);
        return ready;
    }

    public static void querySkuDetailsAsync(List<String> skuList, String skuType) {
        SkuDetailsParams params = new SkuDetailsParams();
        params.setMoreItemSkus(skuList);
        params.setItemType(skuType);
        billingClient.querySkuDetailsAsync(params, skuDetailsResponseListener);
    }

    public static void queryProductDetailsAsync(List<String> products, String productType) {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        for (String product : products) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(product)
                            .setProductType(productType)
                            .build()
            );
        }

        QueryProductDetailsParams queryProductDetailsParams2 =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build();
        billingClient.queryProductDetailsAsync(queryProductDetailsParams2,
                productDetailsResponseListener);
    }

    public static int launchBillingFlowV2(String productId, String productType,
            String developerPayload, String obfuscatedAccountId, boolean freeTrial) {
        ProductDetails productDetails = getProductDetailsFromProductId(productId);
        if (productDetails != null) {
            ArrayList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                    new ArrayList<>();
            productDetailsParamsList.add(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
            );
            BillingFlowParams billingFlowParams =
                    BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .setFreeTrial(freeTrial)
                            .setObfuscatedAccountId(obfuscatedAccountId)
                            .setDeveloperPayload(developerPayload)
                            .build();
            return billingClient.launchBillingFlow(UnityPlayer.currentActivity, billingFlowParams);
        } else {
            BillingFlowParams billingFlowParams = new BillingFlowParams(productId, productType, null,
                    developerPayload,
                    null, obfuscatedAccountId, freeTrial);
            return billingClient.launchBillingFlow(UnityPlayer.currentActivity, billingFlowParams);
        }
    }

    public static int launchBillingFlow(String sku, String skuType, String developerPayload) {
        BillingFlowParams flowParams = new BillingFlowParams(sku, skuType, null, developerPayload,
                null);
        return billingClient.launchBillingFlow(UnityPlayer.currentActivity, flowParams);
    }

    public static int launchBillingFlow(String sku, String skuType, String developerPayload,
            String obfuscatedAccountId, boolean freeTrial) {
        BillingFlowParams flowParams = new BillingFlowParams(sku, skuType, null, developerPayload,
                null, obfuscatedAccountId, freeTrial);
        return billingClient.launchBillingFlow(UnityPlayer.currentActivity, flowParams);
    }

    public static void consumeAsync(String purchaseToken) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();
        billingClient.consumeAsync(consumeParams, consumeResponseListener);
    }

    public static int isFeatureSupported(String feature) {
        FeatureType featureType = FeatureType.valueOf(feature);
        int responseCode = billingClient.isFeatureSupported(featureType);
        Log.d(TAG, "Feature " + feature + " supported: " + (responseCode == 0));
        return responseCode;
    }

    public static String queryPurchases(String skuType) {
        Log.d(TAG, "Querying purchases of sku type: " + skuType);
        QueryPurchasesParams queryPurchasesParams =
                QueryPurchasesParams.newBuilder()
                        .setProductType(skuType)
                        .build();
        PurchasesResult result = billingClient.queryPurchasesAsync(queryPurchasesParams);
        Log.d(TAG, "Queried purchases with result code: " + result.getResponseCode());
        return purchasesResultToJson(result.getResponseCode(), result.getPurchases());
    }

    public static void queryPurchasesAsync(String productType) {
        Log.d(TAG, "Querying purchases async of product type: " + productType);
        QueryPurchasesParams queryPurchasesParams =
                QueryPurchasesParams.newBuilder()
                        .setProductType(productType)
                        .build();
        billingClient.queryPurchasesAsync(queryPurchasesParams, purchasesResponseListener);
    }

    public static String getReferralDeeplink() {
        ReferralDeeplink referralDeeplink = billingClient.getReferralDeeplink();
        Log.d(TAG, "Referral deeplink: " + referralDeeplink);
        return referralDeeplinkResultToJson(referralDeeplink);
    }

    public static boolean isAppUpdateAvailable() {
        boolean isUpdateAvailable = billingClient.isAppUpdateAvailable();
        Log.d(TAG, "Is app update available: " + isUpdateAvailable);
        return isUpdateAvailable;
    }

    public static void launchAppUpdateDialog() {
        billingClient.launchAppUpdateDialog(UnityPlayer.currentActivity);
        Log.d(TAG, "Launched app update dialog.");
    }

    public static void launchAppUpdateStore() {
        billingClient.launchAppUpdateStore(UnityPlayer.currentActivity);
        Log.d(TAG, "Launched app update store.");
    }

    private static String purchasesResultToJson(int responseCode, List<Purchase> purchases) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("responseCode", responseCode);
            JSONArray purchasesJsonArray = new JSONArray();
            for (int i = 0; i < purchases.size(); i++) {
                Purchase purchase = purchases.get(i);
                JSONObject purchaseJsonObject = new JSONObject();
                purchaseJsonObject.put("itemType", purchase.getItemType());
                purchaseJsonObject.put("orderId", purchase.getOrderId());
                purchaseJsonObject.put("packageName", purchase.getPackageName());
                purchaseJsonObject.put("sku", purchase.getSku());
                purchaseJsonObject.put("purchaseTime", purchase.getPurchaseTime());
                purchaseJsonObject.put("purchaseState", purchase.getPurchaseState());
                purchaseJsonObject.put("developerPayload", purchase.getDeveloperPayload());
                purchaseJsonObject.put("obfuscatedAccountId", purchase.getObfuscatedAccountId());
                purchaseJsonObject.put("token", purchase.getToken());
                purchaseJsonObject.put("originalJson", purchase.getOriginalJson());
                purchaseJsonObject.put("signature", purchase.getSignature());
                purchaseJsonObject.put("isAutoRenewing", purchase.isAutoRenewing());
                purchasesJsonArray.put(purchaseJsonObject);
            }
            jsonObject.put("purchases", purchasesJsonArray);
        } catch (JSONException exception) {
            Log.e(TAG, "purchasesResultToJson: ", exception);
            return new JSONObject().toString();
        }
        return jsonObject.toString();
    }

    private static String purchasesResponseResultToJson(BillingResult billingResult,
            List<Purchase> purchases) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject billingResultJsonObject = new JSONObject();
            billingResultJsonObject.put("ResponseCode", billingResult.getResponseCode());
            billingResultJsonObject.put("DebugMessage", billingResult.getDebugMessage());
            jsonObject.put("BillingResult", billingResultJsonObject);
            JSONArray purchasesJsonArray = new JSONArray();
            for (int i = 0; i < purchases.size(); i++) {
                Purchase purchase = purchases.get(i);
                JSONObject purchaseJsonObject = new JSONObject();
                purchaseJsonObject.put("itemType", purchase.getItemType());
                purchaseJsonObject.put("orderId", purchase.getOrderId());
                purchaseJsonObject.put("packageName", purchase.getPackageName());
                purchaseJsonObject.put("sku", purchase.getSku());
                purchaseJsonObject.put("purchaseTime", purchase.getPurchaseTime());
                purchaseJsonObject.put("purchaseState", purchase.getPurchaseState());
                purchaseJsonObject.put("developerPayload", purchase.getDeveloperPayload());
                purchaseJsonObject.put("obfuscatedAccountId", purchase.getObfuscatedAccountId());
                purchaseJsonObject.put("token", purchase.getToken());
                purchaseJsonObject.put("originalJson", purchase.getOriginalJson());
                purchaseJsonObject.put("signature", purchase.getSignature());
                purchaseJsonObject.put("isAutoRenewing", purchase.isAutoRenewing());
                purchasesJsonArray.put(purchaseJsonObject);
            }
            jsonObject.put("Purchases", purchasesJsonArray);
        } catch (JSONException exception) {
            Log.e(TAG, "purchasesResultToJson: ", exception);
            return new JSONObject().toString();
        }
        return jsonObject.toString();
    }

    private static String skuDetailsResultToJson(int responseCode,
            List<SkuDetails> skuDetailsList) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("responseCode", responseCode);
            JSONArray skuDetailsjsonArray = new JSONArray();
            for (int i = 0; i < skuDetailsList.size(); i++) {
                SkuDetails skuDetails = skuDetailsList.get(i);
                JSONObject skuDetailsJsonObject = new JSONObject();
                skuDetailsJsonObject.put("itemType", skuDetails.getItemType());
                skuDetailsJsonObject.put("sku", skuDetails.getSku());
                skuDetailsJsonObject.put("type", skuDetails.getType());
                skuDetailsJsonObject.put("price", skuDetails.getPrice());
                skuDetailsJsonObject.put("priceAmountMicros", skuDetails.getPriceAmountMicros());
                skuDetailsJsonObject.put("priceCurrencyCode", skuDetails.getPriceCurrencyCode());
                skuDetailsJsonObject.put("appcPrice", skuDetails.getAppcPrice());
                skuDetailsJsonObject.put("appcPriceAmountMicros",
                        skuDetails.getAppcPriceAmountMicros());
                skuDetailsJsonObject.put("appcPriceCurrencyCode",
                        skuDetails.getAppcPriceCurrencyCode());
                skuDetailsJsonObject.put("fiatPrice", skuDetails.getFiatPrice());
                skuDetailsJsonObject.put("fiatPriceAmountMicros",
                        skuDetails.getFiatPriceAmountMicros());
                skuDetailsJsonObject.put("fiatPriceCurrencyCode",
                        skuDetails.getFiatPriceCurrencyCode());
                skuDetailsJsonObject.put("title", skuDetails.getTitle());
                if (skuDetails.getDescription() != null) {
                    skuDetailsJsonObject.put("description", skuDetails.getDescription());
                }
                if (skuDetails.getPeriod() != null) {
                    skuDetailsJsonObject.put("period", skuDetails.getPeriod());
                }
                if (skuDetails.getTrialPeriod() != null) {
                    skuDetailsJsonObject.put("trialPeriod", skuDetails.getTrialPeriod());
                }
                if (skuDetails.getTrialPeriodEndDate() != null) {
                    skuDetailsJsonObject.put("trialPeriodEndDate",
                            skuDetails.getTrialPeriodEndDate());
                }
                skuDetailsjsonArray.put(skuDetailsJsonObject);
            }
            jsonObject.put("skuDetails", skuDetailsjsonArray);
        } catch (JSONException exception) {
            Log.e(TAG, "skuDetailsResultToJson: ", exception);
            return new JSONObject().toString();
        }
        return jsonObject.toString();
    }

    private static String productDetailsResultToJson(BillingResult billingResult,
            List<ProductDetails> details) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject billingResultJsonObject = new JSONObject();
            billingResultJsonObject.put("ResponseCode", billingResult.getResponseCode());
            billingResultJsonObject.put("DebugMessage", billingResult.getDebugMessage());
            jsonObject.put("BillingResult", billingResultJsonObject);
            JSONArray productDetailsJsonArray = new JSONArray();
            for (int i = 0; i < details.size(); i++) {
                ProductDetails productDetails = details.get(i);
                JSONObject productDetailsJsonObject = new JSONObject();

                productDetailsJsonObject.put("ProductId", productDetails.getProductId());
                productDetailsJsonObject.put("ProductType", productDetails.getProductType());
                productDetailsJsonObject.put("Title", productDetails.getTitle());

                if (productDetails.getDescription() != null) {
                    productDetailsJsonObject.put("Description", productDetails.getDescription());
                }

                // One-time purchase
                if (productDetails.getOneTimePurchaseOfferDetails() != null) {
                    JSONObject oneTimeOfferJson = getJsonObject(productDetails);

                    productDetailsJsonObject.put("OneTimePurchaseOfferDetails", oneTimeOfferJson);
                }

                // Subscription offers
                if (productDetails.getSubscriptionOfferDetails() != null) {
                    JSONArray subscriptionOffersArray = getJsonArray(productDetails);
                    productDetailsJsonObject.put("SubscriptionOfferDetails",
                            subscriptionOffersArray);
                }

                productDetailsJsonArray.put(productDetailsJsonObject);
            }
            jsonObject.put("Details", productDetailsJsonArray);
        } catch (JSONException exception) {
            Log.e(TAG, "productDetailsResultToJson: ", exception);
            return new JSONObject().toString();
        }
        return jsonObject.toString();
    }

    @NonNull
    private static JSONArray getJsonArray(ProductDetails productDetails) throws JSONException {
        JSONArray subscriptionOffersArray = new JSONArray();

        if (productDetails.getSubscriptionOfferDetails() != null) {
            for (ProductDetails.SubscriptionOfferDetails offerDetail :
                    productDetails.getSubscriptionOfferDetails()) {
                JSONObject offerDetailJson = new JSONObject();

                // Pricing phases
                JSONArray pricingPhasesArray = getJsonArray(offerDetail);

                JSONObject pricingPhasesJson = new JSONObject();
                pricingPhasesJson.put("PricingPhaseList", pricingPhasesArray);
                offerDetailJson.put("PricingPhases", pricingPhasesJson);

                // Trial details
                if (offerDetail.getTrialDetails() != null) {
                    JSONObject trialJson = new JSONObject();
                    trialJson.put("Period", offerDetail.getTrialDetails().getPeriod());
                    trialJson.put("PeriodEndDate",
                            offerDetail.getTrialDetails().getPeriodEndDate());
                    offerDetailJson.put("TrialDetails", trialJson);
                }
                subscriptionOffersArray.put(offerDetailJson);
            }
        }
        return subscriptionOffersArray;
    }

    @NonNull
    private static JSONArray getJsonArray(ProductDetails.SubscriptionOfferDetails offerDetail)
            throws JSONException {
        JSONArray pricingPhasesArray = new JSONArray();
        for (ProductDetails.PricingPhase pricingPhase :
                offerDetail.getPricingPhases()
                        .getPricingPhaseList()) {
            JSONObject phaseJson = new JSONObject();
            phaseJson.put("BillingPeriod", pricingPhase.getBillingPeriod());
            phaseJson.put("FormattedPrice", pricingPhase.getFormattedPrice());
            phaseJson.put("PriceAmountMicros", pricingPhase.getPriceAmountMicros());
            phaseJson.put("PriceCurrencyCode", pricingPhase.getPriceCurrencyCode());
            phaseJson.put("AppcFormattedPrice",
                    pricingPhase.getAppcFormattedPrice());
            phaseJson.put("AppcPriceAmountMicros",
                    pricingPhase.getAppcPriceAmountMicros());
            phaseJson.put("AppcPriceCurrencyCode",
                    pricingPhase.getAppcPriceCurrencyCode());
            phaseJson.put("FiatFormattedPrice",
                    pricingPhase.getFiatFormattedPrice());
            phaseJson.put("FiatPriceAmountMicros",
                    pricingPhase.getFiatPriceAmountMicros());
            phaseJson.put("FiatPriceCurrencyCode",
                    pricingPhase.getFiatPriceCurrencyCode());
            pricingPhasesArray.put(phaseJson);
        }
        return pricingPhasesArray;
    }

    @NonNull
    private static JSONObject getJsonObject(ProductDetails productDetails) throws JSONException {
        ProductDetails.OneTimePurchaseOfferDetails offer =
                productDetails.getOneTimePurchaseOfferDetails();
        JSONObject oneTimeOfferJson = new JSONObject();
        oneTimeOfferJson.put("FormattedPrice", offer.getFormattedPrice());
        oneTimeOfferJson.put("PriceAmountMicros", offer.getPriceAmountMicros());
        oneTimeOfferJson.put("PriceCurrencyCode", offer.getPriceCurrencyCode());
        oneTimeOfferJson.put("AppcFormattedPrice", offer.getAppcFormattedPrice());
        oneTimeOfferJson.put("AppcPriceAmountMicros", offer.getAppcPriceAmountMicros());
        oneTimeOfferJson.put("AppcPriceCurrencyCode", offer.getAppcPriceCurrencyCode());
        oneTimeOfferJson.put("FiatFormattedPrice", offer.getFiatFormattedPrice());
        oneTimeOfferJson.put("FiatPriceAmountMicros", offer.getFiatPriceAmountMicros());
        oneTimeOfferJson.put("FiatPriceCurrencyCode", offer.getFiatPriceCurrencyCode());
        return oneTimeOfferJson;
    }

    private static String consumeResultToJson(int responseCode, String purchaseToken) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("responseCode", responseCode);
            jsonObject.put("purchaseToken", purchaseToken);
        } catch (JSONException exception) {
            Log.e(TAG, "consumeResultToJson: ", exception);
            return new JSONObject().toString();
        }
        return jsonObject.toString();
    }

    private static String referralDeeplinkResultToJson(ReferralDeeplink referralDeeplink) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("responseCode", referralDeeplink.getResponseCode());
            jsonObject.put("storeDeeplink", referralDeeplink.getStoreDeeplink());
            jsonObject.put("fallbackDeeplink", referralDeeplink.getFallbackDeeplink());
        } catch (JSONException exception) {
            Log.e(TAG, "referralDeeplinkResultToJson: ", exception);
            return new JSONObject().toString();
        }
        return jsonObject.toString();
    }

    private static ProductDetails getProductDetailsFromProductId(String productId) {
        return fetchedProductDetailsMap.get(productId);
    }
}
