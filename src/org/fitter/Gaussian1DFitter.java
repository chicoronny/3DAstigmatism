package org.fitter;

import ij.gui.Roi;
import ij.process.ImageProcessor;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.util.Precision;
import org.data.Gaussian;

public class Gaussian1DFitter {
	
	private ImageProcessor ip;
	private Roi roi;
	private int maxIter;
	private int maxEval;
	private int[] xgrid;
	private int[] ygrid;
	private double[] Ivaly, Ivalx;
	
	public Gaussian1DFitter(ImageProcessor ip_, Roi roi_, int maxIter_, int maxEval_) {
		ip = ip_;
		roi = roi_;
		maxIter = maxIter_;
		maxEval = maxEval_;
	}

	public LeastSquaresBuilder builder(Gaussian problem) {
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
	
	private void createProjGrids() {
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
	
	public double[] fit(){
		createProjGrids();
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

			double[] fittedGx = optimumx.getPoint().toArray();
			double[] fittedGy = optimumy.getPoint().toArray();

			double[] result = new double[6];

			result[0] = fittedGx[0];
			result[1] = fittedGy[0];

			// erf is symmetrical with respect to (sx,sy)->(-sx,-sy) /// how to get the convergence for strictly
			// positive sigmas??
			//
			result[2] = Math.abs(fittedGx[1]);
			result[3] = Math.abs(fittedGy[1]);
			result[4] = fittedGy[2]+fittedGx[2];
			result[5] = fittedGy[3]+fittedGx[3];
			return result;

		} catch (TooManyEvaluationsException e) {
			System.err.println("Too many evaluations");
			return null;
		} catch (ConvergenceException e) {
			System.err.println("Do not converged");
			return null;
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

			if (Math.abs(p[INDEX_I0] - c[INDEX_I0]) < 1 
				&& Math.abs(p[INDEX_Bg] - c[INDEX_Bg]) < 0.1
				&& Math.abs(p[INDEX_X0] - c[INDEX_X0]) < 0.02 
				&& Math.abs(p[INDEX_SX] - c[INDEX_SX]) < 0.1) {
				lastResult_ = true;
				return true;
			}

			lastResult_ = false;
			return false;
		}
	}

}
