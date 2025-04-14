public interface IConsumeResponseListener
{
    void OnConsumeResponse(int responseCode, string purchaseToken);
}