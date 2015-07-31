package org.fitter;


import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.util.Precision;
import org.data.Calibration;
import org.data.EllipticalGaussianZ;

import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Gaussian3DFitter2 {

	private ImageProcessor ip;
	private Roi roi;
	private int maxIter;
	private int maxEval;
	private int[] xgrid;
	private int[] ygrid;
	private double[] Ival;
	private double[] initialGuess;
	public static final int NUM_PARAMTER = 6;
	public static final int MIN_ITER = 5;
	Calibration cal;
	Boolean sanity;
	public static int INDEX_X0 = 0;
	public static int INDEX_Y0 = 1;
	public static int INDEX_SX = 2;
	public static int INDEX_Z0 = 2;
	public static int INDEX_SY = 3;
	public static int INDEX_I0 = 3;
	public static int INDEX_Bg = 4;
	
	public Gaussian3DFitter2(ImageProcessor ip_, Roi roi_, int maxIter_, int maxEval_, Calibration cal) {
		ip = ip_;
		roi = roi_;
		maxIter = maxIter_;
		maxEval = maxEval_;
		this.cal = cal;
	}
	
	
	private static LevenbergMarquardtOptimizer getOptimizer() { 
		// Different convergence thresholds seem to have no effect on the resulting fit, only the number of
		// iterations for convergence
		final double initialStepBoundFactor = 100;			// between 1-100
		final double costRelativeTolerance = 1e-9;
		final double parRelativeTolerance = 1e-9;
		final double orthoTolerance = 1e-9;
		final double threshold = Precision.SAFE_MIN;
		return new LevenbergMarquardtOptimizer(initialStepBoundFactor,
				costRelativeTolerance, parRelativeTolerance, orthoTolerance, threshold);
		//return new LevenbergMarquardtOptimizer();
	}
	
	private void createGrids(){
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();

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
	
	public double[] fit(){

		createGrids(ip,roi);
		EllipticalGaussianZ eg = new EllipticalGaussianZ(xgrid, ygrid, cal);
		initialGuess = eg.getInitialGuess(ip,roi);
		
        LevenbergMarquardtOptimizer optimizer = getOptimizer();
        
        double[] results;
        
        try{
	        final Optimum optimum = optimizer.optimize(
	                builder(eg)
	                        .target(Ival)
	                        //.checkerPair(new ConvChecker3DGauss())
	                        .parameterValidator(new ParamValidator3DGauss())
	                        .start(eg.getInitialGuess(ip,roi))
	                        .maxIterations(maxIter)
	                        .maxEvaluations(maxEval)
	                        .build()
	        );
	
	        results =  optimum.getPoint().toArray(); 
	        //System.out.println("---------");    
	        //sanity = sanityCheck(optimum);

	        //System.out.println(optimum.getIterations()+"        "+sanity+"    "+results[0]+"  "+results[1]+"  "+results[2]+"  "+results[3]+"  "+results[4]);
	        //System.out.println("---------");    

	        //if(!sanity){
	        //	return null;
	        //}
        } catch(TooManyEvaluationsException e){
        	System.out.println("Too many evaluations");
        	return null;
        } catch(TooManyIterationsException e){
        	System.out.println("Too many iterations");
        	return null;
        }
        return results;
    }
	
    private boolean sanityCheck(Optimum optimum) {
    	double[] point = optimum.getPoint().toArray();

    	if(optimum.getIterations()<MIN_ITER){
    		return false;
    	}
    	if(point[INDEX_Bg]==0 || point[INDEX_I0]==0){
    		return false;
    	}
    	if(point[INDEX_X0]<0 || point[INDEX_X0]>ip.getWidth()){
    		return false;
    	}
    	if(point[INDEX_Y0]<0 || point[INDEX_Y0]>ip.getHeight()){
    		return false;
    	}
    	//if(Math.abs(initialGuess[INDEX_X0]-point[INDEX_X0])>2 || Math.abs(initialGuess[INDEX_Y0]-point[INDEX_Y0])>2){
    	//	return false;
    	//}
    
    	return true;
	}

    public boolean getSanityStatus(){
    	return sanity;
    }

	public LeastSquaresBuilder builder(EllipticalGaussianZ problem){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	 builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
    }
    
	public void createGrids(ImageProcessor ip, Roi roi){
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();

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
		
		/*csvWriter w = new csvWriter(new File("C:/Users/Ries/Documents/3Dtest.csv"));
	 	 for (int i=0;i<xgrid.length;i++){
	 	    	w.process(xgrid[i]+","+ygrid[i]+","+Ival[i]+"\n");
	 	 }
	 	 w.close();
		*/
	}
	

	private class ConvChecker3DGauss implements ConvergenceChecker<PointVectorValuePair> {
	    
		int iteration_ = 0;
	    boolean lastResult_ = false;

		public static final int INDEX_X0 = 0;
		public static final int INDEX_Y0 = 1;
		public static final int INDEX_Z0 = 2;
		public static final int INDEX_I0 = 3;
		public static final int INDEX_Bg = 4;
		
		@Override
		public boolean converged(int i, PointVectorValuePair previous, PointVectorValuePair current) {
	         if (i == iteration_)
	             return lastResult_;
	          
	          iteration_ = i;
	          double[] p = previous.getPoint();
	          double[] c = current.getPoint();
	          
	          if ( Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 0.1  &&
	                  Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 0.1 &&
	                  Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.02 &&
	                  Math.abs(p[INDEX_Y0] - c[INDEX_Y0]) < 0.02 &&
	                  Math.abs(p[INDEX_Z0] - c[INDEX_Z0]) < 0.02  ) {
	             lastResult_ = true;
	             return true;
	          }
	          
	          lastResult_ = false;
			return false;
		}
	}
	private class ParamValidator3DGauss implements ParameterValidator {
		public static final int INDEX_X0 = 0;
		public static final int INDEX_Y0 = 1;
		public static final int INDEX_Z0 = 2;
		public static final int INDEX_I0 = 3;
		public static final int INDEX_Bg = 4;
		
		@Override
		public RealVector validate(RealVector arg) {
			if(arg.getEntry(INDEX_X0)<0){
				arg.setEntry(INDEX_X0,  -arg.getEntry(INDEX_X0));
			}
			if(arg.getEntry(INDEX_Y0)<0){
				arg.setEntry(INDEX_Y0, -arg.getEntry(INDEX_Y0));
			}
			if(arg.getEntry(INDEX_I0)<0){
				arg.setEntry(INDEX_I0, -arg.getEntry(INDEX_I0));
			}
			if(arg.getEntry(INDEX_Bg)<0){
				arg.setEntry(INDEX_Bg, -arg.getEntry(INDEX_Bg));
			}
			if(arg.getEntry(INDEX_Z0)<0){
				arg.setEntry(INDEX_Z0, -arg.getEntry(INDEX_Z0));
			}
			return arg;
		}

	}

}
