package com.borte.listviewfeed.imageprocessing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

public class FaceDetectionManager {

	private static String TAG = FaceDetectionManager.class.getSimpleName();

	private Camera camera;
	private SurfaceTexture surfaceTexture = new SurfaceTexture(0);

	public FaceDetectionManager() {
		camera = openFrontFacingCameraGingerbread();
		try {
			camera.setPreviewTexture(surfaceTexture);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		camera.startPreview();
		camera.setPreviewCallback(new Camera.PreviewCallback() {

			public void onPreviewFrame(final byte[] data, final Camera camera) {
				// range
				int previewWidth = camera.getParameters().getPreviewSize().width;
				int previewHeight = camera.getParameters().getPreviewSize().height;

				Bitmap bmp = getBitmapImageFromYUV(data, previewWidth, previewHeight);
				Log.d(TAG, "width: " + bmp.getWidth() + ", height: " + bmp.getHeight());
			}
		});
	}

	private Camera openFrontFacingCameraGingerbread() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Log.d(TAG, "cameraCount: " + cameraCount);
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
					Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}

		return cam;
	}

	public Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
		YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
		byte[] jdata = baos.toByteArray();
		BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
		bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
		return bmp;
	}
}
