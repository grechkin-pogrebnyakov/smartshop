package com.technopark.smartbiz;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by titaevskiy.s on 26.11.15.
 */
public final class Utils {
	private Utils() {}

	public static JSONObject mergeJsonObject(JSONObject object1, JSONObject object2) {
		JSONObject mergeJsonObject = new JSONObject();

		try {
			mergeJsonObject = new JSONObject(object1.toString());

			Iterator it = object2.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				mergeJsonObject.accumulate(key, object2.get(key));
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return mergeJsonObject;
	}
}
