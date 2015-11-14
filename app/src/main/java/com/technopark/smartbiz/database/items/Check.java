package com.technopark.smartbiz.database.items;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abovyan on 14.11.15.
 */
public class Check implements Parcelable, ItemForProductAdapter {

	private String productName;
	private String photoPath;
	private int priceSellingProduct;
	private int pricePurchaseProduct;
	private long idFromProductsTable;
	private int count;


	public Check(String productName, String photoPath, int priceSellingProduct, int pricePurchaseProduct, long idFromProductsTable, int count) {
		this.productName = productName;
		this.photoPath = photoPath;
		this.idFromProductsTable = idFromProductsTable;
		this.count = count;
		this.priceSellingProduct = priceSellingProduct;
		this.pricePurchaseProduct = pricePurchaseProduct;
	}

	public Check(Parcel source) {
		productName = source.readString();
		photoPath = source.readString();
		priceSellingProduct = source.readInt();
		pricePurchaseProduct = source.readInt();
		idFromProductsTable = source.readLong();
		count = source.readInt();
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
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

	public long getIdFromProductsTable() {
		return idFromProductsTable;
	}

	public void setIdFromProductsTable(long idFromProductsTable) {
		this.idFromProductsTable = idFromProductsTable;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(productName);
		dest.writeString(photoPath);
		dest.writeInt(priceSellingProduct);
		dest.writeInt(pricePurchaseProduct);
		dest.writeLong(idFromProductsTable);
		dest.writeInt(count);
	}

	public static final Parcelable.Creator<Check> CREATOR = new Creator<Check>() {
		@Override
		public Check createFromParcel(Parcel source) {
			return new Check(source);
		}

		@Override
		public Check[] newArray(int size) {
			return new Check[size];
		}
	};
}
