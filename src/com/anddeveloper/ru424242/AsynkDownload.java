package com.anddeveloper.ru424242;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

class AsynkDownload extends AsyncTask<Object, Void, Bitmap> {
	ImageView image;
	int width;
	int height;
	
	@Override
	protected Bitmap doInBackground(Object... params) {
		Bitmap bitmap = null;
		
		image = (ImageView) params[1];
		width = (Integer) params[3];
		height = (Integer) params[4];
		
		try {
			bitmap = Utils.loadBitmap((String) params[0]);
		} catch (IOException e) {
			Utils.genLog(e.getLocalizedMessage());
			bitmap = null;
		}
		
		if (bitmap != null) {
			try {
				FileOutputStream out = new FileOutputStream(new File((String) params[2]));
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (FileNotFoundException e) {
				Utils.genLog(e.getLocalizedMessage());
			}
		}
		
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result != null)
			if (width != 0 && height !=0 && result != null && result.getWidth() !=0 && result.getHeight() != 0) {
				Log.e("resize", "" + width + " " + height + " scaled");
				float scale = Math.min((float) height/result.getHeight(), (float) width/result.getWidth());
				
				Matrix matrix = new Matrix();
				matrix.preScale(scale, scale);
				
				result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
			}
		
			image.setImageBitmap(result);
	}
}
