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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.CompletableFuture;



public class AppCoinsAdapter {
    private static String MSG_INITIAL_RESULT = "InitialResult";
    private static String MSG_CONNECTION_LOST = "ConnectionLost";
    private static String MSG_PRODUCTS_GET_RESULT = "ProductsGetResult";
    private static String MSG_LAUNCH_BILLING_RESULT = "LaunchBillingResult";
    private static String MSG_PRODUCTS_PAY_RESULT = "ProductsPayResult";
    private static String MSG_PRODUCTS_CONSUME_RESULT = "ProductsConsumeResult";
    private static String MSG_QUERY_PURCHASES_RESULT = "QueryPurchasesResult";

    private static String LOG_TAG = "[AppCoinsAdapter]";

    private static Activity activity;
    private static String unityClassName = "SDKLogic";
    private static String publicKey;
    private static boolean needLog = true;

    public static AppcoinsBillingClient cab = null;

    private static boolean isCabInitialized = false;

    private static List<String> skuInappList = new ArrayList<>();
    private static List<String> skuSubsList = new ArrayList<>();

    private static String _attemptsPrice;
    private static String _goldDicePrice;

    private static boolean isGoldenDiceSubsActive = false;


    public static void queryInappsSkus(List<String> skuInappList) {
        // Implementation for querying in-app SKUs
        // Example: Print the list of in-app SKUs
        for (String sku : skuInappList) {
            AptoLog("Querying in-app SKU: " + sku);
        }
        

        SkuDetailsParams skuDetailsParams = new SkuDetailsParams();
        skuDetailsParams.setItemType(SkuType.inapp.toString());
        skuDetailsParams.setMoreItemSkus(skuInappList);
        cab.querySkuDetailsAsync(skuDetailsParams, skuDetailsResponseListener);

    }

    public static void querySubsSkus(List<String> skuSubsList) {
        // Implementation for querying subscription SKUs
        // Example: Print the list of subscription SKUs
        for (String sku : skuSubsList) {
            AptoLog("Querying subscription SKU: " + sku);
        }

        SkuDetailsParams skuDetailsParams = new SkuDetailsParams();
        skuDetailsParams.setItemType(SkuType.subs.toString());
        skuDetailsParams.setMoreItemSkus(skuSubsList);
        cab.querySkuDetailsAsync(skuDetailsParams, skuDetailsResponseListener);
    }


    private static AppCoinsBillingStateListener appCoinsBillingStateListener = new AppCoinsBillingStateListener() {
        @Override
        public void onBillingSetupFinished(int responseCode) {
            AptoLog("onBillingSetupFinished responseCode = " + responseCode);

            if(responseCode == ResponseCode.OK.getValue()){
                QueryPurchases();
                QuerySubs();
                queryInappsSkus(skuInappList);
                querySubsSkus(skuSubsList);
            }else{
                _attemptsPrice=null;
                _goldDicePrice=null;
            }


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_INITIAL_RESULT);
                jsonObject.put("succeed", responseCode == ResponseCode.OK.getValue());
                jsonObject.put("responseCode", responseCode);

                isCabInitialized = responseCode == ResponseCode.OK.getValue();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            SendUnityMessage(jsonObject);
        }

        @Override
        public void onBillingServiceDisconnected() {
            AptoLog("onBillingServiceDisconnected");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_CONNECTION_LOST);

