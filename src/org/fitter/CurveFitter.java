package org.fitter;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.util.Precision;
import org.data.CalibrationCurve;

public class CurveFitter {
	
	public static int PARAM_1D_LENGTH = 9;
	
	public LeastSquaresBuilder builder(CalibrationCurve problem){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
    }
	
	public LevenbergMarquardtOptimizer getOptimizer() {
		final double initialStepBoundFactor = 100;
		final double costRelativeTolerance = 1e-10;
		final double parRelativeTolerance = 1e-10;
		final double orthoTolerance = 1e-10;
		final double threshold = Precision.SAFE_MIN;
        return new LevenbergMarquardtOptimizer(initialStepBoundFactor,
				costRelativeTolerance, parRelativeTolerance, orthoTolerance, threshold);
	}
	
	public void fitCurves(double[] z, double[] wx, double[] wy, double[] param, double[] curvex, double[] curvey,
			int rStart, int rEnd, int maxIter, int maxEval) {

	  	double[] rangedZ = new double[rEnd-rStart+1];
    	double[] rangedWx = new double[rEnd-rStart+1];
    	double[] rangedWy = new double[rEnd-rStart+1];
    	
    	java.lang.System.arraycopy(z, rStart, rangedZ, 0, rEnd-rStart+1);
    	java.lang.System.arraycopy(wx, rStart, rangedWx, 0, rEnd-rStart+1);
    	java.lang.System.arraycopy(wy, rStart, rangedWy, 0, rEnd-rStart+1); 
    	
        final CalibrationCurve problem = new CalibrationCurve(rangedZ,rangedWx, rangedWy);
        
        LevenbergMarquardtOptimizer optimizer = getOptimizer();

        final Optimum optimum = optimizer.optimize(
                builder(problem)
                        .target(problem.getTarget())
                        //.checkerPair(new SimplePointChecker(10e-5, 10e-5))
                        //.parameterValidator(new ParamValidatorCalibCurves())
                        .start(problem.getInitialGuess())
                        .maxIterations(maxIter)
                        .maxEvaluations(maxIter)
                        .build()
        );
        
    	// Copy the fitted parameters
        double[] result = optimum.getPoint().toArray();

        for(int i=0; i<PARAM_1D_LENGTH;i++){
        	param[i] = result[i];
        }
        
        // Copy the fitted curve values																				
        double[] values = problem.valuesWith(z, result);

        for(int i=0; i<curvex.length;i++){
        	curvex[i] = values[i];
        	curvey[i] = values[i+curvex.length];
        }
	}

}
