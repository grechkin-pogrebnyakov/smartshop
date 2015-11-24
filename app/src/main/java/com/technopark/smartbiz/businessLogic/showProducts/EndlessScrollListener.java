package com.technopark.smartbiz.businessLogic.showProducts;

import android.widget.AbsListView;

/**
 * Created by Abovyan on 19.10.15.
 */
public abstract class EndlessScrollListener implements AbsListView.OnScrollListener {
    public EndlessScrollListener() {
    }

    public abstract void loadData(int offset);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount >= totalItemCount) {
            loadData(totalItemCount);
        }
    }
}
