package org.micromanager.AstigPlugin.pipeline;

public class LocalizationPrecision3D extends Localization{

	final private double z;
	final private double sx;
	final private double sy;
	final private double sz;

	public LocalizationPrecision3D(double x, double y, double z, double sx, double sy, double sz, double intensity, long frame) {
		super(x, y, intensity, frame);
		this.z = z;
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
	}

	public double getZ(){
		return z;
	}
	
	public double getSx(){
		return sx;
	}
	
	public double getSy(){
		return sy;
	}
	
	public double getSz(){
		return sz;
	}
	
	@Override
	public String toString(){
		return "" + getX() + "\t" + getY() + "\t" + getZ() + "\t" + getSx() + "\t" + getSy() + "\t" + getSz() + "\t" + getPhotons() +"\t" + getFrame();
	}
}
