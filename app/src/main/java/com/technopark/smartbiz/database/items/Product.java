package com.technopark.smartbiz.database.items;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abovyan on 18.10.15.
 */
public class Product implements Parcelable, ItemForProductAdapter {

    private String productName;
    private String descriptionProduct;
    private String photoPath;
    private double priceSellingProduct;
    private double pricePurchaseProduct;
    private String productBarcode;
    private int count;
    private long id;
    private long priceId;


    public Product(String productName, String descriptionProduct, String photoPath, double priceSellingProduct, double pricePurchaseProduct, String productBarcode, int count, long id, long priceId) {
        this.productName = productName;
        this.descriptionProduct = descriptionProduct;
        this.photoPath = photoPath;
        this.productBarcode = productBarcode;
        this.count = count;
        this.priceSellingProduct = priceSellingProduct;
        this.pricePurchaseProduct = pricePurchaseProduct;
        this.id = id;
        this.priceId = priceId;
    }

    public Product(Parcel source) {
        productName = source.readString();
        descriptionProduct = source.readString();
        photoPath = source.readString();
        priceSellingProduct = source.readDouble();
        pricePurchaseProduct = source.readDouble();
        productBarcode = source.readString();
        count = source.readInt();
        id = source.readLong();
        priceId = source.readLong();
    }

    public String getProductBarcode() {
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

    public double getPriceSellingProduct() {
        return priceSellingProduct;
    }

    public Check getCheck () {
        return new Check(productName, photoPath, priceSellingProduct, pricePurchaseProduct, id, priceId, count);
    }

    public double getPricePurchaseProduct() {
        return pricePurchaseProduct;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productName);
        dest.writeString(descriptionProduct);
        dest.writeString(photoPath);
        dest.writeDouble(priceSellingProduct);
        dest.writeDouble(pricePurchaseProduct);
        dest.writeString(productBarcode);
        dest.writeInt(count);
        dest.writeLong(id);
        dest.writeLong(priceId);
    }

    public static final Parcelable.Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
