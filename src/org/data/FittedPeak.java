package org.data;

public class FittedPeak extends Peak {
	
	private double fitX, fitY, z_;

	public FittedPeak(int x, int y, int value, double fitX, double fitY, double z) {
		super(x, y, value);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}

	public FittedPeak(int n, int x, int y, int value, double fitX, double fitY, double z) {
		super(n, x, y, value);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}

	public FittedPeak(int x, int y, double sx, double sy, int slice, int value, double fitX, double fitY, double z) {
		super(x, y, sx, sy, slice, value);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}
	
	public FittedPeak(Peak p, double fitX, double fitY, double z){
		super(p);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}
	
	public String toString(){
		String s = "" + getX() + "," + getY()+ "," + getValue() + fitX + "," + fitY+ "," + z_ + "\n";
		return s;
	}

}
