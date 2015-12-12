package com.technopark.smartbiz.businessLogic.userIdentification.activities;


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

import com.technopark.smartbiz.HomeProxyActivity;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.businessLogic.userIdentification.AccessControl;
import com.technopark.smartbiz.businessLogic.userIdentification.InteractionWithUI;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;
import com.technopark.smartbiz.businessLogic.userIdentification.identificationServices.Authorization;
import com.technopark.smartbiz.businessLogic.userIdentification.identificationServices.Registration;
import com.technopark.smartbiz.businessLogic.userIdentification.identificationServices.VkAuthorization;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

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
	private final String ACTION_VALIDATION = "validation";
	private AccessControl accessControl;

	// Ссылки на графические компоненты
	private AutoCompleteTextView mLoginView;
	private EditText mPasswordView;
	private EditText mPasswordRepeatView;
	private View mProgressView;
	private View mLoginFormView;
	private Button buttonRegistration;

	private static final String[] sMyScope = new String[]{
			VKScope.FRIENDS,
			VKScope.WALL,
			VKScope.PHOTOS,
			VKScope.EMAIL
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		accessControl = new AccessControl(getApplicationContext(), this, UserIdentificationContract.REQUEST_CODE_ACCESS_LOGIN);
		accessControl.displayActivityOfAccessRights();
		// Инициализация приложения...
		// Инициализируем компоненты формы логина
		mLoginView = (AutoCompleteTextView) findViewById(R.id.activity_login_login_textField);
		populateAutoComplete();
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
		final Button mEmailSignInButton = (Button) findViewById(R.id.login_button_sing_in);
		Button vkAuthButton = (Button) findViewById(R.id.login_button_vkAuth);
		final Button newAccountButton = (Button) findViewById(R.id.login_button_new_account);

		newAccountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				switch (newAccountButton.getText().toString()) {
					case "Новый акаунт":
						mPasswordRepeatView.setVisibility(View.VISIBLE);
						newAccountButton.setVisibility(View.GONE);
						buttonRegistration.setVisibility(View.VISIBLE);
						mEmailSignInButton.setText("Зарегистрироваться");
						break;
				}
			}
		});

		buttonRegistration = (Button) findViewById(R.id.login_button_registration);
		buttonRegistration.setVisibility(View.GONE);
		buttonRegistration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mPasswordRepeatView.getVisibility() == View.VISIBLE) {
					mPasswordRepeatView.setVisibility(View.GONE);
					buttonRegistration.setVisibility(View.GONE);
					newAccountButton.setVisibility(View.VISIBLE);
					mEmailSignInButton.setText("Войти");
				}
				startActionInitiatedByUser(ACTION_AUTHORIZATION);
			}
		});


		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mPasswordRepeatView.getVisibility() == View.VISIBLE) {
					startActionInitiatedByUser(ACTION_REGISTRATION);
				}
				else {
					startActionInitiatedByUser(ACTION_AUTHORIZATION);
				}
			}
		});


		Account[] accounts = AccountManager.get(this).getAccountsByType("com.vkontakte.account");
		String buttonText;
		if (accounts.length > 0) {
			buttonText = getString(R.string.action_registration_vk, accounts[0].name);
		}
		else {
			buttonText = "Войти через | VK";
		}
		vkAuthButton.setText(buttonText);
		vkAuthButton.setBackgroundColor(Color.BLUE);
		vkAuthButton.setTextColor(Color.WHITE);
		vkAuthButton.setVisibility(View.VISIBLE);
		vkAuthButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				VKSdk.login(LoginActivity.this, sMyScope);
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
			@Override
			public void onResult(VKAccessToken res) {
				// Пользователь успешно авторизовался
				showProgress(true);
				startVkAuthorization(res.accessToken, res.email, res.userId);
			}

			@Override
			public void onError(VKError error) {
				// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
				showProgress(false);
				showToast("Ошибка авторизации !");
			}
		})) {
			super.onActivityResult(requestCode, resultCode, data);
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
		// Сброс ошибок.
		mLoginView.setError(null);
		mPasswordView.setError(null);

		// Получаем значения введенные пользователем для попытки авторизации.
		String email = mLoginView.getText().toString().replace(" ", "");
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
			mLoginView.setError(getString(R.string.error_field_required));
			focusView = mLoginView;
			cancel = true;
		}
		else if (!isEmailValid(email)) {
			mLoginView.setError(getString(R.string.error_invalid_email));
			focusView = mLoginView;
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
	public void netActionResponse(int requestActionCode, JSONObject jsonResponse) {
		showProgress(false);
		switch (requestActionCode) {
			case UserIdentificationContract.REQUEST_CODE_AUTHORIZATION_ACTION:
				authorizationResultAction(jsonResponse);
				break;
			case UserIdentificationContract.REQUEST_CODE_REGISTRATION_ACTION:
				registrationResultAction(jsonResponse);
				break;
			case UserIdentificationContract.REQUEST_CODE_VK_AUTHORIZATION_ACTION:
				vkAuthorizationResultAction(jsonResponse);
				break;
		}
	}

	@Override
	public void callbackAccessControl(int requestActionCode, String accessRightIdentificator) {
		switch (requestActionCode) {
			case UserIdentificationContract.REQUEST_CODE_ACCESS_LOGIN:
				showActivityForAccessStatus(accessRightIdentificator);
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
						Intent goMainActivity = new Intent(getApplicationContext(), HomeProxyActivity.class);
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
					default:
						showProgress(false);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void vkAuthorizationResultAction(JSONObject resultActionCode) {
		try {
			if (resultActionCode.has(UserIdentificationContract.VK_AUTHORIZATION_RESPONSE_STATUS_KEY)) {

				switch (resultActionCode.getInt(UserIdentificationContract.VK_AUTHORIZATION_RESPONSE_STATUS_KEY)) {
					case UserIdentificationContract.VK_AUTHORIZATION_STATUS_SUCCESS:
						Intent goMainActivity = new Intent(getApplicationContext(), HomeProxyActivity.class);
						startActivity(goMainActivity);
						finish();
						break;
					case UserIdentificationContract.VK_AUTHORIZATION_STATUS_FAIL:
						showProgress(false);
						break;
					case UserIdentificationContract.VK_AUTHORIZATION_STATUS_CHANGE_PASSWORD:
						Intent goChangePasswordActivity = new Intent(getApplicationContext(), ChangePasswordActivity.class);
						startActivity(goChangePasswordActivity);
						finish();
						break;
					default:
						showProgress(false);
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
						Intent goMainActivity = new Intent(getApplicationContext(), HomeProxyActivity.class);
						startActivity(goMainActivity);
						finish();
						break;
					case UserIdentificationContract.REGISTRATION_STATUS_FAIL:
						showProgress(false);
						break;
					default:
						showProgress(false);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void startVkAuthorization(final String token, final String email, final String userId) {
		VKRequest request = VKApi.users().get();
		final VkAuthorization vkAuthorization = new VkAuthorization(
				UserIdentificationContract.REQUEST_CODE_VK_AUTHORIZATION_ACTION,
				getApplicationContext(), LoginActivity.this);
		request.executeWithListener(new VKRequest.VKRequestListener() {
			@Override
			public void onComplete(VKResponse response) {
				//Do complete stuff
				try {
					JSONObject jsonObject = new JSONObject(response.json.getJSONArray("response").getString(0));
					vkAuthorization.startAuthorization(token, email, userId, jsonObject.getString("first_name"),
							jsonObject.getString("last_name"));
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(VKError error) {
				//Do error stuff
			}

			@Override
			public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
				//I don't really believe in progress
			}
		});
	}

	private void showActivityForAccessStatus(String accessRightIdentificator) {
		if (accessRightIdentificator.contains(UserIdentificationContract.SUCCESS_AUTHORIZATION)) {
			startActivity(new Intent(getApplicationContext(), HomeProxyActivity.class));
			finish();
		}
	}

	private interface ProfileQuery {
		String[] PROJECTION = {
				ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
		};

		int ADDRESS = 0;
	}


	private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
		//Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(LoginActivity.this,
						android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

		mLoginView.setAdapter(adapter);
	}


}

