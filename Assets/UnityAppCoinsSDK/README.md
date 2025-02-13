
# AppCoins SDK Unity package

## Description

Streamline the process of adding Appcoins SDK to your Unity app through importing from the Unity Package Manager. Below you can see the video about how to integrate the SDK and after it a detailed installation guide of the same process.

<!---               
## Video

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/m-EZxdb7sUY/0.jpg)](https://www.youtube.com/watch?v=m-EZxdb7sUY)
-->
## Installation Guide

### Step 1 - Import Package
* Start by opening on the top menu bar the Window > Package Manager
* In the new window on the top left corner click on the + sign and select Import via git URL and paste the following link: [https://github.com/Aptoide/appcoins-sdk-unity-package](https://github.com/Catappult/appcoins-sdk-unity-package)
* Wait to import and compile all files

### Step 2 - Add AptoPurchaseManager to the Main Camera game object + Set Params
* Select the Game Object, on the Inspector panel scroll to bottom
* Add Component, search for AptoPurchaseManager
* Set the params of KEY, SKU (string divided by ";")** and Developer Payload

<img width="1435" alt="Screenshot 2024-08-09 at 11 33 34" src="https://github.com/user-attachments/assets/9c385ff9-d8d1-44f6-b343-7a0bf02c0e6a">


### Step 3 - Setup the Purchase Button 
* Select the Play button, on the Inspector panel scroll to bottom
* Add entry on the OnClick area
* Drag and drop the Main Camera to the box under Runtime (on OnClick section) 
* After that select the Script AptoPurchaseManager and the method StartPurchase , to which you can set the sku**

<img width="1430" alt="Screenshot 2024-08-09 at 11 34 30" src="https://github.com/user-attachments/assets/c7464974-bbf4-495e-9198-4b1f0822c62d">


### Step 4 - Setup the Consume Item Button
* Select the Consume button and in the inspector on the bottom add on the on click a new entry 
* Drag and drop the Main Camera to the box under Runtime (on OnClick section) 
* After that select the Script AptoPurchaseManager and the method ConsumeItem **

<img width="1422" alt="Screenshot 2024-08-09 at 11 34 43" src="https://github.com/user-attachments/assets/9b86977b-4c09-4c06-a75b-8a4a23ee7424">


### Step 5 - Setup Manifest File
* Open the Manifest file (or create one in your Assets folder) and update the package name to your project 
* Set as well the queries and permissions

```
<manifest>
  ...
  <queries>
    <!-- Required to work with Android 11 and above -->
    <package android:name="com.appcoins.wallet" />
    ...
  </queries>
  ...
  <uses-permission android:name="com.appcoins.BILLING" />
	<uses-permission android:name="android.permission.INTERNET" />
  ...
  <activity android:name="com.appcoins.sdk.billing.WebIapCommunicationActivity"
        android:exported="true">
  	<intent-filter>
      <action android:name="android.intent.action.VIEW"/>
      <category android:name="android.intent.category.DEFAULT"/>
      <category android:name="android.intent.category.BROWSABLE" />
    	<data android:scheme="web-iap-result" android:host="PACKAGE_OF_YOUR_APPLICATION"/>
  	</intent-filter>
  </activity>
  ...
</manifest>
```

### Step 6 - Setup Build Graddle
* Add the implementation to import sdk

```
dependencies {
  implementation("io.catappult:android-appcoins-billing:0.8.0.3") //check the latest version in mvnrepository
	<...other dependencies..>
}
```

Note in case you don't have build.graddle files we suggest this approach:

- Module (baseProjectTemplate.gradle)
  
```
allprojects {
    buildscript {
        repositories {**ARTIFACTORYREPOSITORY**
            google()
            jcenter()
        }

        dependencies {
            // If you are changing the Android Gradle Plugin version, make sure it is compatible with the Gradle version preinstalled with Unity
            // See which Gradle version is preinstalled with Unity here https://docs.unity3d.com/Manual/android-gradle-overview.html
            // See official Gradle and Android Gradle Plugin compatibility table here https://developer.android.com/studio/releases/gradle-plugin#updating-gradle
            // To specify a custom Gradle version in Unity, go do "Preferences > External Tools", uncheck "Gradle Installed with Unity (recommended)" and specify a path to a custom Gradle version
            classpath 'com.android.tools.build:gradle:3.4.3'
            **BUILD_SCRIPT_DEPS**
        }
    }

    repositories {**ARTIFACTORYREPOSITORY**
        google()
        jcenter()
        flatDir {
            dirs "${project(':unityLibrary').projectDir}/libs"
        }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

- Application (mainTemplate.gradle)

```
apply plugin: 'com.android.library'
**APPLY_PLUGINS**

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation("io.catappult:android-appcoins-billing:0.8.0.3") 
    implementation('org.json:json:20210307')
**DEPS**}

android {
    compileSdkVersion **APIVERSION**
    buildToolsVersion '**BUILDTOOLS**'

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    defaultConfig {
        minSdkVersion **MINSDKVERSION**
        targetSdkVersion **TARGETSDKVERSION**
        ndk {
            abiFilters **ABIFILTERS**
        }
        versionCode **VERSIONCODE**
        versionName '**VERSIONNAME**'
        consumerProguardFiles 'proguard-unity.txt'**USER_PROGUARD**
    }

    lintOptions {
        abortOnError false
    }

    aaptOptions {
        noCompress = **BUILTIN_NOCOMPRESS** + unityStreamingAssets.tokenize(', ')
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }**PACKAGING_OPTIONS**
}

**REPOSITORIES**
**IL_CPP_BUILD_SETUP**
**SOURCE_BUILD_SETUP**
**EXTERNAL_SOURCES**
```




### Step 7 - Add OverrideExample and AppCoinsAdapter
* Update as well on the OverrideExample the line 1 package name and the line 27 on the getSharedPreferences
```
//DONT FORGET TO CHANGE THE PACKAGE
package com.appcoins.diceroll;

import com.unity3d.player.UnityPlayerActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;

import android.content.SharedPreferences;

import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class OverrideExample extends UnityPlayerActivity {  
  
  public SharedPreferences pref;
  public SharedPreferences.Editor editor;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d("OverrideExample", "onCreate called!");

    //DONT FORGET TO CHANGE THE PACKAGE
    pref = getApplicationContext().getSharedPreferences("com.appcoins.diceroll.v2.playerprefs", MODE_PRIVATE);
    editor = pref.edit();
    editor.putString("hasDonePurchase", "--");
    editor.apply();
  }
  
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    String dataPurchaseJson = data.getStringExtra("INAPP_PURCHASE_DATA");
    String dataSignatureJson = data.getStringExtra("INAPP_DATA_SIGNATURE");

    if(requestCode==51){
      if(resultCode==-1){
        sharedPrefHasDonePurchase("1",dataPurchaseJson,dataSignatureJson);

      }else{
        sharedPrefHasDonePurchase("0","","");
      }
    }
  }
    
  public void sharedPrefHasDonePurchase(String value, String dataPurchaseJson, String dataSignatureJson)
  {
    editor.putString("hasDonePurchase", value);
    editor.putString("purchaseData", dataPurchaseJson);
    editor.putString("purchaseSignature", dataSignatureJson);
    editor.apply();  
  }

}
```
* Add the following file AppCoinsAdapter to access the AppCoins SDK
```
import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.util.Log;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;

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