                isCabInitialized = false;
                _attemptsPrice=null;
                _goldDicePrice=null;
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
            AptoLog("purchasesUpdatedListener " + responseCode);
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
            AptoLog("Consumption finished. Purchase: " + purchaseToken + ", result: " + responseCode);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_PRODUCTS_CONSUME_RESULT);
                jsonObject.put("succeed", responseCode == ResponseCode.OK.getValue());
                jsonObject.put("purchaseToken", purchaseToken);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            SendUnityMessage(jsonObject);
        }
    };

    private static SkuDetailsResponseListener skuDetailsResponseListener = new SkuDetailsResponseListener() {
        @Override
        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
            AptoLog("Received skus " + skuDetailsList.size());
            JSONObject jsonObject = new JSONObject();
            if(responseCode == ResponseCode.OK.getValue()) {
                JSONArray jsonSkus = new JSONArray();
                for (SkuDetails skuDetails : skuDetailsList) {
                    AptoLog("Processing sku: " + skuDetails.getSku());
                    JSONObject detailJson = GetSkuDetailsJson(skuDetails);
                    jsonSkus.put(detailJson);

                    AptoLog("Details skus " + skuDetails.getSku());
                    if (skuDetails.getSku().equals("attempts")) {  
                        AptoLog("Getting price for attempts");                      
                        _attemptsPrice = skuDetails.getPrice() + ' ' + skuDetails.getFiatPriceCurrencyCode(); // Use getter method
                    } else if (skuDetails.getSku().equals("golden_dice")) {
                        AptoLog("Getting price for subs");
                        _goldDicePrice = skuDetails.getPrice() + ' ' + skuDetails.getFiatPriceCurrencyCode(); // Use getter method
                    }
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

    public static void Initialize(String _unityClassName, String _publicKey, String strSku, boolean _needLog)
    {
        AptoLog("Apto Initialize: " + strSku);
        skuInappList = new ArrayList<String>(Arrays.asList(strSku.split(";")[0]));
        skuSubsList = new ArrayList<String>(Arrays.asList(strSku.split(";")[1]));
        activity = UnityPlayer.currentActivity;
        //AptoLog("activity = " + activity);
        unityClassName = _unityClassName;
        //AptoLog("unityClassName = " + unityClassName);
        publicKey = _publicKey;
        //AptoLog("publicKey = " + publicKey);
        needLog = _needLog;

        cab = CatapultBillingAppCoinsFactory.BuildAppcoinsBilling(
                activity,
                publicKey,
                purchasesUpdatedListener
        );
        cab.startConnection(appCoinsBillingStateListener);
    }

    public static void ProductsStartGet(String strSku)
    {
        AptoLog("Products Start Get");
        List<String> skuList = new ArrayList<String>(Arrays.asList(strSku.split(";")));
        AptoLog("skuList = " + skuList);

        SkuDetailsParams skuDetailsParams = new SkuDetailsParams();
        skuDetailsParams.setItemType(SkuType.inapp.toString());
        skuDetailsParams.setMoreItemSkus(skuList);
        cab.querySkuDetailsAsync(skuDetailsParams, skuDetailsResponseListener);
    }

    public static void ProductsStartPay(String sku, String developerPayload)
    {
        AptoLog("Launching purchase flow.");
        // Your sku type, can also be SkuType.subs.toString()
        String skuType = SkuType.inapp.toString();
        BillingFlowParams billingFlowParams =
                new BillingFlowParams(
                        sku,
                        skuType,
                        null, // Deprecated parameter orderReference
                        developerPayload,
                        "BDS"
                );

        final int responseCode = cab.launchBillingFlow(activity, billingFlowParams);
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
        AptoLog("Launching subs flow.");
        // Your sku type, can also be SkuType.subs.toString()
        String skuType = SkuType.subs.toString();
        BillingFlowParams billingFlowParams =
                new BillingFlowParams(
                        sku,
                        skuType,
                        null, // Deprecated parameter orderReference
                        developerPayload,
                        "BDS"
                );
        
        final int responseCode = cab.launchBillingFlow(activity, billingFlowParams);
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



    public static void QueryPurchases()
    {
        CompletableFuture.runAsync(() -> {
            PurchasesResult purchasesResult = cab.queryPurchases(SkuType.inapp.toString());
            List<Purchase> purchases = purchasesResult.getPurchases();

            JSONArray purchasesJson = new JSONArray();
            for (Purchase purchase : purchases) {
                JSONObject detailJson = GetPurchaseJson(purchase);
                purchasesJson.put(detailJson);
            }
            
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_QUERY_PURCHASES_RESULT);
                jsonObject.put("succeed", true);
                jsonObject.put("purchases", purchasesJson);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            SendUnityMessage(jsonObject);
        });
    }

    public static void QuerySubs()
    {
        CompletableFuture.runAsync(() -> {
            PurchasesResult purchasesResult = cab.queryPurchases(SkuType.subs.toString());
            List<Purchase> purchases = purchasesResult.getPurchases();

            JSONArray purchasesJson = new JSONArray();
            for (Purchase purchase : purchases) {
                JSONObject detailJson = GetPurchaseJson(purchase);
                purchasesJson.put(detailJson);
            }
            
            if (!purchases.stream().noneMatch(purchase -> "golden_dice".equals(purchase.getSku()))) {
                AptoLog("golden_dice already purchased");
                isGoldenDiceSubsActive = true;
            }

            
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_QUERY_PURCHASES_RESULT);
                jsonObject.put("succeed", true);
                jsonObject.put("purchases", purchasesJson);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            SendUnityMessage(jsonObject);
        });
    }

    public static void ProductsStartConsume(String strToken)
    {
        AptoLog("Products Start Consume");
        List<String> tokenList = new ArrayList<String>(Arrays.asList(strToken.split(";")));
        AptoLog("tokenList = " + tokenList);

        for(String token: tokenList)
        {
            cab.consumeAsync(token, consumeResponseListener);
        }
    }

    public static void SendUnityMessage(JSONObject jsonObject)
    {
        UnityPlayer.UnitySendMessage(unityClassName, "OnMsgFromPlugin", jsonObject.toString());
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

             AptoLog("prix: " + skuDetails.getPrice());

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

    public static void AptoLog(String msg) {
        if (needLog) {
            //Log.d(LOG_TAG, msg);
            Log.d(LOG_TAG, new String(msg.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }

    public static boolean IsCabInitialized() {
        return isCabInitialized;
    }

    public static boolean HasWallet() {
        Context context = activity.getApplicationContext();
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.appcoins.wallet", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static String GetPrice(String sku) {
        AptoLog("SKU GetPrice AppCoinsAdapter: " + sku );
        AptoLog("attempts GetPrice AppCoinsAdapter: " + _attemptsPrice );
        AptoLog("gold Dice GetPrice AppCoinsAdapter: " + _goldDicePrice );
        
        if (sku.equals("attempts")) {
            return _attemptsPrice;
        }
        if (sku.equals("golden_dice")) {
            return _goldDicePrice;
        }

        return "";
    }


    public static boolean IsGoldenDiceSubsActive(){
        AptoLog("1Golden Dice Active: " + isGoldenDiceSubsActive );
        return isGoldenDiceSubsActive;
    }
}
