package org.fitter;

import ij.gui.Roi;
import ij.process.ImageProcessor;


// Plugin classes
import org.data.Calibration;
import org.data.CalibrationCurve;
import org.data.DefocusCurve;
import org.data.EllipticalGaussian;
import org.data.EllipticalGaussianZ;
import org.data.Gaussian;
// Apache commons
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;

/**
 * Least-Squares fitting class. Based on apache.commons LSQ fitter and Levenberg-Marquardt optimization.
 * Performs 1D fit of a defocus curve and 2D fit of an elliptical gaussian.
 */
public class LSQFitter {

	// ///////////////////////////
	// Arrays for 2D fit
	int[] xgrid, ygrid; // x and y positions of the pixels
	double[] Ival, Ivalx, Ivaly; // intensity value of the pixels
	private double[] fittedEG, fittedGx, fittedGy;

	// ///////////////////////////
	// Misc
	public static int PARAM_1D_LENGTH = 8; // Number of parameters to fit in 1D (calibration curve)
	public static int PARAM_2D_LENGTH = 6; // Number of parameters to fit in 2D (elliptical gaussian)
	public static int PARAM_3D_LENGTH = 5; // Number of parameters to fit in 2D (elliptical gaussian with z)	
	
	public LSQFitter() {
	}

	// Builder
	public LeastSquaresBuilder builder(EllipticalGaussian problem) {
		LeastSquaresBuilder builder = new LeastSquaresBuilder();
		builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
	}

	public LeastSquaresBuilder builder(Gaussian problem) {
		LeastSquaresBuilder builder = new LeastSquaresBuilder();
		builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
	}

	public LeastSquaresBuilder builder(DefocusCurve problem) {
		LeastSquaresBuilder builder = new LeastSquaresBuilder();
		builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
	}

	public LeastSquaresBuilder builder(EllipticalGaussianZ problem) {
		LeastSquaresBuilder builder = new LeastSquaresBuilder();
		builder.model(problem.getModelFunction(xgrid,ygrid), problem.getModelFunctionJacobian(xgrid,xgrid));
		return builder;
	}
	
	public LeastSquaresBuilder builder(CalibrationCurve problem){
    	LeastSquaresBuilder builder = new LeastSquaresBuilder();
    	builder.model(problem.getModelFunction(), problem.getModelFunctionJacobian());
		return builder;
    }

	// / 1D fit: Defocus Curve
	public void fitCurves(double[] z, double[] wx, double[] wy, double[] param, double[] curvex, double[] curvey,
			int rStart, int rEnd, int maxIter, int maxEval) {

		double[] rangedZ = new double[rEnd - rStart + 1];
		double[] rangedWx = new double[rEnd - rStart + 1];
		double[] rangedWy = new double[rEnd - rStart + 1];

		System.arraycopy(z, rStart, rangedZ, 0, rEnd - rStart + 1);
		System.arraycopy(wx, rStart, rangedWx, 0, rEnd - rStart + 1);
		System.arraycopy(wy, rStart, rangedWy, 0, rEnd - rStart + 1);

		final CalibrationCurve problem = new CalibrationCurve(rangedZ, rangedWx, rangedWy);

		LevenbergMarquardtOptimizer optimizer = getOptimizer();

		final Optimum optimum = optimizer.optimize(builder(problem).target(problem.getTarget())
		// .checkerPair(new SimplePointChecker(10e-5, 10e-5))
		// .parameterValidator(new ParamValidatorCalibCurves())
				.start(problem.getInitialGuess()).maxIterations(maxIter).maxEvaluations(maxIter).build());

		// Copy the fitted parameters
		double[] result = optimum.getPoint().toArray();
		for (int i = 0; i < PARAM_1D_LENGTH; i++) 
			param[i] = result[i];

		// Copy the fitted curve values
		double[] values = CalibrationCurve.valuesWith(z, result);

		for (int i = 0; i < curvex.length; i++) {
			curvex[i] = values[i];
			curvey[i] = values[i + curvex.length];
		}
	}

	// ///////////////////////////////////////////////////////////////////////////
	// / 1D fit: Defocus Curve

	/*public void fit1D(double[] z, double[] w, double[] param, double[] curve, int rStart, int rEnd, int maxIter) {

		double[] rangedZ = new double[rEnd - rStart + 1];
		double[] rangedW = new double[rEnd - rStart + 1];

		fillRanged(rangedZ, z, rangedW, w, rStart, rEnd);

		final DefocusCurve problem = new DefocusCurve(rangedZ, rangedW);

		LevenbergMarquardtOptimizer optimizer = getOptimizer();

		final Optimum optimum = optimizer.optimize(builder(problem).target(rangedW)
		// .checkerPair(new SimplePointChecker<PointVectorValuePair>(10e-5, 10e-5))
				.start(problem.getInitialGuess()).maxIterations(maxIter).maxEvaluations(maxIter).build());

		// Copy the fitted parameters
		double[] result = optimum.getPoint().toArray();
		for (int i = 0; i < PARAM_1D_LENGTH; i++)
			param[i] = result[i];

		// Copy the fitted curve values
		double[] values = problem.valuesWith(z, param);
		for (int i = 0; i < curve.length; i++)
			curve[i] = values[i];

	}

	private void fillRanged(double[] rangedZ, double[] z, double[] rangedW, double[] w, int rStart, int rEnd) {
		int length = rEnd - rStart + 1;
		for (int i = 0; i < length; i++) {
			rangedZ[i] = z[rStart + i];
			rangedW[i] = w[rStart + i];
		}
	}*/

