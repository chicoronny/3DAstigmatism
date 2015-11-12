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
import org.micromanager.AstigPlugin.gui.FitterPanel;
import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.math.CentroidFitterRA;
import org.micromanager.AstigPlugin.pipeline.LocalizationPrecision3D;
import org.micromanager.AstigPlugin.pipeline.Fitter;
import org.micromanager.AstigPlugin.pipeline.Localization;
import org.scijava.plugin.Plugin;

public class CentroidFitter<T extends RealType<T>> extends Fitter<T> {
	
	public static final String NAME = "Centroid Fitter";

	public static final String KEY = "CENTROIDFITTER";

	public static final String INFO_TEXT = "<html>"
			+ "Centroid Fitter Plugin"
			+ "</html>";

	private double thresh;

	public CentroidFitter(int windowSize, double threshold_) {
		super(windowSize);
		thresh = threshold_;
	}

	@Override
	public List<Element> fit(List<Element> sliceLocs,
			RandomAccessibleInterval<T> pixels, long windowSize,
			long frameNumber, double pixelDepth) {
		
		final RandomAccessible<T> source = Views.extendZero(pixels);
		List<Element> found = new ArrayList<Element>();
		int halfKernel = size / 2;
		
		for (Element el : sliceLocs) {
			final Localization loc = (Localization) el;
			
			double x = loc.getX()/pixelDepth;
			double y = loc.getY()/pixelDepth;

			final Interval roi = new FinalInterval(new long[] { (long) StrictMath.floor(x - halfKernel),
					(long) StrictMath.floor(y - halfKernel) }, new long[] { (long) StrictMath.ceil(x + halfKernel),
					(long) StrictMath.ceil(y + halfKernel) });
			IntervalView<T> interval = Views.interval(source, roi);

			CentroidFitterRA<T> cf = new CentroidFitterRA<T>(interval, thresh);
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

		private Map<String, Object> settings = new HashMap<String, Object>();
		private FitterPanel configPanel = new FitterPanel();

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
			final int windowSize = (Integer) settings.get( FitterPanel.KEY_WINDOW_SIZE );
			final double threshold = (Double) settings.get( FitterPanel.KEY_CENTROID_THRESHOLD );
			return new CentroidFitter<T>(windowSize, threshold);
		}

		@Override
		public ConfigurationPanel getConfigurationPanel() {
			configPanel.setName(KEY);
			return configPanel;
		}
		
	}

}