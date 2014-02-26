package br.com.focaand.lousa;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {


	private static final int SELECT_PICTURE = 1;
	private static final int CAPTURE_FROM_CAMERA = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/OpenDyslexicAlta-Regular.otf");
		TextView lblTitle = (TextView)findViewById(R.id.lblTitle);
		lblTitle.setTypeface(face);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onGetFromCamera(View view) {
	    Intent intent = new Intent(this, CameraActivity.class); 
	    startActivityForResult(intent, CAPTURE_FROM_CAMERA); 
	}
	
	public void onGetFromGalery(View view) {
		
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String selectedImagePath = null;
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
            } else if (requestCode == CAPTURE_FROM_CAMERA) {
        	selectedImagePath = data.getStringExtra("photo_path");
            }

            if (selectedImagePath != null  &&  !selectedImagePath.isEmpty()) {
                Intent i = new Intent(MainActivity.this, ImageTreatmentActivity.class);
                i.putExtra("photo_path" ,selectedImagePath);
                startActivity(i);
            }
        }
    }
	
	public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
	

}
