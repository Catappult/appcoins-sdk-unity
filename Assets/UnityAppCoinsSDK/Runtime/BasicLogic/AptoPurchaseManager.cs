using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Threading;
using System.Reflection;

namespace UnityAppCoinsSDK
{
    // Define classes to deserialize JSON data received from the plugin
    [System.Serializable]
    public class PurchaseData
    {
        public string msg; // Message indicating the type of response
        public bool succeed; // Indicates whether the operation succeeded
        public int responseCode; // Response code from the plugin
        public string purchaseToken; // Token of the purchase (if any)
        public Purchase[] purchases; // Array of purchases (if any)
    }

    [System.Serializable]
    public class Purchase
    {
        // Purchase details
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
        public string publicKey = "YOUR_PUBLIC_KEY"; // Public key for AppCoins billing
        public string sku = "YOUR_SKU_ID"; // SKU ID for the item to be purchased
        public string developerPayload = "YOUR_DEVELOPER_PAYLOAD"; // Developer payload for verification (optional)
        private bool lastPurchaseCheck = false; // Check last purchase used in ValidateLastPurchase
        private bool lastSubscriptionCheck = false; // Check last purchase used in ValidateLastPurchase
        private bool walletActivated = false; // ****TEMP***** Checks if the app is activated via OnMsgFromPlugin, should be removed when the AppCoinsAdapter script is updated.
        private AndroidJavaClass appCoinsAdapterClass; // Reference to the AppCoinsAdapter AndroidJavaClass

        private bool isInitialized = false;

        private string _attemptPrice;
        private string _subsPrice;

        private bool isGoldenDiceSubsActive = false;

        void Start()
        {
            if (Application.platform == RuntimePlatform.Android)
            {
                // Instantiate the AppCoinsAdapter AndroidJavaClass when the script starts
                appCoinsAdapterClass = new AndroidJavaClass("AppCoinsAdapter");

                // Initialize the AppCoinsAdapter plugin
                InitializeAppCoinsAdapter();
            }
        }

        // ****TEMP***** Method to query the purchase made upon the closure of the wallet in order to consume it
        // Should not be required when the AppCoinsAdapter script is updated, making the comsuption occur in the OnMsgFromPlugin method
        private void OnApplicationFocus(bool focusStatus)
        {
            if(walletActivated) 
            {
                if(focusStatus == true)
                {
                    appCoinsAdapterClass.CallStatic("QueryPurchases");
                    walletActivated = false;
                }
            }
        }

        // Method to initialize the AppCoinsAdapter plugin
        private void InitializeAppCoinsAdapter()
        {
            appCoinsAdapterClass.CallStatic("Initialize", this.gameObject.name, publicKey, sku ,true);
            Debug.Log("Launch Init! PK : " + publicKey);
            Debug.Log("isInitialized: " + isInitialized.ToString());
        }



        // Method to start the purchase process
        public void StartPurchase()
        {
            string skuInApp = sku.Split(';')[0];
            appCoinsAdapterClass.CallStatic("ProductsStartPay", skuInApp, developerPayload);
        }

        public void StartSubscription()
        {
            string skuSubs = sku.Split(';')[1];
            appCoinsAdapterClass.CallStatic("ProductsStartSubsPay", skuSubs, developerPayload);
            Debug.Log("Launch Subscription!");
        }

        // Example variable for other classes to call in order to validate the last purchase.
        public bool ValidateLastPurchase()
        {
            if (lastPurchaseCheck)
            {
                lastPurchaseCheck = false;
                return true;
            }
            else
            {
                return false;
            }
        }

        public bool ValidateLastSubsPurchase()
        {
            if (lastSubscriptionCheck)
            {
                lastSubscriptionCheck = false;
                return true;
            }
            else
            {
                return false;
            }
        }

