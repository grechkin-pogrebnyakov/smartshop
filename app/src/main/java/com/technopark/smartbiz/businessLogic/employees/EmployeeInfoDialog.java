package com.technopark.smartbiz.businessLogic.employees;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.technopark.smartbiz.R;

/**
 * Created by titaevskiy.s on 27.11.15.
 */
public class EmployeeInfoDialog extends DialogFragment {

	public static final String EMPLOYEE = "employee";

	private static final String LOG = "EmployeeInfoDialog";

	// TODO Change to Employee class and custom adapter
	private String login;

	public static EmployeeInfoDialog newInstance(String login) {
		Log.d(LOG, "newInstance");

		final EmployeeInfoDialog newDialog = new EmployeeInfoDialog();

		Bundle bundle = new Bundle();
		bundle.putString(EMPLOYEE, login);

		newDialog.setArguments(bundle);

		return newDialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		login = getArguments().getString(EMPLOYEE, "");
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(EmployeeRegistrationActivity.TEMPORARY_PASSWORD, Context.MODE_PRIVATE);
		final String temporaryPassword = sharedPreferences.getString(login, "");

		View view = inflater.inflate(R.layout.dialog_employee_info, null);

		getDialog().setTitle("Информация");
		((TextView) view.findViewById(R.id.dialog_employee_info_login)).setText(login);
		((TextView) view.findViewById(R.id.dialog_employee_info_temporary_password)).setText(temporaryPassword);

		return view;
	}
}
