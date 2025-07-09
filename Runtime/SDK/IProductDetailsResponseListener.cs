public interface IProductDetailsResponseListener
{
    void OnProductDetailsResponse(BillingResult billingResult, ProductDetails[] details);
}
