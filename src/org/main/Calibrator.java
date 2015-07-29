package org.main;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTextField;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.data.Calibration;
import org.fitter.CurveFitter;
import org.fitter.Gaussian2DFitter;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.util.ThreadUtil;

/**
* This class handle the calibration by calling the various method of the fitter. Each fit runs on a dedicated thread to allow updating of the GUI.
* 
*/

public class Calibrator {

	/////////////////////////////
	// Fitting parameters
	public static int PARAM_1D_LENGTH = 9;				// Number of parameters to fit in 1D (calibration curve)
	public static int PARAM_2D_LENGTH = 6;				// Number of parameters to fit in 2D (elliptical gaussian)
	int MAX_ITERATIONS_1D = 100;
	int MAX_ITERATIONS_2D = 100;
	int MAX_EVALUATIONS_2D = 100;
	
	/////////////////////////////
	// Results
	private double[] zgrid;									// z positions of the slices in the stack
	private volatile double[] Wx, Wy; 
	private double[] Calibcurve, paramWx;		// 1D and 2D fit results
	private double[] curveWx, curveWy;							// quadratically fitted curves
	/////////////////////////////
    // Parameters from ImageStack
	private int nSlice;

	/////////////////////////////
	// Input from user
    private double zstep;
    private int rangeStart, rangeEnd;	// Both ends of the restricted z range and length of the restriction
    private volatile Roi roi;
    
	private ImageStack is;
	private Calibration cal;
	private CurveFitter cf;
	
	/////////////////////////////
	// Tests
    
	public Calibrator(ImagePlus im, double zstep, Roi r){
		this.is = im.getStack();
		this.zstep = zstep;
    	this.nSlice = im.getNSlices(); 
    	im.getWidth(); 
    	im.getHeight();
    	this.roi = r;
    	
		cf = new CurveFitter();
		
    	// Initialize arrays
    	zgrid = new double[nSlice];						// z position of the frames
    	Wx = new double[nSlice];						// width in x of the PSF
    	Wy = new double[nSlice];						// width in y of the PSF
    	Calibcurve = new double[nSlice];
    	curveWx = new double[nSlice];					// value of the calibration on X
    	curveWy = new double[nSlice];					// value of the calibration on Y
    	paramWx = new double[PARAM_1D_LENGTH];			// parameters of the calibration on X
    	cal = new Calibration(zgrid, Wx, Wy, curveWx, curveWy, Calibcurve, paramWx);
	}
	
	
	// ////////////////////////////////////////////////////////////
	// 1D and 2D fits
	public void fitStack(JTextField tf) {

		Thread[] threads = ThreadUtil.createThreadArray(Runtime.getRuntime().availableProcessors());
		final AtomicInteger ai = new AtomicInteger(0);

		for (int ithread = 0; ithread < threads.length; ithread++) {

			threads[ithread] = new Thread("fit_" + ithread) {
				@Override
				public void run() {
					for (int i = ai.getAndIncrement(); i < nSlice; i = ai.getAndIncrement()) {
						ImageProcessor ip = is.getProcessor(i + 1);
						Gaussian2DFitter gf = new Gaussian2DFitter(ip, roi, 100, 100);
						double[] results = gf.fit();
						if (results!=null){
							Wx[i]=results[2];
							Wy[i]=results[3];
						}

					}
				}
			};
		}
		ThreadUtil.startAndJoin(threads);
		// Find index of focus and map zgrid
		//indexZ0 = findIntersection(Wx, Wy);
		createZgrid(zgrid, 0);
		
		fixCurve(Wx);
		fixCurve(Wy);
		// Save results in calibration
		cal.setZgrid(zgrid);
		cal.setWx(Wx);
		cal.setWy(Wy);

		// Display result
		cal.plot(Wx, Wy, "2D gaussian LSQ");
		
		tf.setText("Done");
	}	
	
	private void fixCurve(double[] d) {
		for (int i=1 ; i<d.length-1;i++)
			if (d[i]<0.1) d[i]=(d[i-1]+d[i+1])/2;
	}


	public void fitCalibrationCurve(JTextField tf, final double rStart, final double rEnd){	
	       new Thread(new Runnable() {

				@Override
	            public void run() {
					double[] param = new double[PARAM_1D_LENGTH];
					calculateRange(rStart, rEnd);
			    	
					try{
						cf.fitCurves(zgrid, Wx, Wy, param, curveWx, curveWy, rangeStart, rangeEnd, 100, 100);
			    	} catch (TooManyEvaluationsException e) {
			    		System.err.println("Too many evaluations!");				
			    	}  catch (TooManyIterationsException e) {
			    		System.err.println("Too many iterations");
			    	}
					
					// sx2-sy2
					for(int i=0;i<nSlice;i++)
						Calibcurve[i] = curveWx[i]*curveWx[i]-curveWy[i]*curveWy[i]; 

					// Save in calibration
					cal.setcurveWx(curveWx);
					cal.setcurveWy(curveWy);
					cal.setCalibcurve(Calibcurve);
					cal.setparam(param);
					
					// Display result
					cal.plotWxWyFitCurves();
					//cal.plotCalibCurve();
	            }
	        }).start();
	       tf.setText("Done");
	}
	
	
	//////////////////////////////////////////////////////////////
	// Save
	public void saveCalib(String path){
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
	
	@SuppressWarnings("unused")
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
	}
}













