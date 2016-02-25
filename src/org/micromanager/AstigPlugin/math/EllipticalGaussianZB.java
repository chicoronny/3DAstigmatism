package org.micromanager.AstigPlugin.math;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.util.FastMath;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

public class EllipticalGaussianZB implements OptimizationData {
	
	int[] xgrid, ygrid;
	PolynomialSplineFunction psx;
	PolynomialSplineFunction psy;
	double[] initialGuess;
	private Double z0;

	public static int INDEX_X0 = 0;
	public static int INDEX_Y0 = 1;
	public static int INDEX_Z0 = 2;
	public static int INDEX_I0 = 3;
	public static int INDEX_Bg = 4;
	public static int PARAM_LENGTH = 5;
	
	static double defaultSigma = 1.5;
	private static double sqrt2 = FastMath.sqrt(2);
	
	public EllipticalGaussianZB(int[] xgrid, int[] ygrid, Map<String,Object> params){
		this.xgrid = xgrid;
		this.ygrid = ygrid;
		psx = (PolynomialSplineFunction) params.get("psx");
		psy = (PolynomialSplineFunction) params.get("psy");
		z0 = (Double) params.get("z0");
	}
	
    public double getValue(double[] parameter, double x, double y) {
        return parameter[INDEX_I0]*Ex(x,parameter)*Ey(y,parameter)+parameter[INDEX_Bg];
    }
    
    public MultivariateVectorFunction getModelFunction() {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] parameter) throws IllegalArgumentException {
                double[] retVal = new double[xgrid.length];
                for(int i = 0; i < xgrid.length; i++) {
                    retVal[i] = getValue(parameter, xgrid[i], ygrid[i]);
                }
                return retVal;
            }
        };
    }
    
    public MultivariateMatrixFunction getModelFunctionJacobian() {
        return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {

            	 double[][] jacobian = new double[xgrid.length][PARAM_LENGTH];
            	 
        	     for (int i = 0; i < xgrid.length; ++i) {
        	    	 double ex = Ex(xgrid[i], point);
        	    	 double ey = Ey(ygrid[i], point);
        	    	 jacobian[i][INDEX_X0] = point[INDEX_I0]*ey*dEx(xgrid[i],point);
        	    	 jacobian[i][INDEX_Y0] = point[INDEX_I0]*ex*dEy(ygrid[i],point); 
        	    	 jacobian[i][INDEX_Z0] = point[INDEX_I0]*
        	    			 (dEsx(xgrid[i],point)*ey*dSx(point[INDEX_Z0])+
	    					 ex*dEsy(ygrid[i], point)*dSy(point[INDEX_Z0]));
        	    	 jacobian[i][INDEX_I0] = ex*ey;
        	    	 jacobian[i][INDEX_Bg] = 1;
        	     }
        	     return jacobian;
            }
        };
    }
 
    public <T extends RealType<T>> double[] getInitialGuess(RandomAccessibleInterval<T> ip, Interval roi) {
		initialGuess = new double[PARAM_LENGTH];
	    Arrays.fill(initialGuess, 0);
	    
        // compute min and max of the Image
	    final T max = LemmingUtils.computeMax( Views.iterable(ip));
	    final T min = LemmingUtils.computeMin( Views.iterable(ip));
	    
	    CentroidFitterRA<T> cf = new CentroidFitterRA<T>(Views.interval(ip, roi), 0);
	    double[] centroid = cf.fit();
	    	    
	    initialGuess[INDEX_X0] = centroid[INDEX_X0];
	    initialGuess[INDEX_Y0] = centroid[INDEX_Y0];
	    initialGuess[INDEX_Z0] = z0;
	    initialGuess[INDEX_I0] = max.getRealDouble()-min.getRealDouble();
	    initialGuess[INDEX_Bg] = min.getRealDouble();
	    
		return initialGuess;
	}

	// /////////////////////////////////////////////////////////////
	// Math functions
	private static double erf(double x) {
		return LemmingUtils.erf(x);
	}

	private static double dErf(double x) {
		return 2 * FastMath.exp(-x * x) / FastMath.sqrt(FastMath.PI);
	}

	public double Ex(double x, double[] variables) {
		double tsx = sqrt2 * Sx(variables[INDEX_Z0]);
		double xm = x - variables[INDEX_X0] - 0.5;
		double xp = x - variables[INDEX_X0] + 0.5;
		return 0.5 * erf(xp / tsx) - 0.5 * erf(xm / tsx);
	}

	public double Ey(double y, double[] variables) {
		double tsy = sqrt2 * Sy(variables[INDEX_Z0]);
		double ym = y - variables[INDEX_Y0] - 0.5;
		double yp = y - variables[INDEX_Y0] + 0.5;
		return 0.5 * erf(yp / tsy) - 0.5 * erf(ym / tsy);
	}

	public double dEx(double x, double[] variables) {
		double xm = x - variables[INDEX_X0] - 0.5;
		double xp = x - variables[INDEX_X0] + 0.5;
		double tsx = sqrt2 * Sx(variables[INDEX_Z0]);
		return 0.5 * (dErf(xm / tsx) - dErf(xp / tsx)) / tsx;
	}

	public double dEy(double y, double[] variables) {
		double ym = y - variables[INDEX_Y0] - 0.5;
		double yp = y - variables[INDEX_Y0] + 0.5;
		double tsy = sqrt2 * Sy(variables[INDEX_Z0]);
		return 0.5 * (dErf(ym / tsy) - dErf(yp / tsy)) / tsy;
	}

	public double dEsx(double x, double[] variables) {
		double tsx = sqrt2 * Sx(variables[INDEX_Z0]);
		double xm = x - variables[INDEX_X0] - 0.5;
		double xp = x - variables[INDEX_X0] + 0.5;
		return 0.5 * (xm * dErf(xm / tsx) - xp * dErf(xp / tsx))
				/ Sx(variables[INDEX_Z0]) / tsx;
	}

	public double dEsy(double y, double[] variables) {
		double tsy = sqrt2 * Sy(variables[INDEX_Z0]);
		double ym = y - variables[INDEX_Y0] - 0.5;
		double yp = y - variables[INDEX_Y0] + 0.5;
		return 0.5 * (ym * dErf(ym / tsy) - yp * dErf(yp / tsy))
				/ Sy(variables[INDEX_Z0]) / tsy;
	}

	public double Sx(double z) {
		if(psx.isValidPoint(z))
			return psx.value(z);
		return 1;
	}

	public double Sy(double z) {
		if(psy.isValidPoint(z))
			return psy.value(z);
		return 1;
	}

	public double dSx(double z) {
		if(psx.isValidPoint(z))
			return psx.derivative().value(z);
		return 1;
	}

	public double dSy(double z) {
		if(psy.isValidPoint(z))
			return -psy.derivative().value(z);
		return 1;
	}
	
	
}