        // Method to handle messages received from the plugin
        public void OnMsgFromPlugin(string message)
        {
            // Deserialize the JSON data into PurchaseData object
            PurchaseData purchaseData = JsonUtility.FromJson<PurchaseData>(message);
            Debug.Log("[AppCoinsAdapter - Unity Side] - Printing ---> " + purchaseData.msg);
            Debug.Log("[AppCoinsAdapter - Unity Side] - Printing ---> " + purchaseData.succeed);
            
        

            // Switch based on the message type received
            switch (purchaseData.msg)
            {
                case "InitialResult":
                    // Handle initialization result
                    if (!purchaseData.succeed)
                    {
                        Debug.LogError("Failed to initialize billing service.");
                    }
                    break;

                case "LaunchBillingResult":
                    // Handle launch billing flow result
                    if (!purchaseData.succeed)
                    {
                        Debug.LogError("Failed to launch billing flow.");
                    }
                    else
                    {
                        Debug.LogError("Launched the billing flow.");
                        walletActivated = true;
                    }
                    break;

                case "ProductsPayResult":
                    // Handle product purchase result
                    if (!purchaseData.succeed)
                    {
                        Debug.LogError("Failed to make the purchase.");
                    }
                    else
                    {
                        Debug.LogError("Made the purchase sucesfully.");


                        foreach (Purchase purchase in purchaseData.purchases)
                        {

                            appCoinsAdapterClass.CallStatic("ProductsStartConsume", purchase.token);

                            if(purchase.itemType == "subs") {
                                Debug.Log("Subscription purchased.");
                                lastSubscriptionCheck = true;
                            } else {
                                Debug.Log("Item purchased.");
                                lastPurchaseCheck = true;
                            }
                        }

                    }
                    break;
                    
                case "ProductsConsumeResult":
                    // Handle product purchase comsuption
                    if (!purchaseData.succeed)
                    {
                        Debug.LogError("Failed to consume the purchase.");
                    }
                    else
                    {
                        Debug.LogError("Consumed the purchase sucesfully.");
                    }
                    break;

                case "QueryPurchasesResult":
                    // Handle query purchases result
                    bool itemPurchased = false;
                    // Check if the item has already been purchased
                    foreach (Purchase purchase in purchaseData.purchases)
                    {
                        if (purchase.sku == sku)
                        {
                            itemPurchased = true;
                            appCoinsAdapterClass.CallStatic("ProductsStartConsume", purchase.token);
                            // ****TEMP***** Bool that checks that the purchase went through, should be moved to the ProductPayResult case when the AppCoinsAdapter script is updated.
                            
                            if(purchase.itemType == "subs") {
                                Debug.Log("Subscription purchased.");
                                lastSubscriptionCheck = true;
                            } else {
                                Debug.Log("Item purchased.");
                                lastPurchaseCheck = true;
                            }
                            Debug.Log("Item already purchased.");
                            break;
                        }
                    }

                    if (!itemPurchased)
                    {
                        
                    }
                    break;

                case "ProductsGetResult":
                    Debug.Log("Test - Getting results from. PriceInapps: " + getPriceInapp() + " PriceSubs: " + getPriceSubs());
                    
                    _attemptPrice = getPriceInapp();
                    _subsPrice = getPriceSubs();
                    
                    break;
            }
        }


        public bool isCabInitialized() {
            return appCoinsAdapterClass.CallStatic<bool>("IsCabInitialized");
        }

        public bool hasWallet() {
            return appCoinsAdapterClass.CallStatic<bool>("HasWallet");
        }

        public string getPriceInapp() {
            return appCoinsAdapterClass.CallStatic<string>("GetPrice", sku.Split(';')[0]);
        }


        public string getPriceSubs() {
            return appCoinsAdapterClass.CallStatic<string>("GetPrice", sku.Split(';')[1]);
        }

        public string getAttemptPrice() {
            return _attemptPrice;
        }

        public string getSubsPrice() {
            return _subsPrice;
        }



        public bool IsGoldenDiceSubsActive() {
            //isGoldenDiceSub already purchased?
            isGoldenDiceSubsActive = appCoinsAdapterClass.CallStatic<bool>("IsGoldenDiceSubsActive");
            Debug.Log("2Golden Dice Active: " + isGoldenDiceSubsActive);
            
            return isGoldenDiceSubsActive;
        }
    }
}
