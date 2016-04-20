package org.micromanager.AstigPlugin.math;

import java.util.Arrays;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;

class Gaussian2DFitter<T extends RealType<T>> {

	private static final int INDEX_X0 = 0;
	private static final int INDEX_Y0 = 1;
	private static final int INDEX_SX = 2;
	private static final int INDEX_SY = 3;
	private static final int INDEX_I0 = 4;
	private static final int INDEX_Bg = 5;

	private final int maxIter;
	private final int maxEval;
	private int[] xgrid;
	private int[] ygrid;
	private double[] Ival;
	private final IntervalView<T> interval;
	
	public Gaussian2DFitter(final IntervalView<T> interval_, int maxIter_, int maxEval_) {
		interval = interval_;
		maxIter = maxIter_;
		maxEval = maxEval_;
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
	
	private double[] getInitialGuess(IntervalView<T> interval) {
		int PARAM_LENGTH = 6;
		double[] initialGuess = new double[PARAM_LENGTH];
	    Arrays.fill(initialGuess, 0);
   
	    CentroidFitterRA<T> cf = new CentroidFitterRA<T>(interval, 0);
	    double[] centroid = cf.fit();

		initialGuess[INDEX_X0] = centroid[INDEX_X0];
		initialGuess[INDEX_Y0] = centroid[INDEX_Y0];    
	    initialGuess[INDEX_SX] = centroid[INDEX_SX];
	    initialGuess[INDEX_SY] = centroid[INDEX_SY];
	    initialGuess[INDEX_I0] = Short.MAX_VALUE-Short.MIN_VALUE;
	    initialGuess[INDEX_Bg] = 0;
	    
		return initialGuess;
	}
	
	public double[] fit() {
		createGrids();
		final EllipticalGaussian eg = new EllipticalGaussian(xgrid, ygrid);
		final LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
		final LeastSquaresBuilder builder = new LeastSquaresBuilder();
   	 	builder.model(eg.getModelFunction(), eg.getModelFunctionJacobian());
   	 	final double[] initial = getInitialGuess(interval);
		double[] fittedEG;
		try {
			final Optimum optimum = optimizer.optimize(
	                builder
	                .target(Ival)
	                .checkerPair(new ConvChecker2DGauss())
                    .parameterValidator(new ParamValidator2DGauss())
	                .start(getInitialGuess(interval))
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
		public boolean converged(int i, PointVectorValuePair previous,
				PointVectorValuePair current) {
			if (i == iteration_)
				return lastResult_;

			iteration_ = i;
			double[] p = previous.getPoint();
			double[] c = current.getPoint();

			if (Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 0.01
					&& Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 0.01
					&& Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.001
					&& Math.abs(p[INDEX_Y0] - c[INDEX_Y0]) < 0.001
					&& Math.abs(p[INDEX_SX] - c[INDEX_SX]) < 0.002
					&& Math.abs(p[INDEX_SY] - c[INDEX_SY]) < 0.002) {
				lastResult_ = true;
				return true;
			}
			lastResult_ = false;
			return false;
		}
	}

	private class ParamValidator2DGauss implements ParameterValidator {
	
		@Override
		public RealVector validate(RealVector arg) {
			arg.setEntry(INDEX_SX, Math.abs(arg.getEntry(INDEX_SX)));
			arg.setEntry(INDEX_SY, Math.abs(arg.getEntry(INDEX_SY)));
			arg.setEntry(INDEX_I0, Math.max(arg.getEntry(INDEX_I0), 1));
			arg.setEntry(INDEX_Bg, Math.max(arg.getEntry(INDEX_Bg), 0));
			return arg;
		}
	}
}
