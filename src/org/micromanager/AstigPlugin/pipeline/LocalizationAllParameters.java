package org.micromanager.AstigPlugin.pipeline;

public class LocalizationAllParameters extends Localization{

	final private double Z;
	final private double sX;
	final private double sY;
	final private double sZ;
	final private double bg;
	final private double RMS;
	final private int iterations;

	public LocalizationAllParameters(double x, double y, double z, double sx, double sy, double sz, double intensity, double bg, 
		double rms, int iterations, long frame) {
		super(x, y, intensity, frame);
		this.Z = z;
		this.sX = sx;
		this.sY = sy;
		this.sZ = sz;
		this.bg = bg;
		this.RMS = rms;
		this.iterations = iterations;
	}
	
	public double getRMS(){
		return RMS;
	}
	
	public int getIterations(){
		return iterations;
	}

	public double getZ(){
		return Z;
	}
	
	public double getsX(){
		return sX;
	}
	
	public double getsY(){
		return sY;
	}
	
	public double getsZ(){
		return sZ;
	}
	
	public double getBg(){
		return bg;
	}
	
	@Override
	public String toString(){
		return "" + getX() + "\t" + getY() + "\t" + getZ() + "\t" + getsX() + "\t" + getsY() + "\t" + getsZ() + "\t" + 
				getIntensity() + "\t" + getFrame() + "\t" + getBg()+ "\t" + getRMS()+ "\t" + getIterations() ;
	}
}
