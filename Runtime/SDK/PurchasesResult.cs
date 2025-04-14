[System.Serializable]
public class PurchasesResult
{
    public int responseCode; // Response code from the plugin
    public Purchase[] purchases; // Array of purchases (if any)
}