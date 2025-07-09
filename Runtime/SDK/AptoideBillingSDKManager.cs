using UnityEngine;
using System;
using System.Linq;

public class AptoideBillingSDKManager : MonoBehaviour
{
    private static AptoideBillingSDKManager instance;
    private static AndroidJavaObject aptoideBillingSDKUnityBridge;

    private static IAppCoinsBillingStateListener appCoinsBillingStateListener;
    private static IConsumeResponseListener consumeResponseListener;
    private static IPurchasesUpdatedListener purchasesUpdatedListener;
    private static IPurchasesResponseListener purchasesResponseListener;
    private static ISkuDetailsResponseListener skuDetailsResponseListener;
    private static IProductDetailsResponseListener productDetailsResponseListener;

    public static void InitializePlugin(IAppCoinsBillingStateListener _appCoinsBillingStateListener,
    IConsumeResponseListener _consumeResponseListener,
    IPurchasesUpdatedListener _purchasesUpdatedListener,
    ISkuDetailsResponseListener _skuDetailsResponseListener,
    string publicKey,
    string className)
    {
        appCoinsBillingStateListener = _appCoinsBillingStateListener;
        consumeResponseListener = _consumeResponseListener;
        purchasesUpdatedListener = _purchasesUpdatedListener;
        skuDetailsResponseListener = _skuDetailsResponseListener;

        if (Application.platform == RuntimePlatform.Android)
        {
            aptoideBillingSDKUnityBridge = new AndroidJavaObject("AptoideBillingSDKUnityBridge");
            Initialize(publicKey, className);
            StartConnection();
        }
    }

    public static void InitializePlugin(IAppCoinsBillingStateListener _appCoinsBillingStateListener,
    IConsumeResponseListener _consumeResponseListener,
    IPurchasesUpdatedListener _purchasesUpdatedListener,
    IProductDetailsResponseListener _productDetailsResponseListener,
    IPurchasesResponseListener _purchasesResponseListener,
    string publicKey,
    string className)
    {
        appCoinsBillingStateListener = _appCoinsBillingStateListener;
        consumeResponseListener = _consumeResponseListener;
        purchasesUpdatedListener = _purchasesUpdatedListener;
        purchasesResponseListener = _purchasesResponseListener;
        productDetailsResponseListener = _productDetailsResponseListener;

        if (Application.platform == RuntimePlatform.Android)
        {
            aptoideBillingSDKUnityBridge = new AndroidJavaObject("AptoideBillingSDKUnityBridge");
            Initialize(publicKey, className);
            StartConnection();
        }
    }

    // ---- SDK Methods ----

    public static void Initialize(string publicKey, string className)
    {
        aptoideBillingSDKUnityBridge?.CallStatic("initialize", className, publicKey);
    }

    public static void StartConnection()
    {
        aptoideBillingSDKUnityBridge?.CallStatic("startConnection");
    }

    public static void EndConnection()
    {
        aptoideBillingSDKUnityBridge?.CallStatic("endConnection");
    }

    public static bool IsReady()
    {
        bool isReady = aptoideBillingSDKUnityBridge?.CallStatic<bool>("isReady") ?? false;
        Debug.Log($"AptoideBillingSDKManager | IsReady: {isReady}");

        return isReady;
    }

    public static void QuerySkuDetailsAsync(string[] skus, string skuType)
    {
        using (AndroidJavaObject skuList = new AndroidJavaObject("java.util.ArrayList"))
        {
            foreach (string sku in skus)
            {
                skuList.Call<bool>("add", sku);
            }
            aptoideBillingSDKUnityBridge?.CallStatic("querySkuDetailsAsync", skuList, skuType);
        }
    }

    public static void QueryProductDetailsAsync(QueryProductDetailsParams queryProductDetailsParams)
    {
        using (AndroidJavaObject productsList = new AndroidJavaObject("java.util.ArrayList"))
        {
            string productType = null;
            foreach (QueryProductDetailsParams.Product productParams in queryProductDetailsParams.ProductList)
            {
                productType = productParams.ProductType;
                productsList.Call<bool>("add", productParams.ProductId);
            }
            aptoideBillingSDKUnityBridge?.CallStatic("queryProductDetailsAsync", productsList, productType);
        }
    }

    public static int LaunchBillingFlow(BillingFlowParams billingFlowParams)
    {
        string sku = billingFlowParams.Sku;
        string skuType = billingFlowParams.SkuType;
        string developerPayload = billingFlowParams.DeveloperPayload;
        string obfuscatedAccountId = billingFlowParams.ObfuscatedAccountId;
        bool freeTrial = billingFlowParams.FreeTrial;
        int launchBillingFlowResponseCode = aptoideBillingSDKUnityBridge?.CallStatic<int>("launchBillingFlowV2", sku, skuType, developerPayload, obfuscatedAccountId, freeTrial) ?? -1;
        Debug.Log($"AptoideBillingSDKManager | LaunchBillingFlow: {launchBillingFlowResponseCode}");

        return launchBillingFlowResponseCode;
    }

    public static int LaunchBillingFlow(string sku, string skuType, string developerPayload)
    {
        int launchBillingFlowResponseCode = aptoideBillingSDKUnityBridge?.CallStatic<int>("launchBillingFlow", sku, skuType, developerPayload) ?? -1;
        Debug.Log($"AptoideBillingSDKManager | LaunchBillingFlow: {launchBillingFlowResponseCode}");

        return launchBillingFlowResponseCode;
    }

