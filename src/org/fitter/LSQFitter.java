package org.fitter;

import ij.gui.Roi;
import ij.process.ImageProcessor;
// Plugin classes
import org.data.DefocusCurve;
import org.data.EllipticalGaussian;
import org.data.Gaussian;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
// Apache commons
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;

/**
*	Least-Squares fitting class. Based on apache.commons LSQ fitter and Levenberg-Marquardt optimization. 
*	Performs 1D fit of a defocus curve and 2D fit of an elliptical gaussian.
*
*/
public class LSQFitter {

	/////////////////////////////
	// Arrays for 2D fit
	int[] xgrid, ygrid;									// x and y positions of the pixels	
	double[] Ival, Ivalx,Ivaly;							// intensity value of the pixels
	private double[] fittedEG,fittedGx,fittedGy;

	/////////////////////////////
	// Misc
	public static int PARAM_1D_LENGTH = 5;				// Number of parameters to fit in 1D (calibration curve)
	public static int PARAM_2D_LENGTH = 6;				// Number of parameters to fit in 2D (elliptical gaussian)
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public LSQFitter(){

    }
    
  /*  public void runStackFit(double[] Wx, double Wy[]){

    	
    	// Save graph as .txt
    	PrintWriter writerX, writerY;
    	try {
     	   writerX = new PrintWriter(new FileWriter("Set2_wx.txt", true));
    	   writerY = new PrintWriter(new FileWriter("Set2_wy.txt", true));

	       	for(int i=0; i<Wx.length;i++){
	       		writerX.println(Wx[i]+"\n");
	       		writerY.println(Wy[i]+"\n");	
	       	}
	       	writerX.close();
	       	writerY.close();
    	} catch (FileNotFoundException e) {
    	      e.printStackTrace();
    	} catch (UnsupportedEncodingException e) {
    	  e.printStackTrace();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	
  
    	//Recorder rec = new Recorder();
    	try {
			Wx = rec.loadFile("Set3_wx.txt");
			Wy = rec.loadFile("Set3_wy.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   
    }
     */	
	
	/////////////////////////////////////////////////////////////////////////////
	/// 1D fit: Defocus Curve
   
    public void fit1D(double[] z, double[] w, double[] param, double[] curve, int rStart, int rEnd, int maxIter){

    	double[] rangedZ = new double[rEnd-rStart+1];
    	double[] rangedW = new double[rEnd-rStart+1];
    	
    	fillRanged(rangedZ,z,rangedW,w,rStart,rEnd);
    	
        final DefocusCurve problem = new DefocusCurve(rangedZ,rangedW);
        
        LevenbergMarquardtOptimizer optimizer = getOptimizer();
    	//System.out.println("Begin optimization");

        final Optimum optimum = optimizer.optimize(
                builder(problem)
                        .target(rangedW)
                        //.checkerPair(new SimplePointChecker(10e-5, 10e-5))
                        .start(problem.getInitialGuess())
                        .maxIterations(maxIter)
                        .maxEvaluations(maxIter)
                        .build()
        );
    	//System.out.println("Done");
        //System.out.println(optimum.getEvaluations());
        //System.out.println(optimum.getIterations());
        //System.out.println("---");
        
    	// Copy the fitted parameters
        double[] result = optimum.getPoint().toArray();
        for(int i=0; i<PARAM_1D_LENGTH;i++){
        	param[i] = result[i];
            //System.out.println(param[i]);
        }
        
        //System.out.println("----------");
        
        // Copy the fitted curve values																				
        double[] values = problem.valuesWith(z, param);
        for(int i=0; i<curve.length;i++){
        	curve[i] = values[i];
        }

    }
    
    private void fillRanged(double[] rangedZ, double[] z, double[] rangedW,double[] w, int rStart, int rEnd) {
		int length = rEnd-rStart+1;
		for(int i=0;i<length;i++){
			rangedZ[i] = z[rStart+i];
			rangedW[i] = w[rStart+i];
		}
	}

	public LeastSquaresBuilder builder(DefocusCurve problem){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
    }

	/////////////////////////////////////////////////////////////////////////////
	/// 2D fit: Elliptical Gaussian
	public void fit2D(ImageProcessor ip, Roi roi, double[] Wx, double[] Wy, int counter, int maxIter, int maxEval){

		createGrids(ip,roi);
		EllipticalGaussian eg = new EllipticalGaussian(xgrid, ygrid);

        LevenbergMarquardtOptimizer optimizer = getOptimizer();
        
        try{
	        final Optimum optimum = optimizer.optimize(
	                builder(eg)
	                        .target(Ival)
	                        .checkerPair(new ConvChecker2DGauss())
	                        .start(eg.getInitialGuess(ip,roi))
	                        .maxIterations(maxIter)
	                        .maxEvaluations(maxEval)
	                        .build()
	        );
	
	        fittedEG =  optimum.getPoint().toArray();        
	        //RealVector res = optimum.getResiduals();
	        
	        //////////////////////////////////////////////////////////
	        // erf is symmetrical with respect to (sx,sy)->(-sx,-sy)																											/// how to get the convergence for strictly positive sigmas??
	        //
	        if(fittedEG[2]<0){
	            Wx[counter] = -fittedEG[2];
	        } else {
	        	Wx[counter] = fittedEG[2];
	        } 
	        if(fittedEG[3]<0){
	        	Wy[counter] = -fittedEG[3];
	        } else {
	        	Wy[counter] = fittedEG[3];
	        }	
	        

	        //System.out.println(Wx[counter]+" "+Wy[counter]);
	        //System.out.println(res.toString());

	        
        } catch(TooManyEvaluationsException e){
        	System.out.println("Too many evaluations");
        	Wx[counter] = 0;																																				////////////// is that legal??
        	Wy[counter] = 0;
        }
    }
	
