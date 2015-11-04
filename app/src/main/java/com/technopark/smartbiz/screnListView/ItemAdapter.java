package com.technopark.smartbiz.screnListView;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.RoundedTransformation;
import com.technopark.smartbiz.database.items.Product;

import java.util.ArrayList;

/**
 * Created by Abovyan on 19.10.15.
 */
public class ItemAdapter extends BaseAdapter {
    private final Context mContext;
    private ArrayList<Product> itemList;

    public ItemAdapter(Context context, ArrayList<Product> itemList) {
        this.mContext = context;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Product getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.layout_product, parent, false);

            holder = new ViewHolder();

            holder.productImageView = (ImageView) convertView.findViewById(R.id.product_image);
            holder.nameProductView = (TextView) convertView.findViewById(R.id.product_name);
            holder.countProduct = (TextView) convertView.findViewById(R.id.product_count);
            holder.priceProductView = (TextView) convertView.findViewById(R.id.product_price);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // TODO debug
        // Picasso.with(mContext).invalidate(itemList.get(position).getImageUrlMedium());
        // Picasso.with(mContext).setIndicatorsEnabled(true);

        // TODO error and placeholder

        final int itemBgColor = mContext.getResources().getColor(R.color.item_bg);
        /*Picasso.with(mContext)
                .load(getItem(position).getUserAvatarUrl())
                .transform(new RoundedTransformation(100, 0))    // Преобразует (скругляет углы)
                .fit()                                          // Подгоняет до размера в ImageView
                .into(holder.productImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.productImageView.setBackgroundColor(itemBgColor);
                    }

                    @Override
                    public void onError() {}
                });

        Picasso.with(mContext)
                .load(getItem(position).getImageUrlMedium())
                .into(holder.productImageView);

        holder.nameProductView.setText(getItem(position).getUserName());
        holder.countProduct.setText(getItem(position).getDescription());
        holder.priceProductView.setText(getItem(position).getDescription());*/


        return convertView;
    }

    public void addItem(Product item) {
        itemList.add(item);
    }


    static class ViewHolder {
        ImageView productImageView;
        TextView nameProductView;
        TextView countProduct;
        TextView priceProductView;
    }
}
