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
import org.micromanager.AstigPlugin.math.BSplines;
import org.micromanager.AstigPlugin.math.GaussianFitterZ;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.pipeline.LocalizationAllParameters;
import org.scijava.plugin.Plugin;

public class AstigFitter<T extends RealType<T>> extends Fitter<T> {
	
	public static final String NAME = "Astigmatism Fitter";

	public static final String KEY = "ASTIGFITTER";

	public static final String INFO_TEXT = "<html>"
			+ "Astigmatism Fitter Plugin"
			+ "</html>";
	
	private final Map<String, Object> params;
	
	public AstigFitter(final int windowSize, final Map<String,Object> params) {
		super(windowSize);
		this.params=params;
	}
	
	@Override
	public List<Element> fit(List<Element> sliceLocs,RandomAccessibleInterval<T> pixels, long windowSize, long frameNumber, double pixelDepth) {
		List<Element> found = new ArrayList<Element>();
		int halfKernel = size;
		long[] imageMax = new long[2];
		long[] imageMin = new long[2];
		for (Element el : sliceLocs) {
			final Localization loc = (Localization) el;
			long x = Math.round(loc.getX()/pixelDepth);
			long y = Math.round(loc.getY()/pixelDepth);
			pixels.min(imageMin);
			pixels.max(imageMax);
			Interval roi = cropInterval(imageMin,imageMax,new long[]{x - halfKernel,y - halfKernel},new long[]{x + halfKernel,y + halfKernel});
			GaussianFitterZ<T> gf = new GaussianFitterZ<T>(Views.interval(pixels, roi), 1000, 1000, pixelDepth, params);
			double[] result = null;
			result = gf.fit();
			if (result != null){
				result[0] *= pixelDepth;
				result[1] *= pixelDepth;
				result[2] *= (Double)params.get("zStep");
				result[3] *= pixelDepth;
				result[4] *= pixelDepth;
				result[5] *= (Double)params.get("zStep");
				found.add(new LocalizationAllParameters(result[0], result[1], result[2], result[3], result[4], result[5], result[6], result[7], loc.getFrame()));
			}			
		}
		return found;
	}

	@Plugin( type = FitterFactory.class, visible = true )
	public static class Factory implements FitterFactory{

		
		private Map<String, Object> settings = new HashMap<String, Object>();
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
			if(settings.get(PanelKeys.KEY_CALIBRATION_FILENAME) != null)
				return true;
			return false;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Fitter getFitter() {
			final int windowSize = (Integer) settings.get( PanelKeys.KEY_WINDOWSIZE );
			final String calibFileName = (String) settings.get( PanelKeys.KEY_CALIBRATION_FILENAME );
			if (calibFileName == null){ 
				IJ.error("No Calibration File!");
				return null;
			}
			
			Map<String,Object> param = BSplines.readCSV(calibFileName);
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
