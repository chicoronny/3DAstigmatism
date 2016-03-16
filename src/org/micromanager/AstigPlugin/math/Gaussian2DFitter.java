package org.micromanager.AstigPlugin.math;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.util.Precision;

import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;

public class Gaussian2DFitter<T extends RealType<T>> {

	private int maxIter;
	private int maxEval;
	private int[] xgrid;
	private int[] ygrid;
	private double[] Ival;
	private IntervalView<T> interval;
	
	public Gaussian2DFitter(final IntervalView<T> interval_, int maxIter_, int maxEval_) {
		interval = interval_;
		maxIter = maxIter_;
		maxEval = maxEval_;
	}
	
	private static LeastSquaresBuilder builder(EllipticalGaussian problem){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	 builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
    }
	
	private static LevenbergMarquardtOptimizer getOptimizer() { 
		// Different convergence thresholds seem to have no effect on the resulting fit, only the number of
		// iterations for convergence
		final double initialStepBoundFactor = 100;
		final double costRelativeTolerance = 1e-9;
		final double parRelativeTolerance = 1e-9;
		final double orthoTolerance = 1e-9;
		final double threshold = Precision.SAFE_MIN;
		return new LevenbergMarquardtOptimizer(initialStepBoundFactor,
				costRelativeTolerance, parRelativeTolerance, orthoTolerance, threshold);
	}
	
	private void createGrids(){
		Cursor<T> cursor = interval.cursor();
		int arraySize=(int)(interval.dimension(0)*interval.dimension(1));
		Ival = new double[arraySize];
		xgrid = new int[arraySize];
		ygrid = new int[arraySize];
		int index=0;
		while(cursor.hasNext()){
			cursor.fwd();
			xgrid[index]=cursor.getIntPosition(0);
			ygrid[index]=cursor.getIntPosition(1);
			Ival[index++]=cursor.get().getRealDouble();
		}
	}
	
	public double[] fit() {
		createGrids();
		final EllipticalGaussian eg = new EllipticalGaussian(xgrid, ygrid);
		final double[] initial = eg.getInitialGuess(interval);
		final LevenbergMarquardtOptimizer optimizer = getOptimizer();
		double[] fittedEG;
		try {
			final Optimum optimum = optimizer.optimize(
	                builder(eg)
	                .target(Ival)
	                .checkerPair(new ConvChecker2DGauss())
                    .parameterValidator(new ParamValidator2DGauss())
	                .start(initial)
	                .maxIterations(maxIter)
	                .maxEvaluations(maxEval)
	                .build()
	        );
			fittedEG = optimum.getPoint().toArray();
	        
		} catch(TooManyEvaluationsException  e){
        	return null;
		} catch(ConvergenceException e){
        	return null;
		}
		if (fittedEG[2]>5*initial[2]||fittedEG[3]>5*initial[3]) //check for extremes
			return null;
        return fittedEG;
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

			if (i >100){
				 return true;
			}
			
			iteration_ = i;
	          double[] p = previous.getPoint();
	          double[] c = current.getPoint();
	          
	          if ( Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 0.01  &&
	                  Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 0.01 &&
	                  Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.002 &&
	                  Math.abs(p[INDEX_Y0] - c[INDEX_Y0]) < 0.002 &&
	                  Math.abs(p[INDEX_SX] - c[INDEX_SX]) < 0.002 &&
	                  Math.abs(p[INDEX_SY] - c[INDEX_SY]) < 0.002 ) {
	             lastResult_ = true;
	             return true;
	          }
	        lastResult_ = false;
	        return false;
		}
	}

	private class ParamValidator2DGauss implements ParameterValidator {
		public static final int INDEX_SX = 2;
		public static final int INDEX_SY = 3;
		public static final int INDEX_I0 = 4;
		public static final int INDEX_Bg = 5;
		
		@Override
		public RealVector validate(RealVector arg) {
			if(arg.getEntry(INDEX_SX)<0){
				arg.setEntry(INDEX_SX, -arg.getEntry(INDEX_SX));
			}
			if(arg.getEntry(INDEX_SY)<0){
				arg.setEntry(INDEX_SY, -arg.getEntry(INDEX_SY));
			}
			if(arg.getEntry(INDEX_I0)<0){
				arg.setEntry(INDEX_I0, -arg.getEntry(INDEX_I0));
			}
			if(arg.getEntry(INDEX_Bg)<0){
				arg.setEntry(INDEX_Bg, -arg.getEntry(INDEX_Bg));
			}
			return arg;
		}
	}
}
