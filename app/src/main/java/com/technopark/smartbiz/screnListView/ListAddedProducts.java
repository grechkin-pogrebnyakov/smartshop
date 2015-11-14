package com.technopark.smartbiz.screnListView;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.adapters.ProductAdapter;
import com.technopark.smartbiz.database.items.Product;

import java.util.ArrayList;
import java.util.List;

public class ListAddedProducts extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView listViewAddedProducts;
    final String LOG_TAG = "myLogs";
    // Уникальный идентификатор загрузчика
    private static final int LOADER_ID = 1;
    private SimpleCursorAdapter simpleCursorAdapter;
    private ProductAdapter adapter;
    // The callbacks through which we will interact with the LoaderManager.
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    final Uri CONTENT_URI = Uri
            .parse("content://ru.tech_mail.smart_biz.data/products");

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_list_added_products );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar(toolbar);

        mCallbacks = this;
        // Инициализируем загрузчик с идентификатором '1' и 'mCallbacks'.
        // Если загрузчик не существует, то он будет создан,
        // иначе он будет перезапущен.
        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, null, mCallbacks);

        listViewAddedProducts = (ListView) findViewById( R.id.clap_name_product_textView );
        adapter = new ProductAdapter( this );
        listViewAddedProducts.setAdapter( adapter );
        listViewAddedProducts.setOnScrollListener( new EndlessScrollListener() {
            @Override
            public void loadData( int offset ) {

            }
        });
    }

    private List<Product> initData () {
        List<Product> listProduct = new ArrayList<>();
        listProduct.add( new Product("Молоко", "Цельное молоко", "", 53, 23, 12324, 12) );
        listProduct.add( new Product("Чай Lipton", "Чай в пакетиках", "", 150, 12, 1254363, 2) );

        return listProduct;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Создаем новый CursorLoader с нужными параметрами.
        return new CursorLoader( this.getApplicationContext(), CONTENT_URI,
                null, null, null, null );
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor cursor ) {
        // Если используется несколько загрузчиков, то удобнее через оператор switch-case
        switch ( loader.getId() ) {
            case LOADER_ID:
                // Данные загружены и готовы к использованию
                //simpleCursorAdapter.swapCursor( cursor );
                if ( cursor.moveToFirst() ) {
                    do {
                        String nameProduct = cursor.getString(cursor.getColumnIndex("name"));
                        String descriptionProduct = cursor.getString(cursor.getColumnIndex("description"));
                        String photoPath = cursor.getString(cursor.getColumnIndex("photo_path"));
                        int priceSellingProduct = cursor.getInt(cursor.getColumnIndex("price_selling_product"));
                        int pricePurchaseProduct = cursor.getInt(cursor.getColumnIndex("price_cost_product"));
                        int productBarcode = cursor.getInt(cursor.getColumnIndex("barcode"));
                        int countProduct = cursor.getInt(cursor.getColumnIndex("count"));
                        Product product = new Product(nameProduct, descriptionProduct, photoPath, priceSellingProduct,
                                pricePurchaseProduct, productBarcode, countProduct);
                        adapter.addItem( product );
                    } while ( cursor.moveToNext() );
                }
                break;
        }
        // список теперь содержит данные на экране
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader ) {
        // Если по какой-то причине данные не доступны, то удаляем ссылки на старые данные,
        // заменяя их пустым курсором
        //simpleCursorAdapter.swapCursor(null);
    }
}
