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
import org.micromanager.AstigPlugin.interfaces.Frame;
import org.micromanager.AstigPlugin.math.GaussianFitterZ;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.pipeline.LocalizationAllParameters;
import org.micromanager.AstigPlugin.tools.LemmingUtils;
import org.scijava.plugin.Plugin;

public class AstigFitter<T extends RealType<T>, F extends Frame<T>> extends Fitter<T> {
	
	public static final String NAME = "Astigmatism Fitter";

	public static final String KEY = "ASTIGFITTER";

	public static final String INFO_TEXT = "<html>"
			+ "Astigmatism Fitter Plugin"
			+ "</html>";
	
	private final double[] params;
	
	public AstigFitter(final int windowSize, final List<Double> list) {
		super(windowSize);
		this.params = new double[list.size()];
		for (int i =0 ; i<list.size(); i++)
			params[i]=list.get(i);
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
			GaussianFitterZ gf = new GaussianFitterZ(ip, roi, 1000, 1000, pixelDepth, params);
			double[] result = null;
			result = gf.fit();
			if (result != null){
				for (int i = 0; i < 6; i++)
					result[i] *= pixelDepth;
				if(result[5]<params[6]*10)
					found.add(new LocalizationAllParameters(result[0], result[1], result[2], result[3], result[4], result[5], result[6], 
						result[7], result[9], (int) result[8], loc.getFrame()));
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
			
			List<Double> param = LemmingUtils.readCSV(calibFileName).get("param");
			if (param.isEmpty()){ 
				IJ.error("Reading calibration file failed!");
				return null;
			}
			return new AstigFitter(windowSize, param );
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
