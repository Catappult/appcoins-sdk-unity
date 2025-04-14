public interface IAppCoinsBillingStateListener {
    void OnBillingSetupFinished(int responseCode);

    void OnBillingServiceDisconnected();
}