	// ///////////////////////////////////////////////////////////////////////////
	// / 2D fit: Elliptical Gaussian
	public void fit2D(ImageProcessor ip, Roi roi, double[] Wx, double[] Wy, int counter, int maxIter, int maxEval) {

		createGrids(ip, roi);
		EllipticalGaussian eg = new EllipticalGaussian(xgrid, ygrid);

		LevenbergMarquardtOptimizer optimizer = getOptimizer();

		try {
			final Optimum optimum = optimizer.optimize(builder(eg).target(Ival)
			// .checkerPair(new ConvChecker2DGauss())
					.start(eg.getInitialGuess(ip, roi)).maxIterations(maxIter).maxEvaluations(maxEval).build());

			fittedEG = optimum.getPoint().toArray();

			// ////////////////////////////////////////////////////////
			// erf is symmetrical with respect to (sx,sy)->(-sx,-sy) /// how to get the convergence for strictly
			// positive sigmas??
			//
			if (fittedEG[2] < 0) {
				Wx[counter] = -fittedEG[2];
			} else {
				Wx[counter] = fittedEG[2];
			}
			if (fittedEG[3] < 0) {
				Wy[counter] = -fittedEG[3];
			} else {
				Wy[counter] = fittedEG[3];
			}

		} catch (TooManyEvaluationsException e) {
			System.err.println("Too many evaluations" + roi);
			Wx[counter] = 0; //  is that legal??
			Wy[counter] = 0;
		}
	}

	public double[] fit2Dsingle(ImageProcessor ip, Roi roi, int maxIter, int maxEval) {

		createGrids(ip, roi);
		EllipticalGaussian eg = new EllipticalGaussian(xgrid, ygrid);

		LevenbergMarquardtOptimizer optimizer = getOptimizer();

		try {
			final Optimum optimum = optimizer.optimize(builder(eg).target(Ival).checkerPair(new ConvChecker2DGauss())
					.start(eg.getInitialGuess(ip, roi)).maxIterations(maxIter).maxEvaluations(maxEval).build());

			fittedEG = optimum.getPoint().toArray();
			double[] result = new double[4];

			result[0] = fittedEG[0];
			result[1] = fittedEG[1];
			// erf is symmetrical with respect to (sx,sy)->(-sx,-sy) /// how to get the convergence for strictly
			// positive sigmas??
			result[2] = Math.abs(fittedEG[2]);
			result[3] = Math.abs(fittedEG[3]);

			return result;

		} catch (TooManyEvaluationsException e) {
			System.err.println("Too many evaluations");
			return null;
		}
	}

	// ///////////////////////////////////////////////////////////////////////////
	// / 1D fit: Gaussian
	public double[] fit1DGsingle(ImageProcessor ip, Roi roi, int maxIter, int maxEval) {

		createProjGrids(ip, roi);
		Gaussian egx = new Gaussian(xgrid);
		Gaussian egy = new Gaussian(ygrid);

		LevenbergMarquardtOptimizer optimizer = getOptimizer();

		try {
			final Optimum optimumx = optimizer.optimize(builder(egx).target(Ivalx)
					.checkerPair(new ConvChecker1DGauss()).start(egx.getInitialGuess(ip, roi, 0))
					.maxIterations(maxIter).maxEvaluations(maxEval).build());

			final Optimum optimumy = optimizer.optimize(builder(egy).target(Ivaly)
					.checkerPair(new ConvChecker1DGauss()).start(egy.getInitialGuess(ip, roi, 1))
					.maxIterations(maxIter).maxEvaluations(maxEval).build());

			fittedGx = optimumx.getPoint().toArray();
			fittedGy = optimumy.getPoint().toArray();

			double[] result = new double[4];

			result[0] = fittedGx[0];
			result[1] = fittedGy[0];

			// erf is symmetrical with respect to (sx,sy)->(-sx,-sy) /// how to get the convergence for strictly
			// positive sigmas??
			//
			result[2] = Math.abs(fittedGx[1]);
			result[3] = Math.abs(fittedGy[1]);
			return result;

		} catch (TooManyEvaluationsException e) {
			System.err.println("Too many evaluations");
			return null;
		}
	}

