package com.technopark.smartbiz.businessLogic.employees;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.technopark.smartbiz.R;
import com.technopark.smartbiz.Utils;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.database.ContractClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.technopark.smartbiz.Utils.isResponseSuccess;

public class EmployeeRegistrationActivity extends AppCompatActivity implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {

	public static final String TEMPORARY_PASSWORD = "TEMPORARY_PASSWORD";

	private static final String LOG = "EmployeeRegister";

	private JSONObject employeeJsonObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_employee_registration);

		Button submitButton = (Button) findViewById(R.id.activity_employee_registration_button_submit);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String firstName = ((EditText) findViewById(R.id.activity_employee_registration_first_name)).getText().toString();
				final String lastName = ((EditText) findViewById(R.id.activity_employee_registration_last_name)).getText().toString();
				final String fatherName = ((EditText) findViewById(R.id.activity_employee_registration_father_name)).getText().toString();

				if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
					sendRegisterEmployee(firstName, lastName, fatherName);
				}
				// TODO Show error message
			}
		});
	}

	private void sendRegisterEmployee(String firstName, String lastName, String fatherName) {
		Log.d(LOG, "sendRegisterEmployee");

		Map<String, String> map = new HashMap<>();
		map.put(ContractClass.Employees.COLUMN_NAME_FIRST_NAME, firstName);
		map.put(ContractClass.Employees.COLUMN_NAME_LAST_NAME, lastName);

		if (!TextUtils.isEmpty(fatherName)) {
			map.put(ContractClass.Employees.COLUMN_NAME_FATHER_NAME, fatherName);
		}

		employeeJsonObject = new JSONObject(map);

		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Employee.URL_EMPLOYEE_REGISTRATION, employeeJsonObject, this, this)
				.execute(HttpsHelper.Method.POST);
	}

	@Override
	public void onPreExecute() {
		showProgress(true);
	}

	@Override
	public void onPostExecute(JSONObject responseJsonObject) {
		JSONObject dbJsonObject = Utils.mergeJsonObject(employeeJsonObject, responseJsonObject);
		Log.d(LOG, dbJsonObject.toString());

		try {
			if (isResponseSuccess(dbJsonObject.getInt(HttpsHelper.RESPONSE_CODE))) {
				keepTempPassword(dbJsonObject);
				saveEmployee(dbJsonObject);
			}
			// TODO Show error message
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(getApplicationContext(), EmployeeListActivity.class);
		startActivity(intent);
	}

	private void saveEmployee(JSONObject jsonObject) {
		String[] names = new String[]{
				ContractClass.Employees.COLUMN_NAME_FIRST_NAME,
				ContractClass.Employees.COLUMN_NAME_LAST_NAME,
				ContractClass.Employees.COLUMN_NAME_FATHER_NAME,
				ContractClass.Employees.COLUMN_NAME_LOGIN,
		};

		ContentValues cv = Utils.jsonToContentValues(jsonObject, names);
		getContentResolver().insert(ContractClass.Employees.CONTENT_URI, cv);
	}

	private void keepTempPassword(JSONObject jsonObject) {
		if (jsonObject.has(ContractClass.Employees.COLUMN_NAME_LOGIN)
				&& jsonObject.has(ContractClass.Employees.TEMPORARY_PASSWORD)) {
			try {
				SharedPreferences sharedPreferences = getSharedPreferences(TEMPORARY_PASSWORD, MODE_PRIVATE);
				sharedPreferences.edit()
						.putString(
								jsonObject.getString(ContractClass.Employees.COLUMN_NAME_LOGIN),
								jsonObject.getString(ContractClass.Employees.TEMPORARY_PASSWORD)
						)
						.apply();
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void showProgress(boolean status) {
		View button = findViewById(R.id.activity_employee_registration_button_submit);
		View progressbar = findViewById(R.id.activity_employee_registration_progressbar);

		if (status) {
			button.setVisibility(View.GONE);
			progressbar.setVisibility(View.VISIBLE);
		}
		else {
			button.setVisibility(View.VISIBLE);
			progressbar.setVisibility(View.GONE);
		}
	}
}
