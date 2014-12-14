package com.borte.listviewfeed.imageprocessing;

import org.opencv.core.Point;

public interface EyePositionListener {
	
	/*
	 * @param point Normalized eye center position
	 * @param eyeDistance Normalized eyeDistance, 0 if only one found
	 */
	abstract void updatePosition(Point eyeCenter, double eyeDistance);

}
