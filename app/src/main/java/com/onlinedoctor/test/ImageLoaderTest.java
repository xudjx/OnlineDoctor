package com.onlinedoctor.test;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

public class ImageLoaderTest {

	public void imageLoader() {
		ImageLoader loader = ImageLoader.getInstance();
		loader.displayImage("http://", new ImageAware() {

			@Override
			public boolean setImageDrawable(Drawable arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean setImageBitmap(Bitmap arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCollected() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public View getWrappedView() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getWidth() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public ViewScaleType getScaleType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getId() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getHeight() {
				// TODO Auto-generated method stub
				return 0;
			}
		});
	}
}
