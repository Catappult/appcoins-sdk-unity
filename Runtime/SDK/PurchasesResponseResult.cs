[System.Serializable]
public class PurchasesResponseResult
{
    public BillingResult BillingResult; // BillingResult of Purchases Response
    public Purchase[] Purchases; // Array of purchases (if any)
}
