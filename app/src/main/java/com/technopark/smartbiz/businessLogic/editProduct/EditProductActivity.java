package com.technopark.smartbiz.businessLogic.editProduct;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.api.HttpsHelper;
import com.technopark.smartbiz.api.SmartShopUrl;
import com.technopark.smartbiz.businessLogic.showProducts.ListAddedProducts;
import com.technopark.smartbiz.database.ContractClass;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.database.items.Product;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Abovyan on 15.11.15.
 */
public class EditProductActivity extends AppCompatActivity implements HttpsHelper.HttpsAsyncTask.HttpsAsyncTaskCallback {
	static final int REQUEST_TAKE_PHOTO = 1;

	private String name, priceCostProduct, priceSellingProduct, count, barcode, description, photoPath;

	private ImageButton addProductPhotoButton;
	private TextView titleTextView;
	private Button addProductButton;
	private Button scanBarcodeButton;

	private EditText nameEditText, priceCostProductEditText, priceSellingProductEditText,
			barcodeEditText, countEditText, descriptionEditText;

	private Product product;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_product);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		titleTextView = (TextView) findViewById(R.id.content_add_product_textView_description_operation);
		titleTextView.setVisibility(View.GONE);

		initializationEditTextFields();
		initializationButtons();

		initializationButtonsListener();

		photoPath = product.getPhotoPath();
		setPic();

	}

	private void initializationEditTextFields() {
		nameEditText = (EditText) findViewById(R.id.content_add_product_name_textField);
		priceCostProductEditText = (AutoCompleteTextView) findViewById(R.id.content_add_product_price_cost_textField);
		priceSellingProductEditText = (EditText) findViewById(R.id.content_add_product_price_selling);
		barcodeEditText = (EditText) findViewById(R.id.content_add_product_barcode);
		countEditText = (EditText) findViewById(R.id.content_add_product_count);
		descriptionEditText = (EditText) findViewById(R.id.content_add_product_description);

		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			product = (Product) extra.get(ListAddedProducts.SEND_PRODUCT_NAME);
		}

		nameEditText.setText(product.getProductName());
		priceCostProductEditText.setText(String.valueOf(product.getPricePurchaseProduct()));
		priceSellingProductEditText.setText(String.valueOf(product.getPriceSellingProduct()));
		barcodeEditText.setText(String.valueOf(product.getProductBarcode()));
		countEditText.setText(String.valueOf(product.getCount()));
		descriptionEditText.setText(product.getDescriptionProduct());

		nameEditText.setEnabled(false);
		priceCostProductEditText.setEnabled(false);
		priceSellingProductEditText.setEnabled(false);
		barcodeEditText.setEnabled(false);
		countEditText.setEnabled(false);
		descriptionEditText.setEnabled(false);
	}

	private void initializationButtons() {
		addProductPhotoButton = (ImageButton) findViewById(R.id.content_add_product_photo);
		addProductButton = (Button) findViewById(R.id.content_add_product_button_add_product);
		scanBarcodeButton = (Button) findViewById(R.id.content_add_product_scan_barcode);

		addProductButton.setText("Редактировать");

		addProductPhotoButton.setEnabled(false);
		scanBarcodeButton.setEnabled(false);
	}

	private void initializationButtonsListener() {
		addProductButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				switch (addProductButton.getText().toString()) {
					case "Редактировать":
						nameEditText.setEnabled(true);
						priceCostProductEditText.setEnabled(true);
						priceSellingProductEditText.setEnabled(true);
						barcodeEditText.setEnabled(true);
						descriptionEditText.setEnabled(true);

						addProductPhotoButton.setEnabled(true);
						scanBarcodeButton.setEnabled(true);
						addProductButton.setText("Сохранить");
						break;
					case "Сохранить":
						actionForSaveChangeProductButton();
				}

			}
		});

		scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				IntentIntegrator integrator = new IntentIntegrator(EditProductActivity.this);
				integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);
			}
		});

		addProductPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dispatchTakePictureIntent();
			}
		});
	}

	private void actionForSaveChangeProductButton() {
		name = nameEditText.getText().toString();
		priceCostProduct = priceCostProductEditText.getText().toString();
		priceSellingProduct = priceSellingProductEditText.getText().toString();
		count = countEditText.getText().toString();
		barcode = barcodeEditText.getText().toString();
		description = descriptionEditText.getText().toString();

		if (updateRecord(product.getId(), name, priceCostProduct, priceSellingProduct, count, barcode, description,
				photoPath) != 0) {
			Toast.makeText(getApplicationContext(), "Изменения сохранены !", Toast.LENGTH_LONG).show();

		}
		else {
			Toast.makeText(getApplicationContext(), "Ошибка сохранения !",
					Toast.LENGTH_LONG).show();
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

	private int updateRecord(long id, String name, String priceCostProduct, String priceSellingProduct,
			String count, String barcode, String description, String photoPath) {
		// Defines a new Uri object that receives the result of the insertion
		int mNewUri;

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

		String mSelectionClause = "_id = ?";
		String[] mSelectionArgs = {String.valueOf(product.getId())};

		mNewUri = getContentResolver().update(SmartShopContentProvider.PRODUCTS_CONTENT_URI, mNewValues,
				mSelectionClause, mSelectionArgs);


		// TODO POOOOOOOOOR
		Map<String, String> map = new HashMap<>();

		// TODO Move product column name to Contract
		map.put("productName", name);
		map.put("descriptionProduct", description);
		map.put("priceSellingProduct", priceSellingProduct);
		map.put("pricePurchaseProduct", priceCostProduct);
		map.put("productBarcode", barcode);
		map.put("count", count);
		map.put("id", String.valueOf(id));

		final JSONObject productJsonObject = new JSONObject(map);

		new HttpsHelper.HttpsAsyncTask(SmartShopUrl.Shop.Item.URL_ITEM_EDIT, productJsonObject, this, this)
				.execute(HttpsHelper.Method.POST);


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
			int targetH = 150; //addProductPhotoButton.getHeight();

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
	public void onPreExecute() { }

	@Override
	public void onPostExecute(JSONObject jsonObject) {
		Intent goToListAddedProduct = new Intent(getApplicationContext(), ListAddedProducts.class);
		startActivity(goToListAddedProduct);
		finish();
	}

	@Override
	public void onCancelled() { }
}
