package com.technopark.smartbiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.technopark.smartbiz.businessLogic.productSales.CheckActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract.Role;

/**
 * Created by titaevskiy.s on 10.12.15
 */

/**
 * Прокси класс для перехода к главному экрану роли (владелец, продавец и др.)
 */
public class HomeProxyActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent();

		Role role = Role.SELLER;

		switch (role) {
			case OWNER:
				intent.setClass(this, MainActivity.class);
				break;
			case SELLER:
				intent.setClass(this, CheckActivity.class);
				break;
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
