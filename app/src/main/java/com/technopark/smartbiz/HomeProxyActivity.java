package com.technopark.smartbiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.technopark.smartbiz.businessLogic.productSales.CheckActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.AccessControl;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract.Role;

/**
 * Created by titaevskiy.s on 10.12.15
 */

/**
 * Прокси класс для перехода к главному экрану роли (владелец, продавец и др.)
 */
public class HomeProxyActivity extends AppCompatActivity {

	private static final String LOG = "HomeProxyActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle bundle = getIntent().getExtras();

		final Intent intent = new Intent();
		if (bundle != null) {
			intent.putExtras(bundle);
		}

		final Role role = AccessControl.getCurrentUserRole(this);
		Log.d(LOG, role.toString());

		switch (role) {
			case OWNER:
				intent.setClass(this, MainActivity.class);
				break;
			case SELLER:
				intent.setClass(this, CheckActivity.class);
				break;
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}
