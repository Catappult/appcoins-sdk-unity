import android.util.Log;
import com.appcoins.sdk.billing.helpers.CatapultBillingAppCoinsFactory;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.billing.types.SkuType;



import com.appcoins.sdk.billing.listeners.*;
import com.appcoins.sdk.billing.AppcoinsBillingClient;
import com.appcoins.sdk.billing.PurchasesUpdatedListener;
import com.appcoins.sdk.billing.BillingFlowParams;
import com.appcoins.sdk.billing.Purchase;
import com.appcoins.sdk.billing.PurchasesResult;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.SkuDetails;
import com.appcoins.sdk.billing.SkuDetailsParams;
import com.appcoins.sdk.billing.types.*;
import com.appcoins.sdk.billing.ReferralDeeplink;
import com.appcoins.sdk.billing.FeatureType;

import com.unity3d.player.UnityPlayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;


public class AptoideBillingSDKUnityBridge {
    private static String unityClassName;
    private static String TAG = "AptoideBillingSDKUnityBridge";
    private static AppcoinsBillingClient billingClient;

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

    private static SkuDetailsResponseListener skuDetailsResponseListener =
            (responseCode, skuDetailsList) -> {
                Log.d(TAG, "SKU details received: " + responseCode);
                UnityPlayer.UnitySendMessage(unityClassName, "SkuDetailsResponseCallback",
                        skuDetailsResultToJson(responseCode, skuDetailsList));
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

    public static int launchBillingFlow(String sku, String skuType, String developerPayload) {
        BillingFlowParams flowParams = new BillingFlowParams(sku, skuType, null, developerPayload,
                "BDS");
        return billingClient.launchBillingFlow(UnityPlayer.currentActivity, flowParams);
    }

    public static void consumeAsync(String purchaseToken) {
        billingClient.consumeAsync(purchaseToken, consumeResponseListener);
    }

    public static int isFeatureSupported(String feature) {
        FeatureType featureType = FeatureType.valueOf(feature);
        int responseCode = billingClient.isFeatureSupported(featureType);
        Log.d(TAG, "Feature " + feature + " supported: " + (responseCode == 0));
        return responseCode;
    }

    public static String queryPurchases(String skuType) {
        PurchasesResult result = billingClient.queryPurchases(skuType);
        Log.d(TAG, "Queried purchases with result code: " + result.getResponseCode());
        return purchasesResultToJson(result.getResponseCode(), result.getPurchases());
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

    private static String skuDetailsResultToJson(int responseCode, List<SkuDetails> skuDetailsList) {
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
                skuDetailsjsonArray.put(skuDetailsJsonObject);
            }
            jsonObject.put("skuDetails", skuDetailsjsonArray);
        } catch (JSONException exception) {
            Log.e(TAG, "skuDetailsResultToJson: ", exception);
            return new JSONObject().toString();
        }
        return jsonObject.toString();
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
}