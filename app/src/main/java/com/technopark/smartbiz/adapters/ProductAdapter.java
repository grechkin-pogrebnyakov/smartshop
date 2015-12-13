package com.technopark.smartbiz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.database.items.ItemForProductAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abovyan on 31.10.15.
 */
public class ProductAdapter extends BaseAdapter {

	private List<ItemForProductAdapter> listItems;
	private LayoutInflater layoutInflater;

	public ProductAdapter(Context context, List<ItemForProductAdapter> listItems) {
		this.listItems = listItems;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public ProductAdapter(Context context) {
		this.listItems = new ArrayList<>();
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addItem(ItemForProductAdapter product) {
		listItems.add(product);
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int i) {
		return listItems.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		StringBuilder stringBuilder = new StringBuilder();
		View tempView = view;
		if (tempView == null) {
			tempView = layoutInflater.inflate(R.layout.product_layout, viewGroup, false);
		}
		ItemForProductAdapter product = getProduct(i);
		TextView nameProductTextView = (TextView) tempView.findViewById(R.id.clap_name_product_textView);
		TextView priceOfTheProductTextView = (TextView) tempView.findViewById(R.id.clap_price_of_the_product);
		TextView countProductTextView = (TextView) tempView.findViewById(R.id.clap_count_product);
		ImageView imageProductView = (ImageView) tempView.findViewById(R.id.clap_photo_product);

		nameProductTextView.setText(product.getProductName());
		countProductTextView.setText(stringBuilder.append("Кол-во : ").append(String.valueOf(product.getCount())));
		stringBuilder.setLength(0);
		priceOfTheProductTextView.setText(stringBuilder.append("Цена : ").append(String.valueOf(product.getPriceSellingProduct())).append(" р."));

		File imageFile = new File(product.getPhotoPath());

		if (imageFile.exists()) {
			Picasso.with(layoutInflater.getContext())
					.load(imageFile)
					.resize(300, 300)
					.into(imageProductView);
		}
		else {
			final String imageUrl = SmartShopUrl.URL_HOST + "/" + product.getPhotoPath();

			Picasso.with(layoutInflater.getContext())
					.load(imageUrl)
					.into(imageProductView);
		}

		return tempView;
	}

	private ItemForProductAdapter getProduct(int position) {
		return (ItemForProductAdapter) getItem(position);
	}

	public List<ItemForProductAdapter> getListItems() {
		return listItems;
	}
}
