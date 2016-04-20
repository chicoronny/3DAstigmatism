package org.micromanager.AstigPlugin.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.micromanager.AstigPlugin.factories.FitterFactory;
import org.micromanager.AstigPlugin.gui.ConfigurationPanel;
import org.micromanager.AstigPlugin.gui.PanelKeys;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.math.CentroidFitterRA;
import org.micromanager.AstigPlugin.pipeline.LocalizationPrecision3D;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.micromanager.AstigPlugin.tools.LemmingUtils;
import org.scijava.plugin.Plugin;

public class CentroidFitter<T extends RealType<T>> extends Fitter<T> {
	
	private static final String NAME = "Centroid Fitter";

	private static final String KEY = "CENTROIDFITTER";

	private static final String INFO_TEXT = "<html>"
			+ "Centroid Fitter Plugin"
			+ "</html>";

	private final double threshold;

	private CentroidFitter(int windowSize, double threshold_) {
		super((int) ((windowSize-1)/2.0));
		threshold = threshold_;
	}

	@Override
	public List<Element> fit(List<Element> sliceLocs,
			RandomAccessibleInterval<T> pixels, long windowSize,
			long frameNumber, double pixelDepth) {
		
		final RandomAccessible<T> source = Views.extendZero(pixels);
        
        // compute max of the Image
        T max = LemmingUtils.computeMax( Views.iterable(pixels));
        double threshold_ = max.getRealDouble() / 100 * threshold;
		
		List<Element> found = new ArrayList<Element>();
		int halfKernel = size / 2;
		
		for (Element el : sliceLocs) {
			final Localization loc = (Localization) el;
			
			double x = loc.getX()/pixelDepth;
			double y = loc.getY()/pixelDepth;

			final Interval roi = new FinalInterval(new long[] { (long) Math.floor(x - halfKernel),
					(long) Math.floor(y - halfKernel) }, new long[] { (long) Math.ceil(x + halfKernel),
					(long) Math.ceil(y + halfKernel) });
			IntervalView<T> interval = Views.interval(source, roi);

			CentroidFitterRA<T> cf = new CentroidFitterRA<T>(interval, threshold_);
			double[] result = cf.fit();
			if (result != null){
				for (int i = 0; i < 4; i++)
					result[i] *= pixelDepth;
				found.add(new LocalizationPrecision3D(result[0], result[1], 0, result[2], result[3], 0, result[4], loc.getFrame()));
			}
		}
		
		return found;
	}
	
	@Plugin( type = FitterFactory.class, visible = true )
	public static class Factory<T extends RealType<T>> implements FitterFactory{

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
			this.settings.putAll(settings);
			return settings!=null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Fitter<T> getFitter() {
			final int windowSize = (Integer) settings.get( PanelKeys.KEY_WINDOWSIZE );
			final double threshold = (Double) settings.get( PanelKeys.KEY_THRESHOLD );
			return new CentroidFitter<T>(windowSize, threshold);
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
