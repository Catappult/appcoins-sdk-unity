using System;
using System.Collections.Generic;
using System.Serializable;

public class ProductDetails
{
    public string ProductId { get; }
    public string ProductType { get; }
    public string Title { get; }
    public string Description { get; }
    public OneTimePurchaseOfferDetails OneTimeOfferDetails { get; }
    public List<SubscriptionOfferDetails> SubscriptionOfferDetailsList { get; }

    internal ProductDetails(
        string productId,
        string productType,
        string title,
        string description = null,
        OneTimePurchaseOfferDetails oneTimeOfferDetails = null,
        List<SubscriptionOfferDetails> subscriptionOfferDetailsList = null)
    {
        ProductId = productId;
        ProductType = productType;
        Title = title;
        Description = description;
        OneTimeOfferDetails = oneTimeOfferDetails;
        SubscriptionOfferDetailsList = subscriptionOfferDetailsList;
    }

    public class OneTimePurchaseOfferDetails
    {
        public string FormattedPrice { get; }
        public long PriceAmountMicros { get; }
        public string PriceCurrencyCode { get; }
        public string AppcFormattedPrice { get; }
        public long AppcPriceAmountMicros { get; }
        public string AppcPriceCurrencyCode { get; }
        public string FiatFormattedPrice { get; }
        public long FiatPriceAmountMicros { get; }
        public string FiatPriceCurrencyCode { get; }

        internal OneTimePurchaseOfferDetails(
            string formattedPrice,
            long priceAmountMicros,
            string priceCurrencyCode,
            string appcFormattedPrice,
            long appcPriceAmountMicros,
            string appcPriceCurrencyCode,
            string fiatFormattedPrice,
            long fiatPriceAmountMicros,
            string fiatPriceCurrencyCode)
        {
            FormattedPrice = formattedPrice;
            PriceAmountMicros = priceAmountMicros;
            PriceCurrencyCode = priceCurrencyCode;
            AppcFormattedPrice = appcFormattedPrice;
            AppcPriceAmountMicros = appcPriceAmountMicros;
            AppcPriceCurrencyCode = appcPriceCurrencyCode;
            FiatFormattedPrice = fiatFormattedPrice;
            FiatPriceAmountMicros = fiatPriceAmountMicros;
            FiatPriceCurrencyCode = fiatPriceCurrencyCode;
        }
    }

    public class SubscriptionOfferDetails
    {
        public PricingPhases PricingPhases { get; }
        public TrialDetails TrialDetails { get; }

        internal SubscriptionOfferDetails(PricingPhases pricingPhases, TrialDetails trialDetails = null)
        {
            PricingPhases = pricingPhases;
            TrialDetails = trialDetails;
        }
    }

    public class PricingPhases
    {
        public List<PricingPhase> PricingPhaseList { get; }

        internal PricingPhases(List<PricingPhase> pricingPhases)
        {
            PricingPhaseList = pricingPhases ?? throw new ArgumentNullException(nameof(pricingPhases));
        }
    }

    public class PricingPhase
    {
        public string BillingPeriod { get; }
        public string FormattedPrice { get; }
        public long PriceAmountMicros { get; }
        public string PriceCurrencyCode { get; }
        public string AppcFormattedPrice { get; }
        public long AppcPriceAmountMicros { get; }
        public string AppcPriceCurrencyCode { get; }
        public string FiatFormattedPrice { get; }
        public long FiatPriceAmountMicros { get; }
        public string FiatPriceCurrencyCode { get; }

        internal PricingPhase(
            string billingPeriod,
            string formattedPrice,
            long priceAmountMicros,
            string priceCurrencyCode,
            string appcFormattedPrice,
            long appcPriceAmountMicros,
            string appcPriceCurrencyCode,
            string fiatFormattedPrice,
            long fiatPriceAmountMicros,
            string fiatPriceCurrencyCode)
        {
            BillingPeriod = billingPeriod;
            FormattedPrice = formattedPrice;
            PriceAmountMicros = priceAmountMicros;
            PriceCurrencyCode = priceCurrencyCode;
            AppcFormattedPrice = appcFormattedPrice;
            AppcPriceAmountMicros = appcPriceAmountMicros;
            AppcPriceCurrencyCode = appcPriceCurrencyCode;
            FiatFormattedPrice = fiatFormattedPrice;
            FiatPriceAmountMicros = fiatPriceAmountMicros;
            FiatPriceCurrencyCode = fiatPriceCurrencyCode;
        }
    }

    public class TrialDetails
    {
        public string Period { get; }
        public string PeriodEndDate { get; }

        internal TrialDetails(string period, string periodEndDate)
        {
            Period = period;
            PeriodEndDate = periodEndDate;
        }
    }
}
