[System.Serializable]
public class PurchasesResponseResult
{
    public BillingResult billingResult; // BillingResult of Purchases Response
    public Purchase[] purchases; // Array of purchases (if any)
}
