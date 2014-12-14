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

import com.borte.listviewfeed.FeedFragment;
import com.borte.listviewfeed.R;

public class FaceDetectionOpenCV implements CvCameraViewListener2 {

	private static final String TAG = FaceDetectionOpenCV.class.getSimpleName();

	private final Activity activity;
	private final FeedFragment feedFragment;

	private CameraBridgeViewBase openCvCameraView;
	private CascadeClassifier cascadeClassifier;

	private Mat mGray;

	private int absoluteFaceSize;

	private BaseLoaderCallback mLoaderCallback;

	private int width;
	private int height;

	public FaceDetectionOpenCV(Activity activity, FeedFragment feedFragment) {
		this.activity = activity;
		this.feedFragment = feedFragment;

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

		openCvCameraView = (CameraBridgeViewBase) activity
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

	private Point getMidpoint(Point tl, Point br) {
		return new Point((tl.x + br.x) / 2.0, (tl.y + br.y) / 2.0);
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
			Point leftEyeCenter = getMidpoint(eyesArray[0].tl(), eyesArray[0].br());
			Point rightEyeCenter = getMidpoint(eyesArray[1].tl(), eyesArray[1].br());
			updateCellnumber(getMidpoint(leftEyeCenter, rightEyeCenter));
		} else if (eyesArray.length == 2) {
			// Only one eye found
			updateCellnumber(getMidpoint(eyesArray[0].tl(), eyesArray[0].br()));
		}

		return null;
	}

	private void updateCellnumber(Point eyeCenter) {
		int row, column;
		if (eyeCenter.x < 2 * width / 5) {
			row = 2;
		} else if (eyeCenter.x < 3 * width / 5) {
			row = 1;
		} else {
			row = 0;
		}

		if (eyeCenter.y < 2 * height / 5) {
			column = 2;
		} else if (eyeCenter.y < 3 * height / 5) {
			column = 1;
		} else {
			column = 0;
		}

		final int cellNumber = 3 * row + column + 1;
		Log.d(TAG, eyeCenter.x / width + " " + eyeCenter.y / height);

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				feedFragment.updateCellNumber(cellNumber);
			}
		});
	}
}
