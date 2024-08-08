using UnityEngine;
using System.Threading;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace AppCoins.Billing.Editor
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
        public SkuDetails[] products; // Array of purchases (if any)
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
        public string appPrice;
    }

    [System.Serializable]
    public class SkuDetails
    {
        public string appPrice;
        public long appcPriceAmountMicros;
        public string appcPriceCurrencyCode;
        public string description;
        public string fiatPrice;
        public long fiatPriceAmountMicros;
        public string fiatPriceCurrencyCode;
        public string itemType;
        public string price;
        public long priceAmountMicros;
        public string priceCurrencyCode;
        public string sku;
        public string title;
        public string type;
    }

    public class AptoPurchaseManager : MonoBehaviour
    {
        public string publicKey = "YOUR_PUBLIC_KEY"; // Public key for AppCoins billing
        public string sku = "YOUR_SKU_ID"; // SKU ID for the item to be purchased, can be a string separated by ";" for multiple items, or you can generate from your backend
        public string developerPayload = "YOUR_DEVELOPER_PAYLOAD"; // Developer payload for verification (optional)
        
        
        private bool lastPurchaseCheck = false; // Check last purchase used in ValidateLastPurchase
        private bool walletActivated = false; // Checks if the app is activated via OnMsgFromPlugin, should be removed when the AptoBridge script is updated.
        private AndroidJavaClass aptoBridgeClass; // Reference to the AptoBridge AndroidJavaClass

        private string tokenPurchase;
        private bool canConsumeItem = false;

        private PurchaseData purchaseData;

        private List<SkuDetails> products = new List<SkuDetails>(); //list of skudetails with the products set on catappult available to be purchased 


        void Start()
        {
            if (Application.platform == RuntimePlatform.Android)
            {
                // Instantiate the AptoBridge AndroidJavaClass when the script starts
                aptoBridgeClass = new AndroidJavaClass("AppCoinsAdapter");
                // Initialize the AptoBridge plugin
                InitializeAptoBridge();
            }
        }

        private void OnApplicationFocus(bool focusStatus)
        {
            if(products.Count == 0){
                products.Clear();
                StartGet();
            }

            string hasDonePurchase = PlayerPrefs.GetString("hasDonePurchase","-");
            string purchaseDataStr = PlayerPrefs.GetString("purchaseData","-");
            string purchaseSignature = PlayerPrefs.GetString("purchaseSignature","-");

            //convertString to Json
            PurchaseData purchaseDataJson = JsonUtility.FromJson<PurchaseData>(purchaseDataStr);
            //getTheToken
            tokenPurchase = purchaseDataJson.purchaseToken;

            if(hasDonePurchase=="1"){
                        aptoBridgeClass.CallStatic("ShareActivityResult", 51, -1, purchaseDataStr, purchaseSignature);
            }

            if(walletActivated) 
            {
                if(focusStatus == true)
                {
                    aptoBridgeClass.CallStatic("QueryPurchases");
                    walletActivated = false;
                }
            }

        }

        // Method to initialize the AptoBridge plugin
        private void InitializeAptoBridge()
        {
            aptoBridgeClass.CallStatic("Initialize", this.gameObject.name, publicKey, true);
        }

        public void StartGet(){
            aptoBridgeClass.CallStatic("ProductsStartGet", sku);
        }


        // Method to start the purchase process
        public void StartPurchase(string sku)
        {
            if(products.Count == 0){
                _ShowAndroidToastMessage("Loading...");
                StartGet();
            }


            Debug.Log("[AptoBridge - Unity Side] - Size: " + products.Count);
            foreach(SkuDetails p in products){
                if(p.sku == sku){
                    _ShowAndroidToastMessage("Purchasing!");
                    aptoBridgeClass.CallStatic("ProductsStartPay", sku, p.type, developerPayload);
                    break;
                }
            }
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

        public void ConsumeItem(){
            if(canConsumeItem){
                _ShowAndroidToastMessage("Consuming Item!");
                aptoBridgeClass.CallStatic("ProductsStartConsume", tokenPurchase);

                PlayerPrefs.SetString("hasDonePurchase","-");
                PlayerPrefs.SetString("purchaseData","-");
                PlayerPrefs.SetString("purchaseSignature","-");
                aptoBridgeClass.CallStatic("ShareActivityResult", 0, 0, "", "");

                //CallStatic novo metodo para consumir apos resultado do
                lastPurchaseCheck = true;
                Debug.LogError("Made the purchase sucesfully.");

                canConsumeItem = false;
            }else{
                _ShowAndroidToastMessage("You have items to consume first!");
            }
    
        }

        // Method to handle messages received from the plugin
        public void OnMsgFromPlugin(string message)
        {   
            // Deserialize the JSON data into PurchaseData object
            purchaseData = JsonUtility.FromJson<PurchaseData>(message);
            
            // Switch based on the message type received
            switch (purchaseData.msg)
            {
                case "InitialResult":
                    // Handle initialization result
                    if (!purchaseData.succeed)
                    {
                        Debug.LogError("Failed to initialize billing service.");
                    }else{
                        _ShowAndroidToastMessage("Billing Service Initialized! Loading Start Get!");
                        StartGet();
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
                        canConsumeItem = true;
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
                            Debug.Log("Item already purchased.");
                            break;
                        }
                    }

                    if (!itemPurchased)
                    {
                        
                    }
                    break;
                
                case "ProductsGetResult":
                    if(products.Count > 0){
                        products.Clear();
                    }

                    foreach(var product in purchaseData.products)
                    {
                        var skuDetails = JsonUtility.FromJson<SkuDetails>(JsonConvert.SerializeObject(product));
                        products.Add(skuDetails);
                    }
                    break;
            }
        }


        private void _ShowAndroidToastMessage(string message)
        {
            AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            AndroidJavaObject unityActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");

            if (unityActivity != null)
            {
                AndroidJavaClass toastClass = new AndroidJavaClass("android.widget.Toast");
                unityActivity.Call("runOnUiThread", new AndroidJavaRunnable(() =>
                {
                    AndroidJavaObject toastObject = toastClass.CallStatic<AndroidJavaObject>("makeText", unityActivity, message, 0);
                    toastObject.Call("show");
                }));
            }
        }
    }

}
