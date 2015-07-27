package org.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.graphics.ChartBuilder;

public class Calibration {

	public static int INDEX_WX = 0;
	public static int INDEX_WY = 1;
	public static int INDEX_AX = 2;
	public static int INDEX_AY = 3;
	public static int INDEX_BX = 4;
	public static int INDEX_BY = 5;
	public static int INDEX_C = 6;
	public static int INDEX_D = 7;
	public static int INDEX_Mp = 8;
	public static int PARAM_LENGTH = 9;

	double[] zgrid;										// z positions of the slices in the stack
	double[] Wx, Wy, Calibcurve;						// 1D and 2D fit results
	double[] curveWx, curveWy;							// quadratically fitted curves
	int nSlice=1;
	ChartBuilder cb;

	private double[] param;
	
	public Calibration(){
		cb = new ChartBuilder();
		initialize();	
	}
	
	public Calibration(double[] zgrid, double[] Wx, double[] Wy, double[] curveWx, double[] curveWy, double[] Calibcurve, double[] param){
		cb = new ChartBuilder();
		nSlice = zgrid.length;
		this.zgrid = zgrid;						// z position of the frames
		this.Wx = Wx;							// width in x of the PSF
		this.Wy = Wy;							// width in y of the PSF
		this.Calibcurve = Calibcurve;
		this.curveWx = curveWx;					// value of the calibration on X
		this.curveWy = curveWy;					// value of the calibration on Y
		this.param = param;						// parameters of the calibration on X and Y
	}
	
	private void initialize(){
		zgrid = new double[nSlice];						// z position of the frames
		Wx = new double[nSlice];						// width in x of the PSF
		Wy = new double[nSlice];						// width in y of the PSF
		Calibcurve = new double[nSlice];
		curveWx = new double[nSlice];					// value of the calibration on X
		curveWy = new double[nSlice];					// value of the calibration on Y
		param = new double[PARAM_LENGTH];				// parameters of the calibration on X and Y
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
	public void saveAsCSV(String path){
		csvWriter w = new csvWriter(new File(path));
 	    for (int i=0; i< zgrid.length;i++){
 	    	String s = "" + zgrid[i] + ", " + Wx[i] + ", " + Wy[i] + ", " + Calibcurve[i] + ", " +  curveWx[i] + ", " + curveWy[i] + "\n";
 	    	w.process(s);
 	    }
 	   w.process("--\n");
 	   String ps = "";
 	   for (int j=0;j<PARAM_LENGTH;j++)
 		   ps += param[j] + ", ";
 	   ps = ps.substring(0,ps.length()-3);
 	   ps += "\n";
 	   w.process(ps);	   
 	   w.close();
	}
	
	public void readCSV(String path){
		final Locale curLocale = Locale.getDefault();
		final Locale usLocale = new Locale("en", "US"); // setting us locale
		Locale.setDefault(usLocale);
		
		List<String> list = new ArrayList<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));

			String line;
			String[] s;
			while ((line=br.readLine())!=null){
				if (line.contains("--")) break;
				list.add(line);
			}
			
			nSlice = list.size();
			initialize();
			
			if ((line=br.readLine())!=null){
				s = line.split(",");
				for (int i = 0; i < s.length; i++)
					s[i] = s[i].trim();
				param = new double[]{Double.parseDouble(s[0]),
						Double.parseDouble(s[1]),
						Double.parseDouble(s[2]),
						Double.parseDouble(s[3]),
						Double.parseDouble(s[4]),
						Double.parseDouble(s[5]),
						Double.parseDouble(s[6]),
						Double.parseDouble(s[7]),
						Double.parseDouble(s[8])};
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
		
		//System.out.println(param[0]+"  "+param[1]+"  "+param[2]+"  "+param[3]+"  "+param[4]+"  "+param[5]+"  "+param[6]+"  "+param[7]+"  "+param[8]);
	}

	public double getValueWx(double z) {
		double b = (z-param[INDEX_C]-param[INDEX_Mp])/param[INDEX_D];
		return param[INDEX_WX]*Math.sqrt(1+b*b+param[INDEX_AX]*b*b*b+param[INDEX_BX]*b*b*b*b);
	}

	public double getValueWy(double z) {
		double b = (z+param[INDEX_C]-param[INDEX_Mp])/param[INDEX_D];
		return param[INDEX_WY]*Math.sqrt(1+b*b+param[INDEX_AY]*b*b*b+param[INDEX_BY]*b*b*b*b);
	}

	public double[] getparam() {
		return param;
	}
	
	public void setparam(double[] param){
		this.param = param;
	}
}
