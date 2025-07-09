using System;

[System.Serializable]
public class BillingResult
{
    public int? ResponseCode { get; }
    public string DebugMessage { get; }

    internal BillingResult(int? responseCode = null, string debugMessage = "")
    {
        ResponseCode = responseCode;
        DebugMessage = debugMessage;
    }

    public override string ToString()
    {
        return $"ResponseCode: {ResponseCode}, DebugMessage: {DebugMessage}";
    }

    public class Builder
    {
        private int? responseCode = null;
        private string debugMessage = "";

        internal Builder() { }

        public Builder SetResponseCode(int responseCode)
        {
            this.responseCode = responseCode;
            return this;
        }

        public Builder SetDebugMessage(string debugMessage)
        {
            this.debugMessage = debugMessage;
            return this;
        }

        public BillingResult Build()
        {
            return new BillingResult(responseCode, debugMessage);
        }
    }

    public static Builder NewBuilder()
    {
        return new Builder();
    }
}
