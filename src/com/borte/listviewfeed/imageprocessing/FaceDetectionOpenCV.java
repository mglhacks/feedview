package com.borte.listviewfeed.imageprocessing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.borte.listviewfeed.R;

public class FaceDetectionOpenCV implements CvCameraViewListener2 {

	private static final String TAG = FaceDetectionOpenCV.class.getSimpleName();

	private final Activity context;

	private CameraBridgeViewBase openCvCameraView;
	private CascadeClassifier cascadeClassifier;

	private Mat mGray;

	private int absoluteFaceSize;

	private BaseLoaderCallback mLoaderCallback;

	public FaceDetectionOpenCV(Activity context) {
		this.context = context;

		mLoaderCallback = new BaseLoaderCallback(context) {

			@Override
			public void onManagerConnected(int status) {
				switch (status) {
				case LoaderCallbackInterface.SUCCESS:
					initializeOpenCVDependencies();
					break;
				default:
					super.onManagerConnected(status);
					break;
				}
			}
		};

		openCvCameraView = (CameraBridgeViewBase) context.findViewById(R.id.fd_activity_surface_view);
		openCvCameraView.setCvCameraViewListener(this);
	}

	private void initializeOpenCVDependencies() {
		Log.d(TAG, "initializeOpenCVDependencies");
		try {
			// Copy the resource into a temp file so OpenCV can load it
			InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
			File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
			File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
			FileOutputStream os = new FileOutputStream(mCascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();

			// Load the cascade classifier
			cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
		} catch (Exception e) {
			Log.d(TAG, "Error loading cascade", e);
		}

		// And we are ready to go
		openCvCameraView.enableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		Log.d(TAG, "onCameraViewStarted");
		
		mGray = new Mat();
		absoluteFaceSize = (int) (height * 0.15);
	}

	@Override
	public void onCameraViewStopped() {
		Log.d(TAG, "onCameraViewStopped");
		mGray.release();
	}

	public void resume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, context, mLoaderCallback);
	}

	public void pause() {
		if (openCvCameraView != null) {
			openCvCameraView.disableView();
		}
	}
	
	private Point faceCenter(Point tl, Point br) {
		return new Point((tl.x + br.x) / 2, (tl.y + br.y) / 2);
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mGray = inputFrame.gray();

		MatOfRect faces = new MatOfRect();

		if (cascadeClassifier != null) {
			cascadeClassifier.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(
					absoluteFaceSize, absoluteFaceSize), new Size());
		}

		Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++) {
			// Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
			Log.d(TAG, faceCenter(facesArray[i].tl(), facesArray[i].br()).toString());
		}

		return null;
	}
}
