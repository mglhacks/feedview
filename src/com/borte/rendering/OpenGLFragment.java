/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.borte.rendering;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.borte.rendering.objloader.ObjRendererView;

public class OpenGLFragment extends Fragment {

	private static final String TAG = OpenGLFragment.class.getSimpleName();
	
	private ObjRendererView glSurfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: start");
		glSurfaceView = new ObjRendererView(getActivity());
		Log.d(TAG, "onCreate: finish");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: start");
		if (glSurfaceView.getParent() != null) {
			((ViewGroup) glSurfaceView.getParent()).removeView(glSurfaceView);
		}
		Log.d(TAG, "onCreateView: finish");
		return glSurfaceView;
	}

	@Override
	public void onPause() {
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		super.onPause();
		Log.d(TAG, "onPause");
		glSurfaceView.onPause();
	}

	@Override
	public void onResume() {
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		super.onResume();
		glSurfaceView.onResume();
	}

}