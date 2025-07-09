using System;

public class QueryPurchasesParams
{
    public string ProductType { get; }

    private QueryPurchasesParams(string productType)
    {
        ProductType = productType;
    }

    public class Builder
    {
        private string productType;

        public Builder SetProductType(string productType)
        {
            if (string.IsNullOrEmpty(productType))
            {
                throw new ArgumentException("Product type must be provided.");
            }

            this.productType = productType;
            return this;
        }

        public QueryPurchasesParams Build()
        {
            if (string.IsNullOrEmpty(productType))
            {
                throw new InvalidOperationException("Product type must be provided.");
            }

            return new QueryPurchasesParams(productType);
        }
    }

    public static Builder NewBuilder()
    {
        return new Builder();
    }
}
