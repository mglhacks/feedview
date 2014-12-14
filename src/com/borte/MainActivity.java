package com.borte;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.borte.listviewfeed.FeedFragment;
import com.borte.listviewfeed.R;
import com.borte.rendering.OpenGLFragment;
import com.borte.uploading.UploadCameraFragment;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		setupTabs();
	}

	@SuppressLint("NewApi")
	private void setupTabs() {
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab feedTab = actionBar
				.newTab()
				.setText("Home")
				.setIcon(R.drawable.ic_action_view_as_list)
				.setTabListener(
						new FragmentTabListener<FeedFragment>(R.id.flContainer, this, "feed", FeedFragment.class));
		actionBar.addTab(feedTab);

		Tab renderTab = actionBar
				.newTab()
				.setText("3D")
				.setIcon(R.drawable.ic_action_slideshow)
				.setTabListener(
						new FragmentTabListener<OpenGLFragment>(R.id.flContainer, this, "render",
								OpenGLFragment.class));
		actionBar.addTab(renderTab);

		
		Tab uploadTab = actionBar
				.newTab()
				.setText("Upload")
				.setIcon(R.drawable.ic_action_camera)
				.setTabListener(
						new FragmentTabListener<UploadCameraFragment>(R.id.flContainer, this, "upload",
								UploadCameraFragment.class));
		actionBar.addTab(uploadTab);

		actionBar.selectTab(feedTab);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