	public double[] fit2Dsingle(ImageProcessor ip, Roi roi, int maxIter){

		createGrids(ip,roi);
		EllipticalGaussian eg = new EllipticalGaussian(xgrid, ygrid);

        LevenbergMarquardtOptimizer optimizer = getOptimizer();
        
        try{
	        final Optimum optimum = optimizer.optimize(
	                builder(eg)
	                        .target(Ival)
	                        .start(eg.getInitialGuess(ip,roi))
	                        .maxIterations(maxIter)
	                        .maxEvaluations(maxIter)
	                        .build()
	        );
	
	        fittedEG =  optimum.getPoint().toArray();        
	        //RealVector res = optimum.getResiduals();
	        
	        double[] result = new double[4];

            result[0] = fittedEG[0];
            result[1] = fittedEG[1];
            
	        //////////////////////////////////////////////////////////
	        // erf is symmetrical with respect to (sx,sy)->(-sx,-sy)																											/// how to get the convergence for strictly positive sigmas??
	        //
	        if(fittedEG[2]<0){
	            result[2] = -fittedEG[2];
	        } else {
	        	result[2] = fittedEG[2];
	        } 
	        if(fittedEG[3]<0){
	        	result[3] = -fittedEG[3];
	        } else {
	        	result[3] = fittedEG[3];
	        }	
	        
	        return result;
	        
        } catch(TooManyEvaluationsException e){
        	System.out.println("Too many evaluations");
        	return null;
        }
    }
	
    public LeastSquaresBuilder builder(EllipticalGaussian problem){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	 builder.model(problem.getModelFunction(xgrid, ygrid), problem.getModelFunctionJacobian(xgrid, ygrid));
		return builder;
    }

	/////////////////////////////////////////////////////////////////////////////
	/// 1D fit: Gaussian	
	public double[] fit1DGsingle(ImageProcessor ip, Roi roi, int maxIter, int maxEval){

		createProjGrids(ip,roi);
		Gaussian egx = new Gaussian(xgrid);
		Gaussian egy = new Gaussian(ygrid);

        LevenbergMarquardtOptimizer optimizer = getOptimizer();
        
        try{
	        final Optimum optimumx = optimizer.optimize(
	                builder(egx,xgrid)
	                        .target(Ivalx)
	                        .checkerPair(new ConvChecker1DGauss())
	                        .start(egx.getInitialGuess(ip,roi,0))
	                        .maxIterations(maxIter)
	                        .maxEvaluations(maxEval)
	                        .build()
	        );
	        System.out.println("Iteration : "+optimumx.getIterations());
	        System.out.println("Evaluation : "+optimumx.getEvaluations());
	        //System.out.println(Ivaly.length);
	        //System.out.println(ygrid.length);
	        final Optimum optimumy = optimizer.optimize(
	                builder(egy,ygrid)
	                        .target(Ivaly)
	                        .checkerPair(new ConvChecker1DGauss())
	                        .start(egy.getInitialGuess(ip,roi,1))
	                        .maxIterations(maxIter)
	                        .maxEvaluations(maxEval)
	                        .build()
	        );

	        fittedGx =  optimumx.getPoint().toArray();
	        fittedGy =  optimumy.getPoint().toArray();        
	        //RealVector res = optimumx.getResiduals();
	        
	        //for(int i=0;i<xgrid.length;i++){
	        //	System.out.println(egx.getValue(fittedGx, xgrid[i]));
	        //}
	        
	        
	        double[] result = new double[4];

            result[0] = fittedGx[0];
            result[1] = fittedGy[0];
            
	        //////////////////////////////////////////////////////////
	        // erf is symmetrical with respect to (sx,sy)->(-sx,-sy)									/// how to get the convergence for strictly positive sigmas??
	        //
	        if(fittedGx[1]<0){
	            result[2] = -fittedGx[1];
	        } else {
	        	result[2] = fittedGx[1];
	        } 
	        if(fittedGy[1]<0){
	        	result[3] = -fittedGy[1];
	        } else {
	        	result[3] = fittedGy[1];
	        }	
	        //System.out.println(result[0]+" "+result[1]+" "+result[2]+" "+result[3]);
	        return result;
	        
        } catch(TooManyEvaluationsException e){
        	System.out.println("Too many evaluations");
        	return null;
        }
    }
	
