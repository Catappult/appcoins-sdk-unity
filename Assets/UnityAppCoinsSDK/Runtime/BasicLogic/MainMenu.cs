using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace UnityAppCoinsSDK
{
    public class MainMenu : MonoBehaviour
    {
        
        public void PlayGame(string message){
            ShowToastMessage("Hello World: " + message);
        }

        public void ShowToastMessage(string message){

            AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            AndroidJavaObject unityActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");

            if(unityActivity != null){
                AndroidJavaClass toastClass = new AndroidJavaClass("android.widget.Toast");
                unityActivity.Call("runOnUiThread", new AndroidJavaRunnable(() =>
                {
                    AndroidJavaObject toastObject = toastClass.CallStatic<AndroidJavaObject>("makeText", unityActivity, message, 1);
                    toastObject.Call("show");
                }));
            }
        }
    }
}
