package com.technopark.smartbiz.database.items;

/**
 * Created by Abovyan on 18.10.15.
 */
public class Product implements ItemForProductAdapter {

    private String productName;
    private String descriptionProduct;
    private String photoPath;
    private int priceSellingProduct;
    private int pricePurchaseProduct;
    private int productBarcode;
    private int count;


    public Product(String productName, String descriptionProduct, String photoPath, int priceSellingProduct, int pricePurchaseProduct, int productBarcode, int count) {
        this.productName = productName;
        this.descriptionProduct = descriptionProduct;
        this.photoPath = photoPath;
        this.productBarcode = productBarcode;
        this.count = count;
        this.priceSellingProduct = priceSellingProduct;
        this.pricePurchaseProduct = pricePurchaseProduct;
    }

    public int getProductBarcode() {
        return productBarcode;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescriptionProduct() {
        return descriptionProduct;
    }

    public int getCount() {
        return count;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public int getPriceSellingProduct() {
        return priceSellingProduct;
    }

    public int getPricePurchaseProduct() {
        return pricePurchaseProduct;
    }

    public void setPricePurchaseProduct(int pricePurchaseProduct) {
        this.pricePurchaseProduct = pricePurchaseProduct;
    }

    public void setPriceSellingProduct(int priceSellingProduct) {
        this.priceSellingProduct = priceSellingProduct;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setDescriptionProduct(String descriptionProduct) {
        this.descriptionProduct = descriptionProduct;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setProductBarcode(int productBarcode) {
        this.productBarcode = productBarcode;
    }
}
