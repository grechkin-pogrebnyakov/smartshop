package com.technopark.smartbiz.businessLogic.userIdentification;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.technopark.smartbiz.MainActivity;
import com.technopark.smartbiz.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, InteractionWithUI {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	public static final String APP_PREFERENCES = "mysettings";

	private final String ACTION_AUTHORIZATION = "authorization";
	private final String ACTION_REGISTRATION = "registration";
	private final String ACTION_PASSWORD_CHANGE = "passwordChange";
	private final String ACTION_VALIDATION = "validation";
	private String temporaryToken = null;
	private SharedPreferences sharedPreferences;
	// Ссылки на графические компоненты
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;
	private EditText mPasswordRepeatView;
	private View mProgressView;
	private View mLoginFormView;
	private Button registrtionButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		// Инициализация приложения...
		// Инициализируем компоненты формы логина
		mEmailView = (AutoCompleteTextView) findViewById(R.id.activity_login_login_textField);
		populateAutoComplete();
		if (sharedPreferences.contains(UserIdentificationContract.STATUS_AUTHORIZATION_KEY)) {
			String statusAuthorization = sharedPreferences.getString(UserIdentificationContract.STATUS_AUTHORIZATION_KEY, "");
			Log.e("statusAuthorization", statusAuthorization);
			if (statusAuthorization.equals(UserIdentificationContract.SUCCESS_AUTHORIZATION)) {
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
				finish();
			}
		}
		mPasswordRepeatView = (EditText) findViewById(R.id.login_password_repeat);
		mPasswordRepeatView.setVisibility(View.GONE);
		mPasswordView = (EditText) findViewById(R.id.login_password);
		// Слушатель редактирования поля пароля
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					startActionInitiatedByUser(ACTION_VALIDATION);
					return true;
				}
				return false;
			}
		});

		registrationButtons();

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);
	}

	private void registrationButtons() {
		registrtionButton = (Button) findViewById(R.id.login_button_registration);
		registrtionButton.setVisibility(View.GONE);
		registrtionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startActionInitiatedByUser(ACTION_REGISTRATION);
			}
		});

		final Button newAccountButton = (Button) findViewById(R.id.login_button_new_account);
		newAccountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				switch (newAccountButton.getText().toString()) {
					case "Новый акаунт":
						mPasswordRepeatView.setVisibility(View.VISIBLE);
						newAccountButton.setVisibility(View.GONE);
						registrtionButton.setVisibility(View.VISIBLE);
						break;
				}
			}
		});

		Button mEmailSignInButton = (Button) findViewById(R.id.login_button_sing_in);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mPasswordRepeatView.getVisibility() == View.VISIBLE) {
					mPasswordRepeatView.setVisibility(View.GONE);
					registrtionButton.setVisibility(View.GONE);
					newAccountButton.setVisibility(View.VISIBLE);
				}
				startActionInitiatedByUser(ACTION_AUTHORIZATION);

			}
		});

		Button vkAuthButton = (Button) findViewById(R.id.login_button_vkAuth);
		Account[] accounts = AccountManager.get(this).getAccountsByType("com.vkontakte.account");
		if (accounts.length > 0) {
			String buttonText = getString(R.string.action_registration_vk, accounts[0].name);
			vkAuthButton.setText(buttonText);
			vkAuthButton.setBackgroundColor(Color.BLUE);
			vkAuthButton.setTextColor(Color.WHITE);
			vkAuthButton.setVisibility(View.VISIBLE);
			vkAuthButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent("com.vkontakte.android.action.SDK_AUTH", null)
							.putExtra("version", "5")
							.putExtra("client_id", 5093720)
							.putExtra("scope", "friends,photos");
					startActivityForResult(intent, 1);
				}
			});
		}
		else {
			vkAuthButton.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && data != null && data.hasExtra("access_token")) {
			String accessToken = data.getStringExtra("access_token");
			int userId = data.getIntExtra("user_id", 0);
			sharedPreferences.edit().putString(UserIdentificationContract.TOKEN_AUTHORIZATION, accessToken).commit();
			startActivity(new Intent(getApplicationContext(), MainActivity.class));
			Log.i("VK_LOGIN", "accessToken: " + accessToken + ", userId: " + userId);
		}
	}

	private void populateAutoComplete() {
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	/**
	 * Попытка авторизоваться по логину и паролю указанному в форме входа.
	 * Если есть ошибки (некорректный email, недостающие поля, и т. д.),
	 * Представляются ошибки и не производиться попытка авторизации.
	 */
	private void startActionInitiatedByUser(String action) {
//		if (mAuthTask != null) {
//			return;
//		}

		// Сброс ошибок.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Получаем значения введенные пользователем для попытки авторизации.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();
		String repeatPassword = mPasswordRepeatView.getVisibility() == View.VISIBLE ? mPasswordRepeatView.getText().toString() : null;

		boolean cancel = false;
		View focusView = null;

		// Проверка правильности пароля.
		if (!isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Проверка правильности повторного ввода пароля.
		if (repeatPassword != null && !isRepeatPasswordValid(password, repeatPassword)) {
			mPasswordRepeatView.setError(getString(R.string.error_invalid_repeat_password));
			focusView = mPasswordRepeatView;
			cancel = true;
		}

		// Проверка правильности email.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}
		else if (!isEmailValid(email)) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// Была допущена ошибка; не производиться попытка авторизации и фокус устанавливаеться
			// на поле ввода с ошибкой.
			focusView.requestFocus();
		}
		else {
			// Показываем прогресс выполнения задачи аутентификации в background
			// выполняем попытку входа.
			showProgress(true);

			if (isNetworkConnected()) {
				switch (action) {
					case ACTION_AUTHORIZATION:
						new Authorization(UserIdentificationContract.REQUEST_CODE_AUTHORIZATION_ACTION,
								getApplicationContext(), this).startAuthorization(email, password);
						break;
					case ACTION_REGISTRATION:
						new Registration(UserIdentificationContract.REQUEST_CODE_REGISTRATION_ACTION,
								getApplicationContext(), this).startRegistration(email, password, password);
						break;
					case ACTION_PASSWORD_CHANGE:
//						new ChangePassword(UserIdentificationContract.REQUEST_CODE_CHANGE_PASSWORD_ACTION,
//								getApplicationContext(), this).startChangePassword();
						break;
				}
			}
			else {
				showProgress(false);
				(Toast.makeText(getApplicationContext(), "Отсутствует соединение с интернетом !", Toast.LENGTH_SHORT)).show();
			}
		}
	}

	private boolean isNetworkConnected() {
		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	private boolean isEmailValid(String email) {
		email = email.replaceAll(" ", "");
		return true;//email.matches("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$");
	}

	private boolean isPasswordValid(String password) {
		//TODO: реализовать логику валидации пароля
		return password.length() > 5;
	}

	private boolean isRepeatPasswordValid(String password, String repeatPassword) {
		if (password.equals(repeatPassword)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Показывает прогресс на пользовательском интерфейсе и закрывается после входа.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		}
		else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this,
				// Извлечь строки данных для устройства пользователя с контактами профиля.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

				// Выбрать только email адресс.
				ContactsContract.Contacts.Data.MIMETYPE +
						" = ?", new String[]{ContactsContract.CommonDataKinds.Email
				.CONTENT_ITEM_TYPE},

				// Show primary email addresses first. Note that there won't be
				// a primary email address if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		List<String> emails = new ArrayList<>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			emails.add(cursor.getString(ProfileQuery.ADDRESS));
			cursor.moveToNext();
		}

		addEmailsToAutoComplete(emails);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}

	@Override
	public void asynctaskActionResponse(int requestActionCode, JSONObject jsonResponce) {
		showProgress(false);
		switch (requestActionCode) {
			case UserIdentificationContract.REQUEST_CODE_AUTHORIZATION_ACTION:
				authorizationResultAction(jsonResponce);
				break;
			case UserIdentificationContract.REQUEST_CODE_REGISTRATION_ACTION:
				registrationResultAction(jsonResponce);
				break;
		}
	}

	@Override
	public void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private void authorizationResultAction(JSONObject resultActionCode) {
		try {
			if (resultActionCode.has(UserIdentificationContract.AUTHORIZATION_RESPONSE_STATUS_KEY)) {

				switch (resultActionCode.getInt(UserIdentificationContract.AUTHORIZATION_RESPONSE_STATUS_KEY)) {
					case UserIdentificationContract.AUTHORIZATION_STATUS_SUCCESS:
						Intent goMainActivity = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(goMainActivity);
						finish();
						break;
					case UserIdentificationContract.AUTHORIZATION_STATUS_FAIL:
						showProgress(false);
						break;
					case UserIdentificationContract.AUTHORIZATION_STATUS_CHANGE_PASSWORD:
						Intent goChangePasswordActivity = new Intent(getApplicationContext(), ChangePasswordActivity.class);
						startActivity(goChangePasswordActivity);
						finish();
						break;
					default: showProgress(false);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void registrationResultAction(JSONObject resultActionCode) {
		try {
			if (resultActionCode.has(UserIdentificationContract.REGISTRATION_RESPONSE_STATUS_KEY)) {

				switch (resultActionCode.getInt(UserIdentificationContract.REGISTRATION_RESPONSE_STATUS_KEY)) {
					case UserIdentificationContract.REGISTRATION_STATUS_SUCCESS:
						Intent goMainActivity = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(goMainActivity);
						finish();
						break;
					case UserIdentificationContract.REGISTRATION_STATUS_FAIL:
						showProgress(false);
						break;
					default: showProgress(false);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}



	private interface ProfileQuery {
		String[] PROJECTION = {
				ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
		};

		int ADDRESS = 0;
		int IS_PRIMARY = 1;
	}


	private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
		//Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(LoginActivity.this,
						android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

		mEmailView.setAdapter(adapter);
	}

//
//		@Override
//		protected void onCancelled() {
//			mAuthTask = null;
//			showProgress(false);
//		}

}

