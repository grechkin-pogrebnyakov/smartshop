package com.technopark.smartbiz.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.technopark.smartbiz.businessLogic.userIdentification.LoginActivity;
import com.technopark.smartbiz.businessLogic.userIdentification.UserIdentificationContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by titaevskiy.s on 25.11.15.
 */
public final class HttpsHelper {

	private static final String LOG = "HttpsHelper";

	public static final String RESPONSE_CODE = "response";

	private static String token = "";


	private HttpsHelper() {}

	public static void setToken(String token) {
		Log.d(LOG, "Set token " + token);

		HttpsHelper.token = token;
	}

	public static void resetToken() {
		Log.d(LOG, "Reset token");

		HttpsHelper.token = "";
	}

	public static JSONObject post(String urlString, JSONObject requestJsonObject) {
		Log.d(LOG, "POST " + urlString);
		return makeRequest(urlString, requestJsonObject, "POST");
	}

	public static JSONObject get(String urlString, JSONObject requestJsonObject) {
		Log.d(LOG, "GET " + urlString);
		return makeRequest(urlString, requestJsonObject, "GET");
	}

	private static JSONObject makeRequest(String urlString, JSONObject requestJsonObject, String method) {
		JSONObject outJsonObject = new JSONObject();

		disableSSLCertificateChecking();

		HttpsURLConnection urlConnection = null;
		try {
			URL url = new URL(urlString);
			urlConnection = (HttpsURLConnection) url.openConnection();

			setupConnection(urlConnection, method);

			writeToConnection(urlConnection, requestJsonObject);
			outJsonObject = readFromConnection(urlConnection);

			int responseCode = urlConnection.getResponseCode();
			outJsonObject.put(RESPONSE_CODE, responseCode);
		}
		catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}

		if (requestJsonObject != null) {
			Log.d(LOG, requestJsonObject.toString());
		}
		Log.d(LOG, "Response: " + outJsonObject.toString());

		return outJsonObject;
	}

	private static void disableSSLCertificateChecking() {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
		}};

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	private static void writeToConnection(HttpsURLConnection connection, JSONObject requestJsonObject) {
		if (requestJsonObject != null) {
			OutputStream os = null;
			try {
				os = connection.getOutputStream();
				os.write(requestJsonObject.toString().getBytes("UTF-8"));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (os != null) {
						os.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static JSONObject readFromConnection(HttpsURLConnection connection) {
		String result;
		StringBuilder stringBuilder = new StringBuilder();
		InputStream is = null;

		try {
			InputStream tempStream;
			if (isSuccesResponce(connection)) {
				tempStream = connection.getInputStream();
			}
			else {
				tempStream = connection.getErrorStream();
			}
			is = new BufferedInputStream(tempStream);

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				stringBuilder.append(inputLine);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (is != null) {
					is.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		result = stringBuilder.toString();

		return stringToJsonObject(result);
	}

	private static JSONObject stringToJsonObject(String result) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(result);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	private static boolean isSuccesResponce(HttpsURLConnection connection) throws IOException {
		if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
			return true;
		}
		return false;
	}

	private static void setupConnection(HttpsURLConnection urlConnection, String method) throws ProtocolException {
		urlConnection.setRequestMethod(method);

		urlConnection.setRequestProperty("Content-Type", "application/json");
		urlConnection.setRequestProperty("Accept", "application/json");

		setAuthorizationToken(urlConnection);
	}

	private static void setAuthorizationToken(HttpsURLConnection urlConnection) {
		if (token != null && token.length() > 0) {
			Log.d(LOG, "Send token " + token);

			urlConnection.setRequestProperty("Authorization", "Token " + token);
		}
	}


	public enum Method {
		GET,
		POST
	}


	public static final class HttpsAsyncTask extends AsyncTask<Method, Void, JSONObject> {

		String url;
		JSONObject requestJsonObject;
		HttpsAsyncTaskCallback callback;
		Context context;

		public HttpsAsyncTask(String url, JSONObject requestJsonObject, HttpsAsyncTaskCallback callback, Context context) {
			this.url = url;
			this.requestJsonObject = requestJsonObject;
			this.callback = callback;
			this.context = context;
		}

		@Override
		protected JSONObject doInBackground(Method... params) {
			JSONObject outJsonObject = new JSONObject();
			switch (params[0]) {
				case GET:
					outJsonObject = HttpsHelper.get(url, requestJsonObject);
					break;
				case POST:
					outJsonObject = HttpsHelper.post(url, requestJsonObject);
					break;
			}
			return outJsonObject;
		}

		@Override
		protected void onPreExecute() {
			if (callback != null) {
				callback.onPreExecute();
			}

			if (context != null) {
				SharedPreferences sharedPreferences = context.getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
				String token = sharedPreferences.getString(UserIdentificationContract.TOKEN_AUTORIZATION, "");
				setToken(token);
			}
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			if (callback != null) {
				callback.onPostExecute(jsonObject);
			}
		}

		public interface HttpsAsyncTaskCallback {
			void onPreExecute();

			void onPostExecute(JSONObject jsonObject);
		}
	}
}
