import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.util.Log;
import android.content.Context;
import android.content.DialogInterface;

import android.content.pm.PackageManager;

import com.appcoins.sdk.billing.listeners.*;
import com.appcoins.sdk.billing.AppcoinsBillingClient;
import com.appcoins.sdk.billing.PurchasesUpdatedListener;
import com.appcoins.sdk.billing.BillingFlowParams;
import com.appcoins.sdk.billing.Purchase;
import com.appcoins.sdk.billing.PurchasesResult;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.SkuDetails;
import com.appcoins.sdk.billing.SkuDetailsParams;
import com.appcoins.sdk.billing.helpers.CatapultBillingAppCoinsFactory;
import com.appcoins.sdk.billing.types.*;
import com.appcoins.sdk.billing.ReferralDeeplink;
import com.appcoins.sdk.billing.FeatureType;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.CompletableFuture;


public class AppCoinsAdapter {
    private static String unityClassName; // Declare the variable

    private static String MSG_INITIAL_RESULT = "InitialResult";
    private static String MSG_CONNECTION_LOST = "ConnectionLost";
    private static String MSG_PRODUCTS_GET_RESULT = "ProductsGetResult";
    private static String MSG_LAUNCH_BILLING_RESULT = "LaunchBillingResult";
    private static String MSG_PRODUCTS_PAY_RESULT = "ProductsPayResult";
    private static String MSG_PRODUCTS_CONSUME_RESULT = "ProductsConsumeResult";
    private static String MSG_QUERY_PURCHASES_RESULT = "QueryPurchasesResult";

    private static final String LOG_TAG = "[AppCoinsAdapter]";
    private static Activity activity;
    private static AppcoinsBillingClient billingClient;

    //startConnection - But adapted to initialize on Unity
    public static void initialize(String _unityClassName, String _publicKey, String strSku, boolean _needLog){
        unityClassName = _unityClassName; 
        activity = UnityPlayer.currentActivity;
        billingClient = CatapultBillingAppCoinsFactory.BuildAppcoinsBilling(activity, _publicKey, purchasesUpdatedListener);
        billingClient.startConnection(appCoinsBillingStateListener);
    }

    public static void endConnection() {
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

    public static boolean isReady() {
        return billingClient != null && billingClient.isReady();
    }

    public static int isFeatureSupported(FeatureType feature) {
        return billingClient != null ? billingClient.isFeatureSupported(feature) : ResponseCode.FEATURE_NOT_SUPPORTED.getValue();
    }

    public static boolean isAppUpdateAvailable() {
        return billingClient != null && billingClient.isAppUpdateAvailable();
    }

    public static void launchAppUpdateDialog(Context context) {
        if (billingClient != null) {
            billingClient.launchAppUpdateDialog(context);
        }
    }

    public static void launchAppUpdateStore(Context context) {
        if (billingClient != null) {
            billingClient.launchAppUpdateStore(context);
        }
    }

    public static int launchBillingFlow(BillingFlowParams billingFlowParams) {
        if (billingClient != null) {
            return billingClient.launchBillingFlow(activity, billingFlowParams);
        }
        return ResponseCode.ERROR.getValue();
    }

    public static void queryPurchases(String skuType) {
        if (billingClient != null) {
            billingClient.queryPurchases(skuType);
        }
        // Ensure the constructor arguments match the expected types
        //return new PurchasesResult(ResponseCode.ERROR, new ArrayList<Purchase>());
    }

    public static void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams, SkuDetailsResponseListener listener) {
        if (billingClient != null) {
            billingClient.querySkuDetailsAsync(skuDetailsParams, listener);
        }
    }

    public static void consumeAsync(String token, ConsumeResponseListener listener) {
        if (billingClient != null) {
            billingClient.consumeAsync(token, listener);
        }
    }

    public static ReferralDeeplink getReferralDeeplink() {
        if (billingClient != null) {
            return billingClient.getReferralDeeplink();
        }
        return new ReferralDeeplink(ResponseCode.ERROR, "", ""); // Pass ResponseCode object and empty strings
    }

    private static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    //LISTENERS SETUP
    private static AppCoinsBillingStateListener appCoinsBillingStateListener = new AppCoinsBillingStateListener() {
        @Override
        public void onBillingSetupFinished(int responseCode) {
            
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_INITIAL_RESULT);
                jsonObject.put("succeed", responseCode == ResponseCode.OK.getValue());
                jsonObject.put("responseCode", responseCode);

                //isCabInitialized = responseCode == ResponseCode.OK.getValue();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            SendUnityMessage(jsonObject);
        }

