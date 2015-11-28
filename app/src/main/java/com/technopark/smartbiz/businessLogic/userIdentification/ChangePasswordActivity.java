package com.technopark.smartbiz.businessLogic.userIdentification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.technopark.smartbiz.MainActivity;
import com.technopark.smartbiz.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordActivity extends AppCompatActivity implements InteractionWithUI{

	private View mProgressView;
	private View mChangePasswordFormView;
	private Button changePasswordButton;
	private EditText oldPasswordEditText, newPassword1EditText, newPassword2EditText;
	private String oldPassword, newPassword1, newPassword2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		mProgressView = findViewById(R.id.activity_change_password_progressBar);
		mChangePasswordFormView = findViewById(R.id.activity_change_password_form);
		changePasswordButton = (Button) findViewById(R.id.activity_change_password_change_button);
		oldPasswordEditText = (EditText) findViewById(R.id.activity_change_password_old_password);
		newPassword1EditText = (EditText) findViewById(R.id.activity_change_password_new_password1);
		newPassword2EditText = (EditText) findViewById(R.id.activity_change_password_new_password2);
		changePasswordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				oldPassword = oldPasswordEditText.getText().toString();
				newPassword1 = newPassword1EditText.getText().toString();
				newPassword2 = newPassword2EditText.getText().toString();
				changePassword();
			}
		});

	}

	@Override
	public void asynctaskActionResponse(int requestActionCode, JSONObject jsonResponce) {
		showProgress(false);
		switch (requestActionCode) {
			case UserIdentificationContract.REQUEST_CODE_CHANGE_PASSWORD_ACTION:
				changePasswordResultAction(jsonResponce);
				break;
		}
	}

	@Override
	public void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private void changePasswordResultAction(JSONObject resultActionCode) {
		try {
			if (resultActionCode.has(UserIdentificationContract.CHANGE_PASSWORD_RESPONSE_STATUS_KEY)) {
				switch (resultActionCode.getInt(UserIdentificationContract.CHANGE_PASSWORD_RESPONSE_STATUS_KEY)) {
					case UserIdentificationContract.CHANGE_PASSWORD_STATUS_SUCCESS:
						Intent goMainActivity = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(goMainActivity);
						finish();
						break;
					case UserIdentificationContract.CHANGE_PASSWORD_STATUS_FAIL:
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

	private void changePassword() {
		boolean cancel = false;
		View focusView = null;
		oldPassword = oldPasswordEditText.getText().toString();
		newPassword1 = newPassword1EditText.getText().toString();
		newPassword2 = newPassword2EditText.getText().toString();

		// Проверка правильности пароля.
		if (!isPasswordValid(newPassword1)) {
			newPassword1EditText.setError(getString(R.string.error_invalid_password));
			focusView = newPassword1EditText;
			cancel = true;
		}

		// Проверка правильности повторного ввода пароля.
		if (isRepeatPasswordValid(newPassword1, newPassword2)) {
			newPassword2EditText.setError(getString(R.string.error_invalid_repeat_password));
			focusView = newPassword2EditText;
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
				new ChangePassword(UserIdentificationContract.REQUEST_CODE_CHANGE_PASSWORD_ACTION,
						getApplicationContext(), ChangePasswordActivity.this).startChangePassword(oldPassword, newPassword1, newPassword2);
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

			mChangePasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mChangePasswordFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mChangePasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
			mChangePasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private boolean isPasswordValid(String password) {
		return password.length() > 5;
	}

	private boolean isRepeatPasswordValid(String password, String repeatPassword) {
		if (repeatPassword != null && password.equals(repeatPassword)) {
			return true;
		}
		return false;
	}
}
