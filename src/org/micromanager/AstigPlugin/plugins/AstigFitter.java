package org.micromanager.AstigPlugin.plugins;

import ij.IJ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.PanelKeys;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.interfaces.LocalizationInterface;
import org.micromanager.AstigPlugin.math.GaussianFitterZ;
import org.micromanager.AstigPlugin.pipeline.Fitter;
//import org.micromanager.AstigPlugin.pipeline.LocalizationAllParameters;
import org.micromanager.AstigPlugin.pipeline.LocalizationPrecision3D;
import org.micromanager.AstigPlugin.tools.LemmingUtils;
import org.scijava.plugin.Plugin;

public class AstigFitter<T extends RealType<T>> extends Fitter<T> {
	
	private static final String NAME = "Astigmatism Fitter";

	public static final String KEY = "ASTIGFITTER";

	private static final String INFO_TEXT = "<html>"
			+ "Astigmatism Fitter Plugin"
			+ "</html>";
	
	private final Map<String, Object> params;
	
	public AstigFitter(final int windowSize, final Map<String,Object> params) {
		super((windowSize-1)/2);
		this.params=params;
	}
	
	@Override
	public List<Element> fit(List<Element> sliceLocs,RandomAccessibleInterval<T> pixels, long windowSize, long frameNumber, double pixelsize) {
		
		List<Element> found = new ArrayList<Element>();
		int halfKernel = size;
		long[] imageMax = new long[2];
		long[] imageMin = new long[2];
		double[] result;
		for (Element el : sliceLocs) {
			final LocalizationInterface loc = (LocalizationInterface) el;
			pixels.min(imageMin);
			pixels.max(imageMax);
			long xdetect = (long) (loc.getX()/pixelsize);
			long ydetect = (long) (loc.getY()/pixelsize);
			Interval roi = cropInterval(imageMin,imageMax,new long[]{xdetect - halfKernel,ydetect - halfKernel},new long[]{xdetect + halfKernel,ydetect + halfKernel});
			GaussianFitterZ<T> gf = new GaussianFitterZ<T>(Views.interval(pixels, roi), xdetect, ydetect, 200, 200, pixelsize, params);
			result = gf.fit();
			if (result != null){
				// bounds check: max deviation equals two pixels
				if(Math.abs(xdetect-result[0])<2 && Math.abs(ydetect-result[1])<2 /*&& result[9]<30 && result[10]<30*/){
					result[0] *= pixelsize; // x
					result[1] *= pixelsize; // y
					//xdetect *=  pixelsize;
					//ydetect *=  pixelsize;
					found.add(new LocalizationPrecision3D(result[0], result[1], result[2], result[5], result[6], result[7], result[3], loc.getFrame()));
					//found.add(new LocalizationAllParameters(result[0], result[1], result[2], result[3], result[4], result[5], result[6], result[7], result[8], result[9], result[10], xdetect, ydetect, loc.getFrame()));
				}
			}			
		}
		return found;
	}

	@Plugin( type = FitterFactory.class, visible = true )
	public static class Factory implements FitterFactory{

		
		private final Map<String, Object> settings = new HashMap<String, Object>();
		private ConfigurationPanel configPanel;

		@Override
		public String getInfoText() {
			return INFO_TEXT;
		}

		@Override
		public String getKey() {
			return KEY;
		}

		@Override
		public String getName() {
			return NAME;
		}


		@Override
		public boolean setAndCheckSettings(Map<String, Object> settings) {
			if(settings==null) return false;
			this.settings.putAll(settings);
			configPanel.setSettings(settings);
			return settings.get(PanelKeys.KEY_CALIBRATION_FILENAME) != null;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Fitter getFitter() {
			final int windowSize = (Integer) settings.get( PanelKeys.KEY_WINDOWSIZE );
			final String calibFileName = (String) settings.get( PanelKeys.KEY_CALIBRATION_FILENAME );										///////////////// here creates error when just calibrated and start localizing
			if (calibFileName == null){ 
				IJ.error("No Calibration File!");
				return null;
			}
			
			Map<String,Object> param = LemmingUtils.readCSV(calibFileName);
			if (param.isEmpty()){ 
				IJ.error("Reading calibration file failed!");
				return null;
			}
			return new AstigFitter(windowSize, param);
		}

		@Override
		public ConfigurationPanel getConfigurationPanel() {
			configPanel.setName(KEY);
			return configPanel;
		}

		@Override
		public void setConfigurationPanel(ConfigurationPanel panel) {
			this.configPanel = panel;
		}
		
	}


}
