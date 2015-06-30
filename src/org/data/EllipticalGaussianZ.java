package org.data;

import java.util.Arrays;

import ij.gui.Roi;
import ij.process.ImageProcessor;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.special.Erf;
import org.fitter.CentroidFitter;

public class EllipticalGaussianZ {

	public static int INDEX_WX = 0;
	public static int INDEX_WY = 1;
	public static int INDEX_AX = 2;
	public static int INDEX_AY = 3;
	public static int INDEX_BX = 4;
	public static int INDEX_BY = 5;
	public static int INDEX_C = 6;
	public static int INDEX_D = 7;
	public static int PARAM_1D_LENGTH = 8;

	public static int INDEX_X0 = 0;
	public static int INDEX_Y0 = 1;
	public static int INDEX_Z0 = 2;
	public static int INDEX_I0 = 3;
	public static int INDEX_Bg = 4;
	public static int PARAM_LENGTH = 5;
	// for centroid
	public static int INDEX_SX = 2;	
	public static int INDEX_SY = 3;
	public static double defaultSigma = 1.5;
	private double[] param;

	public EllipticalGaussianZ(Calibration cal) {
		this.param = cal.getparam();
	}

	public double getValue(double[] params, double x, double y) {
		return params[INDEX_I0] * Ex(x, params) * Ey(y, params) + params[INDEX_Bg];
	}

	public MultivariateVectorFunction getModelFunction(final int[] xgrid, final int[] ygrid) {
		return new MultivariateVectorFunction() {
			@Override
			public double[] value(double[] params) throws IllegalArgumentException {
				double[] retVal = new double[xgrid.length];
				for (int i = 0; i < xgrid.length; i++) {
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
					jacobian[i][INDEX_X0] = point[INDEX_I0] * Ey(ygrid[i], point) * dEx(xgrid[i], point);
					jacobian[i][INDEX_Y0] = point[INDEX_I0] * Ex(xgrid[i], point) * dEy(ygrid[i], point);
					jacobian[i][INDEX_Z0] = point[INDEX_I0] * 
							(dEsx(xgrid[i], point) * Ey(ygrid[i], point) * dSx(point[INDEX_Z0]) 
							+ Ex(xgrid[i], point)  * dEsy(ygrid[i], point) * dSy(point[INDEX_Z0]));
					jacobian[i][INDEX_I0] = Ex(xgrid[i], point) * Ey(ygrid[i], point);
					jacobian[i][INDEX_Bg] = 1;
				}
				return jacobian;
			}
		};
	}

	// /////////////////////////////////////////////////////////////
	// Math functions
	public double erf(double x) {
		return Erf.erf(x);
	}

	public double dErf(double x) {
		return 2 * Math.exp(-x * x) / Math.sqrt(Math.PI);
	}

	public double Ex(double x, double[] variables) {
		double tsx = 1 / (Math.sqrt(2) * Sx(variables[INDEX_Z0]));
		return 0.5 * erf(tsx * (x - variables[INDEX_X0] + 0.5)) - 0.5 * erf(tsx * (x - variables[INDEX_X0] - 0.5));
	}

	public double Ey(double y, double[] variables) {
		double tsy = 1 / (Math.sqrt(2) * Sy(variables[INDEX_Z0]));
		return 0.5 * erf(tsy * (y - variables[INDEX_Y0] + 0.5)) - 0.5 * erf(tsy * (y - variables[INDEX_Y0] - 0.5));
	}

	public double dEx(double x, double[] variables) {
		double tsx = 1 / (Math.sqrt(2) * Sx(variables[INDEX_Z0]));
		return 0.5 * tsx * (dErf(tsx * (x - variables[INDEX_X0] - 0.5)) - dErf(tsx * (x - variables[INDEX_X0] + 0.5)));
	}

	public double dEy(double y, double[] variables) {
		double tsy = 1 / (Math.sqrt(2) * Sy(variables[INDEX_Z0]));
		return 0.5 * tsy * (dErf(tsy * (y - variables[INDEX_Y0] - 0.5)) - dErf(tsy * (y - variables[INDEX_Y0] + 0.5)));
	}

	public double dEsx(double x, double[] variables) {
		double tsx = 1 / (Math.sqrt(2) * Sx(variables[INDEX_Z0]));
		return 0.5 	* tsx * ((x - variables[INDEX_X0] - 0.5) * dErf(tsx * (x - variables[INDEX_X0] - 0.5))
				- (x - variables[INDEX_X0] + 0.5) * dErf(tsx * (x - variables[INDEX_X0] + 0.5))) / Sx(variables[INDEX_Z0]);
	}

	public double dEsy(double y, double[] variables) {
		double tsy = 1 / (Math.sqrt(2) * Sy(variables[INDEX_Z0]));
		return 0.5 * tsy * ((y - variables[INDEX_Y0] - 0.5) * dErf(tsy * (y - variables[INDEX_Y0] - 0.5)) 
				- (y - variables[INDEX_Y0] + 0.5) * dErf(tsy * (y - variables[INDEX_Y0] + 0.5))) / Sy(variables[INDEX_Z0]);
	}

	public double Sx(double z) {
		double b = (z - param[INDEX_C]) / param[INDEX_D];
		return param[INDEX_WX] * Math.sqrt(1 + b * b + param[INDEX_AX] * b * b * b + param[INDEX_BX] * b * b * b * b);
	}

	public double Sy(double z) {
		double b = (z - param[INDEX_C]) / param[INDEX_D];
		return param[INDEX_WY] * Math.sqrt(1 + b * b + param[INDEX_AY] * b * b * b + param[INDEX_BY] * b * b * b * b);
	}

	public double dSx(double z) {
		double value;
		double A = param[INDEX_AX];
		double B = param[INDEX_BX];
		double d = param[INDEX_D];
		double b = (z - param[INDEX_C]) / d;
		value = 0.5 * param[INDEX_WX] * param[INDEX_WX] * (2 * b / d + 3 * A * b * b / d + 4 * B * b * b * b / d) / Sx(z);
		return value;
	}

	public double dSy(double z) {
		double value;
		double A = param[INDEX_AY];
		double B = param[INDEX_BY];
		double d = param[INDEX_D];
		double b = (z - param[INDEX_C]) / d;
		value = 0.5 * param[INDEX_WY] * param[INDEX_WY] * (2 * b / d + 3 * A * b * b / d + 4 * B * b * b * b / d) / Sx(z);
		return value;
	}

	public double[] getInitialGuess(ImageProcessor ip, Roi roi) {
		double[] initialGuess = new double[PARAM_LENGTH];
		Arrays.fill(initialGuess, 0);

		double[] centroid = CentroidFitter.fitCentroidandWidth(ip, roi,
				(int) (ip.getStatistics().mean + 2 * ip.getStatistics().stdDev));

		initialGuess[INDEX_X0] = centroid[INDEX_X0];
		initialGuess[INDEX_Y0] = centroid[INDEX_Y0];

		double w0 = (param[INDEX_WX] + param[INDEX_WY]) / 2;
		double c = param[INDEX_C];
		double d = param[INDEX_D];
		initialGuess[INDEX_Z0] = d * d
				* (centroid[INDEX_SY] * centroid[INDEX_SY] - centroid[INDEX_SX] * centroid[INDEX_SX])
				/ (4 * w0 * w0 * c);
		initialGuess[INDEX_I0] = ip.getMax() - ip.getMin();
		initialGuess[INDEX_Bg] = ip.getMin();

		return initialGuess;
	}

}
