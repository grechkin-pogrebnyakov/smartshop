package com.technopark.smartbiz.userIdentification;


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
import android.os.AsyncTask;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[]{
			"foo@example.com:hello", "bar@example.com:world"
	};
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	public static final String APP_PREFERENCES = "mysettings";
	public static final String TOKEN_AUTORIZATION = "token";
	private UserLoginTask mAuthTask = null;
	SharedPreferences sharedPreferences;
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
		mEmailView = (AutoCompleteTextView) findViewById(R.id.login_email);
		populateAutoComplete();
		if (sharedPreferences.contains(TOKEN_AUTORIZATION)) {
			String session = sharedPreferences.getString(TOKEN_AUTORIZATION, "");
			Log.e("session", session);
			if (!session.isEmpty()) {
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
					attemptLoginOrRegistration();
					return true;
				}
				return false;
			}
		});

		registrationButtons();

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	private void registrationButtons() {
		registrtionButton = (Button) findViewById(R.id.login_button_registration);
		registrtionButton.setVisibility(View.GONE);
		registrtionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLoginOrRegistration();
			}
		});

		final Button newAccountButton = (Button) findViewById(R.id.login_button_new_account);
		newAccountButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mPasswordRepeatView.setVisibility(View.VISIBLE);
				newAccountButton.setVisibility(View.GONE);
				registrtionButton.setVisibility(View.VISIBLE);
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
				attemptLoginOrRegistration();

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
		else vkAuthButton.setVisibility(View.GONE);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && data != null && data.hasExtra("access_token")) {
			String accessToken = data.getStringExtra("access_token");
			int userId = data.getIntExtra("user_id", 0);
			sharedPreferences.edit().putString(TOKEN_AUTORIZATION, accessToken).commit();
			startActivity(new Intent(getApplicationContext(), MainActivity.class));
			Log.i("VK_LOGIN", "accessToken: " + accessToken + ", userId: " + userId);
		}
	}

	private void populateAutoComplete() {
		getLoaderManager().initLoader(0, null, this);
	}


	/**
	 * Попытка авторизоваться по логину и паролю указанному в форме входа.
	 * Если есть ошибки (некорректный email, недостающие поля, и т. д.),
	 * Представляются ошибки и не производиться попытка авторизации.
	 */
	private void attemptLoginOrRegistration() {
		if (mAuthTask != null) {
			return;
		}

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
				mAuthTask = new UserLoginTask(email, password);
				if (repeatPassword == null) {
					mAuthTask.execute("authorization");
				}
				else mAuthTask.execute("registration");
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
		return true;//password.length() > 4;
	}

	private boolean isRepeatPasswordValid(String password, String repeatPassword) {
		if (password.equals(repeatPassword)) {
			return true;
		}
		else return false;
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

	/**
	 * Представляет асинхронную задачу аутентификации пользователя.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
		private static final String DEBUG_TAG = "HttpExample";
		private static final String urlAuthorization = "https://smartshop1.ddns.net/api/auth/login/";//"https://smartshop1.ddns.net:8000/api/login";
		private static final String urlRegistration = "https://smartshop1.ddns.net/api/auth/registration/";//"https://smartshop1.ddns.net:8000/api/registration";
		private final String mEmail;
		private final String mPassword;

		UserLoginTask(String email, String password) {
			mEmail = email;
			mPassword = password;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO: реализовать попытку авторизации с помощью сервиса сетевых коммуникаций.
			switch (params[0]) {
				case "authorization":
					return authorization();
				case "registration":
					return registration();
				case "refreshAuthorization":
					return refreshAutorization(params[1]);
				default:
					return false;
			}


//            try {
//                // Эмуляция сетевого доступа.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Акаунт существует, вернуть true если пароль совподает.
//                    return pieces[1].equals(mPassword);
//                }
//            }

			// TODO: регистрировать новый акаунт для пользователя.
			//return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Intent goMainActivity = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(goMainActivity);
				finish();
			}
			else {
				showProgress(false);
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}


		private boolean authorization() {
			JSONObject jsonRequest = new JSONObject();
			try {
				jsonRequest.accumulate("username", mEmail);
				jsonRequest.accumulate("password", mPassword);
				JSONObject jsonResponse = requestPostMethod(urlAuthorization, jsonRequest);

				switch (jsonResponse.getInt("responceCode")) {

					case 200:
						switch (jsonResponse.getString("body")) {
							case "ok": // TODO: успешная авторизация. нужно получить токен !
								showToast("Успешный вход");
								return true;
						}
						break;

					case 400:
						switch (jsonResponse.getString("body")) {
							case "exists": // TODO: пользователь существует
								break;
							case "login fail": // TODO: невалидный пароль
								showToast("Некорректный логин или пароль !");
								return false;
							case "email": // TODO: невалидный логин
								break;
							default: // TODO: неизвестная ошибка
								return false;
						}
						break;

					case 500:
						switch (jsonResponse.getString("body")) {
							case "internal server error": // TODO: ошибка сервера
								showToast("Ошибка сервера !");
								return false;
							default: //TODO: неизвестная ошибка
								showToast("Неизвестная ошибка !");
								return false;
						}

				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
				showToast("Ошибка сервера !");
			}
			return true;
		}

		private boolean registration() {
			JSONObject jsonRequest = new JSONObject();
			try {
				jsonRequest.accumulate("username", mEmail);
				jsonRequest.accumulate("password1", mPassword);
				jsonRequest.accumulate("password2", mPassword);
				JSONObject jsonResponse = requestPostMethod(urlRegistration, jsonRequest);
				switch (jsonResponse.getInt("status")) {

					case 200:
						switch (jsonResponse.getString("body")) {
							case "registration success": // TODO: успешная авторизация. нужно получить токен !
								showToast("Регистрация прошла успешно !");
								runOnUiThread(new Runnable() {
									public void run() {
										mPasswordRepeatView.setVisibility(View.GONE);
										registrtionButton.setVisibility(View.GONE);
									}
								});
								return false;
							default:
								showToast("Неизвестный ответ сервера !");
								return false;
						}

					case 400:
						switch (jsonResponse.getString("body")) {
							case "exists": // TODO: пользователь существует
								showToast("Пользователь уже существует !");
								return false;
							case "registration failed": // TODO: невалидный пароль
								showToast("Пользователь уже существует !");
								return false;
							case "email": // TODO: невалидный логин
								break;
							default: // TODO: неизвестная ошибка
								showToast("Неизвестная ошибка !");
								return false;
						}
						break;

					case 500:
						switch (jsonResponse.getString("body")) {
							case "server": // TODO: ошибка сервера
								showToast("Ошибка сервера !");
								return false;
							default: //TODO: неизвестная ошибка
								showToast("Неизвестная ошибка !");
								return false;
						}

				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

		private boolean refreshAutorization(String sessionId) {
			return true;
		}

		private void showToast(final String message) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				}
			});
		}

		private JSONObject requestPostMethod(String requesrUrl, JSONObject jsonRequest) throws IOException {
			InputStream is = null;
			OutputStream os = null;
			int len = 500;
			try {
				disableSSLCertificateChecking();
				CookieManager cookieManager = new CookieManager();
				CookieHandler.setDefault(cookieManager);
				URL url = new URL(requesrUrl);
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("Accept", "application/json");
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setDoInput(true);


				os = conn.getOutputStream();
				os.write(jsonRequest.toString().getBytes("UTF-8"));

				// Starts the query
				conn.connect();

				int responce = conn.getResponseCode();
				Log.d(DEBUG_TAG, "The responce is: " + responce);
				is = conn.getInputStream();

				// Convert the InputStream into a string
				JSONObject jsonResponseObject = new JSONObject(readIt(is, len));
				jsonResponseObject.put("responceCode", responce);

//                if (!requesrUrl.equals("https://smartshop1.ddns.net/api/auth/registration/")) {
//                    String token = jsonResponseObject.getString("key");//conn.getHeaderFields().get("Key").get(0);
//                    Log.e("cookie", token);
//                    sharedPreferences.edit().putString(TOKEN_AUTORIZATION, token).commit();
//                    Log.e("session", sharedPreferences.getString(TOKEN_AUTORIZATION, "default"));
//                }


				return jsonResponseObject;

				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (is != null) {
					os.close();
					is.close();
				}
			}
			return new JSONObject();
		}

		private void disableSSLCertificateChecking() {
			HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					// Not implemented
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					// Not implemented
				}
			}};

			try {
				SSLContext sc = SSLContext.getInstance("TLS");

				sc.init(null, trustAllCerts, new java.security.SecureRandom());

				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			}
			catch (KeyManagementException e) {
				e.printStackTrace();
			}
			catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		private String readIt(InputStream stream, int len) throws IOException {
			Reader reader = null;
			reader = new InputStreamReader(stream, "UTF-8");
			char[] buffer = new char[len];
			reader.read(buffer);
			return new String(buffer);
		}


	}


	private class NullHostNameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String s, SSLSession sslSession) {
			return true;
		}
	}
}

