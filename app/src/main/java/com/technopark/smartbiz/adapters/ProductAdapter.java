package com.technopark.smartbiz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.items.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abovyan on 31.10.15.
 */
public class ProductAdapter extends BaseAdapter {

    private List<Product> listProduct;
    private LayoutInflater layoutInflater;

    public ProductAdapter (Context context, List<Product> listProduct) {
        this.listProduct = listProduct;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ProductAdapter (Context context) {
        this.listProduct = new ArrayList<>();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(Product product) {
        listProduct.add(product);
    }

    @Override
    public int getCount() {
        return listProduct.size();
    }

    @Override
    public Object getItem(int i) {
        return listProduct.get(i);
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
        Product product = getProduct(i);
        TextView nameProductTextView = (TextView) tempView.findViewById(R.id.clap_name_product_textView);
        TextView priceOfTheProductTextView = (TextView) tempView.findViewById(R.id.clap_price_of_the_product);
        TextView countProductTextView = (TextView) tempView.findViewById(R.id.clap_count_product);
        nameProductTextView.setText(product.getProductName());
        countProductTextView.setText(stringBuilder.append("Кол-во : ").append(String.valueOf(product.getCount())));
        stringBuilder.setLength(0);
        priceOfTheProductTextView.setText(stringBuilder.append("Цена : ").append( String.valueOf( product.getPriceSellingProduct() ) ).append(" р.") );
        return tempView;
    }

    private Product getProduct (int position) {
        return (Product) getItem(position);
    }
}
