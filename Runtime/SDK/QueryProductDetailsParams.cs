using System;
using System.Collections.Generic;
using System.Linq;

public class QueryProductDetailsParams
{
    public List<Product> ProductList { get; }

    private QueryProductDetailsParams(List<Product> productList)
    {
        ProductList = productList;
    }

    public class Builder
    {
        private List<Product> productList = new List<Product>();

        public Builder SetProductList(List<Product> productList)
        {
            if (productList == null || productList.Count == 0)
            {
                throw new ArgumentException("Product list must not be empty.");
            }

            var distinctProductTypes = productList.Select(p => p.ProductType).Distinct().ToList();
            if (distinctProductTypes.Count != 1)
            {
                throw new ArgumentException("All products must be of the same type.");
            }

            var distinctProductIds = productList.Select(p => p.ProductId).Distinct().ToList();
            if (distinctProductIds.Count != productList.Count)
            {
                throw new ArgumentException("Product id should not be repeated.");
            }

            this.productList = productList;
            return this;
        }

        public QueryProductDetailsParams Build()
        {
            if (productList.Count == 0)
            {
                throw new InvalidOperationException("Product list must not be empty.");
            }

            return new QueryProductDetailsParams(productList);
        }
    }

    public static Builder NewBuilder()
    {
        return new Builder();
    }

    public class Product
    {
        public string ProductId { get; }
        public string ProductType { get; }

        private Product(string productId, string productType)
        {
            ProductId = productId;
            ProductType = productType;
        }

        public class Builder
        {
            private string productId;
            private string productType;

            public Builder SetProductId(string productId)
            {
                this.productId = productId;
                return this;
            }

            public Builder SetProductType(string productType)
            {
                this.productType = productType;
                return this;
            }

            public Product Build()
            {
                if (string.IsNullOrEmpty(productId))
                {
                    throw new InvalidOperationException("Product id must be provided.");
                }

                if (string.IsNullOrEmpty(productType))
                {
                    throw new InvalidOperationException("Product type must be provided.");
                }

                return new Product(productId, productType);
            }
        }

        public static Builder NewBuilder()
        {
            return new Builder();
        }
    }
}