    private void createProjGrids(ImageProcessor ip, Roi roi) {
    	int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();
		
		//System.out.println(rwidth+" "+rheight);
				
		ygrid = new int[rheight];
		xgrid = new int[rwidth];
		Ivaly = new double[rheight];
		Ivalx = new double[rwidth];
		
		for(int i=0;i<rheight;i++){
			ygrid[i] = ystart+i;
			Ivaly[i] = 0;
			for(int j=0;j<rwidth;j++){
				Ivaly[i] += ip.get(xstart+j,ystart+i)/rheight;
			}

			//System.out.println(Ivaly[i]);
		}
		
		System.out.println("---");
		for(int i=0;i<rwidth;i++){
			xgrid[i] = xstart+i;
			Ivalx[i] = 0;
			for(int j=0;j<rheight;j++){
				Ivalx[i] += ip.get(xstart+i,ystart+j)/rwidth;
			}
			
			//System.out.println(Ivalx[i]);
		}

		
		/*ImagePlus imp = new ImagePlus();
		imp.setProcessor(ip);
		Overlay overlay = new Overlay(); 
		overlay.add(roi);
		imp.setOverlay(overlay);
		imp.show();*/

	}

    public void printProfiles(){
		for(int i=0;i<Ivalx.length;i++){
			System.out.println(Ivalx[i]);
		}
		System.out.println("-");
		for(int i=0;i<xgrid.length;i++){
	        System.out.println((new Gaussian(xgrid)).getValue(fittedGx, xgrid[i]));
	    }
		System.out.println("--");
		for(int i=0;i<Ivaly.length;i++){
			System.out.println(Ivaly[i]);
		}
		System.out.println("-");
		for(int i=0;i<ygrid.length;i++){
	        System.out.println((new Gaussian(ygrid)).getValue(fittedGy, ygrid[i]));
	    }
		System.out.println("--- "+fittedGy[0]);
		System.out.println("--- "+fittedGy[1]);
		System.out.println("--- "+fittedGy[2]);
		System.out.println("--- "+fittedGy[3]);
    }
    
	public LeastSquaresBuilder builder(Gaussian problem, int[] grid){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	 builder.model(problem.getModelFunction(grid), problem.getModelFunctionJacobian(grid));
		return builder;
    }

	/////////////////////////////////////////////////////////////////////////////
	/// Misc Functions
	public void createGrids(ImageProcessor ip, Roi roi){
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();

		//System.out.println("ROI size: "+rwidth+" "+rheight);
		//System.out.println("ROI base: "+xstart+" "+ystart);
		
		xgrid = new int[rwidth*rheight];
		ygrid = new int[rwidth*rheight];
		Ival = new double[rwidth*rheight];
		for(int i=0;i<rheight;i++){
			for(int j=0;j<rwidth;j++){
				ygrid[i*rwidth+j] = i+ystart;
				xgrid[i*rwidth+j] = j+xstart;
				Ival[i*rwidth+j] = ip.get(j+xstart,i+ystart);
			}
		}
	}
	
	public LevenbergMarquardtOptimizer getOptimizer() {
	        return new LevenbergMarquardtOptimizer();
	}
	

	private class ConvChecker2DGauss implements ConvergenceChecker<PointVectorValuePair> {
	    
		int iteration_ = 0;
	    boolean lastResult_ = false;

		public static final int INDEX_X0 = 0;
		public static final int INDEX_Y0 = 1;
		public static final int INDEX_SX = 2;
		public static final int INDEX_SY = 3;
		public static final int INDEX_I0 = 4;
		public static final int INDEX_Bg = 5;
		
		@Override
		public boolean converged(int i, PointVectorValuePair previous, PointVectorValuePair current) {
	         if (i == iteration_)
	             return lastResult_;
	          
	          iteration_ = i;
	          double[] p = previous.getPoint();
	          double[] c = current.getPoint();
	          
	          if ( Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 10  &&
	                  Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 2 &&
	                  Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.1 &&
	                  Math.abs(p[INDEX_Y0] - c[INDEX_Y0]) < 0.1 &&
	                  Math.abs(p[INDEX_SX] - c[INDEX_SX]) < 3 &&
	                  Math.abs(p[INDEX_SY] - c[INDEX_SY]) < 3 ) {
	             lastResult_ = true;
	             return true;
	          }
	          
	          lastResult_ = false;
			return false;
		}
	}
	
	private class ConvChecker1DGauss implements ConvergenceChecker<PointVectorValuePair> {
	    
		int iteration_ = 0;
	    boolean lastResult_ = false;

		public static final int INDEX_X0 = 0;
		public static final int INDEX_SX = 1;
		public static final int INDEX_I0 = 2;
		public static final int INDEX_Bg = 3;
		
		@Override
		public boolean converged(int i, PointVectorValuePair previous, PointVectorValuePair current) {
	        if (i == iteration_)
	             return lastResult_;
	          
	          iteration_ = i;
	          double[] p = previous.getPoint();
	          double[] c = current.getPoint();
	          
	          if ( Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 5  &&
	                  Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 1 &&
	                  Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.02   &&
	                  Math.abs(p[INDEX_SX] - c[INDEX_SX]) < .1) {
	             lastResult_ = true;
	             return true;
	          }
	          
	        lastResult_ = false;
			return false;
		}
	}

}




