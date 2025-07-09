using System;
using System.Collections.Generic;

[Obsolete("Deprecated constructor. Use the Builder to create the BillingFlowParams.")]
public class BillingFlowParams
{
    public string Sku { get; }
    public string SkuType { get; }
    public string OrderReference { get; }
    public string DeveloperPayload { get; }
    public string Origin { get; }
    public string ObfuscatedAccountId { get; set; }
    public bool FreeTrial { get; set; }

    [Obsolete("Deprecated constructor. Use the Builder to create the BillingFlowParams.")]
    public BillingFlowParams(string sku, string skuType, string orderReference, string developerPayload, string origin)
    {
        Sku = sku;
        SkuType = skuType;
        OrderReference = orderReference;
        DeveloperPayload = developerPayload;
        Origin = origin;
    }

    [Obsolete("Deprecated constructor. Use the Builder to create the BillingFlowParams.")]
    public BillingFlowParams(
        string sku,
        string skuType,
        string orderReference,
        string developerPayload,
        string origin,
        string obfuscatedAccountId,
        bool freeTrial
    ) : this(sku, skuType, orderReference, developerPayload, origin)
    {
        ObfuscatedAccountId = obfuscatedAccountId;
        FreeTrial = freeTrial;
    }

    public class Builder
    {
        private List<ProductDetailsParams> productDetailsParamsList = new List<ProductDetailsParams>();
        private string developerPayload;
        private string obfuscatedAccountId;
        private bool freeTrial;

        public Builder SetProductDetailsParamsList(List<ProductDetailsParams> list)
        {
            if (list == null || list.Count == 0)
            {
                throw new ArgumentException("Product list must not be empty.");
            }
            if (list.Count != 1)
            {
                throw new ArgumentException("Only one product is supported.");
            }

            productDetailsParamsList = list;
            return this;
        }

        [Obsolete("DeveloperPayload should not be used to identify Purchases. Use instead the purchaseToken. If needed to identify the User use the obfuscatedAccountId parameter.")]
        public Builder SetDeveloperPayload(string payload)
        {
            if (string.IsNullOrEmpty(payload))
            {
                throw new ArgumentException("Developer Payload must not be empty. Use null if not necessary.");
            }

            developerPayload = payload;
            return this;
        }

        public Builder SetObfuscatedAccountId(string accountId)
        {
            if (string.IsNullOrEmpty(accountId))
            {
                throw new ArgumentException("Obfuscated Account ID must not be empty. Use null if not necessary.");
            }

            obfuscatedAccountId = accountId;
            return this;
        }

        public Builder SetFreeTrial(bool trial)
        {
            freeTrial = trial;
            return this;
        }

        public BillingFlowParams Build()
        {
            if (productDetailsParamsList == null || productDetailsParamsList.Count == 0)
            {
                throw new InvalidOperationException("ProductDetailsParams list must be provided and not empty.");
            }

            var productDetails = productDetailsParamsList[0].ProductDetails;

            return new BillingFlowParams(
                productDetails.ProductId,
                productDetails.ProductType,
                null,
                developerPayload,
                null,
                obfuscatedAccountId,
                freeTrial
            );
        }
    }

    public class ProductDetailsParams
    {
        public ProductDetails ProductDetails { get; }

        private ProductDetailsParams(ProductDetails productDetails)
        {
            ProductDetails = productDetails;
        }

        public class Builder
        {
            private ProductDetails productDetails;

            public Builder SetProductDetails(ProductDetails details)
            {
                productDetails = details;
                return this;
            }

            public ProductDetailsParams Build()
            {
                if (productDetails == null)
                {
                    throw new InvalidOperationException("ProductDetails is required for constructing ProductDetailsParams.");
                }

                return new ProductDetailsParams(productDetails);
            }
        }

        public static Builder NewBuilder()
        {
            return new Builder();
        }
    }

    public static Builder NewBuilder()
    {
        return new Builder();
    }
}
