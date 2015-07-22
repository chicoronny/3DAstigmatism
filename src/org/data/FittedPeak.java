package org.data;

public class FittedPeak extends Peak {
	
	private double fitX, fitY, z_, fitI, fitBg;

	public FittedPeak(int x, int y, int value, double fitX, double fitY, double z) {
		super(x, y, value);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}

	public FittedPeak(int slice, int x, int y, int value, double fitX, double fitY, double z) {
		super(slice, x, y, value);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}

	public FittedPeak(int slice, int x, int y, int value, double sx, double sy, double fitX, double fitY, double z) {
		super(slice, x, y, value, sx, sy);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}
	
	public FittedPeak(int slice, int x, int y, int value, double sx, double sy, double fitX, double fitY, double z, double fitI, double fitBg) {
		super(slice, x, y, value, sx, sy);
		this.fitX = fitX;
		this.fitY = fitY;
		this.fitI = fitI;
		this.fitBg = fitBg;
		z_ = z;
	}
	
	public FittedPeak(Peak p, double fitX, double fitY, double z){
		super(p);
		this.fitX = fitX;
		this.fitY = fitY;
		z_ = z;
	}
	
	public String toString(){
		String s = fitX + "\t" + fitY+ "\t" + z_ + "\t" + fitI + "\t" + getSlice() + "\r\n";
		return s;
	}

}
