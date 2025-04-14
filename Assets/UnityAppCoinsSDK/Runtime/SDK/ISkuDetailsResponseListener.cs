using System.Collections.Generic;

namespace UnityAppCoinsSDK
{
    public interface ISkuDetailsResponseListener
    {
        void OnSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList);
        void OnSkuDetailsReceived(int responseCode, List<SkuDetails> skuDetailsList); // Add this method
    }
}