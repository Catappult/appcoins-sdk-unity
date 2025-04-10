using System.Collections.Generic;
using UnityEngine;

namespace UnityAppCoinsSDK
{
    [System.Serializable]
    public class PurchaseData
    {
        public string msg;
        public bool succeed;
        public int responseCode;
        public string purchaseToken;
        public Purchase[] purchases;
    }

    [System.Serializable]
    public class Purchase
    {
        public string developerPayload;
        public bool isAutoRenewing;
        public string itemType;
        public string orderId;
        public string originalJson;
        public string packageName;
        public int purchaseState;
        public long purchaseTime;
        public string sku;
        public string token;
    }

    public class AptoPurchaseManager : MonoBehaviour
    {
        public string publicKey = "YOUR_PUBLIC_KEY";
        public string skuInApp = "attempts"; // SKU for in-app purchases
        public string skuSubscription = "golden_dice"; // SKU for subscriptions
        public string developerPayload = "YOUR_DEVELOPER_PAYLOAD";

        private AndroidJavaClass appCoinsAdapterClass;
        private bool isInitialized = false;
        private string attemptsPrice;
        private string subscriptionPrice;
        private bool isGoldenDiceSubsActive = false;

        void Start()
        {
            if (Application.platform == RuntimePlatform.Android)
            {
                appCoinsAdapterClass = new AndroidJavaClass("AppCoinsAdapter");
                InitializeAppCoinsAdapter();
            }
        }

        private void InitializeAppCoinsAdapter()
        {
            appCoinsAdapterClass.CallStatic("initialize", this.gameObject.name, publicKey, skuInApp, true);
            Debug.Log("AppCoinsAdapter initialized with public key: " + publicKey);
        }

        public void StartPurchase()
        {
            appCoinsAdapterClass.CallStatic("ProductsStartPay", skuInApp, developerPayload);
            Debug.Log("Started purchase for SKU: " + skuInApp);
        }

        public void StartSubscription()
        {
            appCoinsAdapterClass.CallStatic("ProductsStartSubsPay", skuSubscription, developerPayload);
            Debug.Log("Started subscription for SKU: " + skuSubscription);
        }

        public void QueryPrices()
        {
            appCoinsAdapterClass.CallStatic("ProductsStartGet", $"{skuInApp};{skuSubscription}");
            Debug.Log("Querying prices for SKUs: " + skuInApp + " and " + skuSubscription);
        }

        public string GetPriceInApp()
        {
            return appCoinsAdapterClass.CallStatic<string>("GetPrice", skuInApp);
        }

        public string GetPriceSubscription()
        {
            return appCoinsAdapterClass.CallStatic<string>("GetPrice", skuSubscription);
        }

        public bool IsGoldenDiceSubscriptionActive()
        {
            isGoldenDiceSubsActive = appCoinsAdapterClass.CallStatic<bool>("IsGoldenDiceSubsActive");
            Debug.Log("Golden Dice subscription active: " + isGoldenDiceSubsActive);
            return isGoldenDiceSubsActive;
        }

        public void OnMsgFromPlugin(string message)
        {
            PurchaseData purchaseData = JsonUtility.FromJson<PurchaseData>(message);
            Debug.Log("[AptoPurchaseManager] Message from plugin: " + purchaseData.msg);

            switch (purchaseData.msg)
            {
                case "ProductsPayResult":
                    HandlePurchaseResult(purchaseData);
                    break;

                case "ProductsConsumeResult":
                    HandleConsumeResult(purchaseData);
                    break;

                case "ProductsGetResult":
                    attemptsPrice = GetPriceInApp();
                    subscriptionPrice = GetPriceSubscription();
                    Debug.Log("Prices updated: In-App = " + attemptsPrice + ", Subscription = " + subscriptionPrice);
                    break;

                case "QueryPurchasesResult":
                    HandleQueryPurchasesResult(purchaseData);
                    break;

                default:
                    Debug.Log("Unhandled message: " + purchaseData.msg);
                    break;
            }
        }

        private void HandlePurchaseResult(PurchaseData purchaseData)
        {
            if (purchaseData.succeed)
            {
                Debug.Log("Purchase successful.");
                foreach (Purchase purchase in purchaseData.purchases)
                {
                    appCoinsAdapterClass.CallStatic("ProductsStartConsume", purchase.token);
                    if (purchase.itemType == "subs")
                    {
                        Debug.Log("Subscription purchased.");
                        isGoldenDiceSubsActive = true;
                    }
                    else
                    {
                        Debug.Log("In-app item purchased.");
                    }
                }
            }
            else
            {
                Debug.LogError("Purchase failed.");
            }
        }

        private void HandleConsumeResult(PurchaseData purchaseData)
        {
            if (purchaseData.succeed)
            {
                Debug.Log("Purchase consumed successfully.");
            }
            else
            {
                Debug.LogError("Failed to consume purchase.");
            }
        }

        private void HandleQueryPurchasesResult(PurchaseData purchaseData)
        {
            foreach (Purchase purchase in purchaseData.purchases)
            {
                if (purchase.sku == skuInApp || purchase.sku == skuSubscription)
                {
                    appCoinsAdapterClass.CallStatic("ProductsStartConsume", purchase.token);
                    if (purchase.itemType == "subs")
                    {
                        Debug.Log("Subscription already active.");
                        isGoldenDiceSubsActive = true;
                    }
                    else
                    {
                        Debug.Log("In-app item already purchased.");
                    }
                }
            }
        }
    }
}