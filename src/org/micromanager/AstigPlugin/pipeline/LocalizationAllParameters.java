package org.micromanager.AstigPlugin.pipeline;

public class LocalizationAllParameters extends Localization{

	final private double Z;
	final private double sX;
	final private double sY;
	final private double sZ;
	final private double bg;
	final private double RMS;
	final private double iter;
	final private double eval;
	final private double xdetect;
	final private double ydetect;

	public LocalizationAllParameters(double x, double y, double z, double intensity, double bg, double sx, double sy, double sz, double RMS, double iter, double eval, double xdetect, double ydetect, long frame) {
		super(x, y, intensity, frame);
		this.Z = z;
		this.sX = sx;
		this.sY = sy;
		this.sZ = sz;
		this.bg = bg;
		this.RMS = RMS;
		this.iter = iter;
		this.eval = eval;
		this.xdetect = xdetect;
		this.ydetect = ydetect;
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

	public double getRMS(){
		return RMS;
	}

	public double getEval(){
		return eval;
	}

	public double getIter(){
		return iter;
	}
	public double getXdetect(){
		return xdetect;
	}
	public double getYdetect(){
		return ydetect;
	}
	
	@Override
	public String toString(){
		return "" + getX() + "\t" + getY() + "\t" + getZ() + "\t" + getsX() + "\t" + getsY() + "\t" + getsZ() + "\t" + getIntensity() +"\t" + getBg()+  "\t"+ getXdetect()+ "\t" + getYdetect()+"\t" + getRMS()+ "\t" + getIter()+ "\t" + getEval()+ "\t"  + getFrame();
	}
}
