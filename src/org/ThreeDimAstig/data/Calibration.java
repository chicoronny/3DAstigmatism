package org.data;

import org.graphics.ChartBuilder;

public class Calibration {

	public static int PARAM_1D_LENGTH = 5;	
	double[] zgrid;										// z positions of the slices in the stack
	double[] Wx, Wy, Calibcurve, paramWx, paramWy;		// 1D and 2D fit results
	double[] curveWx, curveWy;							// quadratically fitted curves
	int nSlice;
	ChartBuilder cb;
	TextWriter tw;
	
	public Calibration(){
		cb = new ChartBuilder();
		zgrid = new double[nSlice];						// z position of the frames
		Wx = new double[nSlice];						// width in x of the PSF
		Wy = new double[nSlice];						// width in y of the PSF
		Calibcurve = new double[nSlice];
		curveWx = new double[nSlice];					// value of the calibration on X
		curveWy = new double[nSlice];					// value of the calibration on Y
		paramWx = new double[PARAM_1D_LENGTH];			// parameters of the calibration on X
		paramWy = new double[PARAM_1D_LENGTH];			// parameters of the calibration on Y
	}
	
	public Calibration(double[] zgrid, double[] Wx, double[] Wy, double[] curveWx, double[] curveWy, double[] Calibcurve, double[] paramWx, double[] paramWy){
		cb = new ChartBuilder();
		nSlice = zgrid.length;
		this.zgrid = zgrid;						// z position of the frames
		this.Wx = Wx;						// width in x of the PSF
		this.Wy = Wy;						// width in y of the PSF
		this.Calibcurve = Calibcurve;
		this.curveWx = curveWx;					// value of the calibration on X
		this.curveWy = curveWy;					// value of the calibration on Y
		this.paramWx = paramWx;			// parameters of the calibration on X
		this.paramWy = paramWy;			// parameters of the calibration on Y
		tw = new TextWriter();
	}
	
	///////////////////////////////////////// Setters and getters
	public void setZgrid(double[] zgrid){
		this.zgrid = zgrid;
	}
	public double[] getZgrid(){
		return zgrid;
	}
	
	public void setWx(double[] Wx){
		this.Wx = Wx;
	}
	public double[] getWx(){
		return Wx;
	}

	public void setWy(double[] Wy){
		this.Wy = Wy;
	}
	public double[] getWy(){
		return Wy;
	}
	
	public void setcurveWx(double[] curveWx){
		this.curveWx = curveWx;
	}
	public double[] getcurveWx(){
		return curveWx;
	}

	public void setcurveWy(double[] curveWy){
		this.curveWy = curveWy;
	}
	public double[] getcurveWy(){
		return curveWy;
	}

	public void setCalibcurve(double[] Calibcurve){
		this.Calibcurve = Calibcurve;
	}
	public double[] getCalibcurve(){
		return Calibcurve;
	}

	public void setparamWx(double[] paramWx){
		this.paramWx = paramWx;
	}
	public double[] getparamWx(){
		return paramWx;
	}

	public void setparamWy(double[] paramWy){
		this.paramWy = paramWy;
	}
	public double[] getparamWy(){
		return paramWy;
	}
	
	///////////////////////////////////////// Plot
	public void plotWxWy(){
		if(Wx.length > 0 && Wy.length>0){
        	cb.plotXYDots(zgrid, Wx, "Width in x", Wy, "Width in y", "Z (nm)", "Width", "Width of Elliptical Gaussian");
		}
	}

	public void plotWxWyFitCurves(){
		if(Wx.length > 0 && Wy.length>0 && curveWy.length>0 && curveWy.length>0){
			cb.plotXYDotsAndLines(zgrid, Wx, "Width in X", Wy, "Width in Y", curveWx, "Fitted width in X", curveWy, "Fitted width in Y","Z (nm)", "Width", "Width of Elliptical Gaussian");
		}
	}

	public void plotCalibCurve(){
		if(Calibcurve.length > 0){
			cb.plotXYLine(zgrid, Calibcurve, "Calibration curve", "Z (nm)", "sx^2-sy^2", "Calibration curve");
		}
	}
	
	///////////////////////////////////////// Save
	public void saveExp(String path){
		String s = path+"_StackFit.txt";
		tw.saveNew(s, Wx);
		System.out.println(path);
	}
	public void saveFit(String path){
		System.out.println("save Fit");
	}
	public void saveCalib(String path){
		System.out.println("save Calib");
	}
	
}
