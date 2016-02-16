package org.micromanager.AstigPlugin.tests;

import org.micromanager.AstigPlugin.math.Calibrator;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.StackWindow;

public class CalibrationTest {
	
	private StackWindow calibWindow;
	private Calibrator calibrator;
	private static String appPath ="D:\\Images\\";
	
	public CalibrationTest(){
		ImagePlus calibImage = new ImagePlus(appPath+"set1.tif");
		calibWindow = new StackWindow(calibImage);
		calibImage.setRoi(21, 19, 21, 21);
	}
	
	private boolean fitbeads() {
		final Roi roitemp = calibWindow.getImagePlus().getRoi();
		Roi calibRoi = null;
		try {
			final double w = roitemp.getFloatWidth();
			final double h = roitemp.getFloatHeight();
			if (w != h) {
				IJ.showMessage("Needs a quadratic ROI /n(hint: press Shift).");
				return false;
			}
			calibRoi = roitemp;
		} catch (NullPointerException e) {
			calibRoi = new Roi(0, 0, calibWindow.getImagePlus().getWidth(), calibWindow.getImagePlus().getHeight());
		}

		final int zstep = 10; // set
		calibrator = new Calibrator(calibWindow.getImagePlus(), zstep, calibRoi);
		calibrator.fitStack();
		//final double[] zgrid = calibrator.getCalibration().getZgrid();
		//Arrays.sort(zgrid);

		return true;
	}

	private boolean fitCurve() {
		final int rangeMin = 205; //set
		final int rangeMax = 1080; //set
		calibrator.fitCalibrationCurve(rangeMin, rangeMax);
		return true;
	}

	private void saveCalibration() {
		calibrator.saveCalib(appPath+"set1-calib.csv");
		calibWindow.close();
		//calibrator.getCalibration().closePlotWindows();
	}

	public static void main(String[] args) {
		CalibrationTest ct = new CalibrationTest();
		ct.fitbeads();
		ct.fitCurve();
		ct.saveCalibration();
	}
}
