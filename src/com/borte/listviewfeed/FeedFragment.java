package com.borte.listviewfeed;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.borte.listviewfeed.adapter.FeedListAdapter;
import com.borte.listviewfeed.app.AppController;
import com.borte.listviewfeed.data.FeedItem;
import com.borte.listviewfeed.imageprocessing.EyePositionListener;
import com.borte.listviewfeed.imageprocessing.FaceDetectionOpenCV;

public class FeedFragment extends Fragment implements EyePositionListener {
	private static final String TAG = FeedFragment.class.getSimpleName();
	private ListView listView;
	private FeedListAdapter listAdapter;
	private List<FeedItem> feedItems;

	private final static String URL_FEED = "http://10.100.1.198:5000/feed";
	private final static String BASE_URL = "http://10.100.1.198:5000/";

	private FaceDetectionOpenCV facedetector;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		feedItems = new ArrayList<FeedItem>();
		listAdapter = new FeedListAdapter(getActivity(), feedItems);
		// We first check for cached request
		Cache cache = AppController.getInstance().getRequestQueue().getCache();
		Entry entry = cache.get(URL_FEED);
		if (entry != null) {
			// fetch the data from cache
			try {
				String data = new String(entry.data, "UTF-8");
				try {
					parseJsonFeed(new JSONObject(data));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		} else {
			// making fresh volley request and getting json
			JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET,
					URL_FEED, null, new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject response) {
					VolleyLog.d(TAG, "Response: " + response.toString());
					if (response != null) {
						parseJsonFeed(response);
					}
				}
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					VolleyLog.d(TAG, "Error: " + error.getMessage());
				}
			});

			// Adding request to volley request queue
			AppController.getInstance().addToRequestQueue(jsonReq);
		}
	}

	/**
	 * Parsing json reponse and passing the data to feed view list adapter
	 * */
	private void parseJsonFeed(JSONObject response) {
		try {
			JSONArray feedArray = response.getJSONArray("feed");

			for (int i = 0; i < feedArray.length(); i++) {
				JSONObject feedObj = (JSONObject) feedArray.get(i);

				FeedItem item = new FeedItem();
				item.setId(feedObj.getInt("id"));
				item.setName(feedObj.getString("name"));

				// Image might be null sometimes
				String image = feedObj.isNull("image") ? null : feedObj
						.getString("image");
				item.setImage(BASE_URL + image);
//				item.setImage("http://10.100.1.198:5000/static/36cd220726bdeff076f09c04a4c00f1b/5.jpg");
				item.setStatus(feedObj.getString("status"));
				item.setProfilePic(BASE_URL + feedObj.getString("profilePic"));
				item.setTimeStamp(feedObj.getString("timeStamp"));

				// url might be null sometimes
				String feedUrl = feedObj.isNull("url") ? null : feedObj
						.getString("url");
				item.setUrl(feedUrl);

				feedItems.add(item);
			}

			// notify data changes to list adapter
			listAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.feed_main, container, false);
		if (listView == null) {
			listView = (ListView) view.findViewById(R.id.feed_list);
			listView.setAdapter(listAdapter);
		} else {
			if (((ViewGroup) listView.getParent()) != null) {
				((ViewGroup) listView.getParent()).removeView(listView);
			}
			((ViewGroup) view).addView(listView);
		}
		facedetector = new FaceDetectionOpenCV(getActivity(), view, this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	};

	@Override
	public void onResume() {
		super.onResume();
		facedetector.resume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		facedetector.pause();
	}

	private int getCellNumber(Point point) {
		Log.d(TAG, point.toString());
		int row, column;
		if (point.x < 0.35) {
			row = 2;
		} else if (point.x < 0.55) {
			row = 1;
		} else {
			row = 0;
		}

		if (point.y < 0.35) {
			column = 2;
		} else if (point.y < 0.60) {
			column = 1;
		} else {
			column = 0;
		}

		return 3 * row + column + 1;
	}

	@Override
	public void updatePosition(Point point, double eyeDistance) {
		int cellNumber = getCellNumber(point);
		Log.d(TAG, "cellNum: " + cellNumber);
		for (FeedItem item : feedItems) {
			String old = item.getImage();
			item.setImage(old.substring(0, old.length() - 5) + cellNumber + ".jpg");
		}
		listAdapter.notifyDataSetChanged();
	}

}
