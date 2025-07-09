using System;
using System.Collections.Generic;

[System.Serializable]
public class ProductDetails
{
    public string ProductId;
    public string ProductType;
    public string Title;
    public string Description;
    public OneTimePurchaseOfferDetails OneTimePurchaseOfferDetails;
    public List<SubscriptionOfferDetails> SubscriptionOfferDetails;
}

[System.Serializable]
public class OneTimePurchaseOfferDetails
{
    public string FormattedPrice;
    public long PriceAmountMicros;
    public string PriceCurrencyCode;
    public string AppcFormattedPrice;
    public long AppcPriceAmountMicros;
    public string AppcPriceCurrencyCode;
    public string FiatFormattedPrice;
    public long FiatPriceAmountMicros;
    public string FiatPriceCurrencyCode;
}

[System.Serializable]
public class SubscriptionOfferDetails
{
    public PricingPhases PricingPhases;
    public TrialDetails TrialDetails;
}

[System.Serializable]
public class PricingPhases
{
    public List<PricingPhase> PricingPhaseList;
}

[System.Serializable]
public class PricingPhase
{
    public string BillingPeriod;
    public string FormattedPrice;
    public long PriceAmountMicros;
    public string PriceCurrencyCode;
    public string AppcFormattedPrice;
    public long AppcPriceAmountMicros;
    public string AppcPriceCurrencyCode;
    public string FiatFormattedPrice;
    public long FiatPriceAmountMicros;
    public string FiatPriceCurrencyCode;
}

[System.Serializable]
public class TrialDetails
{
    public string Period;
    public string PeriodEndDate;
}
