package com.technopark.smartbiz.buisnessLogic.shopProfile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.technopark.smartbiz.buisnessLogic.main.MainActivity;
import com.technopark.smartbiz.R;

public class ShopProfileActivity extends AppCompatActivity {
	private SharedPreferences sharedPreferences;

	private EditText nameEditText;
	private EditText addressEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_profile);

		nameEditText = (EditText) findViewById(R.id.activity_shop_profile_edittext_name);
		addressEditText = (EditText) findViewById(R.id.activity_shop_profile_edittext_address);

		Button saveButton = (Button) findViewById(R.id.activity_shop_profile_button_save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();

				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
			}
		});


		loadData();
	}

	private void loadData() {
		sharedPreferences = getPreferences(MODE_PRIVATE);
		String shopName = sharedPreferences.getString("SHOP_NAME", "");
		String shopAddress = sharedPreferences.getString("SHOP_ADDRESS", "");

		nameEditText.setText(shopName);
		addressEditText.setText(shopAddress);
	}

	private void saveData() {
		String shopName = nameEditText.getText().toString();
		String shopAddress = addressEditText.getText().toString();

		sharedPreferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString("SHOP_NAME", shopName)
				.putString("SHOP_ADDRESS", shopAddress)
				.apply();
	}
}
