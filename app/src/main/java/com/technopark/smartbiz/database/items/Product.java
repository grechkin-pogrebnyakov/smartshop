package com.technopark.smartbiz.database.items;

import android.graphics.Bitmap;

/**
 * Created by Abovyan on 18.10.15.
 */
public class Product {

    private String nameProduct;
    private String descriptionProduct;
    private String photoPath;
    private int productBarcode;
    private int count;


    public int getProductBarcode() {
        return productBarcode;
    }

    public String getNameProduct() {
        return nameProduct;
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

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
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
