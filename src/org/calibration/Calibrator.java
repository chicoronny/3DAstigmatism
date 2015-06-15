package org.calibration;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.data.Calibration;
import org.data.TextWriter;
import org.fitter.LSQFitter;
import org.graphics.ChartBuilder;
import org.swing.ProgressDisplay;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ImageProcessor;

/**
* This class handle the calibration by calling the various method of the fitter. Each fit runs on a dedicated thread to allow updating of the GUI.
* 
*/

public class Calibrator {

	/////////////////////////////
	// Fitting parameters
	public static int PARAM_1D_LENGTH = 8;				// Number of parameters to fit in 1D (calibration curve)
	public static int PARAM_2D_LENGTH = 6;				// Number of parameters to fit in 2D (elliptical gaussian)
	int MAX_ITERATIONS_1D = 50000;
	int MAX_ITERATIONS_2D = 3000;
	int MAX_EVALUATIONS_2D = 1000;
	
	/////////////////////////////
	// Results
	double[] zgrid;										// z positions of the slices in the stack
	double[] Wx, Wy, Calibcurve, paramWx, paramWy;		// 1D and 2D fit results
	double[] curveWx, curveWy;							// quadratically fitted curves
	int indexZ0;										// Slice number of the focus
    
    /////////////////////////////
    // Parameters from ImageStack
    int nSlice, width, height;

	/////////////////////////////
	// Input from user
    double zstep;
    int rangeStart, rangeEnd, rangeSize;	// Both ends of the restricted z range and length of the restriction
    Roi roi;
    
	/////////////////////////////
	// Misc
	LSQFitter lsq;
	ChartBuilder cb;
	TextWriter rec;
	ImageStack is;
	Calibration cal;
	
	/////////////////////////////
	// Tests
    
	public Calibrator(ImagePlus im, double zstep, Roi r){
		this.is = im.getStack();
		this.zstep = zstep;
    	this.nSlice = im.getNSlices(); 
    	this.width = im.getWidth(); 
    	this.height = im.getHeight();
    	this.roi = r;
    	
		lsq = new LSQFitter();
		cb = new ChartBuilder();
		rec = new TextWriter();
		
    	// Initialize arrays
    	zgrid = new double[nSlice];						// z position of the frames
    	Wx = new double[nSlice];						// width in x of the PSF
    	Wy = new double[nSlice];						// width in y of the PSF
    	Calibcurve = new double[nSlice];
    	curveWx = new double[nSlice];					// value of the calibration on X
    	curveWy = new double[nSlice];					// value of the calibration on Y
    	paramWx = new double[PARAM_1D_LENGTH];			// parameters of the calibration on X
    	paramWy = new double[PARAM_1D_LENGTH];			// parameters of the calibration on Y
 
    	cal = new Calibration();
	}
	
	
	//////////////////////////////////////////////////////////////
	// 1D and 2D fits
	public void fitStack(final ProgressDisplay progress){
		
        new Thread(new Runnable() {
            @Override
            public void run() {
            	for(int i=0;i<nSlice;i++){
            		ImageProcessor ip = is.getProcessor(i+1);
            		lsq.fit2D(ip,roi,Wx,Wy,i,3000,1000);
            		try{
            			progress.updateProgress(i);
            		} catch (NullPointerException ne){
            			
            		}
            	}
            	
            	// Find index of focus and map zgrid
            	indexZ0 = findIntersection(Wx, Wy);
            	createZgrid(zgrid, indexZ0);
            	
            	// Save results in calibration
            	cal.setZgrid(zgrid);
            	cal.setWx(Wx);
            	cal.setWy(Wy);
            	
                // Display result
            	cal.plot(Wx,Wy,"2D gaussian LSQ");
            }
        }).start();        
	}	
	
	public void fitCalibrationCurve(final ProgressDisplay progress, final double rStart, final double rEnd){	
	       new Thread(new Runnable() {

				@Override
	            public void run() {
					double[] param = new double[PARAM_1D_LENGTH];;
					calculateRange(rStart, rEnd);
					progress.updateProgress(10);
			    	
					try{
						lsq.fitCurves(zgrid, Wx, Wy, param, curveWx, curveWy, rangeStart, rangeEnd, 100, 100);
			    	} catch (TooManyEvaluationsException e) {
			    		System.err.println("Too many evaluations!");				
			    	}
			
					// sx2-sy2
					for(int i=0;i<nSlice;i++){
						Calibcurve[i] = curveWx[i]*curveWx[i]-curveWy[i]*curveWy[i]; 
					}

					// Save in calibration
					cal.setcurveWx(curveWx);
					cal.setcurveWy(curveWy);
					cal.setCalibcurve(Calibcurve);
					cal.setparam(param);
					
					// Display result
					cal.plotWxWyFitCurves();
					cal.plotCalibCurve();
	            }
	        }).start();
	}
	
	
	//////////////////////////////////////////////////////////////
	// Save
	public void saveExp(String path){
		cal.saveExp(path);
	}
	public void saveFit(String path){
		cal.saveFit(path);
	}
	public void saveCalib(String path){
		//cal.saveCalib(path);
		cal.saveAsCSV(path);
	}
	
	//////////////////////////////////////////////////////////////
	// Misc functions
	public Calibration getCalibration(){
		return cal;												
	}
	
	public double getMinZ(){
		double zmin = zgrid[0];
		
		// In case the zstack was acquired plus to minus
		if(zgrid[zgrid.length-1]<zmin){
			return zgrid[zgrid.length-1];
		}
		return zmin;
	}
	
	public double getMaxZ(){
		double zmax = zgrid[zgrid.length-1];
		
		// In case the zstack was acquired plus to minus
		if(zgrid[0]>zmax){
			return zgrid[0];
		}
		return zmax;
	}
	
	private int findIntersection(double[] X, double[] Y){
		int z = 0;
		double min=1;
		int ixmin = findMin(X);
		int iymin = findMin(Y);
		
		// Make sure intersection is at a higher value than the minimum of both curves to avoid bad points to be selected
		for(int i=0;i<nSlice;i++){
			if(Math.abs(X[i]-Y[i])<min && X[i]>X[ixmin] && Y[i]>Y[iymin]){
				z = i;
				min = Math.abs(X[i]-Y[i]);
			}
		}
		return z;
	}
	
	private int findMin(double[] x){
		double min = x[0];
		int index = 0;
		for(int i=1;i<x.length;i++){
			if(x[i]<min){
				min = x[i];
				index = i;
			}
		}
		return index;
	}
	
	private void createZgrid(double[] z, int offset){
		for(int i=0;i<z.length;i++)
			z[i] = (i-offset)*zstep;
	}
	
	private void calculateRange(double rStart, double rEnd){
		double minStart = Math.abs(rStart-zgrid[0]);
		double minEnd = Math.abs(rEnd-zgrid[nSlice-1]);
		int iStart = 0;
		int iEnd = nSlice-1;
		for(int i=1;i<nSlice;i++){
			if(Math.abs(rStart-zgrid[i])<minStart){
				minStart = Math.abs(rStart-zgrid[i]);
				iStart = i;
			}
			if(Math.abs(rEnd-zgrid[nSlice-1-i])<minEnd){
				minEnd = Math.abs(rEnd-zgrid[nSlice-1-i]);
				iEnd = nSlice-1-i;
			}
		}
		this.rangeStart = iStart;
		this.rangeEnd = iEnd;
		this.rangeSize = iEnd-iStart+1;
	}
}













