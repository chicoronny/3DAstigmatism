package org.fitter;

// IJ
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;



import ij.process.ShortProcessor;


// Plugin classes
import org.data.DefocusCurve;
import org.data.EllipticalGaussian;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
// Apache commons
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealVector;

/**
*	Least-Squares fitting class. Based on apache.commons LSQ fitter and Levenberg-Marquardt optimization. 
*	Performs 1D fit of a defocus curve and 2D fit of an elliptical gaussian.
*
*/
public class LSQFitter {

	/////////////////////////////
	// Arrays for 2D fit
	int[] xgrid, ygrid;									// x and y positions of the pixels	
	double[] Ival;										// intensity value of the pixels
	private double[] fittedEG;

	/////////////////////////////
	// Misc
	public static int PARAM_1D_LENGTH = 5;				// Number of parameters to fit in 1D (calibration curve)
	public static int PARAM_2D_LENGTH = 6;				// Number of parameters to fit in 2D (elliptical gaussian)
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public LSQFitter(ImagePlus im){

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
    		// TODO Auto-generated catch block
    	      e.printStackTrace();
    	} catch (UnsupportedEncodingException e) {
    		// TODO Auto-generated catch block
    	  e.printStackTrace();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
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
	public void fit2D(ImageProcessor ip, Roi roi, double[] Wx, double[] Wy, int counter, int maxIter){

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
	        RealVector res = optimum.getResiduals();
	        
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
	
    public LeastSquaresBuilder builder(EllipticalGaussian problem){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	 builder.model(problem.getModelFunction(xgrid, ygrid), problem.getModelFunctionJacobian(xgrid, ygrid));
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
				ygrid[i*rheight+j] = i+ystart;
				xgrid[i*rheight+j] = j+xstart;
				Ival[i*rheight+j] = ip.get(j+xstart,i+ystart);
			}
		}
	}
	
	public LevenbergMarquardtOptimizer getOptimizer() {
	        return new LevenbergMarquardtOptimizer();
	}
}
