package br.com.focaand.lousa;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import br.com.focaand.lousa.util.ImageFileUtil;
import br.com.focaand.lousa.util.Preferences;

public class ImageTreatmentActivity
    extends Activity {

    private static final String TAG = "focaand.lousa.ImageTreatmentActivity";
    private String segmentFileName = "";
    int imgMarcador[][];

    Bitmap finalPicture;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.treatment, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_trt_cancelar:
        	finish();
                return true;
            case R.id.action_trt_confirmar:
        	finalizarTratamento();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_image_treatment);

	if (!Preferences.getInstance().getShowButtons())
	    hideButtons();

	try {
	    Bundle extras = getIntent().getExtras();
	    segmentFileName = extras.getString("segment_path");
	    final Bitmap segmentation = ImageFileUtil.getBitmap(segmentFileName);
	    System.out.println("*focaAndLousa* - Loaded imageTreatment segmentFileName: " + segmentFileName);
	    final ImageView image = (ImageView)findViewById(R.id.imageViewImageTreatment);
	    image.setImageBitmap(segmentation);

	    final ProgressDialog dialog = new ProgressDialog(ImageTreatmentActivity.this);
	    dialog.setTitle(R.string.processing_image);
	    dialog.setMessage(getResources().getString(R.string.please_wait));
	    dialog.show();

	    new Thread(new Runnable() {

		@Override
		public void run() {
		    finalPicture = processaFiltrosImagem(segmentation);
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    dialog.dismiss();
			    image.setImageBitmap(finalPicture);
			}
		    });
		}
	    }).start();

	} catch (Exception exc) {
	    exc.printStackTrace();
	}

    }
    
    public void onDoneTreatment(View view) {
	finalizarTratamento();
    }

    public void onCancelTreatment(View view) {
	Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show();
	finish();
    }

    private void finalizarTratamento() {
	String finalFileName = ImageFileUtil.getOutputMediaFileUri(ImageFileUtil.MEDIA_TYPE_FINAL).getPath();
	if (finalPicture != null && finalFileName != null && !finalFileName.isEmpty()) {
	    boolean saveOk = ImageFileUtil.saveBitmap(finalPicture, finalFileName);
	    if (saveOk) {
		Toast.makeText(this, "Salvo: " + finalFileName, Toast.LENGTH_SHORT).show();
		finish();
	    } else {
		Toast.makeText(this, "Erro ao salvar: " + finalFileName, Toast.LENGTH_SHORT).show();
	    }
	} else {
	    Toast.makeText(this, "Nao eh possivel salvar!", Toast.LENGTH_SHORT).show();
	}
    }

    private void hideButtons() {
	ImageButton imgBtnDoneTreatment = (ImageButton)findViewById(R.id.imgBtnDoneTreatment);
	imgBtnDoneTreatment.setVisibility(View.GONE);
	ImageButton imgBtnCancelTreatment = (ImageButton)findViewById(R.id.imgBtnCancelTreatment);
	imgBtnCancelTreatment.setVisibility(View.GONE);
    }

    private Bitmap processaFiltrosImagem(Bitmap segmentation) {

	Bitmap contrastBitmap = adjustedContrast(segmentation, 50);
	
	Bitmap colorInverted = colorInvert(contrastBitmap);

	Bitmap bmpGrayscale = toGrayscale(colorInverted);

	// TODO: Processamento de filtros da imagem deve ser feito aqui
	Bitmap processBitmap = bmpGrayscale.copy(Bitmap.Config.ARGB_8888, true);
	return processBitmap;
    }

    /**
     * A simple toGrayscale test
     * @param bmpOriginal
     * @return
     */
    public Bitmap toGrayscale(Bitmap bmpOriginal) {        
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();    

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap colorInvert(Bitmap src) {
	Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
	int A, R, G, B;
	int pixelColor;
	int height = src.getHeight();
	int width = src.getWidth();

	for (int y = 0; y < height; y++) {
	    for (int x = 0; x < width; x++) {
		pixelColor = src.getPixel(x, y);
		A = Color.alpha(pixelColor);

		R = 255 - Color.red(pixelColor);
		G = 255 - Color.green(pixelColor);
		B = 255 - Color.blue(pixelColor);

		output.setPixel(x, y, Color.argb(A, R, G, B));
	    }
	}

	return output;
    }

    private Bitmap adjustedContrast(Bitmap src, double value)
    {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap

        // create a mutable empty bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // create a canvas so that we can draw the bmOut Bitmap from source bitmap
        Canvas c = new Canvas();
        c.setBitmap(bmOut);

        // draw bitmap to bmOut from src bitmap so we can modify it
        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));


        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.green(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.blue(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }

}
