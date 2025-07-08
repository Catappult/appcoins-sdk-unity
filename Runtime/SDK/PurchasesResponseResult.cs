[System.Serializable]
public class PurchasesResult
{
    public BillingResult billingResult; // BillingResult of Purchases Response
    public Purchase[] purchases; // Array of purchases (if any)
}