import com.appcoins.sdk.billing.Inventory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

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
    private static String unityClassName = "";
    private static String publicKey = "";

    private static boolean needLog = true;

    private static AppCoinsBillingStateListener appCoinsBillingStateListener = new AppCoinsBillingStateListener() {
        @Override
        public void onBillingSetupFinished(int responseCode) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_INITIAL_RESULT);
                jsonObject.put("succeed", responseCode == ResponseCode.OK.getValue());
                jsonObject.put("responseCode", responseCode);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            SendUnityMessage(jsonObject);
        }

        @Override
        public void onBillingServiceDisconnected() {
            AdapterLog("onBillingServiceDisconnected");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("msg", MSG_CONNECTION_LOST);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            SendUnityMessage(jsonObject);
        }
    };

    public static AppcoinsBillingClient cab = null;

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
            AdapterLog("Consumption finished. Purchase: " + purchaseToken + ", result: " + responseCode);

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
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    jsonObject.put("msg", MSG_PRODUCTS_GET_RESULT);
                    jsonObject.put("succeed", false);
                    jsonObject.put("responseCode", responseCode);
                    AdapterLog("NO SKUS");
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            SendUnityMessage(jsonObject);
        }
    };

    public static void Initialize(String _unityClassName, String _publicKey, boolean _needLog)
    {
        activity = UnityPlayer.currentActivity;
        unityClassName = _unityClassName;
        publicKey = _publicKey;
        needLog = _needLog;

        cab = CatapultBillingAppCoinsFactory.BuildAppcoinsBilling(
                activity,
                publicKey,
                purchasesUpdatedListener
        );
        cab.startConnection(appCoinsBillingStateListener);
    }

    //Try to receive here an arraylist of skus
    public static void ProductsStartGet(String strSku)
    {
        List<String> skuList = new ArrayList<String>(Arrays.asList(strSku.split(";")));
        
        SkuDetailsParams skuDetailsParams = new SkuDetailsParams();
        skuDetailsParams.setItemType(SkuType.inapp.toString());
        skuDetailsParams.setMoreItemSkus(skuList);
        cab.querySkuDetailsAsync(skuDetailsParams, skuDetailsResponseListener);

        AdapterLog("After skuDetailsResponseListener > " + skuDetailsResponseListener.toString());
        //aqui recebe os skus e da a info deles
        //enviado aquela flag de erros
    }

    public static void ProductsStartPay(String sku, String skuTypeGiven, String developerPayload)
    {
        String skuType = skuTypeGiven;
        BillingFlowParams billingFlowParams =
                new BillingFlowParams(
                        sku,
                        skuType,
                        "orderId=" +System.currentTimeMillis(),
                        developerPayload,
                        "BDS"
                );

        final int responseCode = cab.launchBillingFlow(activity, billingFlowParams);
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

    public static void QueryPurchases()
    {
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
    }

    public static void ProductsStartConsume(String strToken)
    {
        List<String> tokenList = new ArrayList<String>(Arrays.asList(strToken.split(";")));
        
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


    public static void ShareActivityResult(int requestCode, int resultCode, String dataPurchase, String dataSignature) {
        AdapterLog("Launching Shared Activity Result. reqCode " + requestCode + " resultCode: " + resultCode + " dataPurchase: " + dataPurchase + "dataSignature: " + dataSignature);
        Intent intent = new Intent();
        if(requestCode==51){
            intent.putExtra("INAPP_PURCHASE_DATA", dataPurchase);
            intent.putExtra("INAPP_DATA_SIGNATURE", dataSignature);
            cab.onActivityResult(requestCode, resultCode, intent);
        }else{
            intent.removeExtra("INAPP_PURCHASE_DATA");
            intent.removeExtra("INAPP_DATA_SIGNATURE");
        }
    }


    public static void AdapterLog(String msg) {
        if (needLog) {
            Log.d(LOG_TAG, msg);
        }
    }
}
```


After that you can run and you have successfully integrate the Appcoins SDK on your Unity App through Package Manager. Your project should look like this:

- Assets Folder (with the Android Manifest, Graddle files, AppCoinsAdapter and OverrideExample) + Plugin Imported on Packages Folder

<img width="614" alt="Screenshot 2024-08-09 at 11 31 33" src="https://github.com/user-attachments/assets/ddb0b917-7053-48d3-887c-1ed58336ec3c">




<br /><br />
### **ADDITIONAL NOTES
> [!NOTE]
> This is a demo project to test integration, you should perform consumption after purchase of the item<br />
> The SKUs can be set on the AptoPurchaseManager SKU (Inspector of MainCamera - Game Object) or through AptoPurchaseManager Script accessing your backend and setting the string<br />
> You also can dinamically atribute the SKU para of the button and pass the SKU once calling the StartPurchase

