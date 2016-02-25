package org.micromanager.AstigPlugin.math;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.micromanager.AstigPlugin.interfaces.FitterInterface;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public class GaussianFitterZ<T extends RealType<T>> implements FitterInterface {
	
	public static final int INDEX_X0 = 0;
	public static final int INDEX_Y0 = 1;
	public static final int INDEX_Z0 = 2;
	public static final int INDEX_I0 = 3;
	public static final int INDEX_Bg = 4;
	private static final int INDEX_C = 6;
	private static final int INDEX_D = 7;
	
	private Interval roi;
	private int maxIter;
	private int maxEval;
	private int[] xgrid;
	private int[] ygrid;
	private double[] Ival;
	private double[] params;
	private double pixelSize;
	private RandomAccessibleInterval<T> ip;

	public GaussianFitterZ(RandomAccessibleInterval<T> pixels_, Interval roi_, int maxIter_, int maxEval_, double pixelSize_, double[] params_) {
		ip = pixels_;
		roi = roi_;
		maxIter = maxIter_;
		maxEval = maxEval_;
		params = params_;
		pixelSize = pixelSize_;
	}
	
	private static LeastSquaresBuilder builder(EllipticalGaussianZ problem){
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
		int rwidth = (int) roi.dimension(0);
		int rheight = (int) roi.dimension(1);
		int xstart = (int) roi.min(0);
		int ystart = (int) roi.min(1);

		xgrid = new int[rwidth*rheight];
		ygrid = new int[rwidth*rheight];
		Ival = new double[rwidth*rheight];
		RandomAccess<T> ra = ip.randomAccess();
		
		//double max = Double.NEGATIVE_INFINITY;
		for(int i=0;i<rheight;i++){
			for(int j=0;j<rwidth;j++){
				ygrid[i*rwidth+j] = i+ystart;
				xgrid[i*rwidth+j] = j+xstart;
				
				ra.setPosition(new int[]{j+xstart,j+xstart});
				Ival[i*rwidth+j]  = ra.get().getRealDouble();                 //ip.get(j+xstart,i+ystart);
				//max = Math.max(max, Ival[i*rwidth+j]);
			}
		}
		//for (int l=0; l<Ival.length;l++)
		//	Ival[l] /= max;
	}

	@Override
	public double[] fit() {
		createGrids();
		EllipticalGaussianZ eg = new EllipticalGaussianZ(xgrid, ygrid, params);
		double[] initialGuess = eg.getInitialGuess(ip,roi);
		LevenbergMarquardtOptimizer optimizer = getOptimizer();
		double[] fittedEG;
		//double[] sigmas;
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
			//sigmas = optimum.getSigma(0.01).toArray();
		} catch(Exception e){
        	return null;
		}
   
		
		double[] result = new double[10];
		double[] error = get3DError(fittedEG, eg);
		result[0] = fittedEG[0]; // X								
		result[1] = fittedEG[1]; // Y
		result[2] = fittedEG[2]; // Z
		result[3] = error[0]; // Sy
		result[4] = error[1]; // Sx
		result[5] = error[2]; // Sz
		result[6] = fittedEG[3]; // I0
		result[7] = RMS;
		result[8] = iter;
		result[9] = eval;
		return result;
	}
	
	private double[] get3DError(double[] fittedEG, EllipticalGaussianZ eg) {
		// see thunderstorm corrections
		double[] error3d = new double[3];
		
		double sx,sy, dx2, dy2,dsx2, dsy2, dz2;
		int r=0, g=2;
		double N = fittedEG[INDEX_I0];
		double b = fittedEG[INDEX_Bg];
		double a2 = pixelSize*pixelSize;
		sx = eg.Sx(fittedEG[INDEX_Z0]);
		sy = eg.Sy(fittedEG[INDEX_Z0]);
		double sigma2 = a2*sx*sy;
		double l2 = params[INDEX_C]*params[INDEX_C];
		double d2 = params[INDEX_D]*params[INDEX_D];
		double tau = 2*FastMath.PI*(b*b+r)*(sigma2*(1+l2/d2)+a2/12)/(N*a2);
		
		dsx2 = (g*sx*sx+a2/12)*(1+8*tau)/N;
		dsy2 = (g*sy*sy+a2/12)*(1+8*tau)/N;
		dx2 = (g*sx*sx+a2/12)*(16/9+4*tau)/N;
		dy2 = (g*sy*sy+a2/12)*(16/9+4*tau)/N;
		error3d[0] = FastMath.sqrt(dx2);
		error3d[1] = FastMath.sqrt(dy2);

		double z2 = fittedEG[INDEX_Z0]*fittedEG[INDEX_Z0];
		double F2 = 4*l2*z2/(l2+d2+z2)/(l2+d2+z2);
		double dF2 = (1-F2)*(dsx2/(sx*sx)+dsy2/(sy*sy));

		dz2 = dF2*(l2+d2+z2)*(l2+d2+z2)*(l2+d2+z2)*(l2+d2+z2)/(4*l2*(l2+d2-z2)*(l2-d2-z2));
		error3d[2] = FastMath.sqrt(dz2);

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
			return arg;
		}
	}

}
