package org.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.graphics.ChartBuilder;

public class Calibration {

	public static int PARAM_1D_LENGTH = 5;	
	double[] zgrid;										// z positions of the slices in the stack
	double[] Wx, Wy, Calibcurve, paramWx, paramWy;		// 1D and 2D fit results
	double[] curveWx, curveWy;							// quadratically fitted curves
	int nSlice=1;
	ChartBuilder cb;
	TextWriter tw;
	
	public Calibration(){
		cb = new ChartBuilder();
		initialize();	
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
	
	private void initialize(){
		zgrid = new double[nSlice];						// z position of the frames
		Wx = new double[nSlice];						// width in x of the PSF
		Wy = new double[nSlice];						// width in y of the PSF
		Calibcurve = new double[nSlice];
		curveWx = new double[nSlice];					// value of the calibration on X
		curveWy = new double[nSlice];					// value of the calibration on Y
		paramWx = new double[PARAM_1D_LENGTH];			// parameters of the calibration on X
		paramWy = new double[PARAM_1D_LENGTH];			// parameters of the calibration on Y
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
	public void plot(double[] W1, double[] W2, String title){
		if(W1.length > 0 && W2.length>0){
        	cb.plotXYDots(zgrid, W1, "Width in x", W2, "Width in y", "Z (nm)", "Width", title);
		}
	}	
	
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
	
	public void saveAsCSV(String path){
		csvWriter w = new csvWriter(new File(path));
 	    for (int i=0; i< zgrid.length;i++){
 	    	String s = "" + zgrid[i] + ", " + Wx[i] + ", " + Wy[i] + ", " + Calibcurve[i] + ", " +  curveWx[i] + ", " + curveWy[i] + "\n";
 	    	w.process(s);
 	    }
 	   w.process("--\n");
 	   String ps = "";
 	   for (int j=0;j<PARAM_1D_LENGTH;j++)
 		   ps += paramWx[j] + ", ";
 	   ps = ps.substring(0,ps.length()-3);
 	   ps += "\n";
 	   w.process(ps);
 	   ps="";
 	   for (int j=0;j<PARAM_1D_LENGTH;j++)
		   ps += paramWy[j] + ", ";
 	   ps = ps.substring(0,ps.length()-3);
 	   ps += "\n";
	   w.process(ps);
	   
 	   w.close();
	}
	
	public void readCSV(String path){
		final Locale curLocale = Locale.getDefault();
		final Locale usLocale = new Locale("en", "US"); // setting us locale
		Locale.setDefault(usLocale);
		
		List<String> list = new ArrayList<>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			Iterator<String> stream = br.lines().iterator();
			String line;
			String[] s;
			while (stream.hasNext()){
				line = stream.next();
				if (line.contains("--")) break;
				list.add(line);
			}
			
			nSlice = list.size();
			initialize();
			
			if (stream.hasNext()){
				line = stream.next();
				s = line.split(",");
				for (int i = 0; i < s.length; i++)
					s[i] = s[i].trim();
				paramWx = new double[]{Double.parseDouble(s[0]),
						Double.parseDouble(s[1]),
						Double.parseDouble(s[2]),
						Double.parseDouble(s[3]),
						Double.parseDouble(s[4])};
			}
			if (stream.hasNext()){
				line = stream.next();
				s = line.split(",");
				for (int i = 0; i < s.length; i++)
					s[i] = s[i].trim();
				paramWy = new double[]{Double.parseDouble(s[0]),
						Double.parseDouble(s[1]),
						Double.parseDouble(s[2]),
						Double.parseDouble(s[3]),
						Double.parseDouble(s[4])};
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i=0; i<nSlice;i++){
			String[] s = list.get(i).split(",");
			for (int j = 0; j < s.length; j++)
				s[j] = s[j].trim();
			zgrid[i] = Double.parseDouble(s[0]);
			Wx[i] = Double.parseDouble(s[1]);
			Wy[i] = Double.parseDouble(s[2]);
			Calibcurve[i] = Double.parseDouble(s[3]);
			curveWx[i] = Double.parseDouble(s[4]);
			curveWy[i] = Double.parseDouble(s[5]);
		}
		
		Locale.setDefault(curLocale);
	}
}
