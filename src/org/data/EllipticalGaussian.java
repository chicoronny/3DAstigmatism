package org.data;

import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.util.Arrays;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.special.Erf;
import org.fitter.CentroidFitter;
/**
*
*
*/
public class EllipticalGaussian {

	int[] xgrid, ygrid;
	double[] params;
	double[] initialGuess;
	
	boolean calibration = false;

	public static int INDEX_X0 = 0;
	public static int INDEX_Y0 = 1;
	public static int INDEX_SX = 2;
	public static int INDEX_SY = 3;
	public static int INDEX_I0 = 4;
	public static int INDEX_Bg = 5;
	public static int PARAM_LENGTH = 6;
	
	static double defaultSigma = 1.5;
	
	public EllipticalGaussian(int[] xgrid, int[] ygrid){
		this.xgrid = xgrid;
		this.ygrid = ygrid;
	}
	
    public double getValue(double[] params, double x, double y) {

        return params[INDEX_I0]*Ex(x,params)*Ey(y,params)+params[INDEX_Bg];
    }

	
    public MultivariateVectorFunction getModelFunction(final int[] xgrid, final int[] ygrid) {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) throws IllegalArgumentException {
                double[] retVal = new double[xgrid.length];
                for(int i = 0; i < xgrid.length; i++) {
                    retVal[i] = getValue(params, xgrid[i], ygrid[i]);
                }
                return retVal;
            }
        };
    }
    
    public MultivariateMatrixFunction getModelFunctionJacobian(final int[] xgrid, final int[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {

            	 double[][] jacobian = new double[xgrid.length][PARAM_LENGTH];
            	 
            	 
        	     for (int i = 0; i < xgrid.length; ++i) {
        	    	 //System.out.println("-----------");
        	    	 //System.out.println(i);
        	    	 
        	    	 jacobian[i][INDEX_X0] = point[INDEX_I0]*Ey(ygrid[i], point)*dEx(xgrid[i],point);
        	    	 //System.out.println(jacobian[i][INDEX_X0] );
        	    	 
        	    	 jacobian[i][INDEX_Y0] = point[INDEX_I0]*Ex(xgrid[i], point)*dEy(ygrid[i],point);
        	    	 //System.out.println(jacobian[i][INDEX_Y0]);
        	    	 
        	    	 jacobian[i][INDEX_SX] = point[INDEX_I0]*Ey(ygrid[i], point)*dEsx(xgrid[i],point);
        	   
        	    	 jacobian[i][INDEX_SY] = point[INDEX_I0]*Ex(xgrid[i], point)*dEsy(ygrid[i],point);
        	    	 //System.out.println(jacobian[i][INDEX_SY]);
        	    	 
        	    	 jacobian[i][INDEX_I0] = Ex(xgrid[i], point)*Ey(ygrid[i],point);
        	    	 //System.out.println(jacobian[i][INDEX_I0]);
        	    	 
        	    	 jacobian[i][INDEX_Bg] = 1;
        	    	 //System.out.println(jacobian[i][INDEX_Bg]);
        	     }
        	     
				return jacobian;
            }
        };
    }
 
	public double[] getInitialGuess(ImageProcessor ip, Roi roi) {
		initialGuess = new double[PARAM_LENGTH];
	    Arrays.fill(initialGuess, 0);
	        
	    CentroidFitter cf = new CentroidFitter();
	    double[] centroid = cf.fitThreshold(ip,roi);
	    
	    initialGuess[INDEX_X0] = centroid[INDEX_X0];
	    initialGuess[INDEX_Y0] = centroid[INDEX_Y0];

	    initialGuess[INDEX_SX] = defaultSigma;
	    initialGuess[INDEX_SY] = defaultSigma;
	        
	    initialGuess[INDEX_I0] = ip.getMax()-ip.getMin();
	    initialGuess[INDEX_Bg] = ip.getMin();
		
		return initialGuess;
	}

	public double[] checkParameter(double[] param){
		if(param[INDEX_SX]<0){
			param[INDEX_SX]=0;
		}
		if(param[INDEX_SY]<0){
			param[INDEX_SY]=0;
		}
		if(param[INDEX_I0]<0){
			param[INDEX_I0]=0;
		}
		if(param[INDEX_Bg]<0){
			param[INDEX_Bg]=0;
		}
		return param;
	}
	
	///////////////////////////////////////////////////////////////
	// Math functions
	public double erf(double x) {
		return Erf.erf(x);
	}
	
	public double dErf(double x){
		return 2*Math.exp(-x*x)/Math.sqrt(Math.PI);
	}

	public double Ex(double x, double[] variables){
		double tsx = 1/(Math.sqrt(2)*variables[INDEX_SX]);
		return 0.5*erf(tsx*(x-variables[INDEX_X0]+0.5))-0.5*erf(tsx*(x-variables[INDEX_X0]-0.5));
	}
	
	public double Ey(double y, double[] variables){
		double tsy = 1/(Math.sqrt(2)*variables[INDEX_SY]);
		return 0.5*erf(tsy*(y-variables[INDEX_Y0]+0.5))-0.5*erf(tsy*(y-variables[INDEX_Y0]-0.5));
	}	
	
	public double dEx(double x, double[] variables){
		double tsx = 1/(Math.sqrt(2)*variables[INDEX_SX]);
		return 0.5*tsx*(dErf(tsx*(x-variables[INDEX_X0]-0.5))-dErf(tsx*(x-variables[INDEX_X0]+0.5)));
	}
	
	public double dEy(double y, double[] variables){
		double tsy = 1/(Math.sqrt(2)*variables[INDEX_SY]);
		return 0.5*tsy*(dErf(tsy*(y-variables[INDEX_Y0]-0.5))-dErf(tsy*(y-variables[INDEX_Y0]+0.5)));
	}
	
	public double dEsx(double x, double[] variables){
		double tsx = 1/(Math.sqrt(2)*variables[INDEX_SX]);
		return 0.5*tsx*((x-variables[INDEX_X0]-0.5)*dErf(tsx*(x-variables[INDEX_X0]-0.5))-(x-variables[INDEX_X0]+0.5)*dErf(tsx*(x-variables[INDEX_X0]+0.5)))/variables[INDEX_SX];
	}
	
	public double dEsy(double y, double[] variables){
		double tsy = 1/(Math.sqrt(2)*variables[INDEX_SY]);
		return 0.5*tsy*((y-variables[INDEX_Y0]-0.5)*dErf(tsy*(y-variables[INDEX_Y0]-0.5))-(y-variables[INDEX_Y0]+0.5)*dErf(tsy*(y-variables[INDEX_Y0]+0.5)))/variables[INDEX_SY];
	}
}
