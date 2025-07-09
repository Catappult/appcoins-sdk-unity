public interface IPurchasesResponseListener
{
    void OnQueryPurchasesResponse(BillingResult billingResult, Purchase[] purchases);
}
