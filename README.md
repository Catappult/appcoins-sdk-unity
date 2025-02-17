
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
* In the new window on the top left corner click on the + sign and select Import via git URL and paste the following link: https://github.com/Catappult/appcoins-sdk-unity.git#upm
* Wait to import and compile all files
* After import it should look like this:

<img width="1301" alt="Screenshot 2025-02-13 at 17 40 26" src="https://github.com/user-attachments/assets/3706371c-bb8f-410c-aa75-3e828215a097" />



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


### Step 4 - Setup the Consume Item Button**
* Select the Consume button and in the inspector on the bottom add on the on click a new entry 
* Drag and drop the Main Camera to the box under Runtime (on OnClick section) 
* After that select the Script AptoPurchaseManager and the method ConsumeItem **

**Note: The current version as the startPurchase making the consumption as well, but you can separate

<img width="1422" alt="Screenshot 2024-08-09 at 11 34 43" src="https://github.com/user-attachments/assets/9b86977b-4c09-4c06-a75b-8a4a23ee7424">


### Step 5 - Setup Manifest File**
* Open the Manifest file (or create one in your Assets folder) and update the package name to your project 
* Set as well the queries and permissions

**Note: In the plugins folder on Runtime you get a sample of Manifest, note that permissions, queries and intent-actions are required to be there in order to work properly


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

### Step 6 - Setup Build Graddle**
* Add the implementation to import sdk

**Note: As well as in the Manifest, at the Plugin folder we  provide a set of graddle files to base and mainTemplate required to import libraries needed and proper setup, you may use those files or pass some parts to your current base and mainTemplate file


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




### Step 7 - Add AppCoinsAdapter
* Add the following file AppCoinsAdapter (this file is in the Plugin folder as well this makes the connection between Unity and Android native


After that you can run and you have successfully integrate the Appcoins SDK on your Unity App through Package Manager. Your project should look like this:

- Assets Folder (with the Android Manifest, Graddle files, AppCoinsAdapter and OverrideExample) + Plugin Imported on Packages Folder

<img width="614" alt="Screenshot 2024-08-09 at 11 31 33" src="https://github.com/user-attachments/assets/ddb0b917-7053-48d3-887c-1ed58336ec3c">




<br /><br />
### **ADDITIONAL NOTES
> [!NOTE]
> This is a demo project to test integration, you should perform consumption after purchase of the item<br />
> The SKUs can be set on the AptoPurchaseManager SKU (Inspector of MainCamera - Game Object) or through AptoPurchaseManager Script accessing your backend and setting the string<br />
> You also can dinamically atribute the SKU para of the button and pass the SKU once calling the StartPurchase

