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
    private static ISkuDetailsResponseListener skuDetailsResponseListener;

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

    public static int LaunchBillingFlow(string sku, string skuType, string developerPayload)
    {
        int launchBillingFlowResponseCode = aptoideBillingSDKUnityBridge?.CallStatic<int>("launchBillingFlow", sku, skuType, developerPayload) ?? -1;
        Debug.Log($"AptoideBillingSDKManager | LaunchBillingFlow: {launchBillingFlowResponseCode}");

        return launchBillingFlowResponseCode;
    }

    public static void ConsumeAsync(string purchaseToken)
    {
        aptoideBillingSDKUnityBridge?.CallStatic("consumeAsync", purchaseToken);
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

    public void SkuDetailsResponseCallback(string skuDetailsResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | SKU Details Received: {skuDetailsResultJson}");

        SkuDetailsResult skuDetailsResult = JsonUtility.FromJson<SkuDetailsResult>(skuDetailsResultJson);

        skuDetailsResponseListener.OnSkuDetailsResponse(skuDetailsResult.responseCode, skuDetailsResult.skuDetails);
    }

    public void ConsumeResponseCallback(string consumeResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | Consume Response: {consumeResultJson}");

        ConsumeResult consumeResult = JsonUtility.FromJson<ConsumeResult>(consumeResultJson);

        consumeResponseListener.OnConsumeResponse(consumeResult.responseCode, consumeResult.purchaseToken);
    }
}