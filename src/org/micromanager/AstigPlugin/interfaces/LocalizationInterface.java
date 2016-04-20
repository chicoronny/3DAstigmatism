package org.micromanager.AstigPlugin.interfaces;


public interface LocalizationInterface extends Element {
	/**
	 * @return x
	 */
	double getX();
	
	/**
	 * @return y
	 */
	double getY();
	
	/**
	 * @return intensity
	 */
	double getIntensity();
	
	/**
	 * @return frame
	 */
	long getFrame();
}
