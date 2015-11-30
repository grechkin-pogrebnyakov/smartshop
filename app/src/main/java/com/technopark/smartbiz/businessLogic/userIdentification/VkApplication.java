package com.technopark.smartbiz.businessLogic.userIdentification;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.technopark.smartbiz.businessLogic.userIdentification.activities.LoginActivity;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * Created by Abovyan on 29.11.15.
 */
public class VkApplication extends Application {

	VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
		@Override
		public void onVKAccessTokenChanged(@Nullable VKAccessToken oldToken, @Nullable VKAccessToken newToken) {
			if (newToken == null) {
				Toast.makeText(VkApplication.this, "AccessToken invalidated", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(VkApplication.this, LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		vkAccessTokenTracker.startTracking();
		VKSdk.initialize(VkApplication.this);
	}

}