    public static int LaunchBillingFlow(string sku, string skuType, string developerPayload, string obfuscatedAccountId, bool freeTrial)
    {
        int launchBillingFlowResponseCode = aptoideBillingSDKUnityBridge?.CallStatic<int>("launchBillingFlow", sku, skuType, developerPayload, obfuscatedAccountId, freeTrial) ?? -1;
        Debug.Log($"AptoideBillingSDKManager | LaunchBillingFlow: {launchBillingFlowResponseCode}");

        return launchBillingFlowResponseCode;
    }

    public static void ConsumeAsync(string purchaseToken)
    {
        aptoideBillingSDKUnityBridge?.CallStatic("consumeAsync", purchaseToken);
    }

    public static void ConsumeAsync(ConsumeParams consumeParams)
    {
        aptoideBillingSDKUnityBridge?.CallStatic("consumeAsync", consumeParams.PurchaseToken);
    }

    public static int IsFeatureSupported(string feature)
    {
        int isFeatureSupportedResponseCode = aptoideBillingSDKUnityBridge?.CallStatic<int>("isFeatureSupported", feature) ?? -1;
        Debug.Log($"AptoideBillingSDKManager | IsFeatureSupported: {isFeatureSupportedResponseCode}");

        return isFeatureSupportedResponseCode;
    }

    public static PurchasesResult QueryPurchases(string skuType)
    {
        string purchasesResultJson = aptoideBillingSDKUnityBridge?.CallStatic<string>("queryPurchases", skuType);
        Debug.Log($"AptoideBillingSDKManager | QueryPurchases: {purchasesResultJson}");

        PurchasesResult purchasesResult = JsonUtility.FromJson<PurchasesResult>(purchasesResultJson);
        return purchasesResult;
    }

    public static void QueryPurchasesAsync(QueryPurchasesParams queryPurchasesParams)
    {
        aptoideBillingSDKUnityBridge?.CallStatic("queryPurchasesAsync", queryPurchasesParams.ProductType);
    }

    public static ReferralDeeplinkResult GetReferralDeeplink()
    {
        string referralDeeplinkJson = aptoideBillingSDKUnityBridge?.CallStatic<string>("getReferralDeeplink");
        Debug.Log($"AptoideBillingSDKManager | GetReferralDeeplink: {referralDeeplinkJson}");

        ReferralDeeplinkResult referralDeeplinkResult = JsonUtility.FromJson<ReferralDeeplinkResult>(referralDeeplinkJson);
        return referralDeeplinkResult;
    }

    public static bool IsAppUpdateAvailable()
    {
        bool isAppUpdateAvailable = aptoideBillingSDKUnityBridge?.CallStatic<bool>("isAppUpdateAvailable") ?? false;
        Debug.Log($"AptoideBillingSDKManager | IsAppUpdateAvailable: {isAppUpdateAvailable}");

        return isAppUpdateAvailable;
    }

    public static void LaunchAppUpdateDialog()
    {
        aptoideBillingSDKUnityBridge?.CallStatic("launchAppUpdateDialog");
    }

    public static void LaunchAppUpdateStore()
    {
        aptoideBillingSDKUnityBridge?.CallStatic("launchAppUpdateStore");
    }

    // ---- Callback Handlers from Java ----

    public void BillingSetupFinishedCallback(string responseCode)
    {
        Debug.Log("AppCoins Billing Setup Finished");
        appCoinsBillingStateListener.OnBillingSetupFinished(int.Parse(responseCode));
    }

    public void BillingServiceDisconnectedCallback(string _)
    {
        Debug.LogWarning("AptoideBillingSDKManager | AppCoins Billing Service Disconnected");
        appCoinsBillingStateListener.OnBillingServiceDisconnected();
    }

    public void PurchasesUpdatedCallback(string purchasesResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | Purchase Updated: {purchasesResultJson}");

        PurchasesResult purchasesResult = JsonUtility.FromJson<PurchasesResult>(purchasesResultJson);

        purchasesUpdatedListener.OnPurchasesUpdated(purchasesResult.responseCode, purchasesResult.purchases);
    }

    public void PurchasesResponseCallback(string purchasesResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | Purchases Response: {purchasesResultJson}");

        PurchasesResponseResult purchasesResponseResult = JsonUtility.FromJson<PurchasesResponseResult>(purchasesResultJson);

        purchasesResponseListener.OnQueryPurchasesResponse(purchasesResponseResult.BillingResult, purchasesResponseResult.Purchases);
    }

    public void SkuDetailsResponseCallback(string skuDetailsResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | SKU Details Received: {skuDetailsResultJson}");

        SkuDetailsResult skuDetailsResult = JsonUtility.FromJson<SkuDetailsResult>(skuDetailsResultJson);

        skuDetailsResponseListener.OnSkuDetailsResponse(skuDetailsResult.responseCode, skuDetailsResult.skuDetails);
    }

    public void ProductDetailsResponseCallback(string productDetailsResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | Product Details Received: {productDetailsResultJson}");

        ProductDetailsResult productDetailsResult = JsonUtility.FromJson<ProductDetailsResult>(productDetailsResultJson);

        productDetailsResponseListener.OnProductDetailsResponse(productDetailsResult.BillingResult, productDetailsResult.Details);
    }

    public void ConsumeResponseCallback(string consumeResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | Consume Response: {consumeResultJson}");

        ConsumeResult consumeResult = JsonUtility.FromJson<ConsumeResult>(consumeResultJson);

        consumeResponseListener.OnConsumeResponse(consumeResult.responseCode, consumeResult.purchaseToken);
    }
}
