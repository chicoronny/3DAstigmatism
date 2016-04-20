package org.micromanager.AstigPlugin.pipeline;

import org.micromanager.AstigPlugin.interfaces.LocalizationInterface;

public class Localization implements LocalizationInterface {

	final private double X,Y;
	private final long ID;
	static private long curID = 0;
	private boolean isLast;
	final private long frame;
	final private double photons;
	
	
	public Localization(double x, double y, double intensity, long frame) {
		X=x; Y=y; ID=curID++; this.frame=frame; isLast=false; this.photons = intensity;
	}
	
	@Override
	public boolean isLast() {
		return isLast;
	}

	/**
	 * @param isLast - switch
	 */
	@Override
	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}

	@Override
	public double getX() {
		return X;
	}

	@Override
	public double getY() {
		return Y;
	}

	@Override
	public long getFrame() {
		return frame;
	}
	
	@Override
	public double getIntensity() {
		return photons;
	}

	@Override
	public String toString(){
		return "" + getX() + "\t" + getY() + "\t" + getIntensity() + "\t" + getFrame();
	}

	public long getID() {
		return ID;
	}	

}
