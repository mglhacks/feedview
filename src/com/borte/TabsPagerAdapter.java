package com.borte;

//import info.androidhive.tabsswipe.GamesFragment;
//import info.androidhive.tabsswipe.MoviesFragment;
//import info.androidhive.tabsswipe.TopRatedFragment;
import android.support.v4.app.FragmentPagerAdapter;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
 
public TabsPagerAdapter(android.support.v4.app.FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

//    public TabsPagerAdapter(FragmentManager fm) {
//    	
////        super(fm);
//    }
 
    @Override
    public android.support.v4.app.Fragment getItem(int index) {
 
//        switch (index) {
//        case 0:
//            // Top Rated fragment activity
//            return new FeedFragment();
//        case 1:
//            // Games fragment activity
//            return new GamesFragment();
//        case 2:
//            // Movies fragment activity
//            return new MoviesFragment();
//        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 1;
    }
}