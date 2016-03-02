package org.micromanager.AstigPlugin.math;

import java.util.Arrays;

import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.util.FastMath;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

public class EllipticalGaussian implements OptimizationData {
	private int[] xgrid, ygrid;
	double[] params;
	double[] initialGuess;
	
	boolean calibration = false;

	private static int INDEX_X0 = 0;
	private static int INDEX_Y0 = 1;
	private static int INDEX_SX = 2;
	private static int INDEX_SY = 3;
	private static int INDEX_I0 = 4;
	private static int INDEX_Bg = 5;
	private static int PARAM_LENGTH = 6;
	private static double sqrt2 = FastMath.sqrt(2);
	private static double sqrtPI = FastMath.sqrt(FastMath.PI);
	
	//private static double defaultSigma = 1.5;
	
	public EllipticalGaussian(int[] xgrid, int[] ygrid){
		this.xgrid = xgrid;
		this.ygrid = ygrid;
	}
	
    public static double getValue(double[] params, double x, double y) {

        return params[INDEX_I0]*Ex(x,params)*Ey(y,params)+params[INDEX_Bg];
    }

	
    public MultivariateVectorFunction getModelFunction() {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] params_) throws IllegalArgumentException {
                double[] retVal = new double[xgrid.length];
                for(int i = 0; i < xgrid.length; i++) {
                    retVal[i] = getValue(params_, xgrid[i], ygrid[i]);
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
        	    	 jacobian[i][INDEX_SX] = point[INDEX_I0]*ey*dEsx(xgrid[i],point);
        	    	 jacobian[i][INDEX_SY] = point[INDEX_I0]*ex*dEsy(ygrid[i],point);
        	    	 jacobian[i][INDEX_I0] = ex*ey;
        	    	 jacobian[i][INDEX_Bg] = 1;
        	     }
        	     
				return jacobian;
            }
        };
    }
 
	public double[] getInitialGuess(ImageProcessor ip, Roi roi) {
		initialGuess = new double[PARAM_LENGTH];
	    Arrays.fill(initialGuess, 0);
	    ip.setRoi(roi);
	    ImageStatistics stat = ip.getStatistics();
	    
	    initialGuess[INDEX_X0] = stat.xCenterOfMass;
	    initialGuess[INDEX_Y0] = stat.yCenterOfMass;

	    initialGuess[INDEX_SX] = Math.abs(stat.skewness);
	    initialGuess[INDEX_SY] = Math.abs(stat.skewness);
	        
	    initialGuess[INDEX_I0] = Short.MAX_VALUE-Short.MIN_VALUE; 
	    initialGuess[INDEX_Bg] = 0;
		
		return initialGuess;
	}

	///////////////////////////////////////////////////////////////
	// Math functions
	private static double erf(double x) {
		return LemmingUtils.erf(x);
	}
	
	private static double dErf(double x){
		return 2*FastMath.exp(-x*x)/sqrtPI;
	}

	private static double Ex(double x, double[] variables){
		double tsx = 1/(sqrt2*variables[INDEX_SX]);
		return 0.5*erf(tsx*(x-variables[INDEX_X0]+0.5))-0.5*erf(tsx*(x-variables[INDEX_X0]-0.5));
	}
	
	private static double Ey(double y, double[] variables){
		double tsy = 1/(sqrt2*variables[INDEX_SY]);
		return 0.5*erf(tsy*(y-variables[INDEX_Y0]+0.5))-0.5*erf(tsy*(y-variables[INDEX_Y0]-0.5));
	}	
	
	private static double dEx(double x, double[] variables){
		double tsx = 1/(sqrt2*variables[INDEX_SX]);
		return 0.5*tsx*(dErf(tsx*(x-variables[INDEX_X0]-0.5))-dErf(tsx*(x-variables[INDEX_X0]+0.5)));
	}
	
	private static double dEy(double y, double[] variables){
		double tsy = 1/(sqrt2*variables[INDEX_SY]);
		return 0.5*tsy*(dErf(tsy*(y-variables[INDEX_Y0]-0.5))-dErf(tsy*(y-variables[INDEX_Y0]+0.5)));
	}
	
	private static double dEsx(double x, double[] variables){
		double tsx = 1/(sqrt2*variables[INDEX_SX]);
		return 0.5*tsx*((x-variables[INDEX_X0]-0.5)*dErf(tsx*(x-variables[INDEX_X0]-0.5))-(x-variables[INDEX_X0]+0.5)*dErf(tsx*(x-variables[INDEX_X0]+0.5)))/variables[INDEX_SX];
	}
	
	private static double dEsy(double y, double[] variables){
		double tsy = 1/(sqrt2*variables[INDEX_SY]);
		return 0.5*tsy*((y-variables[INDEX_Y0]-0.5)*dErf(tsy*(y-variables[INDEX_Y0]-0.5))-(y-variables[INDEX_Y0]+0.5)*dErf(tsy*(y-variables[INDEX_Y0]+0.5)))/variables[INDEX_SY];
	}
}