        @Override
        public void onBillingServiceDisconnected() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_CONNECTION_LOST);

                //isCabInitialized = false;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            SendUnityMessage(jsonObject);
        }
    };

    private static PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(int responseCode, List<Purchase> purchases)
        {
            JSONObject jsonObject = new JSONObject();
            JSONArray purchasesJson = new JSONArray();
            for(Purchase purchase: purchases)
            {
                JSONObject purchaseJson = GetPurchaseJson(purchase);
                purchasesJson.put(purchaseJson);
            }

            try {
                jsonObject.put("msg", MSG_PRODUCTS_PAY_RESULT);
                jsonObject.put("succeed", responseCode == ResponseCode.OK.getValue());
                jsonObject.put("responseCode", responseCode);
                jsonObject.put("purchases", purchasesJson);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            SendUnityMessage(jsonObject);
        }
    };

    private static ConsumeResponseListener consumeResponseListener = new ConsumeResponseListener() {
        @Override public void onConsumeResponse(int responseCode, String purchaseToken) {
        }
    };

    private static SkuDetailsResponseListener skuDetailsResponseListener = new SkuDetailsResponseListener() {
        @Override
        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
            JSONObject jsonObject = new JSONObject();
            if(responseCode == ResponseCode.OK.getValue()) {
                 JSONArray jsonSkus = new JSONArray();
                for (SkuDetails skuDetails : skuDetailsList) {
                    JSONObject detailJson = GetSkuDetailsJson(skuDetails);
                    jsonSkus.put(detailJson);
                }

                try {
                    jsonObject.put("msg", MSG_PRODUCTS_GET_RESULT);
                    jsonObject.put("succeed", true);
                    jsonObject.put("responseCode", responseCode);
                    jsonObject.put("products", jsonSkus);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    jsonObject.put("msg", MSG_PRODUCTS_GET_RESULT);
                    jsonObject.put("succeed", false);
                    jsonObject.put("responseCode", responseCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            SendUnityMessage(jsonObject);
        }
    };


    //HELPERS UNITY
    public static void SendUnityMessage(JSONObject jsonObject)
    {
        UnityPlayer.UnitySendMessage(unityClassName, "OnMsgFromPlugin", jsonObject.toString());
    }
 
    public static JSONObject GetPurchaseJson(Purchase purchase)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("developerPayload", purchase.getDeveloperPayload());
            jsonObject.put("isAutoRenewing", purchase.isAutoRenewing());
            jsonObject.put("itemType", purchase.getItemType());
            jsonObject.put("orderId", purchase.getOrderId());
            jsonObject.put("originalJson", purchase.getOriginalJson());
            jsonObject.put("packageName", purchase.getPackageName());
            jsonObject.put("purchaseState", purchase.getPurchaseState());
            jsonObject.put("purchaseTime", purchase.getPurchaseTime());
            jsonObject.put("sku", purchase.getSku());
            jsonObject.put("token", purchase.getToken());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static JSONObject GetSkuDetailsJson(SkuDetails skuDetails)
    {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("appPrice", skuDetails.getAppcPrice());
            jsonObject.put("appcPriceAmountMicros", skuDetails.getAppcPriceAmountMicros());
            jsonObject.put("appcPriceCurrencyCode", skuDetails.getAppcPriceCurrencyCode());
            jsonObject.put("description", skuDetails.getDescription());
            jsonObject.put("fiatPrice", skuDetails.getFiatPrice());
            jsonObject.put("fiatPriceAmountMicros", skuDetails.getFiatPriceAmountMicros());
            jsonObject.put("fiatPriceCurrencyCode", skuDetails.getFiatPriceCurrencyCode());
            jsonObject.put("itemType", skuDetails.getItemType());
            jsonObject.put("price", skuDetails.getPrice());
            jsonObject.put("priceAmountMicros", skuDetails.getPriceAmountMicros());
            jsonObject.put("priceCurrencyCode", skuDetails.getPriceCurrencyCode());
            jsonObject.put("sku", skuDetails.getSku());
            jsonObject.put("title", skuDetails.getTitle());
            jsonObject.put("type", skuDetails.getType());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return jsonObject;
    }


    //BRIDGE ADAPTERS UNITY - SDK
    public static void ProductsStartGet(String strSku)
    {
        List<String> skuList = new ArrayList<String>(Arrays.asList(strSku.split(";")));
        
        SkuDetailsParams skuDetailsParams = new SkuDetailsParams();
        skuDetailsParams.setItemType(SkuType.inapp.toString());
        skuDetailsParams.setMoreItemSkus(skuList);
        billingClient.querySkuDetailsAsync(skuDetailsParams, skuDetailsResponseListener);
    }

    public static void ProductsStartPay(String sku, String developerPayload)
    {
        String skuType = SkuType.inapp.toString();
        BillingFlowParams billingFlowParams =
                new BillingFlowParams(
                        sku,
                        skuType,
                        null, // Deprecated parameter orderReference
                        developerPayload,
                        "BDS"
                );

        final int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("msg", MSG_LAUNCH_BILLING_RESULT);
                    jsonObject.put("succeed", responseCode == ResponseCode.OK.getValue());
                    jsonObject.put("responseCode", responseCode);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                SendUnityMessage(jsonObject);
            }
        });
    }

    public static void ProductsStartSubsPay(String sku, String developerPayload)
    {
        String skuType = SkuType.subs.toString();
        BillingFlowParams billingFlowParams =
                new BillingFlowParams(
                        sku,
                        skuType,
                        null, // Deprecated parameter orderReference
                        developerPayload,
                        "BDS"
                );
        
        final int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("msg", MSG_LAUNCH_BILLING_RESULT);
                    jsonObject.put("succeed", responseCode == ResponseCode.OK.getValue());
                    jsonObject.put("responseCode", responseCode);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                SendUnityMessage(jsonObject);
            }
        });
    }

    public static void ProductsStartConsume(String strToken)
    {
        List<String> tokenList = new ArrayList<String>(Arrays.asList(strToken.split(";")));
        
        for(String token: tokenList)
        {
            billingClient.consumeAsync(token, consumeResponseListener);
        }
    }



}