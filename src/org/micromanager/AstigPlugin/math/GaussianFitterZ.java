package org.micromanager.AstigPlugin.math;

import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.util.Precision;
import org.micromanager.AstigPlugin.interfaces.FitterInterface;

import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;

public class GaussianFitterZ<T extends RealType<T>>  implements FitterInterface {
	
	public static final int INDEX_X0 = 0;
	public static final int INDEX_Y0 = 1;
	public static final int INDEX_Z0 = 2;
	public static final int INDEX_I0 = 3;
	public static final int INDEX_Bg = 4;
	public static final int INDEX_Sx = 5;
	public static final int INDEX_Sy = 6;
	public static final int INDEX_Sz = 7;
	public static final int INDEX_RMS = 8;
	public static final int INDEX_Iter = 9;
	public static final int INDEX_Eval = 10;
	
	private int maxIter;
	private int maxEval;
	private double xdetect;
	private double ydetect;
	private int[] xgrid;
	private int[] ygrid;
	private double[] Ival;
	private Map<String, Object> params;
	private double pixelSize;
	private IntervalView<T> interval;

	public GaussianFitterZ(final IntervalView<T> interval_, double x, double y, int maxIter_, int maxEval_, double pixelSize_, Map<String, Object> params_) {
		interval = interval_;
		maxIter = maxIter_;
		maxEval = maxEval_;
		params = params_;
		pixelSize = pixelSize_;
		xdetect = x;
		ydetect = y;
	}
	
	private static LeastSquaresBuilder builder(EllipticalGaussianZ eg){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	 builder.model(eg.getModelFunction(), eg.getModelFunctionJacobian());
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

	@Override
	public double[] fit() {
		createGrids();
		EllipticalGaussianZ eg = new EllipticalGaussianZ(xgrid, ygrid, params);
		double[] initialGuess = eg.getInitialGuess(interval, xdetect, ydetect);
		LevenbergMarquardtOptimizer optimizer = getOptimizer();
		double[] fittedEG;
		double RMS;
		int iter, eval;
		try {
			final Optimum optimum = optimizer.optimize(
	                builder(eg)
	                .target(Ival)
	                .checkerPair(new ConvChecker3DGauss())
	                .parameterValidator(new ParamValidator3DGauss())
	                .start(initialGuess)
	                .maxIterations(maxIter)
	                .maxEvaluations(maxEval)
	                .build()
	        );
			fittedEG = optimum.getPoint().toArray();
			RMS = optimum.getRMS();
			iter = optimum.getIterations();
			eval = optimum.getEvaluations();
		} catch(TooManyEvaluationsException e){
			System.err.println("max evaluations: "+e.getMax());
        	return null;
		} catch(DimensionMismatchException e1){
			return null;
		}
        
		double[] result = new double[11];
		double[] error = get3DError(fittedEG, eg);
		result[INDEX_X0] = fittedEG[INDEX_X0]; // X								
		result[INDEX_Y0] = fittedEG[INDEX_Y0]; // Y
		result[INDEX_Z0] = fittedEG[INDEX_Z0]; // Z
		result[INDEX_I0] = fittedEG[INDEX_I0]; // I0
		result[INDEX_Bg] = fittedEG[INDEX_Bg]; // Bg
		result[INDEX_Sx] = error[0]; // Sx
		result[INDEX_Sy] = error[1]; // Sy
		result[INDEX_Sz] = error[2]; // Sz
		result[INDEX_RMS] = RMS;
		result[INDEX_Iter] = iter;
		result[INDEX_Eval] = eval;
	
		
		return result;
	}
	
	private double[] get3DError(double[] fittedEG, EllipticalGaussianZ eg) {
		// see thunderstorm corrections
		double[] error3d = new double[3];
		
		double sx,sy, dx2, dy2;
		int r=0, g=2;
		double N = fittedEG[INDEX_I0];
		double b = fittedEG[INDEX_Bg];
		double a2 = pixelSize*pixelSize;
		sx = eg.Sx(fittedEG[INDEX_Z0]);
		sy = eg.Sy(fittedEG[INDEX_Z0]);
		double sigma2 = a2*sx*sy;
		double tau = 2*Math.PI*(b*b+r)*(sigma2+a2/12)/(N*a2);
		
		dx2 = (g*sx*sx+a2/12)*(16/9+4*tau)/N;
		dy2 = (g*sy*sy+a2/12)*(16/9+4*tau)/N;
		error3d[0] = Math.sqrt(dx2);
		error3d[1] = Math.sqrt(dy2);
		
		double[] knots = (double[]) params.get("zgrid");
		for (r=0; r<knots.length;++r)
			if(fittedEG[INDEX_Z0]<knots[r]) break;
		r = Math.max(1, r);
		r = Math.min(r, knots.length-1);
		double hx = (knots[r]-knots[r-1])/24*sx;
		double hy = (knots[r]-knots[r-1])/24*sy;
		error3d[2] = hx+hy;

		return error3d;
	}

	// Convergence Checker
	private class ConvChecker3DGauss implements ConvergenceChecker<PointVectorValuePair> {

		int iteration_ = 0;
		boolean lastResult_ = false;

		@Override
		public boolean converged(int i, PointVectorValuePair previous, PointVectorValuePair current) {
			if (i == iteration_)
				return lastResult_;

			iteration_ = i;
			double[] p = previous.getPoint();
			double[] c = current.getPoint();

			if (Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 0.1
					&& Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 0.01
					&& Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.002
					&& Math.abs(p[INDEX_Y0] - c[INDEX_Y0]) < 0.002
					&& Math.abs(p[INDEX_Z0] - c[INDEX_Z0]) < 0.01) {
				lastResult_ = true;
				return true;
			}

			lastResult_ = false;
			return false;
		}
	}

	private class ParamValidator3DGauss implements ParameterValidator {

		@Override
		public RealVector validate(RealVector arg) {
			if (arg.getEntry(INDEX_I0) < 0) {
				arg.setEntry(INDEX_I0, 0);
			}
			if (arg.getEntry(INDEX_Bg) < 0) {
				arg.setEntry(INDEX_Bg, 0);
			}
			if (arg.getEntry(INDEX_Z0) < 0) {
				arg.setEntry(INDEX_Z0, 0);
			}
			return arg;
		}
	}

}
