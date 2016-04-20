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
	 * @return photons
	 */
	double getPhotons();
	
	/**
	 * @return frame
	 */
	long getFrame();
	
}
