package com.technopark.smartbiz.businessLogic.employees;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.technopark.smartbiz.R;

public class EmployeeRegistrationActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_employee_registration);

		Button submitButton = (Button) findViewById(R.id.activity_employee_registration_button_submit);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				registerEmployee();

				Intent intent = new Intent(getApplicationContext(), EmployeeListActivity.class);
				startActivity(intent);
			}
		});
	}

	private void registerEmployee() {
		Log.d("EmployeeRegister", "registerEmployee");
		// TODO
	}
}
