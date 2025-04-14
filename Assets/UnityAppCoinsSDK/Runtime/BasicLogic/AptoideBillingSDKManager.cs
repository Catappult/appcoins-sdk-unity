using UnityEngine;
using System;
using System.Linq;

public class AptoideBillingSDKManager : MonoBehaviour
{
    private static AptoideBillingSDKManager instance;
    private static AndroidJavaObject aptoideBillingSDKUnityBridge;
    public static string publicKey = "INSERT_YOUR_PUBLIC_KEY_HERE";

    private static string [] inappSkus = new string [] { "attempts" };
    private static string [] subsSkus = new string [] { "golden_dice" };

    void Start()
    {
            if (Application.platform == RuntimePlatform.Android)
            {
                InitializePlugin();
            }
    }

    private void InitializePlugin()
    {
        if (Application.platform == RuntimePlatform.Android)
        {
            aptoideBillingSDKUnityBridge = new AndroidJavaObject("AptoideBillingSDKUnityBridge");
            Initialize();
            StartConnection();
        }
    }

    // ---- SDK Methods ----

    public void Initialize()
    {
        aptoideBillingSDKUnityBridge?.CallStatic("initialize", this.gameObject.name, publicKey);
    }

    public static void StartConnection()
    {
        aptoideBillingSDKUnityBridge?.CallStatic("startConnection");
    }

    public static void EndConnection()
    {
        aptoideBillingSDKUnityBridge?.CallStatic("endConnection");
    }

    public static void IsReady()
    {
        bool isReady = aptoideBillingSDKUnityBridge?.CallStatic<bool>("isReady") ?? false;
        Debug.Log($"AptoideBillingSDKManager | IsReady: {isReady}");
    }

    public static void QuerySkuDetailsAsync(string skuType)
    {
        string[] skus = skuType == "subs"
                ? subsSkus
                : inappSkus;
        using (AndroidJavaObject skuList = new AndroidJavaObject("java.util.ArrayList"))
        {
            foreach (string sku in skus)
            {
                skuList.Call<bool>("add", sku);
            }
            aptoideBillingSDKUnityBridge?.CallStatic("querySkuDetailsAsync", skuList, skuType);
        }
    }

    public static void LaunchBillingFlow(string sku)
    {
        string skuType = subsSkus.Contains(sku) ? "subs" : "inapp";
        int launchBillingFlowResponseCode = aptoideBillingSDKUnityBridge?.CallStatic<int>("launchBillingFlow", sku, skuType, "teste") ?? -1;
        Debug.Log($"AptoideBillingSDKManager | LaunchBillingFlow: {launchBillingFlowResponseCode}");
    }

    public static void ConsumeAsync(string purchaseToken)
    {
        aptoideBillingSDKUnityBridge?.CallStatic("consumeAsync", purchaseToken);
    }

    public static void IsFeatureSupported(string feature)
    {
        int isFeatureSupportedResponseCode = aptoideBillingSDKUnityBridge?.CallStatic<int>("isFeatureSupported", feature) ?? -1;
        Debug.Log($"AptoideBillingSDKManager | IsFeatureSupported: {isFeatureSupportedResponseCode}");
    }

    public static void QueryPurchases(string skuType)
    {
        string purchasesResultJson = aptoideBillingSDKUnityBridge?.CallStatic<string>("queryPurchases", skuType);
        Debug.Log($"AptoideBillingSDKManager | QueryPurchases: {purchasesResultJson}");
    }

    public static void GetReferralDeeplink()
    {
        string referralDeeplinkJson = aptoideBillingSDKUnityBridge?.CallStatic<string>("getReferralDeeplink");
        Debug.Log($"AptoideBillingSDKManager | GetReferralDeeplink: {referralDeeplinkJson}");
    }

    public static void IsAppUpdateAvailable()
    {
        bool isAppUpdateAvailable = aptoideBillingSDKUnityBridge?.CallStatic<bool>("isAppUpdateAvailable") ?? false;
        Debug.Log($"AptoideBillingSDKManager | IsAppUpdateAvailable: {isAppUpdateAvailable}");
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

    public void OnBillingSetupFinished(string responseCode)
    {
        Debug.Log("AppCoins Billing Setup Finished");
        QuerySkuDetailsAsync("inapp");
        QuerySkuDetailsAsync("subs");
        QueryPurchases("inapp");
        QueryPurchases("subs");
        IsFeatureSupported("SUBSCRIPTIONS");
        IsAppUpdateAvailable();
        GetReferralDeeplink();
        LaunchAppUpdateDialog();
        LaunchAppUpdateStore();
    }

    public void OnBillingServiceDisconnected(string _)
    {
        Debug.LogWarning("AptoideBillingSDKManager | AppCoins Billing Service Disconnected");
    }

    public void OnPurchasesUpdated(string purchasesResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | Purchase Updated: {purchasesResultJson}");
    }

    public void OnSkuDetailsReceived(string skuDetailsResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | SKU Details Received: {skuDetailsResultJson}");
    }

    public void OnConsumeResponse(string consumeResultJson)
    {
        Debug.Log($"AptoideBillingSDKManager | Consume Response: {consumeResultJson}");
    }
}