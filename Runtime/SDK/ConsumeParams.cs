using System;

public class ConsumeParams
{
    public string PurchaseToken { get; }

    private ConsumeParams(string purchaseToken)
    {
        PurchaseToken = purchaseToken;
    }

    public class Builder
    {
        private string purchaseToken;

        public Builder SetPurchaseToken(string token)
        {
            if (string.IsNullOrEmpty(token))
            {
                throw new ArgumentException("Purchase token must not be empty.");
            }

            purchaseToken = token;
            return this;
        }

        public ConsumeParams Build()
        {
            if (string.IsNullOrEmpty(purchaseToken))
            {
                throw new InvalidOperationException("Purchase token must be provided.");
            }

            return new ConsumeParams(purchaseToken);
        }
    }

    public static Builder NewBuilder()
    {
        return new Builder();
    }
}
