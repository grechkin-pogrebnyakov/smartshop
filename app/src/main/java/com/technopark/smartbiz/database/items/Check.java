package com.technopark.smartbiz.database.items;

/**
 * Created by Abovyan on 14.11.15.
 */
public class Check {

    private String checkName;
    private String photoPath;
    private int priceSellingProduct;
    private int pricePurchaseProduct;
    private int idFromProductsTable;
    private int count;


    public Check(String checkName, String photoPath, int priceSellingProduct, int pricePurchaseProduct, int idFromProductsTable, int count) {
        this.checkName = checkName;
        this.photoPath = photoPath;
        this.idFromProductsTable = idFromProductsTable;
        this.count = count;
        this.priceSellingProduct = priceSellingProduct;
        this.pricePurchaseProduct = pricePurchaseProduct;
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public int getPriceSellingProduct() {
        return priceSellingProduct;
    }

    public void setPriceSellingProduct(int priceSellingProduct) {
        this.priceSellingProduct = priceSellingProduct;
    }

    public int getPricePurchaseProduct() {
        return pricePurchaseProduct;
    }

    public void setPricePurchaseProduct(int pricePurchaseProduct) {
        this.pricePurchaseProduct = pricePurchaseProduct;
    }

    public int getIdFromProductsTable() {
        return idFromProductsTable;
    }

    public void setIdFromProductsTable(int idFromProductsTable) {
        this.idFromProductsTable = idFromProductsTable;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
