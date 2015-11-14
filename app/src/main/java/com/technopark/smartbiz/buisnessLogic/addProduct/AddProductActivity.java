package com.technopark.smartbiz.buisnessLogic.addProduct;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.technopark.smartbiz.R;
import com.technopark.smartbiz.database.SmartShopContentProvider;
import com.technopark.smartbiz.screnListView.ListAddedProducts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddProductActivity extends AppCompatActivity {

	static final int REQUEST_TAKE_PHOTO = 1;

	String name, priceCostProduct, priceSellingProduct, count, barcode, description, photoPath;

	ImageButton addProductPhotoButton;
	Button addProductButton;
	Button scanBarcodeButton;

	EditText nameEditText, priceCostProductEditText, priceSellingProductEditText,
			barcodeEditText, countEditText, descriptionEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_product);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		photoPath = "";

		initializationEditTextFields();
		initializationButtons();

		initializationButtonsListener();

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

	private void actionForAddProductButton() {
		name = nameEditText.getText().toString();
		priceCostProduct = priceCostProductEditText.getText().toString();
		priceSellingProduct = priceSellingProductEditText.getText().toString();
		count = countEditText.getText().toString();
		barcode = barcodeEditText.getText().toString();
		description = descriptionEditText.getText().toString();

		if (!addRecord(name, priceCostProduct, priceSellingProduct, count, barcode, description,
				photoPath).toString().contains("-1")) {
			Toast.makeText(getApplicationContext(), "Продукт добавлен", Toast.LENGTH_LONG).show();
			Intent goToListAddedProduct = new Intent(getApplicationContext(), ListAddedProducts.class);
			startActivity(goToListAddedProduct);
			finish();
		}
		else {
			Toast.makeText(getApplicationContext(), "Ошибка добавления продукта",
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
		mNewValues.put("barcode", barcode);
		mNewValues.put("photo_path", photoPath);
		mNewValues.put("name", name);
		mNewValues.put("price_selling_product", priceSellingProduct);
		mNewValues.put("price_cost_product", priceCostProduct);
		mNewValues.put("description", description);
		mNewValues.put("count", count);

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
		// Get the dimensions of the View
		int targetW = addProductPhotoButton.getWidth();
		int targetH = addProductPhotoButton.getHeight();

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