	// / 3D fit: Elliptical Gaussian with Z
	public double[] fit3D(ImageProcessor ip, Roi roi, Calibration cal, int maxIter, int maxEval) {

		createGrids(ip, roi);
		EllipticalGaussianZ eg = new EllipticalGaussianZ(cal);

		LevenbergMarquardtOptimizer optimizer = getOptimizer();

		double[] results;

		try {
			final Optimum optimum = optimizer.optimize(builder(eg).target(Ival).checkerPair(new ConvChecker3DGauss())
					.parameterValidator(new ParamValidator3DGauss()).start(eg.getInitialGuess(ip, roi))
					.maxIterations(maxIter).maxEvaluations(maxEval).build());

			fittedEG = optimum.getPoint().toArray();
			results = fittedEG;

		} catch (TooManyEvaluationsException e) {
			System.out.println("Too many evaluations");
			return null;
		} catch (TooManyIterationsException e) {
			System.out.println("Too many iterations");
			return null;
		}
		return results;
	}

	private void createProjGrids(ImageProcessor ip, Roi roi) {
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();

		ygrid = new int[rheight];
		xgrid = new int[rwidth];
		Ivaly = new double[rheight];
		Ivalx = new double[rwidth];

		for (int i = 0; i < rheight; i++) {
			ygrid[i] = ystart + i;
			Ivaly[i] = 0;
			for (int j = 0; j < rwidth; j++) {
				Ivaly[i] += ip.get(xstart + j, ystart + i) / rheight;
			}
		}

		for (int i = 0; i < rwidth; i++) {
			xgrid[i] = xstart + i;
			Ivalx[i] = 0;
			for (int j = 0; j < rheight; j++) {
				Ivalx[i] += ip.get(xstart + i, ystart + j) / rwidth;
			}
		}
	}

	public void printProfiles() {
		for (int i = 0; i < Ivalx.length; i++) {
			System.out.println(Ivalx[i]);
		}
		System.out.println("-");
		for (int i = 0; i < xgrid.length; i++) {
			System.out.println((new Gaussian(xgrid)).getValue(fittedGx, xgrid[i]));
		}
		System.out.println("--");
		for (int i = 0; i < Ivaly.length; i++) {
			System.out.println(Ivaly[i]);
		}
		System.out.println("-");
		for (int i = 0; i < ygrid.length; i++) {
			System.out.println((new Gaussian(ygrid)).getValue(fittedGy, ygrid[i]));
		}
		System.out.println("--- " + fittedGy[0]);
		System.out.println("--- " + fittedGy[1]);
		System.out.println("--- " + fittedGy[2]);
		System.out.println("--- " + fittedGy[3]);
	}

	// ///////////////////////////////////////////////////////////////////////////
	// / Misc Functions
	public void createGrids(ImageProcessor ip, Roi roi) {
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();

		xgrid = new int[rwidth * rheight];
		ygrid = new int[rwidth * rheight];
		Ival = new double[rwidth * rheight];
		for (int i = 0; i < rheight; i++) {
			for (int j = 0; j < rwidth; j++) {
				ygrid[i * rwidth + j] = i + ystart;
				xgrid[i * rwidth + j] = j + xstart;
				Ival[i * rwidth + j] = ip.get(j + xstart, i + ystart);
			}
		}
	}

	public LevenbergMarquardtOptimizer getOptimizer() {
		return new LevenbergMarquardtOptimizer();
	}

	// Convergence Checker

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

			if (Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 5 
				&& Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 1
				&& Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.02 
				&& Math.abs(p[INDEX_Y0] - c[INDEX_Y0]) < 0.02
				&& Math.abs(p[INDEX_Z0] - c[INDEX_Z0]) < 0.08) {
				lastResult_ = true;
				return true;
			}

			lastResult_ = false;
			return false;
		}
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

			if (Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 10 
				&& Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 2
				&& Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.1 
				&& Math.abs(p[INDEX_Y0] - c[INDEX_Y0]) < 0.1
				&& Math.abs(p[INDEX_SX] - c[INDEX_SX]) < 3 
				&& Math.abs(p[INDEX_SY] - c[INDEX_SY]) < 3) {
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

			if (Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 5 
				&& Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 1
				&& Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.02 
				&& Math.abs(p[INDEX_SX] - c[INDEX_SX]) < 0.1) {
				lastResult_ = true;
				return true;
			}

			lastResult_ = false;
			return false;
		}
	}

	private class ParamValidator3DGauss implements ParameterValidator {
		public static final int INDEX_I0 = 3;
		public static final int INDEX_Bg = 4;

		@Override
		public RealVector validate(RealVector arg) {
			if (arg.getEntry(INDEX_I0) < 0) {
				arg.setEntry(INDEX_I0, 0);
			}
			if (arg.getEntry(INDEX_Bg) < 0) {
				arg.setEntry(INDEX_Bg, 0);
			}
			return arg;
		}

	}

}
