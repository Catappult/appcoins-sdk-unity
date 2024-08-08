using System.Collections;
using System.Collections.Generic;
using UnityEngine;


namespace AppCoins.Billing.Editor 
{

    public class PurchaseManager : MonoBehaviour
    {
        public void OnPurchaseFinished(string product) {
            Debug.Log("Purchase of product " + product + " finished");   
        }
    }
}