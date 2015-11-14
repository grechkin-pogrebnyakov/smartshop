package com.technopark.smartbiz.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
        ImageView imageProductView = (ImageView) tempView.findViewById(R.id.clap_photo_product);

        nameProductTextView.setText(product.getProductName());
        countProductTextView.setText(stringBuilder.append("Кол-во : ").append(String.valueOf(product.getCount())));
        stringBuilder.setLength(0);
        priceOfTheProductTextView.setText(stringBuilder.append("Цена : ").append( String.valueOf( product.getPriceSellingProduct() ) ).append(" р.") );
        setPic(imageProductView, product.getPhotoPath());
        return tempView;
    }

    private Product getProduct (int position) {
        return (Product) getItem(position);
    }

    private void setPic(ImageView imageProductView, String photoPath) {
        // Get the dimensions of the View
        int targetW = 50;
        int targetH = 50;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;


        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        imageProductView.setImageBitmap(bitmap);
    }
}
