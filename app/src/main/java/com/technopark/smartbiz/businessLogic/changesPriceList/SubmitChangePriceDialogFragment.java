package com.technopark.smartbiz.businessLogic.changesPriceList;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by titaevskiy.s on 05.12.15
 */
public class SubmitChangePriceDialogFragment extends DialogFragment {

	NoticeDialogListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (NoticeDialogListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Подтвердить изменение цены?")
				.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mListener.onDialogPositiveClick(SubmitChangePriceDialogFragment.this);
					}
				})
				.setNegativeButton("Отмена", null);

		return builder.create();
	}

	public interface NoticeDialogListener {
		void onDialogPositiveClick(DialogFragment dialog);
	}
}
