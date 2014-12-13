package com.borte.listviewfeed.imageprocessing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.util.Log;

public class FaceDetectionManagerObsolete {

	private static final String TAG = FaceDetectionManagerObsolete.class.getSimpleName();
	private static final int MAX_FACES = 1;

	private final Context context;
	private final Camera camera;
	private final SurfaceTexture surfaceTexture = new SurfaceTexture(0);

	private float faceXRatio; // 0: left edge, 1: right edge
	private float faceYRatio; // 0: top edge, 1: bottom edge
	private PointF midPoint = new PointF();

	private FaceDetector.Face[] faces;

	public FaceDetectionManagerObsolete(Context context) {
		this.context = context;
		this.camera = openFrontFacingCameraGingerbread();
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

				Bitmap bitmap = getBitmapImageFromYUV(data, previewWidth, previewHeight);
				updateFacePosition(bitmap);
			}
		});
	}
	
	public void close() {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
		}
	}

	private void updateFacePosition(Bitmap bitmap) {
		FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(),
				MAX_FACES);
		faces = new FaceDetector.Face[MAX_FACES];
		faceDetector.findFaces(bitmap, faces);
		for (FaceDetector.Face face : faces) {
			if (face != null) {
				face.getMidPoint(midPoint);
				Log.d(TAG, "x: " + midPoint.x + ", y: " + midPoint.y);
			}
		}
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
