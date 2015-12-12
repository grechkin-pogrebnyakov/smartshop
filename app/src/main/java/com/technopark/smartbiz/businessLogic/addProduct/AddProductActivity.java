package com.technopark.smartbiz.businessLogic.addProduct;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.ActivityWithNavigationDrawer;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.Utils;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.showProducts.ListAddedProducts;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.SmartShopContentProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.technopark.smartbiz.Utils.isResponseSuccess;

public class AddProductActivity extends ActivityWithNavigationDrawer implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {

	static final int REQUEST_TAKE_PHOTO = 1;

	private String name, priceCostProduct, priceSellingProduct, count, barcode, description, photoPath;
	private String productId;

	ImageButton addProductPhotoButton;
	Button addProductButton;
	Button scanBarcodeButton;

	EditText nameEditText, priceCostProductEditText, priceSellingProductEditText,
			barcodeEditText, countEditText, descriptionEditText;

	private View progressView;
	private View addProductFormView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_product);

		progressView = findViewById(R.id.activity_add_product_progress_view);
		addProductFormView = findViewById(R.id.activity_add_product_view_for_add_product);

		photoPath = "";

		initializationEditTextFields();
		initializationButtons();

		initializationButtonsListener();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setDrawerToolbar(toolbar);
	}

	private void initializationEditTextFields() {
		nameEditText = (EditText) findViewById(R.id.content_add_product_name_textField);
		priceCostProductEditText = (EditText) findViewById(R.id.content_add_product_price_cost_textField);
		priceSellingProductEditText = (EditText) findViewById(R.id.content_add_product_price_selling);
		barcodeEditText = (EditText) findViewById(R.id.content_add_product_barcode);
		countEditText = (EditText) findViewById(R.id.content_add_product_count);
		descriptionEditText = (EditText) findViewById(R.id.content_add_product_description);
	}

	private void initializationButtons() {
		addProductPhotoButton = (ImageButton) findViewById(R.id.content_add_product_photo);
		addProductButton = (Button) findViewById(R.id.content_add_product_button_add_product);
		scanBarcodeButton = (Button) findViewById(R.id.content_add_product_scan_barcode);
	}

	private void initializationButtonsListener() {
		addProductButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				actionForAddProductButton();
			}
		});

		scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				IntentIntegrator integrator = new IntentIntegrator(AddProductActivity.this);
				integrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES);
			}
		});

		addProductPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dispatchTakePictureIntent();
			}
		});
	}

	private void actionForAddProductButton() {
		Utils.showProgress(true, addProductFormView, progressView, this);
		name = nameEditText.getText().toString();
		priceCostProduct = priceCostProductEditText.getText().toString();
		priceSellingProduct = priceSellingProductEditText.getText().toString();
		count = countEditText.getText().toString();
		barcode = barcodeEditText.getText().toString();
		description = descriptionEditText.getText().toString();

		final Uri productUri = addRecord(name, priceCostProduct, priceSellingProduct, count, barcode, description, photoPath);
		productId = productUri.getLastPathSegment();

		if (!productId.equals("-1")) {
			Map<String, String> map = new HashMap<>();

			String imageString = Utils.imageToBase64String(photoPath);
			// TODO Move product column name to Contract
			map.put("productName", name);
			map.put("descriptionProduct", description);
			map.put("priceSellingProduct", priceSellingProduct);
			map.put("pricePurchaseProduct", priceCostProduct);
			map.put("productBarcode", barcode);
			map.put("count", count);
			map.put("image", imageString);

			final JSONObject productJsonObject = new JSONObject(map);

			new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Item.URL_ITEM_ADD, productJsonObject, this, this)
					.execute(HttpsHelper.Method.POST);
		}
		else {
			Utils.showProgress(false, addProductFormView, progressView, this);
			Toast.makeText(getApplicationContext(), "Ошибка добавления продукта", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			setPic();
		}

		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null) {
			String contents = result.getContents();
			if (contents != null) {
				Toast.makeText(getApplicationContext(),
						"Успешно отсканированно !" + result.toString(),
						Toast.LENGTH_LONG)
						.show();
				barcodeEditText.setText(result.getContents());
			}
			else {
				Toast.makeText(getApplicationContext(), "Не отсканировано !",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private Uri addRecord(String name, String priceCostProduct, String priceSellingProduct,
			String count, String barcode, String description, String photoPath) {
		// Defines a new Uri object that receives the result of the insertion
		Uri mNewUri;

		// Defines an object to contain the new values to insert
		ContentValues mNewValues = new ContentValues();

        /*
        * Sets the values of each column and inserts the word. The arguments to the "put"
        * method are "column name" and "value"
        */
		mNewValues.put(ContractClass.Products.BARCODE, barcode);
		mNewValues.put(ContractClass.Products.PHOTO_PATH, photoPath);
		mNewValues.put(ContractClass.Products.NAME, name);
		mNewValues.put(ContractClass.Products.PRICE_SELLING, priceSellingProduct);
		mNewValues.put(ContractClass.Products.PRICE_COST, priceCostProduct);
		mNewValues.put(ContractClass.Products.DESCRIPTION, description);
		mNewValues.put(ContractClass.Products._COUNT, count);

		mNewUri = getContentResolver().insert(
				SmartShopContentProvider.PRODUCTS_CONTENT_URI,   // the user dictionary content URI
				mNewValues                          // the values to insert
		);
		return mNewUri;
	}


	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			}
			catch (IOException ex) {
				// Error occurred while creating the File
				ex.printStackTrace();
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		photoPath = image.getAbsolutePath();
		return image;
	}

	private void setPic() {
		if (photoPath != null && !photoPath.isEmpty()) {
			// Get the dimensions of the View
			int targetW = 150; //addProductPhotoButton.getWidth();
			int targetH = 150;  //addProductPhotoButton.getHeight();

			// Get the dimensions of the bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(photoPath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			// Determine how much to scale down the image
			int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
			addProductPhotoButton.setImageBitmap(bitmap);
		}
	}

	@Override
	public void onPreExecute() {
		Utils.showProgress(true, addProductFormView, progressView, this);
	}

	@Override
	public void onPostExecute(JSONObject jsonObject) {
		try {
			if (isResponseSuccess(jsonObject.getInt(HttpsHelper.RESPONSE_CODE))) {
				int id = jsonObject.getInt("id");
				int priceId = jsonObject.getInt("price_id");

				ContentValues contentValues = new ContentValues();
				contentValues.put("_id", id);
				contentValues.put(ContractClass.Products.PRICE_ID, priceId);

				getContentResolver().update(
						SmartShopContentProvider.PRODUCTS_CONTENT_URI,
						contentValues, "_id=" + productId,
						null
				);
				Utils.showProgress(false, addProductFormView, progressView, this);
				Toast.makeText(getApplicationContext(), "Продукт добавлен", Toast.LENGTH_LONG).show();
				Intent goToListAddedProduct = new Intent(getApplicationContext(), ListAddedProducts.class);
				startActivity(goToListAddedProduct);
				finish();
			}
			else {
				Utils.showProgress(false, addProductFormView, progressView, this);
				Toast.makeText(getApplicationContext(), "Ошибка добавления продукта", Toast.LENGTH_LONG).show();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCancelled() {
		Utils.showProgress(false, addProductFormView, progressView, this);
	}
}
