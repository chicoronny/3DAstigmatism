package org.micromanager.AstigPlugin.plugins;

import ij.IJ;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;

import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.PanelKeys;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.math.BSplines;
import org.micromanager.AstigPlugin.math.GaussianFitterZB;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.pipeline.LocalizationAllParameters;
import org.scijava.plugin.Plugin;

public class AstigFitterB<T extends RealType<T>> extends Fitter<T> {
	
	public static final String NAME = "Astigmatism Fitter";

	public static final String KEY = "ASTIGFITTER";

	public static final String INFO_TEXT = "<html>"
			+ "Astigmatism Fitter Plugin"
			+ "</html>";
	
	private final Map<String, Object> params;
	
	public AstigFitterB(final int windowSize, final Map<String,Object> params) {
		super(windowSize);
		this.params=params;
	}
	
	@Override
	public List<Element> fit(List<Element> sliceLocs,RandomAccessibleInterval<T> pixels, long windowSize, long frameNumber, double pixelDepth) {
		ImageProcessor ip = ImageJFunctions.wrap(pixels,"").getProcessor();
		List<Element> found = new ArrayList<Element>();
		int halfKernel = size / 2;
		for (Element el : sliceLocs) {
			final Localization loc = (Localization) el;
			double x = loc.getX()/pixelDepth;
			double y = loc.getY()/pixelDepth;
			final Roi origroi = new Roi(x - halfKernel, y - halfKernel, size, size);
			final Roi roi = cropRoi(ip.getRoi(),origroi.getBounds());
			GaussianFitterZB gf = new GaussianFitterZB(ip, roi, 1000, 1000, pixelDepth, params);
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
			return new AstigFitterB(windowSize, param);
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
