package com.technopark.smartbiz.businessLogic.deleteProduct;

/**
 * Created by Abovyan on 12.12.15.
 */

import android.support.v4.app.DialogFragment;
import android.view.animation.Animation;

import com.technopark.smartbiz.businessLogic.showProducts.ListAddedProducts;

/**
 * Listenter служит для удаления айтема после того, как анимация удаления завершилась
 */
public class DeleteAnimationListenter implements Animation.AnimationListener
{
	private ListAddedProducts listAddedProducts;
	public DeleteAnimationListenter(ListAddedProducts listAddedProducts)
	{
		this.listAddedProducts = listAddedProducts;
	}
	@Override
	public void onAnimationEnd(Animation arg0) {
		// Create an instance of the dialog fragment and show it
		DialogFragment dialog = new DeleteProductFromListDialogFragment();
		dialog.show(listAddedProducts.getSupportFragmentManager(), "NoticeDialogFragment");
	}

	@Override
	public void onAnimationRepeat(Animation animation) {


	}

	@Override
	public void onAnimationStart(Animation animation) {

	}
}
