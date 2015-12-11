package com.technopark.smartbiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.technopark.smartbiz.businessLogic.addProduct.AddProductActivity;
import com.technopark.smartbiz.businessLogic.changesPriceList.ListChangesPriceActivity;
import com.technopark.smartbiz.businessLogic.discard.DiscardActivity;
import com.technopark.smartbiz.businessLogic.employees.EmployeeListActivity;
import com.technopark.smartbiz.businessLogic.employees.EmployeeRegistrationActivity;
import com.technopark.smartbiz.businessLogic.productSales.CheckActivity;
import com.technopark.smartbiz.businessLogic.shopProfile.ShopProfileActivity;
import com.technopark.smartbiz.businessLogic.showProducts.ListAddedProducts;
import com.technopark.smartbiz.businessLogic.supply.SupplyActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.AccessControl;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by titaevskiy.s on 11.12.15
 */
public class ActivityWithNavigationDrawer extends AppCompatActivity {

	private final ArrayList<Pair<Permission, IDrawerItem>> permissionToIDrawerItem = new ArrayList<Pair<Permission, IDrawerItem>>() {{
		add(new Pair<Permission, IDrawerItem>(
				Permission.FOR_ALL,
				new PrimaryDrawerItem().withName("Home")
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.FOR_ALL,
				new DividerDrawerItem()
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.FOR_ALL,
				new SecondaryDrawerItem().withName("Список продуктов").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(ActivityWithNavigationDrawer.this, ListAddedProducts.class);
						startActivity(intent);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.OWNER,
				new SecondaryDrawerItem().withName("Добавить продукт").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(ActivityWithNavigationDrawer.this, AddProductActivity.class);
						startActivity(intent);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.SELLER,
				new DividerDrawerItem()
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.SELLER,
				new SecondaryDrawerItem().withName("Продать").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(ActivityWithNavigationDrawer.this, CheckActivity.class);
						startActivity(intent);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.SELLER,
				new SecondaryDrawerItem().withName("Поставка").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(getApplicationContext(), SupplyActivity.class);
						startActivity(intent);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.SELLER,
				new SecondaryDrawerItem().withName("Списание").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(ActivityWithNavigationDrawer.this, DiscardActivity.class);
						startActivity(intent);
						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.OWNER,
				new DividerDrawerItem()
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.OWNER,
				new SecondaryDrawerItem().withName("Список сотрудников").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(ActivityWithNavigationDrawer.this, EmployeeListActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						startActivity(intent);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.OWNER,
				new SecondaryDrawerItem().withName("Зарегистрировать сотрудника").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(ActivityWithNavigationDrawer.this, EmployeeRegistrationActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						startActivity(intent);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.FOR_ALL,
				new DividerDrawerItem()
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.FOR_ALL,
				new SecondaryDrawerItem().withName("Список изменений").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent intent = new Intent(ActivityWithNavigationDrawer.this, ListChangesPriceActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						startActivity(intent);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.OWNER,
				new SecondaryDrawerItem().withName("Профиль магазина").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						Intent show = new Intent(ActivityWithNavigationDrawer.this, ShopProfileActivity.class);
						show.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						startActivity(show);

						return true;
					}
				})
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.FOR_ALL,
				new DividerDrawerItem()
		));
		add(new Pair<Permission, IDrawerItem>(
				Permission.FOR_ALL,
				new SecondaryDrawerItem().withName("Выйти").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.APP_PREFERENCES, MODE_PRIVATE);

						sharedPreferences.edit()
								.remove(UserIdentificationContract.STATUS_AUTHORIZATION_KEY)
								.remove(UserIdentificationContract.TOKEN_AUTHORIZATION).apply();

						final Intent intent = new Intent(ActivityWithNavigationDrawer.this, LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

						startActivity(intent);

						//if (Utils.isNetworkEnabled()) {
						//	new LogOut(UserIdentificationContract.REQUEST_CODE_LOG_OUT_ACTION, getApplicationContext(), MainActivity.this).startLogOut();
						//	return true;
						//}
						return true;
					}
				})
		));
	}};

	private Drawer drawer;
	private DrawerBuilder drawerBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupDrawerBuilder();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (drawer == null) {
			drawer = drawerBuilder.build();
		}
	}

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen()) {
			drawer.closeDrawer();
		}
		else {
			super.onBackPressed();
		}
	}

	/**
	 * Создание и заполнение дровер-билдера
	 * Узнаются разрешения текущего пользователя, после чего
	 * из мапы с итемами дровера выбираются доступные пользователю
	 */
	private void setupDrawerBuilder() {
		final List<Permission> permissions = AccessControl.getCurrentUserPermissions(this);

		drawerBuilder = new DrawerBuilder()
				.withActivity(this)
				.withDisplayBelowStatusBar(true)
				.addDrawerItems();

		for (Pair<Permission, IDrawerItem> pair : permissionToIDrawerItem) {
			if (pair.first.equals(Permission.FOR_ALL) || permissions.contains(pair.first)) {
				drawerBuilder.addDrawerItems(pair.second);
			}
		}
	}

	public void setDrawerToolbar(Toolbar toolbar) {
		if (drawerBuilder != null) {
			drawerBuilder.withToolbar(toolbar)
					.withActionBarDrawerToggleAnimated(true);
		}
	}
}
