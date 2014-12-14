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
import android.view.View;

import com.borte.listviewfeed.R;

public class FaceDetectionOpenCV implements CvCameraViewListener2 {

	private static final String TAG = FaceDetectionOpenCV.class.getSimpleName();

	private final Activity activity;
	private final EyePositionListener positionListener;

	private CameraBridgeViewBase openCvCameraView;
	private CascadeClassifier cascadeClassifier;

	private Mat mGray;

	private int absoluteFaceSize;

	private BaseLoaderCallback mLoaderCallback;

	private int width;
	private int height;

	public FaceDetectionOpenCV(Activity activity, View view, EyePositionListener positionListener) {
		this.activity = activity;
		this.positionListener = positionListener;

		mLoaderCallback = new BaseLoaderCallback(activity) {

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

		openCvCameraView = (CameraBridgeViewBase) view
				.findViewById(R.id.fd_surface_view);
		openCvCameraView.setCvCameraViewListener(this);
	}

	private void initializeOpenCVDependencies() {
		Log.d(TAG, "initializeOpenCVDependencies");
		try {
			// Copy the resource into a temp file so OpenCV can load it
			InputStream is = activity.getResources().openRawResource(R.raw.haarcascade_eye);
			File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
			File mCascadeFile = new File(cascadeDir, "eye.xml");
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
		Log.d(TAG, "onCameraViewStarted, width: " + width + ", height: " + height);
		this.width = width;
		this.height = height;

		mGray = new Mat();
		absoluteFaceSize = (int) (height * 0.15);
	}

	@Override
	public void onCameraViewStopped() {
		Log.d(TAG, "onCameraViewStopped");
		mGray.release();
	}

	public void resume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, activity, mLoaderCallback);
	}

	public void pause() {
		if (openCvCameraView != null) {
			openCvCameraView.disableView();
		}
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Log.d(TAG, "onCameraFrame");
		mGray = inputFrame.gray();

		MatOfRect eyes = new MatOfRect();

		if (cascadeClassifier != null) {
			cascadeClassifier.detectMultiScale(mGray, eyes, 1.1, 2, 0, new Size(absoluteFaceSize,
					absoluteFaceSize), new Size());
		}

		Rect[] eyesArray = eyes.toArray();
		if (eyesArray.length == 2) {
			Point leftEyeCenter = normalize(getMidpoint(eyesArray[0].tl(), eyesArray[0].br()));
			Point rightEyeCenter = normalize(getMidpoint(eyesArray[1].tl(), eyesArray[1].br()));
			updatePositionUIThread(getMidpoint(leftEyeCenter, rightEyeCenter), distance(leftEyeCenter, rightEyeCenter));
		} else if (eyesArray.length == 1) {
			// Only one eye found
			updatePositionUIThread(normalize(getMidpoint(eyesArray[0].tl(), eyesArray[0].br())), 0);
		}

		return null;
	}
	
	private Point normalize(Point point) {
		return new Point(point.x / width, point.y / height);
	}
	
	private static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}
	
	private static Point getMidpoint(Point a, Point b) {
		return new Point((a.x + b.x) / 2.0, (a.y + b.y) / 2.0);
	}

	private void updatePositionUIThread(final Point point, final double eyeDistance) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				positionListener.updatePosition(point, eyeDistance);
			}
		});
	}

}
