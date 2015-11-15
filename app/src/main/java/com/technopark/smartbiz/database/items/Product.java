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
    private int priceSellingProduct;
    private int pricePurchaseProduct;
    private int productBarcode;
    private int count;
    private long id;


    public Product(String productName, String descriptionProduct, String photoPath, int priceSellingProduct, int pricePurchaseProduct, int productBarcode, int count) {
        this.productName = productName;
        this.descriptionProduct = descriptionProduct;
        this.photoPath = photoPath;
        this.productBarcode = productBarcode;
        this.count = count;
        this.priceSellingProduct = priceSellingProduct;
        this.pricePurchaseProduct = pricePurchaseProduct;
    }

    public Product(String productName, String descriptionProduct, String photoPath, int priceSellingProduct, int pricePurchaseProduct, int productBarcode, int count, long id) {
        this.productName = productName;
        this.descriptionProduct = descriptionProduct;
        this.photoPath = photoPath;
        this.productBarcode = productBarcode;
        this.count = count;
        this.priceSellingProduct = priceSellingProduct;
        this.pricePurchaseProduct = pricePurchaseProduct;
        this.id = id;
    }

    public Product(Parcel source) {
        productName = source.readString();
        descriptionProduct = source.readString();
        photoPath = source.readString();
        priceSellingProduct = source.readInt();
        pricePurchaseProduct = source.readInt();
        productBarcode = source.readInt();
        count = source.readInt();
        id = source.readLong();
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

    public Check getCheck () {
        return new Check(productName, photoPath, priceSellingProduct, pricePurchaseProduct, id, count);
    }

    public int getPricePurchaseProduct() {
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

    public void setProductBarcode(int productBarcode) {
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
        dest.writeInt(priceSellingProduct);
        dest.writeInt(pricePurchaseProduct);
        dest.writeInt(productBarcode);
        dest.writeInt(count);
        dest.writeLong(id);
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
