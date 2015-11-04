package com.technopark.smartbiz.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.items.Product;
import com.technopark.smartbiz.screnListView.EndlessScrollListener;
import com.technopark.smartbiz.screnListView.ItemAdapter;

import java.util.ArrayList;

/**
 * Created by Abovyan on 19.10.15.
 */
public class ProductFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Product> itemsList = new ArrayList<>();
    ItemAdapter itemAdapter;
    final String LOG_TAG = "myLogs";
    static final String from[] = { "description", "download_time" };
    // Уникальный идентификатор загрузчика
    private static final int LOADER_ID = 1;
    private SimpleCursorAdapter adapter;
    // The callbacks through which we will interact with the LoaderManager.
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    final Uri CONTENT_URI = Uri
            .parse("content://ru.tech_mail.voto.data/Items");
    final Uri CONTENT_URI_ID = Uri
            .parse("content://ru.tech_mail.voto.data/Items/2");

    public ProductFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_product, container, false);
        mCallbacks = this;
        // Инициализируем загрузчик с идентификатором '1' и 'mCallbacks'.
        // Если загрузчик не существует, то он будет создан,
        // иначе он будет перезапущен.
        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, null, mCallbacks);
        ListView listView = (ListView) rootView.findViewById(R.id.clap_name_product_textView);
        itemAdapter = new ItemAdapter(getActivity(), itemsList);
        listView.setAdapter(itemAdapter);
        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void loadData(int offset) {
                // TODO
            }
        });
        return rootView;
    }




    private ArrayList<Product> getItems(Cursor cursor) {

        return new ArrayList<>();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Создаем новый CursorLoader с нужными параметрами.
        return new CursorLoader(this.getActivity(), CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Если используется несколько загрузчиков, то удобнее через оператор switch-case
        switch (loader.getId()) {
            case LOADER_ID:
                // Данные загружены и готовы к использованию
                adapter.swapCursor(data);
                if (data.moveToFirst()) {
                    do {
                        String username = "Dima", avatarUserPath = "https://pp.vk.me/c9591/v9591001/74/bGqB3eciXRc.jpg";
                        String imagePath = data.getString(data.getColumnIndex("photo_medium"));
                        String imageUrlLarge = data.getString(data.getColumnIndex("photo_large"));
                        String description = data.getString(data.getColumnIndex("description"));
                        Product item = new Product("", "","", 0, 0, 0, 0);
                        itemAdapter.addItem(item);
                    }while (data.moveToNext());
                }

//                new Item("MAKS ",
//                        "https://pp.vk.me/c9591/v9591001/74/bGqB3eciXRc.jpg",
//                        data.getString(data.getColumnIndex("photo_medium")),
//                        data.getString(data.getColumnIndex("photo_large")),
//                        data.getString(data.getColumnIndex("description")) );
                break;
        }
        // список теперь содержит данные на экране
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Если по какой-то причине данные не доступны, то удаляем ссылки на старые данные,
        // заменяя их пустым курсором
        adapter.swapCursor(null);
    }

}
